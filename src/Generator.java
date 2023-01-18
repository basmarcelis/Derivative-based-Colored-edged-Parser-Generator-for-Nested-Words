import java.util.*;

/**
 * Generator of a PDA for VPGs
 * Algorithm is based on the pseudo-code algorithm of Jia et al. on page 7.
 * (Jia, X., Kumar, A. (2021). A Derivative-Based Parser Generator for Visibly Pushdown Grammars. https://doi.org/10.1145/3485528)
 *
 * Generates:
 *      - A: States of the automata
 *      - T: Transitions of the automata
 *      - S0: Start State
 */
public class Generator {

    private final Map<NonTerminal, Set<List<Token>>> rules;
    private final Map<String, String> opentoclose;

    private final State s0;
    private TreeSet<State> A;
    private TreeSet<Transition> T;

    /**
     * Constructor of the class and automatically generates the automata
     * @param grammar
     */
    public Generator(String grammar) {

        GrammarTokenizer gt = new GrammarTokenizer();
        rules = gt.tokenize(grammar);
        NonTerminal start = gt.getStart();
        Set<String> el = gt.getEl();
        Set<String> ec = gt.getEc();
        Set<String> er = gt.getEr();
        opentoclose = gt.getOpentoclose();

        for (NonTerminal nt : rules.keySet()) {
            if (hasEpsilon(nt)) {
                nt.setNullable();
            }
        }

        s0 = new State();
        s0.addPair(start, start);

        TreeSet<State> n = new TreeSet<>();
        n.add(s0);

        A = new TreeSet<>();
        A.add(s0);

        T = new TreeSet<>();

        //The lines correspond to the lines of the pseudo-code algorithm of Jia et al. on page 7.
        while (!n.isEmpty()) {

            //Line 7 + 8
            TreeSet<State> Nprime = new TreeSet<>();
            for (State state : n) {
                for (String i : el) {
                    Transition tr = deriveInternal(i, state);
                    if (tr != null) {
                        Nprime.add(tr.getDestination());
                        T.add(tr);
                    }
                }
                for (String i : ec) {
                    Transition tr = deriveCall(i, state);
                    if (tr != null) {
                        Nprime.add(tr.getDestination());
                        T.add(tr);
                    }
                }
            }

            //Line 9
            Set<Stackaction> R = new HashSet<>();
            for (State state : A) {
                for (String open : ec) {
                    R.add(new Stackaction(TType.CALL, state, open));
                }
            }

            //Line 10 + 11
            TreeSet<State> Nr = new TreeSet<>();
            for (State state : A) {
                for (String i : er) {
                    for (Stackaction r : R) {
                        Transition transition = deriveReturn(i, state, r);
                        if (transition != null) {
                            Nr.add(transition.getDestination());
                            T.add(transition);
                        }
                    }
                }
            }

            //Line 12
            n = new TreeSet<>();
            n.addAll(Nr);
            n.addAll(Nprime);
            n.removeAll(A);

            //Line 13
            A.addAll(n);
        }

        for (State a : A) {
            if (isFinalState(a)) {
                a.setFinal();
            }
        }



    }

    /**
     * Check all possible new states from the current state for a specific character c,
     * where c is an element of the internal alphabet.
     * @param c next character
     * @param current current State
     * @return Transition from current to the destination state with an internal stackaction (do nothing). If such a transition does not exists, returns null.
     */
    public Transition deriveInternal(String c, State current) {
        State state = new State();
        Set<ParseTreeEdge> edges = new TreeSet<>();
        for (Pair<NonTerminal, NonTerminal> pair : current.getPairs()) {
            for (List<Token> rightside : rules.get(pair.getRight())) {
                if (rightside.get(0).getToken() == TokenType.String
                    && rightside.get(0).getValue().equals(c)
                    && rightside.get(1) instanceof NonTerminal
                ) {
                    state.addPair(pair.getLeft(), (NonTerminal) rightside.get(1));
                    edges.add(new ParseTreeEdge(pair.getRight(), c, (NonTerminal) rightside.get(1)));
                }
            }
        }
        if (!state.getPairs().isEmpty()) {
            return new Transition(current, checkState(state), c, new Stackaction(TType.INTERNAL, null, null), edges);
        } else {
            return null;
        }
    }

    /**
     * Check all possible new states from the current state for a specific character c,
     * where c is an element of the call alphabet.
     * @param c next character
     * @param current current State
     * @return Transition from current to the destination state with a call stackaction (push). If such a transition does not exists, returns null.
     */
    public Transition deriveCall(String c, State current) {
        State state = new State();
        Set<ParseTreeEdge> edges = new TreeSet<>();
        for (Pair<NonTerminal, NonTerminal> pair : current.getPairs()) {
            for (List<Token> rightside : rules.get(pair.getRight())) {
                if (rightside.get(0).getToken() == TokenType.NestOpen
                        && rightside.get(0).getValue().equals(c)
                        && rightside.get(1) instanceof NonTerminal) {
                    state.addPair((NonTerminal) rightside.get(1), (NonTerminal) rightside.get(1));
                    edges.add(new ParseTreeEdge(pair.getRight(), c, (NonTerminal) rightside.get(1)));
                }
            }
        }
        if (!state.getPairs().isEmpty()) {
            return new Transition(current, checkState(state), c, new Stackaction(TType.CALL, current, c), edges);
        } else {
            return null;
        }
    }

    /**
     * Check all possible new states from the current state for a specific character c,
     * where c is an element of the return alphabet.
     * @param c next character
     * @param current current State
     * @param sa stackaction currently on top of the stack
     * @return Transition from current to the destination state with a return stackaction (pop). If such a transition does not exists, returns null;
     */
    public Transition deriveReturn(String c, State current, Stackaction sa) {
        State state = new State();
        Set<ParseTreeEdge> edges = new TreeSet<>();
        for (Pair<NonTerminal, NonTerminal> pair1 : sa.getState().getPairs()) {
            for (Pair<NonTerminal, NonTerminal> pair2 : current.getPairs()) {
               if (pair2.getRight().nullable()) {
                   for (List<Token> rule : rules.get(pair1.getRight())) {
                       if (rule.get(0).getValue() != null
                               && rule.get(0).getValue().equals(sa.getNestsymbol())
                               && rule.get(1).equals(pair2.getLeft())
                               && rule.get(2).getValue().equals(opentoclose.get(sa.getNestsymbol()))
                               && rule.get(3) instanceof NonTerminal
                       ) {
                           state.addPair(pair1.getLeft(), (NonTerminal) rule.get(3));
                           edges.add(new ParseTreeEdge(new Pair<>(pair1.getRight(), (NonTerminal) rule.get(1)), c, (NonTerminal) rule.get(3)));
                       }
                   }
               }
            }
        }
        if (!state.getPairs().isEmpty() && opentoclose.get(sa.getNestsymbol()).equals(c)) {
            return new Transition(current, checkState(state), c, new Stackaction(TType.RETURN, sa.getState(), sa.getNestsymbol()), edges);
        } else {
            return null;
        }
    }

    /**
     * Checks if an equal State already exists. If so, it will return the existing State. If not, it will return the parameter State.
     * @param s1 state
     * @return equal state
     */
    public State checkState(State s1) {
        if (s1.equals(s0)) {
            return s0;
        }
        for (Transition t : T) {
            if (s1.equals(t.getOrigin())) {
                return t.getOrigin();
            }
            if (s1.equals(t.getDestination())) {
                return t.getDestination();
            }
        }
        return s1;
    }

    /**
     * Checks if a Non Terminal has a rule to epsilon
     * @param nt
     * @return boolean
     */
    public boolean hasEpsilon(NonTerminal nt) {
        for (List<Token> rule : rules.get(nt)) {
            if (rule.get(0).getToken() == TokenType.Epsilon) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether a state should be final.
     * A state is final when the context (left) is the same as the start state and the current NonTerminal (right) can derive epsilon.
     * @param state state
     * @return if state is a final state
     */
    public boolean isFinalState(State state) {
        for (Pair<NonTerminal, NonTerminal> pair : state.getPairs()) {
            for (Pair<NonTerminal, NonTerminal> startpair : s0.getPairs()) {
                if (startpair.getLeft().equals(pair.getLeft())
                && hasEpsilon(pair.getRight())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return All states of the automata
     */
    public TreeSet<State> getA() {
        return A;
    }

    /**
     * @return All transitions of the automata
     */
    public TreeSet<Transition> getT() {
        return T;
    }

    /**
     * @return Start State of the automata
     */
    public State getS0() {
        return s0;
    }

    /**
     * @return Map from open nesting symbol to closing nesting symbol
     */
    public Map<String, String> getOpentoclose() {
        return opentoclose;
    }

    /**
     * @return Map with rules
     */
    public Map<NonTerminal, Set<List<Token>>> getRules() {
        return rules;
    }
}