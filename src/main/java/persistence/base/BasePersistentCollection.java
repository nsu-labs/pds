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
    private int count;
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
        this.setCount(count);
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
