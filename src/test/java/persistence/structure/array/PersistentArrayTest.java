package persistence.structure.array;

import org.junit.jupiter.api.Test;
import persistence.base.ModificationCount;
import persistence.base.PersistentContent;
import persistence.base.PersistentNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistentArrayTest {

    @Test
    void testEmptyConstructor() {
        // Создаём пустой массив
        PersistentArray<String> array = new PersistentArray<>();

        // Проверяем, что при итерации элементов нет
        assertFalse(array.iterator().hasNext(),
                "Только что созданный массив должен быть пустым");

        // Попытка получить элемент 0 должна бросать исключение
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0),
                "Попытка получить элемент в пустом массиве должна вызывать IndexOutOfBoundsException");
    }

    @Test
    void testAdd() {
        PersistentArray<String> array = new PersistentArray<>();

        // Добавляем первый элемент
        array = array.add("A");
        // Проверяем, что теперь get(0) = "A"
        assertEquals("A", array.get(0),
                "После добавления 'A' в пустой массив по индексу 0 должно лежать 'A'");

        // Добавляем второй элемент
        array = array.add("B");
        // Проверяем, что get(1) = "B"
        assertEquals("B", array.get(1),
                "После добавления 'B' по индексу 1 должно лежать 'B'");

        // Проверим, что get(2) выбрасывает исключение
        PersistentArray<String> finalArray = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray.get(2));
    }

    @Test
    void testInsert() {
        PersistentArray<String> array = new PersistentArray<>();
        // Для удобства — добавим сразу несколько элементов
        array = array.add("A").add("B").add("C");  // [A, B, C]

        // Вставка в начало (индекс 0)
        array = array.insert(0, "X");              // [X, A, B, C]
        assertEquals("X", array.get(0));
        assertEquals("A", array.get(1));
        assertEquals("B", array.get(2));
        assertEquals("C", array.get(3));

        // Вставка в середину (индекс 2)
        array = array.insert(2, "Y");              // [X, A, Y, B, C]
        assertEquals("X", array.get(0));
        assertEquals("A", array.get(1));
        assertEquals("Y", array.get(2));
        assertEquals("B", array.get(3));
        assertEquals("C", array.get(4));

        // Вставка в конец (индекс == count)
        array = array.insert(5, "Z");              // [X, A, Y, B, C, Z]
        assertEquals("Z", array.get(5));

        // Проверка выхода за границы
        PersistentArray<String> finalArray = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray.insert(-1, "Bad"));
        PersistentArray<String> finalArray1 = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray1.insert(999, "Bad"));
    }

    @Test
    void testReplace() {
        // Создадим массив и добавим элементы [A, B, C]
        PersistentArray<String> array = new PersistentArray<>();
        array = array.add("A").add("B").add("C");

        // Заменяем элемент по индексу 1 (B -> X)
        array = array.replace(1, "X");
        assertEquals("X", array.get(1),
                "После replace(1, 'X') элемент по индексу 1 должен быть 'X'");
        // Остальные остались без изменений
        assertEquals("A", array.get(0));
        assertEquals("C", array.get(2));

        // Проверим замену по последнему индексу (2)
        array = array.replace(2, "Z");
        assertEquals("Z", array.get(2));

        // Выход за границы
        PersistentArray<String> finalArray = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray.replace(-1, "Bad"));
        PersistentArray<String> finalArray1 = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray1.replace(999, "Bad"));
    }

    @Test
    void testRemove() {
        // Создадим массив [A, B, C, D]
        PersistentArray<String> array = new PersistentArray<>();
        array = array.add("A").add("B").add("C").add("D");

        // Удаляем элемент по индексу 1 (B)
        array = array.remove(1);  // Результат [A, C, D, null] физически, но логически мы видим [A, C, D]
        assertEquals("A", array.get(0));
        assertEquals("C", array.get(1));
        assertEquals("D", array.get(2));
        // Проверка, что следующий индекс уже выходит за границы
        PersistentArray<String> finalArray = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray.get(3));

        // Удаляем элемент по индексу 0 (A)
        array = array.remove(0);  // [C, D, null]
        assertEquals("C", array.get(0));
        assertEquals("D", array.get(1));
        PersistentArray<String> finalArray1 = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray1.get(2));

        // Удаляем последний оставшийся элемент (индекс 1 => D)
        array = array.remove(1);  // [C, null]
        assertEquals("C", array.get(0));
        // Теперь при запросе индекса 1 ожидаем OutOfBounds
        PersistentArray<String> finalArray2 = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray2.get(1));

        // Попытка удалить за границами
        PersistentArray<String> finalArray3 = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray3.remove(-1));
        PersistentArray<String> finalArray4 = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray4.remove(999));
    }

    @Test
    void testClearAll() {
        // Создадим массив [A, B, C]
        PersistentArray<String> array = new PersistentArray<>();
        array = array.add("A").add("B").add("C");

        // Очищаем массив
        array = array.clearAll();
        // Теперь при итерации не должно быть видимых элементов
        assertFalse(array.iterator().hasNext(),
                "После clearAll() массив должен быть пустым (логически)");

        // Любой индекс => OutOfBounds
        PersistentArray<String> finalArray = array;
        assertThrows(IndexOutOfBoundsException.class, () -> finalArray.get(0));
    }

    @Test
    void testIteration() {
        // Создадим массив [A, B, C]
        PersistentArray<String> array = new PersistentArray<>();
        array = array.add("A").add("B").add("C");

        List<String> values = new ArrayList<>();
        for (String s : array) {
            values.add(s);
        }

        // Проверяем, что мы получили ровно 3 элемента: A, B, C
        assertEquals(3, values.size());
        assertEquals(List.of("A", "B", "C"), values);

        // Сделаем remove(1) => уберём B
        array = array.remove(1);

        values.clear();
        for (String s : array) {
            values.add(s);
        }
        // Теперь должно остаться [A, C]
        assertEquals(2, values.size());
        assertEquals(List.of("A", "C"), values);
    }

    @Test
    void testUndoRedo() {
        // Создаём массив [A, B, C]
        PersistentArray<String> array = new PersistentArray<>();
        array = array.add("A").add("B").add("C"); // => [A, B, C]

        array = array.remove(2);

        PersistentArray<String> undone = array.undo();

        List<String> undoneValues = new ArrayList<>();
        for (String s : undone) {
            undoneValues.add(s);
        }

        assertEquals(List.of("A", "B"), undoneValues,
                "После undo остаёмся в [A, B], т.к. C не восстанавливаем");

        PersistentArray<String> redone = undone.redo();
        List<String> redoneValues = new ArrayList<>();
        for (String s : redone) {
            redoneValues.add(s);
        }
        assertEquals(List.of("A", "B"), redoneValues,
                "После redo [A, B]");
    }
}