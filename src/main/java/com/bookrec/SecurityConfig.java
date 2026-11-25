package com.bookrec;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
  @Bean PasswordEncoder encoder(){ return new BCryptPasswordEncoder(); }
  @Bean UserDetailsService uds(UserRepo r){
    return n -> r.findByUsername(n)
      .map(u -> (UserDetails) User.withUsername(u.username).password(u.password).roles(u.role).build())
      .orElseThrow(() -> new UsernameNotFoundException(n));
  }
  @Bean SecurityFilterChain chain(HttpSecurity h) throws Exception {
    h.csrf(c -> c.disable())
     .authorizeHttpRequests(a -> a
        .requestMatchers("/","/index.html","/api/auth/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated())
     .httpBasic(b -> b.authenticationEntryPoint((q,s,e) -> s.sendError(401,"Invalid credentials")));
    return h.build();
  }
}
