import java.io.*;
import java.util.Scanner;

public class Translator {
    public static void main(String[] args) {
        try {
            File program = new File(args[0]);
            Scanner scan = new Scanner(program);
            while (scan.hasNextLine()) {
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
}