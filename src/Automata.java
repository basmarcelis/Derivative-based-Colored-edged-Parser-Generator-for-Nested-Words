import java.util.*;

public class Automata {

    private final Map<State, Set<Transition>> map;
    private final Map<State, Set<ColoredEdge>> coloredmap;
    private final State start;
    private Stack<Pair<State, String>> stack;
    private final Map<String, String> opentoclose;
    private final Map<String, Integer> colors;

    /**
     * Create an Automata based on a Set of States, a Set of Transitions and a Start State
     * @param A States
     * @param T Transitions
     * @param start Start State
     */
    public Automata(Set<State> A, Set<Transition> T, State start, Map<String, String> opentoclose, Map<String, Integer> colors, Set<ColoredEdge> coloredT) {
        this.start = start;
        this.opentoclose = opentoclose;
        this.colors = colors;
        map = new TreeMap<>();
        for (Transition t : T) {
            if (!map.containsKey(t.getOrigin())) {
                map.put(t.getOrigin(), new HashSet<>());
            }
            map.get(t.getOrigin()).add(t);
        }
        coloredmap = new TreeMap<>();
        for (ColoredEdge t : coloredT) {
            if (!coloredmap.containsKey(t.getOrigin())) {
                coloredmap.put(t.getOrigin(), new HashSet<>());
            }
            coloredmap.get(t.getOrigin()).add(t);
        }

    }

    /**
     * Parse an input in accordance with the grammar. Returns a set of possible ASTs if recognized.
     * @param input String input
     * @return null if not recognized, otherwise a set of possible ASTs
     */
    public Set<AST> parse(String input) {
        stack = new Stack<>();
        List<Set<ParseTreeEdge>> l = new ArrayList<>();
        if (!recognize(start, input, l)) {
            return null;
        }
        List<Set<ParseTreeEdge>> prunedParseForest = prune(l);
        Set<AST> res = new HashSet<>();
        if (input.equals("") || prunedParseForest.isEmpty()) {
            return res;
        }
        for (ParseTreeEdge start : prunedParseForest.get(0)) {
            for (List<ParseTreeEdge> trace : puretrace(start, l)) {
                res.add(buildAST(trace));
            }
        }
        return res;
    }

    /**
     * Build an AST for a given trace
     * @param trace trace to be converted to AST
     * @return AST corresponding to the trace
     */
    public AST buildAST(List<ParseTreeEdge> trace) {
        Stack<Pair<NonTerminal, NonTerminal>> s = new Stack();
        Stack<AST> sreturn = new Stack();

        AST res = null;
        AST currentast = null;
        for (int i = 0; i < trace.size(); i++) {
            ParseTreeEdge edge = trace.get(i);

            if (opentoclose.containsValue(edge.getC()) && !s.isEmpty() && s.peek().equals(edge.getO())) {
                s.pop();
                currentast = sreturn.pop();
                if (!edge.isColored()) currentast.addChild(new AST(edge.getC()));
                continue;
            }

            AST ast = new AST(edge.getO().toString());
            if (!edge.isColored()) ast.addChild(new AST(edge.getC()));

            if (opentoclose.containsKey(edge.getC())) {
                s.push(new Pair<>((NonTerminal) edge.getO(), edge.getD()));
                sreturn.push(ast);
            }

            if (res == null) {
                res = currentast = ast;
            } else {
                currentast.addChild(ast);
                currentast = ast;
            }
        }
        return res;
    }

    /**
     * Starting function of puretrace. Extracts a trace based on a given starting point PTE.
     * @param start starting PTE
     * @param l Pruned Parse Forest
     * @return all valid traces from the given starting point
     */
    public Set<List<ParseTreeEdge>> puretrace(ParseTreeEdge start, List<Set<ParseTreeEdge>> l) {
        Stack<Pair<NonTerminal, NonTerminal>> s = new Stack();
        List<Set<ParseTreeEdge>> remove = new ArrayList<>();
        return puretrace(start, l, s, 1);
    }

    /**
     * Recursive definition of puretrace. Extracts a trace by following valid PTE transitions.
     * @param start current PTE
     * @param l Pruned Parse Forest
     * @param s Stack to handle nesting
     * @param i next position in the Parse Forest
     * @return remaining trace sequences
     */
    private Set<List<ParseTreeEdge>> puretrace(ParseTreeEdge start, List<Set<ParseTreeEdge>> l, Stack<Pair<NonTerminal, NonTerminal>> s, int i) {
        Set<List<ParseTreeEdge>> res = new HashSet<>();
        if (i == l.size()) {
            ArrayList<ParseTreeEdge> list = new ArrayList<>();
            list.add(start);
            res.add(list);
            return res;
        }
        if (opentoclose.containsKey(start.getC())) {
            s.push(new Pair<>((NonTerminal) start.getO(), start.getD()));
        }
        for (ParseTreeEdge edge : l.get(i)) {
            if (start.getD().equals(edge.getO())) {
                for (List<ParseTreeEdge> child : puretrace(edge, l, s, i+1)) {
                    child.add(0, start);
                    res.add(child);
                }
            } else if (start.getD().nullable() && edge.getO() instanceof Pair
                    && !s.isEmpty() && s.peek().equals(edge.getO())
            ) {
                s.pop();
                for (List<ParseTreeEdge> child : puretrace(edge, l, s, i+1)) {
                    child.add(0, start);
                    res.add(child);
                }
            }
        }
        return res;
    }

    /**
     * Start function of prune.
     * @param l Parse Forest
     * @return Pruned Parse Forest
     */
    public List<Set<ParseTreeEdge>> prune(List<Set<ParseTreeEdge>> l) {
        if (l.isEmpty()) {
            return null;
        } else if (l.size() == 1) {
            return l;
        }
        //Delete all non-nullable ParseTreeEdges in the last set of the Parse Forest
        //Additionally, push the prunestack if the last set contains a nesting return
        Stack<ParseTreeEdge> prunestack = new Stack<>();
        for (ParseTreeEdge edge : l.get(l.size() - 1)) {
            if (!edge.getD().nullable()) {
                l.get(l.size() - 1).remove(edge);
            } else if (edge.getO() instanceof Pair && opentoclose.containsValue(edge.getC())) {
                prunestack.push(edge);
            }
        }

        prune(l, l.size()-2, prunestack);
        return l;
    }

    /**
     * Recursive prune function. Compares all edges of a set to an already pruned set and prunes if necessary.
     * @param l Parse Forest
     * @param i index on position in the Parse Forest
     * @param prunestack internal Stack to relate nesting calls to already pruned nesting returns
     */
    private void prune(List<Set<ParseTreeEdge>> l, int i, Stack<ParseTreeEdge> prunestack) {
        if (i == 0) {
            return;
        }

        //compare next set to the already correct set
        Set<ParseTreeEdge> correct = l.get(i+1);
        Set<ParseTreeEdge> check = l.get(i);
        check.removeIf(edge -> !checkEdge(edge, correct, prunestack));

        prune(l, i-1, prunestack);
    }

    /**
     * Checks if a given edge can be linked to one of the edges of an already pruned set
     * @param edge edge to be checked
     * @param set already pruned set of edges
     * @param prunestack internal Stack to relate nesting calls to already pruned nesting returns
     * @return boolean, if the edge is correct or should be pruned
     */
    private boolean checkEdge(ParseTreeEdge edge, Set<ParseTreeEdge> set, Stack<ParseTreeEdge> prunestack) {

        //Check if edge contains a call symbol
        if (opentoclose.containsKey(edge.getC())) {

            //If symbol is a call symbol but the stack is empty or the closing symbol on the stack did not relate
            if (prunestack.isEmpty() || !prunestack.peek().getC().equals(opentoclose.get(edge.getC()))) {
                return false;
            }

            //Determines if the opening of the nesting is related to an already pruned return symbol by peeking the stack
            Pair<NonTerminal, NonTerminal> pair = (Pair<NonTerminal, NonTerminal>) prunestack.peek().getO();
            if (pair.getLeft().equals(edge.getO()) && pair.getRight().equals(edge.getD())) {
                prunestack.pop();

            //If the nesting call does not correspond to the nesting return on the stack
            } else {
                return false;
            }

        //If edge contains a return symbol, push edge to the Stack
        } else if (edge.getO() instanceof Pair && opentoclose.containsValue(edge.getC())) {
            prunestack.push(edge);
        }

        //Check if the current edge links to an edge in the previously pruned set
        for (ParseTreeEdge edge2 : set) {

            //If the destination of the edge exists as an origin in the already pruned set
            if (edge2.getO() instanceof NonTerminal && edge.getD().equals(edge2.getO())) {
                return true;

            //If trace leaves current context (for which it needs to be nullable)
            //and continues at the end of a nesting rule (for which the destination needs to be a Pair)
            } else if (edge2.getO() instanceof Pair && edge.getD().nullable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Public method on recognizing an input String in accordance with the grammar automata
     * @param input input String
     * @return if input is recognized by the grammar automata
     */
    public boolean recognize(String input) {
        stack = new Stack<>();
        return recognize(start, input);
    }

    /**
     * Helper method for the public recognize method. Recursively handles the first character of the input.
     * If the String is empty the automata traversal is done. If then the stack is empty and is in a final state, return true.
     * @param state current State
     * @param input (remaining) input String
     * @return if input is recognized by the grammar automata
     */
    private boolean recognize(State state, String input) {
        if (input.length() == 0) {
            return (stack.isEmpty() && state.isFinal());
        } else if (!map.containsKey(state)) {
            return false;
        }
        for (Transition t : map.get(state)) {
            if (t.getC().equals(input.charAt(0) + "") && doStackAction(t.getStackAction())) {
                return recognize(t.getDestination(), input.substring(1));
            }
        }
        if (!stack.isEmpty() && coloredmap.containsKey(state)) {
            for (ColoredEdge t : coloredmap.get(state)) {
                if (t.getC().equals(input.charAt(0) + "")) {
                    while (colors.get(stack.peek().getRight()) > colors.get(t.getC())) {
                        stack.pop();
                    }
                    if (colors.get(stack.peek().getRight()).equals(colors.get(t.getC())) && doStackAction(t.getStackAction())) {
                        return recognize(t.getDestination(), input.substring(1));
                    }
                }
            }
        }
        return false;
    }

    /**
     * Same method as 2-parameter recognizer. However, this recognizer keeps track of the rules of the traversed transition for a parser to use.
     * @param state current State
     * @param input (remaining) input String
     * @param l List of Sets of ParseTreeEdges for every possible rule for every transition
     * @return
     */
    private boolean recognize(State state, String input, List<Set<ParseTreeEdge>> l) {
        if (input.length() == 0) {
            return (stack.isEmpty() && state.isFinal());
        }
        for (Transition t : map.get(state)) {
            if (t.getC().equals(input.charAt(0) + "") && doStackAction(t.getStackAction())) {
                l.add(new HashSet<>(t.getParseTreeEdges()));
                return recognize(t.getDestination(), input.substring(1), l);
            }
        }
        if (!stack.isEmpty() && coloredmap.containsKey(state)) {
            for (ColoredEdge t : coloredmap.get(state)) {
                if (t.getC().equals(input.charAt(0) + "")) {
                    while (colors.get(stack.peek().getRight()) > colors.get(t.getC())) {
                        stack.pop();
                    }
                    if (colors.get(stack.peek().getRight()).equals(colors.get(t.getC())) && doStackAction(t.getStackAction())) {
                        for (Set<ParseTreeEdge> s : t.getParseTreeEdgesList()) {
                            l.add(new HashSet<>(s));
                        }
                        return recognize(t.getDestination(), input.substring(1));
                    }
                }
            }
        }
        return false;
    }

    /**
     * Try to do the given Stackaction. If possible, return true, if not, return false.
     * @param sa Stackaction to be executed
     * @return success of execution
     */
    private boolean doStackAction(Stackaction sa) {
        switch (sa.getTtype()) {
            case INTERNAL:
                return true;
            case CALL:
                stack.push(new Pair<>(sa.getState(), sa.getNestsymbol()));
                return true;
            case RETURN:
                if (!stack.isEmpty() && sa.getState().equals(stack.peek().getLeft())
                && sa.getNestsymbol().equals(stack.peek().getRight())) {
                    stack.pop();
                    return true;
                }
                break;
        }
        return false;
    }








    //----------------------------------------------------------------------------------------------------------------
    // OLD FUNCTIONS => NOT USED ANYMORE

    /** THIS FUNCTION IS NOT USED ANYMORE. => Tracing and building the AST is seperated
     * Attempts to parse the input String in accordance with the grammar
     * @param input String input
     * @return Set of all possible ASTs
     */
    public Set<AST> oldparse(String input) {
        stack = new Stack<>();
        List<Set<ParseTreeEdge>> l = new ArrayList<>();
        if (!recognize(start, input, l)) {
            return null;
        }
        List<Set<ParseTreeEdge>> prunedParseForest = prune(l);
        Set<AST> res = new HashSet<>();
        if (input.equals("") || prunedParseForest.isEmpty()) {
            return res;
        }
        for (ParseTreeEdge start : prunedParseForest.get(0)) {
            res.addAll(trace(start, l));
        }
        return res;
    }

    /** THIS FUNCTION IS NOT USED ANYMORE. => Tracing and building the AST is seperated
     * Start function of trace. Follows a Trace and returns the corresponding AST.
     * @param start start of the trace
     * @param l Pruned Parse Forest
     * @return Set of ASTs corresponding to the Trace
     */
    public Set<AST> trace(ParseTreeEdge start, List<Set<ParseTreeEdge>> l) {
        Stack<Pair<NonTerminal, NonTerminal>> s = new Stack();
        Stack<AST> sreturn = new Stack<>();
        List<Set<ParseTreeEdge>> remove = new ArrayList<>();
        return trace(start, l, s, sreturn, 1);
    }

    /**
     * THIS FUNCTION IS NOT USED ANYMORE. => Tracing and building the AST is seperated
     * Recursive function of trace. Follows a Trace while building the corresponding AST.
     * Always starts at i = 1, as @param start already represent the ParseTreeEdge from l[0].
     * @param start start of the trace
     * @param l Pruned Parse Forest
     * @param s Stack to check nesting
     * @param i index in the Parse Forest
     * @return Set of AST corresponding to the Trace
     */
    private Set<AST> trace(ParseTreeEdge start, List<Set<ParseTreeEdge>> l, Stack<Pair<NonTerminal, NonTerminal>> s, Stack<AST> sreturn, int i) {

        Set<AST> res = new HashSet<>();

        if (i == l.size()) {
            res.add(new AST(start.getC()));
            return res;
        }

        if (opentoclose.containsKey(start.getC())) {
            s.push(new Pair<>((NonTerminal) start.getO(), start.getD()));
        }

        for (ParseTreeEdge edge : l.get(i)) {
            if (start.getD().equals(edge.getO())) {
                for (AST child : trace(edge, l, s, sreturn, i+1)) {
                    AST ast = new AST(start.getO().toString());
                    ast.addChild(new AST(start.getC()));
                    ast.addChild(child);
                    res.add(ast);
                }

            } else if (start.getD().nullable() && edge.getO() instanceof Pair
                    && !s.isEmpty() && s.peek().equals(edge.getO())
            ) {
                s.pop();
                for (AST child : trace(edge, l, s, sreturn, i+1)) {
                    AST ast = new AST(start.getO().toString());
                    if (!start.isColored()) {
                        ast.addChild(new AST(start.getC()));
                    }
                    ast.addChild(child);
                    res.add(ast);
                }
            }
        }
        return res;
    }

    /**
     * THIS FUNCTION IS NOT USED
     * Test function for parsing of SPPFs. Function is not completed nor tested.
     * @param input
     * @return
     */
    public SPPF parseSPPF(String input) {
        stack = new Stack<>();
        List<Set<ParseTreeEdge>> l = new ArrayList<>();
        if (!recognize(start, input, l)) {
            return null;
        }
        List<Set<ParseTreeEdge>> prunedParseForest = prune(l);
        if (input.equals("") || prunedParseForest.isEmpty()) {
            return null;
        }
        TreeMap<SPPF, SPPF> existingSPPFs = new TreeMap<>();

        for (ParseTreeEdge start : prunedParseForest.get(0)) {
            SPPF sppf = new SPPF(start.getO().toString(), existingSPPFs);
            Stack<Pair<NonTerminal, NonTerminal>> s = new Stack<>();
            Stack<Pair<Integer, SPPF>> returnstack = new Stack<>();
            for (SPPF child : traceSPPF(start, l, s, returnstack, existingSPPFs, 1)) {
                List<SPPF> childlist = new ArrayList<>();
                childlist.add(new SPPF(start.getC(), existingSPPFs));
                childlist.add(child);
                sppf.addChildList(childlist);
            }
            return sppf;
        }
        return null;
    }

    /**
     * THIS FUNCTION IS NOT USED
     * Test function for creation of SPPFs during extraction. Function is not completed nor tested.
     * @param start
     * @param l
     * @param s
     * @param returnstack
     * @param existingSPPFs
     * @param i
     * @return
     */
    public Set<SPPF> traceSPPF(ParseTreeEdge start, List<Set<ParseTreeEdge>> l, Stack<Pair<NonTerminal, NonTerminal>> s, Stack<Pair<Integer, SPPF>> returnstack, TreeMap<SPPF, SPPF> existingSPPFs, int i) {
        Set<SPPF> res = new HashSet<>();

        if (i == l.size()) {
            res.add(new SPPF(start.getC(), existingSPPFs));
            return res;
        }

        for (ParseTreeEdge edge : l.get(i)) {
            if (start.getD().equals(edge.getO()) && opentoclose.containsKey(start.getC())) {
                s.push(new Pair<>((NonTerminal) start.getO(), start.getD()));

                SPPF sppf = new SPPF(start.getO().toString(), existingSPPFs);
                Pair<Integer, SPPF> returnvalue = null;
                for (SPPF child : traceSPPF(edge, l, s, returnstack, existingSPPFs, i+1)) {
                    if (returnvalue == null) returnvalue = returnstack.pop();
                    for (SPPF child2 : traceSPPF(edge, l, s, returnstack, existingSPPFs, returnvalue.getLeft())) {
                        List<SPPF> childlist = new ArrayList<>();
                        childlist.add(new SPPF(start.getC(), existingSPPFs));
                        childlist.add(child);
                        childlist.add(returnvalue.getRight());
                        childlist.add(child2);

                        sppf.addChildList(childlist);
                    }


                }
                res.add(sppf);



                //Regular transition
            } else if (start.getD().equals(edge.getO())) {
                SPPF sppf = new SPPF(start.getO().toString(), existingSPPFs);
                for (SPPF child : traceSPPF(edge, l, s, returnstack, existingSPPFs, i+1)) {
                    List<SPPF> childlist = new ArrayList<>();
                    childlist.add(new SPPF(start.getC(), existingSPPFs));
                    childlist.add(child);
                    sppf.addChildList(childlist);
                }
                res.add(sppf);

            } else if (start.getD().nullable() && edge.getO() instanceof Pair
                    && !s.isEmpty() && s.peek().equals(edge.getO())
            ) {
                s.pop();
                SPPF sppf = new SPPF(start.getO().toString(), existingSPPFs);
                if (!start.isColored()) {
                    List<SPPF> list = new ArrayList<>();
                    list.add(new SPPF(start.getC(), existingSPPFs));
                    sppf.addChildList(list);
                }
                returnstack.push(new Pair<>(i, sppf));

            }
        }
        return res;
    }

}
