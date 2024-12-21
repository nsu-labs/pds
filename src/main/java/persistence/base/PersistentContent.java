package persistence.base;

import java.util.function.Consumer;

/**
 * Класс для хранения персистентного содержимого с поддержкой модификаций.
 *
 * @param <T> Тип содержимого.
 */
public class PersistentContent<T> {
    // Содержимое, которое хранит объект
    public T content;
    // Счетчик модификаций
    public ModificationCount maxModification;

    /**
     * Конструктор для инициализации содержимого.
     *
     * @param content Содержимое.
     * @param step    Начальное значение счетчика модификаций.
     */
    public PersistentContent(T content, ModificationCount step) {
        this.content = content; // Инициализируем содержимое
        this.maxModification = step; // Инициализируем счетчик модификаций
    }

    /**
     * Метод для обновления содержимого с помощью переданного обновляющего действия.
     *
     * @param contentUpdater Лямбда или функция, обновляющая содержимое.
     */
    public void update(Consumer<T> contentUpdater) {
        contentUpdater.accept(content); // Применяем обновление к содержимому
        maxModification.value++; // Увеличиваем значение счетчика модификаций
    }
}
