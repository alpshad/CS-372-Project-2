public class Wrapper {
    private Type varType;
    private Object value;
    private int pc;
    private int scope; // 0 is global, then add 1 for each inner scope
    public boolean funcReturn;

    public Wrapper() { }

    public Wrapper(Object value) {
        // Check type
        // Could come as "6", 6, ""Hello"", "[1,2,3,4]"
        // if Translator.isNumeric(value)
        if (value instanceof String) {
            // Check type
            // String: Anything starting and ending with a quote
            // int: Anything numeric
            // Boolean: True or False
            String str = (String) value;
            if (str != null && str.startsWith("\"") && str.endsWith("\"")) {
                // String
                str = str.substring(1, str.length() - 1);
                this.value = str;
                this.varType = Type.STRING;
            } else if (str != null && str.matches("-?\\d+")) {
                // Int
                this.value = Integer.parseInt(str);
                this.varType = Type.NUMERIC;
            } else if (str != null && str.matches("(?:True|False)")) {
                // Boolean
                if (value.equals("True")) {
                    this.value = true;
                } else {
                    this.value = false;
                }

                this.varType = Type.BOOLEAN;
            } else {
                throw new UnsupportedTypeException();
            }
        } else if (value instanceof Integer) {
            this.varType = Type.NUMERIC;
            this.value = value;
        } else if (value instanceof Boolean) {
            this.varType = Type.BOOLEAN;
            this.value = value;
        } else {
            throw new UnsupportedTypeException();
        }
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

    public void setReturn() {
        this.funcReturn = true;
    }

    public boolean isNumeric() {
        return this.varType == Type.NUMERIC;
    }

    public boolean isBoolean() {
        return this.varType == Type.BOOLEAN;
    }

    public boolean getBooleanValue() {
        if (!this.isBoolean()) {
            System.err.println("Unsupported Operation");
            System.exit(1);
        }
        
        return (boolean) this.value;
    }

    public int getNumericValue() {
        if (!this.isNumeric()) {
            System.err.println("Unsupported Operation");
            System.exit(1);
        }

        return (int) this.value;
    }

    public int getProgramCounter() {
        return this.pc;
    }

    public void setProgramCounter(int pc) {
        this.pc = pc;
    }

    public Object and(Wrapper other) {
        return this.getBooleanValue() && other.getBooleanValue();
    }

    public Object or(Wrapper other) {
        return this.getBooleanValue() || other.getBooleanValue();
    }

    public Object xor(Wrapper other) {
        if (this.getBooleanValue() && !other.getBooleanValue()) return true;
        if (!this.getBooleanValue() && other.getBooleanValue()) return true;

        return false;
    }

    public Object less(Wrapper other) {
        return this.getNumericValue() < other.getNumericValue();
    }

    public Object greater(Wrapper other) {
        return this.getNumericValue() > other.getNumericValue();
    }

    public Object leq(Wrapper other) {
        return this.getNumericValue() <= other.getNumericValue();
    }

    public Object geq(Wrapper other) {
        return this.getNumericValue() >= other.getNumericValue();
    }

    public Object add(Wrapper other) {
        return this.getNumericValue() + other.getNumericValue();
    }

    public Object not() {
        return !this.getBooleanValue();
    }

    public Object mult(Wrapper other) {
        return this.getNumericValue() * other.getNumericValue();
    }

    public Object div(Wrapper other) {
        return (int) (this.getNumericValue() / other.getNumericValue());
    }

    public Object mod(Wrapper other) {
        return this.getNumericValue() % other.getNumericValue();
    }

    public Object sub(Wrapper other) {
        return this.getNumericValue() - other.getNumericValue();
    }

    public Object pow(Wrapper other) {
        return (int) Math.pow(this.getNumericValue(), other.getNumericValue());
    }

    @Override
    public String toString() {
        if (this.isBoolean()) {
            if (this.getBooleanValue()) {
                return "True";
            }

            return "False";
        }

        if (this.isNumeric()) {
            return Integer.toString(this.getNumericValue());
        }

        return this.value.toString();
    }
}
