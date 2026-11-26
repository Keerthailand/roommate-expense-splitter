package logic;
import java.util.ArrayList;
import java.util.HashMap;
import model.Household;
import model.Person;


public class SettleUpPlanner {
    private Household household;

    public SettleUpPlanner(Household household) {
        this.household = household;
    }

    public ArrayList<Payment> makePlan() {
        HashMap<Person, Long> balances = new BalanceCalculator(household).getAllBalances();
        ArrayList<Person> people = household.getPeople();
        ArrayList<Payment> plan = new ArrayList<>();

        while (true) {          // Keep going until nobody owes anything
            Person biggestDebtor =null;
            Person biggestCreditor =null;
            for (Person p:people) {
                long balance=balances.get(p);
                if (balance < 0 && (biggestDebtor ==null || balance < balances.get(biggestDebtor))) {      // Pay as much as possible without overpaying, so at least one person hits $0 this round.
                    biggestDebtor = p;
                }
                if (balance > 0 && (biggestCreditor ==null || balance > balances.get(biggestCreditor))) {
                    biggestCreditor = p;
                }
            }
            // Nobody owes anything, so we're done
            if (biggestDebtor == null || biggestCreditor == null) {
                break;
            }

            long owed = -balances.get(biggestDebtor);
            long due = balances.get(biggestCreditor);
            long amount = Math.min(owed, due);

            plan.add(new Payment(biggestDebtor, biggestCreditor, amount));
            balances.put(biggestDebtor, balances.get(biggestDebtor) + amount);
            balances.put(biggestCreditor, balances.get(biggestCreditor) - amount);      // Record the payment so the next round sees the new balances
        }
        return plan;
    }
}