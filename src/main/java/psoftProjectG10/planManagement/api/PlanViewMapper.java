package psoftProjectG10.planManagement.api;

import org.mapstruct.Mapper;
import psoftProjectG10.planManagement.model.IDPlanCashFlow;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.model.PriceHistory;
import psoftProjectG10.planManagement.model.RevenueByPlan;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class PlanViewMapper {

    public abstract PlanView toPlanView(Plan plan);

    public abstract Iterable<PlanViewFLOW> toPlanViewFlow(Iterable<IDPlanCashFlow> plan);

    public abstract List<RevenueByPlanView> toRevenueByPlanView(List<RevenueByPlan> revenueByPlan);

    public abstract Iterable<PriceHistoryView> toPriceHistoryView(Iterable<PriceHistory> priceHistory);

    public abstract Iterable<PlanView> toPlanView(Iterable<Plan> plans);

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
