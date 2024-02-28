package eu.macarropueyo.terapweb.Controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.macarropueyo.terapweb.Model.User;
import eu.macarropueyo.terapweb.Services.SystemOperation;
import eu.macarropueyo.terapweb.Services.UserOperation;

@Controller
public class ControllerUser
{
    @Autowired
    private UserOperation useop;
    @Autowired
    private SystemOperation sysop;
    
    @RequestMapping("/myuser")
    public String myuserPage(Model modelo, HttpServletRequest sesion,
        Optional<String> value,
        Optional<String> changePass,
        Optional<String> changeMail,
        Optional<String> sendMe)
    {
        User user = useop.getUser(sesion.getUserPrincipal().getName());
        if(changePass.isPresent() && value.isPresent())
        {
            useop.updatePassword(user, value.get());
            modelo.addAttribute("msgOfSystem", "Password changed");
        }
        if(changeMail.isPresent() && value.isPresent())
        {
            useop.updateMail(user, value.get());
            modelo.addAttribute("msgOfSystem", "Email changed");
        }
        if(sendMe.isPresent())
        {
            sysop.sendMail(user.mail, "Message test.\nYoure email address is correct and not have problems.");
            modelo.addAttribute("msgOfSystem", "Email sent");
        }
        
        modelo.addAttribute("nVms", user.myGroup.maxvms);
        modelo.addAttribute("nCores", user.myGroup.maxcores);
        modelo.addAttribute("nMem", user.myGroup.maxmem);
        modelo.addAttribute("nDisk", user.myGroup.maxdisk);
        comun(modelo, sesion);
        chargeTags(modelo, sesion, 0);
        return "myuser";
    }

    private void comun(Model modelo, HttpServletRequest sesion)
    {
        User user = useop.getUser(sesion.getUserPrincipal().getName());
        modelo.addAttribute("userAcces",sesion.getUserPrincipal()!=null);
        modelo.addAttribute("username",sesion.getUserPrincipal().getName());
        modelo.addAttribute("pageName","My user");
        modelo.addAttribute("colorBmyuser","grey");
        if(user.isRoot()) //Si es admin
            modelo.addAttribute("admP", true);
        modelo.addAttribute("usermail", user.mail);
        
        modelo.addAttribute("title", sysop.getSystemValue("title"));
        modelo.addAttribute("generalPageDescription", sysop.getSystemValue("generalPageDescription"));
        modelo.addAttribute("logoUrl", sysop.getSystemValue("logoUrl"));
        modelo.addAttribute("colorHead", sysop.getSystemValue("colorHead"));
        modelo.addAttribute("colorBox", sysop.getSystemValue("colorBox"));
        modelo.addAttribute("colorBTN", sysop.getSystemValue("colorBTN"));
        modelo.addAttribute("colorBackground", sysop.getSystemValue("colorBackground"));
        modelo.addAttribute("colorTags", sysop.getSystemValue("colorTags"));

        List<String[]> sections = new LinkedList<>();
        sections.add(new String[]{"Configuration","/myuser"});
        modelo.addAttribute("sections",sections);
    }

    private void chargeTags(Model modelo, HttpServletRequest sesion, int code)
    {
        List<String[]> tags=new LinkedList<>();
        switch(code)
        { 
            case 0: //Admin Vhosts
                tags.add(new String[]{"My_information", "0"});
                break;
        } 
        modelo.addAttribute("tags", tags);
    }
}
