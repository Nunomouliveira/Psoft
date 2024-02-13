package psoftProjectG10.configuration;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import psoftProjectG10.userManagement.model.Role;
import psoftProjectG10.userManagement.repositories.UserRepository;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static java.lang.String.format;
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final UserRepository userRepo;

	@Value("${jwt.public.key}")
	private RSAPublicKey rsaPublicKey;

	@Value("${jwt.private.key}")
	private RSAPrivateKey rsaPrivateKey;

	@Value("${springdoc.api-docs.path}")
	private String restApiDocPath;

	@Value("${springdoc.swagger-ui.path}")
	private String swaggerPath;

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(username -> userRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(format("User: %s, not found", username))));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http = http.cors().and().csrf().disable();

		http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();

		http = http.exceptionHandling(
				exceptions -> exceptions.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
						.accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

		// Set permissions on endpoints
		http.authorizeRequests()
				// Swagger endpoints must be publicly accessible
				.antMatchers("/").permitAll().antMatchers(format("%s/**", restApiDocPath)).permitAll()
				.antMatchers(format("%s/**", swaggerPath)).permitAll()
				// Our public endpoints
				.antMatchers("/api/account/**").permitAll()
				.antMatchers(HttpMethod.GET, "/api/plan/**").permitAll()
				.antMatchers(HttpMethod.GET, "/api/subscription/**").permitAll()
				.antMatchers(HttpMethod.GET, "/api/device/**").permitAll()
				// Our private endpoints
				.antMatchers("/api/plan/**").hasAnyRole(Role.ADMIN, Role.MARKETING_DIRECTOR, Role.CUSTOMER, Role.FINANCIAL_DIRECTOR)
				.antMatchers("/api/subscription/**").hasAnyRole(Role.ADMIN, Role.SUBSCRIBER, Role.CUSTOMER, Role.PRODUCT_MANAGER,Role.FINANCIAL_DIRECTOR)
				.antMatchers("/api/device/**").hasAnyRole(Role.ADMIN, Role.SUBSCRIBER)
				.antMatchers("/api/user/**").hasAnyRole(Role.ADMIN, Role.SUBSCRIBER, Role.PRODUCT_MANAGER, Role.MARKETING_DIRECTOR, Role.CUSTOMER, Role.FINANCIAL_DIRECTOR)
				.anyRequest().authenticated()
				// Set up oauth2 resource server
				.and().httpBasic(Customizer.withDefaults()).oauth2ResourceServer().jwt();
	}

	// Used by JwtAuthenticationProvider to generate JWT tokens
	@Bean
	public JwtEncoder jwtEncoder() {
		final JWK jwk = new RSAKey.Builder(this.rsaPublicKey).privateKey(this.rsaPrivateKey).build();
		final JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}

	// Used by JwtAuthenticationProvider to decode and validate JWT tokens
	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
	}

	// Extract authorities from the roles claim
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
		jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

		final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	// Set password encoding schema
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Used by spring security if CORS is enabled.
	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	// Expose authentication manager bean
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}