import java.util.Iterator;

@SuppressWarnings("unchecked")
public class DynamicArray<T> implements Iterable<T> {
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

    public T get(int index) {
        return this.array[index];
    }

    public int indexOf(T item) {
        for (int i = 0; i < this.length; i++) {
            if (this.array[i].equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return this.length;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> it = new Iterator<T>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size() && array[currentIndex] != null;
            }

            @Override
            public T next() {
                return array[currentIndex++];
            }
        };
        return it;
    }
}
