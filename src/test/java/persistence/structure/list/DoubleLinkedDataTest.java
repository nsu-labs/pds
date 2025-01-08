package persistence.structure.list;

import org.junit.jupiter.api.Test;
import persistence.base.PersistentNode;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DoubleLinkedDataTest {

    @Test
    void testConstructorWithoutExplicitId() {
        // Подготавливаем тестовые узлы (можно упростить, если не нужны реальные данные)
        PersistentNode<DoubleLinkedData<String>> nextNode = new PersistentNode<>(0, null);
        PersistentNode<DoubleLinkedData<String>> prevNode = new PersistentNode<>(0, null);
        PersistentNode<String> valueNode = new PersistentNode<>(0, "Hello");

        // Создаём DoubleLinkedData без передачи ID
        DoubleLinkedData<String> data = new DoubleLinkedData<>(nextNode, prevNode, valueNode);

        // Проверяем, что поля корректно установлены
        assertSame(nextNode, data.next,
                "Поле 'next' должно соответствовать переданному узлу");
        assertSame(prevNode, data.previous,
                "Поле 'previous' должно соответствовать переданному узлу");
        assertSame(valueNode, data.value,
                "Поле 'value' должно соответствовать переданному узлу");

        // Проверяем, что ID сгенерировался (не null)
        assertNotNull(data.id,
                "ID должен быть автоматически сгенерированным непустым UUID");
    }

    @Test
    void testConstructorWithExplicitId() {
        // Подготавливаем тестовые узлы
        PersistentNode<DoubleLinkedData<String>> nextNode = new PersistentNode<>(10, null);
        PersistentNode<DoubleLinkedData<String>> prevNode = new PersistentNode<>(10, null);
        PersistentNode<String> valueNode = new PersistentNode<>(10, "World");

        // Создаём фиксированный UUID
        UUID customId = UUID.fromString("11111111-2222-3333-4444-555555555555");

        // Создаём DoubleLinkedData с передачей customId
        DoubleLinkedData<String> data = new DoubleLinkedData<>(nextNode, prevNode, valueNode, customId);

        // Проверяем, что поля корректно установлены
        assertSame(nextNode, data.next,
                "Поле 'next' должно соответствовать переданному узлу");
        assertSame(prevNode, data.previous,
                "Поле 'previous' должно соответствовать переданному узлу");
        assertSame(valueNode, data.value,
                "Поле 'value' должно соответствовать переданному узлу");

        // Проверяем, что ID установлен именно тот, который мы передали
        assertEquals(customId, data.id,
                "ID должен совпадать с переданным в конструктор");
    }

    @Test
    void testValueNodeContents() {
        // Дополнительный тест, чтобы убедиться,
        // что мы можем корректно получить значение из valueNode
        PersistentNode<String> valueNode = new PersistentNode<>(0, "Initial");
        DoubleLinkedData<String> data = new DoubleLinkedData<>(null, null, valueNode);

        // Шаг 5 — обновляем значение
        valueNode.update(5, "Updated");

        // Проверяем, что при запросе на нужном шаге получаем ожидаемое значение
        assertEquals("Updated", data.value.value(5),
                "valueNode должен вернуть 'Updated' на шаге 5");
    }
}