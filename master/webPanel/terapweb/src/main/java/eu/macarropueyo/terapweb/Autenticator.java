package eu.macarropueyo.terapweb;

import eu.macarropueyo.terapweb.Model.*;
import eu.macarropueyo.terapweb.Repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Autenticator implements UserDetailsService
{
	@Autowired
	private UserRepository repo;

    @Autowired
	private PasswordEncoder passwordEncoder;

    @Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException
	{
		Optional<User> candidatos=repo.findByName(name);
		if(candidatos.isEmpty())
			throw new UsernameNotFoundException("Error UserName, not exist");
		User usuario=candidatos.get();
		if(!usuario.isEnable())
			throw new UsernameNotFoundException("User lock by administartor.");
        String passwd=passwordEncoder.encode(usuario.getPassword()).toString();
        List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
		if(usuario.isRoot())
			roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		return new org.springframework.security.core.userdetails.User(name,passwd,roles);
    }
}
