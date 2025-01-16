package persistence.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест-класс для проверки работы PersistentNode.
 */
class PersistentNodeTest {

    @Test
    void testConstructorAndInitialValue() {
        // Создаём узел с шагом 0 и начальными данными "Initial"
        PersistentNode<String> node = new PersistentNode<>(0, "Initial");

        // Проверяем, что на шаге 0 действительно "Initial"
        assertEquals("Initial", node.value(0),
                "На шаге 0 должно быть значение 'Initial'");

        // Проверяем, что бинарное дерево создано не null
        assertNotNull(node.getModifications(),
                "Должно существовать дерево 'modifications'");
    }

    @Test
    void testSingleUpdate() {
        PersistentNode<String> node = new PersistentNode<>(0, "Initial");

        // Обновляем значение на шаге 10
        node.update(10, "UpdatedAt10");

        // Проверяем, что на шаге 10 получили "UpdatedAt10"
        assertEquals("UpdatedAt10", node.value(10),
                "На шаге 10 должно вернуться 'UpdatedAt10'");

        // Проверяем, что на шаге 5 (меньше 10, но больше 0) вернётся старое значение
        assertEquals("Initial", node.value(5),
                "На шаге 5 должно вернуться 'Initial', т.к. это ближайший меньший шаг");
    }

    @Test
    void testMultipleUpdates() {
        PersistentNode<String> node = new PersistentNode<>(0, "v0");

        // Последовательно вставляем обновления на разных шагах
        node.update(5, "v5")
                .update(10, "v10")
                .update(20, "v20");

        // Проверяем значения на разных шагах
        assertEquals("v0", node.value(0),
                "На шаге 0 ожидаем изначальное значение");
        assertEquals("v0", node.value(1),
                "На шаге 1 всё ещё 'v0', так как нет ближайшего меньшего шага, кроме 0");
        assertEquals("v5", node.value(5),
                "На шаге 5 должны получить 'v5'");
        assertEquals("v5", node.value(7),
                "На шаге 7 ближайший меньший - это 5, значит 'v5'");
        assertEquals("v10", node.value(10),
                "На шаге 10 - значение 'v10'");
        assertEquals("v10", node.value(15),
                "На шаге 15 ближайший меньший - это 10, значение 'v10'");
        assertEquals("v20", node.value(20),
                "На шаге 20 - значение 'v20'");
        assertEquals("v20", node.value(999),
                "На шаге 999 ближайший меньший - это 20, значит 'v20'");
    }

    @Test
    void testUpdateSameStepOverwritesValue() {
        PersistentNode<String> node = new PersistentNode<>(0, "v0");

        // Делаем два update на один и тот же шаг (5)
        node.update(5, "firstVersion");
        node.update(5, "secondVersion");

        // Проверяем, что на шаге 5 теперь "secondVersion"
        assertEquals("secondVersion", node.value(5),
                "При повторном update на тот же шаг значение должно перезаписаться");

        // На шаге 4 всё ещё "v0"
        assertEquals("v0", node.value(4),
                "На шаге 4 всё ещё изначальное значение");
    }

    @Test
    void testValueBeforeAnyUpdates() {
        // Начальный шаг 10, начальное значение "Initial"
        PersistentNode<String> node = new PersistentNode<>(10, "Initial");

        // Если попросим значение на шаге 0 (меньше шага создания),
        // то ничего не найдём -> null
        assertNull(node.value(0),
                "Запрос шага меньше шага создания (10) должен вернуть null, так как нет более ранних значений");

        // Шаг 10 — изначальное значение
        assertEquals("Initial", node.value(10));
    }
}