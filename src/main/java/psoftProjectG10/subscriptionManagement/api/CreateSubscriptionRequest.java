package psoftProjectG10.subscriptionManagement.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateSubscriptionRequest extends GetIdPlanRequest {

    private Long user;

    private Long plan;

}