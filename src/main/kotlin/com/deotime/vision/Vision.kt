@file:Suppress("UNCHECKED_CAST")

package com.deotime.vision

import com.deotime.vision.Vision.Companion.plus
import com.deotime.vision.Vision.View.Companion.unlock
import com.deotime.vision.Vision.View.Companion.unlockable
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class Vision<out T>(
    private val compute: () -> Iterable<View<T>>
) {
    constructor(views: List<View<T>>) : this({ views })

    fun views() = compute()

    interface View<out T> {
        fun get(): T
        fun unlockable(type: KType): Boolean
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
            override fun unlockable(type: KType) = type == this.type || type.isSubtypeOf(this.type)
            override fun <Key> unlock(type: KType, closure: (Unlocked<Key>) -> Unit) {
                if (unlockable(type))
                    closure(object : View<Key> by (this@Simple as View<Key>), Unlocked<Key> {
                        override fun set(value: @UnsafeVariance Key) =
                            (_set as (Key) -> Unit)(value)
                    })
            }
        }

        companion object {
            inline fun <reified Key> View<*>.unlockable() = unlockable(typeOf<Key>())
            inline fun <reified Key> View<*>.unlock(noinline closure: (Unlocked<Key>) -> Unit) =
                unlock(typeOf<Key>(), closure)
        }
    }


    companion object {

        /**
         * @throws [StackOverflowError] if there is a cyclic reference
         */
        inline fun <reified T> Vision<T>.deepViews() = _deepViews(typeOf<T>())

        @PublishedApi
        internal fun <T> Vision<T>._deepViews(type: KType): List<View<T>> = views().let { views ->
            views + views.mapNotNull { (it.get() as? Eyes<*>)?.sight }
                .flatMap { it._deepViews(type).filter { it.unlockable(type) } as List<View<T>> }
        }

        operator fun <T> Vision<T>.plus(other: Vision<T>) = Vision { compute() + other.compute() }

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

fun <T> blurred(vararg props: KMutableProperty0<T?>): Vision<T> =
    Vision {
        props.filter { it() != null }.map { Vision.View.Simple(it as KMutableProperty0<T>) }
    }

fun <T> eyesight(vararg eyes: KProperty0<Eyes<T>>): Vision<T> = Vision {
    eyes.flatMap { it().sight.views() }
}

@JvmName("eyesight_iterable")
fun <T> eyesight(vararg eyes: KProperty0<Iterable<Eyes<T>>>): Vision<T> = Vision {
    eyes.flatMap { it() }.flatMap { it.sight.views() }
}