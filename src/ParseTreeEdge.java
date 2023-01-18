import java.util.List;

/**
 * ParseTreeEdge contains information on an origin NonTerminal and a destination NonTerminal, with a symbol which is consumed in the mean time.
 * Every ParseTreeEdge represents an applicable rule.
 * Origin is a NonTerm for normal rules and a Pair of NonTerminal context and current NonTerminal if corresponding rule is a return rule.
 */
public class ParseTreeEdge implements Comparable<ParseTreeEdge> {

    //NonTerm for normal rules, Pair of NonTerminal context and current rule when a nesting is closed
    private Object o;
    private String c;
    private NonTerminal d;
    private boolean isColored;

    public ParseTreeEdge(NonTerminal o, String c, NonTerminal d) {
        this.o = o;
        this.c = c;
        this.d = d;
        isColored = false;
    }

    public ParseTreeEdge(Pair<NonTerminal, NonTerminal> o, String c, NonTerminal d) {
        this.o = o;
        this.c = c;
        this.d = d;
        isColored = false;
    }

    public ParseTreeEdge(ParseTreeEdge e) {
        this.o = e.getO();
        this.c = e.getC();
        this.d = e.getD();
        isColored = false;
    }

    public Object getO() {
        return o;
    }

    public String getC() {
        return c;
    }

    public NonTerminal getD() {
        return d;
    }

    public boolean isColored() {
        return isColored;
    }

    public void setColored() {
        isColored = true;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", o.toString(), c, d.toString());
    }

    @Override
    public int compareTo(ParseTreeEdge o) {
        if (this.o.equals(o.o) && this.c.equals(o.c) && this.d.equals(o.d)) {
            return 0;
        }
        return -1; //TODO
    }
}