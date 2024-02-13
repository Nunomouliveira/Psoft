package psoftProjectG10.planManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Plan")
public class PriceHistoryView {

    private Long id;
    private LocalDate changeDate;
    private double oldPrice_monthlyFee;

    private double oldPrice_annualFee;


    private double newPrice_monthlyFee;

    private double newPrice_annualFee;

}