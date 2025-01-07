package persistence.base;

import org.junit.Test;
import static org.junit.Assert.*;

public class PersistentNodeTest {

    @Test
    public void testConstructorAndInitialValue() {
        // Создаем узел с начальными данными на шаге 10
        PersistentNode<String> node = new PersistentNode<>(10, "initial");

        // Проверяем, что на шаге 10 значение "initial"
        assertEquals("initial", node.value(10));

        // На шаге 9 ничего не было записано, значит nearestLess(9) -> null
        // так как шаг 10 > 9
        assertNull("На шаге 9 значение отсутствует", node.value(9));
    }

    @Test
    public void testUpdateAndValue() {
        // Создаем узел на шаге 1 со значением "one"
        PersistentNode<String> node = new PersistentNode<>(1, "one");

        // На шаге 1 => "one"
        assertEquals("one", node.value(1));
        // На шаге 0 => null (нет ближайшего меньшего)
        assertNull(node.value(0));

        // Делаем update на шаге 5 со значением "five"
        node.update(5, "five");
        // Проверяем, что на шаге 5 => "five"
        assertEquals("five", node.value(5));
        // А на шаге 3, ближайшее меньшее — шаг 1 => "one"
        assertEquals("one", node.value(3));

        // Ещё один update на шаге 2 => "two"
        node.update(2, "two");
        // Теперь на шаге 2 => "two"
        assertEquals("two", node.value(2));
        // На шаге 3, ближайшее меньшее шаг — 2 => "two"
        assertEquals("two", node.value(3));

        // На шаге 5 всё ещё => "five"
        assertEquals("five", node.value(5));
        // На шаге 6 => тоже "five" (ближайший меньше)
        assertEquals("five", node.value(6));
    }

    @Test
    public void testMultipleUpdatesAndHigherStep() {
        // Создаем узел на шаге 10 => "ten"
        PersistentNode<String> node = new PersistentNode<>(10, "ten");

        // Обновляем на шаге 12 => "twelve"
        node.update(12, "twelve");
        // Обновляем на шаге 15 => "fifteen"
        node.update(15, "fifteen");

        // На шаге 10 => "ten"
        assertEquals("ten", node.value(10));
        // На шаге 11 => "ten" (closest <= 11 is 10)
        assertEquals("ten", node.value(11));
        // На шаге 12 => "twelve"
        assertEquals("twelve", node.value(12));
        // На шаге 14 => "twelve"
        assertEquals("twelve", node.value(14));
        // На шаге 15 => "fifteen"
        assertEquals("fifteen", node.value(15));
        // На шаге 100 => "fifteen" (closest <= 100 is 15)
        assertEquals("fifteen", node.value(100));
    }

    @Test
    public void testChainUpdate() {
        // Тестируем, что update возвращает сам узел для цепного вызова
        PersistentNode<Integer> node = new PersistentNode<>(0, 100);

        node.update(5, 200)
                .update(10, 300)
                .update(20, 400);

        // Проверяем значения
        // шаг 5 => 200
        assertEquals((Integer)200, node.value(5));
        // шаг 15 => ближ. <= 15 => 10 => 300
        assertEquals((Integer)300, node.value(15));
        // шаг 50 => ближ. <= 50 => 20 => 400
        assertEquals((Integer)400, node.value(50));
    }

    @Test
    public void testNoUpdatesAfterCreation() {
        // Если не делаем update, то только одно значение на шаге создания
        PersistentNode<String> node = new PersistentNode<>(10, "init");
        assertEquals("init", node.value(10));
        assertNull(node.value(9));
        assertEquals("init", node.value(1000));
    }
}