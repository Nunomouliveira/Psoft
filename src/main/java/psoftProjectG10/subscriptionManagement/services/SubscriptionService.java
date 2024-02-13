package psoftProjectG10.subscriptionManagement.services;

import psoftProjectG10.subscriptionManagement.api.CreateSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.api.EditSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.model.Subscription;

import java.time.LocalDate;
import java.util.Optional;

public interface SubscriptionService {

    Iterable<Subscription> findAll();
    Optional<Subscription> findOne(Long id);
    Subscription create(CreateSubscriptionRequest resource);
    Subscription forDashBoard(LocalDate startDate, LocalDate endDate);
    Subscription update(Long id, EditSubscriptionRequest resource, long desiredVersion);
    Subscription updateState(Long id);
    Subscription partialUpdate(Long id, EditSubscriptionRequest resource, long parseLong);
    int deleteById(Long id, long desiredVersion);
    Subscription switchPlan(Long idPlan, Long id);
    Subscription RenewAnnualSubscription(long id);
    Iterable<Subscription> migrateAllSubscribers(Long currentPlanId, Long newPlanId);

}