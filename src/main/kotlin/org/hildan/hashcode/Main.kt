package org.hildan.hashcode

import kotlinx.coroutines.runBlocking
import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.solveHCFilesInParallel
import kotlin.math.ceil

fun main(args: Array<String>) = runBlocking {
    solveHCFilesInParallel(*args) {
        readProblem().solve()
    }
}

@OptIn(ExperimentalStdlibApi::class)
data class Problem(
    val duration: Int,
    val nIntersections: Int,
    val nStreets: Int,
    val nCars: Int,
    val nPointsByCar: Int,
    val streets: List<Street>,
    val cars: List<Car>,
) {
    val streetPopularityByIntersection: Map<Int, MutableMap<String, Int>> = buildMap {
        val mainMap = this
        cars.forEach { c ->
            c.streets.forEach { s ->
                mainMap.computeIfAbsent(s.end) { HashMap() }.merge(s.name, 1, Int::plus)
            }
        }
    }

    fun solve(): List<String> {
        val intersections = streetPopularityByIntersection.mapValues { (_, popularityMap) ->
            val totalCars = popularityMap.values.sum()
            val timesByStreet = popularityMap.mapValues { (_, qty) ->
                ceil(15.0 * qty / totalCars).toInt().coerceAtMost(duration)
            }
            timesByStreet.filterValues { it > 0 }.toMap(LinkedHashMap())
        }
        var filteredIntersections = intersections.filterValues { it.isNotEmpty() }

        println("Initial solution built")
        var solution = Solution(filteredIntersections)
        repeat(5) {
            println("Simulation $it")
            val result = solution.simulate(this)
            filteredIntersections = filteredIntersections.mapValues { (id, popularityMap) ->
                val maxQueuesByStreet = result.intersectionMaxQueues[id]!!
                val totalMaxCars = maxQueuesByStreet.values.sum()
                val avgMaxCars = maxQueuesByStreet.values.average()

                popularityMap.mapValuesTo(LinkedHashMap()) { (street, time) ->
                    val maxQueue = maxQueuesByStreet[street] ?: 0
                    val diffWithAvg = maxQueue - avgMaxCars
                    val scale = (totalMaxCars + diffWithAvg) / totalMaxCars
                    ceil(time * scale).toInt().coerceAtLeast(1).coerceAtMost(duration)
                }
            }
            solution = Solution(filteredIntersections)
        }

        return solution.toLines()
    }
}

typealias StreetName = String
typealias CarID = Int
typealias IntersectionID = Int
typealias Time = Int

@OptIn(ExperimentalStdlibApi::class)
data class Solution(
    val schedules: Map<IntersectionID, LinkedHashMap<StreetName, Time>>,
) {
    fun toLines(): List<String> = buildList {
        add(schedules.size.toString())
        schedules.forEach { (id, popularityMap) ->
            add(id.toString())
            add(popularityMap.size.toString())
            popularityMap.forEach { (s, time) ->
                add("$s $time")
            }
        }
    }

    fun simulate(problem: Problem): SimulationResult {
        val carStates: MutableList<CarState> = problem.cars.mapTo(ArrayList()) { CarState(it) }
        val intersectionStates = (0 until problem.nIntersections).associateWith {
            IntersectionState(schedule = Schedule(schedules[it] ?: LinkedHashMap()))
        }
        problem.cars.forEach { car ->
            val initialStreet = car.streets.first()
            val initialIntersectionId = initialStreet.end
            val initialIntersectionState = intersectionStates[initialIntersectionId]!!
            initialIntersectionState.enqueueCar(initialStreet.name, car.id)
        }

        var score = 0
        for (t in 0 until problem.duration) {
            intersectionStates.forEach { (_, state) -> state.step() }
            val carsToRemove = mutableSetOf<CarID>()
            carStates.forEach { state ->
                state.step(intersectionStates)
                if (state.reachedEndOfPath()) {
                    score += problem.nPointsByCar
                    carsToRemove.add(state.car.id)
                }
            }
            carStates.removeIf { it.car.id in carsToRemove }
        }
        val intersectionMaxQueues = intersectionStates.mapValues { (_, state) -> state.maxQueueByStreet }
        return SimulationResult(intersectionMaxQueues, score)
    }
}

data class CarState(
    val car: Car,
    /** Index in the list of streets */
    var streetIndex: Int = 0,
    /** Position within the street */
    var posInStreet: Int = car.streets.first().length - 1,
) {
    val street: Street get() = car.streets[streetIndex]

    fun step(intersectionStates: Map<IntersectionID, IntersectionState>) {
        if (endOfStreet()) {
            val inter = intersectionStates[street.end]!!
            if (inter.didCarLeave(car.id)) {
                streetIndex++
            }
        } else {
            posInStreet++
        }
    }

    private fun endOfStreet(): Boolean = posInStreet == street.length

    fun reachedEndOfPath(): Boolean = endOfStreet() && streetIndex == car.streets.lastIndex
}

data class IntersectionState(
    val schedule: Schedule,
) {
    val maxQueueByStreet: MutableMap<StreetName, Int> = mutableMapOf()

    private val carQueuesByStreet: MutableMap<StreetName, MutableList<CarID>> = mutableMapOf()
    private var lastThatLeft: CarID? = null

    fun enqueueCar(street: StreetName, car: CarID) {
        val queue = carQueuesByStreet.computeIfAbsent(street) { ArrayList() }
        queue.add(car)
        maxQueueByStreet.merge(street, queue.size, ::maxOf)
    }

    fun didCarLeave(car: CarID): Boolean = car == lastThatLeft

    fun step() {
        val cars = carQueuesByStreet[schedule.greenStreet] ?: mutableListOf()
        lastThatLeft = cars.removeFirstOrNull()
        schedule.step()
    }
}

data class Schedule(
    private val schedule: LinkedHashMap<StreetName, Time>,
) {
    val greenStreet: StreetName?
        get() = if (greenStreetIndex in streets.indices) streets[greenStreetIndex] else null

    private val streets = schedule.keys.toList()
    private var greenStreetIndex: Int = 0
    private var greenFor: Time = 0

    fun step() {
        greenFor++
        if (greenStreet != null && greenFor >= schedule[greenStreet]!!) {
            greenFor = 0
            greenStreetIndex = (greenStreetIndex + 1) % streets.size
        }
    }
}

data class SimulationResult(
    val intersectionMaxQueues: Map<IntersectionID, Map<StreetName, Int>>,
    val score: Int,
)

data class Street(
    val start: Int,
    val end: Int,
    val name: String,
    val length: Int,
)

data class Car(
    val id: CarID,
    val streets: List<Street>,
)

fun HCReader.readProblem(): Problem {
    val duration = readInt()
    val nIntersections = readInt()
    val nStreets = readInt()
    val nCars = readInt()
    val nPoints = readInt()
    val streets = List(nStreets) { readStreet() }
    val streetsByName = streets.associateBy { it.name }
    val cars = List(nCars) { readCar(it, streetsByName) }
    return Problem(duration, nIntersections, nStreets, nCars, nPoints, streets, cars)
}

fun HCReader.readStreet(): Street {
    val start = readInt()
    val end = readInt()
    val name = readString()
    val timeToCross = readInt()
    return Street(start, end, name, timeToCross)
}

fun HCReader.readCar(id: Int, streetsByName: Map<String, Street>): Car {
    val nStreets = readInt()
    val streets = List(nStreets) {
        val streetName = readString()
        streetsByName[streetName]!!
    }
    return Car(id, streets)
}
