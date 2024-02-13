package psoftProjectG10.deviceManagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditDeviceRequest {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    @Size(min=1, max=50)
    private String description;
}
