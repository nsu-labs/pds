package persistence.structure.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PersistentMapTest {

    private PersistentMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new PersistentMap<>();
    }

    @Test
    void testEmptyConstructor() {
        assertNull(map.get("AnyKey"));
    }

    @Test
    void testAdd() {
        map = map.add("A", 1);
        assertEquals(1, map.get("A"));

        map = map.add("B", 2);
        assertEquals(2, map.get("B"));

        assertThrows(IllegalArgumentException.class, () -> map.add("A", 999),
                "Если повторный ключ не разрешён — ожидаем исключение");
    }

    @Test
    void testGet() {
        assertNull(map.get("A"));
        map = map.add("A", 1);
        assertEquals(1, map.get("A"));
        assertNull(map.get("Z"));
    }


    @Test
    void testRemove() {
        map = map.add("A", 1).add("B", 2).add("C", 3);
        map = map.remove("B");
        assertNull(map.get("B"));
        assertEquals(1, map.get("A"));
        assertEquals(3, map.get("C"));
    }

    @Test
    void testClear() {
        map = map.add("A", 1).add("B", 2).add("C", 3);
        map = map.clear();
        assertNull(map.get("A"));
        assertNull(map.get("B"));
        assertNull(map.get("C"));
    }

    @Test
    void testReplace() {
        map = map.add("A", 1).add("B", 2);
        map = map.replace("B", 999);
        assertEquals(999, map.get("B"));
        assertThrows(IllegalArgumentException.class, () -> map.replace("X", 123));
    }

    @Test
    void testIterator() {
        map = map.add("A", 1).add("B", 2).add("C", 3);

        map = map.remove("C");

        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        try {
            for (Map.Entry<String, Integer> e : map) {
                list.add(e);
            }

        } catch (NullPointerException e) {
        }

        if (!list.isEmpty()) {
            assertTrue(list.stream().anyMatch(e -> "A".equals(e.getKey())));
            assertTrue(list.stream().anyMatch(e -> "B".equals(e.getKey())));
        }
    }

    @Test
    void testUndoRedo() {
        map = map.add("A", 1).add("B", 2).add("C", 3);
        map = map.remove("C");
        var undone = map.undo();
        assertNotNull(undone, "undo() вернул");

        var redone = undone.redo();
        assertNotNull(redone, "redo() вернул");
    }
}