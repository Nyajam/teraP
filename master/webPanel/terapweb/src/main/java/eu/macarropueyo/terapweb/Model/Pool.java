package eu.macarropueyo.terapweb.Model;

import java.util.LinkedList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
/**
 * Clase que representa a las pool de discos
 */
public class Pool
{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * Identificador de la pool
     */
    private long id;

    @OneToMany(mappedBy = "pool")
    /**
     * Lista de discos de la pool
     */
    public List<Vdisk> vdisks;
    @OneToOne
    /**
     * Vm propietaria de la pool
     */
    public VM vm;

    /**
     * Agrupacion logica de initiators de los discos de la pool
     */
    public String target;

    /**
     * Por imperativo de Spring.
     */
    public Pool()
    {}

    /**
     * Construye una pool para vm con una target
     * @param vm
     * @param target
     */
    public Pool(VM vm, String target)
    {
        this.vdisks=new LinkedList<Vdisk>();
        this.vm=vm;
        this.target=target;
    }

    /**
     * Retorna el identificador de la pool
     * @return
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Retorna el espacio que ocupan todos los discos de la pool
     * @return
     */
    public int getSpace()
    {
        int tmp=0;
        for(Vdisk dsk:this.vdisks)
            tmp=tmp+dsk.space;
        return tmp;
    }

    /**
     * Retorna true si no tiene discos asociados o no esta inicializada (necesita una target para estarlo)
     * @return
     */
    public boolean isEmpty()
    {
        return vdisks.isEmpty() && ( target=="" || target==null);
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
            Pool tmp = (Pool) obj;
            return tmp.id == this.id;
        }
        return false;
    }
}