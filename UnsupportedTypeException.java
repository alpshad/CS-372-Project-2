/*
    Author: Amy Paul, Elijah Acuna
    Course: CSc 372, Fall 2021
    Purpose: A runtime exception that gets thrown if an invalid value is 
    assigned to a variable.
*/
public class UnsupportedTypeException extends RuntimeException {
    public UnsupportedTypeException() {}

    public UnsupportedTypeException(String message)
    {
       super(message);
    }
}
