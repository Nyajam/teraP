package eu.macarropueyo.terapweb.Model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
/**
 * Clase que representa la entidad de una cabina en el cluster
 */
public class Storage
{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * Identificador de indice
     */
    private long id;

    @OneToMany(mappedBy = "storage")
    /**
     * Lista de discos virtuales asociados a este storage
     */
    public List<Vdisk> vdisks;

    @Column(unique = true)
    /**
     * Direccion de red del storage
     */
    public String ip;
    
    /**
     * Tamano del storage, en GiB
     */
    public int space;
    /**
     * Ancho de banda de la conexion del storage en Mbps (en total)
     */
    public int bandwidth;
    /**
     * Estado del storage
     */
    public StatusHost status;
    /**
     * Usuario de acceso al storage
     */
    public String user;

    /**
     * Por imperativo de Spring.
     */
    public Storage()
    {}

    /**
     * Crea un storage nuevo segun los parametros indicados, vacio y en estado cerrado
     * @param ip
     * @param space
     * @param bandwidth
     */
    public Storage(String ip, int space, int bandwidth, String user)
    {
        this.vdisks=new LinkedList<>();
        this.ip=ip;
        this.space=space;
        this.bandwidth=bandwidth;
        this.status=StatusHost.CLOSE;
        this.user = user;
    }

    /**
     * Retorna el identificador del storage
     * @return
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Retorna el espacio libre en el storage (en GiB)
     * @return
     */
    public int getFreeSpace()
    {
        int ocu=0;
        for (Vdisk v : vdisks) 
            ocu=ocu+v.space;
        return this.space-ocu;
    }

    /**
     * Ratio [0,1] de tasa de ocupacion del storage, solo tiene encuenta los discos
     * @return
     */
    public double ratio()
    {
        return ((this.space-getFreeSpace()) / (double)this.space);
    }

    /**
     * Convierte al storage a String (lo que mas le representa)
     */
    @Override
    public String toString()
    {
        return this.ip;
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
            Storage tmp = (Storage) obj;
            return tmp.id == this.id;
        }
        return false;
    }
}
