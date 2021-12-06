package org.example.myplugin

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File

class ResourceNotClosedInspectionTest {
    suspend fun useResource() {
        val file = magic<File>()
        val contact = magic<Contact>()

        contact.uploadImage(file.toExternalResource()) // should report warning
        contact.sendImage(file) // should report warning

        //file.toExternalResource().uploadAsImage(contact)

        file.toExternalResource().uploadAsImage(contact)
        file.toExternalResource().sendAsImageTo(contact)

        // contact.uploadImage(file) // should ok

        // replace to net.mamoe.mirai.contact.Contact.Companion.uploadImage
    }
}

fun <T> magic(): T = null!!