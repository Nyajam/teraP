package eu.macarropueyo.terapweb.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.DiskExpansionRepository;
import eu.macarropueyo.terapweb.Repository.VmRepository;

@Component
public class VmOperation
{
    @Autowired
    private VmRepository vmRepo;
    @Autowired
    private SystemOperation sysop;
    @Autowired
    private StorageOperation stgop;
    @Autowired
    private DiskExpansionRepository deRepo;
    

    /**
     * Create a VM without definition
     * @param group owner of the vm
     * @param name of the vm (only for the users)
     * @param cores of CPU of the vm
     * @param freq of CPU of the vm
     * @param mem RAM of the vm in MiB
     * @param space disk of the vm in GiB
     * @return The VM, null if had a problem
     */
    public VM newVM(Groupp group, String name, int cores, int freq, int mem, int space)
    {
        if(group == null || name == null || cores < 0 || freq < 0 || mem < 0 || space < 0)
            return null;
        String uuid = sysop.getUuid();
        if(uuid == null)
            return null;
        if(uuid.equals(""))
            return null;
        return vmRepo.save(new VM(group, uuid, name, cores, freq, mem));
    }

    /**
     * Delete a VM without definition
     * @param vm
     */
    public void removeVM(VM vm)
    {
        if(vm!=null)
            if(!vm.isDefine())
                vmRepo.delete(vm);
    }

    /**
     * Shutdown a define VM
     * @param vm
     */
    public void shutdown(VM vm)
    {
        if(vm != null)
            sysop.commandToInternalService("/shutdownvm/"+vm.getUuid());
    }

    /**
     * Force the shutdown of a define VM
     * @param vm
     */
    public void shutdownNow(VM vm)
    {
        if(vm != null)
            sysop.commandToInternalService("/forceshutdownvm/"+vm.getUuid());
    }

    /**
     * Reboot a define VM
     * @param vm
     */
    public void restart(VM vm)
    {
        if(vm != null)
            sysop.commandToInternalService("/restartvm/"+vm.getUuid());
    }

    /**
     * Startup a define VM
     * @param vm
     */
    public void start(VM vm)
    {
        if(vm != null)
            sysop.commandToInternalService("/startvm/"+vm.getUuid());
    }

    /**
     * Undefined a define VM
     * @param vm
     */
    public void undefine(VM vm)
    {
        if(vm != null)
        {
            if(vm.isDefine())
            {
                if(vm.pool.vdisks.isEmpty())
                    sysop.commandToInternalService("/undefinevm/"+vm.getUuid());
            }
        }
    }

    /**
     * Define a undefined VM
     * @param vm an undefine vm
     * @param space size of disk
     * @return true if all it's ok else false
     */
    public boolean define(VM vm, int space)
    {
        if(vm == null || space < 0)
            return false;
        return sysop.commandToInternalService("/definevm/"+vm.getUuid()+"/"+space) != null;
    }

    /**
     * Define a undefined VM in to a especific vhost
     * @param vm an undefine vm
     * @param host an vhost to define the vm
     * @param space size of disk
     * @return true if all it's ok else false
     */
    public boolean define(VM vm, Vhost host, int space)
    {
        if(vm == null || space < 0 || host == null)
            return false;
        return sysop.commandToInternalService("/definevmvh/"+host.ip+"/"+vm.getUuid()+"/"+space) != null;
    }

    /**
     * List of VM requests (no define)
     * @return The list
     */
    public List<VM> requests()
    {
        List<VM> vms = vmRepo.findAllByOrderByDateAsc();
        vms.removeIf(a -> a.isDefine());
        return vms;
    }

    /**
     * Find a VM by your uuid
     * @param uuid of the vm
     * @return null if had a proble
     */
    public VM findByUUID(String uuid)
    {
        if(uuid == null)
            return null;
        Optional<VM> tmp = vmRepo.findByUuid(uuid);
        if(tmp.isEmpty())
            return null;
        return tmp.get();
    }

    /**
     * Create a new request for a new disk
     * @param vm
     * @param size in GiB
     */
    public void newExpansion(VM vm, int size)
    {
        if(vm == null || size < 0)
            return;
        if(vm.expansion != null)
            return;
        vm.expansion = new DiskExpansion(size, vm);
        deRepo.save(vm.expansion);
        vmRepo.save(vm);
    }

    /**
     * List of request for new disk
     * @return
     */
    public List<DiskExpansion> getDiskExpansions()
    {
        return deRepo.findAll();
    }

    /**
     * Refuse a request for new disk
     * @param id The id of the request
     */
    public void refuseDisExpansion(long id)
    {
        deRepo.deleteById(id);
    }

    /**
     * Accept a request for new disk
     * @param id The id of the request
     */
    public void acceptDiskExpansion(long id)
    {
        Optional<DiskExpansion> tmp = deRepo.findById(id);
        if(tmp.isPresent())
            if(stgop.createDisk(tmp.get().vm.pool, tmp.get().expansion))
                deRepo.delete(tmp.get());
    }

    /**
     * List with all define VMs
     * @return
     */
    public List<VM> getAllVms()
    {
        List<VM> vms = vmRepo.findAllByOrderByDateAsc();
        vms.removeIf(a -> !a.isDefine());
        return vms;
    }

    /**
     * Erase the disk and remake the VM with the image
     * @param vm
     */
    public void remake(VM vm)
    {
        if(vm != null)
            sysop.commandToInternalService("/remakevm/"+vm.getUuid());
    }

    /**
     * Return the number of VMs in the cluster
     * @return
     */
    public int numberOfVMs()
    {
        return (int)vmRepo.count();
    }
}