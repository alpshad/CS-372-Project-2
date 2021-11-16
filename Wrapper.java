public class Wrapper {
    private Type varType;
    private Object value;
    private DynamicArray<Wrapper> array;

    public Wrapper(Object value) {
        // Check type
        // Could come as "6", "Hello", "[1,2,3,4]"
        //if Translator.isNumeric(value)
    }

    public Wrapper() {

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

    public boolean isNumeric() {
        return this.varType == Type.NUMERIC;
    }

    public boolean isBoolean() {
        return this.varType == Type.BOOLEAN;
    }

    public boolean getBooleanValue() {
        return (boolean) this.value;
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
}
