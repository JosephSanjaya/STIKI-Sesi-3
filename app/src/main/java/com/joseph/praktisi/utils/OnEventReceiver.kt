package com.joseph.praktisi.utils

import androidx.compose.runtime.Stable

@Stable
fun interface OnEventReceiver<T> {
    fun onEvent(event: T)
}
