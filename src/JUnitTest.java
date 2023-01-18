import org.junit.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JUnitTest {


    /**
     * Example grammar from Jia et al.
     */
    @Test
    public void test1() {
        String grammar ="L : [ \"a\" A \"b\" ] L " +
                        "| e; " +

                        "A : \"c\"C " +
                        "| \"c\"D; " +

                        "C : \"c\"E; " +

                        "D : \"d\"E; " +

                        "E : e;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize(""));
        assertTrue(automata.recognize("accb"));
        assertTrue(automata.recognize("acdb"));
        assertTrue(automata.recognize("acdbaccbaccbacdb"));

        assertFalse(automata.recognize("a"));
        assertFalse(automata.recognize("adc"));
        assertFalse(automata.recognize("adcb"));

        assertTrue(checkParseTrees("accb", automata));
        assertTrue(checkParseTrees("acdb", automata));
        assertTrue(checkParseTrees("acdbaccbaccbacdb", automata));

    }

    @Test
    public void test2() {

        String grammar ="L : [ \"[\" L \"]\" ] K\n" +
                        "  | \"a\" K\n" +
                        "  | e;\n" +

                        "K : \",\" L\n" +
                        "  | e;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize(""));
        assertTrue(automata.recognize("[]"));
        assertTrue(automata.recognize("[a,a,a,a,a,a]"));
        assertTrue(automata.recognize("[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]]"));
        assertTrue(automata.recognize("[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]],[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]],[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]]"));

        assertFalse(automata.recognize("[aa]"));
        assertFalse(automata.recognize("[a,a,a,a,a,a"));

        assertTrue(checkParseTrees("[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]]", automata));
        assertTrue(checkParseTrees("[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]],[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]],[[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a],[a,a,a,a,a,a]]", automata));

    }

    @Test
    public void test3() {

        String grammar ="S : \"a\" A\n" +
                        "  | \"a\" B\n" +
                        "  | e;\n" +

                        "A : \"a\" S\n" +
                        "  | e;\n" +

                        "B : \"a\" S\n" +
                        "  | e;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize("aaaaaaaaaaaaaaaaaaaaa"));

        //For every 2 a's the amount of possible parse trees doubles. Here are 24 a's, so we expect 2^12 = 4096 parse trees
        assertEquals(4096, automata.parse("aaaaaaaaaaaaaaaaaaaaaaaa").size());
        assertTrue(checkParseTrees("aaaaaaaaaaaaaaaaaaaaaaaa", automata));
    }

    @Test
    public void test4() {

        String grammar ="S : [ \"[\" S \"]\" ] K\n" +
                        "  | [ \"{\" S \"}\" ] K\n" +
                        "  | [ \"<\" S \">\" ] K\n" +
                        "  | \"s\" K\n" +
                        "  | e;\n" +

                        "K : \",\" S\n" +
                        "  | e;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize("<s>"));
        assertTrue(automata.recognize("{<s>}"));
        assertTrue(automata.recognize("[{<s>}]"));
        assertTrue(automata.recognize("{[<s>]}"));
        assertTrue(automata.recognize("[{<<[{s}]>>}]"));

        assertTrue(checkParseTrees("", automata));
        assertTrue(checkParseTrees("{[<s>]}", automata));
        assertTrue(checkParseTrees("[{<<[{s}]>>}]", automata));

    }

    @Test
    public void test5() {

        String grammar ="S : [ \"[\" L \"]\" ] KS\n" +
                        "  | e;\n" +

                        "KS: \",\" S\n" +
                        "  | e;\n" +

                        "L : [ \"(\" C \")\" ] KL\n" +
                        "  | e;\n" +

                        "KL: \",\" L\n" +
                        "  | e;\n" +

                        "C : \"c\" C\n" +
                        "  | e;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize("[],[],[],[]"));
        assertTrue(automata.recognize("[(ccc),(ccc),(ccc),(ccc)]"));


        assertTrue(checkParseTrees("", automata));
        assertTrue(checkParseTrees("[],[],[],[]", automata));
        assertTrue(checkParseTrees("[(ccc),(ccc),(ccc),(ccc)],[(ccc),(ccc),(ccc),(ccc)],[(ccc),(ccc),(ccc),(ccc)]", automata));

        assertTrue(checkParseTrees("[(c]",  automata));
    }

    @Test
    public void test6() {

        String grammar ="S : [ \"[\" A \"]\" ] S\n" +
                        "  | e ;\n" +

                        "A : [ \"{\" B \"}\" ] A\n" +
                        "  | e ;\n" +

                        "B : [ \"(\" C \")\" ] B\n" +
                        "  | e ;\n" +

                        "C : \"c\" C\n" +
                        "  | e ;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize("[{(ccc)}]"));
        assertTrue(automata.recognize("[{(ccc}]"));
        assertTrue(automata.recognize("[{(ccc)]"));
        assertTrue(automata.recognize("[{(ccc]"));
        assertTrue(automata.recognize("[{(ccc][{(ccc][{(ccc]"));
    }

    @Test
    public void test7() {

        String grammar ="S : [ \"[\" A \"]\" ] S\n" +
                        "  | e ;\n" +

                        "A : [ \"{\" B \"}\" ] A\n" +
                        "  | e ;\n" +

                        "B : [ \"(\" S \")\" ] B\n" +
                        "  | e ;";

        Automata automata = getAutomata(grammar);

        assertTrue(automata.recognize("[{()}]"));
        assertTrue(automata.recognize("[{()}][{()}][{()}]"));
        assertTrue(automata.recognize("[{(]"));
        assertTrue(automata.recognize("[{(][{(][{(]"));

        assertTrue(automata.recognize("[{([{()}])}]"));
        assertTrue(automata.recognize("[{(" + "[{(]" + "]"));

        assertFalse(automata.recognize("[{(" + "[{(]" + "}"));

        assertTrue(checkParseTrees("[{(]", automata));

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
}