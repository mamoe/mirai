package net.mamoe.mirai.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object NetworkScope : CoroutineScope by CoroutineScope(Dispatchers.Default)