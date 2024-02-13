package psoftProjectG10.model;

import org.hibernate.StaleObjectStateException;
import psoftProjectG10.deviceManagement.model.Device;
import org.junit.jupiter.api.Test;

import psoftProjectG10.planManagement.model.Plan;
import psoftProjectG10.subscriptionManagement.model.Subscription;
import psoftProjectG10.userManagement.model.User;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTest {

    final Plan testPlan = new Plan("Free", 1000, 1, 0, "automatic", 0.00, 0.00, "Plan free");
    final User testUser = new User("admin@mail.com", "Admin123", "Admin");
    Subscription subscription = new Subscription(testPlan, testUser,"Monthly");
    @Test
    void ensureMacAddressMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,null,"Tv Sala", "Tv 4K"));
    }

    @Test
    void ensureMacAddressMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"","Tv Sala", "Tv 4K"));
    }

    @Test
    void ensureMacAddressMustNotBeBlankSpaces() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"   ", "Tv Sala", "Tv 4k"));
    }

    @Test
    void ensureMacAddressMustHaveSyntax() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"fffffffffffffffff","Tv Sala", "Tv 4K"));
    }

    @Test
    void ensureMacAddressMustBeValid() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"ZZ-44-D6-7B-86-23", "Tv Sala", "Tv 4k"));
    }
    @Test
    void ensureNameMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"56-44-D6-7B-86-23",null, "Tv 4K"));
    }

    @Test
    void ensureNameMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"56-44-D6-7B-86-23","", "Tv 4K"));
    }

    @Test
    void ensureNameMustNotBeBlankSpaces() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"56-44-D6-7B-86-23", "   ", "Tv 4k"));
    }

    @Test
    void ensureDescriptionMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"56-44-D6-7B-86-23","Tv Sala", null));
    }

    @Test
    void ensureDescriptionMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"56-44-D6-7B-86-23","Tv Sala", ""));
    }

    @Test
    void ensureDescriptionMustNotBeBlankSpaces() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription,"56-44-D6-7B-86-23", "Tv Sala", "   "));
    }

    @Test
    void ensureMacAddressIsSet() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        assertEquals("56-44-D6-7B-86-23", device.getMacAddress());
    }

    @Test
    void ensureNameIsSet() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        assertEquals("Tv Sala", device.getName());
    }

    @Test
    void ensureDescriptionIsSet() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        assertEquals("Tv 4K", device.getDescription());
    }
    @Test
    void ensureDeviceAttributesSet() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        assertEquals(subscription, device.getSubscription());
        assertEquals("56-44-D6-7B-86-23", device.getMacAddress());
        assertEquals("Tv Sala", device.getName());
        assertEquals("Tv 4K", device.getDescription());
    }

    @Test
    void ensureDeviceHasSubscription() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        assertEquals(subscription, device.getSubscription());
    }

    @Test
    void ensureDeviceInitializationThrowsOnNullSubscription() {
        assertThrows(IllegalArgumentException.class, () -> new Device(null, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K"));
    }

    @Test
    void ensureDeviceInitializationThrowsOnInvalidMacAddressLength() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription, "56-44-D6-7B-86-2", "Tv Sala", "Tv 4K"));
    }

    @Test
    void ensureDeviceInitializationThrowsOnInvalidNameLength() {
        assertThrows(IllegalArgumentException.class, () -> new Device(subscription, "56-44-D6-7B-86-23", "This name is way too long and exceeds the maximum allowed length", "Tv 4K"));
    }

    @Test
    void ensureDeviceInitializationSucceedsWithValidArguments() {
        assertDoesNotThrow(() -> new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K"));
    }

    @Test
    void ensureDeviceCanUpdateData() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        final long desiredVersion = device.getVersion();
        final String newName = "New Name";
        final String newDescription = "New Description";
        assertDoesNotThrow(() -> device.updateData(desiredVersion, newName, newDescription));
        assertEquals(newName, device.getName());
        assertEquals(newDescription, device.getDescription());
    }

    @Test
    void ensureDeviceUpdateThrowsOnStaleObjectStateException() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        final long desiredVersion = device.getVersion();
        device.updateData(desiredVersion, "New Name", "New Description");
        final String newName = "Newer Name";
        final String newDescription = "Newer Description";
        assertThrows(StaleObjectStateException.class, () -> device.updateData(desiredVersion, newName, newDescription));
        assertThrows(StaleObjectStateException.class, () -> device.applyPatch(desiredVersion, newName, newDescription));
    }

    @Test
    void ensureDeviceCanApplyPatch() {
        final var device = new Device(subscription, "56-44-D6-7B-86-23", "Tv Sala", "Tv 4K");
        final long desiredVersion = device.getVersion();
        final String newName = "New Name";
        final String newDescription = "New Description";
        assertDoesNotThrow(() -> device.applyPatch(desiredVersion, newName, newDescription));
        assertEquals(newName, device.getName());
        assertEquals(newDescription, device.getDescription());
    }

}





