package psoftProjectG10.planManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Plan")
public class PlanView {

    private Long id;
    @Schema(description = "Plan type (name of the plan)")
    private String planType;

    private Integer numMinutes;

    private Integer maxDevices;

    private Integer musicCollections;

    private String musicSuggestions;

    private Double monthlyFee;

    private Double annualFee;

    private String planState;

    private String planPromote;

    private String htmlDescription;
}