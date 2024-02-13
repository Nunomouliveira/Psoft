package psoftProjectG10.planManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Plan")
public class RevenueByPlanView {

    @Schema(description = "Plan type (name of the plan)")
    private Long idPlan;

    private Double totalRevenue;
}