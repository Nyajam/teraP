package eu.macarropueyo.terapweb.Services;

import java.util.Optional;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
     * Send a GET request to internal service
     * @param getPath GET request (only the path and values, like "/operation/param1/param2")
     * @return Response of the internal service (null if have problems)
     */
    public String commandToInternalService(String getPath)
    {
        HttpURLConnection connection = null;
        String address = "http://localhost:5000";
        try
        {
            URL url = new URL(address+getPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);

            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    /**
     * Send a POST request to internal service
     * @param postPath GET request (only the path and values, like "/operation/param1/param2")
     * @param post POST parameters, JSON, like "{'param':'value','param2':'value2'}" (dont use ' )
     * @return Response of the internal service (null if have problems)
     */
    public String commandToInternalServicePost(String postPath, String post)
    {
        //https://www.digitalocean.com/community/tutorials/java-httpurlconnection-example-java-http-request-get-post
        HttpURLConnection connection = null;
        String address = "http://localhost:5000";
        try
        {
            URL url = new URL(address+postPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "teraPwebpanel");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Expect", "100-continue");

            // For POST only - START
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(post.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            }
            else
                return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
	}
}
