package eu.macarropueyo.terapweb.Model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
/**
 * Entradas del log de los vhosts
 */
public class LogHost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    /**
     * Ientificador de entreda
     */
    private long id;

    @ManyToOne
    /**
     * Vhost al que pertenece esta entrada
     */
    public Vhost vhost;
    
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
    public LogHost()
    {}

    /**
     * Crea una entrada del log de los vhosts
     * @param storage
     * @param message
     */
    public LogHost(Vhost vhost, String message)
    {
        this.vhost = vhost;
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
            LogHost tmp = (LogHost) obj;
            return tmp.id == this.id;
        }
        return false;
    }
}
