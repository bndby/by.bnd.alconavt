package by.bnd.alconavt

enum class Drink(val title: String) {
    GUINNESS_DRAUGHT("Guinness Draught"),
    PILSNER_URQUELL("Pilsner Urquell"),
    APEROL("Aperol"),
    GIFFARD_CREME_DE_VIOLETTE("Giffard Crème de Violette"),
    BAILEYS_ORIGINAL_IRISH_CREAM("Baileys Original Irish Cream"),
    GIFFARD_LICHI_LI("Giffard Lichi-Li"),
    BOLS_BLUE_CURACAO("Bols Blue Curaçao"),
    CAMPARI("Campari"),
    CHARTREUSE_VERTE("Chartreuse Verte")
}

data class BottleSnapshot(
    val portions: List<Drink>,
    val freeSpaces: Int,
    val selected: Boolean
)

data class VictoryBannerSnapshot(
    val title: String,
    val moves: Int
)

data class CounterSnapshot(
    val capacity: Int,
    val bottles: List<BottleSnapshot>,
    val moves: Int,
    val movesLabel: String,
    val stamina: Int,
    val staminaCeiling: Int,
    val victoryBanner: VictoryBannerSnapshot?
)

class LevelSession(
    private val capacity: Int,
    initialBottles: List<List<Drink>>
) {
    private val bottles: List<MutableList<Drink>> = initialBottles.map { it.toMutableList() }
    private var selectedIndex: Int? = null
    private var moves: Int = 0
    private var won: Boolean = isSolved()

    fun tapBottle(index: Int) {
        if (won) return
        if (index !in bottles.indices) return
        val currentSelection = selectedIndex
        if (currentSelection == null) {
            if (bottles[index].isNotEmpty()) {
                selectedIndex = index
            }
        } else if (currentSelection == index) {
            selectedIndex = null
        } else {
            pour(fromIndex = currentSelection, toIndex = index)
        }
    }

    private fun pour(fromIndex: Int, toIndex: Int) {
        val src = bottles[fromIndex]
        val dst = bottles[toIndex]
        if (src.isEmpty()) return
        val freeSpaces = capacity - dst.size
        if (freeSpaces <= 0) return

        val topDrink = src.last()
        if (dst.isNotEmpty() && dst.last() != topDrink) return

        var topRunLength = 0
        for (i in src.lastIndex downTo 0) {
            if (src[i] == topDrink) {
                topRunLength++
            } else {
                break
            }
        }

        val amountToMove = minOf(topRunLength, freeSpaces)
        val movedPortions = MutableList(amountToMove) { src.removeAt(src.lastIndex) }.asReversed()
        dst.addAll(movedPortions)
        moves++
        selectedIndex = null
        if (isSolved()) {
            won = true
        }
    }

    private fun isSolved(): Boolean {
        return bottles.all { bottle ->
            bottle.isEmpty() || (bottle.size == capacity && bottle.all { it == bottle[0] })
        }
    }

    fun snapshot(): CounterSnapshot {
        return CounterSnapshot(
            capacity = capacity,
            bottles = bottles.mapIndexed { idx, portions ->
                BottleSnapshot(
                    portions = portions.toList(),
                    freeSpaces = capacity - portions.size,
                    selected = selectedIndex == idx
                )
            },
            moves = moves,
            movesLabel = "ходы",
            stamina = 120,
            staminaCeiling = 120,
            victoryBanner = if (won) VictoryBannerSnapshot(title = "Победа", moves = moves) else null
        )
    }
}
