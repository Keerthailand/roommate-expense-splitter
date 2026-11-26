package model;


import java.util.ArrayList;
import exceptions.InvalidExpenseException;

public abstract class Expense implements Splittable {

    private String description;
    private long amountCents;
    private Person paidBy;
    private ArrayList<Person> participants;

    public Expense(String description, long amountCents,Person paidBy, ArrayList<Person> participants){
        this.description = description;
        this.amountCents =amountCents;
        this.paidBy = paidBy;
        this.participants = new ArrayList<>(participants);

    }

    public String getDescription(){
        return description;

    }

    public long getAmountCents(){
        return amountCents;
    }

    public Person getPaidBy(){
        return paidBy;
    }

    public ArrayList<Person> getParticipants(){
        return new ArrayList<>(participants);
    }

    @Override
    public boolean involves(Person person){
        return paidBy.equals(person) || participants.contains(person);
    }

    //makes sure every field is filled out and throws a custom exception if user didn't.
    public void validate() throws InvalidExpenseException{
        if(description == null || description.trim().isEmpty()){
            throw new InvalidExpenseException("Description can't be blank");
        }
        if (amountCents<=0){
            throw new InvalidExpenseException("Amount must be more than $0.00");
        }
        if (paidBy==null){
            throw new InvalidExpenseException("Someone has to pay for the expense");
        }
        if (participants.isEmpty()){
            throw new InvalidExpenseException("At least one person has to be in the split");
        }

    }

    // Each subclass splits the bill its own way
    @Override
    public abstract long getShare(Person person);


    //helps convert the number into numerical cents form, for example 1234 becomes 12.34;
    public static String formatCents(long cents){
        String sign = cents <0?"-":"";
        cents = Math.abs(cents);
        return String.format("%s$%d.%02d", sign, cents/100, cents%100);
    }


    public static long parseDollars(String text){
        String s= text.trim();
        boolean negative =s.startsWith("-"); //checks if the input is negative
        if (negative){  //if is starts at the second character after
            s=s.substring(1);
        }
        if(s.startsWith("$")){
            s=s.substring(1);
        }
        String[] parts = s.split("\\.",-1);  //cool thing splits it at a period and stores into an array
        if (parts.length>2 || parts[0].isEmpty() || !allDigits(parts[0])){
            throw new NumberFormatException("Not a dollar amount: "+text);
        }
        long cents = Long.parseLong(parts[0])*100;
        if (parts.length==2){
            String decimals=parts[1];
            if (decimals.isEmpty() || decimals.length()>2||!allDigits(decimals)){
                throw new NumberFormatException("Not a dollar amount: "+text);
            }

            if(decimals.length()==1){
                decimals+="0";
            }
            cents+=Long.parseLong(decimals);
        }
        return negative ? -cents : cents;

    }



    private static boolean allDigits(String s){
        for (int i=0;i<s.length();i++){
            if (!Character.isDigit(s.charAt(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString(){
        return description + " ("+formatCents(amountCents) + ", paid by "+paidBy+")";
    }
}
