import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    public static HashMap<String, Wrapper> varList;
    public static HashMap<String, DynamicArray<String>> funcList;
    public static void main(String[] args) {
        try {
            File program = new File(args[0]);
            Scanner scan = new Scanner(program);
            List<String> lines = new ArrayList<String>();

            while (scan.hasNextLine()) lines.add(scan.nextLine());
            scan.close();

            for(String line : lines) System.out.println(line);

            readFile(lines);
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        }
    }

    public static void readFile(List<String> lines) {
        int current = 0;
        while (current < lines.size()) {
            // When find loop: Keep track of initial index, then keep running statements
            // When find end of loop: set current to initial index
            // Do regex
            // If line is start of block (func, cond, loop) then execute all statements in block
            Pattern assignment = Pattern.compile("(\\b[a-z]+[^\\S]*\\b) ?= ?(\\w+)");
            Pattern say = Pattern.compile("(?:Say|SaySame) (.*)");
            Pattern funcDecl = Pattern.compile("Func ([a-z]+) (([a-z]+[^,]*,?)*)"); // Then split on commas
            Pattern funcUse = Pattern.compile("([a-z]+) (([^,]*,?)*)");
            Pattern condFirst = Pattern.compile("If (.*) Then");
            Pattern condElse = Pattern.compile("Else ?(.*)*");
            Pattern end = Pattern.compile("End");
            Pattern whileFirst = Pattern.compile("While (.*) Do");
            Pattern forFirst = Pattern.compile("For (\\b[a-z]+[^\\S]*\\b) ?= ?(\\w+) To ([\\w]+) Do");
            Pattern returnStat = Pattern.compile("Return (.*)");
            String s1 = "z x = 5";
            Matcher assgMatch = assignment.matcher(s1);
            if (assgMatch.find()) {
                if (!assgMatch.group(0).equals(s1)) {
                    // Syntax Error
                    //exit();
                }

                System.out.println(assgMatch.group(0));
                System.out.println(assgMatch.group(1));
            }
            current++;
        }
    }

    public static void parseString(String[] command) {
        /*
        If x == 5 Then
            Say "x is five"
        Else If x == 6 Then
            Say "x is six"
        Else
            Say "x is not five or six"
        End
        */
        if (command[0].equals("If")) {
            // Shut up java
        } 
        /*
        While x < 5 Do
            x += 1
        End
        */
        else if (command[0].equals("While")) {
            //
        } else if (command[0].equals("For")) {
            // TODO: Pass to for loop
        } else if (command[0].equals("Say")) {
            say(command);
        }  else if (command[0].equals("Return")) {
            //
        } else if (command[0].equals("Func")) {
            //
        } else if (command[1].equals("=")) {
            //
       // } else if (funcList.contains(command[0])) {

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
        // Make stack of blocks so innermost block is processed completely first then wrapping block is processed
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

/*    public static boolean validAssignment(String str) {
        // "String"
        // True/False
        //[Array]
        // 6, 6/2
        // add 1,2
        // return str != null && 
    }
*/
    // Function parameters -- superimpose onto first function line
    // Partition var: after ,
    // func: no commas
}