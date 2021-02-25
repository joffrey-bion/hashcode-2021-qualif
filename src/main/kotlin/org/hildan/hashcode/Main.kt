package org.hildan.hashcode

import kotlinx.coroutines.runBlocking
import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.solveHCFilesInParallel

fun main(args: Array<String>) = runBlocking {
    solveHCFilesInParallel(*args) {
        readProblem().solve()
    }
}

data class Problem(
    val duration: Int,
    val nIntersections: Int,
    val nStreets: Int,
    val nCars: Int,
    val nPoints: Int,
    val streets: List<Street>,
    val cars: List<Car>,
) {
    fun solve(): List<String> {
        return emptyList()
    }
}

data class Street(
    val start: Int,
    val end: Int,
    val name: String,
    val length: Int,
)

data class Car(
    val nStreets: Int,
    val streets: List<String>,
)

fun HCReader.readProblem(): Problem {
    val duration = readInt()
    val nIntersections = readInt()
    val nStreets = readInt()
    val nCars = readInt()
    val nPoints = readInt()
    val streets = List(nStreets) { readStreet() }
    val cars = List(nCars) { readCar() }
    return Problem(duration, nIntersections, nStreets, nCars, nPoints, streets, cars)
}

fun HCReader.readStreet(): Street {
    val start = readInt()
    val end = readInt()
    val name = readString()
    val timeToCross = readInt()
    return Street(start, end, name, timeToCross)
}

fun HCReader.readCar(): Car {
    val nStreets = readInt()
    val streets = List(nStreets) { readString() }
    return Car(nStreets, streets)
}
