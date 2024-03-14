package eu.macarropueyo.terapweb.Model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
/**
 * Clase que representa a las maquinas virtuales
 */
public class VM
{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * Identificador de la maquina virtual
     */
    private long id;

    @ManyToOne
    /**
     * Grupo propietario de la vm
     */
    public Groupp group;
    @ManyToOne
    /**
     * Host donde esta alojada la vm (si esta inicializada)
     */
    public Vhost host;
    @OneToOne(mappedBy = "vm")
    /**
     * Pool de discos de la vm
     */
    public Pool pool;
    @OneToOne(mappedBy = "vm")
    /**
     * Solicitudes de expansion de disco
     */
    public DiskExpansion expansion;
    
    /**
     * Identificador de la vm en el cluster 
     */
    private String uuid;
    /**
     * Nombre asignado a la vm
     */
    public String name;
    /**
     * Estado de la VM
     */
    public StatusVM status;
    /**
     * Ubicacion de la definicion de la VM
     */
    public String definition;
    /**
     * Nucleos de CPU que dispone esta VM
     */
    public int cores;
    /**
     * Frecuencia requerida por la VM, en MHz
     */
    public int freq;
    /**
     * Memoria que emplea la VM (en MiB)
     */
    public int mem;
    /**
     * Fecha de creacion de la VM (que al inicio es una request)
     */
    public Date date;
    /**
     * Puerto de acceso a la conexion de la VM
     */
    public int port;
    /**
     * Token de acceso a la VM
     */
    public String token;

    /**
     * Por imperativo de Spring.
     */
    public VM()
    {}

    /**
     * Crea una VM sin definir: una request
     * @param g
     * @param uuid
     * @param name
     * @param cores
     * @param freq
     * @param mem
     */
    public VM(Groupp g, String uuid, String name, int cores, int freq, int mem)
    {
        this.group=g;
        this.host=null;
        this.pool=null;
        this.uuid=uuid;
        this.name=name;
        this.status=StatusVM.NOTDEFINED;
        this.definition="";
        this.cores=cores;
        this.freq=freq;
        this.mem=mem;
        date = new Date(new Date().getTime());
        this.token="";
        this.port=-1;
        this.expansion = null;
    }

    /**
     * Define una VM en un espacio especifico
     * @param host
     * @param pool
     * @param def
     */
    public void setDefinition(Vhost host, Pool pool, String def)
    {
        this.host=host;
        this.pool=pool;
        this.definition=def;
        this.status=StatusVM.READY;
    }

    /**
     * Retorna true si la VM esta definida
     * @return
     */
    public boolean isDefine()
    {
        return !(this.status.equals(StatusVM.NOTDEFINED)||this.status.equals(StatusVM.ERRORTODEFINE));
    }

    /**
     * Retorna el UUID de la VM
     * @return
     */
    public String getUuid()
    {
        return this.uuid;
    }

    /**
     * Marca el estado de la VM como no definido
     */
    public void setStatusIsVoid()
    {
        this.status=StatusVM.NOTDEFINED;
    }

    /**
     * Marca la VM como error al definir
     */
    public void setStatusIsNotDefinedByError()
    {
        this.status=StatusVM.ERRORTODEFINE;
    }
    
    /**
     * Marca a la VM como que ha habido un error o problema, pero esta definida
     */
    public void setStatusIsAnError()
    {
        this.status=StatusVM.ERROR;
    }
    
    /**
     * Marca la VM como operativa
     */
    public void setStatusIsOperative()
    {
        this.status=StatusVM.READY;
    }

    /**
     * Retorna el espacio en disco que ocupa (su pool)
     * @return
     */
    public int getSpace()
    {
        if(this.pool==null)
            return 0;
        return this.pool.getSpace();
    }

    /**
     * Comprueba si esta vm es de este usuario (puede operarla)
     * @param user
     * @return
     * True si es suya
     */
    public boolean isMine(User user)
    {
        if(supervisor(user))
            return true;
        for (Groupp grupo : user.groups)
            if(this.group.getId() == grupo.getId())
                return true;
        return false;
    }

    /**
     * Comprueba si esta vm es responsabilidad de este usuario (puede borrarla)
     * @param user
     * @return
     * True si es responsable
     */
    public boolean supervisor(User user)
    {
        if(this.group.getId() == user.myGroup.getId())
            return true;
        for (Groupp grupo : user.mainGroupps)
            if(this.group.getId() == grupo.getId())
                return true;
        return false;
    }

    @Override
    /**
     * Indica si el objeto indicado es el mismo
     */
    public boolean equals(Object obj)
    {
        if(obj == null)
            return false;
        if(obj.getClass()==this.getClass())
        {
            VM tmp = (VM) obj;
            return tmp.id == this.id;
        }
        return false;
    }
}
