package persistence.structure.list;

import persistence.base.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentLinkedList<T> extends BasePersistentCollection<Integer, T, DoubleLinkedContent<T>> implements IUndoRedo<PersistentLinkedList<T>> {

    public PersistentLinkedList() {
        var head = new PersistentNode<>(modificationCount - 1, new DoubleLinkedData<T>(null, null, new PersistentNode<>(-1, null)));
        var tail = new PersistentNode<>(modificationCount - 1, new DoubleLinkedData<>(null, null, new PersistentNode<T>(-1, null)));
        head.update(modificationCount, new DoubleLinkedData<>(tail, null, head.value(modificationCount - 1).getValue(), head.value(modificationCount - 1).getId()));
        tail.update(modificationCount, new DoubleLinkedData<>(null, head, tail.value(modificationCount - 1).getValue(), tail.value(modificationCount - 1).getId()));

        nodes = new PersistentContent<>(new DoubleLinkedContent<>(head, tail), new ModificationCount(modificationCount));

    }

    private PersistentLinkedList(PersistentContent<DoubleLinkedContent<T>> nodes,
                                 int count,
                                 int modificationCount) {
        super(nodes, count, modificationCount);
    }

    public PersistentLinkedList(PersistentContent<DoubleLinkedContent<T>> nodes,
                                int count, int modificationCount,
                                int start) {
        super(nodes, count, modificationCount, start);
    }

    @Override
    protected int recalculateCount(int modificationStep) {
        return toList(modificationStep).size();
    }

    @Override
    protected PersistentContent<DoubleLinkedContent<T>> reassembleNodes() {
        var allModifications = new ArrayList<Map.Entry<UUID, Map.Entry<Integer, DoubleLinkedData<T>>>>();
        var nodeModificationCount = new LinkedHashMap<UUID, Integer>();
        var current = nodes.getContent().getPseudoHead();

        for (var i = getCount(); i != -2; i--) {
            var neededModifications = current.getModifications().toList()
                    .stream()
                    .filter(m -> m.getKey() <= modificationCount)
                    .map(m -> Map.entry(m.getValue().getId(), m))
                    .sorted(Map.Entry.comparingByKey()).toList();

            allModifications.addAll(neededModifications);
            nodeModificationCount.put(current.value(modificationCount).getId(), neededModifications.size());
            current = current.value(modificationCount).getNext();
        }
        var orderedModifications = new ArrayList<>(allModifications.stream()
                .collect(Collectors.groupingBy(m -> m.getValue().getKey()))
                .values()
                .stream()
                .flatMap(entries -> entries
                        .stream()
                        .sorted((Comparator.comparing(o -> nodeModificationCount.get(o.getKey())))))
                .toList())
                .stream()
                .sorted(Comparator.comparing(o -> o.getValue().getKey()))
                .collect(Collectors.toList());

        var newNodes = new LinkedHashMap<UUID, PersistentNode<DoubleLinkedData<T>>>();
        newNodes.put(orderedModifications.get(0).getKey(),
                new PersistentNode<>(-1,
                        new DoubleLinkedData<>(null,
                                null,
                                new PersistentNode<>(-1, null),
                                orderedModifications.get(0).getKey())));


        orderedModifications.remove(0);

        newNodes.put(orderedModifications.get(0).getKey(),
                new PersistentNode<>(-1,
                        new DoubleLinkedData<>(null,
                                null,
                                new PersistentNode<>(-1, null),
                                orderedModifications.get(0).getKey())
                )
        );

        orderedModifications.remove(0);

        for (var entry : orderedModifications) {
            var nodeKey = entry.getKey();
            var step = entry.getValue().getKey();
            var nodeValue = entry.getValue().getValue();
            if (newNodes.containsKey(nodeKey)) {
                var node = newNodes.get(nodeKey);
                node.update(step,
                        new DoubleLinkedData<>(nodeValue.getNext() == null ? null :
                                newNodes.get(nodeValue.getNext().value(step).getId()),
                                nodeValue.getPrevious() == null ? null : newNodes.get(nodeValue.getPrevious().value(step).getId()),
                                node.value(step - 1).getValue().update(step, nodeValue.getValue().value(step)), nodeKey));
            } else {
                var newNode = new PersistentNode<>(step,
                        new DoubleLinkedData<>(newNodes.get(nodeValue.getNext().value(step).getId()),
                                newNodes.get(nodeValue.getPrevious().value(step).getId()),
                                new PersistentNode<>(step, nodeValue.getValue().value(step)),
                                nodeKey)
                );
                newNodes.put(nodeKey, newNode);
            }
        }

        var newHead = newNodes.get(nodes.getContent().getPseudoHead().value(modificationCount).getId());
        var newTail = newNodes.get(nodes.getContent().getPseudoTail().value(modificationCount).getId());
        return new PersistentContent<>(new DoubleLinkedContent<>(newHead, newTail),
                new ModificationCount(modificationCount));

    }

    public T get(Integer num) {
        var node = findNode(num);
        return node == nodes.getContent().getPseudoTail() ? null :
                node.value(modificationCount).getValue() == null ?
                        null :
                        node.value(modificationCount).getValue().value(modificationCount);
    }

    public PersistentLinkedList<T> clear() {
        if (getCount() == 0) {
            return this;
        }

        Function<PersistentContent<DoubleLinkedContent<T>>, PersistentLinkedList<T>> updContent = x -> {
            x.update(m ->
            {
                m.getPseudoHead().update(modificationCount + 1,
                        new DoubleLinkedData<>(m.getPseudoTail(), null, m.getPseudoHead().value(modificationCount).getValue(), m.getPseudoHead().value(modificationCount).getId()));
                m.getPseudoTail().update(modificationCount + 1,
                        new DoubleLinkedData<>(null, m.getPseudoHead(), m.getPseudoTail().value(modificationCount).getValue(), m.getPseudoTail().value(modificationCount).getId()));
            });

            return new PersistentLinkedList<>(x, 0, modificationCount + 1);
        };

        if (nodes.getMaxModification().getValue() > modificationCount) {
            var newContent = reassembleNodes();
            return updContent.apply(newContent);
        }

        return updContent.apply(nodes);
    }

    public boolean contains(T item) {
        var current = nodes.getContent().getPseudoHead().value(modificationCount).getNext();
        for (var i = getCount(); i != 0; i--) {
            if (current.value(modificationCount).getValue().value(modificationCount).equals(item)) {
                return true;
            }
            current = current.value(modificationCount).getNext();
        }
        return false;
    }

    public PersistentLinkedList<T> replace(Integer num, T value) {
        if (num > getCount()) return this;
        if (nodes.getMaxModification().getValue() > modificationCount) {
            var newContent = reassembleNodes();
            return replace(newContent, num, value);
        } else {
            return replace(nodes, num, value);
        }
    }

    public PersistentLinkedList<T> addLast(T value) {
        if (nodes.getMaxModification().getValue() > modificationCount) {
            var newContent = reassembleNodes();
            return addLast(newContent, value);
        }

        return addLast(nodes, value);
    }

    public PersistentLinkedList<T> addFirst(T value) {
        if (nodes.getMaxModification().getValue() > modificationCount) {
            var newContent = reassembleNodes();
            return addFirst(newContent, value);
        }

        return addFirst(nodes, value);
    }

    public PersistentLinkedList<T> removeLast() {
        if (getCount() == 0) {
            return this;
        }

        if (nodes.getMaxModification().getValue() > modificationCount) {
            var newContent = reassembleNodes();
            return removeLast(newContent);
        }

        return removeLast(nodes);
    }

    public PersistentLinkedList<T> removeFirst() {
        if (getCount() == 0) {
            return this;
        }

        if (nodes.getMaxModification().getValue() > modificationCount) {
            var newContent = reassembleNodes();
            return removeFirst(newContent);
        }

        return removeFirst(nodes);
    }

    private PersistentLinkedList<T> addFirst(PersistentContent<DoubleLinkedContent<T>> content, T value) {
        var oldHead = content.getContent().getPseudoHead().value(modificationCount);
        var oldNextToHead = oldHead.getNext();
        var oldNextToHeadValue = oldNextToHead.value(modificationCount);
        var newHead = new PersistentNode<>(modificationCount + 1,
                new DoubleLinkedData<>(oldHead.getNext(),
                        content.getContent().getPseudoHead(),
                        new PersistentNode<>(modificationCount + 1, value)
                )
        );
        content.update(m -> {
                    oldNextToHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldNextToHeadValue.getNext(),
                                    newHead,
                                    oldNextToHeadValue.getValue(),
                                    oldNextToHeadValue.getId())
                    );
                    m.getPseudoHead().update(modificationCount + 1,
                            new DoubleLinkedData<>(newHead, null, oldHead.getValue(), oldHead.getId()));
                }
        );

        return new PersistentLinkedList<>(content, getCount() + 1, modificationCount + 1);
    }

    private PersistentNode<DoubleLinkedData<T>> findNode(int num) {
        var current = nodes.getContent().getPseudoHead().value(modificationCount).getNext();
        for (var i = num; i != 0; i--) {
            current = current.value(modificationCount).getNext();
        }

        return current;
    }


    private PersistentLinkedList<T> replace(PersistentContent<DoubleLinkedContent<T>> content, int num, T value) {
        var node = findNode(num);
        var nodeValue = node.value(modificationCount);
        content.update(m ->
                node.update(modificationCount + 1,
                        new DoubleLinkedData<>(nodeValue.getNext(),
                                nodeValue.getPrevious(),
                                nodeValue.getValue().update(modificationCount + 1, value),
                                nodeValue.getId())
                )
        );

        return new PersistentLinkedList<>(content, getCount(), modificationCount + 1);
    }

    private PersistentLinkedList<T> addLast(PersistentContent<DoubleLinkedContent<T>> content, T value) {
        var oldTail = content.getContent().getPseudoTail().value(modificationCount);
        var oldNextToTail = oldTail.getPrevious();
        var oldNextToTailValue = oldNextToTail.value(modificationCount);
        var newTail = new PersistentNode<>(modificationCount + 1,
                new DoubleLinkedData<>(content.getContent().getPseudoTail(),
                        oldTail.getPrevious(),
                        new PersistentNode<>(modificationCount + 1, value)
                )
        );
        content.update(m -> {
                    oldNextToTail.update(modificationCount + 1,
                            new DoubleLinkedData<>(newTail,
                                    oldNextToTailValue.getPrevious(),
                                    oldNextToTailValue.getValue(),
                                    oldNextToTailValue.getId())
                    );
                    m.getPseudoTail().update(modificationCount + 1,
                            new DoubleLinkedData<>(null, newTail, oldTail.getValue(), oldTail.getId())
                    );
                }
        );

        return new PersistentLinkedList<>(content, getCount() + 1, modificationCount + 1);
    }

    private PersistentLinkedList<T> removeFirst(PersistentContent<DoubleLinkedContent<T>> content) {
        var oldHead = content.getContent().getPseudoHead();
        var oldHeadValue = oldHead.value(modificationCount);
        var oldNextToNextToHead = oldHeadValue.getNext().value(modificationCount).getNext();
        var oldNextToNextToHeadValue = oldNextToNextToHead.value(modificationCount);
        content.update(m -> {
                    oldNextToNextToHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldNextToNextToHeadValue.getNext(),
                                    oldHead,
                                    oldNextToNextToHeadValue.getValue(),
                                    oldNextToNextToHeadValue.getId())
                    );
                    oldHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldNextToNextToHead,
                                    null, oldHeadValue.getValue(),
                                    oldHeadValue.getId())
                    );
                }
        );
        return new PersistentLinkedList<>(content, getCount() - 1, modificationCount + 1);
    }

    private PersistentLinkedList<T> removeLast(PersistentContent<DoubleLinkedContent<T>> content) {
        var oldTail = content.getContent().getPseudoTail();
        var oldTailValue = oldTail.value(modificationCount);
        var oldNextToNextToTail = oldTailValue.getPrevious().value(modificationCount).getPrevious();
        var oldNextToNextToTailValue = oldNextToNextToTail.value(modificationCount);
        content.update(m -> {
                    oldNextToNextToTail.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldTail,
                                    oldNextToNextToTailValue.getPrevious(),
                                    oldNextToNextToTailValue.getValue(),
                                    oldNextToNextToTailValue.getId())
                    );
                    oldTail.update(modificationCount,
                            new DoubleLinkedData<>(null,
                                    oldNextToNextToTail,
                                    oldTailValue.getValue(),
                                    oldTailValue.getId())
                    );
                }
        );
        return new PersistentLinkedList<>(content, getCount() - 1, modificationCount + 1);
    }


    private ArrayList<T> toList(int modificationStep) {
        var newList = new ArrayList<T>();
        var current = nodes.getContent().getPseudoHead().value(modificationStep).getNext();
        for (var i = getCount(); i != 0; i--) {
            newList.add(current.value(modificationStep).getValue().value(modificationStep));
            current = current.value(modificationStep).getNext();
        }
        return newList;
    }

    @Override
    public PersistentLinkedList<T> undo() {
        return modificationCount == startModificationCount ? this :
                new PersistentLinkedList<>(
                        nodes,
                        recalculateCount(modificationCount - 1),
                        modificationCount - 1);

    }

    @Override
    public PersistentLinkedList<T> redo() {
        return modificationCount == nodes.getMaxModification().getValue() ? this :
                new PersistentLinkedList<>(nodes,
                        recalculateCount(modificationCount + 1),
                        modificationCount + 1);

    }

    public int size() {
        return getCount();
    }
}
