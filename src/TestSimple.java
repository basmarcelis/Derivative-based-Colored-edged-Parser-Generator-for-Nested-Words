public class TestSimple {

    public static void main(String[] args) {

        State s1 = new State(new NonTerminal("A"), new NonTerminal("E"));
        State s2 = new State(new NonTerminal("A"), new NonTerminal("E"));
        State s3 = new State(new NonTerminal("L"), new NonTerminal("L"));
        State s4 = new State(new NonTerminal("L"), new NonTerminal("L"));

        s1.addPair(new NonTerminal("L"), new NonTerminal("L"));
        s2.addPair(new NonTerminal("L"), new NonTerminal("L"));

        System.out.println(s1.equals(s2));

    }

}
