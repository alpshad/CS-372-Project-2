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
        while (pc < lines.size()) {
            String instruction = lines.get(pc);
            if (instruction.equals("")) {
                pc++;
                continue;
            }

            Wrapper pcWrap = parseString(instruction, 0, pc, null);
            pc = pcWrap.getProgramCounter();
            System.out.println("pc in parse lines " + (pc+1));
            pc++;
        }
    }

    public static Wrapper parseString(String instruction, int scope, int tempPC, HashMap<String, Wrapper> funcVarList) {
        //System.out.println("Instruction: " + instruction);
        //System.out.println("Temp pc: "+(tempPC+1));
        instruction = instruction.strip();
        Pattern assignment = Pattern.compile("(\\b[a-z]+[A-Za-z0-9]*\\b) ?= ?(.+)");
        Pattern say = Pattern.compile("((?:Say|SaySame)) (.*)");
        Pattern funcDecl = Pattern.compile("Func (\\b[a-z]+[A-Za-z0-9]*\\b) ?(([a-z]+[^,]*,? ?)*)"); // Then split on commas
        Pattern funcUse = Pattern.compile("(\\b[a-z]+[A-Za-z0-9]*\\b) ?(([^,]*,? ?)*)");
        Pattern condFirst = Pattern.compile("If (.*) Then");
        Pattern condElse = Pattern.compile("Else ?(.*)");
        Pattern end = Pattern.compile("End");
        Pattern whileFirst = Pattern.compile("While (.*) Do");
        Pattern forFirst = Pattern.compile("For (\\b[a-z]+[A-Za-z0-9]*\\b) ?= ?(.+) To (.+) Do");
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
            return new Wrapper(0, tempPC);
        } else if (sayMatch.find()) {
            // Print statement
            if (!sayMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid Say invocation): " + instruction);
                System.exit(1);
            }

            return handleSay(sayMatch.group(1), sayMatch.group(2), scope, tempPC, funcVarList);
        } else if (returnStatMatch.find()) {
            // Return statement
            if (!returnStatMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid Return statement): " + instruction);
                System.exit(1);
            }

            return handleReturnStat(returnStatMatch.group(1), scope, tempPC, funcVarList);
        } else if (funcDeclMatch.find()) {
            // Function Declaration
            if (!funcDeclMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid Func declaration): " + instruction);
                System.exit(1);
            }

            if (funcDeclMatch.group(2) == null) {
                return new Wrapper(0, handleFuncDecl(funcDeclMatch.group(1), "", scope));
            }

            //System.out.println(funcDeclMatch.group(2));
            return new Wrapper(0, handleFuncDecl(funcDeclMatch.group(1), funcDeclMatch.group(2), scope));
        } else if (condElseMatch.find()) {
            // Conditional Else
            if (!condElseMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) +  " (Invalid Else conditional): " + instruction);
                System.exit(1);
            }

            return handleCondElse(condElseMatch.group(1), scope, tempPC, funcVarList);
        } else if (condFirstMatch.find()) {
            // Conditional If Then
            if (!condFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid If header): " + instruction);
                System.exit(1);
            }

            return handleCondFirst(condFirstMatch.group(1), scope, tempPC, funcVarList);
        } else if (endMatch.find()) {
            // End of block
            if (!endMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid End of block): " + instruction);
                System.exit(1);
            }

            handleEnd(tempPC);
        } else if (whileFirstMatch.find()) {
            // While loop header
            if (!whileFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid While loop header): " + instruction);
                System.exit(1);
            }

            return handleWhileFirst(whileFirstMatch.group(1), scope, tempPC, funcVarList);
        } else if (forFirstMatch.find()) {
            // For loop header
            if (!forFirstMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid For loop header): " + instruction);
                System.exit(1);
            }

            return handleForFirst(forFirstMatch.group(1), forFirstMatch.group(2), forFirstMatch.group(3), scope, tempPC, funcVarList);
        } else if (assgMatch.find()) {
            // Assignment statement
            if (!assgMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid assignment): " + instruction);
                System.exit(1);
            }

            return handleAssg(assgMatch.group(1), assgMatch.group(2), scope, tempPC, funcVarList);
        } else if (funcUseMatch.find()) {
            // Function use
            if (!funcUseMatch.group(0).equals(instruction)) {
                System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid function usage): " + instruction);
                System.exit(1);
            }

            return handleFuncUse(funcUseMatch.group(1), funcUseMatch.group(2), scope, tempPC, funcVarList);
        } else {
            System.err.println("Syntax error executing line " + (tempPC + 1) + " (Invalid expression): \n" + instruction);
            System.exit(1);
        }

        return new Wrapper(0, tempPC);
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+");
    }

    public static Wrapper evaluateExpr(String expr, int scope, int tempPC, HashMap<String, Wrapper> funcVarList) {
        expr = expr.strip();
        System.out.println(expr);
        //System.out.println(funcVarList);

        // Priority order: Func call, == / And / Or / Xor / Comparison, / / *, + / -
        if (funcVarList != null && funcVarList.get(expr) != null) {
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
            Pattern funcCall = Pattern.compile("([a-z]+) (([^,]*,?)*)");
            Pattern boolComp = Pattern.compile("(.*) ?((?:And|Or|Xor)) ?(.*)");
            Pattern secondOrder = Pattern.compile("(.*) ?((?:==|<=|>=|<|>)) ?(.*)"); // Strip() afterward
            Pattern not = Pattern.compile("Not (.*)");
            Pattern thirdOrder = Pattern.compile("(.*) ?((?:\\/|\\*|\\%)) ?(.*)"); // Strip()
            Pattern fourthOrder = Pattern.compile("(.*) ?((?:\\+|-)) ?(.*)"); // Strip()
            Matcher funcMatch = funcCall.matcher(expr);
            Matcher boolCompMatch = boolComp.matcher(expr);
            Matcher secondOrderMatch = secondOrder.matcher(expr);
            Matcher notMatch = not.matcher(expr);
            Matcher thirdOrderMatch = thirdOrder.matcher(expr);
            Matcher fourthOrderMatch = fourthOrder.matcher(expr);
            if (boolCompMatch.find() && boolCompMatch.group(0).length() == expr.length()) {
                Wrapper left = evaluateExpr(boolCompMatch.group(1).strip(), scope, tempPC, funcVarList);
                Wrapper right = evaluateExpr(boolCompMatch.group(3).strip(), scope, tempPC, funcVarList);

                String operator = boolCompMatch.group(2).strip();
                if (operator.equals("And")) {
                    // Variables must be booleans
                    if (!(left.isBoolean() && right.isBoolean())) {
                        // Error
                        System.err.println("Type Error on line " + (tempPC + 1) + " : And must be used on boolean types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.and(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("Or")) {
                    if (!(left.isBoolean() && right.isBoolean())) {
                        // Error
                        System.err.println("Type Error: Or must be used on boolean types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.or(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("Xor")) {
                    if (!(left.isBoolean() && right.isBoolean())) {
                        // Error
                        System.err.println("Type Error: Xor must be used on boolean types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.xor(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }
            } else if (secondOrderMatch.find() && secondOrderMatch.group(0).length() == expr.length()) {
                Wrapper left = evaluateExpr(secondOrderMatch.group(1).strip(), scope, tempPC, funcVarList);
                Wrapper right = evaluateExpr(secondOrderMatch.group(3).strip(), scope, tempPC, funcVarList);

                String operator = secondOrderMatch.group(2).strip();
                

                if (operator.equals("==")) {
                    Wrapper temp =  new Wrapper(left.equals(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("<=")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: <= must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.leq(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals(">=")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: >= must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.geq(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("<")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: < must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.less(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals(">")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: > must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.greater(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
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

                Wrapper temp =  new Wrapper(value.not());
                temp.setProgramCounter(tempPC);
                return temp;
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

                    Wrapper temp =  new Wrapper(left.div(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("*")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: * must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.mult(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("%")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: % must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.mod(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }
            } else if (fourthOrderMatch.find() && fourthOrderMatch.group(0).length() == expr.length()) {
                Wrapper left = evaluateExpr(fourthOrderMatch.group(1).strip(), scope, tempPC, funcVarList);
                Wrapper right = evaluateExpr(fourthOrderMatch.group(3).strip(), scope, tempPC, funcVarList);

                String operator = fourthOrderMatch.group(2).strip();
                if (operator.equals("+")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: + must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.add(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }

                if (operator.equals("-")) {
                    if (!(left.isNumeric() && right.isNumeric())) {
                        // Error
                        System.err.println("Type Error: - must be used on numeric types");
                        System.exit(1);
                    }

                    Wrapper temp =  new Wrapper(left.sub(right));
                    temp.setProgramCounter(tempPC);
                    return temp;
                }
            } else if (funcMatch.find() && funcMatch.group(0).length() == expr.length()) {
                return handleFuncUse(funcMatch.group(1), funcMatch.group(2), scope, tempPC, funcVarList);
            } else {
                System.err.println("Syntax Error executing line " + (tempPC + 1) + " (Invalid expression): " + expr);
                System.exit(1);
            }
        }

        return null;
    }

    public static Wrapper handleAssg(String left, String right, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        Wrapper val = evaluateExpr(right, scope, pc, funcVarList);
        varList.put(left, val);
        val.setProgramCounter(blockPc);
        return val;
    }

    public static Wrapper handleSay(String type, String operand, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        // Say/SaySame, [toPrint]
        // Say "Amy Paul", add 1, 2
        Pattern funcCall = Pattern.compile("(\\b[a-z]+[A-Za-z0-9]*\\b) ?(([^,]*,?)*)");
        Matcher funcMatch = funcCall.matcher(operand);
        String[] items;
        if (funcMatch.find() && funcList.get(funcMatch.group(1)) != null) {
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
            Wrapper toPrint = evaluateExpr(item, scope, pc, funcVarList);
            printable += toPrint.toString() + " ";
        }

        printable = printable.strip();

        if (type.equals("Say")) {
            System.out.println(printable);
        }

        if (type.equals("SaySame")) {
            System.out.print(printable);
        }

        Wrapper retval = new Wrapper();
        retval.setProgramCounter(blockPc);

        return retval;
    }

    public static int handleFuncDecl(String name, String parameters, int scope) {
        // parameters: x, y, z, a, b, c
        HashMap<String, Wrapper> localVarList = new HashMap<String, Wrapper>();
        DynamicArray<String> localLines = new DynamicArray<String>();
        DynamicArray<Integer> lineNumbers = new DynamicArray<Integer>();
        DynamicArray<String> paramOrder = new DynamicArray<String>();
        Stack<Integer> blocks = new Stack<Integer>();
        for (int i = pc + 1; !(lines.get(i).contains("End") && blocks.empty()); i++) {
            String instr = lines.get(i).strip();
            if (instr.equals("")) {
                pc++;
                continue;
            }

            lineNumbers.insert(i);
            localLines.insert(instr);
            if ((instr.contains("If") && !instr.contains("Else")) || instr.contains("While") || instr.contains("For")) {
                blocks.add(1);
            }

            if (instr.contains("End")) {
                blocks.pop();
            }
        }
        
        //System.out.println(parameters);
        String[] indivParams = parameters.split("(?:, |,)");
        int i = 0;
        for (String param : indivParams) {
            System.out.println("Param " + i + ": " + param);
            localVarList.put(param, null);
            paramOrder.insert(param);
            i++;
        }

        Func f = new Func(name, localLines, localVarList, paramOrder, lineNumbers);
        funcList.put(name, f);

        return pc + localLines.length + 1;
    }

    public static Wrapper handleFuncUse(String name, String parameters, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        Func f = funcList.get(name);
        if (f == null) {
            System.err.println("Invalid Function: " + name);
            System.exit(1);
        }

        if (!parameters.equals("")) {
            Pattern funcCall = Pattern.compile("(\\b[a-z]+[A-Za-z0-9]*\\b) ?(([^,]*,?)*)");
            Matcher funcMatch = funcCall.matcher(parameters);
            System.out.println(parameters);
            String[] indivParams;
            if (funcMatch.find()) {
                String[] nonFunc = parameters.split(funcMatch.group(0));
                String[] temp = nonFunc[0].split("(?:, |,)");
                indivParams = new String[temp.length + 1];
                for (int i = 0; i < temp.length; i++) {
                    indivParams[i] = temp[i];
                }

                indivParams[indivParams.length - 1] = funcMatch.group(0);
            } else {
                indivParams = parameters.split("(?:, |,)");
            }

            for (String param: indivParams) System.out.println(param);

            if (indivParams.length != f.paramOrder.size()) {
                // Syntax error
                System.err.println("Incorrect number of parameters for Function " + name);
                System.err.println("You gave: " + indivParams.length);
                System.err.println("Should be: " + f.paramOrder.size());
                System.exit(1);
            }

            // Add params to function
            for (int i = 0; i < indivParams.length; i++) {
                // Pass to evaluateExpr
                String paramName = f.paramOrder.get(i);
                Wrapper value = evaluateExpr(indivParams[i].strip(), scope, pc, f.varList);
                f.varList.put(paramName, value);
            }
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
            i = retval.getProgramCounter()-f.lineNumbers.get(0);
            //System.out.println(retval.getProgramCounter());
            //System.out.println(i);
        }

        retval.setProgramCounter(blockPc);
        return retval;
    }

    public static Wrapper handleCondFirst(String condition, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        Wrapper cond = evaluateExpr(condition, scope, pc, funcVarList);

        Stack<Integer> blocks = new Stack<Integer>();
        int tempPc = blockPc + 1;
        int elseIndex = 0;
        while (!(lines.get(tempPc).contains("End") && blocks.empty())) {
            String instr = lines.get(tempPc).strip();
            if (instr.equals("")) {
                tempPc++;
                continue;
            }

            if (instr.contains("Else") && blocks.empty() && elseIndex == 0) {
                elseIndex = tempPc;
            }

            if ((instr.contains("If") && !instr.contains("Else")) || instr.contains("While") || instr.contains("For")) {
                blocks.add(1);
            }

            if (instr.contains("End")) {
                blocks.pop();
            }

            tempPc++;
        }

        if (elseIndex == 0) {
            elseIndex = tempPc; // End index
        }

        Wrapper retval = new Wrapper();
        if (cond.equals(new Wrapper(true))) {
            for (int i = blockPc + 1; i < elseIndex; i++) {
                retval = parseString(lines.get(i), scope + 1, i, funcVarList);
            }

            if (elseIndex == tempPc) {
                blockPc = elseIndex + 1;
            } else {
                blockPc = elseIndex - 1; 
            }

        } else {
            if (elseIndex == tempPc) {
                blockPc = elseIndex + 1;
            } else {
                blockPc = elseIndex - 1;
            }
        }

        retval.setProgramCounter(blockPc);

        return retval;
    }
    
    public static Wrapper handleCondElse(String condition, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        Wrapper retval = new Wrapper();
        if (!condition.equals("")) {
            Pattern p = Pattern.compile("If (.*) Then");
            Matcher matcher = p.matcher(condition);
            if (!matcher.find() || matcher.group(0).length() != condition.length()) {
                System.err.println("Syntax error in line " + (blockPc + 1) + " (Invalid If header): " + condition);
                System.exit(1);
            }

            retval = handleCondFirst(matcher.group(1), scope, blockPc, funcVarList);
        }

        return retval;
    }

    public static int handleEnd(int blockPc) {
        System.err.println("Parsing Error in line " + (blockPc + 1) + ": Unbounded End");
        System.exit(1);
        return -1;
    }

    public static Wrapper handleWhileFirst(String condition, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        Wrapper cond = evaluateExpr(condition, scope, blockPc, funcVarList);
        Stack<Integer> blocks = new Stack<Integer>();
        int whileEnd = blockPc + 1;
        while (!(lines.get(whileEnd).contains("End") && blocks.empty())) {
            String instr = lines.get(whileEnd).strip();
            if (instr.equals("")) {
                whileEnd++;
                continue;
            }

            if ((instr.contains("If") && !instr.contains("Else")) || instr.contains("While") || instr.contains("For")) {
                blocks.add(1);
            }

            if (instr.contains("End")) {
                blocks.pop();
            }

            whileEnd++;
        }

        Wrapper retval = new Wrapper();
        while (cond.equals(new Wrapper(true))) {
            for (int i = blockPc + 1; i < whileEnd; i++) {
                retval = parseString(lines.get(i), scope, i, funcVarList);
            }

            cond = evaluateExpr(condition, scope, blockPc, funcVarList);
        }

        retval.setProgramCounter(whileEnd);
        return retval;
    }

    public static Wrapper handleForFirst(String varName, String start, String end, int scope, int blockPc, HashMap<String, Wrapper> funcVarList) {
        Wrapper startVal = evaluateExpr(start, scope, blockPc, funcVarList);
        Wrapper endVal = evaluateExpr(end, scope, blockPc, funcVarList);
        if (!startVal.isNumeric() || !endVal.isNumeric()) {
            System.err.println("Type error in line " + (blockPc + 1) + " (Start and End loop values must be numbers)");
            System.exit(1);
        }

        //System.out.println(varName);
        HashMap<String, Wrapper> newFuncList = new HashMap<String, Wrapper>();
        if (funcVarList != null) {
            newFuncList.putAll(funcVarList);
        }

        newFuncList.put(varName, startVal);
        //System.out.println(newFuncList);
        Stack<Integer> blocks = new Stack<Integer>();
        int forEnd = blockPc + 1;
        while (!(lines.get(forEnd).contains("End") && blocks.empty())) {
            String instr = lines.get(forEnd).strip();
            if (instr.equals("")) {
                forEnd++;
                continue;
            }

            if ((instr.contains("If") && !instr.contains("Else")) || instr.contains("While") || instr.contains("For")) {
                blocks.add(1);
            }

            if (instr.contains("End")) {
                blocks.pop();
            }

            forEnd++;
        }

        Wrapper retval = new Wrapper();
        while (new Wrapper(startVal.leq(endVal)).equals(new Wrapper(true))) {
            for (int i = blockPc + 1; i < forEnd; i++) {
                retval = parseString(lines.get(i), scope, i, newFuncList);
            }

            startVal = new Wrapper(startVal.add(new Wrapper(1))); // Increment list index
            newFuncList.put(varName, startVal);
        }

        retval.setProgramCounter(forEnd);
        return retval;
    }

    public static Wrapper handleReturnStat(String expr, int scope, int tempPC, HashMap<String, Wrapper> funcVarList) {
        Wrapper retval = evaluateExpr(expr, scope, tempPC, funcVarList);
        return retval;
    }
}