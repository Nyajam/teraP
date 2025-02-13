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
    private LogHostRepository logRepo;
    @Autowired
    private SystemOperation sysop;

    /**
     * Create a new vhost and share ssh keys
     * @param ip address of net
     * @param cores cores of cpu
     * @param freq frequency of cpu
     * @param mem memory in MiB
     * @param user user for access to the system
     * @param password user's password (only to register)
     * @return Return the new Vhost if have been create (null if not)
     */
    public Vhost addVhost(String ip, int cores, int freq, int mem, String user, String password)
    {
        Vhost tmp = null;
        try
        {
            tmp = vhRepo.save(new Vhost(ip, cores, freq, mem, user));
            if(sysop.commandToInternalServicePost("/addhost/"+ip+"/"+user, "{\"password\":\""+password+"\"}") == null) // /addhost/<IP>/<USER> {"password":"<PASSWORD>"}
            {
                logRepo.save(new LogHost(tmp, "Error with the internal service for locate the host"));
                return null;
            }
        }
        catch(Exception e)
        {
            tmp = null;
        }
        return tmp;
    }

    /**
     * Remove a vhost, dont delete the ssh keys
     * @param vhost Host for remove, they cant have vms
     * @return True if are remove, false if not exist or have vms
     */
    public boolean removeVhost(Vhost vhost)
    {
        if(vhost==null) //The host exist
            return false;
        if(!vhost.vms.isEmpty()) //Host is empty
            return false;
        logRepo.deleteByVhost(vhost);
        vhRepo.delete(vhost); //Delete host
        sysop.commandToInternalService("/removehost/"+vhost.ip);
        return true;
    }

    /**
     * Search a host with a specific address
     * @param ip
     * @return Null if not exist
     */
    public Vhost getVhost(String ip)
    {
        Optional<Vhost> tmp = vhRepo.findByIp(ip); //search the host
        if(tmp.isEmpty()) //not found
            return null;
        return tmp.get(); //found
    }

    /**
     * Check the state of the host, lock operation
     * @param vh
     */
    public void checkStatus(Vhost vh)
    {
        sysop.commandToInternalService("/checkhost/"+vh.ip);
    }

    /**
     * Enable the maintenance mode over the host
     * @param vh
     */
    public void maintenance(Vhost vh)
    {
        vh.status=StatusHost.MAINTENANCE;
        vhRepo.save(vh);
        sysop.commandToInternalService("/changehostsatus/"+vh.ip);
    }

    /**
     * Lock the host, does not permit assign new vms.
     * @param vh
     */
    public void lock(Vhost vh)
    {
        vh.status=StatusHost.CLOSE;
        vhRepo.save(vh);
        sysop.commandToInternalService("/changehostsatus/"+vh.ip);
    }

    /**
     * Change the state of the host to the best state is possible
     * @param vh
     */
    public void clear(Vhost vh)
    {
        checkStatus(vh);
        //States of the webapp
        if(vh.status==StatusHost.MAINTENANCE||vh.status==StatusHost.CLOSE||vh.status==StatusHost.EXPULSE)
        {
            vh.status=StatusHost.OK;
            vhRepo.save(vh);
        }
    }

    /**
     * Eject all vms of the host, to other hosts. If not have other hosts, wait to find.
     * @param vh
     */
    public void expulse(Vhost vh)
    {
        vh.status=StatusHost.EXPULSE;
        vhRepo.save(vh);
        sysop.commandToInternalService("/changehostsatus/"+vh.ip);
    }

    /**
     * Move one vm to other host
     * @param dest Vhost target
     * @param vm Vm for move
     */
    public void move(Vhost dest, VM vm)
    {
        sysop.commandToInternalService("/movevmhost/"+dest.ip+"/"+vm.getUuid());
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
     * Return the biggest number of cores might have a vm
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
     * Return the biggest memory might have a vm
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
     * Return the biggest frequency might have a vm
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
     * Return all log of a host
     * @param vhost
     * @return
     */
    public List<LogHost> getLog(Vhost vhost)
    {
        return logRepo.findByVhostOrderByDateDesc(vhost);
    }

    /**
     * Return the last log of a host
     * @param vhost
     * @param lasts The last n logs
     * @return
     */
    public List<LogHost> getLog(Vhost vhost, int lasts)
    {
        return getLog(vhost).subList(0, lasts);
    }

    /**
     * Return all log of a host after of a date
     * @param vhost
     * @param at The log are after this date
     * @return
     */
    public List<LogHost> getLog(Vhost vhost, Date at)
    {
        List<LogHost> tmp = getLog(vhost);
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Return all log of all hosts
     * @return
     */
    public List<LogHost> getLog()
    {
        return logRepo.findAllByOrderByDateAsc();
    }

    /**
     * Return all log of all hosts before of a date
     * @param at The log are after this date
     * @return
     */
    public List<LogHost> getLog(Date at)
    {
        List<LogHost> tmp = getLog();
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Return a list with all hosts
     * @return
     */
    public List<Vhost> getListVhosts()
    {
        return vhRepo.findAll();
    }

    /**
     * Update the address of a host
     * @param vhost the host
     * @param ip the address
     */
    public void updateIp(Vhost vhost, String ip)
    {
        vhost.ip=ip;
        vhRepo.save(vhost);
    }

    /**
     * Update the cores of the host
     * @param vhost
     * @param cores
     */
    public void updateCores(Vhost vhost, int cores)
    {
        vhost.cores=cores;
        vhRepo.save(vhost);
    }

    /**
     * Update the memory of the host
     * @param vhost
     * @param mem memory in MiB
     */
    public void updateMem(Vhost vhost, int mem)
    {
        vhost.mem=mem;
        vhRepo.save(vhost);
    }
}
