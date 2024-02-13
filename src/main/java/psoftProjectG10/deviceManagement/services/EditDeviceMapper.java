package psoftProjectG10.deviceManagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import psoftProjectG10.deviceManagement.api.CreateDeviceRequest;
import psoftProjectG10.deviceManagement.api.EditDeviceRequest;
import psoftProjectG10.deviceManagement.model.Device;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.repositories.SubscriptionRepository;

import javax.validation.ValidationException;

@Mapper(componentModel = "spring")
public abstract class EditDeviceMapper {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public abstract Device create(CreateDeviceRequest request);

    public abstract void update(EditDeviceRequest request, @MappingTarget Device device);

    public Subscription toSubscription(final Long idSub) {
        return subscriptionRepository.findById(idSub).orElseThrow(() -> new ValidationException("Select an existing subscription"));
    }
}
