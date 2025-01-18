package eu.macarropueyo.terapweb.Controller;

import eu.macarropueyo.terapweb.Services.*;
import eu.macarropueyo.terapweb.Model.*;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

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
    public String adminPage(Model modelo, Authentication sesion,
        Optional<String> nuke)
    {
        if(nuke.isPresent())
            sysop.nuke();
        if(!sysop.isNuke())
        {
            modelo.addAttribute("nuke", "ON");
            modelo.addAttribute("nukeColor", "green");
        }
        else
        {
            modelo.addAttribute("nuke", "OFF");
            modelo.addAttribute("nukeColor", "red");
        }
        //
        //STORAGES
        //
        List<Storage> storages = stgop.getAllStorages();
        int spaceFree = stgop.spaceFreeInAll();
        int allSpace = stgop.spaceInAll();
        int nVdisks = stgop.numberOfVdisks();
        double avgStg = 0;
        for (Storage stg : storages) {
            avgStg += stg.ratio();
        }
        avgStg = avgStg/storages.size();
        modelo.addAttribute("tituloGrafica", "Occupate rate of storages");
        modelo.addAttribute("stgs", storages);
        modelo.addAttribute("allSpace", allSpace);
        modelo.addAttribute("spaceInUse", allSpace-spaceFree);
        modelo.addAttribute("spaceFree", spaceFree);
        modelo.addAttribute("nDisk", nVdisks);
        modelo.addAttribute("diskVM", (double)nVdisks/(double)vmop.numberOfVMs()); //average of disks per vm
        modelo.addAttribute("averageRateStorage", avgStg);
        if(storages.size() != 0) //div over 0 protection
            modelo.addAttribute("averageDisk", nVdisks / storages.size()); //average of disks per storage
        else
            modelo.addAttribute("averageDisk", 0);
        //
        //VHOSTS & VMS
        //
        List<Vhost> vhosts = vhop.getListVhosts();
        int allCores = 0;
        int allCoresUse = 0;
        int allMem = 0;
        int allMemUse = 0;
        double hostRatioAvg = 0;
        int nVms = 0;
        for (Vhost vhost : vhosts) {
            allCores += vhost.cores;
            allCoresUse += vhost.getUse()[0];
            allMem += vhost.cores;
            allMemUse += vhost.getUse()[1];
            hostRatioAvg += vhost.ratio();
            nVms += vhost.vms.size();
        }
        hostRatioAvg = hostRatioAvg / (double)vhosts.size();
        modelo.addAttribute("hosts", vhosts);
        modelo.addAttribute("allCores", allCores);
        modelo.addAttribute("allCoresUse", allCoresUse);
        modelo.addAttribute("allMem", allMem);
        modelo.addAttribute("allMemUse", allMemUse);
        modelo.addAttribute("hostRatioAvg", hostRatioAvg);
        modelo.addAttribute("nVms", nVms);
        if(vhosts.size() != 0) //div over 0 protection
            modelo.addAttribute("avgVms", nVms / (double)vhosts.size());
        else
            modelo.addAttribute("avgVms", 0);
        if(nVms != 0) //div over 0 protection
        {
            modelo.addAttribute("avgVmCore", allCoresUse / nVms);
            modelo.addAttribute("avgVmMem", allMemUse / nVms);
            modelo.addAttribute("avgVmDisk", (allSpace-spaceFree) / nVms);
        }
        else
        {
            modelo.addAttribute("avgVmCore", 0);
            modelo.addAttribute("avgVmMem", 0);
            modelo.addAttribute("avgVmDisk", 0);
        }
        //
        //GEBERAL DATA
        //
        List<Vhost> vProblems = new LinkedList<>(vhosts);
        List<Vhost> vNotOperative = new LinkedList<>(vhosts);
        vProblems.removeIf(a -> !a.status.needAtention());
        vNotOperative.removeIf(a -> a.status.acceptEntries());
        List<Storage> sProblems = new LinkedList<>(storages);
        List<Storage> sNotOperative = new LinkedList<>(storages);
        sProblems.removeIf(a -> !a.status.needAtention());
        sNotOperative.removeIf(a -> a.status.acceptEntries());
        List<VM> vmProblems = vmop.getAllVms();
        vmProblems.removeIf(a -> !a.status.isAnError());
        modelo.addAttribute("nUsers", useop.getAllUsers().size());
        modelo.addAttribute("nGroups", grpop.getGroups().size());
        modelo.addAttribute("nSesion", -1); //Pendiente, es dificil y lo más probable es que toque cambiarlo con el security
        modelo.addAttribute("alerts", vProblems.size()+sProblems.size()+vmProblems.size());
        modelo.addAttribute("pHosts", vProblems.size());
        modelo.addAttribute("noHosts", vNotOperative.size());
        modelo.addAttribute("pStorages", sProblems.size());
        modelo.addAttribute("noStorages", sNotOperative.size());

        comun(modelo, sesion);
        chargeTags(modelo, sesion, 6);
        return "admin";
    }

    @RequestMapping("/admin/hosts") //Page for manage the vhosts
    public String hostspsPage(Model modelo, Authentication sesion,
        @RequestParam Optional<String> ip,
        @RequestParam Optional<Integer> cores,
        @RequestParam Optional<Integer> mem,
        @RequestParam Optional<Integer> freq,
        @RequestParam Optional<String> userad,
        @RequestParam Optional<String> host,
        @RequestParam Optional<String> mode,
        @RequestParam Optional<String> delete,
        @RequestParam Optional<String> newIpHost,
        @RequestParam Optional<String> newCoresHost,
        @RequestParam Optional<String> newMemHost)
    {
        //Create a vhost
        if(ip.isPresent() && cores.isPresent() && mem.isPresent() && freq.isPresent() && userad.isPresent())
        {
            if(vhop.addVhost(ip.get(), cores.get(), freq.get(), mem.get(), userad.get()) == null)
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
        modelo.addAttribute("headers", new String[]{"Ip", "Cores", "Memory", "Status", "Last check", "Occupancy rate", "Change status", "Options"});
        modelo.addAttribute("logHeaders", new String[]{"Host", "Date", "Message"});
        modelo.addAttribute("log", vhop.getLog(new Date(new java.util.Date().getTime()-(24*3600000*7)))); //Todos los de la semana
        modelo.addAttribute("vhosts", vhop.getListVhosts());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 0);
        return "admin";
    }

    @RequestMapping("/admin/storages") //Page for manage the storages
    public String storagePage(Model modelo, Authentication sesion,
        @RequestParam Optional<String> ip,
        @RequestParam Optional<Integer> space,
        @RequestParam Optional<Integer> bandwidth,
        @RequestParam Optional<String> userad,
        @RequestParam Optional<String> host,
        @RequestParam Optional<String> mode,
        @RequestParam Optional<String> delete,
        @RequestParam Optional<String> newIpStorage,
        @RequestParam Optional<String> newSpace)
    {
        if(ip.isPresent() && space.isPresent() && bandwidth.isPresent() && userad.isPresent())
        {
            if(stgop.addStorage(ip.get(), space.get(), bandwidth.get(), userad.get()) == null)
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
        return "admin";
    }

    @RequestMapping("/admin/vms") //Page for manage the VMs
    public String vmsPage(Model modelo, Authentication sesion,
    Optional<String> uuid,
    Optional<String> grantVm,
    Optional<String> vmToVhost,
    Optional<Long> expansionId,
    Optional<String> denegateExpansion,
    Optional<String> grantExpansion,
    Optional<String> mode,
    Optional<String> vmcoreminValue,
    Optional<String> vmfreqminValue,
    Optional<String> vmmemminValue,
    Optional<String> vmcorevalValue,
    Optional<String> vmfreqvalValue,
    Optional<String> vmmemvalValue,
    Optional<String> vmcoremaxValue,
    Optional<String> vmfreqmaxValue,
    Optional<String> vmmemmaxValue,
    Optional<String> queueMode,
    Optional<Boolean> useFrequency,
    Optional<String> allocationOrder)
    {
        if(allocationOrder.isPresent())
        {
            sysop.setSystemValue("allocationOrder", allocationOrder.get());
            sysop.commandToInternalService("/allocationoder/"+allocationOrder.get());
        }
        if(queueMode.isPresent())
        {
            if(useFrequency.isPresent())
                sysop.setSystemValue("useFrequency", "1");
            else
                sysop.setSystemValue("useFrequency", "0");
            if(queueMode.get().equals("auto"))
            {
                sysop.setSystemValue("queueMode", "auto");
                sysop.commandToInternalService("/queuemode/auto");
            }
            if(queueMode.get().equals("manual"))
            {
                sysop.setSystemValue("queueMode", "manual");
                sysop.commandToInternalService("/queuemode/manual");
                
            }
        }
        if(vmcoreminValue.isPresent() && vmfreqminValue.isPresent() && vmmemminValue.isPresent() && 
            vmcorevalValue.isPresent() && vmfreqvalValue.isPresent() && vmmemvalValue.isPresent() && 
            vmcoremaxValue.isPresent() && vmfreqmaxValue.isPresent() && vmmemmaxValue.isPresent())
        {
            sysop.setSystemValue("vmcoremin", vmcoreminValue.get());
            sysop.setSystemValue("vmfreqmin", vmfreqminValue.get());
            sysop.setSystemValue("vmmemmin", vmmemminValue.get());
            sysop.setSystemValue("vmcoreval", vmcorevalValue.get());
            sysop.setSystemValue("vmfreqval", vmfreqvalValue.get());
            sysop.setSystemValue("vmmemval", vmmemvalValue.get());
            sysop.setSystemValue("vmcoremax", vmcoremaxValue.get());
            sysop.setSystemValue("vmfreqmax", vmfreqmaxValue.get());
            sysop.setSystemValue("vmmemmax", vmmemmaxValue.get());
        }
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
                if(sysop.getSystemValue("queueMode").equals("manual"))
                {
                    if(vmToVhost.isPresent() && sysop.getSystemValue("allocationOrder").equals("manual"))
                    {
                        Vhost vh = vhop.getVhost(vmToVhost.get());
                        if(vh == null)
                            modelo.addAttribute("msgOfSystem", "The host doesn't exist.");
                        else
                        {
                            if(!vmop.define(victima, vh, 100))
                                modelo.addAttribute("msgOfSystem", "Error to definition the VM.");
                        }
                    }
                    else
                    {
                        if(!vmop.define(victima, 100))
                            modelo.addAttribute("msgOfSystem", "Error to definition the VM.");
                    }
                }
                else
                modelo.addAttribute("msgOfSystem", "Error to definition the VM, the system not be manual.");
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
        modelo.addAttribute("vmcorevalValue", sysop.getSystemValue("vmcoreval"));
        modelo.addAttribute("vmcoreminValue", sysop.getSystemValue("vmcoremin"));
        modelo.addAttribute("vmcoremaxValue", sysop.getSystemValue("vmcoremax"));
        modelo.addAttribute("vmfreqvalValue", sysop.getSystemValue("vmfreqval"));
        modelo.addAttribute("vmfreqminValue", sysop.getSystemValue("vmfreqmin"));
        modelo.addAttribute("vmfreqmaxValue", sysop.getSystemValue("vmfreqmax"));
        modelo.addAttribute("vmmemvalValue", sysop.getSystemValue("vmmemval"));
        modelo.addAttribute("vmmemminValue", sysop.getSystemValue("vmmemmin"));
        modelo.addAttribute("vmmemmaxValue", sysop.getSystemValue("vmmemmax"));
        modelo.addAttribute("requests", vmop.requests());
        modelo.addAttribute("freeResources", vhop.getListVhosts());
        modelo.addAttribute("freeDisk", "Free disk: "+stgop.spaceFreeInAll()+"GB");
        modelo.addAttribute("requestsDiskHeaders", new String[]{"Extra space (GiB)", "Solicitant", "Options"});
        modelo.addAttribute("requestsDisk", vmop.getDiskExpansions());
        modelo.addAttribute("vmControlHeader", new String[]{"Uuid", "Owner", "Status", "Cores", "Memory (GiB)", "Disk (GiB)", "Date", "Options"});
        modelo.addAttribute("vmControl", vmop.getAllVms());
        modelo.addAttribute("modeState", sysop.getSystemValue("queueMode"));
        modelo.addAttribute("allocationState", sysop.getSystemValue("allocationOrder"));
        if(sysop.getSystemValue("useFrequency").equals("1"))
            modelo.addAttribute("frequencyState", true);
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 3);
        return "admin";
    }

    @RequestMapping("/admin/users") //Page of manage the users, only USERS
    public String usersPage(Model modelo, Authentication sesion,
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
            {
                if(!useop.removeUser(name.get()))
                    modelo.addAttribute("msgOfSystem", "This user have a resources or groups and not is possible to delete");
            }
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
                    case "restPassword":
                        useop.updatePassword(victima, value.get());
                        break;
                    case "EXPULSE":
                        User user = useop.getUser(value.get());
                        if(user != null)
                            if(!grpop.removeUserOfGroup(victima.myGroup, user))
                                modelo.addAttribute("msgOfSystem", "This user are not in this group.");
                        break;
                }
            }
        }
        modelo.addAttribute("headers", new String[]{"Name", "Mail", "Admin", "Status", "Max VMs", "Max cores", "Max mem", "Max disk", "Password", "Shares", "Options"});
        modelo.addAttribute("users", useop.getAllUsers());
        List<Groupp> shares = grpop.getGroups(); //Personal areas
        shares.removeIf(a -> !a.personalArea());
        modelo.addAttribute("shares", shares);
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 2);
        return "admin";
    }

    @RequestMapping("/admin/groups") //Page of manage the grouos
    public String groupsPage(Model modelo, Authentication sesion,
        Optional<String> newGroupName,
        Optional<String> newGroupOwner,
        Optional<String> name,
        Optional<String> field,
        Optional<String> value)
    {
        if(newGroupName.isPresent() && newGroupOwner.isPresent())
        {
            User tmp = useop.getUser(newGroupOwner.get());
            if(tmp != null)
                if(grpop.addGroup(newGroupName.get(), tmp) == null)
                    modelo.addAttribute("msgOfSystem", "This name is in use");
        }
        if(name.isPresent() && field.isPresent() && value.isPresent())
        {
            Groupp victima = grpop.getGroupp(name.get());
            if(victima != null)
            {
                switch (field.get())
                {
                    case "changeOwner":
                        User owner = useop.getUser(value.get());
                        if(owner != null)
                            grpop.updateOwner(victima, owner);
                        break;
                    case "changeMVms":
                        grpop.updateLimitVms(victima, Integer.parseInt(value.get()));
                        break;
                    case "changeMCores":
                        grpop.updateLimitVms(victima, Integer.parseInt(value.get()));
                        break;
                    case "changeMMem":
                        grpop.updateLimitVms(victima, Integer.parseInt(value.get()));
                        break;
                    case "changeMDisk":
                        grpop.updateLimitVms(victima, Integer.parseInt(value.get()));
                        break;
                    case "deleteGroup":
                        if(!grpop.removeGroup(victima))
                            modelo.addAttribute("msgOfSystem", "The group need are empty (no users, no vms)");
                        break;
                    case "EXPULSE":
                        User user = useop.getUser(value.get());
                        if(user != null)
                            if(!grpop.removeUserOfGroup(victima, user))
                                modelo.addAttribute("msgOfSystem", "This user are not in this group.");
                        break;
                }
            }
        }
        List<Groupp> grps = grpop.getGroups(); //General groups
        grps.removeIf(a -> a.personalArea());
        modelo.addAttribute("groups", grps);
        modelo.addAttribute("users", useop.getAllUsers());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 4);
        return "admin";
    }

    @RequestMapping("/admin/webpanel")
    public String webpanelPage(Model modelo, Authentication sesion,
        Optional<String> newTitle,
        Optional<String> newDescription,
        Optional<String> newLogoUrl,
        Optional<String> newColorHead,
        Optional<String> newColorBox,
        Optional<String> newColorBTN,
        Optional<String> newColorBackground,
        Optional<String> newColorTags,
        Optional<String> helpToDelete,
        Optional<String> newHelpLinkName,
        Optional<String> newHelpLinkLink)
    {
        if(newTitle.isPresent() && newDescription.isPresent() && newLogoUrl.isPresent() && newColorHead.isPresent()
            && newColorBox.isPresent() && newColorBTN.isPresent() && newColorBackground.isPresent() && newColorTags.isPresent())
        {
            sysop.setSystemValue("title", newTitle.get());
            sysop.setSystemValue("generalPageDescription", newDescription.get());
            sysop.setSystemValue("logoUrl", newLogoUrl.get());
            sysop.setSystemValue("colorHead", newColorHead.get());
            sysop.setSystemValue("colorBox", newColorBox.get());
            sysop.setSystemValue("colorBTN", newColorBTN.get());
            sysop.setSystemValue("colorBackground", newColorBackground.get());
            sysop.setSystemValue("colorTags", newColorTags.get());
        }
        if(helpToDelete.isPresent())
        {
            HelpRows toRemove = sysop.getHelpLink(helpToDelete.get());
            if(toRemove != null)
                sysop.removeHelpLink(toRemove);
        }
        if(newHelpLinkLink.isPresent() && newHelpLinkName.isPresent())
        {
            HelpRows toCreate = new HelpRows(newHelpLinkName.get(), newHelpLinkLink.get());
            sysop.addHelpLink(toCreate);
        }
        
        modelo.addAttribute("helpLinks",sysop.getAllHelpLinks());
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 5);
        return "admin";
    }

    private void comun(Model modelo, Authentication sesion)
    {
        //Specifics of the page
        modelo.addAttribute("userAcces",sesion.isAuthenticated());
        modelo.addAttribute("username",sesion.getName());
        modelo.addAttribute("pageName","Administration");
        modelo.addAttribute("colorBadmin","grey");
        modelo.addAttribute("admP", true); //Si ha llegado hasta aqui, es admin

        //Specifics of the web
        modelo.addAttribute("title", sysop.getSystemValue("title"));
        modelo.addAttribute("generalPageDescription", sysop.getSystemValue("generalPageDescription"));
        modelo.addAttribute("logoUrl", sysop.getSystemValue("logoUrl"));
        modelo.addAttribute("colorHead", sysop.getSystemValue("colorHead"));
        modelo.addAttribute("colorBox", sysop.getSystemValue("colorBox"));
        modelo.addAttribute("colorBTN", sysop.getSystemValue("colorBTN"));
        modelo.addAttribute("colorBackground", sysop.getSystemValue("colorBackground"));
        modelo.addAttribute("colorTags", sysop.getSystemValue("colorTags"));

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

    private void chargeTags(Model modelo, Authentication sesion, int code)
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
                tags.add(new String[]{"Create_user", "7"});
                break;
            case 3: //Admin VMs
                tags.add(new String[]{"Requests", "2"});
                tags.add(new String[]{"VMs", "5"});
                tags.add(new String[]{"Requests_controls", "14"}); //Cambiar de auto a manual, con auto poder establecer parametros
                tags.add(new String[]{"Set_master_image", "15"}); //Establecer una imagen para ser clonada (como????)
                break;
            case 4: //Admin groups
                tags.add(new String[]{"List_of_general_groups", "9"});
                tags.add(new String[]{"Create_group", "10"});
                break;
            case 5: //Admin webpanel
                tags.add(new String[]{"Options", "11"});
                break;
            case 6: //Monitor
                tags.add(new String[]{"General", "12"});
                tags.add(new String[]{"Hosts", "13"});
                tags.add(new String[]{"Storages", "8"});
                break;
        } 
        modelo.addAttribute("tags", tags);
    }
}