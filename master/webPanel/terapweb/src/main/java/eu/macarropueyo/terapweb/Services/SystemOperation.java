package eu.macarropueyo.terapweb.Services;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.macarropueyo.terapweb.Repository.SystemRepository;
import eu.macarropueyo.terapweb.Model.*;

/**
 * Operaciones gennerales del sistema
 */
@Component
public class SystemOperation
{
    @Autowired
    SystemRepository sysRepo;
    /**
     * Boton nuclear, guarda el estado de las vm y se prepara para emergencia inminente.
     * @param vh
     */
    public void nuke()
    {
        Optional <SystemValues> tmp = sysRepo.findById("nuke"); //Recordar de la variable de estado es 'nuke' y los estados ARMED y LAUNCH
        if(tmp.isPresent())
        {
            if("ARMED".equals(tmp.get().value))
            {
                tmp.get().value = "LAUNCH";
                sysRepo.save(tmp.get());
                //Servicio interno
            }
            else
            {
                tmp.get().value = "ARMED";
                sysRepo.save(tmp.get());
                //Servicio interno
            }
        }
    }

    /**
     * True is an emergency
     * @return
     */
    public boolean isNuke()
    {
        return "LAUNCH".equals(getSystemValue("nuke"));
    }

    /**
     * Solicita al servicio interno un uuid
     * @return
     */
    public String getUuid()
    {
        return ""+(int)(Math.random()*1000000);
    }

    /**
     * Solicita al servicio interno generar una definicion a partir de una vm
     * @param vm
     * @return
     */
    public String generationDefinition(VM vm)
    {
        return "";
    }

    public String getSystemValue(String key)
    {
        Optional<SystemValues> tmp = sysRepo.findById(key);
        if(tmp.isPresent())
            return tmp.get().value;
        return "";
    }

    public void setSystemValue(String key, String value)
    {
        Optional<SystemValues> tmp = sysRepo.findById(key);
        if(tmp.isPresent())
        {
            tmp.get().value=value;
            sysRepo.save(tmp.get());
        }
        else
            sysRepo.save(new SystemValues(key, value));
    }

    /**
     * Genera una cadena aleatoria segura, se puede emplear en contraseñas o usos mas simples
     * @return
     */
    public String random()
    {
        return new Random().nextInt()+"";
    }

    /**
     * Envia un mensaje via mail al destino especificado
     * @param address
     * @param message
     */
    public void sendMail(String address, String message)
    {}
}
