package eu.macarropueyo.terapweb.Controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.macarropueyo.terapweb.Model.Groupp;
import eu.macarropueyo.terapweb.Model.User;
import eu.macarropueyo.terapweb.Model.VM;
import eu.macarropueyo.terapweb.Model.Vdisk;
import eu.macarropueyo.terapweb.Services.GrouppOperation;
import eu.macarropueyo.terapweb.Services.StorageOperation;
import eu.macarropueyo.terapweb.Services.SystemOperation;
import eu.macarropueyo.terapweb.Services.UserOperation;
import eu.macarropueyo.terapweb.Services.VmOperation;

@Controller
public class ControllerHome
{
    @Autowired
    private UserOperation useop;
    @Autowired
    private GrouppOperation gpop;
    @Autowired
    private VmOperation vmop;
    @Autowired
    private StorageOperation stgop;
    @Autowired
    private SystemOperation sysop;
    

    @RequestMapping("/home") //Sirve a "My VMs"
    public String homePage(Model modelo, Authentication sesion,
        @RequestParam Optional<String> join)
    {
        User myuser = useop.getUser(sesion.getName());
        if(join.isPresent()) //Generar una solicitud de union a un grupo
        {
            Groupp tmp = gpop.getGroupp(join.get());
            if(tmp != null)
                gpop.sendPetitionForJoin(myuser, tmp);
        }
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 0);
        comunHome(modelo, sesion);
        return "home";
    }

    @RequestMapping("/home/myvms") //Sirve a "My VMs"
    public String myvmsPage(Model modelo, Authentication sesion,
        @RequestParam Optional<Integer> vmcore,
        @RequestParam Optional<Integer> vmfreq,
        @RequestParam Optional<Integer> vmmem,
        @RequestParam Optional<String> uuid,
        @RequestParam Optional<String> unshare)
    {
        User myuser = useop.getUser(sesion.getName());
        if(vmcore.isPresent() && vmfreq.isPresent() && vmmem.isPresent()) //Caso de crear una request
            if(vmop.newVM(myuser.myGroup, "", vmcore.get(), vmfreq.get(), vmmem.get(), 0)==null)
                modelo.addAttribute("msgOfSystem","Error at create the request");
        if(uuid.isPresent()) //Caso de eliminar una request
        {
            VM victima = vmop.findByUUID(uuid.get());
            if(victima==null)
            {
                modelo.addAttribute("msgOfSystem","The request not exist");
            }
            else if(victima.isMine(myuser))
            {
                if(victima.isDefine())
                    vmop.undefine(victima);
                vmop.removeVM(victima);
                myuser.myGroup.vms.remove(victima);
            }
        }
        if(unshare.isPresent()) //Caso de dejar de compartir mis recursos a un usuario
        {
            User tmp = useop.getUser(unshare.get());
            if(tmp != null)
                if(!gpop.removeUserOfGroup(myuser.myGroup, tmp))
                    modelo.addAttribute("msgOfSystem","There has been a problem");
        }
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 1);
        comunVms(modelo, sesion, myuser.myGroup);
        if(myuser.myGroup != null)
            modelo.addAttribute("shares", myuser.myGroup.users);
        modelo.addAttribute("iMaster", true);
        return "home";
    }

    @RequestMapping("/home/mygroups") //Gestion de grupos (usuarios, el grupo, request y VMs)
    public String homeSelectGroupsPage(Model modelo, Authentication sesion,
        @RequestParam Optional<String> groupselect,
        @RequestParam Optional<String> expulse,
        @RequestParam Optional<String> acceptpetition,
        @RequestParam Optional<String> declinepetition,
        @RequestParam Optional<String> deleteRequest,
        @RequestParam Optional<Integer> vmcore,
        @RequestParam Optional<Integer> vmfreq,
        @RequestParam Optional<Integer> vmmem,
        @RequestParam Optional<String> exitgroup,
        @RequestParam Optional<String> deleteGroup)
    {
        User myuser = useop.getUser(sesion.getName());
        Groupp group = null;
        if(deleteGroup.isPresent()) //El admin quiere borrar el grupo
        {
            Groupp tmp = gpop.getGroupp(deleteGroup.get());
            if(tmp != null)
                if(tmp.user.getId()==myuser.getId()) //Si el usuario es el admin del grupo
                {
                    Groupp victima = gpop.getGroupp(deleteGroup.get());
                    if(!gpop.removeGroup(victima))
                        modelo.addAttribute("msgOfSystem","This group not have the requeriments for are remove.");
                }
        }
        if(exitgroup.isPresent()) //Usuario quiere salir del grupo
        {
            for (Groupp tmp : myuser.myGroups())
                if(tmp.getName().equals(exitgroup.get()))
                    group = tmp;
            gpop.removeUserOfGroup(group, myuser);
            group = null;
        }
        if(groupselect.isPresent())
            for (Groupp tmp : myuser.myGroups())
                if(tmp.getName().equals(groupselect.get()))
                    group = tmp;
        if(group == null)
        {
            if(groupselect.isPresent())
                modelo.addAttribute("msgOfSystem","This group not exist (or isn't to you)");
            chargeTags(modelo, sesion, 2);
        }
        else
        {
            modelo.addAttribute("groupname", group.getName());
            if(group.user.getId()==myuser.getId()) //Es admin
            {
                if(expulse.isPresent()) //Hay un usuario que expulsar del grupo
                {
                    User tmp = useop.getUser(expulse.get());
                    if(tmp != null)
                        if(!gpop.removeUserOfGroup(group, tmp))
                            modelo.addAttribute("msgOfSystem","The operation has failed.");
                }
                else if(acceptpetition.isPresent()) //Aceptar peticion de ingreso
                    gpop.acceptPetition(group, acceptpetition.get());
                else if(declinepetition.isPresent()) //Denegar peticion de ingreso
                    gpop.declinePetition(group, declinepetition.get());
                else if(deleteRequest.isPresent()) //Anular request de recursos
                {
                    VM tmp = vmop.findByUUID(deleteRequest.get());
                    if(tmp != null)
                        vmop.removeVM(tmp);
                }
                else if(vmcore.isPresent() && vmfreq.isPresent() && vmmem.isPresent()) //Crear request de recurso
                    vmop.newVM(group, "", vmcore.get(), vmfreq.get(), vmfreq.get(), 0);
                modelo.addAttribute("iMaster", true);
                modelo.addAttribute("listUsers", group.users);
                modelo.addAttribute("pendingPetitions", group.newUsers);
                chargeTags(modelo, sesion, 3);
            }
            else //No es admin
                chargeTags(modelo, sesion, 4);
            comunVms(modelo, sesion, group);
        }
        comun(modelo, sesion);
        modelo.addAttribute("groups", myuser.myGroups());
        return "home";
    }

    @RequestMapping("/home/vmoperation") //Gestion de grupos (usuarios, el grupo, request y VMs)
    public String operationVMs(Model modelo, Authentication sesion,
        @RequestParam Optional<String> uuid,
        @RequestParam Optional<String> operation,
        @RequestParam Optional<Integer> newSize)
    {
        User myuser = useop.getUser(sesion.getName());
        if(operation.isPresent() && uuid.isPresent())
        {
            //Parte comun, solo para controles basicos
            VM vm = vmop.findByUUID(uuid.get());
            if(!vm.isMine(myuser)) //Si no es del usuario, no tiene permisos sobre ella
                return "empty";
            switch(operation.get())
            {
                case "Start": //Solicitud de encender una vm
                    vmop.start(vm);
                    break;
                case "Shutdown": //Solicita apagar una vm
                    vmop.shutdown(vm);
                    break;
                case "Forceshutdown": //Solicita forzar el apagado de una vm
                    vmop.shutdownNow(vm);
                    break;
            }
            //Para controles absolutos
            if(!vm.supervisor(myuser)) //Si soy responsable de esta vm
                return "empty";
            switch(operation.get())
            {
                case "REMAKE": //Solicitud resetear una instancia
                    vmop.remake(vm);
                    break;
                case "DELETE": //Solicita devolver el recurso
                    for (Vdisk vd : vm.pool.vdisks)
                            stgop.deleteDisk(vd);
                    vm.pool.vdisks.clear();
                    vmop.undefine(vm);
                    vmop.removeVM(vm);
                    break;
                case "MoreDisk": //Solicita ampliar el disco
                    if(newSize.isPresent())
                        vmop.newExpansion(vm, newSize.get());
                    break;
            }
        }
        return "empty";
    }

    private void comunHome(Model modelo, Authentication sesion) //Pendiente de limpiar mierda a otro metodo
    {
        User myuser = useop.getUser(sesion.getName());
        modelo.addAttribute("vms", vmop.requests()); //Lista de las request ya ordenadas
        //Lista de grupos a los que enviar una peticion de union (todos menos el propio)
        List<Groupp> tmp = gpop.getGroups();
        tmp.remove(myuser.myGroup);
        modelo.addAttribute("allGroups", tmp);
        modelo.addAttribute("myPetition", myuser.joinToGroups); //Mis peticiones a grupos
    }

    private void comunVms(Model modelo, Authentication sesion, Groupp grupo)
    {
        //Lista de vms solicitadas por el usuario, si tiene
        if(grupo != null)
            modelo.addAttribute("dataMyRequests", grupo.getRequests());
        //Paramatros base de nuevas VMs
        modelo.addAttribute("vmcoreval", sysop.getSystemValue("vmcoreval"));
        modelo.addAttribute("vmcoremin", sysop.getSystemValue("vmcoremin"));
        modelo.addAttribute("vmcoremax", sysop.getSystemValue("vmcoremax"));
        modelo.addAttribute("vmfreqval", sysop.getSystemValue("vmfreqval"));
        modelo.addAttribute("vmfreqmin", sysop.getSystemValue("vmfreqmin"));
        modelo.addAttribute("vmfreqmax", sysop.getSystemValue("vmfreqmax"));
        modelo.addAttribute("vmmemval", sysop.getSystemValue("vmmemval"));
        modelo.addAttribute("vmmemmin", sysop.getSystemValue("vmmemmin"));
        modelo.addAttribute("vmmemmax", sysop.getSystemValue("vmmemmax"));
        //VMs para controlar, si tiene
        if(grupo != null)
            modelo.addAttribute("vmOperate", grupo.getResources());
    }

    private void comun(Model modelo, Authentication sesion)
    {
        modelo.addAttribute("userAcces",sesion.isAuthenticated());
        modelo.addAttribute("username",sesion.getName());
        modelo.addAttribute("pageName","Home");
        modelo.addAttribute("colorBhome","grey");
        modelo.addAttribute("admP", useop.getUser(sesion.getName()).isRoot()); //Si es admin

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
        sections.add(new String[]{"Global","/home"});
        sections.add(new String[]{"My VMs","/home/myvms"});
        sections.add(new String[]{"My groups","/home/mygroups"}); //tiene doble salida -> hay que seleccionar un grupo (post) para abrir todas las pestañas
        modelo.addAttribute("sections",sections);
    }

    private void chargeTags(Model modelo, Authentication sesion, int code)
    {
        List<String[]> tags=new LinkedList<>();
        switch(code)
        { 
            case 0: //Global
                tags.add(new String[]{"Global_qeue", "100"}); //incluye solo la cola global
                tags.add(new String[]{"List_of_groups", "102"}); //lista de todos los grupos (menos el propio) y boton de solicitud de acceso
                tags.add(new String[]{"My_petitions", "103"}); //Tus peticiones pendientes (no se pueden anular)
                break;
            case 1: //My VMs
                tags.add(new String[]{"My_requests", "104"}); //Formulario para realizar la peticion de recurso y lista DE MIS solicitudes, se pueden anular y no incluye las concedidas
                tags.add(new String[]{"Share", "105"}); //Lista de usuarios que pueden acceder a mis VMs y peticiones pendientes
                tags.add(new String[]{"My_VMs", "106"}); //Son las peticiones concedidas convertidas en VMs o recursos
                break;
            case 2: //My Groups, solo la lista de mis grupos
                tags.add(new String[]{"List_of_my_groups", "110"}); //Lista de grupos a los que pertence el usuario
                break;
            case 3: //My Groups, se ha seleccionado uno para operarlo
                tags.add(new String[]{"List_of_my_groups", "110"}); //Lista de grupos a los que pertence el usuario
                tags.add(new String[]{"Manage_my_group", "111"}); //Solo admins, dispone de la lista de usuarios (puede expulsar), la lista de peticiones de usuario (aceptar o rechazar), la lista de solicitudes de VMs (puede cancelarlas) y el formulario de nuevas VMs
                tags.add(new String[]{"VMs_of_group", "106"}); //Dispone de la lista de VMs del grupo y permite controlarlas (excepto eliminar o expandir, salvo que sea el responsable, ampliar la tabla)
                break;
            case 4: //My Groups, igual que el caso 3, pero para usuarios no administradores del grupo
                tags.add(new String[]{"List_of_my_groups", "110"}); //Lista de grupos a los que pertence el usuario
                tags.add(new String[]{"VMs_of_group", "106"}); //Dispone de la lista de VMs del grupo y permite controlarlas (excepto eliminar o expandir, salvo que sea el responsable, ampliar la tabla)
                tags.add(new String[]{"VMs_Monitor", "107"}); //Monitor vnc web de acceso a una vm
                break;
        } 
        modelo.addAttribute("tags", tags);
    }
}
