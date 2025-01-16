package persistence.base.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BinaryTreeTest {

    private BinaryTree<Integer, String> tree;

    @BeforeEach
    void setUp() {
        tree = new BinaryTree<>();
    }

    /**
     * Тест на добавление одного элемента в дерево и проверку get.
     */
    @Test
    void testInsertSingleNodeAndGet() {
        tree.insert(10, "value10");
        assertEquals("value10", tree.get(10),
                "После вставки ключа 10 должен вернуться 'value10'");
        assertNotNull(tree.getRoot(), "Корень дерева не должен быть null");
        assertEquals(Color.Black, tree.getRoot().getColour(), "Корень после вставки должен быть чёрным");
    }

    /**
     * Тест на добавление нескольких элементов и проверку поиска.
     */
    @Test
    void testInsertMultipleNodesAndFind() {
        tree.insert(10, "value10");
        tree.insert(5, "value5");
        tree.insert(15, "value15");

        // Проверяем, что все ключи нашлись
        assertNotNull(tree.find(10), "Ключ 10 должен присутствовать в дереве");
        assertNotNull(tree.find(5),  "Ключ 5 должен присутствовать в дереве");
        assertNotNull(tree.find(15), "Ключ 15 должен присутствовать в дереве");

        // Проверяем, что поиска несуществующих ключей возвращает null
        assertNull(tree.find(999),   "Ключ 999 не должен присутствовать в дереве");
    }

    /**
     * Тест на обновление значения у уже существующего ключа.
     */
    @Test
    void testInsertDuplicateKey() {
        tree.insert(10, "value10");
        tree.insert(10, "newValue10"); // Повторно вставляем с тем же ключом

        assertEquals("newValue10", tree.get(10),
                "При повторной вставке значение должно обновиться");
        assertEquals(1, tree.toList().size(),
                "Должен существовать ровно один узел, так как ключ 10 был один");
    }

    /**
     * Тест на метод contains.
     */
    @Test
    void testContains() {
        tree.insert(10, "value10");
        tree.insert(5, "value5");
        tree.insert(15, "value15");

        assertTrue(tree.contains(10), "Дерево должно содержать ключ 10");
        assertTrue(tree.contains(5),  "Дерево должно содержать ключ 5");
        assertTrue(tree.contains(15), "Дерево должно содержать ключ 15");
        assertFalse(tree.contains(999), "Ключ 999 не должен содержаться в дереве");
    }

    /**
     * Тест на метод findNearestLess.
     */
    @Test
    void testFindNearestLess() {
        tree.insert(10, "value10");
        tree.insert(5,  "value5");
        tree.insert(15, "value15");
        tree.insert(7,  "value7");


        String nearest = tree.findNearestLess(8);
        assertEquals("value7", nearest, "Ближайший меньший или равный ключ для 8 это 7");


        nearest = tree.findNearestLess(5);
        assertEquals("value5", nearest, "Ближайший меньший или равный ключ для 5 это 5 же");


        nearest = tree.findNearestLess(2);
        assertNull(nearest, "Если запрошенное число меньше минимального ключа, возвращается null");


        nearest = tree.findNearestLess(14);
        assertEquals("value10", nearest,
                "Ближайший меньший или равный ключ для 14 это 10");


        nearest = tree.findNearestLess(999);
        assertEquals("value15", nearest,
                "Если число больше всех ключей, возвращаем значение для ключа 15");
    }

    /**
     * Тест на метод toList и корректный порядок (in-order).
     */
    @Test
    void testToList() {
        tree.insert(10, "value10");
        tree.insert(5,  "value5");
        tree.insert(15, "value15");
        tree.insert(7,  "value7");
        tree.insert(3,  "value3");

        List<Map.Entry<Integer, String>> list = tree.toList();

        // Проверим, что в списке 5 элементов
        assertEquals(5, list.size(), "Должно быть 5 элементов в списке");

        // Список должен быть отсортирован по возрастанию ключей (in-order обход)
        // Ключи: 3, 5, 7, 10, 15
        assertEquals(3,  list.get(0).getKey());
        assertEquals(5,  list.get(1).getKey());
        assertEquals(7,  list.get(2).getKey());
        assertEquals(10, list.get(3).getKey());
        assertEquals(15, list.get(4).getKey());
    }

    /**
     * Тест на работу итератора.
     * Аналогичен тесту toList, но проверяем итерацию напрямую.
     */
    @Test
    void testIterator() {
        tree.insert(10, "value10");
        tree.insert(5,  "value5");
        tree.insert(15, "value15");

        int count = 0;
        Integer[] expectedKeys = {5, 10, 15};
        String[] expectedValues = {"value5", "value10", "value15"};

        for (Map.Entry<Integer, String> entry : tree) {
            assertEquals(expectedKeys[count], entry.getKey());
            assertEquals(expectedValues[count], entry.getValue());
            count++;
        }
        assertEquals(3, count, " / тератор должен был вернуть ровно 3 элемента");
    }
}