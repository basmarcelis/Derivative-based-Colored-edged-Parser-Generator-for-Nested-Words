import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Defines an Automata State
 * Contains:
 *      - pairs: A list of pairs of two strings. These pairs represent the possible current contexts (left) and rules (right).
 *               It can be possible that different rules follow the same path, therefore pairs is a list which holds all possible
 *               pairs following this state.
 *      - isFinal: Keeps track of whether the State is a Final state
 */
public class State implements Comparable<State>{

    private final TreeSet<Pair<NonTerminal, NonTerminal>> pairs;
    private boolean isFinal;

    public State() {
        pairs = new TreeSet<>();
        isFinal = false;
    }

    public State(NonTerminal left, NonTerminal right) {
        pairs = new TreeSet<>();
        isFinal = false;
        addPair(left, right);
    }

    public void addPair(NonTerminal left, NonTerminal right) {
        pairs.add(new Pair<>(left, right));
    }

    public TreeSet<Pair<NonTerminal, NonTerminal>> getPairs() {
        return pairs;
    }

    public void setFinal() {
        isFinal = true;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean equals(Object o) {
        return (compareTo((State) o) == 0);
    }

    @Override
    public int compareTo(State o) {

        if (pairs.equals(o.getPairs())) {
            return 0;
        } else if (pairs.size() != o.getPairs().size()){
            return (pairs.size() < o.getPairs().size()) ? -1 : 1;
        } else {
            for (Pair<NonTerminal, NonTerminal> pair1 : pairs) {
                for (Pair<NonTerminal, NonTerminal> pair2 : o.getPairs()) {
                    if (!pair1.equals(pair2)) return pair1.compareTo(pair2);
                }
            }
            return -1;
        }
    }

    public int comparePair(Pair<NonTerminal, NonTerminal> own, Pair<NonTerminal, NonTerminal> other) {
        int res = own.getLeft().getValue().compareTo(other.getLeft().getValue());
        if (res == 0) {
            res = own.getRight().getValue().compareTo(other.getRight().getValue());
        }
        return res;
    }

    @Override
    public String toString() {
        return isFinal ? "FINAL" + pairs.toString() : pairs.toString();
    }
}
