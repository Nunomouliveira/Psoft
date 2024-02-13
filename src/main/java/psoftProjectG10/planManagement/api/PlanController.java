package psoftProjectG10.planManagement.api;

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
import psoftProjectG10.deviceManagement.services.FileStorageService;
import psoftProjectG10.exceptions.NotFoundException;
import psoftProjectG10.planManagement.model.IDPlanCashFlow;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.planManagement.services.PlanService;
import psoftProjectG10.userManagement.model.Role;
import psoftProjectG10.userManagement.model.User;
import psoftProjectG10.userManagement.repositories.UserRepository;
import psoftProjectG10.utils.Utils;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Tag(name="Plans",description = "Plans Endpoints")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
class PlanController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private final PlanService service;

    private final FileStorageService fileStorageService;

    private final PlanViewMapper planViewMapper;

    private final UserRepository userRepository;

    @Autowired
    private Utils utils;

    @Operation(summary = "US03 - Create a Plan")
    @RolesAllowed({Role.ADMIN, Role.MARKETING_DIRECTOR})
    @PostMapping
    public ResponseEntity<PlanView> upsert(@Valid @RequestBody final CreatePlanRequest resource) {

        final var plan = service.create(resource);
        final var newDeviceUri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(plan.getId().toString())
                .build().toUri();

        return ResponseEntity.created(newDeviceUri).eTag(Long.toString(plan.getVersion()))
                .body(planViewMapper.toPlanView(plan));
    }

    @Operation(summary = "US04 - Deactivate a plan / US05 - Partially updates an existing plan / US020 - Promote a plan / US027 - Change the pricing of a plan")
    @RolesAllowed({Role.ADMIN, Role.MARKETING_DIRECTOR})
    @PatchMapping(value = "/{idPlan}")
    public ResponseEntity<PlanView> partialUpdate(HttpServletRequest request,
                                                  @PathVariable("idPlan") @Parameter(description = "The planType of the plan to update") final Long idPlan,
                                                  @Valid @RequestBody final EditPlanRequest resource) {
        final String ifMatchValue = request.getHeader("If-Match");
        if (ifMatchValue == null || ifMatchValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional PATCH using 'if-match'");
        }
        Long id = utils.getUserByToken(request);
        User user = userRepository.getById(id);
        final var plan = service.partialUpdate(idPlan, resource, getVersionFromIfMatchHeader(ifMatchValue),user);
        return ResponseEntity.ok().eTag(Long.toString(plan.getVersion())).body(planViewMapper.toPlanView(plan));
    }

    @Operation(summary = "US06 - Get all existing plans")
    @GetMapping
    public Iterable<PlanView> findAll(HttpServletRequest request, @RequestParam("page") int page, @RequestParam("size") Integer size) {

        long id = utils.getUserByToken(request);

        User user = userRepository.getById(id);

        Set<Role> userRoles = user.getAuthorities();

        Iterator<Role> iterator = userRoles.iterator();

        Role firstRole = iterator.next();

        String roleString = firstRole.toString();

        String roleCustomer = "Role(authority=CUSTOMER)";
        String roleSub = "Role(authority=SUBSCRIBER)";

        if(roleString.equals(roleCustomer) || roleString.equals(roleSub))
        {
            return planViewMapper.toPlanView(service.findAll_User(page, size));
        }
        else {
            return planViewMapper.toPlanView(service.findAll(page, size));
        }
    }

    @Operation(summary = "US021 - Cease a plan")
    @RolesAllowed({Role.ADMIN, Role.MARKETING_DIRECTOR})
    @DeleteMapping(value = "/{idPlan}")
    public ResponseEntity<PlanView> ceasePlan(final WebRequest request,
                                                      @PathVariable("idPlan") @Parameter(description = "The id of the plan to cease") final Long idPlan) {
        final String ifMatchValue = request.getHeader("If-Match");
        if (ifMatchValue == null || ifMatchValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional DELETE using 'if-match'");
        }
        final int count = service.ceasePlanById(idPlan, getVersionFromIfMatchHeader(ifMatchValue));

        return count == 1 ? ResponseEntity.noContent().build() : ResponseEntity.status(409).build();
    }

    @Operation(summary = "US025 - Future cash-flows for the upcoming 'n' months filtered/broken down by plan")
    @RolesAllowed({Role.PRODUCT_MANAGER, Role.ADMIN})
    @GetMapping("/dashboard/futureCashFlowsByPlan")
    public ResponseEntity<Iterable<PlanViewFLOW>> getFutureCashflows(@RequestParam(value = "numberOfMonths") Integer numberOfMonths, @RequestParam(value = "page") Integer page, @RequestParam(value = "size") Integer size) {
        List<IDPlanCashFlow> cashflows = service.forDashBoardFutureCashflows(numberOfMonths,page,size);
        Iterable<PlanViewFLOW> planViewFlow = planViewMapper.toPlanViewFlow(cashflows);
        return ResponseEntity.ok().body( planViewFlow);
    }


    @Operation(summary = "US026 - Year-to-date revenue of my company filtered/broken down by plan")
    @RolesAllowed({Role.ADMIN, Role.MARKETING_DIRECTOR, Role.FINANCIAL_DIRECTOR})
    @GetMapping(value = "/dashboard/totalRevenueByPlan")
    public List<RevenueByPlanView> RevenueByPlan(@RequestParam("page") int page, @RequestParam("size") int size) {
        return planViewMapper.toRevenueByPlanView( service.findRevenueByPlan(page,size));
    }

    @Operation(summary = "US028 - See the price change history")
    @RolesAllowed({Role.ADMIN, Role.MARKETING_DIRECTOR})
    @GetMapping(value = "/priceHistory")
    public Iterable<PriceHistoryView> PriceHistory(HttpServletRequest request,@RequestParam("idPlan") Long id, @RequestParam("page") int page, @RequestParam("size") Integer batchSize) {
        return planViewMapper.toPriceHistoryView(service.findAllPriceHistory(id, page, batchSize));
    }

    @Operation(summary = "Gets a specific plan")
    @GetMapping(value = "/{planType}")
    public ResponseEntity<PlanView> findById(
            @PathVariable("planType") @Parameter(description = "The id of the plan to find") final String id) {
        final var plan = service.findOne(id).orElseThrow(() -> new NotFoundException(Plan.class, id));

        return ResponseEntity.ok().eTag(Long.toString(plan.getVersion())).body(planViewMapper.toPlanView(plan));
    }

    @Operation(summary = "Get a Plan by his name")
    @GetMapping(value="search")
    public ResponseEntity<PlanView> getByPlanType(@RequestParam("planType") final String planType){
        final var plan = service.findByPlanType(planType).orElseThrow(() -> new NotFoundException(Plan.class, planType));

        return ResponseEntity.ok().eTag(Long.toString(plan.getVersion())).body(planViewMapper.toPlanView(plan));
    }

    private Long getVersionFromIfMatchHeader(final String ifMatchHeader) {
        if (ifMatchHeader.startsWith("\"")) {
            return Long.parseLong(ifMatchHeader.substring(1, ifMatchHeader.length() - 1));
        }
        return Long.parseLong(ifMatchHeader);
    }

    @Operation(summary = "Deletes an existing plan")
    @RolesAllowed({Role.ADMIN})
    @DeleteMapping(value = "/delete/{idPlan}")
    public ResponseEntity<PlanView> delete(final WebRequest request,
                                           @PathVariable("idPlan") @Parameter(description = "The planType of the plan to delete") final Long idPlan) {
        final String ifMatchValue = request.getHeader("If-Match");
        if (ifMatchValue == null || ifMatchValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional DELETE using 'if-match'");
        }
        final int count = service.deleteById(idPlan, getVersionFromIfMatchHeader(ifMatchValue));

        return count == 1 ? ResponseEntity.noContent().build() : ResponseEntity.status(409).build();
    }
}
