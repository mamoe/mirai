package net.mamoe.mirai.message.data

sealed class FlashImage : MessageContent {
    companion object Key : Message.Key<FlashImage>

    abstract val image : Image
}

abstract class AbstractGroupFlashImage : FlashImage() {
    abstract override val image: GroupImage
}

abstract class AbstractFriendFlashImage : FlashImage() {
    abstract override val image: FriendImage
}