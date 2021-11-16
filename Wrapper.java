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

    }

    public Object or(Wrapper other) {

    }

    public Object xor(Wrapper other) {

    }

    public Object less(Wrapper other) {

    }

    public Object greater(Wrapper other) {

    }

    public Object leq(Wrapper other) {

    }

    public Object geq(Wrapper other) {

    }

    public Object add(Wrapper other) {

    }

    public Object not() {

    }

    public Object mult(Wrapper other) {

    }

    public Object div(Wrapper other) {

    }

    public Object sub(Wrapper other) {

    }
}
