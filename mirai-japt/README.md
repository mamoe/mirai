
# mirai-japt

Mirai Java Apt  

提供阻塞API 来让 Java 调用 Mirai 的 API 更容易

## 开始

```java
class Test{
    public static void main(String[] args){
        BlockingBot bot = BlockingBot.newInstance(123456, "");
        
        bot.login();
        
        bot.getFriendList().forEach(friend -> {
            System.out.println(friend.getNick());
        });
        
        Events.subscribeAlways(GroupMessage.class, (GroupMessage message) -> {
            final BlockingQQ sender = BlockingContacts.createBlocking(message.getSender());
        
            sender.sendMessage("Hello");
        });
        
        Thread.sleep(999999999);
    }
}
```

## 便捷开发

在 IntelliJ IDEA 或 Android Studio 中找到设置 `Editor -> General -> Postfix Completion`, 添加一个设置到 `Java` 分类中:  
![](.README_images/ce3034e3.png)  
Applicable expression types:
```
net.mamoe.mirai.contact.Contact
```
转换后表达式:
```
net.mamoe.mirai.japt.BlockingContacts.createBlocking($EXPR$)
```

效果:

![4SY8BC@J4ZKQM7OZ_~BC1I_1](.README_images/4SY8BC%40J4ZKQM%5D7OZ_~BC1I_1.png)

![722WEHTTXD6XFFH43](.README_images/722W%28E%24HTTX%7BD6XFFH%5D%5D%2443.png)
