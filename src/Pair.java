/**
 * Implementation of a tuple in Java
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> implements Comparable<Pair<NonTerminal, NonTerminal>>{

    private K left;
    private V right;

    public Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public K getLeft() {
        return left;
    }

    public V getRight() {
        return right;
    }

    public void setRight(V right) { this.right = right; }

    public String toString() {
        return "< " + left + ", " + right + " >";
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Pair
                && ((Pair) o).getLeft().equals(left)
                && ((Pair) o).getRight().equals(right)
        );
    }

//    @Override
//    public int compareTo(Pair<K, V> o) {
//        if (left.equals(o.left) && right.equals(o.right)) {
//            return 0;
//        }
//        NonTerminal oleft = (NonTerminal) o.left;
//        NonTerminal oright = (NonTerminal) o.right;
//    }

    @Override
    public int compareTo(Pair<NonTerminal, NonTerminal> o) {
//        if (left.equals(o.left) && right.equals(o.right)) {
//            return 0;
//        }
        int res = ((NonTerminal) left).getValue().compareTo(o.getLeft().getValue());
        if (res == 0) res = ((NonTerminal) right).getValue().compareTo(o.getRight().getValue());
        return res;
    }

//    int res = own.getLeft().getValue().compareTo(other.getLeft().getValue());
//        if (res == 0) {
//        res = own.getRight().getValue().compareTo(other.getRight().getValue());
//    }
//        return res;
}
