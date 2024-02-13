package psoftProjectG10.planManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.userManagement.model.User;

import javax.validation.constraints.Min;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditPriceHistoryRequest {

    private User user;
    private Plan plan;
    @Min(0)
    private double oldPrice_monthlyFee;
    @Min(0)
    private double newPrice_monthlyFee;
    @Min(0)
    private double oldPrice_annualFee;
    @Min(0)
    private double newPrice_annualFee;
    @Min(0)
    private LocalDate changeDate;

}
