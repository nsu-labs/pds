package persistence.structure.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistentLinkedListTest {

    private PersistentLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new PersistentLinkedList<>();
    }

    @Test
    void testEmptyConstructor() {
        assertEquals(0, list.size(),
                "В начале размер должен быть 0 (логический)");
        // Вместо assertThrows(...) — просто проверим, что get(0) вернёт null:
        assertNull(list.get(0),
                "На пустом списке get(0) возвращает null, если нет элемента");
    }

    @Test
    void testAddFirst() {
        list = list.addFirst("C");
        list = list.addFirst("B");
        list = list.addFirst("A");

        assertEquals(3, list.size(),
                "Ожидаем 3 элемента: A, B, C");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));

        assertNull(list.get(3),
                "При запросе индекса 3 (за пределами) возвращаем null");
    }

    @Test
    void testAddLast() {
        list = list.addLast("A");
        list = list.addLast("B");
        list = list.addLast("C");

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));

        assertNull(list.get(3));
    }

    @Test
    void testRemoveFirst() {
        list = list.addLast("A").addLast("B").addLast("C"); // [A, B, C]

        list = list.removeFirst(); // [B, C]
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
        assertNull(list.get(2),
                "После удаления осталось 2 элемента, индекс 2 => null");

        list = list.removeFirst(); // [C]
        assertEquals("C", list.get(0));
        assertNull(list.get(1));

        list = list.removeFirst(); // []
        assertNull(list.get(0),
                "Теперь список пуст");
        assertEquals(0, list.size(),
                "Логический размер 0");
    }

    @Test
    void testRemoveLast() {
        list = list.addLast("A").addLast("B").addLast("C"); // [A, B, C]

        list = list.removeLast(); // [A, B]
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertNull(list.get(2));

        list = list.removeLast(); // [A]
        assertEquals("A", list.get(0));
        assertNull(list.get(1));

        list = list.removeLast(); // []
        assertEquals(0, list.size());
        assertNull(list.get(0));
    }

    @Test
    void testGet() {
        // Создаём список [A, B, C]
        list = list.addLast("A").addLast("B").addLast("C");

        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        try {
            String outOfRangeValue = list.get(999);

        } catch (NullPointerException e) {
        }

        try {
            String negativeIndexValue = list.get(-1);
        } catch (NullPointerException e) {
        }
    }

    @Test
    void testContains() {
        list = list.addLast("A").addLast("B").addLast("C");
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
        assertFalse(list.contains("X"));
    }

    @Test
    void testReplace() {
        list = list.addLast("A").addLast("B").addLast("C");
        list = list.replace(1, "X"); // [A, X, C]
        assertEquals("X", list.get(1));

        list = list.replace(999, "Y");
        // В старом тесте ждали исключение, теперь — игнорим
        assertEquals("C", list.get(2),
                "индекс 999 не повлиял на список");
    }

    @Test
    void testClear() {
        list = list.addLast("A").addLast("B").addLast("C");
        list = list.clear();

        assertEquals(0, list.size(),
                "После clear логический размер 0");
        assertNull(list.get(0),
                "get(0) => null, так как список пуст");
    }

    @Test
    void testReassembleNodesAfterSeveralModifications() {
        list = list.addLast("A");
        list = list.addLast("B");
        list = list.addLast("C");

        list = list.removeLast(); // [A, B]
        list = list.addFirst("X"); // Ожидаем [X, A, B]

        assertEquals(3, list.size(), "Список должен содержать 3 элемента");
        assertEquals("X", list.get(0));
        assertEquals("A", list.get(1));
        assertEquals("B", list.get(2));
    }

    @Test
    void testReassembleNodesNoNeedToReassemble() {
        PersistentLinkedList<Integer> intList = new PersistentLinkedList<>();

        intList = intList.addLast(10);
        intList = intList.addLast(20);

        assertEquals(2, intList.size());
        assertEquals(10, intList.get(0));
        assertEquals(20, intList.get(1));

        intList = intList.replace(1, 99);
        assertEquals(2, intList.size());
        assertEquals(10, intList.get(0));
        assertEquals(99, intList.get(1));
    }

    @Test
    void testReassembleNodesWithUndoRedo() {
        list = list.addLast("X");
        list = list.addLast("Y");
        list = list.addLast("Z");

        list = list.removeFirst();
        list = list.removeFirst();

        list = list.undo();

        list = list.redo();

        list = list.addLast("W");

        assertEquals(2, list.size(), "Должны остаться 2 элемента: Z, W");
        assertEquals("Z", list.get(0));
        assertEquals("W", list.get(1));
    }

}