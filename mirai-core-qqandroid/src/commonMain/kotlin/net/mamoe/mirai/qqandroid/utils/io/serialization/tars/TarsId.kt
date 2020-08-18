package net.mamoe.mirai.qqandroid.utils.io.serialization.tars

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * The serial id used in Tars serialization
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
internal annotation class TarsId(val id: Int)

