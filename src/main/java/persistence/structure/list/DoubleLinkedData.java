package persistence.structure.list;

import persistence.base.PersistentNode;

import java.util.UUID;

public class DoubleLinkedData<T> {
    private PersistentNode<DoubleLinkedData<T>> previous;
    private PersistentNode<DoubleLinkedData<T>> next;
    private UUID id;
    private PersistentNode<T> value;

    public DoubleLinkedData(PersistentNode<DoubleLinkedData<T>> next, PersistentNode<DoubleLinkedData<T>> previous, PersistentNode<T> value) {
        this.setNext(next);
        this.setPrevious(previous);
        this.setValue(value);
        setId(UUID.randomUUID());
    }

    public DoubleLinkedData(PersistentNode<DoubleLinkedData<T>> next, PersistentNode<DoubleLinkedData<T>> previous, PersistentNode<T> value, UUID id) {
        this.setNext(next);
        this.setPrevious(previous);
        this.setValue(value);
        this.setId(id);
    }

    public PersistentNode<DoubleLinkedData<T>> getPrevious() {
        return previous;
    }

    public void setPrevious(PersistentNode<DoubleLinkedData<T>> previous) {
        this.previous = previous;
    }

    public PersistentNode<DoubleLinkedData<T>> getNext() {
        return next;
    }

    public void setNext(PersistentNode<DoubleLinkedData<T>> next) {
        this.next = next;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PersistentNode<T> getValue() {
        return value;
    }

    public void setValue(PersistentNode<T> value) {
        this.value = value;
    }
}
