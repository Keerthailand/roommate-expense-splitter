package model;

import java.util.ArrayList;
import java.util.HashMap;

import exceptions.InvalidExpenseException;

public class CustomExpense extends Expense{

    private HashMap<Person, Long> shares;

    public CustomExpense(String description, long amountCents, Person paidBy, HashMap<Person, Long> shares){
        super(description, amountCents, paidBy, new ArrayList<>(shares.keySet()));
        this.shares = new HashMap<>(shares);
    }

    @Override
    public long getShare(Person person){
        Long share =shares.get(person);
        if (share==null){
            return 0;
        }
        return share;
    }


    @Override
    public void validate() throws InvalidExpenseException{
        super.validate();
        for(Person p:shares.keySet()){
            if (shares.get(p)<0){
                throw new InvalidExpenseException(p+"'s shares can't be negative");
            }
        }
        if (getSharesTotal()!=getAmountCents()){
            throw new InvalidExpenseException("Shares add up to " + formatCents(getSharesTotal())
                    + " but the total is " + formatCents(getAmountCents()));
        }
    }

    public long getSharesTotal() {
        long total = 0;
        for (long share :shares.values()){
            total +=share;
        }
        return total;
    }

}
