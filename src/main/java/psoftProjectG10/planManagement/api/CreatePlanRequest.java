package psoftProjectG10.planManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanRequest {

    private String planType;

    @Min(0)
    private Integer numMinutes;

    @Min(0)
    private Integer maxDevices;

    @Min(0)
    private Integer musicCollections;

    private String musicSuggestions;

    @Min(0)
    private Double monthlyFee;

    @Min(0)
    private Double annualFee;

    private String htmlDescription;
}
