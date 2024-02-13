package psoftProjectG10.subscriptionManagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.repositories.PlanRepository;
import psoftProjectG10.subscriptionManagement.api.CreateSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.api.EditSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;

import javax.validation.ValidationException;

@Mapper(componentModel = "spring")
public abstract class EditSubscriptionMapper {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    public abstract Subscription create(CreateSubscriptionRequest request);

    public abstract void update(EditSubscriptionRequest request, @MappingTarget Subscription subscription);

    public Plan toPlan(final Long idPlan) {
        return planRepository.findById(idPlan).orElseThrow(() -> new ValidationException("Select an existing plan"));
    }

    public User toUser(final Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ValidationException("Select an existing user"));
    }

}
