package psoftProjectG10.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import psoftProjectG10.userManagement.api.CreateUserRequest;
import psoftProjectG10.userManagement.api.UserView;
import psoftProjectG10.userManagement.api.UserViewMapper;
import psoftProjectG10.userManagement.model.Role;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.services.UserService;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.time.Instant;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Tag(name = "Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/account")
public class AuthApi {

    private final AuthenticationManager authenticationManager;

    private final JwtEncoder jwtEncoder;

    private final UserViewMapper userViewMapper;

    private final UserService userService;

    @Operation(summary = "Login")
    @PostMapping("login")
    public ResponseEntity<UserView> login(@RequestBody @Valid final AuthRequest request) {
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            final User user = (User) authentication.getPrincipal();

            final Instant now = Instant.now();
            final long expiry = 36000L; // 1 hours is usually too long for a token to be valid. adjust for production

            final String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .collect(joining(" "));

            final JwtClaimsSet claims = JwtClaimsSet.builder().issuer("example.io").issuedAt(now)
                    .expiresAt(now.plusSeconds(expiry)).subject(format("%s,%s", user.getId(), user.getUsername()))
                    .claim("roles", scope).build();

            final String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, token).body(userViewMapper.toUserView(user));
        } catch (final BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Register")
    @RolesAllowed({Role.ADMIN})
    @PostMapping("register")
    public UserView register(@RequestBody @Valid final CreateUserRequest request) {
        return userService.create(request);
    }


}
