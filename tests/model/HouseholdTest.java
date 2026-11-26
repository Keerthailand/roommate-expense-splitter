package model;

import java.util.ArrayList;
import java.util.HashMap;

import exceptions.InvalidExpenseException;
import exceptions.PersonNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Tests for adding people and rejecting bad expenses
public class HouseholdTest {

    private Household houseWithAlexAndSam() {
        Household house = new Household();
        house.addPerson("Alex");
        house.addPerson("Sam");
        return house;
    }

    @Test//Valid expense: gets added.
    public void addsValidExpense() throws Exception {
        Household house = houseWithAlexAndSam();
        house.addExpense(new EqualExpense("Wi-Fi", 6000, house.findPerson("Alex"), house.getPeople()));
        assertEquals(1, house.getExpenses().size());
    }

    @Test//Duplicate or blank names: rejected.
    public void rejectsDuplicateAndBlankNames() {
        Household house = houseWithAlexAndSam();
        assertFalse(house.addPerson("ALEX"));
        assertFalse(house.addPerson(""));
        assertFalse(house.addPerson("Smith, Jo"));
        assertEquals(2, house.getPeople().size());
    }

    @Test//$0 or negative amount: rejected.
    public void rejectsZeroOrNegativeAmount() {
        Household house = houseWithAlexAndSam();
        Person alex = house.findPerson("Alex");
        assertThrows(InvalidExpenseException.class,
                () -> house.addExpense(new EqualExpense("Free", 0, alex, house.getPeople())));
        assertThrows(InvalidExpenseException.class,
                () -> house.addExpense(new EqualExpense("Refund", -100, alex, house.getPeople())));
        assertEquals(0, house.getExpenses().size());
    }

    @Test//Blank description: rejected.
    public void rejectsBlankDescription() {
        Household house = houseWithAlexAndSam();
        assertThrows(InvalidExpenseException.class,
                () -> house.addExpense(new EqualExpense(" ", 500, house.findPerson("Alex"), house.getPeople())));
    }

    @Test//Empty split: rejected.
    public void rejectsEmptySplit() {
        Household house = houseWithAlexAndSam();
        assertThrows(InvalidExpenseException.class,
                () -> house.addExpense(new EqualExpense("Nobody", 500, house.findPerson("Alex"), new ArrayList<>())));
    }

    @Test//Custom shares that don’t add up: rejected.
    public void rejectsCustomSharesThatDontAddUp() {
        Household house = houseWithAlexAndSam();
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(house.findPerson("Alex"), 1000L);
        shares.put(house.findPerson("Sam"), 1500L);
        assertThrows(InvalidExpenseException.class,
                () -> house.addExpense(new CustomExpense("Groceries", 3000, house.findPerson("Sam"), shares)));
    }

    @Test//Negative custom share: rejected.
    public void rejectsNegativeCustomShare() {
        Household house = houseWithAlexAndSam();
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(house.findPerson("Alex"), 4000L);
        shares.put(house.findPerson("Sam"), -1000L);
        assertThrows(InvalidExpenseException.class,
                () -> house.addExpense(new CustomExpense("Weird", 3000, house.findPerson("Sam"), shares)));
    }

    @Test//Payer not in the household: throws PersonNotFoundException.
    public void rejectsPayerNotInHousehold() {
        Household house = houseWithAlexAndSam();
        assertThrows(PersonNotFoundException.class,
                () -> house.addExpense(new EqualExpense("Movie", 1500, new Person("Taylor"), house.getPeople())));
    }

    @Test//Participant not in the household: throws PersonNotFoundException.
    public void rejectsParticipantNotInHousehold() {
        Household house = houseWithAlexAndSam();
        ArrayList<Person> group = house.getPeople();
        group.add(new Person("Taylor"));
        assertThrows(PersonNotFoundException.class,
                () -> house.addExpense(new EqualExpense("Movie", 1500, house.findPerson("Alex"), group)));
    }

    @Test//Can’t remove someone who’s in an expense.
    public void cantRemoveSomeoneInAnExpense() throws Exception {
        Household house = houseWithAlexAndSam();
        house.addPerson("Jordan");
        ArrayList<Person> pair = new ArrayList<>();
        pair.add(house.findPerson("Alex"));
        pair.add(house.findPerson("Sam"));
        house.addExpense(new EqualExpense("Lunch", 2000, house.findPerson("Alex"), pair));
        assertFalse(house.removePerson("Sam"));
        assertTrue(house.removePerson("Jordan"));
    }
}