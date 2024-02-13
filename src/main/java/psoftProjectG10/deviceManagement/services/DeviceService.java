package psoftProjectG10.deviceManagement.services;

import psoftProjectG10.deviceManagement.api.CreateDeviceRequest;
import psoftProjectG10.deviceManagement.api.EditDeviceRequest;
import psoftProjectG10.deviceManagement.model.Device;

import java.util.Optional;

public interface DeviceService {
    Iterable<Device> findAll(int page, Integer size);
    Optional<Device> findOne(String macAddress);

    Iterable<Device> listMyDevices(Long id,int page, Integer size);

    Device create(CreateDeviceRequest resource);

    Device update(String macAddress, EditDeviceRequest resource, long desiredVersion);

    Device partialUpdate(String macAddress, EditDeviceRequest resource, long parseLong);
    int deleteById(String macAddress, long desiredVersion);

}
