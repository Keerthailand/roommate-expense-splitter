package storage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import logic.BalanceCalculator;
import model.CustomExpense;
import model.EqualExpense;
import model.Household;
import model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

//Tests for saving to and loading from the CSV files.
public class FileManagerTest {

    @TempDir
    File folder;     //separate folder so no real data is touched and the tests stay separate

    private Household sampleHouse() throws Exception {      //setting up a example househodl to use for tests
        Household house = new Household();
        house.addPerson("Alex");
        house.addPerson("Sam");
        house.addPerson("Jordan");
        Person alex = house.findPerson("Alex");
        Person sam = house.findPerson("Sam");
        house.addExpense(new EqualExpense("Pizza, wings, and soda", 1000, alex, house.getPeople()));
        HashMap<Person, Long> shares = new HashMap<>();
        shares.put(alex, 2000L);
        shares.put(sam, 1000L);
        house.addExpense(new CustomExpense("Groceries", 3000, sam, shares));
        return house;
    }

    private void writeFile(String name, String... lines) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(new File(folder, name)))) {
            for (String line : lines) {
                out.println(line);
            }
        }
    }

    @Test// Saving and loading gives back the same people, expenses, and balances, and a description with commas survives
    public void saveThenLoadKeepsEverything() throws Exception {
        Household house = sampleHouse();
        FileManager files = new FileManager(folder.getPath());
        files.save(house);
        Household loaded = files.load();
        assertEquals(house.getPeople(), loaded.getPeople());
        assertEquals(2, loaded.getExpenses().size());
        assertEquals("Pizza, wings, and soda", loaded.getExpenses().get(0).getDescription());
        assertEquals(0, files.getSkippedLines());
        BalanceCalculator before = new BalanceCalculator(house);           // Balances should be exactly the same after loading
        BalanceCalculator after = new BalanceCalculator(loaded);
        for (Person p : house.getPeople()) {
            assertEquals(before.getBalance(p), after.getBalance(p));
        }
    }

    @Test// The person who got the extra cent still gets it after loading, since list order is kept
    public void leftoverCentOrderIsKept() throws Exception {
        Household house = sampleHouse();
        FileManager files = new FileManager(folder.getPath());
        files.save(house);
        Household loaded = files.load();
        assertEquals(334, loaded.getExpenses().get(0).getShare(loaded.findPerson("Alex")));
    }

    @Test// Loading from a folder with no save files gives an empty household instead of crashing
    public void missingFilesGiveEmptyHousehold() throws Exception {
        FileManager files = new FileManager(new File(folder, "nothing-here").getPath());
        Household loaded = files.load();
        assertEquals(0, loaded.getPeople().size());
        assertEquals(0, loaded.getExpenses().size());
    }

    @Test// save() makes the data folder if it doesn't exist yet
    public void saveCreatesFolderIfMissing() throws Exception {
        File newFolder = new File(folder, "new-data");
        new FileManager(newFolder.getPath()).save(sampleHouse());
        assertTrue(new File(newFolder, "people.csv").exists());
        assertTrue(new File(newFolder, "expenses.csv").exists());
    }

    @Test// Bad lines are skipped and counted, and the good lines still load
    public void brokenLinesAreSkipped() throws Exception {
        writeFile("people.csv", "Alex", "Sam", "", "alex");
        writeFile("expenses.csv", "EQUAL,2000,Alex,Alex;Sam,Lunch", "EQUAL,abc,Alex,Alex;Sam,Bad amount", "EQUAL,500,Taylor,Alex,Unknown payer", "CUSTOM,3000,Sam,Alex=1000;Sam=1000,Doesn't add up", "SOMETHING,100,Alex,Alex,Bad type", "not even close");
        FileManager files = new FileManager(folder.getPath());
        Household loaded = files.load();
        assertEquals(2, loaded.getPeople().size());
        assertEquals(1, loaded.getExpenses().size());
        assertEquals("Lunch", loaded.getExpenses().get(0).getDescription());
        // duplicate "alex" + 5 bad expenses
        assertEquals(6, files.getSkippedLines());
    }

    @Test
    public void savingEmptyHouseholdWorks() throws Exception {
        FileManager files = new FileManager(folder.getPath());
        files.save(new Household());
        Household loaded = files.load();
        assertEquals(new ArrayList<Person>(), loaded.getPeople());
    }
}