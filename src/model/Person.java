package model;

public class Person {

    private String name;

    public Person(String name){
        this.name=name.trim();
    }

    public String getName(){
        return name;
    }


    //Method for differentiating different people using Object to check of they are the exact same Object in memory.
    @Override
    public boolean equals(Object other){
        if(!(other instanceof Person)){
            return false;
        }
        return name.equalsIgnoreCase(((Person) other).name);
    }

    //helps return the name and not the actual hashcode ID
    @Override
    public String toString(){
        return name;
    }

    //this is to help turn the name into a hashCode number which is used to find it in storage
    @Override
    public int hashCode(){
        return name.toLowerCase().hashCode();
    }


}
