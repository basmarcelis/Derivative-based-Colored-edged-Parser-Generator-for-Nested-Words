/**
 * Stackaction defines which action needs to be done to the stack in a transition
 */
public class Stackaction {

    private final TType ttype;
    private final State state;
    private final String nestsymbol;

    /**
     * Constructs a Stackaction
     * @param ttype Transition type (Call (Push), Return (Pop), or Internal (no action))
     * @param state Previous State to be kept on the stack
     * @param nestsymbol open-nesting symbol
     */
    public Stackaction(TType ttype, State state, String nestsymbol) {
        this.ttype = ttype;
        this.state = state;
        this.nestsymbol = nestsymbol;
    }

    public TType getTtype() {
        return ttype;
    }

    public State getState() {
        return state;
    }

    public String getNestsymbol() { return nestsymbol; }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Stackaction
                && ((Stackaction) o).ttype == ttype
                && ((((Stackaction) o).state == null && state == null) || ((Stackaction) o).state.equals(state))
                && ((((Stackaction) o).nestsymbol == null && nestsymbol == null) || ((Stackaction) o).nestsymbol.equals(nestsymbol))
        );
    }

    @Override
    public String toString() {
        if (ttype == TType.INTERNAL) {
            return "-";
        } else {
            return String.format("%s [%s, %s]", (ttype == TType.CALL) ? "PUSH" : "POP", state.toString(), nestsymbol);
        }
    }

}
