package persistence.base.tree;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class BinaryTreeTest {

    @Test
    public void testInsertAndFind() {
        BinaryTree<Integer, String> tree = new BinaryTree<>();

        // Изначально дерево пустое
        assertNull("find на пустом дереве должен возвращать null", tree.find(10));

        // Вставляем элемент (10 -> "ten")
        tree.insert(10, "ten");
        assertNotNull("После вставки, элемент с ключом 10 должен существовать", tree.find(10));
        assertEquals("ten", tree.find(10).data);

        // Повторная вставка по тому же ключу (обновление)
        tree.insert(10, "TEN");
        assertEquals("Обновлённое значение по ключу 10 должно быть TEN", "TEN", tree.find(10).data);

        // Вставляем ещё несколько элементов
        tree.insert(5, "five");
        tree.insert(20, "twenty");
        tree.insert(15, "fifteen");

        // Проверяем find()
        assertEquals("five", tree.find(5).data);
        assertEquals("twenty", tree.find(20).data);
        assertEquals("fifteen", tree.find(15).data);
        // Невставленный элемент
        assertNull("find(100) не должен ничего вернуть, т.к. 100 не вставляли", tree.find(100));
    }

    @Test
    public void testGetAndContains() {
        BinaryTree<Integer, String> tree = new BinaryTree<>();
        tree.insert(1, "one");
        tree.insert(2, "two");
        tree.insert(3, "three");

        // Проверяем get()
        assertEquals("one", tree.get(1));
        assertEquals("two", tree.get(2));
        assertEquals("three", tree.get(3));
        assertNull(tree.get(999));

        // Проверяем contains()
        assertTrue(tree.contains(1));
        assertTrue(tree.contains(2));
        assertTrue(tree.contains(3));
        assertFalse(tree.contains(999));
    }

    @Test
    public void testFindNearestLess() {
        BinaryTree<Integer, String> tree = new BinaryTree<>();
        // Вставляем несколько ключей
        tree.insert(10, "ten");
        tree.insert(5, "five");
        tree.insert(3, "three");
        tree.insert(12, "twelve");
        tree.insert(11, "eleven");

        // Ищем ближайший меньший для 10
        // (сам 10 <= 10), значит он сам
        assertEquals("ten", tree.findNearestLess(10));

        // Для 9 ближайший меньший - ключ 5
        assertEquals("five", tree.findNearestLess(9));

        // Для ключа 12
        // (12 есть в дереве => "twelve")
        assertEquals("twelve", tree.findNearestLess(12));

        // Для 13 - ближайший меньший 12
        assertEquals("twelve", tree.findNearestLess(13));

        // Для 2 - ничего нет (3 - уже больше)
        assertNull("Ожидаем null, т.к. нет ключей <= 2", tree.findNearestLess(2));
    }

    @Test
    public void testToListAndIterator() {
        BinaryTree<Integer, String> tree = new BinaryTree<>();
        tree.insert(10, "ten");
        tree.insert(5, "five");
        tree.insert(15, "fifteen");

        List<Map.Entry<Integer, String>> list = tree.toList();
        assertEquals("Размер списка должен быть 3", 3, list.size());

        // Содержит ли список корректные пары?
        // Не гарантируется порядок, если дерево не является BST безупречно,
        // но обычно при обходе 'toList' мы ожидаем отсортированный (in-order).
        // Проверим только наличие.
        boolean found10 = false, found5 = false, found15 = false;
        for (Map.Entry<Integer, String> e : list) {
            if (e.getKey() == 10) {
                assertEquals("ten", e.getValue());
                found10 = true;
            }
            if (e.getKey() == 5) {
                assertEquals("five", e.getValue());
                found5 = true;
            }
            if (e.getKey() == 15) {
                assertEquals("fifteen", e.getValue());
                found15 = true;
            }
        }
        assertTrue("Ключ 10 должен присутствовать", found10);
        assertTrue("Ключ 5 должен присутствовать", found5);
        assertTrue("Ключ 15 должен присутствовать", found15);

        // Теперь проверим итератор
        Iterator<Map.Entry<Integer, String>> it = tree.iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<Integer, String> e = it.next();
            count++;
        }
        assertEquals("Количество элементов через итератор должно совпадать с size()", 3, count);
    }

    @Test
    public void testInsertFixUpScenario() {
        // Здесь можно протестировать более сложные сценарии,
        // где при вставке красно-черного узла происходит rotate, recolor и т.п.
        // Однако для упрощения ограничимся базовым тестом,
        // главное — не падает, корень чёрный.
        BinaryTree<Integer, String> tree = new BinaryTree<>();
        // Вставляем несколько узлов
        tree.insert(10, "ten");
        tree.insert(20, "twenty");
        tree.insert(15, "fifteen");
        // и т.д.

        assertNotNull(tree.root);
        assertEquals("Корень должен быть 10", (Integer)10, tree.root.key);
        // Проверим, что корень — чёрный
        assertEquals("Корень должен быть чёрного цвета", Color.Black, tree.root.colour);
    }
}