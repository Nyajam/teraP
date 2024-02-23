package eu.macarropueyo.terapweb.Model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class User
{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "user")
    public List<Groupp> mainGroupps;
    @ManyToMany(mappedBy = "users")
    public List<Groupp> groups;
    @Column(unique=true)
    public String name;
    @ManyToMany(mappedBy = "newUsers")
    public List<Groupp> joinToGroups;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    public Groupp myGroup;

    public StatusUser status;
    public boolean root;
    public String mail;
    private String password;

    public User()
    {}

    public User(String name, StatusUser status, boolean root)
    {
        this.name=name;
        this.status=status;
        this.root=root;
        this.mainGroupps = new LinkedList<>();
        this.myGroup = null;
        groups = new LinkedList<>();
        this.mail="";
        this.password="";
        joinToGroups = new LinkedList<>();
    }

    public void setPassword(String password)
    {
        this.password=password;
    }

    public String getPassword()
    {
        return this.password;
    }

    public boolean comparePassword(String password)
    {
        return this.password.equals(password);
    }

    public void block()
    {
        this.status=StatusUser.BLOCK;
    }

    public void unBlock()
    {
        this.status=StatusUser.OK;
    }

    public boolean isEnable()
    {
        return !this.status.isBlock();
    }

    public boolean isRoot()
    {
        return this.root;
    }

    public void setNotRoot()
    {
        this.root=false;
    }

    public void setRoot()
    {
        this.root=true;
    }

    public long getId()
    {
        return this.id;
    }

    public boolean haveResources()
    {
        for (Groupp group : mainGroupps) //Si todos sus grupos administrados no tienen recursos
            if(!group.vms.isEmpty())
                return true;
        return false;
    }

    public boolean administrationGroups()
    {
        return mainGroupps.size()>1;
    }

    public List<Groupp> myGroups()
    {
        List<Groupp> tmp = new LinkedList<>();
        tmp.addAll(this.mainGroupps);
        tmp.addAll(this.groups);
        tmp.remove(this.myGroup);
        return tmp;
    }
}