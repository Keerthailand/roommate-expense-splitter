package logic;
import java.util.HashMap;
import model.Expense;
import model.Household;
import model.Person;

public class BalanceCalculator {
    private Household household;

    public BalanceCalculator(Household household){
        this.household=household;
    }

    public HashMap<Person, Long> getAllBalances(){
        HashMap<Person, Long> balances = new HashMap<>();
        for (Person p: household.getPeople()){
            balances.put(p,0L);       //sets the starting balance for everyone at 0
        }
        for (Expense e :household.getExpenses()){
            Person payer = e.getPaidBy();
            balances.put(payer, balances.get(payer)+e.getAmountCents());      //this sets the credit for the person who is paying for the itme
            for (Person p: e.getParticipants()){
                balances.put(p, balances.get(p)-e.getShare(p));      //this sets the amount that everyone owes
            }

        }
        return balances;
    }

    public long getBalance(Person person){
        Long balance =getAllBalances().get(person);
        if (balance==null){
            return 0;
        }
        return balance;
    }

    public String describe(Person person){
        long balance =getBalance(person);
        if (balance>0){
            return person +" is owed "+Expense.formatCents(balance);

        }else if (balance<0){
            return person +" owes "+Expense.formatCents(-balance);
        }
        return person+" is all settled up.";
    }
}
