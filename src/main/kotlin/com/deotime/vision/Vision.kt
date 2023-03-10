@file:Suppress("UNCHECKED_CAST")

package com.deotime.vision

import com.deotime.vision.Vision.Companion.plus
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class Vision<out T>(
    private val compute: () -> List<View<T>>
) : Sequence<Vision.View<T>> by (Sequence { compute().iterator() }) {
    constructor(views: List<View<T>>) : this({ views })

    interface View<out T> {
        fun get(): T
        fun <Key> unlock(type: KType, closure: (Unlocked<Key>) -> Unit)

        interface Unlocked<out T> : View<T> {
            fun set(value: @UnsafeVariance T)
        }

        class Simple<T>(
            private val _get: () -> T,
            private val _set: (T) -> Unit,
            private val type: KType
        ) : View<T> {
            constructor(prop: KMutableProperty0<T>) : this(prop::get, prop::set, prop.returnType)

            override fun get() = _get()
            override fun <Key> unlock(type: KType, closure: (Unlocked<Key>) -> Unit) {
                if (type == this.type || type.isSubtypeOf(this.type))
                    closure(object : View<Key> by (this@Simple as View<Key>), Unlocked<Key> {
                        override fun set(value: @UnsafeVariance Key) =
                            (_set as (Key) -> Unit)(value)
                    })
            }
        }

        companion object {
            inline fun <reified Key> View<*>.unlock(noinline closure: (Unlocked<Key>) -> Unit) =
                unlock(typeOf<Key>(), closure)
        }
    }

    companion object {
        operator fun <T> Vision<T>.plus(other: Vision<T>) =
            Vision { compute() + other.compute() }

        private val Empty = Vision<Nothing>(emptyList())
        fun <T> empty(): Vision<T> = Empty
    }
}

fun <T> vision(vararg props: KMutableProperty0<out T>): Vision<T> =
    Vision(props.map { Vision.View.Simple(it) })

inline fun <reified T> vision(prop: KProperty0<MutableList<T>>): Vision<T> =
    vision(typeOf<T>(), prop)

inline fun <reified T> vision(vararg props: KProperty0<MutableList<T>>): Vision<T> =
    vision(typeOf<T>(), *props)

fun <T> vision(type: KType, prop: KProperty0<MutableList<T>>): Vision<T> =
    Vision {
        val list = prop()
        List(list.size) { i ->
            Vision.View.Simple(
                { list[i] },
                { list[i] = it },
                type
            )
        }
    }

fun <T> vision(type: KType, vararg props: KProperty0<MutableList<T>>): Vision<T> =
    when (props.size) {
        0 -> Vision.empty()
        1 -> vision(type, props.first())
        else -> {
            props.map { prop -> vision(type, prop) }.fold(Vision.empty()) { a, b -> a + b }
        }
    }