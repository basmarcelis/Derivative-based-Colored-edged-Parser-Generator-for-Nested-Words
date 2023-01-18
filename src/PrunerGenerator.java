import java.util.*;

public class PrunerGenerator {

    private Set<ParseTreeEdge> Mpln;
    private Set<ParseTreeEdge> Mcall;
    private Set<ParseTreeEdge> Mret;
    private final Map<NonTerminal, Set<List<Token>>> rules = new HashMap<>(); //TODO

    public PrunerGenerator() {

        Mpln = new TreeSet<>();
        Mcall = new TreeSet<>();
        Mret = new TreeSet<>();

        for (NonTerminal left : rules.keySet()) {
            for (List<Token> right : rules.get(left)) {
                if (right.size() == 2) {
                    Mpln.add(new ParseTreeEdge(left, right.get(0).getValue(), (NonTerminal) right.get(1)));
                } else if (right.size() > 3) {
                    Mcall.add(new ParseTreeEdge(left, right.get(0).getValue(), (NonTerminal) right.get(1)));
                    Mret.add(new ParseTreeEdge(new Pair<>(left, (NonTerminal) right.get(1)), right.get(2).getValue(), (NonTerminal) right.get(3)));
                }
            }
        }

    }

    public Set<ParseTreeEdge> gEps(Set<ParseTreeEdge> m) {
        Set<ParseTreeEdge> res = new TreeSet<>();
        for (ParseTreeEdge edge : m) {
            res.add(edge);
        }
        return res;
    }

    public Set<ParseTreeEdge> g(Set<ParseTreeEdge> m1, Set<ParseTreeEdge> m2) {
        Set<ParseTreeEdge> prunedm1 = new TreeSet<>();
        for (ParseTreeEdge edge1 : m1) {
            for (ParseTreeEdge edge2 : m2) {
                if ((Mpln.contains(edge1) && Mpln.contains(edge2))
                    || (Mpln.contains(edge1) && Mcall.contains(edge2))
                    || (Mret.contains(edge1) && Mpln.contains(edge2))
                    || (Mret.contains(edge1) && Mcall.contains(edge2))
                ) {
                    if (edge1.getD().equals(edge2.getO())) {
                        prunedm1.add(edge1);
                    }
                } else if ((Mpln.contains(edge1) && Mret.contains(edge2))
                        || (Mret.contains(edge1) && Mret.contains(edge2))
                ) {

                }
            }
        }
        return null; //TODO
    }

}
