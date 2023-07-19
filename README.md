# vision
Simple visitor pattern alternative

### Vision Example:
```kotlin
sealed interface Exampleer {
    sealed interface Thing : Exampleer {
        object One : Thing
        object Two : Thing
    }
    object Other : Exampleer
    object Last : Exampleer
    object Special : Exampleer
}

data class ExampleData(
    var example1: Exampleer.Thing = Exampleer.Thing.One,
    var example2: Exampleer = Exampleer.Other,
    val exampleList: MutableList<Exampleer.Thing> = mutableListOf(),
    var innerExample: Inner = Inner()
) : Eyes<Exampleer> {
    override val sight = vision(::exampleList) + vision(::example1, ::example2) + eyesight(::innerExample)

    data class Inner(
        var innerExample: Exampleer = Exampleer.Special,
        var innerExample2: Exampleer = Exampleer.Special
    ) : Eyes<Exampleer> {
        override val sight = vision(::innerExample, ::innerExample2)
    }
}

fun main() {
    val example = ExampleData()
    val sight = example.sight
    sight.views().forEach {
        println(it.get())
        it.unlock<Exampleer.Thing> {
            it.set(Exampleer.Thing.Two)
        }
    }
}
```