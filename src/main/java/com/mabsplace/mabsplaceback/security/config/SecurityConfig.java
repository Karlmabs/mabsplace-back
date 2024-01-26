package com.mabsplace.mabsplaceback.security.config;

import com.mabsplace.mabsplaceback.security.CustomUserDetailsService;
import com.mabsplace.mabsplaceback.security.TokenAuthenticationFilter;
import com.mabsplace.mabsplaceback.security.jwt.AuthEntryPointJwt;
import com.mabsplace.mabsplaceback.security.jwt.AuthTokenFilter;
import com.mabsplace.mabsplaceback.security.oauth2.CustomOAuth2UserService;
import com.mabsplace.mabsplaceback.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.mabsplace.mabsplaceback.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.mabsplace.mabsplaceback.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.mabsplace.mabsplaceback.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private CustomUserDetailsService customUserDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
    return new HttpCookieOAuth2AuthorizationRequestRepository();
  }

  @Bean
  public TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter();
  }

  @Autowired
  private CustomOAuth2UserService customOAuth2UserService;

  @Autowired
  private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

  @Autowired
  private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/swagger-ui/**").permitAll()
                            .requestMatchers("/v3/**").permitAll()
                            .requestMatchers("/api/test/**").permitAll()
                            .anyRequest().authenticated()
            ).oauth2Login(oauth2 ->
                    oauth2.authorizationEndpoint( auth ->
                                    auth.baseUri( "/oauth2/authorize" )
                                            .authorizationRequestRepository( cookieAuthorizationRequestRepository() )
                            )
                            .redirectionEndpoint( redir ->
                                    redir.baseUri( "/oauth2/callback/*" )
                            )
                            .userInfoEndpoint( userInfo ->
                                    userInfo.userService( customOAuth2UserService )
                            )
                            .successHandler(oAuth2AuthenticationSuccessHandler)
                            .failureHandler(oAuth2AuthenticationFailureHandler)
            );

    // fix H2 database console: Refused to display ' in a frame because it set 'X-Frame-Options' to 'deny'
    http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

    http.authenticationProvider(authenticationProvider());

    http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(){
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(customUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


}

