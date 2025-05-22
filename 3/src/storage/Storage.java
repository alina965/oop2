package storage;

import java.util.LinkedList;
import java.util.Queue;

public class Storage<T> {
    private final int capacity;
    private final Queue<T> components = new LinkedList<>();

    public Storage(int capacity) throws IllegalArgumentException{
        if (capacity <= 0) {
            throw new IllegalArgumentException("Вместимость должна быть положительной");
        }
        this.capacity = capacity;
    }

    public synchronized void putComponent(T component) throws InterruptedException, IllegalArgumentException {
        if (component == null) {
            throw new IllegalArgumentException("Деталь не может быть null");
        }

        while (components.size() >= capacity) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Поток был прерван во время ожидания добавления");
            }
            System.out.printf("[%s] Ожидание: склад %s полон (%d/%d)\n", Thread.currentThread().getName(), component.getClass().getSimpleName(), components.size(), capacity);
            wait();
        }

        components.add(component);
        System.out.printf("[%s] Добавлен %s (теперь %d/%d)\n", Thread.currentThread().getName(), component.getClass().getSimpleName(), components.size(), capacity);
        notifyAll();
    }

    public synchronized T takeComponent() throws InterruptedException{
        while (components.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Поток был прерван во время ожидания извлечения");
            }
            System.out.printf("[%s] Ожидание: склад пуст\n", Thread.currentThread().getName());
            wait();
        }

        T component = components.poll();
        System.out.printf("[%s] Взят %s (осталось %d/%d)\n", Thread.currentThread().getName(), component.getClass().getSimpleName(), components.size(), capacity);
        notifyAll();
        return component;
    }

    public boolean isFull() {
        return (components.size() >= capacity);
    }
}
