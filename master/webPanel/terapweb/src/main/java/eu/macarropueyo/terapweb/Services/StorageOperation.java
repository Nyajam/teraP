package eu.macarropueyo.terapweb.Services;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.*;

@Component
public class StorageOperation
{
    @Autowired
    private StorageRepository stRepo;
    @Autowired
    private PoolRepository polRepo;
    @Autowired
    private VdiskRepository vdRepo;
    @Autowired
    private LogStorageRepository logRepo;

    /**
     * Anade un storage.
     * @param ip
     * @param space
     * @param bandwidth
     * @param user
     * Usuario empleado en el enlace de claves, no se guarda
     * @param pass
     * Password de dicho usuario, no se guarda
     * @return
     * Storage creado, null si no ha sido posible.
     */
    public Storage addStorage(String ip, int space, int bandwidth, String user, String pass)
    {
        Optional<Storage> tmp = stRepo.findByIp(ip);
        if(!tmp.isEmpty())
            return null;
        //Solicita al servicio interno el enlace de claves
        return stRepo.save(new Storage(ip, space, bandwidth));
    }

    /**
     * Borra si esta libre (no hay vms usandolo...) un storage.
     * @param stg
     * Storage a borrar.
     * @return
     * True si ha sido posible borrarlo, false si tiene pool o vdisks
     */
    public boolean removeStorage(Storage stg)
    {
        if(!stg.vdisks.isEmpty())
            return false;
        stRepo.delete(stg);
        return true;
    }

    /**
     * Obtiene un storage por su ip
     * @param ip
     * @return
     * Null si no existe
     */
    public Storage getStorage(String ip)
    {
        Optional<Storage> tmp = stRepo.findByIp(ip);
        if(tmp.isEmpty())
            return null;
        return tmp.get();
    }

    /**
     * Actualiza un storage.
     * @param stg
     * Storage a actualizar.
     */
    public void updateStorage(Storage stg)
    {
        stRepo.save(stg);
    }

    /**
     * Solicita al servicio interno que compruebe el estado del storage.
     * @param stg
     */
    public void checkStatus(Storage stg)
    {}

    /**
     * Habilita el modo mantenimiento del storage.
     * @param stg
     */
    public void maintenance(Storage stg)
    {
        stg.status=StatusHost.MAINTENANCE;
        updateStorage(stg);
    }

    /**
     * Cierra el storage, no permite asignar nuevos discos.
     * @param stg
     */
    public void lock(Storage stg)
    {
        stg.status=StatusHost.CLOSE;
        updateStorage(stg);
    }

    /**
     * Retorna el estado del storage a lo mas perfecto que el se encuentre.
     * @param stg
     */
    public void clear(Storage stg)
    {
        checkStatus(stg);
        //Estados marcados por la aplicacion web
        if(stg.status==StatusHost.MAINTENANCE||stg.status==StatusHost.CLOSE||stg.status==StatusHost.EXPULSE)
        {
            stg.status=StatusHost.OK;
            stRepo.save(stg);
        }
    }

    /**
     * Expulsa a todos los discos a otros storage, si no los hubiera, espera a que aparezca un hueco.
     * EXPERIMENTAL. NO IMPLEMENTADO.
     * @param stg
     */
    public void expulse(Storage stg)
    {}

    /**
     * Crea el espacio de conexiones de una vm (una pool) mediante el servicio interno.
     * @param vm
     * Vm sin pool previamente creada.
     * @param space
     * Espacio estimado requerido
     * @return
     * Pool definida para esa vm.
     */
    public Pool createTarget(VM vm)
    {
        if(vm.pool==null)
        {
            //El servico tiene que propagar la pool por todos los vhosts.
            String target = "iqn.2022-08.server.domain:target-"+(int)(Math.random()*1000); //Solicitar al servicio interno
            Pool pol = new Pool(vm, target);
            polRepo.save(pol);
            return pol;
        }
        return null;
    }

    /**
     * Borra el espacio de conexiones de una pool.
     * @param pol
     */
    public void deleteTarget(Pool pol)
    {
        //La vm tiene que estar apagada, eso lo dice el servicio interno
        if(pol.vdisks.isEmpty())
            polRepo.delete(pol);
    }

    /**
     * Crea un disco en el storage.
     * @param vd
     * Disco con las especificaciones.
     */
    public boolean createDisk(Pool pool, int space)
    {
        String initiator = "iqn.2022-08.server.domain:initiator-"+(int)(Math.random()*1000); //Solicitar al servicio interno
        Storage stg = findFreeStorage(space);
        if(stg!=null)
        {
            vdRepo.save(new Vdisk(pool, stg, space, initiator));
            polRepo.save(pool); //El constructor de vdisk, modifica la pool
            return true;
        }
        return false;
    }

    /**
     * Borra un disco, de forma irrecuperable.
     * @param vd
     */
    public void deleteDisk(Vdisk vd)
    {
        //La vm tiene que estar apagada, eso lo dice el servicio interno
        //Servicio interno borra el disco
        vdRepo.delete(vd);
    }

    /**
     * Busca un storage con espacio libre suficiente
     * @param space
     * @return
     */
    public Storage findFreeStorage(int space)
    {
        for (Storage stg : stRepo.findAll())
        {
            if(stg.getFreeSpace()>=space)
                return stg;
        }
        return null;
    }

    /**
     * Return the list of storages
     * @return
     */
    public List<Storage> getAllStorages()
    {
        return stRepo.findAll();
    }

    /**
     * Return a especific storage (if exist) with the ip choose
     * @param ip
     * @return
     */
    public Optional<Storage> findStorageByIp(String ip)
    {
        return stRepo.findByIp(ip);
    }

    /**
     * Rotorna todo el log de un storage
     * @param storage
     * @return
     */
    public List<LogStorage> getLog(Storage storage)
    {
        return logRepo.findByStorageOrderByDateDesc(storage);
    }

    /**
     * Retorna los ultimos logs de un storage especificado (indicados por parametro)
     * @param storage
     * @param lasts
     * los n ultimos logs
     * @return
     */
    public List<LogStorage> getLog(Storage storage, int lasts)
    {
        return getLog(storage).subList(0, lasts);
    }

    /**
     * Retorna todos los logs de un storage posteriores a una fecha
     * @param storage
     * @param at
     * de esta fecha en adelante son los logs solicitados
     * @return
     */
    public List<LogStorage> getLog(Storage storage, Date at)
    {
        List<LogStorage> tmp = getLog(storage);
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Rotorna todo el log de todos los storages
     * @return
     */
    public List<LogStorage> getLog()
    {
        return logRepo.findAllByOrderByDateAsc();
    }

    /**
     * Retorna todos los logs posteriores a una fecha
     * @param at
     * de esta fecha en adelante son los logs solicitados
     * @return
     */
    public List<LogStorage> getLog(Date at)
    {
        List<LogStorage> tmp = getLog();
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    public void updateIp(Storage storage, String ip)
    {
        storage.ip=ip;
        stRepo.save(storage);
    }

    public void updateSpace(Storage storage, int space)
    {
        if(space<(storage.space-storage.getFreeSpace()))
            return;
        storage.space=space;
        stRepo.save(storage);
    }

    public int spaceFreeInAll()
    {
        int total = 0;
        for (Storage stg : stRepo.findAll())
            total = total + stg.getFreeSpace();
        return total;
    }
}
