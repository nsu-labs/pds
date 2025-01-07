package persistence.structure.array;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import persistence.base.ModificationCount;
import persistence.base.PersistentContent;
import persistence.base.tree.BinaryTree;
import persistence.structure.list.PersistentLinkedList;

public class PersistentArrayTest {

    @Test
    public void testDefaultConstructor() {
        // Проверяем, что по умолчанию массив пуст
        PersistentArray<String> arr = new PersistentArray<>();
        assertEquals("По умолчанию count должен быть 0", 0, arr.count);
        assertEquals("modificationCount должен быть 0", 0, arr.modificationCount);
        assertNotNull("nodes не должен быть null", arr.nodes);
        assertNotNull("nodes.content должен существовать", arr.nodes.content);
        assertTrue("Список узлов должен быть пустым", arr.nodes.content.isEmpty());
    }

    @Test
    public void testAddAndGet() {
        PersistentArray<Integer> arr = new PersistentArray<>();
        // Добавим несколько элементов
        arr = arr.add(10);
        arr = arr.add(20);
        arr = arr.add(30);

        // Проверим count и modificationCount
        assertEquals("После трёх добавлений count = 3", 3, arr.count);
        assertEquals("modificationCount должен быть 3", 3, arr.modificationCount);

        // Проверим get
        assertEquals((Integer)10, arr.get(0));
        assertEquals((Integer)20, arr.get(1));
        assertEquals((Integer)30, arr.get(2));

        // Проверим, что при выходе за границы бросается исключение
        try {
            arr.get(-1);
            fail("Ожидаем IndexOutOfBoundsException при get(-1)");
        } catch (IndexOutOfBoundsException e) {
            // Ожидаемо
        }

        try {
            arr.get(3);
            fail("Ожидаем IndexOutOfBoundsException при get(3)");
        } catch (IndexOutOfBoundsException e) {
            // Ожидаемо
        }
    }

    @Test
    public void testInsert() {
        PersistentArray<String> arr = new PersistentArray<>();
        // Добавим один элемент
        arr = arr.add("A"); // now size=1
        arr = arr.add("B"); // size=2
        arr = arr.add("C"); // size=3

        // Вставим элемент посередине (индекс=1)
        arr = arr.insert(1, "X");

        assertEquals("После вставки count=4", 4, arr.count);
        assertEquals("A", arr.get(0));
        assertEquals("X", arr.get(1));
        assertEquals("B", arr.get(2));
        assertEquals("C", arr.get(3));

        // Вставим элемент в конец (индекс=4)
        arr = arr.insert(4, "Z");
        assertEquals(5, arr.count);
        assertEquals("Z", arr.get(4));

        // Вставка за границами
        try {
            arr.insert(999, "???");
            fail("Ожидаем IndexOutOfBoundsException при insert за границами");
        } catch (IndexOutOfBoundsException e) {
            // Ок
        }
    }

    @Test
    public void testReplace() {
        PersistentArray<Integer> arr = new PersistentArray<>();
        arr = arr.add(1).add(2).add(3);

        // Заменим элемент по индексу 1 (второй элемент)
        arr = arr.replace(1, 20);
        assertEquals((Integer)20, arr.get(1));
        assertEquals(3, arr.count);

        // Заменим элемент по индексу 0
        arr = arr.replace(0, 100);
        assertEquals((Integer)100, arr.get(0));

        // Выход за границы
        try {
            arr.replace(999, 999);
            fail("Ожидаем IndexOutOfBoundsException при replace за границами");
        } catch (IndexOutOfBoundsException e) {
            // Ок
        }
    }

    @Test
    public void testRemove() {
        PersistentArray<String> arr = new PersistentArray<>();
        arr = arr.add("A").add("B").add("C").add("D");

        // Удалим элемент по индексу 1: убираем "B"
        arr = arr.remove(1);
        assertEquals(3, arr.count);
        assertEquals("A", arr.get(0));
        assertEquals("C", arr.get(1));
        assertEquals("D", arr.get(2));

        // Удалим элемент по индексу 2 (был "D")
        arr = arr.remove(2);
        assertEquals(2, arr.count);
        assertEquals("A", arr.get(0));
        assertEquals("C", arr.get(1));

        // Удалим элемент 0 (теперь "A")
        arr = arr.remove(0);
        assertEquals(1, arr.count);
        assertEquals("C", arr.get(0));

        // Выход за границы
        try {
            arr.remove(5);
            fail("remove за границами");
        } catch (IndexOutOfBoundsException e) {
            // Ок
        }
    }

    @Test
    public void testClearAll() {
        PersistentArray<String> arr = new PersistentArray<>();
        arr = arr.add("A").add("B").add("C");

        arr = arr.clearAll();
        // После очистки count=0
        assertEquals(0, arr.count);
        // get(0) => IndexOutOfBoundsException
        try {
            arr.get(0);
            fail("Ожидаем IndexOutOfBoundsException, т.к. массив пуст");
        } catch (IndexOutOfBoundsException e) {
            // Ок
        }
    }

    @Test
    public void testIterator() {
        PersistentArray<Integer> arr = new PersistentArray<>();
        arr = arr.add(1).add(2).add(3);

        Iterator<Integer> it = arr.iterator();
        assertTrue(it.hasNext());
        assertEquals((Integer)1, it.next());
        assertEquals((Integer)2, it.next());
        assertEquals((Integer)3, it.next());
        assertFalse(it.hasNext());
        // Если вызвать next() ещё раз, должно быть исключение
        try {
            it.next();
            fail("Ожидаем NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Ок
        }
    }

    @Test
    public void testUndoRedo() {
        PersistentArray<String> arr = new PersistentArray<>();
        // modificationCount=0, count=0

        arr = arr.add("A"); // now count=1, modificationCount=1
        arr = arr.add("B"); // now count=2, modificationCount=2
        arr = arr.add("C"); // now count=3, modificationCount=3

        // Undo
        arr = arr.undo(); // now modificationCount=2
        // При этом count пересчитывается: recalculateCount(2)
        // Значит массив должен содержать 2 элемента ("A", "B")
        assertEquals(2, arr.count);
        assertEquals("A", arr.get(0));
        assertEquals("B", arr.get(1));

        // Еще раз undo
        arr = arr.undo(); // modificationCount=1
        assertEquals(1, arr.count);
        assertEquals("A", arr.get(0));

        // Еще раз undo - дойдём до startModificationCount (вероятно 0)
        arr = arr.undo();
        // Теперь, если startModificationCount=0, мы в самом начале
        assertEquals(0, arr.count);

        // redo
        arr = arr.redo(); // modificationCount=1
        // Должно быть 1 элемент
        assertEquals(1, arr.count);
        assertEquals("A", arr.get(0));

        // Ещё раз redo => modificationCount=2
        arr = arr.redo();
        assertEquals(2, arr.count);

        // Ещё раз redo => modificationCount=3
        arr = arr.redo();
        assertEquals(3, arr.count);
        // Три элемента: A, B, C
        assertEquals("C", arr.get(2));

        // Попробуем ещё раз redo - ничего не изменится, так как мы на maxModification
        PersistentArray<String> arr2 = arr.redo();
        assertSame("redo без изменений возвращает тот же объект", arr, arr2);
    }
}