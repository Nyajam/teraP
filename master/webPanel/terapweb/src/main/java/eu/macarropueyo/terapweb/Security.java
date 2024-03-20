package eu.macarropueyo.terapweb;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class Security
{
    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/", "/fail").permitAll()
                .requestMatchers("/admin").hasAnyRole("ADMIN")
				.anyRequest().authenticated()
			)
			.formLogin((form) -> form
				.loginPage("/")
				//.permitAll()
                .defaultSuccessUrl("/home")
                .failureUrl("/fail")
			)
			.logout((logout) -> logout
                .permitAll()
                .logoutSuccessUrl("/")
            );
        http.csrf(csrf -> csrf.disable());

		return http.build();
	}

    @Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder(10, new SecureRandom());
	}
}