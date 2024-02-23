package eu.macarropueyo.terapweb.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class DiskExpansion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    public VM vm;

    public int expansion;

    public DiskExpansion()
    {}

    public DiskExpansion(int extraSpace, VM vm)
    {
        this.expansion = extraSpace;
        this.vm = vm;
    }

    public long getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return vm.getUuid()+";"+vm.group.getName();
    }
}
