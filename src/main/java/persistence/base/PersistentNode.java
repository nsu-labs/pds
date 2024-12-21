package persistence.base;

import persistence.base.tree.BinaryTree;

/**
 * Класс для представления узла, поддерживающего персистентность изменений.
 *
 * @param <TV> Тип значения, хранимого в узле.
 */
public class PersistentNode<TV> {
    // Дерево для хранения изменений с привязкой к шагу модификации
    public BinaryTree<Integer, TV> modifications = new BinaryTree<>();

    /**
     * Конструктор для создания узла с начальными данными.
     *
     * @param creationStep Шаг создания узла.
     * @param initialValue Начальное значение узла.
     */
    public PersistentNode(int creationStep, TV initialValue) {
        update(creationStep, initialValue); // Добавляем начальное значение на шаге создания
    }

    /**
     * Метод для получения значения узла на заданном шаге.
     *
     * @param accessStep Шаг, на котором запрашивается значение.
     * @return Значение, соответствующее ближайшему меньшему шагу.
     */
    public TV value(int accessStep) {
        return modifications.findNearestLess(accessStep); // Находим ближайшее значение по шагу
    }

    /**
     * Метод для обновления значения узла.
     *
     * @param accessStep Шаг модификации.
     * @param value      Новое значение.
     * @return Текущий узел после обновления.
     */
    public PersistentNode<TV> update(int accessStep, TV value) {
        modifications.insert(accessStep, value); // Вставляем новое значение в дерево изменений
        return this; // Возвращаем текущий узел для цепочного вызова
    }
}
