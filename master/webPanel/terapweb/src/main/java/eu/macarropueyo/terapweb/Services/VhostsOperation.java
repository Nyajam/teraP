package eu.macarropueyo.terapweb.Services;

import java.util.Optional;
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.*;

@Component
public class VhostsOperation
{
    @Autowired
    private VhostRepository vhRepo;

    @Autowired
    private VmRepository vmRepo;

    @Autowired
    private LogHostRepository logRepo;

    /**
     * Crea un nuevo vhost
     * @param ip direccion de red
     * @param cores nucleos de cpu
     * @param freq frecuencia de cpu
     * @param mem memoria principal en MiB
     * @param user usuario con el que acceder al sistema
     * @param pass password del usuario para crear vinculo de claves
     * @return
     * Retorna el nuevo Vhost si ha sido posible crearlo (null en caso contrario)
     */
    public Vhost addVhost(String ip, int cores, int freq, int mem, String user, String pass)
    {
        Vhost tmp = null;
        try
        {
            tmp = vhRepo.save(new Vhost(ip, cores, freq, mem));
            //Llamar a servicio interno que comparta las claves ssh
        }
        catch(Exception e)
        {
            tmp = null;
        }
        return tmp;
    }

    /**
     * Borra un vhost, no quita las claves ssh
     * @param ip
     * @return
     * True si ha sido posible, false si no existe o tiene VMs
     */
    public boolean removeVhost(Vhost vhost)
    {
        if(vhost==null)
            return false;
        if(!vhost.vms.isEmpty())
            return false;
        vhRepo.delete(vhost);
        return true;
    }

    /**
     * Busca un Vhost a partir de su direccion
     * @param ip
     * @return
     * Null en caso de no existir
     */
    public Vhost getVhost(String ip)
    {
        Optional<Vhost> tmp = vhRepo.findByIp(ip);
        if(tmp.isEmpty())
            return null;
        return tmp.get();
    }

    /**
     * Actualiza el host especificado, operacion bloqueante
     * @param vh
     */
    public void updateVhost(Vhost vh)
    {
        //Solicita al servicio interno actualizar el equipo
        vh.lastUpdate.setTime(new java.util.Date().getTime());
        vhRepo.save(vh);
    }

    /**
     * Comprueba el estado del vhost, operacion bloqueante
     * @param vh
     */
    public void checkStatus(Vhost vh)
    {
        //Solicita al servicio interno que compruebe el estado del vhost y actualice la base de datos
        vh.lastCheck.setTime(new java.util.Date().getTime());
        vhRepo.save(vh);
    }

    /**
     * Habilita el modo mantenimiento del vhost.
     * @param vh
     */
    public void maintenance(Vhost vh)
    {
        //Notifica al servicio interno cambios en este host
        vh.status=StatusHost.MAINTENANCE;
        vhRepo.save(vh);
    }

    /**
     * Cierra el vhost, no permite asignar nuevas vm.
     * @param vh
     */
    public void lock(Vhost vh)
    {
        //Notifica al servicio interno cambios en este host
        vh.status=StatusHost.CLOSE;
        updateVhost(vh);
    }

    /**
     * Retorna el estado del vhost a lo mas perfecto que el se encuentre.
     * @param vh
     */
    public void clear(Vhost vh)
    {
        checkStatus(vh);
        //Estados marcados por la aplicacion web
        if(vh.status==StatusHost.MAINTENANCE||vh.status==StatusHost.CLOSE||vh.status==StatusHost.EXPULSE)
        {
            vh.status=StatusHost.OK;
            vhRepo.save(vh);
        }
    }

    /**
     * Expulsa a todas las vm a otros vhosts, si no los hubiera, espera a que aparezca un hueco.
     * @param vh
     */
    public void expulse(Vhost vh)
    {
        //Operaciones realizadas por el servicio interno, este metodo, solo actualiza el estado y notifica de cambios al servicio interno
        vh.status=StatusHost.EXPULSE;
        vhRepo.save(vh);
        for (VM vm : vh.vms)
        {
            Vhost target = findFreeVhost(vm);
            if(target==null)
                continue;
            move(target, vm);
        }
        if(!vh.vms.isEmpty()) //Si no se han podido mover todos las vm, el servicio interno lo tendra en cuenta
        {}
    }

    /**
     * Mueve una vm de un vhost a otro especificado
     * @param dest
     * Vhost destino de mover la vm
     * @param vm
     * Vm a mover, de ella se obtiene su vhost actual
     */
    public void move(Vhost dest, VM vm)
    {
        //Operacion solicitada al servicio interno, el actualiza la db
        if(dest.fitVm(vm)) //Entra en el host
        {
            Vhost origen = vm.host;
            origen.vms.remove(vm);
            dest.vms.add(vm);
            vm.host=dest;
            vhRepo.save(origen);
            vhRepo.save(dest);
            vmRepo.save(vm);
        }
    }

    /**
     * @deprecated
     * Busca un vhost libre donde entre la vm. Metodo del servicio interno, no tiene que estar en la web.
     * @param vm
     * Vm que describe las caracteristicas requeridas.
     * @return
     * Vhost libre valido, null si no hubiera.
     */
    public Vhost findFreeVhost(VM vm)
    {
        List<Vhost> hosts = vhRepo.findAll();
        hosts.removeIf(a->( !a.acceptVM() || !a.fitVm(vm) )); //Quita de la lista a los hosts que no estan disponibles o donde no cabe la vm

        /*
         * Comparar en que host tiene mayor tasa de ocupación (con la vm en cuestion incluida).
         * Los porcentajes se determminan mediante un T-Conorma entre el uso de procesador y la memoria.
         * La T-Conorma empleada es el Maximo.
         */
        double ocupacion = 0.0;
        Vhost candidato = null;
        for (Vhost host : hosts)
        {
            double hostPercent = 0.0;
            int hostOcu[] = host.getUse(); //obtener su ocupacion actual

            //sumar la esperada
            hostOcu[0] = hostOcu[0]+vm.cores;
            hostOcu[1] = hostOcu[1]+vm.mem;
            hostPercent = Math.max((double)hostOcu[0]/host.cores, (double)hostOcu[1]/host.mem); //T-Conorma

            //comprobar la ocupacion
            if(hostPercent>ocupacion)
            {
                candidato = host;
                ocupacion = hostPercent;
            }
        }
        return candidato;
    }

    /**
     * Retorna el maximo de cores que puede tener una vm.
     * @return
     */
    public int getMaxCores()
    {
        int max = 0;
        for (Vhost vh : vhRepo.findAll())
            if(vh.cores>max)
                max=vh.cores;
        return max;
    }

    /**
     * Retorna el maximo de memoria que puede tener una vm.
     * @return
     */
    public int getMaxMem()
    {
        int max = 0;
        for (Vhost vh : vhRepo.findAll())
            if(vh.mem>max)
                max=vh.mem;
        return max;
    }

    /**
     * Retorna frecuencia maxima de cpu que puede tener una vm.
     * @return
     */
    public int getMaxFreq()
    {
        int max = 0;
        for (Vhost vh : vhRepo.findAll())
            if(vh.freq>max)
                max=vh.freq;
        return max;
    }

    /**
     * Rotorna todo el log de un vhost
     * @param vhost
     * @return
     */
    public List<LogHost> getLog(Vhost vhost)
    {
        return logRepo.findByVhostOrderByDateDesc(vhost);
    }

    /**
     * Retorna los ultimos logs de un vhost especificado (indicados por parametro)
     * @param vhost
     * @param lasts
     * los n ultimos logs
     * @return
     */
    public List<LogHost> getLog(Vhost vhost, int lasts)
    {
        return getLog(vhost).subList(0, lasts);
    }

    /**
     * Retorna todos los logs de un host posteriores a una fecha
     * @param vhost
     * @param at
     * de esta fecha en adelante son los logs solicitados
     * @return
     */
    public List<LogHost> getLog(Vhost vhost, Date at)
    {
        List<LogHost> tmp = getLog(vhost);
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Rotorna todo el log de todos los hosts
     * @return
     */
    public List<LogHost> getLog()
    {
        return logRepo.findAllByOrderByDateAsc();
    }

    /**
     * Retorna todos los logs posteriores a una fecha
     * @param vhost
     * @param at
     * de esta fecha en adelante son los logs solicitados
     * @return
     */
    public List<LogHost> getLog(Date at)
    {
        List<LogHost> tmp = getLog();
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Retorna la lista de hosts.
     * @return
     */
    public List<Vhost> getListVhosts()
    {
        return vhRepo.findAll();
    }

    /**
     * Actualiza la ip de un host.
     * @param vhost
     * @param ip
     */
    public void updateIp(Vhost vhost, String ip)
    {
        vhost.ip=ip;
        vhRepo.save(vhost);
    }

    /**
     * Actualiza los nucleos de un host.
     * @param vhost
     * @param cores
     */
    public void updateCores(Vhost vhost, int cores)
    {
        vhost.cores=cores;
        vhRepo.save(vhost);
    }

    /**
     * Actualiza los memoria de un host.
     * @param vhost
     * @param mem
     */
    public void updateMem(Vhost vhost, int mem)
    {
        vhost.mem=mem;
        vhRepo.save(vhost);
    }
}
