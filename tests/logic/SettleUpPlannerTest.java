package logic;
import java.util.ArrayList;
import java.util.HashMap;
import model.EqualExpense;
import model.Household;
import model.Person;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Tests for the settle-up payment plan.
public class SettleUpPlannerTest {

    private Household makeHouse(String... names) {
        Household house = new Household();
        for (String name:names) {
            house.addPerson(name);
        }
        return house;
    }

    private void assertPlanClearsDebts(Household house, ArrayList<Payment> plan) {       //    // Applies the payments to the balances and checks everyone ends at $0
        HashMap<Person, Long> balances = new BalanceCalculator(house).getAllBalances();
        for (Payment pay:plan) {
            assertTrue(pay.getAmountCents() > 0);
            balances.put(pay.getFrom(), balances.get(pay.getFrom()) + pay.getAmountCents());
            balances.put(pay.getTo(), balances.get(pay.getTo()) - pay.getAmountCents());
        }
        for (long balance:balances.values()) {
            assertEquals(0, balance);
        }
    }

    @Test
    public void noExpensesMeansNoPayments() {    // Nobody has spent anything, so the plan should be empty
        Household house = makeHouse("Alex", "Sam");
        assertEquals(0, new SettleUpPlanner(house).makePlan().size());
    }

    @Test// Alex paid $20 for both, so Sam pays Alex $10 in one payment
    public void simpleTwoPersonDebt() throws Exception {
        Household house = makeHouse("Alex", "Sam");
        house.addExpense(new EqualExpense("Lunch", 2000, house.findPerson("Alex"), house.getPeople()));
        ArrayList<Payment> plan = new SettleUpPlanner(house).makePlan();
        assertEquals(1, plan.size());
        assertEquals("Sam pays Alex $10.00", plan.get(0).toString());
    }

    @Test// One person paid rent for all 4, so the other 3 each pay them back
    public void oneBigPayerGetsPaidByEveryone() throws Exception {
        Household house = makeHouse("Alex", "Sam", "Jordan", "Taylor");
        house.addExpense(new EqualExpense("Rent", 400000, house.findPerson("Alex"), house.getPeople()));
        ArrayList<Payment> plan = new SettleUpPlanner(house).makePlan();
        assertEquals(3, plan.size());
        for (Payment pay:plan) {
            assertEquals("Alex", pay.getTo().getName());
            assertEquals(100000, pay.getAmountCents());
        }
    }

    @Test// A covered B and B covered C for the same amount, so C pays A directly and skips B
    public void chainOfDebtsIsShortened() throws Exception {
        // A paid for B, and B paid the same for C. B ends up even,
        // so C should pay A directly instead of going through B.
        Household house = makeHouse("A", "B", "C");
        ArrayList<Person> ab = new ArrayList<>();
        ab.add(house.findPerson("B"));
        house.addExpense(new EqualExpense("Snack", 500, house.findPerson("A"), ab));
        ArrayList<Person> bc = new ArrayList<>();
        bc.add(house.findPerson("C"));
        house.addExpense(new EqualExpense("Snack", 500, house.findPerson("B"), bc));
        ArrayList<Payment> plan = new SettleUpPlanner(house).makePlan();
        assertEquals(1, plan.size());
        assertEquals("C pays A $5.00", plan.get(0).toString());
    }

    @Test// 5 people with uneven bills: everyone ends at $0 in at most 4 payments
    public void messyHouseholdClearsInFewerThanNPayments() throws Exception {
        Household house = makeHouse("A", "B", "C", "D", "E");
        ArrayList<Person> all = house.getPeople();
        house.addExpense(new EqualExpense("Rent", 250003, all.get(0), all));
        house.addExpense(new EqualExpense("Power", 12345, all.get(1), all));
        house.addExpense(new EqualExpense("Food", 8999, all.get(2), new ArrayList<>(all.subList(2, 5))));
        house.addExpense(new EqualExpense("Movie", 4200, all.get(4), new ArrayList<>(all.subList(0, 2))));
        ArrayList<Payment> plan = new SettleUpPlanner(house).makePlan();
        assertTrue(plan.size() <= all.size() - 1);
        assertPlanClearsDebts(house, plan);
    }
}