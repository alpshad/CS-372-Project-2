import java.io.*;
import java.util.Scanner;
import java.util.HashMap;

public class Translator {
    public static HashMap<String, Wrapper> varList;
    public static HashMap<String, DynamicArray<String>> funcList;
    public static void main(String[] args) {
        try {
            File program = new File(args[0]);
            Scanner scan = new Scanner(program);
            while (scan.hasNextLine()) { // Change to read all at once
                // For loops, mark beginning line and execute code, then go back to beginning
                String command = scan.nextLine();
                if (command.equals("")) {
                    continue;
                }

                String[] line = command.split(" +");
                parseString(line);
                // for (String bit: line) {
                //     System.out.println(bit);
                // }
            }
            scan.close();
        } catch (FileNotFoundException e) {

        }
    }

    public static void parseString(String[] command) {
        if (command[0].equals("If")) {
            // Shut up java
        } else if (command[0].equals("While")) {
            //
        } else if (command[0].equals("For")) {
            // TODO: Pass to for loop
        } else if (command[0].equals("Say")) {
            //
        }  else if (command[0].equals("Return")) {
            //
        } else if (command[0].equals("Func")) {
            //
        } else if (command[1].equals("=")) {
            //
        } else if (funcList.contains(command[0])) {

        } else {
            // Throw some error
        }
        
        
        // If
        // While
        // For
        // Say
        // = [1]
        // Return
        // Func
        // Something in funcList
        // countMultiples args --> Func countMultiples -> add to funcList
        // Process blocks not lines, store function blocks in separate class
        // Mkae stack of blocks so innermost block is processed completely first then wrapping block is processed
    }

    public static void say(String[] message) {
        for (int i = 0; i < message.length; i++) {
            message[i].replace(",", "");
            if (message[i].startsWith("\"") && message[i].endsWith("\"")) {
                System.out.print(message[i] + " ");
                continue;
            } else if (message[i].startsWith("\"") || message[i].endsWith("\"")) {
                // Malformed Literal Input
                // THROW SYNTAX ERROR
            }
            if (varList.containsKey(message[i])) {
                // Where varList is a hashmap mapping variable names to their values
                System.out.print(varList.get(message[i]) + " ");
            }

            System.out.print(message[i] + " ");
        }
        System.out.println();
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean validAssignment(String str) {
        // "String"
        // True/False
        //[Array]
        // 6, 6/2
        // add 1,2
        return str != null && 
    }

    // Function parameters -- superimpose onto first function line
    // Partition var: after ,
    // func: no commas
}