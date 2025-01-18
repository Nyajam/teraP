package eu.macarropueyo.terapweb.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Model.*;
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
    @Autowired
	private PasswordEncoder passwordEncoder;

    /**
     * Create a user with random password.
     * The password it will send by email.
     * @param name Name of the user
     * @param mail Email of the user
     * @return The user created
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
     * Remove a user (by the name) if dont have resources or are the owner of groups
     * @param name Name of the user
     * @return True if it was removed
     */
    public boolean removeUser(String name)
    {
        Optional<User> tmp = userRepo.findByName(name);
        if(tmp.isEmpty())
            return false;
        User user = tmp.get();
        if(!user.haveResources()&&!user.administrationGroups())
        {
            for (Groupp group : user.groups) //Expulse the user of the groups
                grpop.removeUserOfGroup(group, user);
            grpop.removeGroup(user.myGroup); //Remove their group
            userRepo.delete(user);
            return true;
        }
        return false;
    }

    /**
     * Search a user by the name
     * @param name
     * @return Null if not exist
     */
    public User getUser(String name)
    {
        Optional<User> tmp = userRepo.findByName(name);
        if(tmp.isPresent())
            return tmp.get();
        return null;
    }

    /**
     * Send a message by the email.
     * @param msg Message for send
     * @param user Send to this user
     */
    public void sendNotify(String msg, User user)
    {
        sysop.sendMail(user.mail, msg);
    }

    /**
     * Block the access a user and send a message
     * @param user
     */
    public void block(User user)
    {
        //Falta cerrar la sesion del usuario
        user.block();
        userRepo.save(user);
        sendNotify("Your user has been blocked by the administrator.", user);
    }

    /**
     * Unlock the access a user and send a message
     * @param user
     */
    public void unblock(User user)
    {
        user.unBlock();
        userRepo.save(user);
        sendNotify("Your user has been unlocked by the administrator.", user);
    }

    /**
     * Update the password of a user
     * @param user
     * @param pass
     */
    public void updatePassword(User user, String pass)
    {
        if(pass.contains("\""))
            return;
        //user.setPassword(passwordEncoder.encode(pass).toString());
        user.setPassword(pass);
        userRepo.save(user);
        sysop.commandToInternalServicePost("/updatepassword/"+user.name, "{\"passwd\":\""+pass+"\"}"); //Expand the password for other services
    }

    /**
     * Update the email of a user
     * @param user
     * @param mail
     */
    public void updateMail(User user, String mail)
    {
        user.mail=mail;
        userRepo.save(user);
    }

    /**
     * Convert a user to administrator
     * @param user
     */
    public void grantAdm(User user)
    {
        user.setRoot();
        userRepo.save(user);
    }

    /**
     * Convert an administrator to a normal user
     * @param user
     */
    public void revokeAdm(User user)
    {
        user.setNotRoot();
        userRepo.save(user);
    }

    /**
     * Return all users
     * @return
     */
    public List<User> getAllUsers()
    {
        return userRepo.findAll();
    }
}