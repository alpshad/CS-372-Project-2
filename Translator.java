import java.io.*;
import java.util.Scanner;
import java.util.Stack;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    public static HashMap<String, Wrapper> varList;
    public static HashMap<String, DynamicArray<String>> funcList;
    public static void main(String[] args) {
        varList = new HashMap<String, Wrapper>();
        funcList = new HashMap<String, DynamicArray<String>>();
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
        int pc = 0; // Program counter
        Stack<Integer> loopStart = new Stack<Integer>();
        Stack<Integer> blockStart = new Stack<Integer>();
        boolean blockFlag = false;
        boolean loop = false;
        while (pc < lines.size()) {
            // When find loop: Keep track of initial index, then keep running statements
            // When find end of loop: set current to initial index
            // Do regex
            // If line is start of block (func, cond, loop) then execute all statements in block
            String instruction = lines.get(pc);
            int type = parseString(instruction, pc, blockFlag); // regular, loop, funcdecl, blockend
            if (type == 1) {
                // Loop
                loopStart.push(pc);
                blockStart.push(pc);
                blockFlag = true;
                loop = true;
            } else if (type == 2) {
                // Func Decl
                blockFlag = true;
            } else if (type == 3 && loop) {
                // End of Loop -- CHECK ON CONDITIONS
                pc = loopStart.pop();
                loop = !loopStart.isEmpty();
                blockStart.pop();
                blockFlag = !blockStart.isEmpty();
            }
            /*
            stack<queue<int>> = [2, 4, 2]

            Func add x,y
                Return x+y
            End

            1 Say "HI"
            2 For i = 1 to 10 Do loopstart = 2
            3    Say "Hi"
            4    x = add 1, 3
            5    Say "x is", x
            6 End
            7 ... <--
            */
            pc++;
        }
    }

    public static int parseString(String instruction, int pc, boolean blockFlag) {
        Pattern assignment = Pattern.compile("(\\b[a-z]+[^\\S]*\\b) ?= ?(.+)");
        Pattern say = Pattern.compile("((?:Say|SaySame)) (.*)");
        Pattern funcDecl = Pattern.compile("Func ([a-z]+) (([a-z]+[^,]*,?)*)"); // Then split on commas
        Pattern funcUse = Pattern.compile("([a-z]+) (([^,]*,?)*)");
        Pattern condFirst = Pattern.compile("If (.*) Then");
        Pattern condElse = Pattern.compile("Else ?(.*)");
        Pattern end = Pattern.compile("End");
        Pattern whileFirst = Pattern.compile("While (.*) Do");
        Pattern forFirst = Pattern.compile("For (\\b[a-z]+[^\\S]*\\b) ?= ?(.+) To (.+) Do");
        Pattern returnStat = Pattern.compile("Return (.*)");

        Matcher assgMatch = assignment.matcher(instruction);
        Matcher sayMatch = say.matcher(instruction);
        Matcher funcDeclMatch = funcDecl.matcher(instruction);
        Matcher funcUseMatch = funcUse.matcher(instruction);
        Matcher condFirstMatch = condFirst.matcher(instruction);
        Matcher condElseMatch = condElse.matcher(instruction);
        Matcher endMatch = end.matcher(instruction);
        Matcher whileFirstMatch = whileFirst.matcher(instruction);
        Matcher forFirstMatch = forFirst.matcher(instruction);
        Matcher returnStatMatch = returnStat.matcher(instruction);

        if (assgMatch.find()) {
            // Assignment statement
            if (!assgMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid assignment): " + instruction);
                System.exit(1);
            }

            handleAssg(assgMatch.group(1), assgMatch.group(2));
        } else if (sayMatch.find()) {
            // Print statement
            if (!sayMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid Say invocation): " + instruction);
                System.exit(1);
            }

            handleSay(assgMatch.group(1), assgMatch.group(2));
        } else if (funcDeclMatch.find()) {
            // Function Declaration
            if (!funcDeclMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid Func declaration): " + instruction);
                System.exit(1);
            }

            handleFuncDecl(funcDeclMatch.group(1), funcDeclMatch.group(2));
        } else if (funcUseMatch.find()) {
            // Function use
            if (!funcUseMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid function usage): " + instruction);
                System.exit(1);
            }

            handleFuncUse(funcUseMatch.group(1), funcUseMatch.group(2));
        } else if (condFirstMatch.find()) {
            // Conditional If Then
            if (!condFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid If header): " + instruction);
                System.exit(1);
            }

            handleCondFirst(condFirstMatch.group(1));
        } else if (condElseMatch.find()) {
            // Conditional Else
            if (!condElseMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid Else conditional): " + instruction);
                System.exit(1);
            }

            handleCondElse(condElseMatch.group(1));
        } else if (endMatch.find()) {
            // End of block
            if (!endMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid End of block): " + instruction);
                System.exit(1);
            }

            handleEnd();
        } else if (whileFirstMatch.find()) {
            // While loop header
            if (!whileFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid While loop header): " + instruction);
                System.exit(1);
            }

            handleWhileFirst(whileFirstMatch.group(1));
        } else if (forFirstMatch.find()) {
            // For loop header
            if (!forFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid For loop header): " + instruction);
                System.exit(1);
            }

            handleForFirst(forFirstMatch.group(1), forFirstMatch.group(2), forFirstMatch.group(3));
        } else if (returnStatMatch.find()) {
            // Return statement
            if (!returnStatMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error in line (Invalid Return statement): " + instruction);
                System.exit(1);
            }

            handleReturnStat(returnStatMatch.group(1));
        } else {
            System.err.println("Syntax error in line (Invalid expression): \n" + instruction);
            System.exit(1);
        }
    }

    // public static void say(String[] message) {
    //     for (int i = 0; i < message.length; i++) {
    //         message[i].replace(",", "");
    //         if (message[i].startsWith("\"") && message[i].endsWith("\"")) {
    //             System.out.print(message[i] + " ");
    //             continue;
    //         } else if (message[i].startsWith("\"") || message[i].endsWith("\"")) {
    //             // Malformed Literal Input
    //             // THROW SYNTAX ERROR
    //         }
    //         if (varList.containsKey(message[i])) {
    //             // Where varList is a hashmap mapping variable names to their values
    //             System.out.print(varList.get(message[i]) + " ");
    //         }

    //         System.out.print(message[i] + " ");
    //     }
    //     System.out.println();
    // }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("[-+]?\\d*\\.?\\d+");
    }

    public static int handleAssg(String left, String right) {
        // x + y => 5 + 3 = 8
        // varList.put(left, right);
        return 0;
    }

    public static int handleSay(String type, String operand) {
        // Say/SaySame, [toPrint]
        return 0;
    }

    public static int handleFuncDecl(String name, String parameters) {
        return 0;
    }

    public static int handleFuncUse(String name, String parameters) {
        return 0;
    }

    public static int handleCondFirst(String condition) {
        return 0;
    }
    
    public static int handleCondElse(String condition) {
        return 0;
    }

    public static int handleEnd() {
        return 0;
    }

    public static int handleWhileFirst(String condition) {
        return 0;
    }

    public static int handleForFirst(String varName, String start, String end) {
        return 0;
    }

    public static int handleReturnStat(String expr) {
        return 0;
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