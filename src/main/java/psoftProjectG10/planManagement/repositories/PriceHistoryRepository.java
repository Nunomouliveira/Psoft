package psoftProjectG10.planManagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import psoftProjectG10.planManagement.model.PriceHistory;

public interface PriceHistoryRepository extends CrudRepository<PriceHistory,Long> {

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.plan.planType LIKE :planType ORDER BY ph.PH_id  DESC")
    Page<PriceHistory> searchByPlanType(@Param("planType") String planType, Pageable pageable);

}