package eu.macarropueyo.terapweb.Controller;

import eu.macarropueyo.terapweb.Repository.*;
import eu.macarropueyo.terapweb.Services.*;
import eu.macarropueyo.terapweb.Model.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController
{
    @Autowired
	private  GroupRepository repo;
    @Autowired
    private StorageOperation tmp; //para test!!
    @Autowired
    private UserOperation nya; //para test!!
    @Autowired
    private VmOperation tmp2; //para test!!

    @RequestMapping("/test")
    public String test(Model modelo, HttpServletRequest sesion)
    {
        //Storage stg = tmp.getStorage("2");
        //VM vm = nya.getUser("ilde").myGroup.vms.get(0);
        //tmp.createTarget(vm);
        return "empty";
    }

    @RequestMapping("/")
    public String indexPage(Model modelo, HttpServletRequest sesion)
    {
        if(sesion.getUserPrincipal()!=null)
        {
            modelo.addAttribute("userAcces",true);
            modelo.addAttribute("username",sesion.getUserPrincipal().getName());
        }
        else
            modelo.addAttribute("userAcces",false);
        modelo.addAttribute("errorBox",false);
        modelo.addAttribute("title","teraP");
        modelo.addAttribute("generalPageDescription","Plataforma de virtualización en cluster");
        modelo.addAttribute("logoUrl","https://estaticos.muyinteresante.es/media/cache/1140x_thumb/uploads/images/gallery/59c4f5655bafe82c692a7052/gato-marron_0.jpg");
        modelo.addAttribute("pageName","Login");
        return "index";
    }

    @RequestMapping("/fail")
    public String errorPage(Model modelo, HttpServletRequest sesion)
    {
        if(sesion.getUserPrincipal()!=null)
        {
            modelo.addAttribute("userAcces",true);
            modelo.addAttribute("username",sesion.getUserPrincipal().getName());
        }
        else
            modelo.addAttribute("userAcces",false);
        modelo.addAttribute("errorBox",true);
        modelo.addAttribute("title","teraP");
        modelo.addAttribute("generalPageDescription","Plataforma de virtualización en cluster");
        modelo.addAttribute("logoUrl","https://estaticos.muyinteresante.es/media/cache/1140x_thumb/uploads/images/gallery/59c4f5655bafe82c692a7052/gato-marron_0.jpg");
        modelo.addAttribute("pageName","Login");
        return "index";
    }
}
