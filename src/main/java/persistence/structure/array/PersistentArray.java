package persistence.structure.array;

import persistence.base.*;

import java.util.*;

public class PersistentArray<T> extends BasePersistentCollection<Integer, T, List<PersistentNode<T>>> implements Iterable<T>, IUndoRedo<PersistentArray<T>> {

    /**
     * Конструктор по умолчанию. Создаёт пустой массив.
     */
    public PersistentArray() throws IndexOutOfBoundsException {
        // Создаём пустую коллекцию узлов и устанавливаем счётчик модификаций.
        nodes = new PersistentContent<>(new ArrayList<>(), new ModificationCount(modificationCount));
    }

    /**
     * Приватный конструктор для внутреннего использования.
     * Позволяет создавать массив с заданными узлами и параметрами.
     */
    private PersistentArray(PersistentContent<List<PersistentNode<T>>> nodes, int count, int modificationCount) {
        super(nodes, count, modificationCount);
    }

    /**
     * Приватный конструктор для внутреннего использования.
     * Позволяет задавать начальное количество модификаций.
     */
    public PersistentArray(PersistentContent<List<PersistentNode<T>>> nodes, int count, int modificationCount, int start) {
        super(nodes, count, modificationCount, start);
    }

    /**
     * Пересобирает массив, чтобы учесть только изменения до текущего шага модификации.
     */
    @Override
    protected PersistentContent<List<PersistentNode<T>>> reassembleNodes() {
        // Создаём новую коллекцию узлов для нового состояния.
        PersistentContent<List<PersistentNode<T>>> newContent = new PersistentContent<>(new ArrayList<>(),
                new ModificationCount(modificationCount));

        // Список всех модификаций, которые произошли до текущего шага.
        ArrayList<Map.Entry<Integer, Map.Entry<Integer, T>>> allModifications = new ArrayList<>();

        // Проходим по всем узлам массива.
        for (var i = 0; i < nodes.getContent().size(); i++) {
            PersistentNode<T> node = nodes.getContent().get(i); // Текущий узел.
            int finalI = i;

            // Собираем все модификации узла, которые произошли до текущего шага.
            List<Map.Entry<Integer, Map.Entry<Integer, T>>> neededModifications = node
                    .getModifications()
                    .toList()
                    .stream()
                    .filter(
                            m -> m.getKey() <= modificationCount // Только модификации до текущего шага.
                    ).sorted(
                            Comparator.comparingInt(Map.Entry::getKey) // Сортируем по шагу модификации.
                    ).map(val -> Map.entry(finalI, val)) // Создаём пары (индекс узла, модификация).
                    .toList();

            // Добавляем все модификации узла в общий список.
            allModifications.addAll(neededModifications);
        }

        // Применяем модификации к новой коллекции узлов.
        allModifications.forEach(m -> {
            if (m.getKey() >= newContent.getContent().size()) {
                // Если узел новый, создаём его и добавляем в коллекцию.
                newContent.update(c ->
                        c.add(new PersistentNode<>(m.getValue().getKey(), m.getValue().getValue())));
            } else {
                // Если узел существует, обновляем его значение.
                newContent.update(c -> c.get(m.getKey()).update(m.getValue().getKey(), m.getValue().getValue()));
            }
        });

        return newContent;
    }

    /**
     * Реализация добавления элемента в массив.
     */
    private void addImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, T value) {
        // Добавляем новый узел с текущим значением и шагом модификации.
        content.update(c -> c.add(new PersistentNode<>(modificationCount + 1, value)));
    }

    /**
     * Реализация вставки элемента в массив по индексу.
     */
    private void insertImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index, T value) {
        content.update(c -> {
            // Добавляем новый узел в конец, чтобы сохранить размер массива.
            c.add(new PersistentNode<>(modificationCount + 1, c.get(c.size() - 1).value(modificationCount)));

            // Обновляем значение по указанному индексу.
            c.get(index).update(modificationCount + 1, value);

            // Сдвигаем оставшиеся элементы вправо.
            for (var i = index + 1; i < c.size(); i++) {
                c.get(i).update(modificationCount + 1, c.get(i - 1).value(modificationCount));
            }
        });
    }

    /**
     * Реализация замены элемента в массиве.
     */
    private void replaceImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index, T value) {
        // Обновляем значение в узле по индексу.
        content.update(c -> c.get(index).update(modificationCount + 1, value));
    }

    /**
     * Реализация удаления элемента из массива.
     */

    private void removeImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index) {
        content.update(c -> {
            // Сдвигаем элементы влево, начиная с удаляемого индекса
            for (var i = index; i < c.size() - 1; i++) {
                c.get(i).update(modificationCount + 1, c.get(i + 1).value(modificationCount));
            }
            // А вместо записи `null` в последний элемент — физически убираем его
            c.remove(c.size() - 1);
        });
    }

    /**
     * Реализация очистки массива.
     */

    private void clear(PersistentContent<List<PersistentNode<T>>> content, int modificationCount) {
        content.update(c -> c.clear()); // физически делаем c.size() = 0
    }

    /**
     * Добавление элемента в конец массива.
     */
    public PersistentArray<T> add(T value) {
        if (nodes.getMaxModification().getValue() > modificationCount) {
            var res = reassembleNodes(); // Пересобираем узлы.
            addImpl(res, modificationCount, value); // Добавляем элемент.
            return new PersistentArray<>(res, getCount() + 1, modificationCount + 1);
        }

        // Добавляем элемент в текущую коллекцию узлов.
        addImpl(nodes, modificationCount, value);
        return new PersistentArray<>(nodes, getCount() + 1, modificationCount + 1);
    }

    /**
     * Вставка элемента в массив по указанному индексу.
     */
    public PersistentArray<T> insert(int index, T value) {
        if (index < 0 || index > getCount()) {
            throw new IndexOutOfBoundsException(index);
        }

        if (index == getCount()) {
            return add(value);
        }

        if (nodes.getMaxModification().getValue() > modificationCount) {
            var res = reassembleNodes(); // Пересобираем узлы.
            insertImpl(res, modificationCount, index, value); // Вставляем элемент.
            return new PersistentArray<>(res, getCount() + 1, modificationCount + 1);
        }

        // Вставляем элемент в текущую коллекцию узлов.
        insertImpl(nodes, modificationCount, index, value);
        return new PersistentArray<>(nodes, getCount() + 1, modificationCount + 1);
    }

    /**
     * Замена элемента по индексу.
     */
    public PersistentArray<T> replace(Integer index, T value) {
        if (index < 0 || index > getCount()) {
            throw new IndexOutOfBoundsException(index);
        }

        if (nodes.getMaxModification().getValue() > modificationCount) {
            var res = reassembleNodes(); // Пересобираем узлы.
            replaceImpl(res, modificationCount, index, value); // Заменяем элемент.
            return new PersistentArray<>(res, getCount(), modificationCount + 1);
        }

        // Заменяем элемент в текущей коллекции узлов.
        replaceImpl(nodes, modificationCount, index, value);
        return new PersistentArray<>(nodes, getCount(), modificationCount + 1);
    }

    /**
     * Удаление элемента из массива по индексу.
     */
    public PersistentArray<T> remove(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException(index);
        }

        if (nodes.getMaxModification().getValue() > modificationCount) {
            var res = reassembleNodes(); // Пересобираем узлы.
            removeImpl(res, modificationCount, index); // Удаляем элемент.
            return new PersistentArray<>(res, getCount() - 1, modificationCount + 1);
        }

        // Удаляем элемент из текущей коллекции узлов.
        removeImpl(nodes, modificationCount, index);
        return new PersistentArray<>(nodes, getCount() - 1, modificationCount + 1);
    }

    /**
     * Очистка массива (установка всех элементов в null).
     */
    public PersistentArray<T> clearAll() {
        if (nodes.getMaxModification().getValue() > modificationCount) {
            var res = reassembleNodes(); // Пересобираем узлы.
            clear(res, modificationCount); // Очищаем массив.
            return new PersistentArray<>(res, 0, modificationCount + 1);
        }

        // Очищаем текущую коллекцию узлов.
        clear(nodes, modificationCount);
        return new PersistentArray<>(nodes, 0, modificationCount + 1);
    }

    /**
     * Получение элемента по индексу.
     */
    public T get(Integer index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException(index);
        }
        return nodes.getContent().get(index).value(modificationCount); // Возвращаем значение на текущем шаге.
    }


    public Iterator<T> iterator() {
        return nodes.getContent()
                .stream()
                .filter(
                        n -> n.getModifications().toList()
                                .stream()
                                .anyMatch(m -> m.getKey() <= modificationCount)) // Только актуальные узлы.
                .map(n -> n.value(modificationCount)) // Возвращаем значения узлов.
                .iterator();
    }

    /**
     * Откат изменений на один шаг назад.
     */
    public PersistentArray<T> undo() {
        return modificationCount == startModificationCount ? this : new PersistentArray<>(nodes,
                recalculateCount(modificationCount - 1), modificationCount - 1);
    }

    /**
     * Повтор изменений на один шаг вперёд.
     */
    public PersistentArray<T> redo() {
        return modificationCount == nodes.getMaxModification().getValue() ? this : new PersistentArray<>(nodes,
                recalculateCount(modificationCount + 1), modificationCount + 1);
    }

    /**
     * Пересчёт количества элементов в массиве.
     */
    @Override
    protected int recalculateCount(int modificationStep) {
        return (int) nodes.getContent()
                .stream()
                .filter(n -> n.getModifications().toList()
                        .stream()
                        .anyMatch(m -> m.getKey() <= modificationStep)
                ).count();
    }
    
}
