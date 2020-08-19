/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DuplicatedCode")

package net.mamoe.mirai.console.internal.setting

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import net.mamoe.yamlkt.Yaml
import kotlin.reflect.KClass

// TODO: 2020/6/24 优化性能: 引入一个 comparator 之类来替代将 Int 包装为 Value<Int> 后进行 containsKey 比较的方法

internal inline fun <K, V, KR, VR> MutableMap<K, V>.shadowMap(
    crossinline kTransform: (K) -> KR,
    crossinline kTransformBack: (KR) -> K,
    crossinline vTransform: (V) -> VR,
    crossinline vTransformBack: (VR) -> V
): MutableMap<KR, VR> {
    return object : MutableMap<KR, VR> {
        override val size: Int get() = this@shadowMap.size
        override fun containsKey(key: KR): Boolean = this@shadowMap.containsKey(key.let(kTransformBack))
        override fun containsValue(value: VR): Boolean = this@shadowMap.containsValue(value.let(vTransformBack))
        override fun get(key: KR): VR? = this@shadowMap[key.let(kTransformBack)]?.let(vTransform)
        override fun isEmpty(): Boolean = this@shadowMap.isEmpty()

        override val entries: MutableSet<MutableMap.MutableEntry<KR, VR>>
            get() = this@shadowMap.entries.shadowMap(
                transform = { entry: MutableMap.MutableEntry<K, V> ->
                    object : MutableMap.MutableEntry<KR, VR> {
                        override val key: KR get() = entry.key.let(kTransform)
                        override val value: VR get() = entry.value.let(vTransform)
                        override fun setValue(newValue: VR): VR =
                            entry.setValue(newValue.let(vTransformBack)).let(vTransform)

                        override fun hashCode(): Int = 17 * 31 + (key?.hashCode() ?: 0) + (value?.hashCode() ?: 0)
                        override fun toString(): String = "$key=$value"
                        override fun equals(other: Any?): Boolean {
                            if (other == null || other !is Map.Entry<*, *>) return false
                            return other.key == key && other.value == value
                        }
                    }
                } as ((MutableMap.MutableEntry<K, V>) -> MutableMap.MutableEntry<KR, VR>), // type inference bug
                transformBack = { entry ->
                    object : MutableMap.MutableEntry<K, V> {
                        override val key: K get() = entry.key.let(kTransformBack)
                        override val value: V get() = entry.value.let(vTransformBack)
                        override fun setValue(newValue: V): V =
                            entry.setValue(newValue.let(vTransform)).let(vTransformBack)

                        override fun hashCode(): Int = 17 * 31 + (key?.hashCode() ?: 0) + (value?.hashCode() ?: 0)
                        override fun toString(): String = "$key=$value"
                        override fun equals(other: Any?): Boolean {
                            if (other == null || other !is Map.Entry<*, *>) return false
                            return other.key == key && other.value == value
                        }
                    }
                }
            )
        override val keys: MutableSet<KR>
            get() = this@shadowMap.keys.shadowMap(kTransform, kTransformBack)
        override val values: MutableCollection<VR>
            get() = this@shadowMap.values.shadowMap(vTransform, vTransformBack)

        override fun clear() = this@shadowMap.clear()
        override fun put(key: KR, value: VR): VR? =
            this@shadowMap.put(key.let(kTransformBack), value.let(vTransformBack))?.let(vTransform)

        override fun putAll(from: Map<out KR, VR>) {
            from.forEach { (kr, vr) ->
                this@shadowMap[kr.let(kTransformBack)] = vr.let(vTransformBack)
            }
        }

        override fun remove(key: KR): VR? = this@shadowMap.remove(key.let(kTransformBack))?.let(vTransform)
        override fun toString(): String = this@shadowMap.toString()
        override fun hashCode(): Int = this@shadowMap.hashCode()
    }
}

internal inline fun <E, R> MutableCollection<E>.shadowMap(
    crossinline transform: (E) -> R,
    crossinline transformBack: (R) -> E
): MutableCollection<R> {
    return object : MutableCollection<R> {
        override val size: Int get() = this@shadowMap.size

        override fun contains(element: R): Boolean = this@shadowMap.any { it.let(transform) == element }
        override fun containsAll(elements: Collection<R>): Boolean = elements.all(::contains)
        override fun isEmpty(): Boolean = this@shadowMap.isEmpty()
        override fun iterator(): MutableIterator<R> = object : MutableIterator<R> {
            private val delegate = this@shadowMap.iterator()
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): R = delegate.next().let(transform)
            override fun remove() = delegate.remove()
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
        }

        override fun add(element: R): Boolean = this@shadowMap.add(element.let(transformBack))

        override fun addAll(elements: Collection<R>): Boolean = this@shadowMap.addAll(elements.map(transformBack))
        override fun clear() = this@shadowMap.clear()

        override fun remove(element: R): Boolean = this@shadowMap.removeIf { it.let(transform) == element }
        override fun removeAll(elements: Collection<R>): Boolean = elements.all(::remove)
        override fun retainAll(elements: Collection<R>): Boolean = this@shadowMap.retainAll(elements.map(transformBack))
        override fun toString(): String = this@shadowMap.toString()
        override fun hashCode(): Int = this@shadowMap.hashCode()
    }
}

internal inline fun <E, R> MutableList<E>.shadowMap(
    crossinline transform: (E) -> R,
    crossinline transformBack: (R) -> E
): MutableList<R> {
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
        }

        override fun remove(element: R): Boolean = this@shadowMap.removeIf { it.let(transform) == element }
        override fun removeAll(elements: Collection<R>): Boolean = elements.all(::remove)
        override fun removeAt(index: Int): R = this@shadowMap.removeAt(index).let(transform)
        override fun retainAll(elements: Collection<R>): Boolean = this@shadowMap.retainAll(elements.map(transformBack))
        override fun set(index: Int, element: R): R =
            this@shadowMap.set(index, element.let(transformBack)).let(transform)

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<R> =
            this@shadowMap.subList(fromIndex, toIndex).map(transform).toMutableList()

        override fun toString(): String = this@shadowMap.toString()
        override fun hashCode(): Int = this@shadowMap.hashCode()
    }
}


internal inline fun <E, R> MutableSet<E>.shadowMap(
    crossinline transform: (E) -> R,
    crossinline transformBack: (R) -> E
): MutableSet<R> {
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
        }

        override fun add(element: R): Boolean = this@shadowMap.add(element.let(transformBack))
        override fun addAll(elements: Collection<R>): Boolean = this@shadowMap.addAll(elements.map(transformBack))
        override fun clear() = this@shadowMap.clear()

        override fun remove(element: R): Boolean = this@shadowMap.removeIf { it.let(transform) == element }
        override fun removeAll(elements: Collection<R>): Boolean = elements.all(::remove)
        override fun retainAll(elements: Collection<R>): Boolean = this@shadowMap.retainAll(elements.map(transformBack))
        override fun toString(): String = this@shadowMap.toString()
        override fun hashCode(): Int = this@shadowMap.hashCode()
    }
}

internal inline fun <T> dynamicList(crossinline supplier: () -> List<T>): List<T> {
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
        override fun toString(): String = supplier().toString()
        override fun hashCode(): Int = supplier().hashCode()
    }
}

internal inline fun <T> dynamicSet(crossinline supplier: () -> Set<T>): Set<T> {
    return object : Set<T> {
        override val size: Int get() = supplier().size
        override fun contains(element: T): Boolean = supplier().contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = supplier().containsAll(elements)
        override fun isEmpty(): Boolean = supplier().isEmpty()
        override fun iterator(): Iterator<T> = supplier().iterator()
        override fun toString(): String = supplier().toString()
        override fun hashCode(): Int = supplier().hashCode()
    }
}


internal inline fun <T> dynamicMutableList(crossinline supplier: () -> MutableList<T>): MutableList<T> {
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
        override fun toString(): String = supplier().toString()
        override fun hashCode(): Int = supplier().hashCode()
    }
}


internal inline fun <T> dynamicMutableSet(crossinline supplier: () -> MutableSet<T>): MutableSet<T> {
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
        override fun toString(): String = supplier().toString()
        override fun hashCode(): Int = supplier().hashCode()
    }
}

@Suppress("UNCHECKED_CAST", "USELESS_CAST") // type inference bug
internal inline fun <K, V> MutableMap<K, V>.observable(crossinline onChanged: () -> Unit): MutableMap<K, V> {
    return object : MutableMap<K, V>, Map<K, V> by (this as Map<K, V>) {
        override val keys: MutableSet<K>
            get() = this@observable.keys.observable(onChanged)
        override val values: MutableCollection<V>
            get() = this@observable.values.observable(onChanged)
        override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
            get() = this@observable.entries.observable(onChanged)

        override fun clear() = this@observable.clear().also { onChanged() }
        override fun put(key: K, value: V): V? = this@observable.put(key, value).also { onChanged() }
        override fun putAll(from: Map<out K, V>) = this@observable.putAll(from).also { onChanged() }
        override fun remove(key: K): V? = this@observable.remove(key).also { onChanged() }
        override fun toString(): String = this@observable.toString()
        override fun hashCode(): Int = this@observable.hashCode()
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
        }

        override fun remove(element: T): Boolean = this@observable.remove(element).also { onChanged() }
        override fun removeAll(elements: Collection<T>): Boolean =
            this@observable.removeAll(elements).also { onChanged() }

        override fun removeAt(index: Int): T = this@observable.removeAt(index).also { onChanged() }
        override fun retainAll(elements: Collection<T>): Boolean =
            this@observable.retainAll(elements).also { onChanged() }

        override fun set(index: Int, element: T): T = this@observable.set(index, element).also { onChanged() }
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = this@observable.subList(fromIndex, toIndex)
        override fun toString(): String = this@observable.toString()
        override fun hashCode(): Int = this@observable.hashCode()
    }
}

internal inline fun <T> MutableCollection<T>.observable(crossinline onChanged: () -> Unit): MutableCollection<T> {
    return object : MutableCollection<T> {
        override val size: Int get() = this@observable.size
        override fun contains(element: T): Boolean = this@observable.contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = this@observable.containsAll(elements)
        override fun isEmpty(): Boolean = this@observable.isEmpty()
        override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
            private val delegate = this@observable.iterator()
            override fun hasNext(): Boolean = delegate.hasNext()
            override fun next(): T = delegate.next()
            override fun remove() = delegate.remove().also { onChanged() }
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
        }

        override fun add(element: T): Boolean = this@observable.add(element).also { onChanged() }
        override fun addAll(elements: Collection<T>): Boolean = this@observable.addAll(elements).also { onChanged() }
        override fun clear() = this@observable.clear().also { onChanged() }
        override fun remove(element: T): Boolean = this@observable.remove(element).also { onChanged() }
        override fun removeAll(elements: Collection<T>): Boolean =
            this@observable.removeAll(elements).also { onChanged() }

        override fun retainAll(elements: Collection<T>): Boolean =
            this@observable.retainAll(elements).also { onChanged() }

        override fun toString(): String = this@observable.toString()
        override fun hashCode(): Int = this@observable.hashCode()
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
            override fun toString(): String = delegate.toString()
            override fun hashCode(): Int = delegate.hashCode()
        }

        override fun add(element: T): Boolean = this@observable.add(element).also { onChanged() }
        override fun addAll(elements: Collection<T>): Boolean = this@observable.addAll(elements).also { onChanged() }
        override fun clear() = this@observable.clear().also { onChanged() }
        override fun remove(element: T): Boolean = this@observable.remove(element).also { onChanged() }
        override fun removeAll(elements: Collection<T>): Boolean =
            this@observable.removeAll(elements).also { onChanged() }

        override fun retainAll(elements: Collection<T>): Boolean =
            this@observable.retainAll(elements).also { onChanged() }

        override fun toString(): String = this@observable.toString()
        override fun hashCode(): Int = this@observable.hashCode()
    }
}


@OptIn(InternalSerializationApi::class)
internal fun <R : Any> Any.smartCastPrimitive(clazz: KClass<R>): R {
    kotlin.runCatching {
        return Yaml.default.decodeFromString(clazz.serializer(), this.toString())
    }.getOrElse {
        throw IllegalArgumentException("Cannot cast '$this' to ${clazz.qualifiedName}", it)
    }
}

