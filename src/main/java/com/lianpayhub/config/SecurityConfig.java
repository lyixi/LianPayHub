package com.lianpayhub.config;

import com.lianpayhub.security.AdminIpWhitelistFilter;
import com.lianpayhub.security.AdminJwtAuthenticationFilter;
import com.lianpayhub.security.AdminOperationLogFilter;
import com.lianpayhub.security.AdminPasswordPolicyFilter;
import com.lianpayhub.security.AppUserJwtAuthenticationFilter;
import com.lianpayhub.security.ApiAppAuthenticationFilter;
import com.lianpayhub.security.JsonAccessDeniedHandler;
import com.lianpayhub.security.JsonAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminIpWhitelistFilter adminIpWhitelistFilter;
    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final AppUserJwtAuthenticationFilter appUserJwtAuthenticationFilter;
    private final AdminOperationLogFilter adminOperationLogFilter;
    private final AdminPasswordPolicyFilter adminPasswordPolicyFilter;
    private final ApiAppAuthenticationFilter apiAppAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(AdminIpWhitelistFilter adminIpWhitelistFilter,
                          AdminJwtAuthenticationFilter adminJwtAuthenticationFilter,
                          AppUserJwtAuthenticationFilter appUserJwtAuthenticationFilter,
                          AdminOperationLogFilter adminOperationLogFilter,
                          AdminPasswordPolicyFilter adminPasswordPolicyFilter,
                          ApiAppAuthenticationFilter apiAppAuthenticationFilter,
                          JsonAuthenticationEntryPoint authenticationEntryPoint,
                          JsonAccessDeniedHandler accessDeniedHandler) {
        this.adminIpWhitelistFilter = adminIpWhitelistFilter;
        this.adminJwtAuthenticationFilter = adminJwtAuthenticationFilter;
        this.appUserJwtAuthenticationFilter = appUserJwtAuthenticationFilter;
        this.adminOperationLogFilter = adminOperationLogFilter;
        this.adminPasswordPolicyFilter = adminPasswordPolicyFilter;
        this.apiAppAuthenticationFilter = apiAppAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(
                        "/admin/auth/login",
                        "/actuator/health",
                        "/actuator/info",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/console/**",
                        "/admin-ui/**"
                ).permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/api/sync/**", "/api/user/**", "/api/configs/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .addFilterBefore(adminIpWhitelistFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminJwtAuthenticationFilter, AdminIpWhitelistFilter.class)
                .addFilterAfter(adminPasswordPolicyFilter, AdminJwtAuthenticationFilter.class)
                .addFilterAfter(appUserJwtAuthenticationFilter, AdminPasswordPolicyFilter.class)
                .addFilterAfter(apiAppAuthenticationFilter, AppUserJwtAuthenticationFilter.class)
                .addFilterAfter(adminOperationLogFilter, AdminJwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }
}
