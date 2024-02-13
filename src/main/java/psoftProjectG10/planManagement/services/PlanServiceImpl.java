package psoftProjectG10.planManagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psoftProjectG10.deviceManagement.model.Device;
import psoftProjectG10.exceptions.NotFoundException;
import psoftProjectG10.planManagement.api.CreatePlanRequest;
import psoftProjectG10.planManagement.api.EditPlanRequest;
import psoftProjectG10.planManagement.api.EditPriceHistoryRequest;
import psoftProjectG10.planManagement.model.*;
import psoftProjectG10.planManagement.repositories.CeasedPlansRepository;
import psoftProjectG10.planManagement.repositories.PlanRepository;
import psoftProjectG10.planManagement.repositories.PriceHistoryRepository;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.model.SubscriptionStatus;
import psoftProjectG10.subscriptionManagement.repositories.SubscriptionRepository;
import psoftProjectG10.userManagement.model.User;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository repository;

    private final CeasedPlansRepository ceasingRepository;

    private final EditPlanMapper planEditMapper;

    private final SubscriptionRepository subscriptionRepository;

    private final PriceHistoryRepository priceHistoryRepository;


    @Override
    public Iterable<Plan> findAll(int page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllPlans(pageable);
    }


    @Override
    public Iterable<PriceHistory> findAllPriceHistory(Long idPlan,Integer page, Integer batchSize) {
        Pageable pageable = PageRequest.of(page, batchSize);
        final var plan = repository.findById(idPlan)
                .orElseThrow(() -> new NotFoundException("Cannot update a Plan that does not exist"));

        return priceHistoryRepository.searchByPlanType(plan.getPlanType(), pageable);
    }

    @Override
    public Iterable<Plan> findAll_User(int page, Integer size) {
        Iterable<Plan> allPlans = repository.findAll();
        List<Plan> activePlans = new ArrayList<>();

        for (Plan plan : allPlans) {
            if (plan.getPlanState().equals("Active")) {
                activePlans.add(plan);
            }
        }

        return activePlans;
    }

    @Override
    public Optional<Plan> findOne(final String id) {
        return repository.findByPlanType(id);
    }

    @Override
    public Optional<Plan> findByPlanType(String planType){
        return repository.findByPlanType(planType);
    }

    @Override
    public Plan create(final CreatePlanRequest resource) {
        final Plan obj = planEditMapper.create(resource);
        return repository.save(obj);
    }

    @Override
    @Transactional
    public int deleteById(final Long idPlan, final long desiredVersion) {
        return repository.deleteByIdIfMatch(idPlan, desiredVersion);
    }

    @Override
    public int ceasePlanById(Long idPlan, long desiredVersion) {
        final var plan = repository.findById(idPlan)
                .orElseThrow(() -> new NotFoundException("Cannot update a Plan that does not exist"));

        if(plan.getPlanState().equals("Active"))
        {
            throw new IllegalArgumentException("You cannot cease an active plan");
        }

        if(plan.getPlanState().equals("Ceased"))
        {
            throw new IllegalArgumentException("You cannot cease an already ceased plan");
        }

        int count = subscriptionRepository.countSubscriptionsByPlan(idPlan, SubscriptionStatus.Active);
        if (count != 0) {
            throw new IllegalArgumentException("You cannot cease a plan that has active subscriptions");
        } else {
            List<Long> subscriptionIds = subscriptionRepository.findSubscriptionIdsByIdPlan(idPlan);
            if (subscriptionIds.size() == 0) {
                ceasingRepository.save(plan);
                return repository.deleteByIdIfMatch(idPlan, desiredVersion);
            }

            Random random = new Random();
            int randomNumber = random.nextInt(1000);

            Plan ceasedPlan = new Plan(plan.getPlanType()+" - Ceased " + randomNumber, plan.getNumMinutes(), plan.getMaxDevices(), plan.getMusicCollections(), plan.getMusicSuggestions(), plan.getMonthlyFee(), plan.getAnnualFee(), plan.getHtmlDescription());
            ceasedPlan.setPlanState("Ceased");

            ceasingRepository.save(ceasedPlan);

            for (Long subscriptionId : subscriptionIds) {
                Optional<Subscription> sub = subscriptionRepository.findById(subscriptionId);
                Subscription subscription = sub.get();


            subscription.setPlan(ceasingRepository.getByPlanType(ceasedPlan.getPlanType()));
            subscriptionRepository.save(subscription);
            }
            return repository.deleteByIdIfMatch(idPlan, desiredVersion);
        }
    }



    @Override
    public Plan partialUpdate(final Long idPlan, final EditPlanRequest resource, final long desiredVersion, User user) {
        PriceHistory priceHistory = null;

        Double valor1;
        Double valor2;

        final var plan = repository.findById(idPlan)
                .orElseThrow(() -> new NotFoundException("Cannot update a Plan that does not yet exist"));

        if(resource.getPlanState() != null)
        {
            if(resource.getPlanState().equals("Deactivated") && resource.getPlanPromote() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getMonthlyFee() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getAnnualFee() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getMaxDevices() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getNumMinutes() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getMusicCollections() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getMusicSuggestions() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
            if(resource.getPlanState().equals("Deactivated") && resource.getHtmlDescription() != null)
            {
                throw new IllegalArgumentException("You cannot update a plan that you will deactivate");
            }
        }


        if(plan.getPlanState().equals("Deactivated"))
        {
            throw new IllegalArgumentException("You cannot update a plan that is deactivated");
        }

        if(resource.getPlanState() != null)
        {
            if (plan.getPlanState().equals("Deactivated")) {
                throw new IllegalArgumentException("You cannot deactivate a plan that is already deactivated");
            }
            resource.setPlanState("Deactivated");

            if(plan.getPlanPromote().equals("Promoted"))
            {
                plan.setPlanPromote("Not Promoted");
            }

        }

        if(resource.getPlanPromote() != null)
        {
            if(!resource.getPlanPromote().equals("Promoted") && !resource.getPlanPromote().equals("Not Promoted"))
            {
                throw new IllegalArgumentException("You must type 'Promoted', just like that");
            }
            if(resource.getPlanPromote().equals("Not Promoted"))
            {
                throw new IllegalArgumentException("To disable the promotion of a plan, you must promote another plan!");
            }
            if (plan.getPlanPromote().equals("Promoted")) {
                throw new IllegalArgumentException("You cannot promote a plan that is already promoted");
            }
            else if(plan.getPlanState().equals("Deactivated")){
                throw new IllegalArgumentException("You cannot promote a plan that is deactivated");
        }
            repository.unpromoteEveryPlan("Not promoted");
            resource.setPlanPromote("Promoted");

        }

        valor1 = plan.getMonthlyFee();
        valor2 = plan.getAnnualFee();

        plan.applyPatch(desiredVersion, resource.getNumMinutes(), resource.getMaxDevices(), resource.getMusicCollections(), resource.getMusicSuggestions(),
                resource.getPlanState(), resource.getPlanPromote(), resource.getMonthlyFee(), resource.getAnnualFee(), resource.getHtmlDescription());


        if(resource.getMonthlyFee() != null || resource.getAnnualFee() != null)
        {
            final EditPriceHistoryRequest resource1 = new EditPriceHistoryRequest();
            resource1.setOldPrice_monthlyFee(valor1);
            resource1.setOldPrice_annualFee(valor2);
            resource1.setNewPrice_monthlyFee(resource.getMonthlyFee() != null ? resource.getMonthlyFee() : plan.getMonthlyFee());
            resource1.setNewPrice_annualFee(resource.getAnnualFee() != null ? resource.getAnnualFee() : plan.getAnnualFee());
            resource1.setPlan(plan);
            resource1.setUser(user);

            if (resource.getAnnualFee() != null && resource.getAnnualFee() < 0.0) {
                throw new IllegalArgumentException("You cannot assign a negative value to the annual fee");
            }

            if (resource.getMonthlyFee() != null && resource.getMonthlyFee() < 0.0) {
                throw new IllegalArgumentException("You cannot assign a negative value to the monthly fee");
            }

            resource.setMonthlyFee(resource.getMonthlyFee());
            resource.setAnnualFee(resource.getAnnualFee());

            resource1.setChangeDate(LocalDate.now());

            priceHistory = planEditMapper.createPriceHistory(idPlan, resource1);

            priceHistoryRepository.save(priceHistory);

        }

        return repository.save(plan);
    }

    public List<IDPlanCashFlow> forDashBoardFutureCashflows(Integer numberOfMonths, Integer page, Integer batchSize) {
        Pageable pageable = PageRequest.of(page, batchSize);
        List<Plan> plans = repository.findSomePlans(pageable);
        List<IDPlanCashFlow> idPlanCashFlows = new ArrayList<>();

        for(int ceased = 0; ceased < plans.size(); ceased++)
        {
            if(plans.get(ceased).getPlanState().equals("Ceased"))
            {
                plans.remove(plans.get(ceased));
            }
        }

        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();

        for (Plan plan : plans) {
            double totalRevenueMonths = 0;
            double totalRevenueAnnual = 0;

            ArrayList<Integer> thisYearMonths = new ArrayList<>();
            ArrayList<Integer> otherYearsMonths = new ArrayList<>();


            for (int previousYearsMonth = 2000; previousYearsMonth < currentYear; previousYearsMonth++)
            {
                for(int previousYearsMonths = 1; previousYearsMonths <= 12; previousYearsMonths++)
                {
                    double revenue = subscriptionRepository.calculateRevenueByMonth(previousYearsMonths, previousYearsMonth, SubscriptionStatus.Active, plan.getPlanType());
                    double revenue1 = subscriptionRepository.calculateRevenueByMonth(previousYearsMonths, previousYearsMonth, SubscriptionStatus.Expired, plan.getPlanType());

                    double totalRevenue = revenue + revenue1;

                    totalRevenueMonths += totalRevenue;
                }
            }

            for (int month = 1; month <= currentMonth + 1; month++) {
                double revenue = subscriptionRepository.calculateRevenueByMonth(month, currentYear, SubscriptionStatus.Active, plan.getPlanType());
                double revenue1 = subscriptionRepository.calculateRevenueByMonth(month, currentYear, SubscriptionStatus.Expired, plan.getPlanType());

                double totalRevenue = revenue + revenue1;

                totalRevenueMonths += totalRevenue;
            }
            double monthlyCashflowAmount = totalRevenueMonths;



            for (int previousYears = 2000; previousYears < currentYear; previousYears++)
            {
                for(int previousYearsMonths = 1; previousYearsMonths <= 12; previousYearsMonths++)
                {
                    double annualRevenue_LastYear = subscriptionRepository.calculateAnnualRevenue(previousYearsMonths, previousYears, SubscriptionStatus.Active, plan.getPlanType());
                    double annualRevenue_LastYear1 = subscriptionRepository.calculateAnnualRevenue(previousYearsMonths, previousYears, SubscriptionStatus.Expired, plan.getPlanType());

                    if(annualRevenue_LastYear + annualRevenue_LastYear1 != 0)
                    {
                        totalRevenueAnnual = totalRevenueAnnual + annualRevenue_LastYear + annualRevenue_LastYear1;
                        otherYearsMonths.add(previousYearsMonths);
                    }
                }
            }


            for (int month = 1; month <= currentMonth; month++) {

                double annualRevenue_ThisYear = subscriptionRepository.calculateAnnualRevenue(month, currentYear, SubscriptionStatus.Active, plan.getPlanType());
                double annualRevenue_ThisYear1 = subscriptionRepository.calculateAnnualRevenue(month, currentYear, SubscriptionStatus.Expired, plan.getPlanType());

                if(annualRevenue_ThisYear + annualRevenue_ThisYear1 != 0)
                {
                    totalRevenueAnnual = totalRevenueAnnual + annualRevenue_ThisYear + annualRevenue_ThisYear1;
                    thisYearMonths.add(month);
                }
            }

            double annualCashflowAmount = totalRevenueAnnual;


            IDPlanCashFlow idPlanCashFlow = new IDPlanCashFlow(plan);


            for (int month = currentMonth + 1; month <= currentMonth + numberOfMonths; month++) {

                int months = (month - 1) % 12 + 1;
                double cashflowAmount = monthlyCashflowAmount;

                for (Integer i : thisYearMonths) {
                    if (months == i) {
                        cashflowAmount = monthlyCashflowAmount + annualCashflowAmount;
                    }
                }

                for (Integer i : otherYearsMonths) {
                    if (months == i) {
                        cashflowAmount = monthlyCashflowAmount + annualCashflowAmount;
                    }
                }

                PlanType_CashFlow planTypeCashFlow = new PlanType_CashFlow(month, cashflowAmount);
                idPlanCashFlow.addPlanTypeCashFlow(planTypeCashFlow);
            }

            idPlanCashFlows.add(idPlanCashFlow);

        }

        return idPlanCashFlows;
    }

    public List<RevenueByPlan> findRevenueByPlan(Integer page, Integer batchSize)
    {

        Pageable pageable = PageRequest.of(page, batchSize);
        List<Plan> plans = repository.findSomePlans(pageable);
        List<RevenueByPlan> revenueByPlans = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        int currentDay = currentDate.getDayOfMonth();

        for (Plan plan : plans)
        {
            RevenueByPlan revenueByPlan = new RevenueByPlan(plan);

            double totalRevenue = 0;

            List<Subscription> activeSubs = subscriptionRepository.findByPlanAndYear(plan, currentYear, SubscriptionStatus.Active);
            List<Subscription> expiredSubs = subscriptionRepository.findByPlanAndYear(plan, currentYear, SubscriptionStatus.Expired);
            List<Subscription> cancelledSubs = subscriptionRepository.findByPlanAndYear(plan, currentYear, SubscriptionStatus.Cancelled);

            List<Subscription> subscriptions = new ArrayList<>();
            subscriptions.addAll(activeSubs);
            subscriptions.addAll(expiredSubs);
            subscriptions.addAll(cancelledSubs);

            for (Subscription subscription : subscriptions)
            {

                LocalDate startDate = subscription.getStartDate();
                LocalDate renewDate = subscription.getRenewDate();
                LocalDate cancelDate = subscription.getCancelDate();

                int startDateDay = startDate.getDayOfMonth();
                int startDateMonth = startDate.getMonthValue();
                int startDateYear = startDate.getYear();

                int renewDateDay = renewDate.getDayOfMonth();
                int renewDateMonth = renewDate.getMonthValue();
                int renewDateYear = renewDate.getYear();

                int cancelDateDay = cancelDate.getDayOfMonth();
                int cancelDateMonth = cancelDate.getMonthValue();
                int cancelDateYear = cancelDate.getYear();

                LocalDate firstJanuary = LocalDate.now().withMonth(1).withDayOfMonth(1);

                if (subscription.getFeeType().equals("Monthly"))
                {
                    if (subscription.getSubState() == SubscriptionStatus.Active || subscription.getSubState() == SubscriptionStatus.Expired)
                    {
                        if ((startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) && startDate.isAfter(firstJanuary))
                        {
                            int monthsElapsed = (currentMonth - startDateMonth) + 1;
                            double subscriptionRevenue = (monthsElapsed) * subscription.getPlan().getMonthlyFee();

                            totalRevenue += subscriptionRevenue;
                        }
                    }
                    else if (subscription.getSubState() == SubscriptionStatus.Cancelled)
                    {
                        if ((startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) && startDate.isAfter(firstJanuary))
                        {
                            int monthsElapsed = (cancelDateMonth - startDateMonth) + 1;
                            double subscriptionRevenue = (monthsElapsed) * subscription.getPlan().getMonthlyFee();

                            totalRevenue += subscriptionRevenue;
                        }
                    }
                }
                else if (subscription.getFeeType().equals("Annual"))
                {
                    if (subscription.getSubState() == SubscriptionStatus.Active || subscription.getSubState() == SubscriptionStatus.Expired)
                    {
                        if (((startDate.isBefore(LocalDate.now().minusYears(1)) || startDate.isEqual(LocalDate.now().minusYears(1))) && renewDate.isAfter(firstJanuary))
                                || (((startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) && startDate.isAfter(firstJanuary))))
                        {
                            double subscriptionRevenue = subscription.getPlan().getAnnualFee();

                            totalRevenue += subscriptionRevenue;
                        }
                    }
                    else if (subscription.getSubState() == SubscriptionStatus.Cancelled)
                    {
                        if (((startDate.isBefore(LocalDate.now().minusYears(1)) || startDate.isEqual(LocalDate.now().minusYears(1))) && renewDate.isAfter(firstJanuary))
                                || (((startDate.isBefore(LocalDate.now()) || startDate.isEqual(LocalDate.now())) && startDate.isAfter(firstJanuary))))
                        {
                            double subscriptionRevenue = subscription.getPlan().getAnnualFee();

                            totalRevenue += subscriptionRevenue;
                        }
                    }
                }
            }

            revenueByPlan.setTotalRevenue(totalRevenue);
            revenueByPlans.add(revenueByPlan);
        }

        return revenueByPlans;
    }


}
