package com.deotime.vision.test

import com.deotime.vision.Eyes
import com.deotime.vision.Vision
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
    }
    data class TestData(
        var test1: Tester.Thing = Tester.Thing.One,
        var test2: Tester = Tester.Other,
        val testList: MutableList<Tester> = mutableListOf()
    ) : Eyes<Tester> {
        override val sight = vision(::test1, ::test2) + visions(::testList)
    }

    @Test
    fun test() {
        val test = TestData()
        val sight = test.sight
        sight.forEach {
            println("hi 1")
        }
        sight.forEach {
            println("hi 2")
        }
    }


}