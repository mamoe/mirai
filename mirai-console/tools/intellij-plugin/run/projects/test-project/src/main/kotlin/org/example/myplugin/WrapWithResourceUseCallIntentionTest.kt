package org.example.myplugin

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import java.io.File

class WrapWithResourceUseCallIntentionTest {
    suspend fun test() {

        val file = magic<File>()
        val contact = magic<Contact>()
        val resource = magic<ExternalResource>()

        resource.sendAsImageTo(contact)
        resource.run { sendAsImageTo(contact) }
    }
}