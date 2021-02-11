/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DuplicatedCode")

package net.mamoe.mirai.console.internal.data

import java.util.concurrent.ConcurrentMap
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

// TODO: 2020/6/24 优化性能: 引入一个 comparator 之类来替代将 Int 包装为 Value<Int> 后进行 containsKey 比较的方法

// java.util.function was used in mirai-core
// direct improve apis of java.util.function


@Suppress(
    "MANY_IMPL_MEMBER_NOT_IMPLEMENTED", "MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED",
    "UNCHECKED_CAST", "USELESS_CAST", "ACCIDENTAL_OVERRIDE", "TYPE_MISMATCH",
    "NOTHING_TO_OVERRIDE", "EXPLICIT_OVERRIDE_REQUIRED_IN_MIXED_MODE", "CONFLICTING_INHERITED_JVM_DECLARATIONS",
) // for improve java.util.function apis
internal open class ShadowMap<K, V, KR, VR>(
    @JvmField protected val originMapComputer: () -> MutableMap<K, V>,
    @JvmField protected val kTransform: (K) -> KR,
    @JvmField protected val kTransformBack: (KR) -> K,
    @JvmField protected val vTransform: (V) -> VR,
    @JvmField protected val vTransformBack: (VR) -> V
) : MutableMap<KR, VR> {
    override val size: Int get() = originMapComputer().size
    override fun containsKey(key: KR): Boolean = originMapComputer().containsKey(key.let(kTransformBack))
    override fun containsValue(value: VR): Boolean = originMapComputer().containsValue(value.let(vTransformBack))
    override fun get(key: KR): VR? = originMapComputer()[key.let(kTransformBack)]?.let(vTransform)
    override fun isEmpty(): Boolean = originMapComputer().isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<KR, VR>>
        get() = originMapComputer().entries.shadowMap(
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
        get() = originMapComputer().keys.shadowMap(kTransform, kTransformBack)
    override val values: MutableCollection<VR>
        get() = originMapComputer().values.shadowMap(vTransform, vTransformBack)

    override fun clear() = originMapComputer().clear()
    override fun put(key: KR, value: VR): VR? =
        originMapComputer().put(key.let(kTransformBack), value.let(vTransformBack))?.let(vTransform)

    override fun putAll(from: Map<out KR, VR>) {
        from.forEach { (kr, vr) ->
            originMapComputer()[kr.let(kTransformBack)] = vr.let(vTransformBack)
        }
    }

    override fun remove(key: KR): VR? = originMapComputer().remove(key.let(kTransformBack))?.let(vTransform)
    override fun remove(key: KR, value: VR): Boolean =
        originMapComputer().remove(key.let(kTransformBack), value?.let(vTransformBack))


    override fun toString(): String = originMapComputer().toString()
    override fun hashCode(): Int = originMapComputer().hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShadowMap<*, *, *, *>

        if (originMapComputer != other.originMapComputer) return false
        if (kTransform != other.kTransform) return false
        if (kTransformBack != other.kTransformBack) return false
        if (vTransform != other.vTransform) return false
        if (vTransformBack != other.vTransformBack) return false

        return true
    }

    override fun putIfAbsent(key: KR, value: VR): VR? =
        originMapComputer().putIfAbsent(key.let(kTransformBack), value.let(vTransformBack))?.let(vTransform)

    override fun replace(key: KR, oldValue: VR, newValue: VR): Boolean =
        originMapComputer().replace(key.let(kTransformBack), oldValue.let(vTransformBack), newValue.let(vTransformBack))

    override fun replace(key: KR, value: VR): VR? =
        originMapComputer().replace(key.let(kTransformBack), value.let(vTransformBack))?.let(vTransform)

    override fun compute(key: KR, remappingFunction: BiFunction<in KR, in VR?, out VR?>): VR? =
        originMapComputer().compute(key.let(kTransformBack)) { k1, v1 ->
            remappingFunction.apply(k1.let(kTransform), v1?.let(vTransform))?.let(vTransformBack)
        }?.let(vTransform)

    override fun computeIfAbsent(key: KR, mappingFunction: Function<in KR, out VR>): VR =
        originMapComputer().computeIfAbsent(key.let(kTransformBack)) { k ->
            mappingFunction.apply(k.let(kTransform)).let(vTransformBack)
        }.let(vTransform)

    override fun computeIfPresent(key: KR, remappingFunction: BiFunction<in KR, in VR, out VR?>): VR? =
        originMapComputer().computeIfPresent(key.let(kTransformBack)) { k, v ->
            remappingFunction.apply(k.let(kTransform), v.let(vTransform))?.let(vTransformBack)
        }?.let(vTransform)

    override fun merge(key: KR, value: VR, remappingFunction: BiFunction<in VR, in VR, out VR?>): VR? =
        originMapComputer().merge(key.let(kTransformBack), value.let(vTransformBack)) { k, v ->
            remappingFunction.apply(k.let(vTransform), v.let(vTransform))?.let(vTransformBack)
        }?.let(vTransform)

    override fun forEach(action: BiConsumer<in KR, in VR>) {
        @Suppress("JavaMapForEach")
        originMapComputer().forEach { t, u ->
            action.accept(t.let(kTransform), u.let(vTransform))
        }
    }

    override fun replaceAll(function: BiFunction<in KR, in VR, out VR>) {
        originMapComputer().replaceAll { t, u ->
            function.apply(t.let(kTransform), u.let(vTransform))?.let(vTransformBack)
        }
    }
}


@Suppress(
    "MANY_IMPL_MEMBER_NOT_IMPLEMENTED", "MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED",
    "UNCHECKED_CAST", "USELESS_CAST", "ACCIDENTAL_OVERRIDE", "TYPE_MISMATCH",
    "EXPLICIT_OVERRIDE_REQUIRED_IN_MIXED_MODE", "CONFLICTING_INHERITED_JVM_DECLARATIONS"
)
internal open class ConcurrentShadowMap<K, V, KR, VR>(
    originMapComputer: () -> MutableMap<K, V>,
    kTransform: (K) -> KR,
    kTransformBack: (KR) -> K,
    vTransform: (V) -> VR,
    vTransformBack: (VR) -> V
) : ShadowMap<K, V, KR, VR>(
    originMapComputer, kTransform, kTransformBack, vTransform, vTransformBack
), ConcurrentMap<KR, VR>

internal fun <K, V, KR, VR> MutableMap<K, V>.shadowMap(
    kTransform: (K) -> KR,
    kTransformBack: (KR) -> K,
    vTransform: (V) -> VR,
    vTransformBack: (VR) -> V
): MutableMap<KR, VR> = if (this is ConcurrentMap<K, V>) {
    ConcurrentShadowMap({ this }, kTransform, kTransformBack, vTransform, vTransformBack)
} else {
    ShadowMap({ this }, kTransform, kTransformBack, vTransform, vTransformBack)
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

/*

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
*/

@Suppress(
    "UNCHECKED_CAST", "USELESS_CAST",
    "ACCIDENTAL_OVERRIDE", "TYPE_MISMATCH", "NOTHING_TO_OVERRIDE",
    "MANY_IMPL_MEMBER_NOT_IMPLEMENTED", "MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED",
    "UNCHECKED_CAST", "USELESS_CAST", "ACCIDENTAL_OVERRIDE",
    "EXPLICIT_OVERRIDE_REQUIRED_IN_MIXED_MODE", "CONFLICTING_INHERITED_JVM_DECLARATIONS"
) // type inference bug
internal fun <K, V> MutableMap<K, V>.observable(onChanged: () -> Unit): MutableMap<K, V> {
    open class ObservableMap : MutableMap<K, V> by (this as MutableMap<K, V>) {
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
        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other === this) return true
            return this@observable == other
        }

        override fun remove(key: K, value: V): Boolean = this@observable.remove(key, value).also { onChanged() }

        override fun putIfAbsent(key: K, value: V): V? =
            this@observable.putIfAbsent(key, value).also { onChanged() }

        override fun replace(key: K, oldValue: V, newValue: V): Boolean =
            this@observable.replace(key, oldValue, newValue).also { onChanged() }

        override fun replace(key: K, value: V): V? =
            this@observable.replace(key, value).also { onChanged() }
        override fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V>): V =
            this@observable.computeIfAbsent(key, mappingFunction).also { onChanged() }

        override fun replaceAll(function: BiFunction<in K, in V, out V>) =
            this@observable.replaceAll(function).also { onChanged() }

        override fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V?>): V? =
            this@observable.compute(key, remappingFunction).also { onChanged() }

        override fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V, out V?>): V? =
            this@observable.computeIfPresent(key, remappingFunction).also { onChanged() }

        override fun merge(key: K, value: V, remappingFunction: BiFunction<in V, in V, out V?>): V? =
            this@observable.merge(key, value, remappingFunction).also { onChanged() }
    }

    @Suppress(
        "MANY_IMPL_MEMBER_NOT_IMPLEMENTED", "MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED",
        "UNCHECKED_CAST", "USELESS_CAST", "ACCIDENTAL_OVERRIDE", "TYPE_MISMATCH",
        "EXPLICIT_OVERRIDE_REQUIRED_IN_MIXED_MODE", "CONFLICTING_INHERITED_JVM_DECLARATIONS"
    )
    return if (this is ConcurrentMap<*, *>) {
        object : ConcurrentMap<K, V>, MutableMap<K, V>, ObservableMap() {}
    } else ObservableMap()
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

/*
@OptIn(InternalSerializationApi::class)
internal fun <R : Any> Any.smartCastPrimitive(clazz: KClass<R>): R {
    kotlin.runCatching {
        return Yaml.default.decodeFromString(clazz.serializer(), this.toString())
    }.getOrElse {
        throw IllegalArgumentException("Cannot cast '$this' to ${clazz.qualifiedName}", it)
    }
}

*/