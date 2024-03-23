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
    private VdiskRepository vdRepo;
    @Autowired
    private LogStorageRepository logRepo;
    @Autowired
    private SystemOperation sysop;

    /**
     * Add a storage.
     * @param ip
     * @param space
     * @param bandwidth
     * @param user User for iSCSI control
     * @return Storage created, null if not
     */
    public Storage addStorage(String ip, int space, int bandwidth, String user)
    {
        Storage tmp = null;
        try
        {
            tmp = stRepo.save(new Storage(ip, space, bandwidth, user));
            if(sysop.commandToInternalService("/addstorage/"+ip) == null)
            {
                logRepo.save(new LogStorage(tmp, "Error with the internal service to add storage"));
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
     * Remove a storage if are empty (no vdisks, no targets/pools)
     * @param stg
     * @return True if are removed, false if have vdisks or pools
     */
    public boolean removeStorage(Storage stg)
    {
        if(!stg.vdisks.isEmpty())
            return false;
        try
        {
            logRepo.deleteByStorage(stg);
            stRepo.delete(stg);
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Get a sotrage by address
     * @param ip address
     * @return Null if not exist
     */
    public Storage getStorage(String ip)
    {
        Optional<Storage> tmp = stRepo.findByIp(ip);
        if(tmp.isEmpty())
            return null;
        return tmp.get();
    }

    /**
     * Request to internal service check the state of the storage. Lock operation
     * @param stg
     */
    public void checkStatus(Storage stg)
    {
        sysop.commandToInternalService("/checkstorage/"+stg.ip);
    }

    /**
     * Enable the maintenance status over the storage
     * @param stg
     */
    public void maintenance(Storage stg)
    {
        stg.status=StatusHost.MAINTENANCE;
        stRepo.save(stg);
        sysop.commandToInternalService("/changestoragesatus/"+stg.ip);
    }

    /**
     * Close the storage. No more vdisks.
     * @param stg
     */
    public void lock(Storage stg)
    {
        stg.status=StatusHost.CLOSE;
        stRepo.save(stg);
        sysop.commandToInternalService("/changestoragesatus/"+stg.ip);
    }

    /**
     * Change the state of the host to the best state is possible
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
     * Create a vidsk in a storage
     * @param pool Pool owner of the vdisk
     * @param space Space of the disk in GiB
     * @return True if have been create
     */
    public boolean createDisk(Pool pool, int space)
    {
        if(pool == null)
            return false;
        return sysop.commandToInternalService("/createdisk/"+pool.vm.getUuid()+"/"+space) != null;
    }

    /**
     * Delete vdisk
     * @param vd
     */
    public void deleteDisk(Vdisk vd)
    {
        sysop.commandToInternalService("/deletedisk/"+vd.pool.vm.getUuid()+"/"+vd.initiator);
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
     * Return all log of a storage
     * @param storage
     * @return
     */
    public List<LogStorage> getLog(Storage storage)
    {
        return logRepo.findByStorageOrderByDateDesc(storage);
    }

    /**
     * Return the last log of a storage
     * @param storage
     * @param lasts last n logs
     * @return
     */
    public List<LogStorage> getLog(Storage storage, int lasts)
    {
        return getLog(storage).subList(0, lasts);
    }

    /**
     * Return all log of a storage after a date
     * @param storage
     * @param at The log after this date
     * @return
     */
    public List<LogStorage> getLog(Storage storage, Date at)
    {
        List<LogStorage> tmp = getLog(storage);
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Return all log of all storages
     * @return
     */
    public List<LogStorage> getLog()
    {
        return logRepo.findAllByOrderByDateAsc();
    }

    /**
     * Return all log of all storages after a date
     * @param at The log after this date
     * @return
     */
    public List<LogStorage> getLog(Date at)
    {
        List<LogStorage> tmp = getLog();
        tmp.removeIf(a -> a.date.before(at));
        return tmp;
    }

    /**
     * Change the address of a storage
     * @param storage
     * @param ip The new address
     */
    public void updateIp(Storage storage, String ip)
    {
        storage.ip=ip;
        try
        {
            stRepo.save(storage); //This attribute are protected by the db
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Change the space of a storage if it more than use
     * @param storage
     * @param space The new space
     */
    public void updateSpace(Storage storage, int space)
    {
        if(space<(storage.space-storage.getFreeSpace()))
            return;
        storage.space=space;
        stRepo.save(storage);
    }

    /**
     * Return all free space in all storages (the sum)
     * @return
     */
    public int spaceFreeInAll()
    {
        int total = 0;
        for (Storage stg : stRepo.findAll())
            total = total + stg.getFreeSpace();
        return total;
    }

    /**
     * Return all space in all storages (the sum)
     * @return
     */
    public int spaceInAll()
    {
        int total = 0;
        for (Storage stg : stRepo.findAll())
            total = total + stg.space;
        return total;
    }

    /**
     * Return how many vdisk are in the cluster.
     * @return
     */
    public int numberOfVdisks()
    {
        return (int)vdRepo.count();
    }
}
