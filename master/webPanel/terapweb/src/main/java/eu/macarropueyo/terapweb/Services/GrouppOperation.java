package eu.macarropueyo.terapweb.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.GroupRepository;
import eu.macarropueyo.terapweb.Repository.UserRepository;

@Component
public class GrouppOperation
{
    @Autowired
    private GroupRepository grpRepo;
    @Autowired
    private UserRepository usrRepo;

    /**
     * Crea un grupo nuevo
     * @param name
     * Nombre del grupo a crear
     * @return
     * Retorna el grupo creado, null si falla
     */
    public Groupp addGroup(String name, User propietary)
    {
        Groupp grupo = null;
        if(!grpRepo.findByName(name).isPresent())
        {
            grupo = new Groupp(name, propietary);
            grpRepo.save(grupo);
        }
        return grupo;
    }

    /**
     * Elimina un grupo, si esta vacio
     * @param grupo
     * Grupo a borrar
     * @return
     * True si ha sido posible, false si no existe o tiene usuarios
     */
    public boolean removeGroup(Groupp grupo)
    {
        if(grupo==null) //Si no existe el grupo
            return false;
        if(!grupo.users.isEmpty() || grupo.haveResources()) //Si el grupo no esta vacio o tiene recursos
            return false;
        grpRepo.delete(grupo);
        return true;
    }

    /**
     * Retorna la lista de grupos de la base de datos.
     * @return
     */
    public List<Groupp> getGroups()
    {
        return grpRepo.findAll();
    }

    /**
     * Retorna el grupo con ese nombre (si existe).
     * @param name
     * Nombre del grupo
     * @return
     * El grupo si existe o null si no
     */
    public Groupp getGroupp(String name)
    {
        Optional<Groupp> tmp = grpRepo.findByName(name);
        if(tmp.isEmpty())
            return null;
        return tmp.get();
    }

    /**
     * Comprueba si un usuario pertenece a un grupo
     * @param grupo
     * @param user
     * @return
     * True si es verdad, false si no pertenece
     */
    public boolean userInGroup(Groupp grupo, User user)
    {
        for (User usr : grupo.users)
            if(usr.equals(user))
                return true;
        return false;
    }

    /**
     * Anade un usuario a un grupo
     * @param grupo
     * @param user
     * @return
     * Retorna false si el usuario ya estaba en el grupo
     */
    public boolean addUserInGroup(Groupp grupo, User user)
    {
        if(grupo.findUser(user.name)!=null)
            return false;
        grupo.users.add(user);
        user.groups.add(grupo);
        grpRepo.save(grupo);
        usrRepo.save(user);
        return true;
    }

    /**
     * Quita un usuario de un grupo
     * @param grupo
     * @param user
     * @return
     * Retorna false si no estaba en el grupo
     */
    public boolean removeUserOfGroup(Groupp grupo, User user)
    {
        User victima = grupo.findUser(user.name);
        if(victima == null)
            return false;
        grupo.users.remove(victima);
        victima.groups.remove(grupo);
        grpRepo.save(grupo);
        usrRepo.save(user);
        return true;
    }

    /**
     * Crea (envia) una peticion de unirse a un grupo
     * @param user
     * @param group
     */
    public void sendPetitionForJoin(User user, Groupp group)
    {
        user.joinToGroups.add(group);
        group.newUsers.add(user);
        grpRepo.save(group);
        usrRepo.save(user);
    }

    /**
     * Aceptar un peticion de entrada a un grupo
     * @param group
     * @param user
     */
    public void acceptPetition(Groupp group, String user)
    {
        Optional<User> tmp = usrRepo.findByName(user);
        if(tmp.isPresent()) //Si esxiste
        {
            if(tmp.get().joinToGroups.contains(group)) //Si el usuario tiene un peticion pendiente
            {
                tmp.get().joinToGroups.remove(group);
                group.newUsers.remove(tmp.get());
                tmp.get().groups.add(group);
                group.users.add(tmp.get());
                usrRepo.save(tmp.get());
                grpRepo.save(group);
            }
        }
    }

    /**
     * No aceptar una peticion a unirse al grupo
     * @param group
     * @param user
     */
    public void declinePetition(Groupp group, String user)
    {
        Optional<User> tmp = usrRepo.findByName(user);
        if(tmp.isPresent()) //Si esxiste
        {
            if(tmp.get().joinToGroups.contains(group)) //Si el usuario tiene un peticion pendiente
            {
                tmp.get().joinToGroups.remove(group);
                group.newUsers.remove(tmp.get());
                usrRepo.save(tmp.get());
                grpRepo.save(group);
            }
        }
    }

    /**
     * Update the limit of number of vms of a especific group
     * @param group
     * @param vms
     */
    public void updateLimitVms(Groupp group, int vms)
    {
        group.maxvms = vms;
        grpRepo.save(group);
    }

    /**
     * Update the limit of total cores of a especific group
     * @param group
     * @param cores
     */
    public void updateLimitCores(Groupp group, int cores)
    {
        group.maxcores = cores;
        grpRepo.save(group);
    }

    /**
     * Update the limit of total memory of a especific group
     * @param group
     * @param mem
     */
    public void updateLimitMem(Groupp group, int mem)
    {
        group.maxmem = mem;
        grpRepo.save(group);
    }

    /**
     * Update the limit of frequency of this group
     * @param group
     * @param freq
     */
    public void updateLimitFreq(Groupp group, int freq)
    {
        group.maxfreq = freq;
        grpRepo.save(group);
    }

    /**
     * Update the limit of size of disk (total) of a especific group
     * @param group
     * @param disk
     */
    public void updateLimitDisk(Groupp group, int disk)
    {
        group.maxdisk = disk;
        grpRepo.save(group);
    }

    public void updateOwner(Groupp group, User newOwner)
    {
        group.user = newOwner;
        grpRepo.save(group);
    }
}