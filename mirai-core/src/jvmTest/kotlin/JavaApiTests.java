/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 仅用来测试调用，不会被单元测试运行
 */
@SuppressWarnings({"unused", "UnusedAssignment"})
public class JavaApiTests {
    @NotNull
    Bot bot;

    public JavaApiTests(@NotNull Bot bot) {
        this.bot = bot;
    }

    public void generalCalls() {
        bot.login();

        bot.getAsFriend().sendMessage("test"); // blocking bridge
        bot.getOtherClients().getOrFail(1).getBot();
    }

    public void events() {
        bot.getEventChannel().subscribe(MessageEvent.class, event -> ListeningStatus.LISTENING);

        bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            Bot b = event.getBot();

        });

        SimpleListenerHost slh = new SimpleListenerHost() {

            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
            }

            @EventHandler
            public void onMsg(GroupMessageEvent e) {

            }
        };

        bot.getEventChannel().registerListenerHost(slh);
    }

    @NotNull
    private <T> T magic() {
        throw new RuntimeException();
    }

    @NotNull
    MessageChain chain = magic();

    @NotNull
    Contact contact = magic();

    public void messages() {

        Image image = (Image) chain.stream().filter(Image.class::isInstance).findFirst().orElse(null);

        assert image != null;

        String url = Image.queryUrl(image);
        Image.fromId("123");

        MessageSource source = magic();

        MessageUtils.getBot(source);
        MessageUtils.calculateImageMd5(image);
        MessageUtils.isContentEmpty(image);

        MessageSource.quote(source);
        MessageSource.quote(chain);

        MessageSource.recall(source);
    }

    ExternalResource resource = magic();

    public void externalResource() throws IOException {
        resource.inputStream();


        contact.uploadImage(resource); // base method


        ExternalResource r;

        r = ExternalResource.create((InputStream) magic()); // throws IOException
        r = ExternalResource.create((File) magic());
        r = ExternalResource.create((RandomAccessFile) magic());

        ExternalResource.uploadAsImage(r, contact);  // returns Image

        ExternalResource.sendAsImage(r, contact);    // returns MessageReceipt


        ExternalResource.uploadAsImage((ExternalResource) magic(), contact);    // returns Image
        ExternalResource.uploadAsImage((File) magic(), contact);                // returns Image
        ExternalResource.uploadAsImage((InputStream) magic(), contact);         // returns Image

        ExternalResource.sendAsImage((ExternalResource) magic(), contact);  // returns MessageReceipt
        ExternalResource.sendAsImage((File) magic(), contact);              // returns MessageReceipt
        ExternalResource.sendAsImage((InputStream) magic(), contact);       // returns MessageReceipt

        Contact.uploadImage(contact, (ExternalResource) magic());   // returns Image
        Contact.uploadImage(contact, (File) magic());               // returns Image
        Contact.uploadImage(contact, (InputStream) magic());        // returns Image

        Contact.sendImage(contact, (ExternalResource) magic());     // returns MessageReceipt
        Contact.sendImage(contact, (File) magic());                 // returns MessageReceipt
        Contact.sendImage(contact, (InputStream) magic());          // returns MessageReceipt

        // experimental
        ExternalResource.uploadAsVoice(magic(), (Group) contact);
    }

    public static void main(String[] args) {
        Bot bot = BotFactory.INSTANCE.newBot(11, "", configuration -> {
            configuration.fileBasedDeviceInfo();
            configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PHONE);
        });

        new JavaApiTests(bot);
    }
}
