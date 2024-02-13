package psoftProjectG10.subscriptionManagement.api;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditSubscriptionRequest {

    private String feeType;

    private Long idPlan;
}
