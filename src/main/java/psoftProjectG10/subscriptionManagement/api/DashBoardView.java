package psoftProjectG10.subscriptionManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DashBoard")
public class DashBoardView {
    private Integer cancellations;

    private Integer subscriptions;


}
