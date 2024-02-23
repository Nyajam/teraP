package eu.macarropueyo.terapweb.Controller;

import eu.macarropueyo.terapweb.Services.*;
import eu.macarropueyo.terapweb.Model.*;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControllerAdmin
{
    @Autowired
    private UserOperation useop;
    @Autowired
    private GrouppOperation grpop;
    @Autowired
    private VhostsOperation vhop;
    @Autowired
    private StorageOperation stgop;
    @Autowired
    private SystemOperation sysop;
    @Autowired
    private VmOperation vmop;

    @RequestMapping("/admin") //Principal page of administration, it has the monitor
    public String adminPage(Model modelo, HttpServletRequest sesion)
    {
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 6);
        return "home";
    }

    @RequestMapping("/admin/hosts") //Page for manage the vhosts
    public String hostspsPage(Model modelo, HttpServletRequest sesion,
        @RequestParam Optional<String> ip,
        @RequestParam Optional<Integer> cores,
        @RequestParam Optional<Integer> mem,
        @RequestParam Optional<Integer> freq,
        @RequestParam Optional<String> userad,
        @RequestParam Optional<String> pass,
        @RequestParam Optional<String> host,
        @RequestParam Optional<String> mode,
        @RequestParam Optional<String> delete,
        @RequestParam Optional<String> newIpHost,
        @RequestParam Optional<String> newCoresHost,
        @RequestParam Optional<String> newMemHost)
    {
        //Create a vhost
        if(ip.isPresent() && cores.isPresent() && mem.isPresent() && freq.isPresent() && userad.isPresent() && pass.isPresent())
        {
            if(vhop.addVhost(ip.get(), cores.get(), freq.get(), mem.get(), userad.get() ,pass.get()) == null)
                modelo.addAttribute("msgOfSystem", "Error to add virtual host");
            else
                modelo.addAttribute("msgOfSystem", "Add virtual host successfully");
        }
        if(host.isPresent()) //Change status of vhost
        {
            Vhost victima= vhop.getVhost(host.get());
            if(victima!=null) //El vhost existe
            {
                if(delete.isPresent()) //Se desea borrar
                {
                    if(vhop.removeVhost(victima))
                        modelo.addAttribute("msgOfSystem", "Virtual host delete successfully");
                    else
                        modelo.addAttribute("msgOfSystem", "Error to remove virtual host");
                }
                else if(mode.isPresent())
                {
                    switch(mode.get())
                    {
                        case "Lock":
                            vhop.lock(victima);
                            break;
                        case "Maintenance":
                            vhop.maintenance(victima);
                            break;
                        case "Unlock":
                            if(victima.status==StatusHost.CLOSE) //Solo lo desbloquea si estaba bloqueado
                                vhop.clear(victima);
                            break;
                        case "Update":
                            vhop.updateVhost(victima);
                            break;
                    }
                }
                else if(newIpHost.isPresent())
                    vhop.updateIp(victima,newIpHost.get());
                else if(newCoresHost.isPresent())
                {
                    int size;
                    try
                    {
                        size = Integer.parseInt(newCoresHost.get());
                        vhop.updateCores(victima, size);
                    }
                    catch(Exception e)
                    {
                        modelo.addAttribute("msgOfSystem","Error, this isn't a number.");
                    }
                }
                else if(newMemHost.isPresent())
                {
                    int size;
                    try
                    {
                        size = Integer.parseInt(newMemHost.get());
                        vhop.updateMem(victima, size);
                    }
                    catch(Exception e)
                    {
                        modelo.addAttribute("msgOfSystem","Error, this isn't a number.");
                    }
                }
            }
        }
        modelo.addAttribute("headers", new String[]{"Ip", "Cores", "Memory", "Status", "Last check", "Last update", "Occupancy rate", "Change status", "Options"});
        modelo.addAttribute("logHeaders", new String[]{"Host", "Date", "Message"});
        modelo.addAttribute("log", vhop.getLog(new Date(new java.util.Date().getTime()-(24*3600000*7)))); //Todos los de la semana
        modelo.addAttribute("vhosts", vhop.getListVhosts());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 0);
        return "home";
    }

    @RequestMapping("/admin/storages") //Page for manage the storages
    public String storagePage(Model modelo, HttpServletRequest sesion,
        @RequestParam Optional<String> ip,
        @RequestParam Optional<Integer> space,
        @RequestParam Optional<Integer> bandwidth,
        @RequestParam Optional<String> userad,
        @RequestParam Optional<String> pass,
        @RequestParam Optional<String> host,
        @RequestParam Optional<String> mode,
        @RequestParam Optional<String> delete,
        @RequestParam Optional<String> newIpStorage,
        @RequestParam Optional<String> newSpace)
    {
        if(ip.isPresent() && space.isPresent() && bandwidth.isPresent() && userad.isPresent() && pass.isPresent())
        {
            if(stgop.addStorage(ip.get(), space.get(), bandwidth.get(), userad.get(), pass.get()) == null)
                modelo.addAttribute("msgOfSystem", "Error to add virtual host");
            else
                modelo.addAttribute("msgOfSystem", "Add virtual host successfully");
        }
        if(host.isPresent())
        {
            Optional<Storage> tmp = stgop.findStorageByIp(host.get());
            if(tmp.isPresent())
            {
                Storage victima = tmp.get();
                if(delete.isPresent())
                {
                    if(stgop.removeStorage(victima))
                        modelo.addAttribute("msgOfSystem","Storage delete successfully");
                    else
                        modelo.addAttribute("msgOfSystem","Error to remove storage");
                }
                else if(mode.isPresent())
                {
                    switch(mode.get())
                    {
                        case "Lock":
                            stgop.lock(victima);
                            break;
                        case "Maintenance":
                            stgop.maintenance(victima);
                            break;
                        case "Unlock":
                            if(victima.status==StatusHost.CLOSE) //Solo lo desbloquea si estaba bloqueado
                                stgop.clear(victima);
                            break;
                    }
                }
                else if(newSpace.isPresent())
                {
                    int size;
                    try
                    {
                        size = Integer.parseInt(newSpace.get());
                        stgop.updateSpace(victima, size);
                    }
                    catch(Exception e)
                    {
                        modelo.addAttribute("msgOfSystem","Error, this isn't a number.");
                    }
                }
                else if(newIpStorage.isPresent())
                    stgop.updateIp(victima, newIpStorage.get());
            }
        }
        modelo.addAttribute("headers", new String[]{"Ip", "Space", "Status", "Space free", "Occupancy rate", "Change status", "Options"});
        modelo.addAttribute("storages", stgop.getAllStorages());
        modelo.addAttribute("logHeaders", new String[]{"Host", "Date", "Message"});
        modelo.addAttribute("log", stgop.getLog(new Date(new java.util.Date().getTime()-(24*3600000*7)))); //Todos los de la semana
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 1);
        return "home";
    }

    @RequestMapping("/admin/vms") //Page for manage the VMs
    public String vmsPage(Model modelo, HttpServletRequest sesion,
    Optional<String> uuid,
    Optional<String> grantVm,
    Optional<Long> expansionId,
    Optional<String> denegateExpansion,
    Optional<String> grantExpansion,
    Optional<String> mode)
    {
        if(expansionId.isPresent())
        {
            if(denegateExpansion.isPresent())
                vmop.refuseDisExpansion(expansionId.get());
            else if(grantExpansion.isPresent())
                vmop.acceptDisExpansion(expansionId.get());
        }
        if(uuid.isPresent())
        {
            VM victima = vmop.findByUUID(uuid.get());
            if(grantVm.isPresent())
            {
                vmop.define(victima, 100);
            }
            else if(mode.isPresent())
            {
                switch (mode.get()) {
                    case "Start":
                        vmop.start(victima);
                        break;
                    case "Shutdown":
                        vmop.shutdown(victima);
                        break;
                    case "ForceShutdown":
                        vmop.shutdownNow(victima);
                        break;
                    case "REMAKE":
                        vmop.remake(victima);
                    case "DELETE":
                        for (Vdisk vd : victima.pool.vdisks)
                            stgop.deleteDisk(vd);
                        victima.pool.vdisks.clear();
                        vmop.undefine(victima);
                        vmop.removeVM(victima);
                }
            }
        }
        modelo.addAttribute("requestsHeaders", new String[]{"Applicant", "Date", "Cores", "Memory", "Status"});
        modelo.addAttribute("requests", vmop.requests());
        modelo.addAttribute("freeResourcesHeaders", new String[]{"Cores", "Memory"});
        modelo.addAttribute("freeResources", vhop.getListVhosts());
        modelo.addAttribute("freeDisk", "Free disk: "+stgop.spaceFreeInAll()+"GB");
        modelo.addAttribute("requestsDiskHeaders", new String[]{"Extra space (GiB)", "Solicitant", "Options"});
        modelo.addAttribute("requestsDisk", vmop.getDiskExpansions());
        modelo.addAttribute("vmControlHeader", new String[]{"Uuid", "Owner", "Status", "Cores", "Memory (GiB)", "Disk (GiB)", "Date", "Options"});
        modelo.addAttribute("vmControl", vmop.getAllVms());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 3);
        return "home";
    }

    @RequestMapping("/admin/users") //Page of manage the users, only USERS
    public String usersPage(Model modelo, HttpServletRequest sesion,
    Optional<String> name,
    Optional<String> field,
    Optional<String> value,
    Optional<String> delete,
    Optional<String> newUserName,
    Optional<String> newUserMail)
    {
        if(newUserName.isPresent() && newUserMail.isPresent())
            useop.addUser(newUserName.get(), newUserMail.get());
        if(name.isPresent())
        {
            User victima = useop.getUser(name.get());
            if(delete.isPresent())
                if(!useop.removeUser(name.get()))
                    modelo.addAttribute("msgOfSystem", "This user have a resources or groups and not is possible to delete");
            else if(field.isPresent() && value.isPresent())
            {
                switch (field.get())
                {
                    case "mail":
                        useop.updateMail(victima, value.get());
                        break;
                    case "root":
                        if(victima.isRoot())
                            useop.revokeAdm(victima);
                        else
                            useop.grantAdm(victima);
                        break;
                    case "status":
                        if(victima.isEnable())
                            useop.block(victima);
                        else
                            useop.unblock(victima);
                        break;
                    case "maxvms":
                        grpop.updateLimitVms(victima.myGroup, Integer.parseInt(value.get()));
                        break;
                    case "maxcores":
                        grpop.updateLimitCores(victima.myGroup, Integer.parseInt(value.get()));
                        break;
                    case "maxmem":
                        grpop.updateLimitMem(victima.myGroup, Integer.parseInt(value.get()));
                        break;
                    case "maxdisk":
                        grpop.updateLimitDisk(victima.myGroup, Integer.parseInt(value.get()));
                        break;
                }
            }
        }
        modelo.addAttribute("headers", new String[]{"Name", "Mail", "Admin", "Status", "Max VMs", "Max cores", "Max mem", "Max disk", "Options"});
        modelo.addAttribute("users", useop.getAllUsers());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 2);
        return "home";
    }

    @RequestMapping("/admin/groups") //Page of manage the grouos
    public String groupsPage(Model modelo, HttpServletRequest sesion)
    {
        modelo.addAttribute("groups", grpop.getGroups());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 4);
        return "home";
    }

    @RequestMapping("/admin/webpanel")
    public String webpanelPage(Model modelo, HttpServletRequest sesion)
    {
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 5);
        return "home";
    }

    private void comun(Model modelo, HttpServletRequest sesion)
    {
        modelo.addAttribute("userAcces",sesion.getUserPrincipal()!=null);
        modelo.addAttribute("username",sesion.getUserPrincipal().getName());
        modelo.addAttribute("title","teraP");
        modelo.addAttribute("generalPageDescription","Plataforma de virtualización en cluster");
        modelo.addAttribute("logoUrl","https://estaticos.muyinteresante.es/media/cache/1140x_thumb/uploads/images/gallery/59c4f5655bafe82c692a7052/gato-marron_0.jpg");
        modelo.addAttribute("pageName","Administration");
        modelo.addAttribute("colorBadmin","grey");
        modelo.addAttribute("admP", true); //Si ha llegado hasta aqui, es admin
        
        List<String[]> sections = new LinkedList<>();
        sections.add(new String[]{"Monitor","/admin"});
        sections.add(new String[]{"VMs","/admin/vms"});
        sections.add(new String[]{"Hosts","/admin/hosts"});
        sections.add(new String[]{"Storages","/admin/storages"});
        sections.add(new String[]{"Users","/admin/users"});
        sections.add(new String[]{"Groups","/admin/groups"});
        sections.add(new String[]{"Webpanel","/admin/webpanel"});
        modelo.addAttribute("sections",sections);

        List<String[]> statsOperators = new LinkedList<>();
        statsOperators.add(new String[]{"CLOSE", StatusHost.CLOSE.getStatus()+""});
        statsOperators.add(new String[]{"MAINTENANCE", StatusHost.MAINTENANCE.getStatus()+""});
        statsOperators.add(new String[]{"CRITIC", StatusHost.CRITIC.getStatus()+""});
        statsOperators.add(new String[]{"EXPULSE", StatusHost.EXPULSE.getStatus()+""});
        modelo.addAttribute("hostModes",statsOperators);
    }

    private void chargeTags(Model modelo, HttpServletRequest sesion, int code)
    {
        List<String[]> tags=new LinkedList<>();
        switch(code)
        { 
            case 0: //Admin Vhosts
                tags.add(new String[]{"List_of_virtual_hosts_and_create", "0"});
                tags.add(new String[]{"Log", "1"});
                break;
            case 1: //Admin Storage
                tags.add(new String[]{"List_of_storages_and_create", "3"});
                tags.add(new String[]{"Log", "4"});
                break;
            case 2: //Admin users
                tags.add(new String[]{"Users", "6"});
                break;
            case 3: //Admin VMs
                tags.add(new String[]{"Requests", "2"});
                tags.add(new String[]{"VMs", "5"});
                tags.add(new String[]{"Requests_controls", "14"}); //Cambiar de auto a manual, con auto poder establecer parametros
                tags.add(new String[]{"Set_master_image", "15"}); //Establecer una imagen para ser clonada (como????)
                break;
            case 4: //Admin groups
                tags.add(new String[]{"List_of_groups", "9"});
                tags.add(new String[]{"Edit_group", "10"});
                break;
            case 5: //Admin webpanel
                tags.add(new String[]{"Options", "11"});
                break;
            case 6: //Monitor
                tags.add(new String[]{"Graphics", "12"});
                tags.add(new String[]{"Options", "13"});
                break;
        } 
        modelo.addAttribute("tags", tags);
    }
}