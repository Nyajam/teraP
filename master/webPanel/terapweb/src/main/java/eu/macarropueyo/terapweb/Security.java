package eu.macarropueyo.terapweb;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter
{
    @Autowired
	private Autenticator lista;
    
    @Override
    protected void configure(HttpSecurity web) throws Exception
    {
        //Paginas publicas
        web.authorizeRequests().antMatchers("/").permitAll();
        web.authorizeRequests().antMatchers("/login").permitAll();
        web.authorizeRequests().antMatchers("/logout").permitAll();
        web.authorizeRequests().antMatchers("/fail").permitAll();

        //Paginas privadas
        web.authorizeRequests().antMatchers("/home").hasAnyRole("USER");
        web.authorizeRequests().antMatchers("/myuser").hasAnyRole("USER");
        web.authorizeRequests().antMatchers("/help").hasAnyRole("USER");
        web.authorizeRequests().antMatchers("/admin").hasAnyRole("ADMIN");

        //Login & logout
        web.formLogin().loginPage("/");
		web.formLogin().usernameParameter("user");
		web.formLogin().passwordParameter("passwd");
		web.formLogin().defaultSuccessUrl("/home");
		web.formLogin().failureUrl("/fail");
        web.logout().logoutUrl("/logout");
		web.logout().logoutSuccessUrl("/");

        web.csrf().disable();
    }

    @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.userDetailsService(lista).passwordEncoder(passwordEncoder());
    }

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder(10, new SecureRandom());
	}
}