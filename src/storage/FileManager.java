package storage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import exceptions.InvalidExpenseException;
import exceptions.PersonNotFoundException;
import model.CustomExpense;
import model.EqualExpense;
import model.Expense;
import model.Household;
import model.Person;

//Saves a household to two CSV files and loads it back
//people.csv has one name per line
//expenses.csv has one expense per line, with the description last

public class FileManager {
    private File peopleFile;
    private File expensesFile;
    private int skippedLines;

    public FileManager(String folder) {
        peopleFile = new File(folder, "people.csv");
        expensesFile = new File(folder, "expenses.csv");
    }

    public void save(Household house) throws IOException {
        peopleFile.getParentFile().mkdirs();

        try (PrintWriter out = new PrintWriter(new FileWriter(peopleFile))) {
            for (Person p:house.getPeople()) {
                out.println(p.getName());
            }
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(expensesFile))) {
            for (Expense e:house.getExpenses()) {
                out.println(toLine(e));
            }
        }
    }


    public Household load() throws IOException {           // missing files give an empty household broken lines are skipped
        Household house = new Household();
        skippedLines = 0;
        if (peopleFile.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(peopleFile))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (!line.trim().isEmpty() && !house.addPerson(line)) {
                        skippedLines++;
                    }
                }
            }
        }
        if (expensesFile.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(expensesFile))) {
                String line;
                while ((line=in.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        house.addExpense(fromLine(line, house));
                    } catch (InvalidExpenseException | PersonNotFoundException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        skippedLines++;
                    }
                }
            }
        }
        return house;
    }

    public int getSkippedLines() {
        return skippedLines;
    }

    private String toLine(Expense e) {
        String type;
        String split = "";
        if (e instanceof CustomExpense) {
            type = "CUSTOM";
            for (Person p : e.getParticipants()) {
                if (!split.isEmpty()) {
                    split += ";";
                }
                split += p.getName()+"="+e.getShare(p);
            }
        } else {
            type = "EQUAL";
            for (Person p : e.getParticipants()) {
                if (!split.isEmpty()) {
                    split += ";";
                }
                split += p.getName();
            }
        }
        return type+","+e.getAmountCents()+","+e.getPaidBy().getName()+","+split+","+e.getDescription();
    }

    private Expense fromLine(String line, Household house) throws PersonNotFoundException {
        // limit of 5 keeps any commas inside the description
        String[] parts = line.split(",", 5);
        String type = parts[0];
        long amount = Long.parseLong(parts[1]);
        Person payer = lookUp(parts[2], house);
        String[] splitParts = parts[3].split(";");
        String description = parts[4];

        if (type.equals("CUSTOM")) {
            HashMap<Person, Long> shares = new HashMap<>();
            for (String part : splitParts) {
                String[] nameAndShare = part.split("=");
                shares.put(lookUp(nameAndShare[0], house), Long.parseLong(nameAndShare[1]));
            }
            return new CustomExpense(description, amount, payer, shares);
        }
        if (type.equals("EQUAL")) {
            ArrayList<Person> participants = new ArrayList<>();
            for (String name : splitParts) {
                participants.add(lookUp(name, house));
            }
            return new EqualExpense(description, amount, payer, participants);
        }
        throw new NumberFormatException("Unknown expense type: " + type);
    }

    private Person lookUp(String name, Household house) throws PersonNotFoundException {   //looks for the person
        Person p = house.findPerson(name);
        if (p == null) {
            throw new PersonNotFoundException(name);
        }
        return p;
    }
}