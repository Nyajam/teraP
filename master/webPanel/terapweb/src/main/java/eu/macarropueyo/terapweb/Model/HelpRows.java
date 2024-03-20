package eu.macarropueyo.terapweb.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class HelpRows {

    @Id
    public String name;

    public String link;

    public HelpRows(String name, String link)
    {
        this.name = name;
        this.link = link;
    }

    public HelpRows()
    {}
    
}
