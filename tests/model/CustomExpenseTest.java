package model;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for splitting a bill by custom amounts.
 */
public class CustomExpenseTest {

    @Test
    public void eachPersonOwesTheirAmount() {
        Person alex = new Person("Alex");
        Person sam = new Person("Sam");
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(alex, 2000L);
        shares.put(sam, 1000L);
        CustomExpense e = new CustomExpense("Groceries", 3000, sam, shares);
        assertEquals(2000, e.getShare(alex));
        assertEquals(1000, e.getShare(sam));
    }

    @Test
    public void outsiderOwesNothing() {
        Person alex = new Person("Alex");
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(alex, 500L);
        CustomExpense e = new CustomExpense("Coffee", 500, alex, shares);
        assertEquals(0, e.getShare(new Person("Jordan")));
        assertFalse(e.involves(new Person("Jordan")));
    }

    @Test
    public void sharesTotalAddsEveryone() {
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(new Person("A"), 1234L);
        shares.put(new Person("B"), 766L);
        CustomExpense e = new CustomExpense("Dinner", 2000, new Person("A"), shares);
        assertEquals(2000, e.getSharesTotal());
    }

    @Test
    public void namesMatchIgnoringCase() {
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(new Person("Alex"), 700L);
        CustomExpense e = new CustomExpense("Snacks", 700, new Person("Alex"), shares);
        assertEquals(700, e.getShare(new Person("alex")));
    }
}