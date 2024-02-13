package psoftProjectG10.deviceManagement.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateDeviceRequest extends EditDeviceRequest {

    @NotNull
    @NotBlank
    private String macAddress;

    private Long subscription;
}
