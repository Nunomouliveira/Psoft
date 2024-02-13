package psoftProjectG10.subscriptionManagement.api;

import org.mapstruct.Mapper;
import psoftProjectG10.subscriptionManagement.model.Subscription;

import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class SubscriptionViewMapper {

    public abstract SubscriptionView toSubscriptionView(Subscription subscription);

    public abstract DashBoardView toDashBoardView(Subscription subscription);


    public abstract Iterable<SubscriptionView> toSubscriptionView(Iterable<Subscription> subscriptions);

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
