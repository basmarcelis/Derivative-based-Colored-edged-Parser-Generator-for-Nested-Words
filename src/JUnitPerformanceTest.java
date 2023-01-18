import org.junit.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the performance for different metrics.
 * Every test prints all the coordinates to create a graph in LaTeX.
 * For these coordinates, the left integer represent the length/depth/possible derivations for a grammar,
 * the right represent the processing time in ms.
 *
 * The printed coordinates are based on a given number of sets,
 * and the average taken of the processing times for the same length/depth/possible derivations.
 */
public class JUnitPerformanceTest {

    @Test
    public void testLongInput() {

        Set<List<Pair<Integer, Integer>>> set = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            set.add(longInput());
        }
        List<Pair<Integer, Integer>> average = average(set);
//        System.out.println(coordinatestostring(average, variance(set, average)));
        System.out.println(coordinatestostring(average));
    }

    public List<Pair<Integer, Integer>> longInput() {

        String grammar = "S : \"a\" S\n" +
                "  | e;";

        Automata automata = getAutomata(grammar);
        List<Pair<Integer, Integer>> res = new ArrayList<>();

        //Stackoverflow at 1700
        for (int i = 0; i < 1700; i = i + 50) {
            res.add(new Pair<Integer, Integer>(i, timeParse(repeat("a", i), automata)));
        }

        return res;
    }

    @Test
    public void testDeepGrammar() {
        Set<List<Pair<Integer, Integer>>> set = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            set.add(deepGrammar());
        }
        List<Pair<Integer, Integer>> average = average(set);
        System.out.println(coordinatestostring(average, variance(set, average)));
    }

    public List<Pair<Integer, Integer>> deepGrammar() {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int length = 0; length < 1700; length = length + 50) {
            StringBuilder grammar = new StringBuilder();
            for (int i = 0; i < length; i++) {
                grammar.append("S").append(i).append(" : \"a\" S").append(i + 1).append(";\n");
            }
            grammar.append("S").append(length).append(" : e;");
            Automata automata = getAutomata(grammar.toString());
            res.add(new Pair<Integer, Integer>(length, timeParse(repeat("a", length), automata)));
        }
        return res;
    }

    @Test
    public void testDeepNesting() {
        Set<List<Pair<Integer, Integer>>> set = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            set.add(deepNesting());
        }
        List<Pair<Integer, Integer>> average = average(set);
//        System.out.println(coordinatestostring(average, variance(set, average)));
        System.out.println(coordinatestostring(average));
    }

    public List<Pair<Integer, Integer>> deepNesting() {
        StringBuilder grammar = new StringBuilder("RULE : e\n");
        StringBuilder left = new StringBuilder();
        StringBuilder right = new StringBuilder();
        for (int i = 97; i <= 122; i++) {
            grammar.append("  | [ \"").append(String.valueOf(Character.toChars(i))).append("\" RULE \"").append(String.valueOf(Character.toChars(i - 32))).append("\" ] RULE\n");
            left.insert(0, String.valueOf(Character.toChars(i)));
            right.append(String.valueOf(Character.toChars(i - 32)));
        }
        grammar.append(";");
        Automata automata = getAutomata(grammar.toString());
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        //Stackoverflow at 39, so maximum depth is 38*26 = 988 levels of nesting
        for (int i = 0; i < 39; i++) {
            String input = longnesting(left.toString(), right.toString(), i);
            res.add(new Pair<Integer, Integer>(i*26, timeParse(input, automata)));
        }
        return res;
    }

    @Test
    public void testCombined() {
        Set<List<Pair<Integer, Integer>>> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            set.add(combined());
        }
        List<Pair<Integer, Integer>> average = average(set);
//        System.out.println(coordinatestostring(average, variance(set, average)));
        System.out.println(coordinatestostring(average));
    }

    public List<Pair<Integer, Integer>> combined() {
        Map<Integer, Pair<String, String>> map = new HashMap<>();
        while (map.keySet().size() < 17) {
            Pair<String, String> pair = createCombinedGrammar(10, 1700);
            int grammarsize = pair.getLeft().toString().split("\n").length;
            map.put(grammarsize / 100, pair);
        }
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            Pair<String, String> pair = map.get(i);
            String grammar = pair.getLeft();
            String input = pair.getRight();
            Automata automata = getAutomata(grammar);
            res.add(new Pair<Integer, Integer>(i*100, timeParse(input, automata)));
//            System.out.println(i*100 + " => " + timeParse(input, automata));
        }
        return res;

    }

    public Pair<String, String> createCombinedGrammar(int minimum, int maximum) {
        int length = minimum / 10;

        StringBuilder grammar = new StringBuilder();
        StringBuilder input = new StringBuilder();
        Stack<Integer> nestinglocation = new Stack<>();
        Stack<String> closingsymbol = new Stack<>();
        for (int i = 0; i < length || !nestinglocation.isEmpty(); i++) {

            if (i > maximum) {
                return createCombinedGrammar(minimum, maximum);
            }

            int random = (nestinglocation.isEmpty()) ? (int) (Math.random() * 3) : (int) (Math.random() * 4);

            //Regular rule
            if (random == 0 || random == 1) {
                grammar.append("RULE").append(i).append(" : ").append("\"a\" RULE").append(i+1).append(";\n");

                if (i >= length - 1 && nestinglocation.isEmpty()) {
                    grammar.append("RULE").append(i+1).append(" : e;\n");
                }

                input.append("a");

                //Opening rule
            } else if (random == 2) {
                grammar.append("RULE").append(i).append(" : ").append("[ \"[\" RULE").append(i+1).append(" \"]\" ] RULE");
                nestinglocation.push(grammar.length());

                input.append("[");
                closingsymbol.push("]");

            } else if (random == 3) {
                grammar.append("RULE").append(i).append(" : e;\n");
                grammar.insert(nestinglocation.pop(), i+1 + " ;\n");

                if (i >= length - 1 && nestinglocation.isEmpty()) {
                    grammar.append("RULE").append(i+1).append(" : e;\n");
                }

                input.append(closingsymbol.pop());
            }
        }
        int grammarsize = grammar.toString().split("\n").length;
        if (grammarsize >= minimum && grammarsize <= maximum) {
            return new Pair<>(grammar.toString(), input.toString());
        } else {
            return createCombinedGrammar(minimum, maximum);
        }

    }

    @Test
    public void testLongAmbiguity() {
        Set<List<Pair<Integer, Integer>>> set = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            set.add(longAmbiguity());
        }
        List<Pair<Integer, Integer>> average = average(set);
//        System.out.println(coordinatestostring(average, variance(set, average)));
        System.out.println(coordinatestostring(average));
    }

    public List<Pair<Integer, Integer>> longAmbiguity() {

        String grammar ="S : \"a\" S\n" +
                "  | \"a\" A\n" +
                "  | e;\n" +

                "A : \"a\" S\n" +
                "  | \"a\" A\n" +
                "  | e;\n";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize("aaaaaaaaaaaaaaaaaaaaa"));

        //For every a the amount of possible parse trees doubles. 2^n is expected
        assertEquals(Math.pow(2, 5), automata.parse(repeat("a", 5)).size());

        List<Pair<Integer, Integer>> res = new ArrayList<>();

        //Out of Memory at 21
        for (int i = 1; i < 21; i++) {
            res.add(new Pair<Integer, Integer>((int) Math.pow(2, i), timeParse(repeat("a", i), automata)));
//            System.out.println("(" + (int) Math.pow(2, i) + "," + (timeParse(repeat("a", i), automata)) + ")");
        }
        return res;
    }

    @Test
    public void testBroadAmbiguity() {
        Set<List<Pair<Integer, Integer>>> set = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            set.add(broadAmbiguity());
        }
        List<Pair<Integer, Integer>> average = average(set);
//        System.out.println(coordinatestostring(average, variance(set, average)));
        System.out.println(coordinatestostring(average));
    }

    public List<Pair<Integer, Integer>> broadAmbiguity() {

        StringBuilder grammar = new StringBuilder(";\n");

        int width = 10;

        for (int i = 0; i < width; i++) {
            grammar.insert(0, "| \"a\" S" + i + "\n");
            grammar.append("S").append(i).append(" : \"a\" S;\n");
        }
        grammar.insert(0, "S : e\n");

        Automata automata = getAutomata(grammar.toString());

        assertTrue(automata.recognize(repeat("a", 50*2)));
//        assertEquals(Math.pow(width, 10), automata.parse(repeat("a", 10*2)).size());

        List<Pair<Integer, Integer>> res = new ArrayList<>();

        //Out of memory at 20^5
        //Out of memory at 10^7
        for (int i = 0; i < 7; i++) {
//            System.out.println("(" + (int) Math.pow(width, i) + "," + timeParse(repeat("aa", i), automata) + ")");
            res.add(new Pair<Integer, Integer>((int) Math.pow(width, i), timeParse(repeat("aa", i), automata)));
        }
        return res;
    }

    public List<Pair<Integer, Integer>> average(Set<List<Pair<Integer, Integer>>> s) {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (List<Pair<Integer, Integer>> l : s) {
//            System.out.println(l);
            for (int i = 0; i < l.size(); i++) {
                if (i >= res.size()) res.add(new Pair(l.get(i).getLeft(), 0));
                res.get(i).setRight(res.get(i).getRight() + l.get(i).getRight());
            }
        }
        for (Pair<Integer, Integer> pair : res) {
            pair.setRight(Math.round((float) pair.getRight() / s.size()));
        }
        return res;
    }

    public List<Pair<Integer, Integer>> variance(Set<List<Pair<Integer, Integer>>> s, List<Pair<Integer, Integer>> average) {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (List<Pair<Integer, Integer>> l : s) {
            for (int i = 0; i < l.size(); i++) {
                if (i >= res.size()) res.add(new Pair(l.get(i).getLeft(), 0));
                res.get(i).setRight(res.get(i).getRight() + (int) Math.pow(average.get(i).getRight() - l.get(i).getRight(), 2));
            }
        }
        for (Pair<Integer, Integer> pair : res) {
            pair.setRight(Math.round((float) pair.getRight() / (s.size() - 1)));
            pair.setRight((int) Math.round(Math.sqrt(pair.getRight())));
        }
        return res;
    }

    public String coordinatestostring(List<Pair<Integer, Integer>> average, List<Pair<Integer, Integer>> variance) {
        if (average.isEmpty()) {
            return "";
        }
        return "(" + average.get(0).getLeft() + "," + average.get(0).getRight()
                + ") +- (" + (average.get(0).getRight() - variance.get(0).getRight()) + ","
                + (average.get(0).getRight() + variance.get(0).getRight()) + ")\n"
                + coordinatestostring(average.subList(1, average.size()), variance.subList(1, variance.size()));
    }

    public String coordinatestostring(List<Pair<Integer, Integer>> average) {
        if (average.isEmpty()) {
            return "";
        }
        return "(" + average.get(0).getLeft() + "," + average.get(0).getRight() + ")"
                + coordinatestostring(average.subList(1, average.size()));
    }

    public Automata getAutomata(String grammar) {
        Generator g = new Generator(grammar);
//        System.out.println(g.getA());
//        System.out.println(g.getT());
        Colorizer c = new Colorizer(g);
        return new Automata(g.getA(), g.getT(), g.getS0(), g.getOpentoclose(), c.getColors(), c.getColoredEdges());
    }

    public boolean checkParseTrees(String input, Automata automata) {
        Set<AST> set = automata.parse(input);
        if (set.isEmpty() && !input.equals("")) return false;
        for (AST ast : set) {
            if (!ast.walkTerminals().equals(input)) {
                return false;
            }
        }
        return true;
    }

    public int timeParse(String input, Automata automata) {
        long startTime = System.nanoTime();
        Set<AST> set = automata.parse(input);
        long endTime = System.nanoTime();
        for (AST ast : set) {
            assertEquals(ast.walkTerminals(), input);
        }
        int time = (int) ((endTime - startTime)/1000000);
        assert(time >= 0);
        return time;
    }

    public String repeat(String s, int i) {
        return longnesting(s, "", i);
    }

    public String longnesting(String open, String close, int i) {
        if (i == 0) {
            return "";
        }
        return open + longnesting(open, close, i-1) + close;
    }

}