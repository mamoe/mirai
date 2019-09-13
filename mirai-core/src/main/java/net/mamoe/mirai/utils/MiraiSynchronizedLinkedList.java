package net.mamoe.mirai.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author Him188moe
 */
public class MiraiSynchronizedLinkedList<E> extends AbstractList<E> {
    @SuppressWarnings("WeakerAccess")
    protected final List<E> syncList;

    public MiraiSynchronizedLinkedList() {
        this.syncList = Collections.synchronizedList(new LinkedList<>());
    }

    public MiraiSynchronizedLinkedList(Collection<E> collection) {
        this.syncList = Collections.synchronizedList(new LinkedList<>(collection));
    }


    @Override
    public E get(int index) {
        return this.syncList.get(index);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.syncList.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.syncList.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return this.syncList.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.syncList.parallelStream();
    }

    @Override
    public int size() {
        return this.syncList.size();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.syncList.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return this.syncList.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        this.syncList.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        this.syncList.sort(c);
    }

    @Override
    public boolean add(E e) {
        return this.syncList.add(e);
    }

    @Override
    public E set(int index, E element) {
        return this.syncList.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        this.syncList.add(index, element);
    }

    @Override
    public E remove(int index) {
        return this.syncList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.syncList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.syncList.lastIndexOf(o);
    }

    @Override
    public void clear() {
        this.syncList.clear();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return this.syncList.addAll(index, c);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return this.syncList.iterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return this.syncList.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return this.syncList.listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return this.syncList.subList(fromIndex, toIndex);
    }

    @Override
    public int hashCode() {
        return this.syncList.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return this.syncList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.syncList.contains(o);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.syncList.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return this.syncList.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return this.syncList.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.syncList.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return this.syncList.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.syncList.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.syncList.retainAll(c);
    }
}
