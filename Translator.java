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
    public static HashMap<String, Func> funcList;
    public static List<String> lines;
    public static int pc;
    public static void main(String[] args) {
        funcList = new HashMap<String, Func>();
        varList = new HashMap<String, Wrapper>();
        try {
            File program = new File(args[0]);
            Scanner scan = new Scanner(program);
            lines = new ArrayList<String>();

            while (scan.hasNextLine()) lines.add(scan.nextLine());
            scan.close();

            //for(String line : lines) System.out.println(line);

            // Add command line args to the list of variables
            for (int i = 1; i < args.length; i++) {
                varList.put("a" + i, new Wrapper(args[i]));
            }

            varList.put("a#", new Wrapper(args.length - 1)); // Total number of command line args

            readFile(lines);
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        }
    }

    // Reads through globally-scoped lines
    public static void readFile(List<String> lines) {
        pc = 0; // Program counter
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
            if (instruction.equals("")) {
                pc++;
                continue;
            }

            Wrapper pcWrap = parseString(instruction, 0, pc, null);
            pc = pcWrap.getProgramCounter();
            /*
            globalVarList<String, Wrapper>;
            scopedVarList<String, Wrapper>;
            funcList<String, DynamicArray<String>>;
            globalVarList: [
                
            ]
            scopedVarList:[
                "x": 1,
                "y": 3
            ]
            funcList: [
                "add": [{"x": , "y", }, startLine {1}, endLine {2}]
            ]

            Lines to execute:

            0  Func add x,y readfile
            1     Return x+y  funchandler
            2  End funchandler - kicks out to readfile
            3
            4  Say "HI" readfile
            5  z = 3    readfile
            6  For i = 1 to 10 Do loopstart = 2  readfile
            7     Say "Hi"   forhandler
            8     x = add 1, 3    forhandler
            9     Say "x is", x   forhandler
            10    For j = 1 to 10 Do    forhandler
            11        ....        forhandler_v2
            12    End
            10 End
            11 
            */
            pc++;
        }
    }

    public static Wrapper parseString(String instruction, int scope, int tempPC, HashMap<String, Wrapper> funcVarList) {
        instruction = instruction.strip();
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

        if (instruction.startsWith("//")) {
            return new Wrapper(0, pc);
        } else if (sayMatch.find()) {
            // Print statement
            if (!sayMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid Say invocation): " + instruction);
                System.exit(1);
            }

            handleSay(sayMatch.group(1), sayMatch.group(2), scope, funcVarList);
        } else if (assgMatch.find()) {
            // Assignment statement
            if (!assgMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid assignment): " + instruction);
                System.exit(1);
            }

            handleAssg(assgMatch.group(1), assgMatch.group(2), scope, funcVarList);
        } else if (returnStatMatch.find()) {
            // Return statement
            if (!returnStatMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid Return statement): " + instruction);
                System.exit(1);
            }

            return handleReturnStat(returnStatMatch.group(1), scope, funcVarList);
        } else if (funcDeclMatch.find()) {
            // Function Declaration
            if (!funcDeclMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid Func declaration): " + instruction);
                System.exit(1);
            }

            return new Wrapper(0, handleFuncDecl(funcDeclMatch.group(1), funcDeclMatch.group(2), scope));
        } else if (funcUseMatch.find()) {
            // Function use
            if (!funcUseMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid function usage): " + instruction);
                System.exit(1);
            }

            return handleFuncUse(funcUseMatch.group(1), funcUseMatch.group(2), scope, funcVarList);
        } else if (condFirstMatch.find()) {
            // Conditional If Then
            if (!condFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid If header): " + instruction);
                System.exit(1);
            }

            return new Wrapper(0, handleCondFirst(condFirstMatch.group(1), scope, funcVarList));
        } else if (condElseMatch.find()) {
            // Conditional Else
            if (!condElseMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) +  " (Invalid Else conditional): " + instruction);
                System.exit(1);
            }

            return new Wrapper(0, handleCondElse(condElseMatch.group(1), scope, funcVarList));
        } else if (endMatch.find()) {
            // End of block
            if (!endMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid End of block): " + instruction);
                System.exit(1);
            }

            handleEnd();
        } else if (whileFirstMatch.find()) {
            // While loop header
            if (!whileFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid While loop header): " + instruction);
                System.exit(1);
            }

            handleWhileFirst(whileFirstMatch.group(1), scope, funcVarList);
        } else if (forFirstMatch.find()) {
            // For loop header
            if (!forFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid For loop header): " + instruction);
                System.exit(1);
            }

            handleForFirst(forFirstMatch.group(1), forFirstMatch.group(2), forFirstMatch.group(3), scope, funcVarList);
        } else {
            System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid expression): \n" + instruction);
            System.exit(1);
        }

        return new Wrapper(0, pc);
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
        return str != null && str.matches("-?\\d+");
    }

    public static Wrapper evaluateExpr(String expr, int scope, int tempPC, HashMap<String, Wrapper> funcVarList) {
        expr = expr.strip();
        System.out.println(expr);
        //System.out.println("Evaluating " + expr);
        // expr = x + y == 5
        // x + y + 5
        // Recurse into lowest forms -- left associative
        // Check regex in order of priority
        // Then attempt to wrap, if not successful or variable, throw error
        // Priority order: Func call, == / And / Or / Xor / Comparison, / / *, + / -
        if (funcVarList != null && funcVarList.get(expr) != null) {
            System.out.println("Function var");
            return funcVarList.get(expr);
        }
        
        Wrapper varVal = varList.get(expr);
        if (varVal != null && varVal.getScope() <= scope) {
            return varVal;
        }

        try {
            Wrapper value = new Wrapper(expr);
            return value;
        } catch (UnsupportedTypeException e) {
            //System.out.println("Catch");
            Pattern funcCall = Pattern.compile("([a-z]+) (([^,]*,?)*)");
            Pattern secondOrder = Pattern.compile("(.*) ?((?:And|Or|Xor|==|<=|>=|<|>)) ?(.*)"); // Strip() afterward
            Pattern not = Pattern.compile("Not (.*)");
            Pattern thirdOrder = Pattern.compile("(.*) ?((?:\\/|\\*|\\%)) ?(.*)"); // Strip()
            Pattern fourthOrder = Pattern.compile("(.*) ?((?:\\+|-)) ?(.*)"); // Strip()
            Matcher funcMatch = funcCall.matcher(expr);
            Matcher secondOrderMatch = secondOrder.matcher(expr);
            Matcher notMatch = not.matcher(expr);
            Matcher thirdOrderMatch = thirdOrder.matcher(expr);
            Matcher fourthOrderMatch = fourthOrder.matcher(expr);
            if (secondOrderMatch.find() && secondOrderMatch.group(0).length() == expr.length()) {
                Wrapper left = evaluateExpr(secondOrderMatch.group(1).strip(), scope, tempPC, funcVarList);
                Wrapper right = evaluateExpr(secondOrderMatch.group(3).strip(), scope, tempPC, funcVarList);

                String operator = secondOrderMatch.group(2).strip();
                if (operator.equals("And")) {
                    // Variables must be booleans
                    if (!(left.isBoolean() && right.isBoolean())) {
                        // Error
                        System.err.println("Type Error on line " + (tempPC + 1) + " : And must be used on boolean types");
                        System.exit(1);
                    }

                    return new Wrapper(left.and(right));
                }

                if (operator.equals("Or")) {
                    //System.out.println(left);
                    //System.out.println(right);
                    if (!(left.isBoolean() && right.isBoolean())) {
                        // Error
                        System.err.println("Type Error: Or must be used on boolean types");
                        System.exit(1);
                    }

                    return new Wrapper(left.or(right));
                }

                if (operator.equals("Xor")) {
                    if (!(left.isBoolean() && right.isBoolean())) {
                        // Error
                        System.err.println("Type Error: Xor must be used on boolean types");
                        System.exit(1);
                    }

                    return new Wrapper(left.xor(right));
                }

                if (operator.equals("==")) {
                    // Check types: numeric or string
                    return new Wrapper(left.equals(right));
                }

                if (operator.equals("<=")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: <= must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.leq(right));
                }

                if (operator.equals(">=")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: >= must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.geq(right));
                }

                if (operator.equals("<")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: < must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.less(right));
                }

                if (operator.equals(">")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: > must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.greater(right));
                }

                System.err.println("Error evaluating expression: " + operator);
                System.exit(1);
            } else if (notMatch.find() && notMatch.group(0).length() == expr.length()) {
                Wrapper value = evaluateExpr(notMatch.group(1), scope, tempPC, funcVarList);
                // Check type -- boolean
                if (!value.isBoolean()) {
                    // Error
                    System.err.println("Type Error: Not must be used on boolean types");
                    System.exit(1);
                }

                return new Wrapper(value.not());
            } else if (thirdOrderMatch.find() && thirdOrderMatch.group(0).length() == expr.length()) {
                Wrapper left = evaluateExpr(thirdOrderMatch.group(1).strip(), scope, tempPC, funcVarList);
                Wrapper right = evaluateExpr(thirdOrderMatch.group(3).strip(), scope, tempPC, funcVarList);

                String operator = thirdOrderMatch.group(2).strip();
                if (operator.equals("/")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: / must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.div(right));
                }

                if (operator.equals("*")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: * must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.mult(right));
                }

                if (operator.equals("%")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: % must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.mod(right));
                }
            } else if (fourthOrderMatch.find() && fourthOrderMatch.group(0).length() == expr.length()) {
                //System.out.println("Addition");
                Wrapper left = evaluateExpr(fourthOrderMatch.group(1).strip(), scope, tempPC, funcVarList);
                Wrapper right = evaluateExpr(fourthOrderMatch.group(3).strip(), scope, tempPC, funcVarList);

                String operator = fourthOrderMatch.group(2).strip();
                if (operator.equals("+")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: + must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.add(right));
                }

                if (operator.equals("-")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error : - must be used on numeric types");
                        System.exit(1);
                    }

                    return new Wrapper(left.sub(right));
                }
            } else if (funcMatch.find() && funcMatch.group(0).length() == expr.length()) {
                return handleFuncUse(funcMatch.group(1), funcMatch.group(2), scope, funcVarList);
            } else {
                System.err.println("Parsing Error executing line " + (tempPC + 1) + ": Invalid Expression " + expr);
                System.exit(1);
            }
        }

        return null;
    }

    public static int handleAssg(String left, String right, int scope, HashMap<String, Wrapper> funcVarList) {
        // x + y => 5 + 3 = 8
        // varList.put(left, right);
        // Evaluate right
        // Possible things: +,-,/,*,and,or,xor,not,==,<,>,<=,>=,func call, values,[]
        Wrapper val = evaluateExpr(right, scope, pc, null);
        varList.put(left, val);
        return 0;
    }

    public static int handleSay(String type, String operand, int scope, HashMap<String, Wrapper> funcVarList) {
        // Say/SaySame, [toPrint]
        // Say "Amy", add 1, 2
        Pattern funcCall = Pattern.compile("([a-z]+) (([^,]*,?)*)");
        Matcher funcMatch = funcCall.matcher(operand);
        String[] items;
        if (funcMatch.find()) {
            //System.out.println(funcMatch.group(0));
            String[] nonFunc = operand.split(funcMatch.group(0));
            String[] temp = nonFunc[0].split("(?:, |,)");
            items = new String[temp.length + 1];
            for (int i = 0; i < temp.length; i++) {
                items[i] = temp[i];
            }

            items[items.length - 1] = funcMatch.group(0);
        } else {
            items = operand.split("(?:, |,)");
        }

        String printable = "";
        for (String item : items) {
            //System.out.println(item);
            Wrapper toPrint = evaluateExpr(item, scope, pc, null);
            printable += toPrint.toString() + " ";
        }

        printable = printable.strip();

        if (type.equals("Say")) {
            System.out.println(printable);
        }

        if (type.equals("SaySame")) {
            System.out.print(printable);
        }

        return 0;
    }

    public static int handleFuncDecl(String name, String parameters, int scope) {
        // parameters: x, y, z, a, b, c
        HashMap<String, Wrapper> localVarList = new HashMap<String, Wrapper>();
        DynamicArray<String> localLines = new DynamicArray<String>();
        DynamicArray<Integer> lineNumbers = new DynamicArray<Integer>();
        DynamicArray<String> paramOrder = new DynamicArray<String>();
        Stack<Integer> blocks = new Stack<Integer>();
        for (int i = pc + 1; !(lines.get(i).equals("End") && blocks.empty()); i++) {
            String instr = lines.get(i).strip();
            if (instr.equals("")) {
                pc++;
                continue;
            }

            lineNumbers.insert(i);
            localLines.insert(instr);
            if (instr.contains("If") || instr.contains("While") || instr.contains("For")) {
                blocks.add(1);
            }

            if (instr.contains("End")) {
                blocks.pop();
            }
        }
        
        String[] indivParams = parameters.split("(?:, |,)");
        for (String param : indivParams) {
            localVarList.put(param, null);
            paramOrder.insert(param);
        }

        Func f = new Func(name, localLines, localVarList, paramOrder, lineNumbers);
        funcList.put(name, f);

        return pc + localLines.length + 1;
    }

    public static Wrapper handleFuncUse(String name, String parameters, int scope, HashMap<String, Wrapper> funcVarList) {
        // HashMap() 
        // for (variable in array) hashmap.put(variable)
        //System.out.println(parameters);
        Func f = funcList.get(name);
        if (f == null) {
            System.err.println("Invalid Function: " + name);
            System.exit(1);
        }

        Pattern funcCall = Pattern.compile("([a-z]+) (([^,]*,?)*)");
        Matcher funcMatch = funcCall.matcher(parameters);
        String[] indivParams;
        if (funcMatch.find()) {
            //System.out.println(funcMatch.group(0));
            String[] nonFunc = parameters.split(funcMatch.group(0));
            String[] temp = nonFunc[0].split("(?:, |,)");
            indivParams = new String[temp.length + 1];
            for (int i = 0; i < temp.length; i++) {
                //System.out.println(temp[i]);
                indivParams[i] = temp[i];
            }

            indivParams[indivParams.length - 1] = funcMatch.group(0);
        } else {
            indivParams = parameters.split("(?:, |,)");
        }

        if (indivParams.length != f.paramOrder.size()) {
            // Syntax error
            System.err.println("Incorrect number of parameters for Function " + name);
            System.err.println("You gave: " + indivParams.length);
            System.err.println("Should be: " + f.paramOrder.size());
            System.exit(1);
        }

        //System.out.println("Executing function");
        // Add params to function
        for (int i = 0; i < indivParams.length; i++) {
            //System.out.println("param " + indivParams[i]);
            // Pass to evaluateExpr
            String paramName = f.paramOrder.get(i);
            Wrapper value = evaluateExpr(indivParams[i].strip(), scope, pc, f.varList);
            System.out.println(value);
            f.varList.put(paramName, value);
        }

        HashMap<String, Wrapper> newFuncList = new HashMap<String, Wrapper>();
        if (funcVarList != null) {
            newFuncList.putAll(funcVarList);
        }

        newFuncList.putAll(f.varList);

        Wrapper retval = new Wrapper();
        // Run Function block
        for (int i = 0; i < f.lines.size(); i++) {
            retval = parseString(f.lines.get(i), scope, f.lineNumbers.get(i), newFuncList);
            System.out.println("Return " + retval);
        }

        return retval;
    }

    public static int handleCondFirst(String condition, int scope, HashMap<String, Wrapper> funcVarList) {

        return 0;
    }
    
    public static int handleCondElse(String condition, int scope, HashMap<String, Wrapper> funcVarList) {
        return 0;
    }

    public static int handleEnd() {
        // Throw error?
        System.out.println("Parsing Error in line " + (pc + 1) + ": Unbounded End");
        System.exit(1);
        return -1;
    }

    public static int handleWhileFirst(String condition, int scope, HashMap<String, Wrapper> funcVarList) {
        return 0;
    }

    public static int handleForFirst(String varName, String start, String end, int scope, HashMap<String, Wrapper> funcVarList) {
        
        return 0;
    }

    public static Wrapper handleReturnStat(String expr, int scope, HashMap<String, Wrapper> funcVarList) {
        // CHECK 
        Wrapper retval = evaluateExpr(expr, scope, pc, funcVarList);
        System.out.println("Return" + retval);
        return retval;
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