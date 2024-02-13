package psoftProjectG10.planManagement.services;

import psoftProjectG10.planManagement.api.CreatePlanRequest;
import psoftProjectG10.planManagement.api.EditPlanRequest;
import psoftProjectG10.planManagement.model.IDPlanCashFlow;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.model.PriceHistory;
import psoftProjectG10.planManagement.model.RevenueByPlan;
import psoftProjectG10.userManagement.model.User;

import java.util.List;
import java.util.Optional;

public interface PlanService {

    Iterable<Plan> findAll(int page, Integer size);
    Iterable<PriceHistory> findAllPriceHistory(Long id,Integer page, Integer batchSize);
    List<RevenueByPlan>findRevenueByPlan(Integer page, Integer batchSize);
    Optional<Plan> findOne(String id);
    Iterable<Plan> findAll_User(int page, Integer size);
    Optional<Plan> findByPlanType(String planType);
    Plan create(CreatePlanRequest resource);
    Plan partialUpdate(Long idPlan, EditPlanRequest resource, long parseLong, User user);
    List<IDPlanCashFlow> forDashBoardFutureCashflows(Integer numberOfMonths, Integer page, Integer batchSize);
    int deleteById(Long idPlan, long desiredVersion);
    int ceasePlanById(Long idPlan, long desiredVersion);
}
