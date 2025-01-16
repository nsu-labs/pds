package persistence.structure.list;

import persistence.base.PersistentNode;

public class DoubleLinkedContent<T> {
    private final PersistentNode<DoubleLinkedData<T>> pseudoHead;
    private final PersistentNode<DoubleLinkedData<T>> pseudoTail;

    public DoubleLinkedContent(PersistentNode<DoubleLinkedData<T>> pseudoHead, PersistentNode<DoubleLinkedData<T>> pseudoTail) {
        this.pseudoHead = pseudoHead;
        this.pseudoTail = pseudoTail;
    }

    public PersistentNode<DoubleLinkedData<T>> getPseudoHead() {
        return pseudoHead;
    }

    public PersistentNode<DoubleLinkedData<T>> getPseudoTail() {
        return pseudoTail;
    }
}
