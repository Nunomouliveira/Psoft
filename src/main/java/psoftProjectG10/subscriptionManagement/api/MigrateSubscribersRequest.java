package psoftProjectG10.subscriptionManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrateSubscribersRequest {

    private Long currentPlanId;

    private Long newPlanId;
}
