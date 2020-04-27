/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.utils

/**
 * 这不允许多线程同时操作. 在初始化完成后就不要再次修改
 */
open class InsertLink<T> : Iterable<T?> {
    open class Node<T>(open var value: T?) {
        constructor() : this(null)

        open var previous: Node<T>? = null
        open var next: Node<T>? = null
        open fun insertBefore(v: T?): Node<T> {
            val newNode = Node(v)
            val op = previous
            previous = newNode
            newNode.previous = op
            op?.next = newNode
            newNode.next = this
            return newNode
        }

        open fun insertAfter(v: T?): Node<T> {
            val newNode = Node(v)
            val on = next
            next = newNode
            newNode.next = on
            newNode.previous = this
            on?.previous = newNode
            return newNode
        }
    }

    val head = object : Node<T>() {
        override var previous: Node<T>?
            get() = null
            set(_) {
                throw IllegalStateException("Head cannot set previous")
            }

        override fun insertBefore(v: T?) = insertAfter(v)
    }
    val tail = object : Node<T>() {
        override fun insertAfter(v: T?) = insertBefore(v)
        override var next: Node<T>?
            get() = null
            set(_) {
                throw IllegalStateException("Tail cannot set next value")
            }
    }

    init {
        head.next = tail
        tail.previous = head
    }

    override fun iterator(): Iterator<T?> {
        class It<T>(var current: Node<T>?) : Iterator<T?> {
            override fun hasNext(): Boolean {
                while (true) {
                    val c = current ?: return false
                    if (c.value == null) {
                        if (current == tail) {
                            current = null
                            return false
                        }
                        current = c.next
                    } else break
                }
                return true
            }

            override fun next(): T? {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                val c = (current ?: error("current not found"))
                current = c.next
                return c.value
            }

        }
        return It(head)
    }
}