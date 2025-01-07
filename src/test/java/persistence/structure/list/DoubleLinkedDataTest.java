package persistence.structure.list;

import org.junit.Test;
import static org.junit.Assert.*;

import persistence.base.PersistentNode;

import java.util.UUID;

public class DoubleLinkedDataTest {

    @Test
    public void testConstructorNoID() {
        // Создадим фиктивные PersistentNode
        PersistentNode<DoubleLinkedData<String>> nextNode = new PersistentNode<>(5, null);
        PersistentNode<DoubleLinkedData<String>> prevNode = new PersistentNode<>(2, null);
        PersistentNode<String> valueNode = new PersistentNode<>(3, "value");

        // Вызываем первый конструктор (без id)
        DoubleLinkedData<String> data = new DoubleLinkedData<>(nextNode, prevNode, valueNode);

        // Проверяем, что все поля установлены
        assertSame("next должно совпадать", nextNode, data.next);
        assertSame("previous должно совпадать", prevNode, data.previous);
        assertSame("value должно совпадать", valueNode, data.value);
        assertNotNull("id должен генерироваться автоматически", data.id);
    }

    @Test
    public void testConstructorWithID() {
        // Создадим фиктивные PersistentNode
        PersistentNode<DoubleLinkedData<Integer>> nextNode = new PersistentNode<>(10, null);
        PersistentNode<DoubleLinkedData<Integer>> prevNode = new PersistentNode<>(1, null);
        PersistentNode<Integer> valueNode = new PersistentNode<>(2, 999);

        UUID customId = UUID.randomUUID();

        // Вызываем второй конструктор, передаём свой UUID
        DoubleLinkedData<Integer> data = new DoubleLinkedData<>(nextNode, prevNode, valueNode, customId);

        // Проверяем поля
        assertSame(nextNode, data.next);
        assertSame(prevNode, data.previous);
        assertSame(valueNode, data.value);
        assertEquals("Переданный id должен сохраниться", customId, data.id);
    }

    @Test
    public void testIDsAreDifferentIfNoCustomID() {
        // Если мы используем первый конструктор, он генерирует UUID самостоятельно
        DoubleLinkedData<String> data1 = new DoubleLinkedData<>(null, null, null);
        DoubleLinkedData<String> data2 = new DoubleLinkedData<>(null, null, null);

        // Обычно два случайных UUID не совпадают
        assertNotNull(data1.id);
        assertNotNull(data2.id);
        assertNotEquals("Обычно разные UUID", data1.id, data2.id);
    }

    @Test
    public void testSetNextPreviousManually() {
        // Показываем, что ссылки можно менять (если в вашем коде это реально нужно).
        // Но т.к. класс public поля, достаточно проверить, что можно присвоить.

        PersistentNode<DoubleLinkedData<String>> firstNode = new PersistentNode<>(0, null);
        PersistentNode<DoubleLinkedData<String>> secondNode = new PersistentNode<>(1, null);

        DoubleLinkedData<String> data = new DoubleLinkedData<>(null, null, null);

        // Меняем ссылки
        data.next = firstNode;
        data.previous = secondNode;

        assertSame("next должен указывать на firstNode", firstNode, data.next);
        assertSame("previous должен указывать на secondNode", secondNode, data.previous);
    }

    @Test
    public void testValueNode() {
        // Проверка value-ссылки
        PersistentNode<String> valNode = new PersistentNode<>(10, "someValue");
        DoubleLinkedData<String> data = new DoubleLinkedData<>(null, null, valNode);

        assertEquals("someValue", data.value.value(10));
        // Дополнительно меняем value
        valNode.update(11, "changed");
        // Проверяем, что data.value -> valNode -> "changed" на 11 шаге
        assertEquals("changed", data.value.value(11));
    }
}