package psoftProjectG10.planManagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import psoftProjectG10.planManagement.model.Plan;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


public interface PlanRepository extends CrudRepository<Plan,Long> {

    Optional<Plan> findByPlanType(String planType);

    @Override
    Optional<Plan> findById(Long id);

    Plan getById(Long idPlan);

    @Modifying
    @Transactional
    @Query("DELETE FROM Plan p WHERE p.idPlan = ?1 AND p.version = ?2")
    int deleteByIdIfMatch(Long idPlan, long desiredVersion);


    @Modifying
    @Transactional
    @Query("UPDATE Plan p SET p.planPromote = :newPromote ")
    void unpromoteEveryPlan(@Param("newPromote") String newPromote);




    @Query("SELECT p FROM Plan p ORDER BY CASE WHEN p.planState = 'Ceased' THEN 1 ELSE 0 END, p.idPlan ASC")
    Page<Plan> findAllPlans(Pageable pageable);



    @Query("SELECT p FROM Plan p ORDER BY CASE WHEN p.planState = 'Ceased' THEN 1 ELSE 0 END, p.idPlan ASC")
    List<Plan> findSomePlans(Pageable pageable);


}