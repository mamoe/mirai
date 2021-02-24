package test;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.example.myplugin.ResourceNotClosedInspectionTestKt;

import java.io.File;
import java.io.IOException;

import static org.example.myplugin.ResourceNotClosedInspectionTestKt.magic;

public class ResourceNotClosedInspectionTestJava {

    public static Object funA() {
        return new Object();
    }

    public static void funB(Object obj) {
        System.out.println(obj);
    }

    public static void main(String[] args) {
        // https://github.com/mamoe/mirai-console/issues/294
        funB(funA());

        File file = magic();
        Contact contact = magic();

        //  useImage(contact.uploadImage(ExternalResource.create(file)));
        useImage(Contact.uploadImage(contact, ExternalResource.create(file)));

        useImage(Contact.uploadImage(contact, file));

        try (final ExternalResource resource = ExternalResource.create(file)) {
            useImage(contact.uploadImage(resource));
        }
    }

    static void useImage(Image image) {

    }
}
