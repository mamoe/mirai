/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import net.mamoe.mirai.internal.test.AbstractTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private open class TestComponent {
    companion object : ComponentKey<TestComponent>
}


private interface MyInterface

private open class TestComponentExt : TestComponent(), MyInterface

private open class Key<R> : ComponentKey<R> where R : TestComponent, R : MyInterface
private class KeyActual : Key<TestComponentExt>()


internal class ComponentKeyTest : AbstractTest() {

    @Test
    fun testComponentName() {
        assertEquals("TestComponent", TestComponent.componentName(false))
        assertEquals(TestComponent::class.qualifiedName!!, TestComponent.componentName(true))
    }

    @Test
    fun `componentName with erased type argument`() {
        assertEquals(
            "TestComponent & MyInterface",
            Key<TestComponentExt>().componentName(false)
        )
        assertEquals(
            "${TestComponent::class.qualifiedName!!} & ${MyInterface::class.qualifiedName!!}",
            Key<TestComponentExt>().componentName(true)
        )
    }

    @Test
    fun `componentName with actual type argument`() {
        assertEquals(
            "TestComponentExt",
            KeyActual().componentName(false)
        )
        assertEquals(
            TestComponentExt::class.qualifiedName!!,
            KeyActual().componentName(true)
        )
    }

    @Test
    fun `test smartToString`() {
        assertEquals("ComponentKey<TestComponent>", TestComponent.smartToString(false))
        assertEquals("ComponentKey<${TestComponent::class.qualifiedName!!}>", TestComponent.smartToString(true))
        assertEquals(
            "ComponentKey<${TestComponent::class.qualifiedName!!} & ${MyInterface::class.qualifiedName!!}>",
            Key<TestComponentExt>().smartToString(true)
        )
        assertEquals("ComponentKey<${TestComponentExt::class.qualifiedName!!}>", KeyActual().smartToString(true))
    }
}