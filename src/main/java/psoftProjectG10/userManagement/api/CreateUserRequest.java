package psoftProjectG10.userManagement.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
	@NonNull
	@NotBlank
	@Email
	private String username;

	@NonNull
	@NotBlank
	private String fullName;

	@NonNull
	@NotBlank
	private String password;

	@NonNull
	@NotBlank
	private String rePassword;

	private Set<String> authorities = new HashSet<>();

	public CreateUserRequest(final String username, final String fullName, final String password) {
		this.username = username;
		this.fullName = fullName;
		this.password = password;
		this.rePassword = password;
	}
}
