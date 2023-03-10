package com.deotime.vision

interface Eyes<T> {
    val sight: Vision<T> get() = Vision.empty()
}