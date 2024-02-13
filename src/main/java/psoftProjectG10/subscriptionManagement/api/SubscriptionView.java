package psoftProjectG10.subscriptionManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import psoftProjectG10.planManagement.api.PlanView;
import psoftProjectG10.subscriptionManagement.model.SubscriptionStatus;
import psoftProjectG10.userManagement.api.UserView;

import java.time.LocalDate;

@Data
@Schema(description = "Subscription")
public class SubscriptionView {

    private Long id;

    private LocalDate startDate;

    private String feeType;

    private LocalDate renewDate;

    private SubscriptionStatus subState;

    private LocalDate cancelDate;

    private String funnyQuote;

    private String[] weather;

    private PlanView plan;

    private UserView user;
}
