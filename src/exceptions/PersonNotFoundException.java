package exceptions;

public class PersonNotFoundException extends Exception{
    public PersonNotFoundException(String name) {
        super(name + " is not in the household");
    }

}
