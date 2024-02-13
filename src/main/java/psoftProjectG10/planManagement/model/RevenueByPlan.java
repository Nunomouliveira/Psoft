package psoftProjectG10.planManagement.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public class RevenueByPlan {


    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idPlan")
    private Plan plan;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank
    @NotNull
    private Long idPlan;

    @Column(nullable = false)
    @Min(0)
    private Double totalRevenue;


    protected RevenueByPlan() {
    }

    public RevenueByPlan(final Plan plan) {
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

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}