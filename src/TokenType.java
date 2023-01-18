/**
 * Type of input Grammar Token.
 */
public enum TokenType {
    NonTerminal,
    String,
    NestOpen,
    NestClose,
    ParensOpen,
    ParensClose,
    Or,
    Star,
    Epsilon
}
