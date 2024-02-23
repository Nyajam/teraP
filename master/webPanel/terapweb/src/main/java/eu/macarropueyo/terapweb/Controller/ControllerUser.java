package eu.macarropueyo.terapweb.Controller;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.macarropueyo.terapweb.Services.UserOperation;

@Controller
public class ControllerUser
{
    @Autowired
    private UserOperation useop;
    
    @RequestMapping("/myuser")
    public String myuserPage(Model modelo, HttpServletRequest sesion)
    {
        modelo.addAttribute("userAcces",sesion.getUserPrincipal()!=null);
        modelo.addAttribute("username",sesion.getUserPrincipal().getName());
        modelo.addAttribute("title","teraP");
        modelo.addAttribute("generalPageDescription","Plataforma de virtualización en cluster");
        modelo.addAttribute("logoUrl","https://estaticos.muyinteresante.es/media/cache/1140x_thumb/uploads/images/gallery/59c4f5655bafe82c692a7052/gato-marron_0.jpg");
        modelo.addAttribute("pageName","My user");
        modelo.addAttribute("colorBmyuser","grey");
        if(useop.getUser(sesion.getUserPrincipal().getName()).isRoot()) //Si es admin
            modelo.addAttribute("admP", true);

        //modelo.addAttribute("tags",new LinkedList<>());
        return "home";
    }
}
