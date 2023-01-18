import java.util.ArrayList;
import java.util.List;

public class AST {

    private final String value;
    private final List<AST> children;
    private boolean isReturn = false;

    public AST(String value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(AST ast) {
        children.add(ast);
    }

    public int depth() {
        if (children.isEmpty()) {
            return 0;
        } else {
            int max = 0;
            for (AST child : children) {
                max = Math.max(max, child.depth());
            }
            return max;
        }
    }

    /**
     * Returns all terminals in correct order. Can be used to check if it corresponds to the input String
     * @return
     */
    public String walkTerminals() {
        if (children.isEmpty()) {
            return value;
        }
        StringBuilder res = new StringBuilder();
        for (AST child : children) {
            res.append(child.walkTerminals());
        }
        return res.toString();
    }

    public String getValue() {
        return value;
    }

    public List<AST> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return value;
        }
        StringBuilder res = new StringBuilder();
        res.append(value);
        for (AST ast : children) {
            res.append(String.format(" [%s]", ast.toString()));
        }
        return res.toString();
    }
}
