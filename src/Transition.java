import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines an automata transition
 * Also includes Colored Edge
 */
public class Transition implements Comparable<Transition> {

    private final State origin;
    private final State destination;
    private final String c;
    private final Stackaction sa;
    private final Set<ParseTreeEdge> edges;

    /**
     * Constructor of a Transition
     * @param o Origin State
     * @param d Destination State
     * @param c String/Char to be consumed
     * @param sa Stackaction if nesting is opened or closed
     * @param edges contains a set of triples corresponding to the possible rule(s) corresponding to the transition
     */
    public Transition(State o, State d, String c, Stackaction sa, Set<ParseTreeEdge> edges) {
        this.origin = o;
        this.destination = d;
        this.c = c;
        this.sa = sa;
        this.edges = edges;
    }

    public State getOrigin() {
        return origin;
    }

    public State getDestination() {
        return destination;
    }

    public String getC() {
        return c;
    }

    public Stackaction getStackAction() {
        return sa;
    }

    public Set<ParseTreeEdge> getParseTreeEdges() {
        return edges;
    }

    @Override
    public int compareTo(Transition o) {
        if (origin.equals(o.origin)
        && destination.equals(o.destination)
        && c.equals(o.c)
        && sa.equals(o.sa)) {
            return 0;
        } else {
            if (c.compareTo(o.c) != 0) return c.compareTo(o.c);
            if (origin.compareTo(o.origin) != 0) return origin.compareTo(o.origin);
            if (destination.compareTo(o.destination) != 0) return destination.compareTo(o.destination);
            return -1;
        }
    }

    @Override
    public String toString() {
        return String.format("\nFor symbol: %s\n" +
                "From: %s,\n" +
                "To: %s\n" +
                "StackAction: %s\n" +
                "Rule: %s\n",
                c, origin.toString(), destination.toString(), sa == null ? null : sa.toString(), edges == null ? null : edges.toString());
    }
}

/**
 * Special Transition, created by the colorized. Bypasses multiple closing transitions in one transition.
 * Stores all PTEs of all skipped closing transitions, so they can be inserted in the parse forest.
 * PTEs in Colored Edges are tagged so that the symbols they contain are not included in the Parse Tree.
 */
class ColoredEdge extends Transition {

    private final List<Set<ParseTreeEdge>> edges;

    public ColoredEdge(State o, State d, String c, Stackaction sa, List<Set<ParseTreeEdge>> edges) {
        super(o, d, c, sa, null);

        //Copies all the ParseTreeEdges, so that other ParseTreeEdges will not be tagged as colored
        List<Set<ParseTreeEdge>> copylist = new ArrayList<>();
//        for (Set<ParseTreeEdge> s : edges) {
        for (int i = 0; i < edges.size(); i++) {
            Set<ParseTreeEdge> s = edges.get(i);
            Set<ParseTreeEdge> copyset = new HashSet<>();
            for (ParseTreeEdge e : s) {
                ParseTreeEdge copyedge = new ParseTreeEdge(e);
                if (i != edges.size() - 1) copyedge.setColored();
                copyset.add(copyedge);
            }
            copylist.add(copyset);
        }
        this.edges = copylist;
    }

    public List<Set<ParseTreeEdge>> getParseTreeEdgesList() {
        return edges;
    }
}
