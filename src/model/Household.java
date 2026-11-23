package model;
import java.util.ArrayList;
import exceptions.InvalidExpenseException;
import exceptions.PersonNotFoundException;

public class Household {

    private ArrayList<Person> people;
    private ArrayList<Expense> expenses;


    public Household(){
        people = new ArrayList<>();
        expenses = new ArrayList<>();
    }


    public boolean addPerson(String name){
        if (name==null ||name.trim().isEmpty()){
            return false;
        }
        if (name.contains(",")||name.contains(";")||name.contains("=")){  //also avoid same person being added since save file uses these symbols as separators
            return false;
        }
        Person person = new Person(name);
        if (people.contains(person)){  //avoids same person getting added
            return false;
        }
        people.add(person);
        return true;
    }

    public boolean removePerson(String name){
        Person person = findPerson(name);
        if (person ==null){
            return false;
        }
        for (Expense e :expenses){
            if (e.involves(person)){
                return false;
            }

        }
        people.remove(person);
        return true;
    }

    public Person findPerson(String name){
        for (Person p:people){
            if (p.getName().equalsIgnoreCase(name.trim())){
                return p;
            }
        }
        return null;
    }

    public void addExpense(Expense expense) throws InvalidExpenseException, PersonNotFoundException{
        expense.validate();
        if (!people.contains(expense.getPaidBy())){
            throw new PersonNotFoundException(expense.getPaidBy().getName());
        }
        for (Person p : expense.getParticipants()){
            if (!people.contains(p)){
                throw new PersonNotFoundException(p.getName());
            }
        }
        expenses.add(expense);
    }

    public ArrayList<Person> getPeople() {
        return new ArrayList<>(people);
    }

    public ArrayList<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }
}
