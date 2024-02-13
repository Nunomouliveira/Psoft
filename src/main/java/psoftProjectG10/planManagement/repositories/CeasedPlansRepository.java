package psoftProjectG10.planManagement.repositories;

import org.springframework.data.repository.CrudRepository;
import psoftProjectG10.planManagement.model.Plan;


public interface CeasedPlansRepository extends CrudRepository<Plan,Long> {
    Plan getByPlanType(String planType);

}