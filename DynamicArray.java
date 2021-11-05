@SuppressWarnings("unchecked")
public class DynamicArray<T> {
    public T[] array;
    private int capacity;
    public int length;

    public DynamicArray() {
        this.array = (T[]) new Object[10];
        this.capacity = 10;
        this.length = 0;
    }

    private void increaseCapacity() {
        this.capacity = this.capacity * 2;
        T[] temp = (T[]) new Object[this.capacity];
        for (int i = 0; i < this.array.length; i++) {
            temp[i] = this.array[i];
        }

        this.array = temp;
    }

    public void insert(T item) {
        this.array[length] = item;
        this.length++;
        if (this.length == this.capacity) {
            this.increaseCapacity();
        }
    }

    public T remove(int index) {
        // AMY DOES BECAUSE SHE'S LAZY
    }

    public T get(int index) {
        
    }

    public int indexOf(T item) {
        
    }
}
