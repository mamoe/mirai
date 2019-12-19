package net.mamoe.mirai.contact

import net.mamoe.mirai.test.shouldBeEqualTo
import org.junit.Test

@UseExperimental(ExperimentalUnsignedTypes::class)
internal class GroupIdConversionsKtTest {
    @Test
    fun checkToInternalId() {
        GroupId(221056495).toInternalId().value shouldBeEqualTo 4111056495
        //  61 056495
        //4111 056495
    }
}