package psoftProjectG10.deviceManagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psoftProjectG10.deviceManagement.api.CreateDeviceRequest;
import psoftProjectG10.deviceManagement.api.EditDeviceRequest;
import psoftProjectG10.deviceManagement.model.Device;
import psoftProjectG10.deviceManagement.repositories.DeviceRepository;
import psoftProjectG10.exceptions.NotFoundException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final EditDeviceMapper deviceEditMapper;

    @Override
    public Iterable<Device> findAll(int page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return deviceRepository.findAllDevices(pageable);
    }

    public Iterable<Device> listMyDevices(Long id,int page, Integer batchSize) {
        return deviceRepository.searchByIdSub(id);
    }

    @Override
    public Optional<Device> findOne(final String macAddress) {
        return deviceRepository.findByMacAddress(macAddress);
    }

    @Override
    public Device create(final CreateDeviceRequest resource) {
        final Device device = deviceEditMapper.create(resource);

        return deviceRepository.save(device);
    }

    @Override
    public Device update(final String macAddress, final EditDeviceRequest resource, final long desiredVersion) {
        final var device = deviceRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> new NotFoundException("Cannot update a Device that does not yet exist"));

        device.updateData(desiredVersion, resource.getName(), resource.getDescription());

        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public int deleteById(final String macAddress, final long desiredVersion) {
        return deviceRepository.deleteByIDIfMatch(macAddress, desiredVersion);
    }

    @Override
    public Device partialUpdate(final String macAddress, final EditDeviceRequest resource, final long desiredVersion) {
        final var device = deviceRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> new NotFoundException("Cannot update a Device that does not yet exist"));

        device.applyPatch(desiredVersion, resource.getName(), resource.getDescription());

        return deviceRepository.save(device);
    }
}