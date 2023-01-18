import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * Tokenizes the grammar for the PDA generator.
 */
public class GrammarTokenizer {

    private CharacterIterator it;

    private Map<NonTerminal, Set<List<Token>>> rules;
    private Map<String, NonTerminal> getNonTerm;

    private NonTerminal start;
    private Set<String> El;
    private Set<String> Ec;
    private Set<String> Er;
    private Map<String, String> opentoclose;

    /**
     * Tokenizes the grammar by creating the following objects:
     *      - rules: Maps NonTerminal Tokens to a map of arraylists, a list with Tokens for every rule corresponding to that NonTerminal.
     *      - start: The start Non Terminal.
     *      - El: Alphabet of internal symbols.
     *      - Ec: Alphabet of call symbols.
     *      - Er: Alphabet of return symbols.
     *      - opentoclose: Map from start-nesting symbol to its correspond close-nesting symbol.
     * @param grammar String representation of a grammar.
     * @return rules
     */
    public Map<NonTerminal, Set<List<Token>>> tokenize(String grammar) {

        it = new StringCharacterIterator(grammar);
        rules = new HashMap<>();
        getNonTerm = new HashMap<>();
        start = null;
        El = new HashSet<>();
        Ec = new HashSet<>();
        Er = new HashSet<>();
        opentoclose = new HashMap<>();

        while (it.current() != CharacterIterator.DONE) {
            //left
            skipwhitespaces();
            NonTerminal nonterm = nonTerm();

            List<Token> currentrule = new ArrayList<>();
            rules.get(nonterm).add(currentrule);

            //middle
            skipwhitespaces();
            assert(it.current() == ':');
            it.next();

            //right
            while (it.current() != ';') {
                skipwhitespaces();
                switch (it.current()) {
                    case '"':
                        String str = string();
                        El.add(str);
                        currentrule.add(new Token(TokenType.String, str));
                        break;
                    case 'e':
                        it.next();
                        skipwhitespaces();
                        currentrule.add(new Token(TokenType.Epsilon, null));
                        break;
                    case '[':
                        it.next();
                        skipwhitespaces();
                        String opensymbol = string();
                        skipwhitespaces();
                        NonTerminal nt = nonTerm();
                        skipwhitespaces();
                        String closesymbol = string();
                        skipwhitespaces();
                        assert(it.current() == ']');
                        it.next();

                        Ec.add(opensymbol);
                        Er.add(closesymbol);
                        opentoclose.put(opensymbol, closesymbol);

                        currentrule.add(new Token(TokenType.NestOpen, opensymbol));
                        currentrule.add(nt);
                        currentrule.add(new Token(TokenType.NestClose, closesymbol));
                        break;
                    case '(':
                        it.next();
                        skipwhitespaces();
                        currentrule.add(new Token(TokenType.ParensOpen));
                        break;
                    case ')':
                        it.next();
                        skipwhitespaces();
                        currentrule.add(new Token(TokenType.ParensClose));
                        break;
                    case '/':
                        it.next();
                        skipwhitespaces();
                        currentrule.add(new Token(TokenType.Or));
                        break;
                    case '*':
                        it.next();
                        skipwhitespaces();
                        currentrule.add(new Token(TokenType.Star));
                        break;
                    case '|':
                        it.next();
                        skipwhitespaces();
                        currentrule = new ArrayList<>();
                        rules.get(nonterm).add(currentrule);
                        break;
                    default:
                        if (Character.isLetter(it.current())) {
                            currentrule.add((nonTerm()));
                        }

                }
            }
            it.next();
            skipwhitespaces();
        }
        return rules;
    }

    /**
     * Consumes a Non Terminal
     * @return String value of the Non Terminal
     */
    public NonTerminal nonTerm() {
        StringBuilder res = new StringBuilder();
        if (Character.isLetter(it.current())) {
            while (Character.isLetter(it.current()) || Character.isDigit(it.current())) {
                res.append(it.current());
                it.next();
            }
        }
        if (!getNonTerm.containsKey(res.toString())) {
            NonTerminal nt = new NonTerminal(res.toString());
            getNonTerm.put(res.toString(), nt);
            rules.put(nt, new HashSet<>());
            if (start == null) {
                start = nt;
            }
            return nt;
        } else {
            return getNonTerm.get(res.toString());
        }

    }

    /**
     * Consumes a String
     * @return String value
     */
    public String string() {
        StringBuilder res = new StringBuilder();
        while (it.next() != '"') {
            res.append(it.current());
        }
        it.next();
        return res.toString();
    }

    /**
     * Consumes all coming whitespaces
     */
    public void skipwhitespaces() {
        while (it.current() == ' ' || it.current() == '\t' || it.current() == '\n') {
            it.next();
        }
    }

    public NonTerminal getStart() {
        return start;
    }

    public Set<String> getEl() {
        return El;
    }

    public Set<String> getEc() {
        return Ec;
    }

    public Set<String> getEr() {
        return Er;
    }

    public Map<String, String> getOpentoclose() {
        return opentoclose;
    }
}
