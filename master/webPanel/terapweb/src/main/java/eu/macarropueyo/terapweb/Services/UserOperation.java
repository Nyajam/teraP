package eu.macarropueyo.terapweb.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.GroupRepository;
import eu.macarropueyo.terapweb.Repository.UserRepository;

@Component
public class UserOperation
{
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private SystemOperation sysop;
    @Autowired
    private GrouppOperation grpop;

    /**
     * Crea un usuario, la contrasena se genera aleatoria.
     * Se notifica al mail su contrasena.
     * @param name
     * Nombre del usuario.
     * @param mail
     * Mail del usuario.
     * @return
     * El usuario recien creado
     */
    public User addUser(String name, String mail)
    {
        User tmp = new User(name, StatusUser.OK, false);
        String password = sysop.random();
        tmp.mail = mail;
        tmp.setPassword(password);
        userRepo.save(tmp);
        tmp.myGroup = grpop.addGroup(name, tmp);
        userRepo.save(tmp);
        sysop.sendMail(mail, "You're account is create with name: "+name+"\n and password: "+password+"\n");
        return tmp;
    }

    /**
     * Elimina un usuario especificado por el nombre, si no tiene recursos y no administra grupos
     * @param name
     * @return
     * True si se han dado las condiciones para borrarlo
     */
    public boolean removeUser(String name)
    {
        Optional<User> tmp = userRepo.findByName(name);
        if(tmp.isEmpty())
            return false;
        User user = tmp.get();
        if(!user.haveResources()&&!user.administrationGroups())
        {
            for (Groupp group : user.groups) //Saaca al usuario de los grupos
                grpop.removeUserOfGroup(group, user);
            grpop.removeGroup(user.myGroup); //Elimina su grupo personal
            userRepo.delete(user);
            return true;
        }
        return false;
    }

    /**
     * Busca un usuario segun un nombre
     * @param name
     * @return
     * Null si no existe
     */
    public User getUser(String name)
    {
        Optional<User> tmp = userRepo.findByName(name);
        if(tmp.isPresent())
            return tmp.get();
        return null;
    }

    /**
     * Envia un mensaje al usuario a su correo indicado
     * @param msg
     * Mensaje a enviar.
     * @param user
     * Usuario destino.
     */
    public void sendNotify(String msg, User user)
    {
        sysop.sendMail(user.mail, msg);
    }

    /**
     * Bloquea a un usuario y se lo notifica
     * @param user
     * Usuario a bloquear
     */
    public void block(User user)
    {
        //Falta cerrar la sesion del usuario
        user.block();
        userRepo.save(user);
        sendNotify("Your user has been blocked by the administrator.", user);
    }

    /**
     * Desbloquea a un usuario y se lo notifica
     * @param user
     */
    public void unblock(User user)
    {
        user.unBlock();
        userRepo.save(user);
        sendNotify("Your user has been unlocked by the administrator.", user);
    }

    /**
     * Actualiza la password de un usuario
     * @param user
     * @param pass
     */
    public void updatePassword(User user, String pass)
    {
        user.setPassword(pass);
        userRepo.save(user);
        //Servicio interno propaga la clave a servicios asociados
    }

    /**
     * Actualiza el mail de un usuario
     * @param user
     * @param mail
     */
    public void updateMail(User user, String mail)
    {
        user.mail=mail;
        userRepo.save(user);
    }

    /**
     * Convierte en administrador a un usuario
     * @param user
     */
    public void grantAdm(User user)
    {
        user.setRoot();
        userRepo.save(user);
    }

    /**
     * Convierte a un administrador en un usuario sin privilegios
     * @param user
     */
    public void revokeAdm(User user)
    {
        user.setNotRoot();
        userRepo.save(user);
    }

    public List<User> getAllUsers()
    {
        return userRepo.findAll();
    }
}