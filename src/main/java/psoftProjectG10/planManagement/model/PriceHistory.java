package psoftProjectG10.planManagement.model;

import psoftProjectG10.userManagement.model.User;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PH_id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idPlan")
    private Plan plan;

    @OneToOne
    @JoinColumn(name = "id", nullable = true, unique = false, updatable = true)
    private User user;

    private String planType;
    private double oldPrice_monthlyFee;

    private double newPrice_monthlyFee;

    private double oldPrice_annualFee;

    private double newPrice_annualFee;

    private LocalDate changeDate;


    public PriceHistory(final Plan plan, final double oldPrice_monthlyFee, final double newPrice_monthlyFee, final double oldPrice_annualFee, final double newPrice_annualFee, final LocalDate changeDate) {
        setPlan(plan);
        this.oldPrice_monthlyFee = oldPrice_monthlyFee;
        this.newPrice_monthlyFee = newPrice_monthlyFee;
        this.oldPrice_annualFee = oldPrice_annualFee;
        this.newPrice_annualFee = newPrice_annualFee;
        this.changeDate = changeDate;
    }

    public PriceHistory() {

    }

    public User getUser() {
        return user;
    }
    public Long getId() {
        return user.getId();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getPH_id() {
        return PH_id;
    }

    public void setPH_id(Long PH_id) {
        this.PH_id = PH_id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public double getOldPrice_monthlyFee() {
        return oldPrice_monthlyFee;
    }

    public void setOldPrice_monthlyFee(double oldPrice_monthlyFee) {
        this.oldPrice_monthlyFee = oldPrice_monthlyFee;
    }

    public double getNewPrice_monthlyFee() {
        return newPrice_monthlyFee;
    }

    public void setNewPrice_monthlyFee(double newPrice_monthlyFee) {
        this.newPrice_monthlyFee = newPrice_monthlyFee;
    }

    public double getOldPrice_annualFee() {
        return oldPrice_annualFee;
    }

    public void setOldPrice_annualFee(double oldPrice_annualFee) {
        this.oldPrice_annualFee = oldPrice_annualFee;
    }

    public double getNewPrice_annualFee() {
        return newPrice_annualFee;
    }

    public void setNewPrice_annualFee(double newPrice_annualFee) {
        this.newPrice_annualFee = newPrice_annualFee;
    }

    public LocalDate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDate changeDate) {
        this.changeDate = changeDate;
    }
}
