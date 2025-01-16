package persistence.base;

public class ModificationCount {
    private int value;

    public ModificationCount(int value) {
        this.setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
