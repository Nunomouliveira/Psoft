package psoftProjectG10.userManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import psoftProjectG10.userManagement.model.Role;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserView {

	private String id;

	private String username;
	private String fullName;

	private Set<Role> authorities;
}
