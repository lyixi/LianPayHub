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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private final String corsAllowedOriginPatterns;

    public SecurityConfig(AdminIpWhitelistFilter adminIpWhitelistFilter,
                          AdminJwtAuthenticationFilter adminJwtAuthenticationFilter,
                          AppUserJwtAuthenticationFilter appUserJwtAuthenticationFilter,
                          AdminOperationLogFilter adminOperationLogFilter,
                          AdminPasswordPolicyFilter adminPasswordPolicyFilter,
                          ApiAppAuthenticationFilter apiAppAuthenticationFilter,
                          JsonAuthenticationEntryPoint authenticationEntryPoint,
                          JsonAccessDeniedHandler accessDeniedHandler,
                          @Value("${lianpayhub.security.cors-allowed-origin-patterns:*}") String corsAllowedOriginPatterns) {
        this.adminIpWhitelistFilter = adminIpWhitelistFilter;
        this.adminJwtAuthenticationFilter = adminJwtAuthenticationFilter;
        this.appUserJwtAuthenticationFilter = appUserJwtAuthenticationFilter;
        this.adminOperationLogFilter = adminOperationLogFilter;
        this.adminPasswordPolicyFilter = adminPasswordPolicyFilter;
        this.apiAppAuthenticationFilter = apiAppAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.corsAllowedOriginPatterns = corsAllowedOriginPatterns;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(splitCsv(corsAllowedOriginPatterns));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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

    private List<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }
}
