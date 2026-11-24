package model;
import java.util.ArrayList;

public class EqualExpense extends Expense{
    public EqualExpense(String description, long amountCents, Person paidBy, ArrayList<Person> participants){
        super(description, amountCents, paidBy, participants);
    }


    @Override
    public long getShare(Person person){
        ArrayList<Person> participants = getParticipants();
        int index = participants.indexOf(person);
        if(index==-1){
            return 0;
        }
        long base = getAmountCents()/participants.size();
        long leftover =getAmountCents()%participants.size();
        if (index<leftover){
            return base+1;
        }
        return base;
    }
}
