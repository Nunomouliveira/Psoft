package psoftProjectG10.model;


import org.junit.jupiter.api.Test;
import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.subscriptionManagement.api.EditSubscriptionRequest;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.subscriptionManagement.model.SubscriptionStatus;
import psoftProjectG10.userManagement.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubscriptionTest {

    final Plan testPlan = new Plan("Free", 1000, 1, 0, "automatic", 0.00, 0.00, "Plan free");
    final User testUser = new User("admin@mail.com", "Admin123", "Admin");

    long i = 1;
    long j = 2;
    Subscription subscription = new Subscription(testPlan, testUser,"Monthly");

    @Test
    void ensurePlanMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Subscription(null, testUser, "Monthly"));
    }

    @Test
    void ensureNameIsSet() {
        final var subject = new Subscription(testPlan, testUser, "Annual");
        assertEquals(testPlan, subject.getPlan());
    }

    @Test
    void ensureSubscriptionAttributesSet() {
        final var subject = new Subscription(testPlan, testUser, "Monthly");

        assertEquals(testPlan, subject.getPlan());
        assertEquals(testUser, subject.getUser());
        assertEquals("Monthly", subject.getFeeType());

    }

    @Test
    void ensurePatchFeeType() {
        final var patch = new EditSubscriptionRequest("Monthly" , i);

        final var subject = new Subscription(testPlan, testUser, "Monthly");

        subject.applyPatch(0, patch.getFeeType());

        assertEquals("Monthly", subject.getFeeType());
    }


    @Test
    void ensurePatchPlan() {
        final var patch = new EditSubscriptionRequest("Annual" , j);

        final var subject = new Subscription(testPlan, testUser, "Annual");

        subject.applyPatch(0, patch.getFeeType());

        assertEquals(testPlan, subject.getPlan());
    }

    @Test
    void ensureFeeTypeMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Subscription(testPlan, testUser, ""));
    }

    @Test
    void ensureValidFeeType() {
        assertThrows(IllegalArgumentException.class, () -> new Subscription(testPlan, testUser, "InvalidFeeType"));
    }

    @Test
    void ensureSetSubState() {
        Subscription subscription = new Subscription(testPlan, testUser, "Monthly");
        subscription.setSubState(SubscriptionStatus.Cancelled);
        assertEquals(SubscriptionStatus.Cancelled, subscription.getSubState());
    }

    @Test
    void ensureSetStartDate() {
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        Subscription subscription = new Subscription(testPlan, testUser, "Monthly");
        subscription.setStartDate(startDate);
        assertEquals(startDate, subscription.getStartDate());
    }

    @Test
    void ensureSetCancelDate() {
        LocalDate cancelDate = LocalDate.of(2022, 1, 31);
        Subscription subscription = new Subscription(testPlan, testUser, "Monthly");
        subscription.setCancelDate(cancelDate);
        assertEquals(cancelDate, subscription.getCancelDate());
    }

    @Test
    void ensureSetCancellations() {
        Subscription subscription = new Subscription(testPlan, testUser, "Monthly");
        subscription.setCancellations(2);
        assertEquals(2, subscription.getCancellations());
    }

    @Test
    void ensureSetSubscriptions() {
        Subscription subscription = new Subscription(testPlan, testUser, "Monthly");
        subscription.setSubscriptions(5);
        assertEquals(5, subscription.getSubscriptions());
    }

    @Test
    void ensureSetUser() {
        User newUser = new User("newuser@mail.com", "NewUser123", "New User");
        Subscription subscription = new Subscription(testPlan, testUser, "Monthly");
        subscription.setUser(newUser);
        assertEquals(newUser, subscription.getUser());
    }


}





