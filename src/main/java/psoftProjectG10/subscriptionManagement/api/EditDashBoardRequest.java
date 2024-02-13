package psoftProjectG10.subscriptionManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditDashBoardRequest {

    private LocalDate startDate;

    private LocalDate endDate;

}
