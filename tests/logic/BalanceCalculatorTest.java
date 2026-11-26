package logic;

import java.util.ArrayList;
import java.util.HashMap;

import model.CustomExpense;
import model.EqualExpense;
import model.Household;
import model.Person;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Tests for working out who owes whom.
public class BalanceCalculatorTest {

    private Household makeHouse(String... names) {
        Household house = new Household();
        for (String name : names) {
            house.addPerson(name);
        }
        return house;
    }

    @Test
    public void emptyHouseholdIsSettled() {
        Household house = makeHouse("Alex", "Sam");
        BalanceCalculator calc = new BalanceCalculator(house);
        assertEquals(0, calc.getBalance(house.findPerson("Alex")));
        assertEquals("Alex is all settled up", calc.describe(house.findPerson("Alex")));
    }

    @Test
    public void payerIsOwedEveryoneElsesShare() throws Exception {
        Household house = makeHouse("Alex", "Sam", "Jordan");
        Person alex = house.findPerson("Alex");
        house.addExpense(new EqualExpense("Pizza", 3000, alex, house.getPeople()));
        BalanceCalculator calc = new BalanceCalculator(house);
        assertEquals(2000, calc.getBalance(alex));
        assertEquals(-1000, calc.getBalance(house.findPerson("Sam")));
        assertEquals(-1000, calc.getBalance(house.findPerson("Jordan")));
    }

    @Test
    public void expensesCancelOut() throws Exception {
        Household house = makeHouse("Alex", "Sam");
        Person alex = house.findPerson("Alex");
        Person sam = house.findPerson("Sam");
        house.addExpense(new EqualExpense("Lunch", 2000, alex, house.getPeople()));
        house.addExpense(new EqualExpense("Dinner", 2000, sam, house.getPeople()));
        BalanceCalculator calc = new BalanceCalculator(house);
        assertEquals(0, calc.getBalance(alex));
        assertEquals(0, calc.getBalance(sam));
    }

    @Test
    public void worksWithCustomSplits() throws Exception {
        Household house = makeHouse("Alex", "Sam");
        Person alex = house.findPerson("Alex");
        Person sam = house.findPerson("Sam");
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(alex, 2000L);
        shares.put(sam, 1000L);
        house.addExpense(new CustomExpense("Groceries", 3000, sam, shares));
        BalanceCalculator calc = new BalanceCalculator(house);
        assertEquals(-2000, calc.getBalance(alex));
        assertEquals(2000, calc.getBalance(sam));
        assertEquals("Alex owes $20.00", calc.describe(alex));
        assertEquals("Sam is owed $20.00", calc.describe(sam));
    }

    @Test
    public void balancesAlwaysAddUpToZero() throws Exception {
        Household house = makeHouse("A", "B", "C", "D");
        ArrayList<Person> all = house.getPeople();
        house.addExpense(new EqualExpense("Rent", 100001, all.get(0), all));
        house.addExpense(new EqualExpense("Wi-Fi", 7999, all.get(1), all));
        ArrayList<Person> three = new ArrayList<>(all.subList(1, 4));
        house.addExpense(new EqualExpense("Takeout", 4550, all.get(3), three));
        BalanceCalculator calc = new BalanceCalculator(house);
        long sum = 0;
        for (long balance : calc.getAllBalances().values()) {
            sum += balance;
        }
        assertEquals(0, sum);
    }

    @Test
    public void payerOutsideSplitGetsFullCredit() throws Exception {
        Household house = makeHouse("Alex", "Sam", "Jordan");
        Person alex = house.findPerson("Alex");
        ArrayList<Person> others = new ArrayList<>();
        others.add(house.findPerson("Sam"));
        others.add(house.findPerson("Jordan"));
        house.addExpense(new EqualExpense("Gift", 2000, alex, others));
        BalanceCalculator calc = new BalanceCalculator(house);
        assertEquals(2000, calc.getBalance(alex));
        assertEquals(-1000, calc.getBalance(house.findPerson("Sam")));
    }
}