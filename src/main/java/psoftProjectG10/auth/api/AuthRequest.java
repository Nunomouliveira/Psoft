package psoftProjectG10.auth.api;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class AuthRequest {
    @NotNull
    @Email
    String username;
    @NotNull
    String password;
}
