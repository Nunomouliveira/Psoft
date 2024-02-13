package psoftProjectG10.bootstrapping;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.repositories.PlanRepository;

@Component
@RequiredArgsConstructor
@Profile("bootstrap")
public class PlanBootstrapper implements CommandLineRunner {

    private final PlanRepository planRepo;

    @Override
    public void run(final String... args) throws Exception {
        if (planRepo.findByPlanType("Free").isEmpty()) {
            final Plan p1 = new Plan("Free", 1000, 1, 0, "Automatic", 0.00, 0.00, "Free Plan");
            planRepo.save(p1);
        }

        if (planRepo.findByPlanType("Silver").isEmpty()) {
            final Plan p2 = new Plan("Silver", 5000, 3, 10, "Automatic", 4.99, 49.99, "Silver Plan");
            planRepo.save(p2);
        }

        if (planRepo.findByPlanType("Gold").isEmpty()) {
            final Plan p3 = new Plan("Gold", 1000000000, 6, 25, "Personalized", 5.99, 59.99, "Gold Plan");
            p3.setPlanPromote("Promoted");
            planRepo.save(p3);
        }
    }
}
