package psoftProjectG10.subscriptionManagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.model.SubscriptionStatus;
import psoftProjectG10.userManagement.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface SubscriptionRepository extends CrudRepository<Subscription,Long> {
    Optional<Subscription> findByUser(User user);
    Subscription getById(Long id);

    Subscription getByUser(User user);
    @Override
    Optional<Subscription> findById(Long id);
    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.idSub = ?1 AND s.version = ?2")
    int deleteByIdIfMatch(Long id, long desiredVersion);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.startDate >= :startDate AND s.startDate <= :endDate AND s.subState = :status")
    int countSubscriptionsByDatesAndStatus(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") SubscriptionStatus status);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.cancelDate >= :startDate AND s.cancelDate <= :endDate AND s.subState = :status")
    int countSubscriptionsByDatesAndStatus1(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") SubscriptionStatus status);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan.idPlan = ?1 AND s.subState = ?2")
    int countSubscriptionsByPlan(@Param("idPlan") Long idPlan, @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.plan.planType = :planType")
    Page<Subscription> findSubscriptionsByPlanType(@Param("planType") String planType, Pageable pageable);


    @Query("SELECT s.idSub FROM Subscription s WHERE s.plan.idPlan = :idPlan")
    List<Long> findSubscriptionIdsByIdPlan(@Param("idPlan") Long idPlan);

    @Query("SELECT s.plan.idPlan FROM Subscription s WHERE s.idSub = :subscriptionId")
    Long findPlanIdBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    @Query("SELECT s.idSub FROM Subscription s WHERE s.user.id = :userId")
    Long findSubscriptionIdByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.plan = :plan AND YEAR(s.startDate) = :year AND s.subState = :status")
    List<Subscription> findByPlanAndYear(@Param("plan") Plan plan, @Param("year") int year, @Param("status") SubscriptionStatus status);


    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN s.feeType = 'Monthly' THEN s.plan.monthlyFee ELSE 0 END), 0) AS totalRevenue " +
            "FROM Subscription s " +
            "WHERE MONTH(s.startDate) = :month AND YEAR(s.startDate) = :year AND s.subState = :status AND s.plan.planType = :planType")
    Double calculateRevenueByMonth(@Param("month") int month, @Param("year") int year, @Param("status") SubscriptionStatus status,@Param("planType") String planType);

    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN s.feeType = 'Annual' THEN s.plan.annualFee ELSE 0 END), 0) AS totalRevenue " +
            "FROM Subscription s " +
            "WHERE MONTH(s.startDate) = :month AND YEAR(s.startDate) = :year AND s.subState = :status AND s.plan.planType = :planType")
    Double calculateAnnualRevenue(@Param("month") int month, @Param("year") int year, @Param("status") SubscriptionStatus status,@Param("planType") String planType);




}

