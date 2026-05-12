package com.re.project.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. TẮT CSRF để tránh lỗi Forbidden khi gọi Fetch API/POST từ JS
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 2. TÀI NGUYÊN CÔNG KHAI (Ưu tiên số 1: Luôn nằm đầu tiên)
                        .requestMatchers("/", "/login", "/register", "/css/**", "/script/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/**").permitAll()

                        // 3. PHÂN QUYỀN THEO ROLE (Ưu tiên số 2)
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/lecturer/**").hasAuthority("ROLE_LECTURER")
                        .requestMatchers("/student/**").hasAuthority("ROLE_STUDENT")

                        // 4. CÁC TRANG DÙNG CHUNG YÊU CẦU ĐĂNG NHẬP
                        .requestMatchers("/profile/**").authenticated()

                        // 5. CHỐT CHẶN CUỐI CÙNG (Luôn nằm ở dưới cùng)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(roleBasedSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else if (role.equals("ROLE_LECTURER")) {
                response.sendRedirect("/lecturer/dashboard");
            } else if (role.equals("ROLE_STUDENT")) {
                response.sendRedirect("/student/dashboard");
            } else {
                response.sendRedirect("/login?error=unauthorized");
            }
        };
    }
}