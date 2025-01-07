package persistence.structure.list;

import org.junit.Test;
import static org.junit.Assert.*;

import persistence.base.PersistentNode;

/**
 * Тесты для класса DoubleLinkedContent<T>.
 */
public class DoubleLinkedContentTest {

    @Test
    public void testConstructorAndFields() {
        // Готовим фиктивные псевдо-узлы pseudoHead / pseudoTail
        // Для упрощения используем DoubleLinkedData<T> = null
        // (либо создаём какую-то тестовую Data при необходимости)
        PersistentNode<DoubleLinkedData<String>> headNode = new PersistentNode<>(0, null);
        PersistentNode<DoubleLinkedData<String>> tailNode = new PersistentNode<>(10, null);

        // Создаём DoubleLinkedContent
        DoubleLinkedContent<String> content = new DoubleLinkedContent<>(headNode, tailNode);

        // Проверяем, что поля установлены корректно
        assertSame("pseudoHead должен указывать на headNode", headNode, content.pseudoHead);
        assertSame("pseudoTail должен указывать на tailNode", tailNode, content.pseudoTail);
    }

    @Test
    public void testConstructorNullNodes() {
        // Проверим вариант, если pseudoHead или pseudoTail = null
        // (зависит от того, допускается ли по вашей логике)
        DoubleLinkedContent<String> content = new DoubleLinkedContent<>(null, null);
        assertNull("pseudoHead может быть null", content.pseudoHead);
        assertNull("pseudoTail может быть null", content.pseudoTail);
    }

    @Test
    public void testDifferentNodes() {
        // Создаём два разных узла
        PersistentNode<DoubleLinkedData<Integer>> headNode = new PersistentNode<>(1, new DoubleLinkedData<>(null, 123, null));
        PersistentNode<DoubleLinkedData<Integer>> tailNode = new PersistentNode<>(5, new DoubleLinkedData<>(null, 999, null));

        // Создаём DoubleLinkedContent
        DoubleLinkedContent<Integer> content = new DoubleLinkedContent<>(headNode, tailNode);

        // Проверка
        assertEquals("headNode был создан на шаге 1", (Integer)123, headNode.value(1).value);
        assertEquals("tailNode был создан на шаге 5", (Integer)999, tailNode.value(5).value);

        // Убедимся, что ссылки совпадают
        assertSame(headNode, content.pseudoHead);
        assertSame(tailNode, content.pseudoTail);
    }
}