package model;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EqualExpenseTest {

    private ArrayList<Person> people(String... names) {
        ArrayList<Person> list = new ArrayList<>();
        for (String name : names) {
            list.add(new Person(name));
        }
        return list;
    }

    @Test
    public void splitsEvenAmountEqually() {
        ArrayList<Person> group = people("Alex", "Sam");
        EqualExpense e = new EqualExpense("Wi-Fi", 6000, group.get(0), group);
        assertEquals(3000, e.getShare(group.get(0)));
        assertEquals(3000, e.getShare(group.get(1)));
    }

    @Test
    public void leftoverCentsGoToFirstPeople() {
        ArrayList<Person> group = people("Alex", "Sam", "Jordan");
        EqualExpense e = new EqualExpense("Pizza", 1000, group.get(0), group);
        assertEquals(334, e.getShare(group.get(0)));
        assertEquals(333, e.getShare(group.get(1)));
        assertEquals(333, e.getShare(group.get(2)));
    }

    @Test
    public void sharesAddUpToTotal() {
        ArrayList<Person> group = people("A", "B", "C", "D", "E", "F", "G");
        EqualExpense e = new EqualExpense("Rent", 100003, group.get(0), group);
        long sum = 0;
        for (Person p : group) {
            sum += e.getShare(p);
        }
        assertEquals(100003, sum);
    }

    @Test
    public void outsiderOwesNothing() {
        ArrayList<Person> group = people("Alex", "Sam");
        EqualExpense e = new EqualExpense("Wi-Fi", 6000, group.get(0), group);
        assertEquals(0, e.getShare(new Person("Jordan")));
    }

    @Test
    public void payerCanBeLeftOutOfSplit() {
        Person alex = new Person("Alex");
        ArrayList<Person> group = people("Sam", "Jordan");
        EqualExpense e = new EqualExpense("Gift", 2000, alex, group);
        assertEquals(0, e.getShare(alex));
        assertEquals(1000, e.getShare(group.get(0)));
        assertTrue(e.involves(alex));
    }
}
