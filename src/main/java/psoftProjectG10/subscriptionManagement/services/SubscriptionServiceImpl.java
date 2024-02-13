package psoftProjectG10.subscriptionManagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psoftProjectG10.deviceManagement.model.Device;
import psoftProjectG10.deviceManagement.repositories.DeviceRepository;
import psoftProjectG10.exceptions.NotFoundException;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.repositories.PlanRepository;
import psoftProjectG10.subscriptionManagement.api.CreateSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.api.EditSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.model.SubscriptionStatus;
import psoftProjectG10.subscriptionManagement.repositories.SubscriptionRepository;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository repository;
    private final EditSubscriptionMapper subscriptionEditMapper;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final DeviceRepository deviceRepository;
    @Override
    public Iterable<Subscription> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Subscription> findOne(final Long id) {
        User user = userRepository.getById(id);

        Subscription subscription = repository.getByUser(user);

        if(subscription == null)
        {
            throw new NotFoundException("You are not subscribed to any plan");
        }

        if(subscription.getSubState() != SubscriptionStatus.Cancelled)
        {
            String[] weather = subscription.getWeatherInfo();
            subscription.setWeather(weather);
        }

        final Optional<Subscription> subscriptionOptional = repository.findByUser(user);
        subscriptionOptional.ifPresent(Subscription::updateFunnyQuote);
        repository.save(subscriptionOptional.get());

        return repository.findByUser(user);
    }

    @Override
    public Subscription create(final CreateSubscriptionRequest resource) {
        final Subscription subscription = subscriptionEditMapper.create(resource);

        if(subscription.getFeeType().equals("Annual"))
        {
            subscription.setRenewDate(subscription.getStartDate().plusYears(1));
        }
        else if(subscription.getFeeType().equals("Monthly"))
        {
            subscription.setRenewDate(subscription.getStartDate().plusMonths(1));
        }

        subscription.setWeather(subscription.getWeatherInfo());

        return repository.save(subscription);
    }

    @Override
    public Subscription forDashBoard(LocalDate startDate, LocalDate endDate) {
        Plan plan = new Plan("Test" , 1000, 1, 0, "Automatic", 0.00, 0.00, "Free Plan");
        User user = new User("test@mail.com", "Test1234", "Test");
        Subscription found = new Subscription(plan, user, "Annual");
        int X;
        int Y;

        LocalDate minDate = LocalDate.of(1968, 8, 28);

        X = repository.countSubscriptionsByDatesAndStatus(startDate, endDate, SubscriptionStatus.Active);
        Y = repository.countSubscriptionsByDatesAndStatus1(startDate, endDate, SubscriptionStatus.Cancelled);

        found.setPlan(plan);
        found.setStartDate(startDate);
        found.setSubscriptions(X);
        found.setCancellations(Y);

        return found;
    }



    @Override
    public Subscription update(final Long id, final EditSubscriptionRequest resource, final long desiredVersion) {
        User user = userRepository.getById(id);
        final var subscription = repository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Cannot update a subscription that does not yet exist"));

        subscription.updateData(desiredVersion, resource.getFeeType());

        return repository.save(subscription);
    }
    @Override
    public Subscription updateState(final Long id) {
        User user = userRepository.getById(id);

        Subscription sub = repository.getByUser(user);

        List<Device> devices = deviceRepository.searchByIdSub(sub.getId());

        if(devices.size() > 0)
        {
            throw new IllegalArgumentException("You can't cancel a subscription with associated devices!");
        }

        final var subscription = repository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Cannot cancel a subscription that does not yet exist"));

        if (subscription.getSubState() == SubscriptionStatus.Cancelled) {
            throw new IllegalArgumentException("You cannot cancel a subscription that is already cancelled");
        }

        String[] nullString = new String[0];

        subscription.setSubState(SubscriptionStatus.Cancelled);
        subscription.setCancelDate(LocalDate.now());
        subscription.setUser(null);
        subscription.setFunnyQuote("null");
        subscription.setWeather(nullString);

        long id_sub=deviceRepository.findSubscriptionIdByUserId(id);
        List<Device> deviceList = deviceRepository.searchByIdSub(id_sub);
        deviceRepository.deleteAll(deviceList);

        return repository.save(subscription);
    }


    @Override
    @Transactional
    public int deleteById(final Long id, final long desiredVersion) {
        return repository.deleteByIdIfMatch(id, desiredVersion);
    }


    @Override
    public Subscription switchPlan(Long idPlan, Long id) {
        Long idSub = repository.findSubscriptionIdByUserId(id);

        if(idSub == null)
        {
            throw new NotFoundException("You are not subscribed to any plan");
        }

        Plan plan = planRepository.findById(idPlan)
                .orElseThrow(() -> new NotFoundException("Cannot switch to a plan that does not yet exist"));


        Integer numDevices = deviceRepository.countDevicesBySubscriptionId(idSub);

        if(plan.getMaxDevices() < numDevices)
        {
            throw new IllegalArgumentException("You have too many associated devices to change to this plan. Please, remove at least " + (numDevices - plan.getMaxDevices()) + " device(s)");
        }

        if (plan.getPlanState().equals("Deactivated")) {
            throw new IllegalArgumentException("You cannot switch to a plan that is deactivated");
        }

        if(repository.findPlanIdBySubscriptionId(idSub).equals(idPlan)){
            throw new IllegalArgumentException("You cannot switch to the same plan");
        }
        final Optional<Subscription> subscriptionOptional = repository.findById(idSub);

        Subscription subscription1 = repository.getById(idSub);

        if(subscription1.getFeeType().equals("Monthly"))
        {
            subscriptionOptional.ifPresent(subscription -> subscription.setRenewDate(LocalDate.now().plusMonths(1)));
        }

        if(subscription1.getFeeType().equals("Annual"))
        {
            subscriptionOptional.ifPresent(subscription -> subscription.setRenewDate(LocalDate.now().plusYears(1)));
        }

        subscriptionOptional.ifPresent(subscription -> subscription.setPlan(planRepository.getById(idPlan)));
        return repository.save(subscriptionOptional.get());
    }

    public Subscription RenewAnnualSubscription(long id) {

        Long idSub = repository.findSubscriptionIdByUserId(id);

        if(idSub == null)
        {
            throw new NotFoundException("You are not subscribed to any plan");
        }

        Subscription subscription = repository.getById(idSub);

        if (LocalDate.now().isBefore(subscription.getRenewDate())) {

            throw new IllegalArgumentException("You cannot renew your subscription until the subscription expires");

        } else {
            if (subscription.getFeeType().equals("Annual")) {
                subscription.setRenewDate(LocalDate.now().plusYears(1));
                subscription.setSubState(SubscriptionStatus.Active);

            } else if (subscription.getFeeType().equals("Monthly")) {
                subscription.setRenewDate(LocalDate.now().plusMonths(1));
                subscription.setSubState(SubscriptionStatus.Active);
            }
        }
        return repository.save(subscription);
    }

    @Override
    public Iterable<Subscription> migrateAllSubscribers(Long currentPlanId, Long newPlanId) {
        if(currentPlanId.equals(newPlanId)) {
            throw new IllegalArgumentException("It looks like you are trying to change subscribers from one plan to the same plan.");
        }
        Plan plan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new NotFoundException("Cannot switch subscribers to a plan that does not yet exist"));

        Plan plan1 = planRepository.findById(currentPlanId)
                .orElseThrow(() -> new NotFoundException("Cannot switch from a plan that does not yet exist"));

        if(plan.getPlanState().equals("Deactivated"))
        {
            throw new IllegalArgumentException("You can't migrate users to a deactivated plan!");
        }

        if (repository.countSubscriptionsByPlan(currentPlanId, SubscriptionStatus.Active) == 0) {
            throw new IllegalArgumentException("It looks like there are no active subscriptions for this current plan");
        }
        int batchSize = 100;
        int pageNumber = 0;
        boolean hasMoreSubscriptions = true;

        do {
            Pageable pageable = PageRequest.of(pageNumber, batchSize);
            Page<Subscription> subscriptionPage = repository.findSubscriptionsByPlanType(plan1.getPlanType(), pageable);

            int invalidSubscriptions = 0;

            for (Subscription subscription : subscriptionPage.getContent()) {
                if (deviceRepository.countDevicesBySubscriptionId(subscription.getId()) > plan.getMaxDevices()) {
                    invalidSubscriptions++;
                }
            }

            if(invalidSubscriptions > 0)
            {
                throw new IllegalArgumentException("You can't migrate all the users of this plan because there are some users that have more devices than allowed for this plan");
            }

            List<Subscription> subscriptions = subscriptionPage.getContent();
            hasMoreSubscriptions = subscriptionPage.hasNext();

            for (Subscription subscription : subscriptions) {
                subscription.setPlan(plan);
            }

            repository.saveAll(subscriptions);

            pageNumber++;
        } while (hasMoreSubscriptions);


        return repository.findAll();
    }


    @Override
    public Subscription partialUpdate(final Long id, final EditSubscriptionRequest resource, final long desiredVersion) {
        User user = userRepository.getById(id);
        final var subscription = repository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Cannot update a subscription that does not yet exist"));

        subscription.applyPatch(desiredVersion, resource.getFeeType());

        return repository.save(subscription);
    }


}
