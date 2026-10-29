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

enum class GlassFitMode {
    WITH_CELL_MARGIN,
    INTERMEDIATE,
    EDGE_TO_EDGE
}

data class GlassGeometrySnapshot(
    val insideCell: Boolean,
    val sharedNeck: Boolean,
    val fitMode: GlassFitMode,
    val widthFraction: Float,
    val heightFraction: Float,
    val hasCellMargin: Boolean,
    val reachesCellEdge: Boolean
)

data class BottleSnapshot(
    val portions: List<Drink>,
    val freeSpaces: Int,
    val selected: Boolean,
    val completedDrinkName: String?,
    val glass: GlassGeometrySnapshot
)

data class DrinkSheetSnapshot(
    val drink: Drink,
    val title: String
)

data class VictoryBannerSnapshot(
    val title: String,
    val moves: Int
)

data class CounterSnapshot(
    val capacity: Int,
    val glass: GlassGeometrySnapshot,
    val bottles: List<BottleSnapshot>,
    val columnsPerRow: Int,
    val rowCount: Int,
    val rows: List<List<BottleSnapshot>>,
    val moves: Int,
    val movesLabel: String,
    val stamina: Int,
    val staminaCeiling: Int,
    val openedDrinkSheet: DrinkSheetSnapshot?,
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
    private var openedSheetDrink: Drink? = null

    fun tapBottle(index: Int) {
        tapBottleGlass(index)
    }

    fun tapBottleGlass(index: Int) {
        if (won) return
        if (openedSheetDrink != null) return
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

    fun tapBottleName(index: Int) {
        if (index !in bottles.indices) return
        val completed = completedDrinkOf(bottles[index]) ?: return
        openedSheetDrink = completed
    }

    fun closeDrinkSheet() {
        openedSheetDrink = null
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
            bottle.isEmpty() || completedDrinkOf(bottle) != null
        }
    }

    private fun completedDrinkOf(bottle: List<Drink>): Drink? {
        if (capacity <= 0 || bottle.size != capacity) return null
        val first = bottle[0]
        return if (bottle.all { it == first }) first else null
    }

    private fun glassGeometry(): GlassGeometrySnapshot {
        val (fitMode, widthFraction, heightFraction) = when (capacity) {
            3 -> Triple(GlassFitMode.WITH_CELL_MARGIN, 0.78f, 0.76f)
            4 -> Triple(GlassFitMode.INTERMEDIATE, 0.85f, 0.84f)
            5 -> Triple(GlassFitMode.INTERMEDIATE, 0.92f, 0.92f)
            else -> Triple(GlassFitMode.EDGE_TO_EDGE, 1.0f, 1.0f)
        }
        return GlassGeometrySnapshot(
            insideCell = true,
            sharedNeck = true,
            fitMode = fitMode,
            widthFraction = widthFraction,
            heightFraction = heightFraction,
            hasCellMargin = fitMode == GlassFitMode.WITH_CELL_MARGIN,
            reachesCellEdge = fitMode == GlassFitMode.EDGE_TO_EDGE
        )
    }

    fun snapshot(): CounterSnapshot {
        val glass = glassGeometry()
        val bottleSnapshots = bottles.mapIndexed { idx, portions ->
            val completedDrink = completedDrinkOf(portions)
            BottleSnapshot(
                portions = portions.toList(),
                freeSpaces = capacity - portions.size,
                selected = selectedIndex == idx,
                completedDrinkName = completedDrink?.title,
                glass = glass
            )
        }
        val columnsPerRow = when {
            bottleSnapshots.size <= 4 -> 4
            bottleSnapshots.size <= 6 -> 3
            else -> 5
        }
        val rows = if (bottleSnapshots.isEmpty()) emptyList() else bottleSnapshots.chunked(columnsPerRow)
        return CounterSnapshot(
            capacity = capacity,
            glass = glass,
            bottles = bottleSnapshots,
            columnsPerRow = columnsPerRow,
            rowCount = rows.size,
            rows = rows,
            moves = moves,
            movesLabel = "ходы",
            stamina = 120,
            staminaCeiling = 120,
            openedDrinkSheet = openedSheetDrink?.let { DrinkSheetSnapshot(drink = it, title = it.title) },
            victoryBanner = if (won) VictoryBannerSnapshot(title = "Победа", moves = moves) else null
        )
    }
}
