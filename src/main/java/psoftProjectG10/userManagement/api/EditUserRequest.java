package psoftProjectG10.userManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditUserRequest {
	@NotBlank
	private String fullName;

	private Set<String> authorities;
}
