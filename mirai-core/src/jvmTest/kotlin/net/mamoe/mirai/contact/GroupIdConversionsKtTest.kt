package net.mamoe.mirai.contact

import net.mamoe.mirai.test.shouldBeEqualTo
import org.junit.Test
import kotlin.random.Random

@UseExperimental(ExperimentalUnsignedTypes::class)
internal class GroupIdConversionsKtTest {
    @Test
    fun checkToInternalId() {
        GroupId(221056495u).toInternalId().value shouldBeEqualTo 4111056495u
        //  61 056495
        //4111 056495
    }
}