package psoftProjectG10.planManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import psoftProjectG10.planManagement.model.PlanType_CashFlow;

import java.util.List;

@Data
@Schema(description = "Plan")
public class PlanViewFLOW {

    @Schema(description = "Plan type (name of the plan)")
    private Long idPlan;

    private List<PlanType_CashFlow> cashflows;
}