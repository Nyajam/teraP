package eu.macarropueyo.terapweb.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.DiskExpansionRepository;
import eu.macarropueyo.terapweb.Repository.PoolRepository;
import eu.macarropueyo.terapweb.Repository.VhostRepository;
import eu.macarropueyo.terapweb.Repository.VmRepository;

@Component
public class VmOperation
{
    @Autowired
    private VmRepository vmRepo;
    @Autowired
    private PoolRepository polRepo;
    @Autowired
    private VhostsOperation vhop;
    @Autowired
    private SystemOperation sysop;
    @Autowired
    private StorageOperation stgop;
    @Autowired
    private DiskExpansionRepository deRepo;
    

    /**
     * Crea una vm sin definir, sin asignar
     * @param group
     * Grupo que crea la vm
     * @param name
     * @param cores
     * @param freq
     * @param mem
     * @param space
     * @return
     * Vm creada
     */
    public VM newVM(Groupp group, String name, int cores, int freq, int mem, int space)
    {
        return vmRepo.save(new VM(group, sysop.getUuid(), name, cores, freq, mem));
    }

    /**
     * Borra una vm no definida ni asignada
     * @param vm
     */
    public void removeVM(VM vm)
    {
        if(vm!=null)
            if(!vm.isDefine())
                vmRepo.delete(vm);
    }

    /**
     * Apaga una vm definida en un vhost
     * @param vm
     */
    public void shutdown(VM vm)
    {
        vm.token="";
        vmRepo.save(vm);
    }

    /**
     * Fuerza el apagado una vm definida en un vhost
     * @param vm
     */
    public void shutdownNow(VM vm)
    {
        vm.token="";
        vmRepo.save(vm);
    }

    /**
     * Reinicia una vm definida en un vhost
     * @param vm
     */
    public void restart(VM vm)
    {}

    /**
     * Enciende una vm definida en un vhost
     * @param vm
     */
    public void start(VM vm)
    {
        vm.token="trololo";
        vmRepo.save(vm);
    }

    /**
     * Borra la vm del sistema, quita la definicion, desasigna el vhost y elimina su pool (no puede tener discos)
     * @param vm
     */
    public void undefine(VM vm)
    {
        //La vm debe estar apagada, preguntar al servicio interno
        if(vm.pool==null)
            return;
        if(!vm.pool.vdisks.isEmpty())
            return;
        stgop.deleteTarget(vm.pool);
        vm.setDefinition(null, null, "");
        vm.setStatusIsVoid();
        vmRepo.save(vm);
    }

    /**
     * Define una vm en un vhost
     * @param vm
     */
    public void define(VM vm, int space)
    {
        Vhost host = vhop.findFreeVhost(vm);
        if(host==null)
            return;
        Pool pool = stgop.createTarget(vm);
        stgop.createDisk(pool, space);
        String definition = "<KVM>"; //Solicitar al servicio interno
        vm.setDefinition(host, pool, definition);
        vmRepo.save(vm);
    }

    /**
     * Pausa la ejecucion de una vm definida en un vhost
     * @param vm
     */
    public void pause(VM vm)
    {}

    /**
     * Reanuda de la pausa a una vm definida en un vhost
     * @param vm
     */
    public void resume(VM vm)
    {}

    /**
     * Lista de las vms pendientes de crear.
     * @return
     */
    public List<VM> requests()
    {
        List<VM> vms = vmRepo.findAllByOrderByDateAsc();
        vms.removeIf(a -> a.isDefine());
        return vms;
    }

    public VM findByUUID(String uuid)
    {
        Optional<VM> tmp = vmRepo.findByUuid(uuid);
        if(tmp.isEmpty())
            return null;
        return tmp.get();
    }

    public void newExpansion(VM vm, int size)
    {
        if(vm.expansion != null)
            return;
        vm.expansion = new DiskExpansion(size, vm);
        deRepo.save(vm.expansion);
        vmRepo.save(vm);
    }

    public List<DiskExpansion> getDiskExpansions()
    {
        return deRepo.findAll();
    }

    public void refuseDisExpansion(long id)
    {
        deRepo.deleteById(id);
    }

    public void acceptDisExpansion(long id)
    {

        Optional<DiskExpansion> tmp = deRepo.findById(id);
        if(tmp.isPresent())
        {
            stgop.createDisk(tmp.get().vm.pool, tmp.get().expansion);
            deRepo.delete(tmp.get());
        }
    }

    public List<VM> getAllVms()
    {
        List<VM> vms = vmRepo.findAllByOrderByDateAsc();
        vms.removeIf(a -> !a.isDefine());
        return vms;
    }

    /**
     * Borra el disco y vuelve a cargar la imagen
     * @param vm
     */
    public void remake(VM vm)
    {}
}