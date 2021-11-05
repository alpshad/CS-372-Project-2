public class Wrapper {
    private Type varType;
    private String value;
    private DynamicArray<Wrapper> array;

    public Wrapper(Object value) {
        // Check type
        // Could come as "6", "Hello", "[1,2,3,4]"
        //if Translator.isNumeric(value)
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
}
