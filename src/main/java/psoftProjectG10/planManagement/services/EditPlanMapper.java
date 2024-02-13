package psoftProjectG10.planManagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import psoftProjectG10.planManagement.api.CreatePlanRequest;
import psoftProjectG10.planManagement.api.EditPlanRequest;
import psoftProjectG10.planManagement.api.EditPriceHistoryRequest;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.model.PriceHistory;

@Mapper(componentModel = "spring")
public abstract class EditPlanMapper {

    public abstract Plan create(CreatePlanRequest request);

    public abstract PriceHistory createPriceHistory(Long idPlan, EditPriceHistoryRequest request);

    public abstract void update(EditPlanRequest request, @MappingTarget Plan plan);
}