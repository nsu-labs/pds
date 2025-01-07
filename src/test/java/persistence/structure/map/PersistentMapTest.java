package persistence.structure.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import persistence.base.tree.BinaryTree;
import persistence.base.PersistentNode;
import persistence.base.ModificationCount;
import persistence.base.PersistentContent;

public class PersistentMapTest {

    @Test
    public void testDefaultConstructor() {
        // Создаем пустую карту
        PersistentMap<Integer, String> map = new PersistentMap<>();
        // По умолчанию count = 0, modificationCount = 0
        assertEquals("Должно быть 0 элементов", 0, map.count);
        assertEquals("modificationCount должен быть 0", 0, map.modificationCount);

        // Проверяем, что nodes не null
        assertNotNull("nodes не должно быть null", map.nodes);
        assertNotNull("nodes.content не должно быть null", map.nodes.content);
        assertTrue("BinaryTree изначально пустое", map.nodes.content.toList().isEmpty());
    }

    @Test
    public void testAddAndGet() {
        PersistentMap<Integer, String> map = new PersistentMap<>();
        // Добавим несколько ключей
        map = map.add(10, "ten");
        map = map.add(20, "twenty");
        map = map.add(5, "five");

        // Проверим count
        assertEquals("Должно быть 3 элемента", 3, map.count);
        // modificationCount = 3
        assertEquals(3, map.modificationCount);

        // Проверяем get
        assertEquals("ten", map.get(10));
        assertEquals("twenty", map.get(20));
        assertEquals("five", map.get(5));
        // Не существующий ключ -> null
        assertNull(map.get(999));
    }

    @Test
    public void testAddSameKeyShouldFail() {
        PersistentMap<Integer, String> map = new PersistentMap<>();
        map = map.add(1, "one");

        try {
            // Попытка добавить тот же ключ -> выбрасывается IllegalArgumentException
            map = map.add(1, "ONE");
            fail("Ожидаем IllegalArgumentException при добавлении существующего ключа");
        } catch (IllegalArgumentException e) {
            // ОК
        }

        // Убедимся, что карта не изменилась
        assertEquals(1, map.count);
        assertEquals("one", map.get(1));
    }

    @Test
    public void testRemove() {
        PersistentMap<Integer, String> map = new PersistentMap<>();
        map = map.add(10, "ten");
        map = map.add(20, "twenty");

        // Удаляем существующий ключ
        map = map.remove(10);
        // Теперь count=1
        assertEquals(1, map.count);
        assertNull("Ключ 10 удалён", map.get(10));
        assertEquals("twenty", map.get(20));

        // Удалим несуществующий ключ -> не изменяется
        map = map.remove(999);
        assertEquals(1, map.count);
        assertEquals("twenty", map.get(20));
    }

    @Test
    public void testReplace() {
        PersistentMap<String, Integer> map = new PersistentMap<>();
        map = map.add("A", 1);
        map = map.add("B", 2);
        // Замена существующего ключа
        map = map.replace("B", 22);

        assertEquals((Integer)1, map.get("A"));
        assertEquals((Integer)22, map.get("B"));

        // Попытка заменить несуществующий ключ -> Exception
        try {
            map = map.replace("C", 333);
            fail("Ожидаем IllegalArgumentException для несуществующего ключа");
        } catch (IllegalArgumentException e) {
            // ОК
        }
    }

    @Test
    public void testClear() {
        PersistentMap<Integer, String> map = new PersistentMap<>();
        map = map.add(1, "one").add(2, "two").add(3, "three");

        map = map.clear();
        assertEquals("После clear() нет элементов", 0, map.count);
        assertNull(map.get(1));
        assertNull(map.get(2));
        assertNull(map.get(3));

        // Повторный clear не ломает
        map = map.clear();
        assertEquals(0, map.count);
    }

    @Test
    public void testKeySetAndValueSet() {
        PersistentMap<Integer, String> map = new PersistentMap<>();
        map = map.add(10, "ten");
        map = map.add(20, "twenty");
        map = map.add(30, "thirty");

        Set<Integer> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains(10));
        assertTrue(keys.contains(20));
        assertTrue(keys.contains(30));

        Set<String> values = map.valueSet();
        assertEquals(3, values.size());
        assertTrue(values.contains("ten"));
        assertTrue(values.contains("twenty"));
        assertTrue(values.contains("thirty"));
    }

    @Test
    public void testIteration() {
        PersistentMap<Integer, String> map = new PersistentMap<>();
        map = map.add(1, "one").add(2, "two").add(3, "three");

        Iterator<Map.Entry<Integer, String>> it = map.iterator();
        HashMap<Integer, String> found = new HashMap<>();

        while(it.hasNext()){
            Map.Entry<Integer, String> e = it.next();
            found.put(e.getKey(), e.getValue());
        }

        assertEquals("Должно быть 3 найденных элемента", 3, found.size());
        assertEquals("one", found.get(1));
        assertEquals("two", found.get(2));
        assertEquals("three", found.get(3));
    }

    @Test
    public void testUndoRedo() {
        // Проверим, что undo/redo работает
        PersistentMap<Integer, String> map = new PersistentMap<>();
        // step=0, count=0

        map = map.add(10, "ten");    // step=1, count=1
        map = map.add(20, "twenty"); // step=2, count=2
        map = map.add(30, "thirty"); // step=3, count=3

        // undo => step=2, пересчитываем count=2
        map = map.undo();
        assertEquals(2, map.count);
        assertNull("Ключ 30 уже не должен быть доступен", map.get(30));
        assertEquals("twenty", map.get(20));

        // undo => step=1, count=1
        map = map.undo();
        assertEquals(1, map.count);
        assertNull(map.get(20));
        assertEquals("ten", map.get(10));

        // undo => step=0, count=0
        map = map.undo();
        assertEquals(0, map.count);

        // redo => step=1 => count=1
        map = map.redo();
        assertEquals(1, map.count);
        assertEquals("ten", map.get(10));

        // redo => step=2 => count=2
        map = map.redo();
        assertEquals(2, map.count);
        assertEquals("twenty", map.get(20));

        // redo => step=3 => count=3
        map = map.redo();
        assertEquals(3, map.count);
        assertEquals("thirty", map.get(30));

        // повторный redo => уже на макс. шаге => не меняется
        PersistentMap<Integer, String> same = map.redo();
        assertSame(map, same);
    }
}