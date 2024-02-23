package eu.macarropueyo.terapweb.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SystemValues
{
    @Id
    private String k;
    
    public String value;

    public SystemValues()
    {}

    public SystemValues(String key, String value)
    {
        this.k=key;
        this.value=value;
    }

    public String getK()
    {
        return this.k;
    }
}
