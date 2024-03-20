package eu.macarropueyo.terapweb.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
/**
 * Esta entidad represente a los discos virtuales hubicados en las cabinas y gestionados por las pool
 */
public class Vdisk
{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * Identificador de disco virtual
     */
    private long id;

    @ManyToOne
    /**
     * Storage en el que se hubica el disco
     */
    public Storage storage;
    @ManyToOne
    /**
     * Pool al que pertenece el disco
     */
    public Pool pool;

    /**
     * Tamano del disco
     */
    public int space;
    /**
     * Iniciador iSCSI asociado al disco
     */
    public String initiator;

    /**
     * Por imperativo de Spring.
     */
    public Vdisk()
    {}

    /**
     * Crea un disco en una "hubicacion" especifica, pero no actualiza las otras entidades
     * @param pool
     * @param stg
     * @param space
     * @param initiator
     */
    public Vdisk(Pool pool, Storage stg, int space, String initiator)
    {
        this.storage = stg;
        this.pool = pool;
        this.space = space;
        this.initiator = initiator;
    }

    /**
     * Retorna el identificador de la entidad
     * @return
     */
    public long getId()
    {
        return this.id;
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
            Vdisk tmp = (Vdisk) obj;
            return tmp.id == this.id;
        }
        return false;
    }
}
