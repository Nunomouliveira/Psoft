package psoftProjectG10.userManagement.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Entity
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Getter
    private LocalDateTime modifiedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    @Getter
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String modifiedBy;

    @Setter
    @Getter
    private boolean enabled = true;

    @Column(unique = true, updatable = false, nullable = false)
    @Email
    @Getter
    @NotNull
    @NotBlank
    @Size(min=1, max=50)
    private String username;

    @Column(nullable = false)
    @Getter
    @NotNull
    @NotBlank
    @Size(min=8)
    private String password;

    @Getter
    @Setter
    private String fullName;

    @ElementCollection
    @Getter
    private final Set<Role> authorities = new HashSet<>();

    protected User() {
    }

    public User(final String username, final String password, final String fullName) {
        this.username = username;
        setPassword(password);
        setFullName(fullName);
    }

    public static User newUser(final String username, final String password, final String fullName) {
        final var u = new User(username, password, fullName);
        return u;
    }

    public static User newUser(final String username, final String password, final String fullName, final String role) {
        final var u = new User(username, password, fullName);
        u.addAuthority(new Role(role));
        return u;
    }

    public void setPassword(final String password) {
        this.password = Objects.requireNonNull(password);
    }

    public void addAuthority(final Role r) {
        Role r1 = new Role(Role.ADMIN);
        Role r2 = new Role(Role.CUSTOMER);
        Role r3 = new Role(Role.SUBSCRIBER);
        Role r4 = new Role(Role.MARKETING_DIRECTOR);
        Role r5 = new Role(Role.PRODUCT_MANAGER);
        Role r6 = new Role(Role.FINANCIAL_DIRECTOR);
        if(r.equals(r1) || r.equals(r2) || r.equals(r3) || r.equals(r4) || r.equals(r5) || r.equals(r6))
        {
            authorities.add(r);
        }
        else
        {
            throw new IllegalArgumentException("The authority must be: ADMIN, CUSTOMER, SUBSCRIBER, MARKETING_DIRECTOR, FINANCIAL_DIRECTOR or PRODUCT_MANAGER");
        }

    }
    public String retrieveDataFromApi() throws IOException {
        String baseUrl = "https://api.api-ninjas.com/v1/quotes?category=funny";

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(baseUrl);
            request.setHeader("X-Api-Key", "uoRAzOXc9UTovV0+dm8ZxA==tfRtWOhzVmr0nRlY");

            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);
            String content = jsonNode.get(0).get("quote").asText();


            return content;
        }
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }
}