package persistence.base.tree;

public class Node<TK, TV> {
    private TK key;
    private TV data;
    private Node<TK, TV> parent;
    private Node<TK, TV> left;
    private Node<TK, TV> right;
    private int hash;
    private Color colour;


    public Node(TK key, TV data) {
        this.setKey(key);
        this.setData(data);
        setHash(key.hashCode());
    }

    public TK getKey() {
        return key;
    }

    public void setKey(TK key) {
        this.key = key;
    }

    public TV getData() {
        return data;
    }

    public void setData(TV data) {
        this.data = data;
    }

    public Node<TK, TV> getParent() {
        return parent;
    }

    public void setParent(Node<TK, TV> parent) {
        this.parent = parent;
    }

    public Node<TK, TV> getLeft() {
        return left;
    }

    public void setLeft(Node<TK, TV> left) {
        this.left = left;
    }

    public Node<TK, TV> getRight() {
        return right;
    }

    public void setRight(Node<TK, TV> right) {
        this.right = right;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }
}
