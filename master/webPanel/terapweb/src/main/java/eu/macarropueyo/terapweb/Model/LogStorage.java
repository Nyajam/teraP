package eu.macarropueyo.terapweb.Model;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
/**
 * Entradas del log de los storages
 */
public class LogStorage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    /**
     * Ientificador de entreda
     */
    private long id;

    @ManyToOne
    /**
     * Storage al que pertenece esta entrada
     */
    public Storage storage;
    
    /**
     * Timestamp del momento de creacion de la entrada
     */
    public Date date;
    /**
     * Contenido de la entrada
     */
    public String message;

    /**
     * Por imperativo de spring
     */
    public LogStorage()
    {}

    /**
     * Crea una entrada del log de los storages
     * @param storage
     * @param message
     */
    public LogStorage(Storage storage, String message)
    {
        this.storage = storage;
        this.message = message;
        this.date = new Date(new java.util.Date().getTime());
    }

    /**
     * Retorna el valor del id de la entrada
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
            LogStorage tmp = (LogStorage) obj;
            return tmp.id == this.id;
        }
        return false;
    }
}
