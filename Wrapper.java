public class Wrapper {
    private Type varType;
    private Object value;
    private int pc;
    private DynamicArray<Wrapper> array;
    private int scope; // 0 is global, then add 1 for each inner scope

    public Wrapper(Object value) {
        // Check type
        // Could come as "6", "Hello", "[1,2,3,4]"
        // if Translator.isNumeric(value)
        if (value instanceof String) {
            // Check type
        } else {

        }

        this.value = value;
    }

    public Wrapper(Object value, int pc) {
        this(value); 
        this.pc = pc;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Wrapper)) {
            return false;
        }

        Wrapper other = (Wrapper) o;

        return (other.varType == this.varType) && (other.value.equals(this.value));
    }

    public int getScope() {
        return this.scope;
    }

    public boolean isNumeric() {
        return this.varType == Type.NUMERIC;
    }

    public boolean isBoolean() {
        return this.varType == Type.BOOLEAN;
    }

    public boolean getBooleanValue() {
        return (boolean) this.value;
    }

    public int getProgramCounter() {
        return this.pc;
    }

    public int setProgramCounter(int pc) {
        this.pc = pc;
    }

    public Object and(Wrapper other) {
        return new Object();
    }

    public Object or(Wrapper other) {
        return new Object();
    }

    public Object xor(Wrapper other) {
        return new Object();
    }

    public Object less(Wrapper other) {
        return new Object();
    }

    public Object greater(Wrapper other) {
        return new Object();
    }

    public Object leq(Wrapper other) {
        return new Object();
    }

    public Object geq(Wrapper other) {
        return new Object();
    }

    public Object add(Wrapper other) {
        return new Object();
    }

    public Object not() {
        return new Object();
    }

    public Object mult(Wrapper other) {
        return new Object();
    }

    public Object div(Wrapper other) {
        return new Object();
    }

    public Object sub(Wrapper other) {
        return new Object();
    }

    @Override
    public String toString() {
        
    }
}
