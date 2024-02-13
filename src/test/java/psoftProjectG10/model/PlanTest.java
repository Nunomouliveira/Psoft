package psoftProjectG10.model;

import org.junit.jupiter.api.Assertions;
import psoftProjectG10.planManagement.api.EditPlanRequest;
import psoftProjectG10.planManagement.model.Plan;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.hibernate.StaleObjectStateException;

class PlanTest {

    final Plan plan = new Plan("Basic", 100, 1, 10, "Rock", 9.99, 99.99, "Basic plan description");

    @Test
	void ensureNameTypeMustNotBeNull() {
		assertThrows(IllegalArgumentException.class, () -> new Plan(null,1000, 3, 5, "Automatic", 0.00, 0.00, "ola a todos"));
	}

	@Test
	void ensureNameMustNotBeBlank() {
		assertThrows(IllegalArgumentException.class, () -> new Plan("",1000, 3, 5, "Automatic", 0.00, 0.00, "ola a todos"));
	}

	@Test
	void ensureNameMustNotBeBlankSpaces() {
		assertThrows(IllegalArgumentException.class, () -> new Plan("     ",1000, 3, 5, "Automatic", 0.00, 0.00, "ola a todos"));
	}

	@Test
	void ensureNameIsSet() {
		final var subject = new Plan("Silver", 1000, 3, 5, "Automatic", 4.99, 49.99, "ola a todos");
		assertEquals("Silver", subject.getPlanType());
	}



	@Test
	void ensurePlanAttributesSet() {
		final var subject = new Plan("Silver", 1000, 3, 5, "Automatic", 4.99, 49.99, "ola a todos");
		subject.setPlanState("Deactivated");
		assertEquals("Silver", subject.getPlanType());
		assertEquals(Integer.valueOf(1000), subject.getNumMinutes());
		assertEquals(Integer.valueOf(3), subject.getMaxDevices());
		assertEquals(Integer.valueOf(5), subject.getMusicCollections());
		assertEquals("Automatic", subject.getMusicSuggestions());
		assertEquals(Double.valueOf(4.99), subject.getMonthlyFee());
		assertEquals(Double.valueOf(49.99), subject.getAnnualFee());
		assertEquals("ola a todos", subject.getHtmlDescription());
		assertEquals("Deactivated", subject.getPlanState());
		assertEquals("Not Promoted", subject.getPlanPromote());
	}

	@Test
	void whenVersionIsStaledItIsNotPossibleToPatch() {
		final var patch = new EditPlanRequest(10, 50,null, null, null, null, null, null, null);
		final var subject = new Plan("Silver", 1000, 3, 5, "Automatic", 4.99, 49.99, "ola a todos");

		assertThrows(StaleObjectStateException.class,
				() -> subject.applyPatch(999, patch.getNumMinutes(), patch.getMaxDevices(), patch.getMusicCollections(), patch.getMusicSuggestions(), patch.getPlanState(), patch.getPlanPromote(), patch.getMonthlyFee(), patch.getAnnualFee(), patch.getHtmlDescription()));
	}

	@Test
	void ensurePatchNameIsIgnored() {
		final var patch = new EditPlanRequest(10, null,null, null, null, null, null, null, null);
		final var subject = new Plan("Silver", 1000, 3, 5, "Automatic", 4.99, 49.99, "ola a todos");

		subject.applyPatch(0, patch.getNumMinutes(), patch.getMaxDevices(), patch.getMusicCollections(), patch.getMusicSuggestions(), patch.getPlanState(), patch.getPlanPromote(), patch.getMonthlyFee(), patch.getAnnualFee(), patch.getHtmlDescription());

		assertEquals("Silver", subject.getPlanType());
	}

	@Test
	void ensurePatchmaxDivices() {
		final var patch = new EditPlanRequest(10, 50,null, null, null, null, null, null, null);

		final var subject = new Plan("Silver", 1000, 3, 5, "Automatic", 4.99, 49.99, "ola a todos");
		subject.setMaxDevices(99);

		subject.applyPatch(0, patch.getNumMinutes(), patch.getMaxDevices(), patch.getMusicCollections(), patch.getMusicSuggestions(), patch.getPlanState(), patch.getPlanPromote(), patch.getMonthlyFee(), patch.getAnnualFee(), patch.getHtmlDescription());

		assertEquals(Integer.valueOf(50), subject.getMaxDevices());
	}


	@Test
	void ensurePatchnumMinutes() {
		final var patch = new EditPlanRequest(10, null,null, null, null, null, null, null, null);

		final var subject = new Plan("Silver", 1000, 3, 5, "Automatic", 4.99, 49.99, "ola a todos");
		subject.setNumMinutes(99);

		subject.applyPatch(0, patch.getNumMinutes(), patch.getMaxDevices(), patch.getMusicCollections(), patch.getMusicSuggestions(), patch.getPlanState(), patch.getPlanPromote(), patch.getMonthlyFee(), patch.getAnnualFee(), patch.getHtmlDescription());

		assertEquals(Integer.valueOf(10), subject.getNumMinutes());
	}

    @Test
    public void testPromotePlan_ValidPromote() {
        plan.setPlanPromote("Promoted");
        Assertions.assertEquals("Promoted", plan.getPlanPromote());
    }

    @Test
    public void testPromotePlan_InvalidPromote() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            plan.setPlanPromote("NotPromoted");
        });
    }

    @Test
    public void testPromotePlan_NullPromote() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            plan.setPlanPromote(null);
        });
    }

    @Test
    public void testPromotePlan_BlankPromote() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            plan.setPlanPromote("");
        });
    }

    @Test
    public void testPromotePlan_CaseSensitivePromote() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            plan.setPlanPromote("pRoMoTeD");
        });
    }
}


