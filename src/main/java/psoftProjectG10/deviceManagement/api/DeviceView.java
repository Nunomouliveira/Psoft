package psoftProjectG10.deviceManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import psoftProjectG10.subscriptionManagement.api.SubscriptionView;


@Data
@Schema(description = "Device")
public class DeviceView {

    private String macAddress;

    private String name;

    private String description;

    private SubscriptionView Subscription;
}
