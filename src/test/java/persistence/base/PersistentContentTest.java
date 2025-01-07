package persistence.base;

import org.junit.Tet;
import static org.junit.Assert.*;

import java.util.function.Consumer;

public class PersistentContentTest {

    @Test
    public void testConstructor() {
        // Создаем фиктивный счетчик модификаций
        ModificationCount modCount = new ModificationCount();
        modCount.value = 5;

        // Создаем PersistentContent с некоторым контентом
        String initialContent = "Hello";
        PersistentContent<String> pc = new PersistentContent<>(initialContent, modCount);

        // Проверяем инициализированные поля
        assertEquals("Содержимое должно совпадать с переданным", initialContent, pc.content);
        assertEquals("Счетчик модификаций должен совпадать с переданным", 5, pc.maxModification.value);
    }

    @Test
    public void testUpdate() {
        // Счетчик модификаций
        ModificationCount modCount = new ModificationCount();
        modCount.value = 0;

        // Начальное содержимое
        StringBuilder sb = new StringBuilder("foo");
        // Создаем PersistentContent
        PersistentContent<StringBuilder> pc = new PersistentContent<>(sb, modCount);

        // Убеждаемся в начальных значениях
        assertEquals("foo", pc.content.toString());
        assertEquals(0, pc.maxModification.value);

        // Вызываем update, меняем строку "foo" -> "foobar"
        pc.update(strBuilder -> strBuilder.append("bar"));

        // Проверяем, что содержимое обновилось
        assertEquals("foobar", pc.content.toString());
        // Проверяем, что счетчик модификаций увеличился на 1
        assertEquals(1, pc.maxModification.value);

        // Еще раз вызываем update, меняем "foobar" -> "foobar!"
        pc.update(strBuilder -> strBuilder.append('!'));

        assertEquals("foobar!", pc.content.toString());
        assertEquals(2, pc.maxModification.value);
    }

    @Test
    public void testUpdateNoChange() {
        // Проверяем, что даже если мы "ничего не меняем" внутри update,
        // счетчик все равно увеличивается
        ModificationCount modCount = new ModificationCount();
        modCount.value = 10; // Начальное значение

        Integer initialContent = 42;
        PersistentContent<Integer> pc = new PersistentContent<>(initialContent, modCount);

        // Вызываем update, но не меняем контент
        pc.update(content -> {
            // Ничего
        });

        // Содержимое осталось прежним
        assertEquals((Integer)42, pc.content);
        // Но счетчик увеличился
        assertEquals(11, pc.maxModification.value);
    }

    @Test
    public void testMaxModificationReference() {
        // Убедимся, что внутри PersistentContent хранится тот же объект ModificationCount
        ModificationCount modCount = new ModificationCount();
        modCount.value = 100;

        PersistentContent<String> pc = new PersistentContent<>("Test", modCount);

        // Изменим значение напрямую
        modCount.value += 5;

        // Убедимся, что в pc это тоже видно
        assertEquals("Ожидаем 105", 105, pc.maxModification.value);

        // Или наоборот, через pc
        pc.update(s -> {});
        assertEquals("После update должен стать 106", 106, modCount.value);
    }
}