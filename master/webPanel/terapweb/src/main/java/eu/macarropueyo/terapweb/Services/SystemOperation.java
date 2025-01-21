package eu.macarropueyo.terapweb.Services;

import java.util.Optional;
import java.util.Random;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import eu.macarropueyo.terapweb.Repository.HelpRowsRepository;
import eu.macarropueyo.terapweb.Repository.SystemRepository;
import eu.macarropueyo.terapweb.Model.*;


@Component
public class SystemOperation
{
    @Autowired
    SystemRepository sysRepo;
    @Autowired
    HelpRowsRepository helpRepo;
    @Autowired
	private PasswordEncoder passwordEncoder;

    /**
     * Emergency mode, put all cluster in save and lock the operations by the users
     */
    public void nuke()
    {
        Optional <SystemValues> tmp = sysRepo.findById("nuke");
        if(tmp.isPresent())
        {
            if("on".equals(tmp.get().value))
            {
                tmp.get().value = "OFF";
                sysRepo.save(tmp.get());
                commandToInternalService("/nuke/off");
            }
            else
            {
                tmp.get().value = "ON";
                sysRepo.save(tmp.get());
                commandToInternalService("/nuke/on");
            }
        }
    }

    /**
     * True is an emergency
     * @return
     */
    public boolean isNuke()
    {
        return "ON".equals(getSystemValue("nuke"));
    }

    /**
     * Request a new uuid to the internal service
     * @return
     */
    public String getUuid()
    {
        return commandToInternalService("/getuuid");
    }

    /**
     * Request a definition from a vm to the internal service
     * @param vm
     * @return
     */
    public String generationDefinition(VM vm)
    {
        return commandToInternalService("/getdefinition/"+vm.getUuid());
    }

    /**
     * Return a value of the system
     * @param key
     * @return
     */
    public String getSystemValue(String key)
    {
        if(key == null)
            return "";
        Optional<SystemValues> tmp = sysRepo.findById(key);
        if(tmp.isPresent())
            return tmp.get().value;
        return "";
    }

    /**
     * Set (create or update) a value of the system
     * @param key
     * @param value
     */
    public void setSystemValue(@NonNull String key, String value)
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
     * Generate a secure random chain, can use for passwords
     * @return
     */
    public String random()
    {
        StringBuilder chain = new StringBuilder();
        for(int i = 0; i<8; i++)
            chain.append(new Random().nextInt());
        return passwordEncoder.encode(chain).toString();
    }

    /**
     * Send a email to a specific address
     * @param address
     * @param message
     */
    public void sendMail(String address, String message)
    {
        commandToInternalServicePost("/sendmail/"+address, "{\"msg\":\""+message+"\"}");
    }

    /**
     * Return all links of help
     * @return
     */
    public List<HelpRows> getAllHelpLinks()
    {
        return helpRepo.findAll();
    }

    /**
     * Remove a help link.
     * @param link cant be null or it name or link
     */
    public void removeHelpLink(HelpRows link)
    {
        if(link == null)
            return;
        if(link.name != null && link.link != null)
            helpRepo.delete(link);
    }

    /**
     * Add or update help link.
     * @param link cant be null or it name or link
     */
    public void addHelpLink(HelpRows link)
    {
        if(link == null)
            return;
        if(link.name != null && link.link != null)
            helpRepo.save(link);
    }

    /**
     * Return a help link with a name
     * @param name Name of the help link
     * @return HelpRows with the link, null if not exist
     */
    public HelpRows getHelpLink(String name)
    {
        if(name == null)
            return null;
        Optional<HelpRows> tmp = helpRepo.findByName(name);
        if(tmp.isPresent())
            return tmp.get();
        return null;
    }

    /**
     * Change allocation criteria.
     * @param order new criteria
     */
    public void setAllocationOrder(String order)
    {
        setSystemValue("allocationOrder", order);
        commandToInternalService("/allocationorder/"+order);
    }

    /**
     * Get current allocation criteria.
     * @return
     */
    public String getAllocationOrder()
    {
        return getSystemValue("allocationOrder");
    }

    /**
     * Change the algorithm to sort the requests queue.
     * @param mode
     */
    public void setQueueMode(String mode)
    {
        setSystemValue("queueMode", mode);
        commandToInternalService("/queuemode/"+mode);
    }

    /**
     * Get current algorithm to sort the requests queue.
     * @return
     */
    public String getQueueMode()
    {
        return getSystemValue("queueMode");
    }

    /**
     * Send a GET request to internal service
     * @param getPath GET request (only the path and values, like "/operation/param1/param2")
     * @return Response of the internal service (null if have problems)
     */
    public String commandToInternalService(String getPath)
    {
        //https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
        RestClient client = RestClient.builder().baseUrl("http://localhost:5000").build();
        String response;
        try
        {
            response = client.get().uri(getPath).retrieve().body(String.class);
        }
        catch(Exception e)
        {
            response = null;
        }
        return response;
    }

    /**
     * Send a POST request to internal service
     * @param postPath GET request (only the path and values, like "/operation/param1/param2")
     * @param post POST parameters, JSON, like {"param":"value","param2":"value2"}
     * @return Response of the internal service (null if have problems)
     */
    public String commandToInternalServicePost(String postPath, String post)
    {
        //https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
        //https://dev.to/agustinventura/usando-spring-boot-restclient-11og
        RestClient client = RestClient.builder().baseUrl("http://localhost:5000").build();
        String response;
        try
        {
            response = client.post().uri(postPath).contentType(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN).body(post).retrieve().body(String.class);
        }
        catch(Exception e)
        {
            response = null;
        }
        return response;
	}
}
