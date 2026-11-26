package logic;
import model.Expense;
import model.Person;

public class Payment {
    private Person from;
    private Person to;
    private long amountCents;

    public Payment(Person from, Person to, long amountCents) {
        this.from=from;
        this.to=to;
        this.amountCents=amountCents;
    }


    public Person getFrom() {
        return from;
    }

    public Person getTo() {
        return to;
    }



    public long getAmountCents() {
        return amountCents;
    }

    @Override
    public String toString() {
        return from+" pays "+to+" "+Expense.formatCents(amountCents);
    }

}
