import java.util.*;

public class Colorizer {

    private final Map<NonTerminal, Set<List<Token>>> rules;
    private NonTerminal start;
    private final Map<String, Integer> colors;
    private Set<NonTerminal> traversed;

    private final TreeSet<Transition> T;
    private final Map<String, String> opentoclose;

    public Colorizer(Generator g) {
        this.rules = g.getRules();
        for (Pair<NonTerminal, NonTerminal> pair : g.getS0().getPairs()) {
            this.start = pair.getLeft();
        }
        this.opentoclose = g.getOpentoclose();
        this.T = g.getT();
        colors = new HashMap<>();

        determineColors();
    }

    public Map<String, Integer> getColors() {
        return colors;
    }

    /**
     * @return All colored edges for this grammar
     */
    public Set<ColoredEdge> getColoredEdges() {
        return colorEdges();
    }

    /**
     * Determines the color for every nesting symbol.
     * The color is represented by the int value of its depth in the grammar, which is determined using the recurse function.
     * @return Map which maps return symbol to color
     */
    private Map<String, Integer> determineColors() {
        traversed = new HashSet<>();
        recurse(start, 0);
        return colors;
    }

    /**
     * Recursively dives deeper in the grammar. Determines the depth for every closing symbol
     * @param current current NonTerminal
     * @param depth current depth
     */
    private void recurse(NonTerminal current, int depth) {
        if (!traversed.add(current)) return;
        for (List<Token> rule : rules.get(current)) {
            if (rule.size() > 2) {
                colors.put(rule.get(0).getValue(), depth);
                colors.put(rule.get(2).getValue(), depth);
            }
            for (Token t : rule) {
                if (t instanceof NonTerminal) {
                    recurse((NonTerminal) t, depth + 1);
                }
            }
        }
    }

    /**
     * Creates all colored edges
     * @return Set of all colored edges
     */
    private Set<ColoredEdge> colorEdges() {
        Set<ColoredEdge> coloredEdges = new TreeSet<>();

        Set<List<Transition>> traces = findClosingSequences();

        if (traces.size() == 0) return coloredEdges;
        for (List<Transition> sequence : traces){


            List<Set<ParseTreeEdge>> l = new ArrayList<>();
            for (Transition t : sequence) {
                l.add(t.getParseTreeEdges());
            }

            for (int i = 0; i < sequence.size() - 1; i++) {
                Transition originT = sequence.get(i);
                for (int j = i+1; j < sequence.size(); j++) {
                    coloredEdges.add(new ColoredEdge(originT.getOrigin(), sequence.get(j).getDestination(), sequence.get(j).getC(), sequence.get(j).getStackAction(), l.subList(i, j+1)));
                }
            }
        }
        return coloredEdges;
    }

    /**
     * Finds a sequence of following transition with return symbols. Returns all sequences with size > 1.
     * Uses the recursive trace function to trace a sequence.
     * @return Sequences of following return transitions
     */
    private Set<List<Transition>> findClosingSequences() {
        Set<Transition> copyT = new TreeSet<>(T);
        copyT.removeIf(transition -> !opentoclose.containsValue(transition.getC()));

        Set<List<Transition>> traces = new HashSet<>();
        for (Transition t : copyT) {
            List<Transition> l = new ArrayList<>();
            trace(t, l, copyT);
            if (l.size() > 1) traces.add(l);
        }
        return traces;
    }

    /**
     * Follows the trace for a specific Transition sequence
     * @param t previous Transition in sequence
     * @param l sequence
     * @param copyT all return Transitions
     */
    private void trace(Transition t, List<Transition> l, Set<Transition> copyT) {
        l.add(t);
        for (Transition t2 : copyT) {
            if (t.getDestination().equals(t2.getOrigin()) && colors.get(t.getC()) > colors.get(t2.getC())) {
                trace(t2, l, copyT);
            }
        }
    }
}
