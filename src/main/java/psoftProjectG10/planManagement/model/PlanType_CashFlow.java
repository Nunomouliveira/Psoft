package psoftProjectG10.planManagement.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
@Access(AccessType.FIELD)
public class PlanType_CashFlow {

    private Integer month;

    private Integer year = LocalDate.now().getYear();
    private Double cashflow;


    public PlanType_CashFlow() {
    }

    public PlanType_CashFlow(final Integer month, final Double cashflow) {
        setMonth(month);
        setCashflow(cashflow);
    }


    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        if(month >= 13)
        {
            int extraYears = (month - 1) / 12;
            this.year += extraYears;
            this.month = (month - 1) % 12 + 1;
        }
        else
        {
            this.month = month;
        }
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getCashflow() {
        return cashflow;
    }

    public void setCashflow(Double cashflow) {
        this.cashflow = cashflow;
    }


}
