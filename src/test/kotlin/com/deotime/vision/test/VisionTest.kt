package com.deotime.vision.test

import com.deotime.vision.Eyes
import com.deotime.vision.Vision
import com.deotime.vision.Vision.Companion.plus
import com.deotime.vision.Vision.View.Companion.unlock
import com.deotime.vision.eyesight
import com.deotime.vision.vision
import com.deotime.vision.visions
import kotlin.test.Test
import kotlin.test.assertTrue

class VisionTest {
    sealed interface Tester {
        sealed interface Thing : Tester {
            object One : Thing
            object Two : Thing
        }
        object Other : Tester
        object Last : Tester
        object Special : Tester
    }
    data class TestData(
        var test1: Tester.Thing = Tester.Thing.One,
        var test2: Tester = Tester.Other,
        val testList: MutableList<Tester.Thing> = mutableListOf(),
        var innerTest: Inner = Inner()
    ) : Eyes<Tester> {
        override val sight = visions(::testList) + vision(::test1, ::test2) + eyesight(::innerTest)

        data class Inner(
            var innerTest: Tester = Tester.Special,
            var innerTest2: Tester = Tester.Special
        ) : Eyes<Tester> {
            override val sight = vision(::innerTest, ::innerTest2)
        }
    }

    @Test
    fun test() {
        val test = TestData()
        val sight = test.sight
        sight.views().forEach {
            println(it.get())
            it.unlock<Tester.Thing> {
                it.set(Tester.Thing.Two)
            }
        }
    }


}