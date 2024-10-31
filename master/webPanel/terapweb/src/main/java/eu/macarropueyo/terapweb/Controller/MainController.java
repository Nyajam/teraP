package eu.macarropueyo.terapweb.Controller;

import eu.macarropueyo.terapweb.Services.*;
import eu.macarropueyo.terapweb.Model.*;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController
{
    @Autowired
    private UserOperation usop;
    @Autowired
    private SystemOperation sysop;

    @RequestMapping("/start")
    public String test(Model modelo, Authentication sesion)
    {
        if(sysop.getSystemValue("start").length() > 0)
            return "empty";
        User tmp = usop.addUser("admin", "admin@localhost");
        usop.grantAdm(tmp);
        usop.updatePassword(tmp, "123456789");
        sysop.setSystemValue("nuke", "ARMED");
        sysop.setSystemValue("title", "teraP");
        sysop.setSystemValue("generalPageDescription", "Plataforma de virtualización en cluster");
        sysop.setSystemValue("logoUrl", "https://raw.githubusercontent.com/Nyajam/teraP/main/images/logo.png");
        sysop.setSystemValue("colorHead", "#74b1ff");
        sysop.setSystemValue("colorBox", "#b0c4de");
        sysop.setSystemValue("colorBTN", "#a9a9a9");
        sysop.setSystemValue("colorBackground", "#ffffff");
        sysop.setSystemValue("colorTags", "#808080");
        return "empty";
    }

    @RequestMapping("/")
    public String indexPage(Model modelo, Authentication sesion)
    {
        if(sesion != null)
        {
            modelo.addAttribute("userAcces",true);
            modelo.addAttribute("username",sesion.getName());
        }
        else
            modelo.addAttribute("userAcces",false);
        modelo.addAttribute("errorBox",false);
        modelo.addAttribute("pageName","Login");

        modelo.addAttribute("title", sysop.getSystemValue("title"));
        modelo.addAttribute("generalPageDescription", sysop.getSystemValue("generalPageDescription"));
        modelo.addAttribute("logoUrl", sysop.getSystemValue("logoUrl"));
        modelo.addAttribute("colorHead", sysop.getSystemValue("colorHead"));
        modelo.addAttribute("colorBox", sysop.getSystemValue("colorBox"));
        modelo.addAttribute("colorBTN", sysop.getSystemValue("colorBTN"));
        modelo.addAttribute("colorBackground", sysop.getSystemValue("colorBackground"));
        modelo.addAttribute("colorTags", sysop.getSystemValue("colorTags"));
        return "index";
    }

    @RequestMapping("/fail")
    public String errorPage(Model modelo, Authentication sesion)
    {
        if(sesion != null)
        {
            modelo.addAttribute("userAcces",true);
            modelo.addAttribute("username",sesion.getName());
        }
        else
            modelo.addAttribute("userAcces",false);
        modelo.addAttribute("errorBox",true);
        modelo.addAttribute("pageName","Login");

        modelo.addAttribute("title", sysop.getSystemValue("title"));
        modelo.addAttribute("generalPageDescription", sysop.getSystemValue("generalPageDescription"));
        modelo.addAttribute("logoUrl", sysop.getSystemValue("logoUrl"));
        modelo.addAttribute("colorHead", sysop.getSystemValue("colorHead"));
        modelo.addAttribute("colorBox", sysop.getSystemValue("colorBox"));
        modelo.addAttribute("colorBTN", sysop.getSystemValue("colorBTN"));
        modelo.addAttribute("colorBackground", sysop.getSystemValue("colorBackground"));
        modelo.addAttribute("colorTags", sysop.getSystemValue("colorTags"));
        return "index";
    }
}
