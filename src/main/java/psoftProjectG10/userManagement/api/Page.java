package psoftProjectG10.userManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@AllArgsConstructor
@Data
public class Page {
	@Min(value = 1, message = "Paging must start with page 1")
	int number;

	@Min(value = 1, message = "You can request minimum 1 records")
	@Max(value = 100, message = "You can request maximum 100 records")
	int limit;

	public Page() {
		this(1, 10);
	}
}