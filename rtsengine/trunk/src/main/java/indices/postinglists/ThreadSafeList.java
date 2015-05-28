package indices.postinglists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * Unused, probably we will not use it, just tried to write an own thread safe list implementation
 * also only the most important methods currently implemented
 *
 */
public class ThreadSafeList<T>  implements List<T> {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final List<T> list;

    public ThreadSafeList(List<T> list) {
        this.list = list;
    }

    @Override
    public boolean remove(Object o) {
        readWriteLock.writeLock().lock();
        boolean ret;
        try {
            ret = list.remove(o);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean add(T t) {
        readWriteLock.writeLock().lock();
        boolean ret;
        try {
            ret = list.add(t);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return ret;
    }

    @Override
    public void clear() {
        readWriteLock.writeLock().lock();
        try {
            list.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        readWriteLock.readLock().lock();
        try {
            return list.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        readWriteLock.readLock().lock();
        try {
            return list.contains(o);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public T get(int index) {
        readWriteLock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public T set(int index, T element) {
        return null;
    }

    @Override
    public void add(int index, T element) {

    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }

}