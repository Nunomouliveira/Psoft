package psoftProjectG10.deviceManagement.api;

import org.mapstruct.Mapper;
import psoftProjectG10.deviceManagement.model.Device;

import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class DeviceViewMapper {

    public abstract DeviceView toDeviceView(Device device);

    public abstract Iterable<DeviceView> toDeviceView(Iterable<Device> devices);

    public Integer mapOptInt(final Optional<Integer> i) {
        return i.orElse(null);
    }

    public Long mapOptLong(final Optional<Long> i) {
        return i.orElse(null);
    }

    public String mapOptString(final Optional<String> i) {
        return i.orElse(null);
    }
}
