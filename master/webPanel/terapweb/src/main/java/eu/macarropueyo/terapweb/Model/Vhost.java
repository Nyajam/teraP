package eu.macarropueyo.terapweb.Model;

import java.sql.Date;
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
 * Clase que representa a los host de las vm
 */
public class Vhost
{
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    /**
     * Identificador del vhost
     */
    private long id;

    @OneToMany(mappedBy = "host")
    /**
     * Lista de VMs en el host
     */
    public List<VM> vms;
    
    @Column(unique=true)
    /**
     * Direccion de red del vhost
     */
    public String ip;

    /**
     * Numero de nucleos de CPU que dispone el vhost
     */
    public int cores;
    /**
     * Frecuencia de(l) procesador(es) del vhost
     */
    public int freq;
    /**
     * Memoria principal que dispone el vhost
     */
    public int mem;
    /**
     * Ultima comprobacion realizada por el sistema
     */
    public Date lastCheck;
    /**
     * Ultima actualizacion de sistema del vhost
     */
    public Date lastUpdate;
    /**
     * Estado del vhost
     */
    public StatusHost status;

    /**
     * Por imperativo de Spring.
     */
    public Vhost()
    {}

    /**
     * Contruye un vhost cerrado y con check y update al comienzo del reloj
     * @param ip
     * @param cores
     * @param freq
     * @param mem
     */
    public Vhost(String ip, int cores, int freq, int mem)
    {
        this.vms=new LinkedList<>();
        this.ip=ip;
        this.cores=cores;
        this.freq=freq;
        this.mem=mem;
        this.status=StatusHost.CLOSE;
        this.lastCheck=new Date(0);
        this.lastUpdate=new Date(0);
    }

    /**
     * Pregunta si un host acepta nuevas VM
     * @return
     * True es que si aceptan
     */
    public boolean acceptVM()
    {
        return this.status.acceptEntries();
    }

    /**
     * Retorna el identificador del vhost
     * @return
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Recursos usados
     * @return
     * 0->cores, 1->memory
     */
    public int[] getUse()
    {
        int cpu = 0;
        int ram = 0;
        for (VM vms : this.vms)
        {
            cpu=cpu+vms.cores; //cores empleados
            ram=ram+vms.mem; //memoria empleada
        }
        return new int[]{cpu, ram};
    }

    /**
     * Recursos disponibles
     * @return
     * 0->cores, 1->memory
     */
    public int[] getNoneUse()
    {
        int cpu;
        int ram;
        int[] use = this.getUse();
        cpu = this.cores-use[0]; //capacidad actual es, la total menos la usada
        ram = this.mem-use[1]; //capacidad actual es, la total menos la usada
        return new int[]{cpu, ram};
    }

    /**
     * Comprueba si "cabe" una vm
     * @param vm
     * @return
     * True si hay espacio para ella
     */
    public boolean fitVm(VM vm)
    {
        int[] use = this.getNoneUse();
        int cpu = use[0];
        int ram = use[1];
        return cpu >= vm.cores && ram >=vm.mem;
    }

    @Override
    /**
     * Convierte al vhost a String (lo que mas le representa)
     */
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
            Vhost tmp = (Vhost) obj;
            return tmp.id == this.id;
        }
        return false;
    }

    /**
     * Ratio [0,1] de tasa de ocupacion del vhost, es la media de tasa de ocupacion de cores y la memoria
     * @return
     */
    public double ratio()
    {
        int cores = 0;
        int memory = 0;
        for (VM vm : vms)
        {
            cores = cores + vm.cores;
            memory = memory + vm.mem;
        }
        return ((cores/(double)this.cores)+(memory/(double)this.mem)) / 2.0;
    }
}
