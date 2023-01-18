/**
 *  Tokens contain information on specific parts of the input grammar
 */
public class Token {

    private final TokenType token;
    private final String value;

    /**
     * Create a token with its type and its value.
     * @param token type
     * @param value value
     */
    public Token(TokenType token, String value) {
        this.token = token;
        this.value = value;
    }

    /**
     * Create a token with only a type. Value will be null.
     * @param token type
     */
    public Token(TokenType token) {
        this.token = token;
        this.value = null;
    }

    public TokenType getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Token
                && token == ((Token) o).token
                && ((value == null && ((Token) o).value == null)
                    || value.equals(((Token) o).value))
        );
    }

    @Override
    public String toString() {
        return value;
    }

}

/**
 * Specific subclass for NonTerminals for application where other types of Tokens do not suffice.
 */
class NonTerminal extends Token {

    private boolean nullable = false;

    /**
     * Create a NonTerminal with its value.
     * @param value value
     */
    public NonTerminal(String value) {
        super(TokenType.NonTerminal, value);
    }

    /**
     * Create an empty NonTerminal. Value will be null.
     */
    public NonTerminal() {
        super(TokenType.NonTerminal);
    }

    public void setNullable() {
        nullable = true;
    }

    public boolean nullable() {
        return nullable;
    }
}
