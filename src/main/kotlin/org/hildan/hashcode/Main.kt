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
    val n2PTeams: Int,
    val n3PTeams: Int,
    val n4PTeams: Int,
    val pizzas: List<Pizza>,
) {
    @OptIn(ExperimentalStdlibApi::class)
    fun solve(): List<String> {
        val nPizzas = pizzas.size
        val iter = pizzas.sortedByDescending { it.ingredients.size }.iterator()
        val n2POrders = n2PTeams.coerceAtMost(nPizzas / 2)
        val n3POrders = n3PTeams.coerceAtMost((nPizzas - n2POrders * 2) / 3)
        val n4POrders = n2PTeams.coerceAtMost((nPizzas - n2POrders * 2 - n3POrders * 3) / 4)
        val orders = buildList {
            repeat(n2POrders) {
                add("2 ${iter.next().id} ${iter.next().id}")
            }
            repeat(n3POrders) {
                add("3 ${iter.next().id} ${iter.next().id} ${iter.next().id}")
            }
            repeat(n4POrders) {
                add("4 ${iter.next().id} ${iter.next().id} ${iter.next().id} ${iter.next().id}")
            }
        }
        return listOf(orders.size.toString()) + orders
    }
}

data class Pizza(
    val id: Int,
    val ingredients: Set<String>,
)

fun HCReader.readProblem(): Problem {
    val nPizzas = readInt()
    val n2PTeams = readInt()
    val n3PTeams = readInt()
    val n4PTeams = readInt()
    val pizzas = List(nPizzas) { readPizza(it) }
    return Problem(n2PTeams, n3PTeams, n4PTeams, pizzas)
}

fun HCReader.readPizza(index: Int): Pizza {
    val nIngredients = readInt()
    val ingredients = List(nIngredients) { readString() }.toSet()
    return Pizza(index, ingredients)
}