package eu.macarropueyo.terapweb.Model;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * The name of this class repeats the 'p' for the requirements of the data base (mariadb).
 */
@Entity
public class Groupp
{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    public User user;
    @ManyToMany
    public List<User> users;
    @OneToMany(mappedBy = "group")
    public List<VM> vms;
    @Column(unique=true)
    private String name;
    @ManyToMany
    public List<User> newUsers;

    public Date date;
    public int maxvms; //numero de vm permitidas
    public int maxcores; //numero de cores permitidos
    public int maxfreq; //frecuencia maxima permitida
    public int maxmem; //memoria maxima permitida
    public int maxdisk; //capacidad maxima de disco permitida

    /**
     * Por imperativo de Spring.
     */
    public Groupp()
    {}

    public Groupp(String name, User propietary)
    {
        this.user = propietary;
        this.name = name;
        this.date = new Date(new java.util.Date().getTime());
        this.users = new LinkedList<>();
        this.maxvms = 0;
        this.maxcores = 0;
        this.maxdisk = 0;
        this.maxmem = 0;
        this.maxfreq = 0;
        this.newUsers = new LinkedList<>();
        this.vms = new LinkedList<>();
    }

    public long getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public String getStringDate()
    {
        return this.date.toString();
    }

    public boolean equals(Groupp g)
    {
        return this.id==g.id;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.name;
    }

    public User findUser(String name)
    {
        if(this.user.name.equals(name))
            return this.user;
        for (User user : this.users)
            if(user.name.equals(name))
                return user;
        return null;
    }

    /**
     * Comprueba si tiene recursos asociados
     * @return
     * True si tiene recursos
     */
    public boolean haveResources()
    {
        return !vms.isEmpty();
    }

    public List<VM> getRequests()
    {
        List<VM> tmp = new LinkedList<>(this.vms);
        tmp.removeIf(a -> a.status.isDefined());
        return tmp;
    }

    public List<VM> getResources()
    {
        List<VM> tmp = new LinkedList<>(this.vms);
        tmp.removeIf(a -> a.status.isVoid());
        return tmp;
    }

    /**
     * Says if this group its a general group or a personal group.
     * @return True if is a personal group
     */
    public boolean personalArea()
    {
        return this.user.myGroup.equals(this);
    }
}
