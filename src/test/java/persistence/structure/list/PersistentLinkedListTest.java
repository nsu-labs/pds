package persistence.structure.list;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import persistence.base.ModificationCount;
import persistence.base.PersistentNode;

public class PersistentLinkedListTest {

    @Test
    public void testDefaultConstructor() {
        // Создаём пустой список
        PersistentLinkedList<String> list = new PersistentLinkedList<>();

        // Проверяем, что по умолчанию count=0
        assertEquals("По умолчанию список должен быть пустым", 0, list.size());

        // Проверяем, что pseudoHead/pseudoTail созданы
        assertNotNull("nodes не должно быть null", list.nodes);
        assertNotNull("nodes.content не должно быть null", list.nodes.content);
        assertNotNull("pseudoHead не должно быть null", list.nodes.content.pseudoHead);
        assertNotNull("pseudoTail не должно быть null", list.nodes.content.pseudoTail);
    }

    @Test
    public void testAddLast() {
        PersistentLinkedList<Integer> list = new PersistentLinkedList<>();

        // Добавим несколько элементов
        list = list.addLast(10);
        list = list.addLast(20);
        list = list.addLast(30);

        // Проверим количество
        assertEquals(3, list.size());

        // Проверим get(0..2)
        assertEquals((Integer)10, list.get(0));
        assertEquals((Integer)20, list.get(1));
        assertEquals((Integer)30, list.get(2));

        // Выход за границы
        try {
            list.get(-1);
            fail("Ожидаем IndexOutOfBoundsException при get(-1)");
        } catch (IndexOutOfBoundsException e) {
            // ОК
        }

        try {
            list.get(999);
            fail("Ожидаем IndexOutOfBoundsException при get(999)");
        } catch (IndexOutOfBoundsException e) {
            // ОК
        }
    }

    @Test
    public void testAddFirst() {
        PersistentLinkedList<String> list = new PersistentLinkedList<>();
        // Добавим в начало три элемента
        list = list.addFirst("C");
        list = list.addFirst("B");
        list = list.addFirst("A");

        // Проверим порядок
        // А — в начале (индекс 0)
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    public void testRemoveFirst() {
        PersistentLinkedList<String> list = new PersistentLinkedList<>();
        list = list.addLast("A").addLast("B").addLast("C");

        // Удалим первый элемент
        list = list.removeFirst();
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));

        // Удалим ещё раз
        list = list.removeFirst();
        assertEquals(1, list.size());
        assertEquals("C", list.get(0));

        // Удалим ещё раз => пустой
        list = list.removeFirst();
        assertEquals(0, list.size());

        // Повторная попытка удалить => должен вернуться тот же список (или пустой)
        list = list.removeFirst();
        assertEquals(0, list.size());
    }

    @Test
    public void testRemoveLast() {
        PersistentLinkedList<Integer> list = new PersistentLinkedList<>();
        list = list.addLast(1).addLast(2).addLast(3);

        // Удалим последний элемент
        list = list.removeLast();
        assertEquals(2, list.size());
        assertEquals((Integer)1, list.get(0));
        assertEquals((Integer)2, list.get(1));

        // Удалим ещё раз => size=1
        list = list.removeLast();
        assertEquals(1, list.size());
        assertEquals((Integer)1, list.get(0));

        // Удалим ещё => size=0
        list = list.removeLast();
        assertEquals(0, list.size());

        // Повторное удаление ничего не ломает
        list = list.removeLast();
        assertEquals(0, list.size());
    }

    @Test
    public void testReplace() {
        PersistentLinkedList<String> list = new PersistentLinkedList<>();
        list = list.addLast("A").addLast("B").addLast("C");

        // Заменим элемент по индексу 1
        list = list.replace(1, "BBB");
        assertEquals("A", list.get(0));
        assertEquals("BBB", list.get(1));
        assertEquals("C", list.get(2));

        // Выход за границы
        list = list.replace(99, "???");
        // По реализации, если index>count, "return this"
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
    }

    @Test
    public void testContains() {
        PersistentLinkedList<String> list = new PersistentLinkedList<>();
        list = list.addLast("X").addLast("Y").addLast("Z");

        assertTrue(list.contains("X"));
        assertTrue(list.contains("Y"));
        assertTrue(list.contains("Z"));
        assertFalse(list.contains("W"));
    }

    @Test
    public void testClear() {
        PersistentLinkedList<String> list = new PersistentLinkedList<>();
        list = list.addLast("A").addLast("B").addLast("C");
        assertEquals(3, list.size());

        list = list.clear();
        assertEquals(0, list.size());

        // Проверим, что повторный clear не ломает
        list = list.clear();
        assertEquals(0, list.size());
    }

    @Test
    public void testUndoRedo() {
        // Проверим, что undo/redo корректно меняет count
        PersistentLinkedList<Integer> list = new PersistentLinkedList<>();
        // modificationCount=0 initially, count=0

        list = list.addLast(10); // now modificationCount=1, count=1
        list = list.addLast(20); // now modificationCount=2, count=2
        list = list.addLast(30); // now modificationCount=3, count=3

        // undo => modificationCount=2
        list = list.undo();
        // пересчитываем count=2
        assertEquals(2, list.size());
        // get(1)=20
        assertEquals((Integer)20, list.get(1));

        // ещё раз undo => modificationCount=1
        list = list.undo();
        assertEquals(1, list.size());
        assertEquals((Integer)10, list.get(0));

        // ещё раз undo => modificationCount=0 => empty
        list = list.undo();
        assertEquals(0, list.size());

        // redo => modificationCount=1 => size=1
        list = list.redo();
        assertEquals(1, list.size());
        assertEquals((Integer)10, list.get(0));

        // redo => modificationCount=2 => size=2
        list = list.redo();
        assertEquals(2, list.size());
        assertEquals((Integer)20, list.get(1));

        // redo => modificationCount=3 => size=3
        list = list.redo();
        assertEquals(3, list.size());
        assertEquals((Integer)30, list.get(2));

        // повторный redo => ничего не меняет, т.к. мы уже на maxModification
        PersistentLinkedList<Integer> same = list.redo();
        assertSame("Ожидаем, что redo на maxModification вернёт тот же объект", list, same);
    }

    @Test
    public void testGetOutOfBounds() {
        // Проверим, что get() выбрасывает исключение, если index <0 или index>=count
        PersistentLinkedList<String> list = new PersistentLinkedList<>();
        list = list.addLast("A").addLast("B");
        try {
            list.get(-1);
            fail("Ожидаем IndexOutOfBoundsException при get(-1)");
        } catch (IndexOutOfBoundsException e) {
            // Ожидаемо
        }
        try {
            list.get(999);
            fail("Ожидаем IndexOutOfBoundsException при get(999)");
        } catch (IndexOutOfBoundsException e) {
            // Ожидаемо
        }
    }
}