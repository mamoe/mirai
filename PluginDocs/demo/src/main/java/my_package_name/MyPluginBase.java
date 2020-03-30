//在这里创建你的PluginBase, 他应该是一个object
/**
kotlin example


object ExamplePluginMain : PluginBase() {
    override fun onLoad() {
        super.onLoad()
    }

    override fun onEnable() {
        super.onEnable()

        logger.info("Plugin loaded!")

        subscribeMessages {
            "greeting" reply { "Hello ${sender.nick}" }
        }

        subscribeAlways<MessageRecallEvent> { event ->
            logger.info { "${event.authorId} 的消息被撤回了" }
        }
    }
}



java example


class ExamplePluginBase extends PluginBase {

    public void onLoad(){
        bot.getFriends().forEach(friend -> {
            System.out.println(friend.getId() + ":" + friend.getNick());
            return Unit.INSTANCE; // kotlin 的所有函数都有返回值. Unit 为最基本的返回值. 请在这里永远返回 Unit
        });

        Events.subscribeAlways(GroupMessage.class, (GroupMessage event) -> {

            if (event.getMessage().contains("reply")) {
                // 引用回复
                final QuoteReplyToSend quote = MessageUtils.quote(event.getMessage(), event.getSender());
                event.getGroup().sendMessage(quote.plus("引用回复"));

            } else if (event.getMessage().contains("at")) {
                // at
                event.getGroup().sendMessage(new At(event.getSender()));

            } else if (event.getMessage().contains("permission")) {
                // 成员权限
                event.getGroup().sendMessage(event.getPermission().toString());

            } else if (event.getMessage().contains("mixed")) {
                // 复合消息, 通过 .plus 连接两个消息
                event.getGroup().sendMessage(
                        MessageUtils.newImage("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png") // 演示图片, 可能已过期
                                .plus("Hello") // 文本消息
                                .plus(new At(event.getSender())) // at 群成员
                                .plus(AtAll.INSTANCE) // at 全体成员
                );

            } else if (event.getMessage().contains("recall1")) {
                event.getGroup().sendMessage("你看不到这条消息").recall();
                // 发送消息马上就撤回. 因速度太快, 客户端将看不到这个消息.

            } else if (event.getMessage().contains("recall2")) {
                final Job job = event.getGroup().sendMessage("3秒后撤回").recallIn(3000);

                // job.cancel(new CancellationException()); // 可取消这个任务

            } else if (event.getMessage().contains("上传图片")) {
                File file = new File("myImage.jpg");
                if (file.exists()) {
                    final Image image = event.getGroup().uploadImage(new File("myImage.jpg"));
                    // 上传一个图片并得到 Image 类型的 Message

                    final String imageId = image.getImageId(); // 可以拿到 ID
                    final Image fromId = MessageUtils.newImage(imageId); // ID 转换得到 Image

                    event.getGroup().sendMessage(image); // 发送图片
                }

            } else if (event.getMessage().contains("friend")) {
                final Future<MessageReceipt<? extends Contact>> future = event.getSender().sendMessageAsync("Async send"); // 异步发送
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onEnable(){
        logger.info("Plugin loaded!");
    }

}
