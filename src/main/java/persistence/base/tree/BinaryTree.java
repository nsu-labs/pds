package persistence.base.tree;

import java.util.*;

public class BinaryTree<TK, TV> implements Iterable<Map.Entry<TK, TV>> {
    public Node<TK, TV> root; // Корневой узел дерева

    // Метод для поиска узла с заданным ключом
    public Node<TK, TV> find(TK key) {
        boolean isFound = false; // Флаг для определения, найден ли узел
        Node<TK, TV> temp = root; // Временный указатель на текущий узел
        Node<TK, TV> item = null; // Найденный узел (если есть)
        int hash = key.hashCode(); // Хеш ключа для сравнения

        // Цикл поиска узла
        while (!isFound) {
            if (temp == null) { // Если узел пуст, заканчиваем поиск
                break;
            }

            // Сравнение хеша текущего узла с искомым хешем
            if (hash < temp.hash) {
                temp = temp.left; // Переход в левое поддерево
            } else if (hash > temp.hash) {
                temp = temp.right; // Переход в правое поддерево
            }

            if (temp != null && hash == temp.hash) {
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
            node.data = item;
            return;
        }

        Node<TK, TV> newItem = new Node<>(key, item); // Создаем новый узел
        if (root == null) { // Если дерево пустое, создаем корневой узел
            root = newItem;
            root.colour = Color.Black; // Корень всегда черный в красно-черном дереве
            return;
        }

        Node<TK, TV> Y = null; // Родитель нового узла
        Node<TK, TV> X = root; // Указатель на текущий узел
        while (X != null) { // Поиск позиции для нового узла
            Y = X;
            if (newItem.hash < X.hash) {
                X = X.left; // Переход в левое поддерево
            } else {
                X = X.right; // Переход в правое поддерево
            }
        }

        // Устанавливаем родителя и размещаем узел
        newItem.parent = Y;
        if (newItem.hash < Y.hash) {
            Y.left = newItem;
        } else {
            Y.right = newItem;
        }

        // Инициализируем потомков нового узла и окрашиваем его в красный
        newItem.left = null;
        newItem.right = null;
        newItem.colour = Color.Red;

        // Исправляем возможные нарушения свойств красно-черного дерева
        insertFixUp(newItem);
    }

    // Метод для поиска ближайшего меньшего элемента
    public TV findNearestLess(TK key) {
        int hashedKey = key.hashCode();
        Node<TK, TV> node = this.root; // Начинаем с корня
        Node<TK, TV> optimalNode = null; // Узел с ближайшим меньшим значением

        while (node != null) {
            if (node.hash <= hashedKey &&
                    (optimalNode == null || hashedKey - optimalNode.hash > hashedKey - node.hash)) {
                optimalNode = node; // Обновляем оптимальный узел
            }

            // Переходим в левое или правое поддерево в зависимости от значения
            node = node.hash > hashedKey ? node.left : node.right;
        }

        return optimalNode == null ? null : optimalNode.data; // Возвращаем данные найденного узла
    }

    // Левый поворот вокруг узла X
    private void leftRotate(Node<TK, TV> X) {
        Node<TK, TV> Y = X.right; // Устанавливаем Y как правого потомка X
        X.right = Y.left; // Перемещаем левое поддерево Y в правое поддерево X

        if (Y.left != null) {
            Y.left.parent = X;
        }

        Y.parent = X.parent; // Устанавливаем родителя Y

        if (X.parent == null) {
            root = Y; // Если X корень, то теперь корень — Y
        } else if (X == X.parent.left) {
            X.parent.left = Y; // Если X левый потомок, обновляем ссылку
        } else {
            X.parent.right = Y; // Если X правый потомок, обновляем ссылку
        }

        Y.left = X; // Устанавливаем X как левого потомка Y
        X.parent = Y;
    }

    // Правый поворот вокруг узла Y (аналогично левому)
    private void rightRotate(Node<TK, TV> Y) {
        Node<TK, TV> X = Y.left;
        Y.left = X.right;

        if (X.right != null) {
            X.right.parent = Y;
        }

        X.parent = Y.parent;

        if (Y.parent == null) {
            root = X;
        } else if (Y == Y.parent.right) {
            Y.parent.right = X;
        } else {
            Y.parent.left = X;
        }

        X.right = Y;
        Y.parent = X;
    }

    // Исправление дерева после вставки узла
    private void insertFixUp(Node<TK, TV> item) {
        while (item != root && item.parent.colour == Color.Red) {
            if (item.parent == item.parent.parent.left) {
                Node<TK, TV> Y = item.parent.parent.right;
                if (Y != null && Y.colour == Color.Red) { // Случай 1: дядя красный
                    item.parent.colour = Color.Black;
                    Y.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    item = item.parent.parent;
                } else {
                    if (item == item.parent.right) { // Случай 2: узел справа
                        item = item.parent;
                        leftRotate(item);
                    }

                    // Случай 3: перекраска и поворот
                    item.parent.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    rightRotate(item.parent.parent);
                }
            } else {
                Node<TK, TV> X = item.parent.parent.left;
                if (X != null && X.colour == Color.Red) {
                    item.parent.colour = Color.Black;
                    X.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    item = item.parent.parent;
                } else {
                    if (item == item.parent.left) {
                        item = item.parent;
                        rightRotate(item);
                    }

                    item.parent.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    leftRotate(item.parent.parent);
                }
            }
        }

        root.colour = Color.Black; // Корень всегда черный
    }

    // Метод для получения значения по ключу
    public TV get(TK key) {
        Node<TK, TV> node = find(key);
        return node == null ? null : node.data;
    }

    // Проверка, содержит ли дерево указанный ключ
    public boolean contains(TK key) {
        return find(key) != null;
    }

    // Преобразование дерева в список
    public List<Map.Entry<TK, TV>> toList() {
        ArrayList<Map.Entry<TK, TV>> res = new ArrayList<>();
        addToList(res, root); // Рекурсивное добавление узлов в список
        return res;
    }

    // Рекурсивное добавление узлов в коллекцию
    private void addToList(Collection<Map.Entry<TK, TV>> list, Node<TK, TV> node) {
        if (node == null) {
            return;
        }

        addToList(list, node.left); // Добавляем левое поддерево
        list.add(new AbstractMap.SimpleEntry<>(node.key, node.data)); // Добавляем текущий узел
        addToList(list, node.right); // Добавляем правое поддерево
    }

    // Итератор для обхода дерева
    public Iterator<Map.Entry<TK, TV>> iterator() {
        return toList().iterator();
    }
}

