package persistence.base;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class PersistentContentTest {

    @Test
    void testConstructorInitialization() {
        ModificationCount step = new ModificationCount(0);
        PersistentContent<String> persistentContent =
                new PersistentContent<>("Hello", step);

        assertNotNull(persistentContent.content,
                "Содержимое не должно быть null после инициализации");
        assertEquals("Hello", persistentContent.content,
                "Содержимое должно совпадать с переданным в конструктор");
        assertEquals(0, persistentContent.maxModification.value,
                "Счётчик модификаций должен совпадать с переданным в конструктор");
    }

    @Test
    void testUpdateSingleTime() {
        ModificationCount step = new ModificationCount(0);
        // Для демонстрации обновления используем StringBuilder (мутируемый тип)
        PersistentContent<StringBuilder> persistentContent =
                new PersistentContent<>(new StringBuilder("Hello"), step);

        // Обновляем содержимое добавлением " World"
        persistentContent.update(sb -> sb.append(" World"));

        // Проверяем, что строка реально изменилась
        assertEquals("Hello World", persistentContent.content.toString(),
                "Содержимое должно обновиться после вызова update");

        assertEquals(1, persistentContent.maxModification.value,
                "Счётчик модификаций должен увеличиться на 1");
    }

    @Test
    void testUpdateMultipleTimes() {
        ModificationCount step = new ModificationCount(0);
        PersistentContent<StringBuilder> persistentContent =
                new PersistentContent<>(new StringBuilder("A"), step);

        // Выполняем несколько обновлений подряд
        persistentContent.update(sb -> sb.append("B"));
        persistentContent.update(sb -> sb.append("C"));
        persistentContent.update(sb -> sb.append("D"));


        assertEquals("ABCD", persistentContent.content.toString(),
                "После серии апдейтов строка должна быть 'ABCD'");

        // Счётчик должен увеличиться на 3
        assertEquals(3, persistentContent.maxModification.value,
                "Счётчик модификаций должен увеличиться на количество апдейтов");
    }

    @Test
    void testUpdateWithNoRealChange() {
        ModificationCount step = new ModificationCount(5);
        // Содержимое - пусть просто число
        PersistentContent<Integer> persistentContent =
                new PersistentContent<>(42, step);

        // Обновляющая функция ничего не меняет (пустая лямбда)
        Consumer<Integer> doNothingUpdater = i -> {};

        // Выполним два таких "обновления"
        persistentContent.update(doNothingUpdater);
        persistentContent.update(doNothingUpdater);

        // Содержимое остаётся 42, но счётчик всё равно должен увеличиться
        assertEquals(42, persistentContent.content,
                "Содержимое не должно измениться при пустом апдейте");
        assertEquals(7, persistentContent.maxModification.value,
                "Счётчик модификаций всё равно должен увеличиться (5 + 2 = 7)");
    }
}