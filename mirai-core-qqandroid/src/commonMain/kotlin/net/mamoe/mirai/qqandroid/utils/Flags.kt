package net.mamoe.mirai.qqandroid.utils

inline class MacOrAndroidIdChangeFlag(val value: Long = 0) {
    fun macChanged(): MacOrAndroidIdChangeFlag =
        MacOrAndroidIdChangeFlag(this.value or 0x1)

    fun androidIdChanged(): MacOrAndroidIdChangeFlag =
        MacOrAndroidIdChangeFlag(this.value or 0x2)

    fun guidChanged(): MacOrAndroidIdChangeFlag =
        MacOrAndroidIdChangeFlag(this.value or 0x3)

    companion object {
        val NoChange: MacOrAndroidIdChangeFlag get() = MacOrAndroidIdChangeFlag()
    }
}