package eu.macarropueyo.terapweb.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Services.SystemOperation;
import eu.macarropueyo.terapweb.Services.UserOperation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class ControllerHelp {
    @Autowired
    private UserOperation useop;
    @Autowired
    private SystemOperation sysop;

    @RequestMapping(value = {"/help", "/help/{link}"})
    public String helpHubPage(Model modelo, Authentication sesion, @PathVariable("link") Optional<String> link)
    {
        User user = useop.getUser(sesion.getName());
        modelo.addAttribute("userAcces",sesion.isAuthenticated());
        modelo.addAttribute("username",sesion.getName());
        modelo.addAttribute("pageName","Help");
        modelo.addAttribute("colorBhelp","grey");
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

        List<HelpRows> helpLinks = sysop.getAllHelpLinks();
        modelo.addAttribute("helps", helpLinks);
        if(link.isPresent())
        {
            HelpRows tmp = sysop.getHelpLink(link.get());
            if(tmp != null)
                modelo.addAttribute("framePage", tmp.link);
        }
        else if(!helpLinks.isEmpty())
            modelo.addAttribute("framePage", helpLinks.get(0).link);
        return "help";
    }
    
    
}
