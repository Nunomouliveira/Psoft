package psoftProjectG10.planManagement.model;

import lombok.Getter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


public class IDPlanCashFlow {


    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idPlan")
    private Plan plan;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank
    @NotNull
    private Long idPlan;

    @Getter
    private List<PlanType_CashFlow> cashflows = new ArrayList<>();



    protected IDPlanCashFlow() {
    }

    public IDPlanCashFlow(final Plan plan) {
        setIdPlan(plan);
    }

    public Long getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(Plan plan) {
        this.idPlan = plan.getId();
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public List<PlanType_CashFlow> getCashflows() {
        return cashflows;
    }

    public void setCashflows(List<PlanType_CashFlow> cashflows) {
        this.cashflows = cashflows;
    }

    public void addPlanTypeCashFlow(final PlanType_CashFlow r) {
        cashflows.add(r);
    }
    public void setPlanTypeCashFlows(List<PlanType_CashFlow>  cashflows) {
        this.cashflows = cashflows;
    }
}