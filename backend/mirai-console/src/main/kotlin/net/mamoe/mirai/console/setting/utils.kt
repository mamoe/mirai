/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.serializer
import net.mamoe.yamlkt.Yaml
import kotlin.reflect.KClass

internal fun <E, R> MutableList<E>.shadowMap(transform: (E) -> R, transformBack: (R) -> E): MutableList<R> {
    return object : MutableList<R> {
        override val size: Int get() = this@shadowMap.size

        override fun contains(element: R): Boolean = this@shadowMap.any { it.let(transform) == element }
        override fun containsAll(elements: Collection<R>): Boolean = elements.all(::contains)
        override fun get(index: Int): R = this@shadowMap[index].let(transform)
        override fun indexOf(element: R): Int = this@shadowMap.indexOfFirst { it.let(transform) == element }
        override fun isEmpty(): Boolean = this@shadowMap.isEmpty()
        override fun iterator(): MutableIterator<R> = object : MutableIterator<R> {
            private val delegate = this@shadowMap.iterator()
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): R = delegate.next().let(transform)
            override fun remove() = delegate.remove()
        }

        override fun lastIndexOf(element: R): Int = this@shadowMap.indexOfLast { it.let(transform) == element }
        override fun add(element: R): Boolean = this@shadowMap.add(element.let(transformBack))
        override fun add(index: Int, element: R) = this@shadowMap.add(index, element.let(transformBack))
        override fun addAll(index: Int, elements: Collection<R>): Boolean =
            this@shadowMap.addAll(index, elements.map(transformBack))

        override fun addAll(elements: Collection<R>): Boolean = this@shadowMap.addAll(elements.map(transformBack))
        override fun clear() = this@shadowMap.clear()

        override fun listIterator(): MutableListIterator<R> = object : MutableListIterator<R> {
            private val delegate = this@shadowMap.listIterator()
            override fun hasPrevious(): Boolean = delegate.hasPrevious()
            override fun nextIndex(): Int = delegate.nextIndex()
            override fun previous(): R = delegate.previous().let(transform)
            override fun previousIndex(): Int = delegate.previousIndex()
            override fun add(element: R) = delegate.add(element.let(transformBack))
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): R = delegate.next().let(transform)
            override fun remove() = delegate.remove()
            override fun set(element: R) = delegate.set(element.let(transformBack))
        }

        override fun listIterator(index: Int): MutableListIterator<R> = object : MutableListIterator<R> {
            private val delegate = this@shadowMap.listIterator(index)
            override fun hasPrevious(): Boolean = delegate.hasPrevious()
            override fun nextIndex(): Int = delegate.nextIndex()
            override fun previous(): R = delegate.previous().let(transform)
            override fun previousIndex(): Int = delegate.previousIndex()
            override fun add(element: R) = delegate.add(element.let(transformBack))
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): R = delegate.next().let(transform)
            override fun remove() = delegate.remove()
            override fun set(element: R) = delegate.set(element.let(transformBack))
        }

        override fun remove(element: R): Boolean = this@shadowMap.removeIf { it.let(transform) == element }
        override fun removeAll(elements: Collection<R>): Boolean = elements.all(::remove)
        override fun removeAt(index: Int): R = this@shadowMap.removeAt(index).let(transform)
        override fun retainAll(elements: Collection<R>): Boolean = this@shadowMap.retainAll(elements.map(transformBack))
        override fun set(index: Int, element: R): R =
            this@shadowMap.set(index, element.let(transformBack)).let(transform)

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<R> =
            this@shadowMap.subList(fromIndex, toIndex).map(transform).toMutableList()
    }
}


internal fun <E, R> MutableSet<E>.shadowMap(transform: (E) -> R, transformBack: (R) -> E): MutableSet<R> {
    return object : MutableSet<R> {
        override val size: Int get() = this@shadowMap.size

        override fun contains(element: R): Boolean = this@shadowMap.any { it.let(transform) == element }
        override fun containsAll(elements: Collection<R>): Boolean = elements.all(::contains)
        override fun isEmpty(): Boolean = this@shadowMap.isEmpty()
        override fun iterator(): MutableIterator<R> = object : MutableIterator<R> {
            private val delegate = this@shadowMap.iterator()
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): R = delegate.next().let(transform)
            override fun remove() = delegate.remove()
        }

        override fun add(element: R): Boolean = this@shadowMap.add(element.let(transformBack))
        override fun addAll(elements: Collection<R>): Boolean = this@shadowMap.addAll(elements.map(transformBack))
        override fun clear() = this@shadowMap.clear()

        override fun remove(element: R): Boolean = this@shadowMap.removeIf { it.let(transform) == element }
        override fun removeAll(elements: Collection<R>): Boolean = elements.all(::remove)
        override fun retainAll(elements: Collection<R>): Boolean = this@shadowMap.retainAll(elements.map(transformBack))
    }
}

internal fun <T> dynamicList(supplier: () -> List<T>): List<T> {
    return object : List<T> {
        override val size: Int get() = supplier().size
        override fun contains(element: T): Boolean = supplier().contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = supplier().containsAll(elements)
        override fun get(index: Int): T = supplier()[index]
        override fun indexOf(element: T): Int = supplier().indexOf(element)
        override fun isEmpty(): Boolean = supplier().isEmpty()
        override fun iterator(): Iterator<T> = supplier().iterator()
        override fun lastIndexOf(element: T): Int = supplier().lastIndexOf(element)
        override fun listIterator(): ListIterator<T> = supplier().listIterator()
        override fun listIterator(index: Int): ListIterator<T> = supplier().listIterator(index)
        override fun subList(fromIndex: Int, toIndex: Int): List<T> = supplier().subList(fromIndex, toIndex)
    }
}

internal fun <T> dynamicSet(supplier: () -> Set<T>): Set<T> {
    return object : Set<T> {
        override val size: Int get() = supplier().size
        override fun contains(element: T): Boolean = supplier().contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = supplier().containsAll(elements)
        override fun isEmpty(): Boolean = supplier().isEmpty()
        override fun iterator(): Iterator<T> = supplier().iterator()
    }
}


internal fun <T> dynamicMutableList(supplier: () -> MutableList<T>): MutableList<T> {
    return object : MutableList<T> {
        override val size: Int get() = supplier().size
        override fun contains(element: T): Boolean = supplier().contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = supplier().containsAll(elements)
        override fun get(index: Int): T = supplier()[index]
        override fun indexOf(element: T): Int = supplier().indexOf(element)
        override fun isEmpty(): Boolean = supplier().isEmpty()
        override fun iterator(): MutableIterator<T> = supplier().iterator()
        override fun lastIndexOf(element: T): Int = supplier().lastIndexOf(element)
        override fun add(element: T): Boolean = supplier().add(element)
        override fun add(index: Int, element: T) = supplier().add(index, element)
        override fun addAll(index: Int, elements: Collection<T>): Boolean = supplier().addAll(index, elements)
        override fun addAll(elements: Collection<T>): Boolean = supplier().addAll(elements)
        override fun clear() = supplier().clear()
        override fun listIterator(): MutableListIterator<T> = supplier().listIterator()
        override fun listIterator(index: Int): MutableListIterator<T> = supplier().listIterator(index)
        override fun remove(element: T): Boolean = supplier().remove(element)
        override fun removeAll(elements: Collection<T>): Boolean = supplier().removeAll(elements)
        override fun removeAt(index: Int): T = supplier().removeAt(index)
        override fun retainAll(elements: Collection<T>): Boolean = supplier().retainAll(elements)
        override fun set(index: Int, element: T): T = supplier().set(index, element)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = supplier().subList(fromIndex, toIndex)
    }
}


internal fun <T> dynamicMutableSet(supplier: () -> MutableSet<T>): MutableSet<T> {
    return object : MutableSet<T> {
        override val size: Int get() = supplier().size
        override fun contains(element: T): Boolean = supplier().contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = supplier().containsAll(elements)
        override fun isEmpty(): Boolean = supplier().isEmpty()
        override fun iterator(): MutableIterator<T> = supplier().iterator()
        override fun add(element: T): Boolean = supplier().add(element)
        override fun addAll(elements: Collection<T>): Boolean = supplier().addAll(elements)
        override fun clear() = supplier().clear()
        override fun remove(element: T): Boolean = supplier().remove(element)
        override fun removeAll(elements: Collection<T>): Boolean = supplier().removeAll(elements)
        override fun retainAll(elements: Collection<T>): Boolean = supplier().retainAll(elements)
    }
}

internal inline fun <T> MutableList<T>.observable(crossinline onChanged: () -> Unit): MutableList<T> {
    return object : MutableList<T> {
        override val size: Int get() = this@observable.size
        override fun contains(element: T): Boolean = this@observable.contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = this@observable.containsAll(elements)
        override fun get(index: Int): T = this@observable[index]
        override fun indexOf(element: T): Int = this@observable.indexOf(element)
        override fun isEmpty(): Boolean = this@observable.isEmpty()
        override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            private val delegate = this@observable.iterator()
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): T = delegate.next()
            override fun remove() = delegate.remove().also { onChanged() }
        }

        override fun lastIndexOf(element: T): Int = this@observable.lastIndexOf(element)
        override fun add(element: T): Boolean = this@observable.add(element).also { onChanged() }
        override fun add(index: Int, element: T) = this@observable.add(index, element).also { onChanged() }
        override fun addAll(index: Int, elements: Collection<T>): Boolean =
            this@observable.addAll(index, elements).also { onChanged() }

        override fun addAll(elements: Collection<T>): Boolean = this@observable.addAll(elements).also { onChanged() }
        override fun clear() = this@observable.clear().also { onChanged() }
        override fun listIterator(): MutableListIterator<T> = object : MutableListIterator<T> {
            private val delegate = this@observable.listIterator()
            override fun hasPrevious(): Boolean = delegate.hasPrevious()
            override fun nextIndex(): Int = delegate.nextIndex()
            override fun previous(): T = delegate.previous()
            override fun previousIndex(): Int = delegate.previousIndex()
            override fun add(element: T) = delegate.add(element).also { onChanged() }
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): T = delegate.next()
            override fun remove() = delegate.remove().also { onChanged() }
            override fun set(element: T) = delegate.set(element).also { onChanged() }
        }

        override fun listIterator(index: Int): MutableListIterator<T> = object : MutableListIterator<T> {
            private val delegate = this@observable.listIterator(index)
            override fun hasPrevious(): Boolean = delegate.hasPrevious()
            override fun nextIndex(): Int = delegate.nextIndex()
            override fun previous(): T = delegate.previous()
            override fun previousIndex(): Int = delegate.previousIndex()
            override fun add(element: T) = delegate.add(element).also { onChanged() }
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): T = delegate.next()
            override fun remove() = delegate.remove().also { onChanged() }
            override fun set(element: T) = delegate.set(element).also { onChanged() }
        }

        override fun remove(element: T): Boolean = this@observable.remove(element).also { onChanged() }
        override fun removeAll(elements: Collection<T>): Boolean =
            this@observable.removeAll(elements).also { onChanged() }

        override fun removeAt(index: Int): T = this@observable.removeAt(index).also { onChanged() }
        override fun retainAll(elements: Collection<T>): Boolean =
            this@observable.retainAll(elements).also { onChanged() }

        override fun set(index: Int, element: T): T = this@observable.set(index, element).also { onChanged() }
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = this@observable.subList(fromIndex, toIndex)
    }
}

internal inline fun <T> MutableSet<T>.observable(crossinline onChanged: () -> Unit): MutableSet<T> {
    return object : MutableSet<T> {
        override val size: Int get() = this@observable.size
        override fun contains(element: T): Boolean = this@observable.contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = this@observable.containsAll(elements)
        override fun isEmpty(): Boolean = this@observable.isEmpty()
        override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            private val delegate = this@observable.iterator()
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): T = delegate.next()
            override fun remove() = delegate.remove().also { onChanged() }
        }

        override fun add(element: T): Boolean = this@observable.add(element).also { onChanged() }
        override fun addAll(elements: Collection<T>): Boolean = this@observable.addAll(elements).also { onChanged() }
        override fun clear() = this@observable.clear().also { onChanged() }
        override fun remove(element: T): Boolean = this@observable.remove(element).also { onChanged() }
        override fun removeAll(elements: Collection<T>): Boolean =
            this@observable.removeAll(elements).also { onChanged() }

        override fun retainAll(elements: Collection<T>): Boolean =
            this@observable.retainAll(elements).also { onChanged() }
    }
}


@OptIn(ImplicitReflectionSerializer::class)
internal fun <R : Any> Any.smartCastPrimitive(clazz: KClass<R>): R {
    kotlin.runCatching {
        return Yaml.default.parse(clazz.serializer(), this.toString())
    }.getOrElse {
        throw IllegalArgumentException("Cannot cast '$this' to ${clazz.qualifiedName}", it)
    }
}

