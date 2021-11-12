import java.util.HashMap;

public class Func {
    public String name;
    public DynamicArray<String> lines;
    public HashMap<String, Wrapper> varList;

    public Func(String name, DynamicArray<String> lines, HashMap<String, Wrapper> varList) {
        this.name = name;
        this.lines = lines;
        this.varList = varList;    
    }
}
