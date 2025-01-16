package persistence.base.tree;

import java.util.*;

public class BinaryTree<TK, TV> implements Iterable<Map.Entry<TK, TV>> {
    private Node<TK, TV> root; // Корневой узел дерева

    // Метод для поиска узла с заданным ключом
    public Node<TK, TV> find(TK key) {
        boolean isFound = false; // Флаг для определения, найден ли узел
        Node<TK, TV> temp = getRoot(); // Временный указатель на текущий узел
        Node<TK, TV> item = null; // Найденный узел (если есть)
        int hash = key.hashCode(); // Хеш ключа для сравнения

        // Цикл поиска узла
        while (!isFound) {
            if (temp == null) { // Если узел пуст, заканчиваем поиск
                break;
            }

            // Сравнение хеша текущего узла с искомым хешем
            if (hash < temp.getHash()) {
                temp = temp.getLeft(); // Переход в левое поддерево
            } else if (hash > temp.getHash()) {
                temp = temp.getRight(); // Переход в правое поддерево
            }

            if (temp != null && hash == temp.getHash()) {
                isFound = true; // Узел найден
                item = temp; // Сохраняем найденный узел
            }
        }

        return isFound ? item : null; // Возвращаем найденный узел или null
    }

    // Метод для вставки нового узла
    public void insert(TK key, TV item) {
        Node<TK, TV> node = find(key); // Проверяем, существует ли узел с таким ключом

        if (node != null) { // Если узел найден, обновляем его данные
            node.setData(item);
            return;
        }

        Node<TK, TV> newItem = new Node<>(key, item); // Создаем новый узел
        if (getRoot() == null) { // Если дерево пустое, создаем корневой узел
            setRoot(newItem);
            getRoot().setColour(Color.Black); // Корень всегда черный в красно-черном дереве
            return;
        }

        Node<TK, TV> Y = null; // Родитель нового узла
        Node<TK, TV> X = getRoot(); // Указатель на текущий узел
        while (X != null) { // Поиск позиции для нового узла
            Y = X;
            if (newItem.getHash() < X.getHash()) {
                X = X.getLeft(); // Переход в левое поддерево
            } else {
                X = X.getRight(); // Переход в правое поддерево
            }
        }

        // Устанавливаем родителя и размещаем узел
        newItem.setParent(Y);
        if (newItem.getHash() < Y.getHash()) {
            Y.setLeft(newItem);
        } else {
            Y.setRight(newItem);
        }

        newItem.setLeft(null);
        newItem.setRight(null);
        newItem.setColour(Color.Red);

        // Исправляем возможные нарушения свойств красно-черного дерева
        insertFixUp(newItem);
    }

    // Метод для поиска ближайшего меньшего элемента
    public TV findNearestLess(TK key) {
        int hashedKey = key.hashCode();
        Node<TK, TV> node = this.getRoot(); // Начинаем с корня
        Node<TK, TV> optimalNode = null; // Узел с ближайшим меньшим значением

        while (node != null) {
            if (node.getHash() <= hashedKey &&
                    (optimalNode == null || hashedKey - optimalNode.getHash() > hashedKey - node.getHash())) {
                optimalNode = node; // Обновляем оптимальный узел
            }

            // Переходим в левое или правое поддерево в зависимости от значения
            node = node.getHash() > hashedKey ? node.getLeft() : node.getRight();
        }

        return optimalNode == null ? null : optimalNode.getData(); // Возвращаем данные найденного узла
    }

    // Левый поворот вокруг узла X
    private void leftRotate(Node<TK, TV> X) {
        Node<TK, TV> Y = X.getRight(); // Устанавливаем Y как правого потомка X
        X.setRight(Y.getLeft()); // Перемещаем левое поддерево Y в правое поддерево X

        if (Y.getLeft() != null) {
            Y.getLeft().setParent(X);
        }

        Y.setParent(X.getParent()); // Устанавливаем родителя Y

        if (X.getParent() == null) {
            setRoot(Y); // Если X корень, то теперь корень — Y
        } else if (X == X.getParent().getLeft()) {
            X.getParent().setLeft(Y); // Если X левый потомок, обновляем ссылку
        } else {
            X.getParent().setRight(Y); // Если X правый потомок, обновляем ссылку
        }

        Y.setLeft(X); // Устанавливаем X как левого потомка Y
        X.setParent(Y);
    }

    // Правый поворот вокруг узла Y (аналогично левому)
    private void rightRotate(Node<TK, TV> Y) {
        Node<TK, TV> X = Y.getLeft();
        Y.setLeft(X.getRight());

        if (X.getRight() != null) {
            X.getRight().setParent(Y);
        }

        X.setParent(Y.getParent());

        if (Y.getParent() == null) {
            setRoot(X);
        } else if (Y == Y.getParent().getRight()) {
            Y.getParent().setRight(X);
        } else {
            Y.getParent().setLeft(X);
        }

        X.setRight(Y);
        Y.setParent(X);
    }

    // Исправление дерева после вставки узла
    private void insertFixUp(Node<TK, TV> item) {
        while (item != getRoot() && item.getParent().getColour() == Color.Red) {
            if (item.getParent() == item.getParent().getParent().getLeft()) {
                Node<TK, TV> Y = item.getParent().getParent().getRight();
                if (Y != null && Y.getColour() == Color.Red) { // Случай 1: дядя красный
                    item.getParent().setColour(Color.Black);
                    Y.setColour(Color.Black);
                    item.getParent().getParent().setColour(Color.Red);
                    item = item.getParent().getParent();
                } else {
                    if (item == item.getParent().getRight()) { // Случай 2: узел справа
                        item = item.getParent();
                        leftRotate(item);
                    }

                    // Случай 3: перекраска и поворот
                    item.getParent().setColour(Color.Black);
                    item.getParent().getParent().setColour(Color.Red);
                    rightRotate(item.getParent().getParent());
                }
            } else {
                Node<TK, TV> X = item.getParent().getParent().getLeft();
                if (X != null && X.getColour() == Color.Red) {
                    item.getParent().setColour(Color.Black);
                    X.setColour(Color.Black);
                    item.getParent().getParent().setColour(Color.Red);
                    item = item.getParent().getParent();
                } else {
                    if (item == item.getParent().getLeft()) {
                        item = item.getParent();
                        rightRotate(item);
                    }

                    item.getParent().setColour(Color.Black);
                    item.getParent().getParent().setColour(Color.Red);
                    leftRotate(item.getParent().getParent());
                }
            }
        }

        getRoot().setColour(Color.Black); // Корень всегда черный
    }

    // Метод для получения значения по ключу
    public TV get(TK key) {
        Node<TK, TV> node = find(key);
        return node == null ? null : node.getData();
    }

    // Проверка, содержит ли дерево указанный ключ
    public boolean contains(TK key) {
        return find(key) != null;
    }

    // Преобразование дерева в список
    public List<Map.Entry<TK, TV>> toList() {
        ArrayList<Map.Entry<TK, TV>> res = new ArrayList<>();
        addToList(res, getRoot()); // Рекурсивное добавление узлов в список
        return res;
    }

    // Рекурсивное добавление узлов в коллекцию
    private void addToList(Collection<Map.Entry<TK, TV>> list, Node<TK, TV> node) {
        if (node == null) {
            return;
        }

        addToList(list, node.getLeft()); // Добавляем левое поддерево
        list.add(new AbstractMap.SimpleEntry<>(node.getKey(), node.getData())); // Добавляем текущий узел
        addToList(list, node.getRight()); // Добавляем правое поддерево
    }

    // Итератор для обхода дерева
    public Iterator<Map.Entry<TK, TV>> iterator() {
        return toList().iterator();
    }

    public Node<TK, TV> getRoot() {
        return root;
    }

    public void setRoot(Node<TK, TV> root) {
        this.root = root;
    }
}

