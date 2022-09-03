package by.varyvoda.lemer.domain

import java.util.stream.IntStream
import kotlin.streams.toList

class LemerGenerator(val a: Int, val r0: Int, val m: Int) {

    var previous: Int = r0
        private set

    fun next(): Double {
        previous = (a * previous) % m
        return previous.toDouble() / m
    }

    fun batch(count: Int): List<Double> {
        reset()
        return IntStream.range(0, count).mapToDouble { next() }.toList().also { reset() }
    }

    fun reset() {
        previous = r0
    }

    fun duplicate(): LemerGenerator {
        return LemerGenerator(a, r0, m)
    }
}