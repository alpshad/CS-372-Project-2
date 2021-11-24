/*
    Author: Amy Paul, Elijah Acuna
    Course: CSc 372, Fall 2021
    Purpose: Holds a function definition with its lines of code, program
    counters, name, and parameters.
*/
import java.util.HashMap;

public class Func {
    public String name;
    public DynamicArray<String> lines;
    public HashMap<String, Wrapper> varList;
    public DynamicArray<String> paramOrder;
    public DynamicArray<Integer> lineNumbers;

    public Func(String name, DynamicArray<String> lines, HashMap<String, Wrapper> varList, DynamicArray<String> paramOrder, DynamicArray<Integer> lineNumbers) {
        this.name = name;
        this.lines = lines;
        this.varList = varList;    
        this.paramOrder = paramOrder;
        this.lineNumbers = lineNumbers;
    }
}
