package persistence.base;

import java.util.Arrays;

/**
 * Абстрактный класс для работы с персистентными коллекциями.
 *
 * @param <K>  Тип ключа.
 * @param <OT> Тип значений в коллекции.
 * @param <BT> Тип узлов структуры коллекции.
 */
public abstract class BasePersistentCollection<K, OT, BT> {
    // Количество модификаций коллекции
    protected final int modificationCount, startModificationCount;
    // Текущее количество элементов в коллекции
    public int count;
    // Персистентное содержимое узлов
    protected PersistentContent<BT> nodes;

    /**
     * Конструктор по умолчанию. Создает пустую коллекцию.
     */
    protected BasePersistentCollection() {
        modificationCount = 0;
        startModificationCount = 0;
    }

    /**
     * Конструктор для инициализации коллекции с узлами и параметрами.
     *
     * @param nodes            Узлы коллекции.
     * @param count            Количество элементов.
     * @param modificationCount Количество модификаций.
     */
    protected BasePersistentCollection(PersistentContent<BT> nodes, int count, int modificationCount) {
        this(nodes, count, modificationCount, 0);
    }

    /**
     * Конструктор с расширенной инициализацией.
     *
     * @param nodes                Узлы коллекции.
     * @param count                Количество элементов.
     * @param modificationCount    Количество модификаций.
     * @param startModificationCount Начальное количество модификаций.
     */
    protected BasePersistentCollection(PersistentContent<BT> nodes, int count, int modificationCount, int startModificationCount) {
        this.nodes = nodes;
        this.modificationCount = modificationCount;
        this.startModificationCount = startModificationCount;
        this.count = count;
    }

    /**
     * Получение значения по ключу.
     *
     * @param key Ключ.
     * @return Значение.
     */
    public abstract OT get(K key);

    /**
     * Замена значения по ключу.
     *
     * @param key      Ключ.
     * @param newValue Новое значение.
     * @return Обновленная коллекция.
     */
    public abstract BasePersistentCollection<K, OT, BT> replace(K key, OT newValue);

    /**
     * Пересчет количества элементов в коллекции.
     *
     * @param modificationStep Шаг модификации.
     * @return Обновленное количество элементов.
     */
    protected abstract int recalculateCount(int modificationStep);

    /**
     * Пересборка узлов коллекции.
     *
     * @return Пересобранное содержимое узлов.
     */
    protected abstract PersistentContent<BT> reassembleNodes();

    /**
     * Получение значения по вложенному набору ключей.
     *
     * @param keys Массив ключей.
     * @return Найденное значение.
     */
    public Object getIn(Object... keys) {
        // Получаем элемент по первому ключу
        Object item = get((K) keys[0]);
        int keysLength = keys.length;

        // Проходим по оставшимся ключам
        for (int i = 1; i < keysLength; i++) {
            // Если элемент является коллекцией, продолжаем искать
            if (item instanceof BasePersistentCollection bpc) {
                item = bpc.get(keys[i]);
            } else {
                // Если вышли за границы вложенности, выбрасываем исключение
                throw new IndexOutOfBoundsException(String.format(
                        "out of nested bounds - real nest:%d, got keys: %d",
                        (i - 1),
                        keysLength
                ));
            }
        }

        return item; // Возвращаем найденный элемент
    }

    /**
     * Установка значения по вложенному набору ключей.
     *
     * @param value Значение для установки.
     * @param keys  Массив ключей.
     * @return Обновленная коллекция.
     */
    public BasePersistentCollection<K, OT, BT> setIn(Object value, Object... keys) {
        if (keys.length == 1) {
            // Если ключ один, просто заменяем значение
            return replace((K) keys[0], (OT) value);
        }

        // Проверяем, является ли текущий элемент коллекцией
        if (!(get((K) keys[0]) instanceof BasePersistentCollection bpc)) {
            throw new IndexOutOfBoundsException(String.format(
                    "out of nested bounds - real nest:%d from the keys end", keys.length
            ));
        }

        // Рекурсивно вызываем setIn для вложенных ключей
        return replace((K) keys[0], (OT) bpc.setIn(value, removeFirstKey(keys)));
    }

    /**
     * Удаление первого ключа из массива ключей.
     *
     * @param keys Массив ключей.
     * @return Новый массив без первого ключа.
     */
    private Object[] removeFirstKey(Object... keys) {
        return Arrays.copyOfRange(keys, 1, keys.length); // Копируем часть массива без первого элемента
    }
}
