package storage;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class Storage<T> {
    private final int capacity;
    private final Queue<T> components = new LinkedList<>();
    private static final Logger logger = Logger.getLogger(Storage.class.getName());

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
            logger.info(Thread.currentThread().getName() + " Ожидание: склад " + component.getClass().getSimpleName() + " полон: " + components.size() + "/" + capacity);
            wait();
        }

        components.add(component);
        logger.info(Thread.currentThread().getName() + " Добавлен " + component.getClass().getSimpleName() + " теперь: " + components.size() + "/" + capacity);
        notifyAll();
    }

    public synchronized T takeComponent() throws InterruptedException{
        while (components.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Поток был прерван во время ожидания извлечения");
            }
            logger.info(Thread.currentThread().getName() + " Ожидание: склад пуст");
            wait();
        }

        T component = components.poll();
        logger.info(Thread.currentThread().getName() + " Взят " + component.getClass().getSimpleName() + " теперь: " + components.size() + "/" + capacity);
        notifyAll();
        return component;
    }

    public boolean isFull() {
        return (components.size() >= capacity);
    }
}
