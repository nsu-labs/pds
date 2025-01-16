package persistence.base;

import java.util.function.Consumer;

/**
 * Класс для хранения персистентного содержимого с поддержкой модификаций.
 *
 * @param <T> Тип содержимого.
 */
public class PersistentContent<T> {
    // Содержимое, которое хранит объект
    private T content;
    // Счетчик модификаций
    private ModificationCount maxModification;

    /**
     * Конструктор для инициализации содержимого.
     *
     * @param content Содержимое.
     * @param step    Начальное значение счетчика модификаций.
     */
    public PersistentContent(T content, ModificationCount step) {
        this.setContent(content);
        this.setMaxModification(step);
    }

    /**
     * Метод для обновления содержимого с помощью переданного обновляющего действия.
     *
     * @param contentUpdater Лямбда или функция, обновляющая содержимое.
     */
    public void update(Consumer<T> contentUpdater) {
        contentUpdater.accept(getContent()); // Применяем обновление к содержимому
        getMaxModification().setValue(getMaxModification().getValue() + 1); // Увеличиваем значение счетчика модификаций
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public ModificationCount getMaxModification() {
        return maxModification;
    }

    public void setMaxModification(ModificationCount maxModification) {
        this.maxModification = maxModification;
    }
}
