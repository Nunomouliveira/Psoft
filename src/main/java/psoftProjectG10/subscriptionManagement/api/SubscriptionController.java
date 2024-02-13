package psoftProjectG10.subscriptionManagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import psoftProjectG10.exceptions.NotFoundException;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.repositories.PlanRepository;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.repositories.SubscriptionRepository;
import psoftProjectG10.subscriptionManagement.services.SubscriptionService;
import psoftProjectG10.userManagement.model.Role;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;
import psoftProjectG10.userManagement.services.UserService;
import psoftProjectG10.utils.Utils;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.Year;
import java.util.Iterator;
import java.util.Set;

@Tag(name = "Subscription", description = "Endpoints of Subscriptions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sub")
class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    private final SubscriptionService service;

    private final UserService userService;

    private final SubscriptionViewMapper subscriptionViewMapper;

    private final PlanRepository planRepository;

    private final UserRepository userRepository;

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    private Utils utils;

    @Operation(summary = "US07 - Creates a new Subscription")
    @RolesAllowed({Role.CUSTOMER, Role.ADMIN})
    @PostMapping(value = "/{idPlan}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SubscriptionView> create(HttpServletRequest request, @PathVariable Long idPlan,
                                                   @Valid @RequestBody final CreateSubscriptionRequest resource) {


        long id = utils.getUserByToken(request);

        resource.setUser(id);
        User user = userRepository.getById(id);

        Set<Role> userRoles = user.getAuthorities();

        Iterator<Role> iterator = userRoles.iterator();

        Role firstRole = iterator.next();

        String roleString = firstRole.toString();

        String roleIf = "Role(authority=CUSTOMER)";

        String roleIfAdmin = "Role(authority=ADMIN)";

        if (roleString.equals(roleIf) || roleString.equals(roleIfAdmin)) {
            if (planRepository.existsById(idPlan)) {

                Plan plan1 = planRepository.getById(idPlan);

                if (plan1.getPlanState().equals("Deactivated")) {
                    throw new IllegalArgumentException("The chosen plan is deactivated. Choose another one");
                }
                resource.setPlan(plan1.getId());
            } else {
                throw new IllegalArgumentException("Select an available Plan");
            }

            if(roleString.equals(roleIf))
            {
                Role role = new Role("SUBSCRIBER");
                iterator.remove();
                user.addAuthority(role);
            }


            final var subscription = service.create(resource);

            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(subscription.getId()).toUri()).body(subscriptionViewMapper.toSubscriptionView(subscription));


        } else {
            throw new IllegalArgumentException("Only customers can subscribe to a plan, and you can't subscribe to 2 plans");
        }
    }

    @Operation(summary = "US08 - Cancel a Subscription")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @PatchMapping
    public ResponseEntity<SubscriptionView> Cancelled(HttpServletRequest request){

        long id = utils.getUserByToken(request);

        User user = userRepository.getById(id);

        Set<Role> userRoles = user.getAuthorities();

        Iterator<Role> iterator = userRoles.iterator();

        Role firstRole = iterator.next();

        String roleString = firstRole.toString();

        String roleIf = "Role(authority=SUBSCRIBER)";

        String roleIfAdmin = "Role(authority=ADMIN)";

        if(roleString.equals(roleIf) || roleString.equals(roleIfAdmin))
        {
            if(roleString.equals(roleIf))
            {
                Role role = new Role("CUSTOMER");
                iterator.remove();
                user.addAuthority(role);
            }

            final var sub = service.updateState(utils.getUserByToken(request));
            return ResponseEntity.ok().eTag(Long.toString(sub.getVersion())).body(subscriptionViewMapper.toSubscriptionView(sub));

        }
        else {
            throw new IllegalArgumentException("You are not subscribed to any plan yet!");
        }
    }

    @Operation(summary = "US09 - Get the details of my subscription")
    @RolesAllowed({Role.ADMIN, Role.SUBSCRIBER})
    @GetMapping
    public ResponseEntity<SubscriptionView> findByUsername(HttpServletRequest request) {

        final var subscription = service.findOne(utils.getUserByToken(request)).orElseThrow(() -> new NotFoundException(Subscription.class, utils.getUserByToken(request)));

        return ResponseEntity.ok().eTag(Long.toString(subscription.getVersion())).body(subscriptionViewMapper.toSubscriptionView(subscription));
    }

    @Operation(summary = "US010 - Get new subscriptions and cancellations in a specific month")
    @RolesAllowed({Role.PRODUCT_MANAGER, Role.ADMIN})
    @GetMapping("/dashboard/newSubAndCancel")
    public ResponseEntity<DashBoardView> getDashboardData(@RequestParam("month") String month, @RequestParam("year") String year) {

        int monthInt = Integer.parseInt(month);
        int yearInt = Integer.parseInt(year);

        if(monthInt < 1 || monthInt > 12)
        {
            throw new IllegalArgumentException("Invalid month!");
        }

        if(yearInt < 2021)
        {
            throw new IllegalArgumentException("Invalid year!");
        }
        int lastDayOfMonth = 31;
        if (monthInt == 2) {
            if (Year.of(yearInt).isLeap()) {
                lastDayOfMonth = 29;
            } else {
                lastDayOfMonth = 28;
            }
        } else if (monthInt == 4 || monthInt == 6 || monthInt == 9 || monthInt == 11) {
            lastDayOfMonth = 30;
        }
        LocalDate startDate = LocalDate.of(yearInt, monthInt, 1);
        LocalDate endDate = LocalDate.of(yearInt, monthInt, lastDayOfMonth);

        final var subscription = service.forDashBoard(startDate, endDate);

        return ResponseEntity.ok().eTag(Long.toString(subscription.getVersion())).body(subscriptionViewMapper.toDashBoardView(subscription));
    }

    @Operation(summary = "US022 - Switch my plan (upgrade/downgrade)")
    @RolesAllowed({Role.ADMIN, Role.SUBSCRIBER})
    @PatchMapping(value = "/{idPlan}")
    public ResponseEntity<SubscriptionView> switchPlan(HttpServletRequest request, @PathVariable Long idPlan) {
        long id = utils.getUserByToken(request);
        final var subscription = service.switchPlan(idPlan,id);
        return ResponseEntity.ok().eTag(Long.toString(subscription.getVersion())).body(subscriptionViewMapper.toSubscriptionView(subscription));

    }

    @Operation(summary = "US023 - Renew subscription")
    @RolesAllowed({Role.SUBSCRIBER, Role.ADMIN})
    @PatchMapping(value = "/renew")
    public ResponseEntity<SubscriptionView> RenewAnnualSubscription(HttpServletRequest request){

        long id = utils.getUserByToken(request);

        User user = userRepository.getById(id);

        Set<Role> userRoles = user.getAuthorities();

        Iterator<Role> iterator = userRoles.iterator();

        Role firstRole = iterator.next();

        String roleString = firstRole.toString();

        String roleIf = "Role(authority=SUBSCRIBER)";

        String roleIfAdmin = "Role(authority=ADMIN)";
        if(roleString.equals(roleIf) || roleString.equals(roleIfAdmin))
        {
            final var subscription = service.RenewAnnualSubscription(id);
            return ResponseEntity.ok().eTag(Long.toString(subscription.getVersion())).body(subscriptionViewMapper.toSubscriptionView(subscription));
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only subscribers can renew their subscription!");
        }
    }

    @Operation(summary = "US024 - Migrate all subscribers of a certain plan to a different plan")
    @RolesAllowed({Role.ADMIN, Role.MARKETING_DIRECTOR})
    @PutMapping
    public Iterable<SubscriptionView> MigrateAllSubscribers(@Valid @RequestBody final MigrateSubscribersRequest resource) {

        final var subscription = service.migrateAllSubscribers(resource.getCurrentPlanId(), resource.getNewPlanId());
        return subscriptionViewMapper.toSubscriptionView(service.findAll());

    }

    @Operation(summary = "Gets all subscriptions")
    @RolesAllowed({Role.ADMIN})
    @GetMapping(value = "/all")
    public Iterable<SubscriptionView> findAll() {
        return subscriptionViewMapper.toSubscriptionView(service.findAll());
    }

    private Long getVersionFromIfMatchHeader(final String ifMatchHeader) {
        if (ifMatchHeader.startsWith("\"")) {
            return Long.parseLong(ifMatchHeader.substring(1, ifMatchHeader.length() - 1));
        }
        return Long.parseLong(ifMatchHeader);
    }

    @Operation(summary = "Deletes an existing subscription")
    @RolesAllowed({Role.ADMIN})
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<SubscriptionView> delete(final WebRequest request,
                                                   @PathVariable("id") @Parameter(description = "The id of the subscription to delete") final Long idSub) {
        final String ifMatchValue = request.getHeader("If-Match");
        if (ifMatchValue == null || ifMatchValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional DELETE using 'if-match'");
        }
        final int count = service.deleteById(idSub, getVersionFromIfMatchHeader(ifMatchValue));

        return count == 1 ? ResponseEntity.noContent().build() : ResponseEntity.status(409).build();
    }



}