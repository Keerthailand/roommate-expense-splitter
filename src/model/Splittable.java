package model;

public interface Splittable {
    long getShare(Person person);  //gets how much the person owes
    boolean involves(Person person); //checks if this person is part of the bill at all
}
