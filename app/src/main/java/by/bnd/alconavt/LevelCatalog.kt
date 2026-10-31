package by.bnd.alconavt

data class CatalogBar(
    val cityName: String,
    val streetName: String,
    val barName: String,
    val rank: Int,
    val levels: List<LevelLayout>
) {
    val levelCount: Int get() = levels.size
}

object LevelCatalog {
    private val DRINKS = Drink.entries

    private fun bottle(vararg indices: Int): List<Drink> = indices.map { DRINKS[it] }

    private fun moves(vararg pairs: Int): List<Pair<Int, Int>> =
        pairs.toList().chunked(2).map { (from, to) -> from to to }

    private fun level(
        levelNumber: Int,
        capacity: Int,
        bottles: List<List<Drink>>,
        moves: List<Pair<Int, Int>>
    ): LevelLayout = LevelLayout(
        levelNumber = levelNumber,
        capacity = capacity,
        initialBottles = bottles,
        parMoves = moves.size,
        solutionMoves = moves
    )

    private fun createUnderLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Under",
            rank = 1,
            levels = listOf(
                level(1, 3, listOf(bottle(0, 1, 0), bottle(1, 0, 1), emptyList(), emptyList()), moves(0, 2, 0, 3, 1, 3, 1, 2, 0, 2, 1, 3)),
                level(2, 3, listOf(bottle(2, 2, 7), bottle(7, 7, 2), emptyList(), emptyList()), moves(0, 2, 1, 0, 1, 2)),
                level(3, 3, listOf(bottle(6, 8, 6), bottle(8, 6, 8), emptyList(), emptyList()), moves(0, 2, 0, 3, 1, 3, 1, 2, 0, 2, 1, 3)),
                level(4, 3, listOf(bottle(3, 5, 3), bottle(5, 3, 5), emptyList(), emptyList()), moves(0, 2, 0, 3, 1, 3, 1, 2, 0, 2, 1, 3)),
                level(5, 3, listOf(bottle(0, 0, 1), bottle(1, 1, 4), bottle(4, 4, 0), emptyList(), emptyList()), moves(0, 3, 2, 0, 1, 2, 3, 1)),
                level(6, 3, listOf(bottle(2, 2, 7), bottle(7, 7, 8), bottle(8, 8, 2), emptyList(), emptyList()), moves(0, 3, 2, 0, 1, 2, 3, 1)),
                level(7, 3, listOf(bottle(6, 6, 3), bottle(3, 3, 5), bottle(5, 5, 6), emptyList(), emptyList()), moves(0, 3, 2, 0, 1, 2, 3, 1)),
                level(8, 3, listOf(bottle(4, 4, 0), bottle(0, 0, 8), bottle(8, 8, 4), emptyList(), emptyList()), moves(0, 3, 2, 0, 1, 2, 3, 1))
            )
    )

    private fun createKoyotLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Койот",
            rank = 1,
            levels = listOf(
                level(1, 3, listOf(bottle(7, 8, 7), bottle(7, 8, 8), emptyList(), emptyList()), moves(1, 2, 0, 1, 0, 2, 0, 1)),
                level(2, 3, listOf(bottle(6, 7, 6), bottle(6, 7, 7), emptyList(), emptyList()), moves(1, 2, 0, 1, 0, 2, 0, 1)),
                level(3, 3, listOf(bottle(3, 2, 2), bottle(3, 2, 3), emptyList(), emptyList()), moves(0, 2, 1, 0, 1, 2, 0, 1)),
                level(4, 3, listOf(bottle(4, 5, 5), bottle(4, 4, 5), emptyList(), emptyList()), moves(0, 2, 1, 2, 0, 1)),
                level(5, 3, listOf(bottle(1, 6, 1), bottle(1, 6, 8), bottle(6, 8, 8), emptyList(), emptyList()), moves(2, 3, 1, 3, 1, 2, 0, 1, 0, 2, 0, 1)),
                level(6, 3, listOf(bottle(0, 4, 6), bottle(4, 4, 0), bottle(0, 6, 6), emptyList(), emptyList()), moves(2, 3, 0, 3, 1, 2, 0, 1, 0, 2)),
                level(7, 3, listOf(bottle(2, 5, 7), bottle(5, 5, 7), bottle(2, 2, 7), emptyList(), emptyList()), moves(1, 3, 2, 3, 0, 3, 0, 1, 0, 2)),
                level(8, 3, listOf(bottle(4, 5, 6), bottle(5, 4, 6), bottle(6, 4, 5), emptyList(), emptyList()), moves(0, 3, 1, 3, 2, 0, 1, 2, 0, 1, 2, 0, 2, 3))
            )
    )

    private fun createZodiacLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Zodiac",
            rank = 2,
            levels = listOf(
                level(1, 3, listOf(bottle(2, 4, 2), bottle(5, 4, 5), bottle(2, 5, 4), emptyList(), emptyList()), moves(2, 3, 1, 2, 1, 3, 2, 1, 0, 2, 0, 3, 0, 2)),
                level(2, 3, listOf(bottle(7, 2, 7), bottle(3, 2, 7), bottle(3, 2, 3), emptyList(), emptyList()), moves(0, 3, 0, 4, 1, 0, 1, 4, 0, 3, 2, 1, 2, 4, 1, 2)),
                level(3, 3, listOf(bottle(4, 3, 1), bottle(3, 4, 4), bottle(1, 3, 1), emptyList(), emptyList()), moves(0, 3, 2, 3, 0, 2, 1, 0, 2, 1, 2, 3)),
                level(4, 3, listOf(bottle(3, 4, 4), bottle(7, 3, 7), bottle(4, 7, 3), emptyList(), emptyList()), moves(0, 3, 2, 0, 1, 2, 1, 0, 2, 1, 2, 3)),
                level(5, 3, listOf(bottle(7, 1, 0), bottle(8, 7, 1), bottle(8, 0, 7), bottle(1, 8, 0), emptyList(), emptyList()), moves(0, 4, 1, 0, 2, 1, 2, 4, 3, 4, 3, 2, 0, 3, 1, 0, 1, 2)),
                level(6, 3, listOf(bottle(0, 8, 3), bottle(0, 8, 3), bottle(1, 1, 3), bottle(8, 1, 0), emptyList(), emptyList()), moves(2, 4, 0, 4, 1, 4, 0, 1, 3, 0, 3, 2, 1, 3, 0, 1)),
                level(7, 3, listOf(bottle(5, 4, 5), bottle(3, 2, 5), bottle(2, 4, 3), bottle(4, 2, 3), emptyList(), emptyList()), moves(0, 4, 1, 4, 1, 5, 2, 1, 3, 1, 0, 2, 3, 5, 2, 3, 0, 4, 2, 5)),
                level(8, 3, listOf(bottle(4, 5, 8), bottle(4, 0, 4), bottle(0, 8, 8), bottle(5, 5, 0), emptyList(), emptyList()), moves(2, 4, 0, 4, 3, 2, 0, 3, 1, 0, 1, 2, 0, 1)),
                level(9, 4, listOf(bottle(6, 8, 8, 8), bottle(6, 6, 7, 2), bottle(8, 7, 0, 0), bottle(2, 0, 0, 7), bottle(2, 2, 7, 6), emptyList(), emptyList()), moves(2, 5, 2, 6, 0, 2, 3, 6, 3, 5, 1, 3, 1, 6, 4, 0, 4, 6, 0, 1, 3, 4)),
                level(10, 4, listOf(bottle(6, 0, 0, 2), bottle(0, 3, 2, 2), bottle(3, 8, 8, 8), bottle(3, 3, 2, 0), bottle(6, 6, 8, 6), emptyList(), emptyList()), moves(2, 5, 0, 6, 1, 6, 1, 2, 0, 1, 3, 1, 3, 6, 2, 3, 4, 0, 4, 5, 0, 4)),
                level(11, 4, listOf(bottle(2, 2, 8, 3), bottle(3, 3, 8, 7), bottle(3, 7, 7, 2), bottle(8, 1, 1, 1), bottle(8, 1, 7, 2), emptyList(), emptyList()), moves(3, 5, 1, 6, 1, 3, 0, 1, 0, 3, 2, 0, 4, 0, 2, 6, 4, 6, 4, 5, 1, 2, 3, 4)),
                level(12, 4, listOf(bottle(4, 4, 6, 1), bottle(1, 6, 6, 1), bottle(8, 0, 0, 8), bottle(4, 0, 1, 8), bottle(6, 0, 4, 8), emptyList(), emptyList()), moves(0, 5, 1, 5, 0, 1, 2, 6, 3, 6, 3, 5, 2, 3, 4, 2, 2, 6, 3, 2, 4, 0, 4, 2, 1, 4, 0, 3, 1, 5))
            )
    )

    private fun createRyadomLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Рядом",
            rank = 2,
            levels = listOf(
                level(1, 3, listOf(bottle(8, 1, 8), bottle(1, 3, 1), bottle(3, 3, 8), emptyList(), emptyList()), moves(2, 3, 0, 3, 1, 0, 1, 2, 0, 1, 0, 3)),
                level(2, 3, listOf(bottle(2, 8, 5), bottle(5, 8, 8), bottle(2, 2, 5), emptyList(), emptyList()), moves(1, 3, 2, 1, 0, 1, 0, 3, 0, 2)),
                level(3, 3, listOf(bottle(5, 3, 3), bottle(7, 7, 5), bottle(3, 7, 5), emptyList(), emptyList()), moves(0, 3, 1, 0, 2, 0, 2, 1, 2, 3)),
                level(4, 3, listOf(bottle(1, 3, 5), bottle(1, 3, 1), bottle(3, 5, 5), emptyList(), emptyList()), moves(2, 3, 0, 3, 0, 2, 1, 0, 1, 2, 0, 1)),
                level(5, 3, listOf(bottle(4, 7, 5), bottle(0, 5, 0), bottle(4, 4, 0), bottle(5, 7, 7), emptyList(), emptyList()), moves(2, 4, 3, 5, 0, 3, 0, 5, 0, 2, 1, 4, 1, 3, 1, 4)),
                level(6, 3, listOf(bottle(0, 1, 6), bottle(6, 1, 5), bottle(5, 6, 5), bottle(0, 1, 0), emptyList(), emptyList()), moves(1, 4, 1, 5, 0, 1, 0, 5, 2, 4, 2, 1, 2, 4, 3, 0, 3, 5, 0, 3)),
                level(7, 3, listOf(bottle(5, 5, 2), bottle(8, 8, 4), bottle(2, 8, 4), bottle(2, 4, 5), emptyList(), emptyList()), moves(1, 4, 2, 4, 2, 1, 0, 2, 3, 0, 3, 4, 2, 3)),
                level(8, 3, listOf(bottle(6, 5, 0), bottle(6, 8, 0), bottle(8, 6, 5), bottle(0, 5, 8), emptyList(), emptyList()), moves(0, 4, 0, 5, 1, 4, 2, 5, 2, 0, 1, 2, 3, 2, 3, 5, 0, 1, 3, 4)),
                level(9, 4, listOf(bottle(6, 6, 6, 0), bottle(5, 5, 0, 1), bottle(7, 6, 7, 7), bottle(0, 5, 1, 1), bottle(1, 5, 0, 7), emptyList(), emptyList()), moves(1, 5, 2, 6, 3, 5, 4, 6, 1, 4, 3, 1, 0, 3, 2, 0, 4, 3, 4, 1, 2, 6, 4, 5)),
                level(10, 4, listOf(bottle(2, 6, 2, 0), bottle(8, 0, 6, 4), bottle(6, 4, 2, 0), bottle(8, 8, 6, 0), bottle(4, 2, 4, 8), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 5, 2, 6, 1, 2, 1, 0, 1, 5, 3, 5, 3, 0, 4, 1, 1, 3, 4, 2, 4, 6, 2, 4, 0, 2, 0, 6)),
                level(11, 4, listOf(bottle(1, 8, 8, 4), bottle(7, 8, 8, 6), bottle(7, 1, 6, 1), bottle(6, 6, 7, 4), bottle(1, 7, 4, 4), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 0, 1, 2, 1, 6, 3, 5, 4, 5, 3, 1, 2, 3, 2, 0, 4, 1, 0, 4, 1, 2)),
                level(12, 4, listOf(bottle(5, 0, 8, 5), bottle(2, 2, 0, 1), bottle(8, 8, 1, 1), bottle(1, 5, 2, 8), bottle(0, 2, 0, 5), emptyList(), emptyList()), moves(2, 5, 0, 6, 0, 2, 3, 2, 1, 5, 1, 0, 3, 1, 3, 6, 3, 5, 4, 6, 4, 0, 4, 1, 0, 4, 0, 6))
            )
    )

    private fun createBelyNalyvLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Белый Налив",
            rank = 3,
            levels = listOf(
                level(1, 4, listOf(bottle(8, 0, 6, 6), bottle(0, 0, 4, 6), bottle(8, 6, 4, 0), bottle(4, 8, 4, 8), emptyList(), emptyList()), moves(0, 4, 0, 5, 1, 4, 2, 5, 3, 0, 3, 1, 3, 0, 1, 3, 2, 3, 2, 4, 0, 2, 1, 5)),
                level(2, 4, listOf(bottle(7, 5, 7, 6), bottle(8, 6, 6, 5), bottle(8, 5, 8, 7), bottle(7, 6, 8, 5), emptyList(), emptyList()), moves(0, 4, 1, 5, 1, 4, 2, 0, 2, 1, 2, 5, 3, 5, 3, 1, 3, 4, 1, 2, 0, 3, 0, 5, 0, 3)),
                level(3, 4, listOf(bottle(2, 5, 6, 2), bottle(5, 5, 6, 8), bottle(6, 8, 8, 5), bottle(6, 2, 8, 2), emptyList(), emptyList()), moves(0, 4, 1, 5, 1, 0, 2, 1, 2, 5, 0, 2, 0, 1, 3, 0, 3, 5, 3, 0, 0, 4, 2, 3)),
                level(4, 4, listOf(bottle(3, 0, 1, 3), bottle(5, 0, 0, 5), bottle(1, 5, 1, 3), bottle(1, 5, 3, 0), emptyList(), emptyList()), moves(0, 4, 2, 4, 0, 2, 0, 5, 3, 5, 3, 0, 0, 4, 1, 3, 1, 5, 3, 1, 2, 3, 2, 1, 2, 3)),
                level(5, 4, listOf(bottle(6, 7, 6, 6), bottle(1, 6, 4, 1), bottle(1, 4, 3, 1), bottle(3, 7, 3, 3), bottle(7, 4, 4, 7), emptyList(), emptyList()), moves(1, 5, 1, 6, 0, 1, 2, 5, 4, 0, 4, 6, 0, 4, 1, 0, 1, 5, 2, 1, 2, 6, 2, 5, 3, 1, 3, 4, 1, 3)),
                level(6, 4, listOf(bottle(3, 6, 7, 0), bottle(3, 0, 3, 0), bottle(6, 0, 7, 3), bottle(7, 4, 4, 4), bottle(4, 6, 6, 7), emptyList(), emptyList()), moves(3, 5, 0, 6, 0, 3, 1, 6, 2, 1, 2, 3, 4, 3, 0, 4, 2, 6, 4, 2, 4, 5, 1, 0, 1, 6, 0, 1)),
                level(7, 4, listOf(bottle(8, 5, 8, 0), bottle(8, 5, 2, 2), bottle(5, 2, 3, 5), bottle(8, 3, 0, 3), bottle(0, 2, 3, 0), emptyList(), emptyList()), moves(0, 5, 1, 6, 2, 1, 3, 2, 3, 5, 2, 3, 2, 6, 1, 2, 0, 1, 0, 2, 4, 5, 0, 1, 3, 0, 4, 0, 4, 6, 1, 3, 4, 5)),
                level(8, 4, listOf(bottle(0, 1, 5, 0), bottle(3, 5, 0, 2), bottle(3, 2, 2, 3), bottle(1, 1, 5, 3), bottle(5, 0, 2, 1), emptyList(), emptyList()), moves(0, 5, 1, 6, 1, 5, 0, 1, 4, 0, 4, 6, 4, 5, 1, 4, 2, 1, 2, 6, 3, 1, 3, 4, 0, 3, 0, 5, 1, 2)),
                level(9, 5, listOf(bottle(1, 8, 5, 6, 6), bottle(0, 0, 0, 1, 1), bottle(5, 6, 1, 6, 5), bottle(0, 1, 8, 8, 4), bottle(8, 5, 6, 5, 4), bottle(4, 4, 8, 0, 4), emptyList(), emptyList()), moves(1, 6, 0, 7, 2, 0, 2, 7, 6, 2, 3, 6, 4, 6, 4, 0, 4, 7, 0, 4, 0, 3, 2, 0, 2, 7, 4, 2, 3, 4, 3, 0, 5, 6, 5, 1, 5, 4, 1, 3, 5, 6)),
                level(10, 5, listOf(bottle(5, 1, 1, 8, 5), bottle(1, 6, 0, 1, 6), bottle(8, 2, 2, 8, 1), bottle(5, 0, 8, 6, 0), bottle(6, 2, 5, 8, 6), bottle(5, 0, 0, 2, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 0, 2, 7, 5, 2, 3, 5, 1, 3, 1, 0, 1, 5, 3, 1, 3, 7, 4, 1, 4, 7, 6, 4, 2, 6, 2, 7, 3, 2, 5, 2, 4, 3, 4, 6, 1, 4, 0, 1, 0, 3, 3, 5)),
                level(11, 5, listOf(bottle(4, 8, 7, 8, 6), bottle(7, 3, 7, 6, 8), bottle(6, 4, 2, 6, 7), bottle(3, 4, 2, 3, 8), bottle(3, 8, 4, 3, 2), bottle(2, 2, 4, 7, 6), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 1, 6, 0, 1, 0, 7, 2, 1, 2, 6, 3, 7, 4, 2, 3, 4, 2, 3, 2, 0, 5, 2, 2, 6, 1, 2, 4, 1, 4, 0, 4, 7, 1, 4, 5, 1, 5, 0, 3, 5, 3, 0, 1, 2, 3, 4)),
                level(12, 5, listOf(bottle(5, 6, 0, 2, 0), bottle(7, 1, 6, 2, 7), bottle(2, 1, 1, 7, 7), bottle(5, 0, 6, 6, 5), bottle(1, 0, 2, 5, 1), bottle(7, 5, 6, 2, 0), emptyList(), emptyList()), moves(0, 6, 0, 7, 0, 6, 5, 6, 5, 7, 5, 0, 3, 5, 3, 0, 3, 6, 5, 3, 1, 5, 1, 7, 2, 5, 4, 2, 4, 3, 4, 7, 4, 6, 2, 4, 2, 7, 0, 2, 1, 2, 1, 4, 0, 3, 1, 5)),
                level(13, 5, listOf(bottle(3, 6, 0, 1, 1), bottle(3, 4, 6, 8, 1), bottle(6, 1, 5, 8, 0), bottle(4, 6, 0, 1, 6), bottle(5, 0, 4, 4, 5), bottle(3, 8, 8, 0, 8), bottle(3, 5, 3, 4, 5), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 7, 2, 8, 1, 2, 0, 1, 3, 1, 3, 7, 8, 3, 2, 8, 4, 2, 5, 8, 6, 2, 6, 4, 6, 0, 2, 6, 2, 7, 1, 2, 4, 1, 4, 5, 6, 4, 0, 6, 3, 0, 3, 2, 1, 3, 5, 0, 5, 8, 1, 5, 5, 6)),
                level(14, 5, listOf(bottle(7, 6, 5, 6, 5), bottle(2, 8, 6, 0, 7), bottle(0, 8, 0, 2, 8), bottle(8, 2, 7, 2, 8), bottle(0, 5, 1, 7, 7), bottle(5, 1, 1, 1, 6), bottle(1, 0, 2, 5, 6), emptyList(), emptyList()), moves(0, 7, 0, 8, 0, 7, 0, 8, 1, 0, 4, 0, 5, 8, 4, 5, 4, 7, 1, 4, 1, 8, 6, 8, 2, 1, 3, 1, 2, 3, 2, 4, 1, 2, 3, 1, 3, 0, 3, 1, 2, 3, 6, 7, 6, 1, 6, 2, 5, 6, 2, 4, 5, 7)),
                level(15, 5, listOf(bottle(1, 3, 8, 3, 3), bottle(4, 3, 1, 1, 5), bottle(8, 3, 4, 6, 4), bottle(1, 5, 5, 7, 7), bottle(7, 6, 6, 8, 8), bottle(6, 7, 8, 4, 7), bottle(1, 6, 5, 5, 4), emptyList(), emptyList()), moves(0, 7, 0, 8, 4, 8, 7, 0, 4, 7, 3, 4, 1, 3, 5, 4, 2, 5, 2, 7, 5, 2, 5, 8, 5, 4, 5, 7, 0, 5, 1, 0, 1, 5, 2, 1, 2, 5, 6, 1, 2, 8, 3, 2, 6, 2, 6, 7, 0, 3, 3, 6)),
                level(16, 5, listOf(bottle(6, 7, 7, 5, 5), bottle(0, 2, 2, 5, 7), bottle(8, 0, 8, 3, 3), bottle(3, 5, 2, 8, 8), bottle(0, 7, 6, 3, 7), bottle(3, 5, 2, 0, 8), bottle(6, 0, 6, 6, 2), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 8, 1, 7, 4, 8, 6, 1, 0, 6, 3, 0, 3, 1, 3, 7, 2, 3, 2, 0, 4, 3, 5, 0, 2, 5, 0, 2, 1, 0, 5, 1, 5, 0, 5, 7, 3, 5, 4, 3, 4, 8, 6, 3, 6, 1, 1, 4, 3, 6))
            )
    )

    private fun createGorkyLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Горький",
            rank = 3,
            levels = listOf(
                level(1, 4, listOf(bottle(4, 0, 4, 5), bottle(4, 5, 0, 0), bottle(5, 2, 0, 2), bottle(5, 2, 2, 4), emptyList(), emptyList()), moves(1, 4, 0, 1, 2, 5, 2, 4, 2, 5, 1, 2, 0, 1, 0, 4, 3, 0, 3, 5, 0, 1, 2, 3)),
                level(2, 4, listOf(bottle(7, 4, 0, 4), bottle(4, 4, 0, 7), bottle(1, 1, 0, 0), bottle(1, 1, 7, 7), emptyList(), emptyList()), moves(2, 4, 3, 5, 2, 3, 1, 5, 1, 4, 0, 1, 0, 4, 0, 1, 0, 5)),
                level(3, 4, listOf(bottle(0, 2, 1, 0), bottle(4, 0, 1, 1), bottle(4, 4, 2, 2), bottle(2, 0, 1, 4), emptyList(), emptyList()), moves(1, 4, 0, 1, 0, 4, 2, 0, 3, 2, 3, 4, 3, 1, 0, 3, 1, 0, 1, 2)),
                level(4, 4, listOf(bottle(3, 1, 4, 8), bottle(4, 3, 4, 4), bottle(1, 1, 3, 3), bottle(8, 8, 8, 1), emptyList(), emptyList()), moves(1, 4, 2, 1, 3, 2, 0, 3, 0, 4, 0, 2, 1, 0, 1, 4)),
                level(5, 4, listOf(bottle(2, 2, 3, 6), bottle(6, 0, 3, 0), bottle(0, 1, 2, 1), bottle(2, 6, 3, 1), bottle(0, 6, 3, 1), emptyList(), emptyList()), moves(0, 5, 2, 6, 3, 6, 0, 3, 2, 0, 2, 6, 4, 6, 1, 2, 1, 4, 1, 2, 1, 5, 3, 1, 4, 1, 3, 4, 4, 5, 0, 3, 2, 4)),
                level(6, 4, listOf(bottle(3, 3, 6, 8), bottle(6, 3, 0, 8), bottle(6, 4, 0, 4), bottle(0, 0, 6, 3), bottle(8, 4, 8, 4), emptyList(), emptyList()), moves(0, 5, 1, 5, 2, 6, 1, 2, 3, 1, 0, 3, 1, 0, 3, 1, 2, 3, 2, 6, 1, 2, 4, 6, 4, 5, 4, 6, 4, 5)),
                level(7, 4, listOf(bottle(2, 1, 5, 2), bottle(0, 2, 5, 0), bottle(0, 5, 1, 1), bottle(7, 2, 7, 5), bottle(7, 0, 7, 1), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 0, 2, 6, 1, 2, 1, 6, 3, 6, 1, 5, 1, 2, 0, 1, 4, 1, 3, 4, 3, 0, 0, 5, 4, 3, 4, 2, 3, 4)),
                level(8, 4, listOf(bottle(1, 8, 8, 6), bottle(2, 5, 5, 2), bottle(1, 1, 6, 5), bottle(8, 8, 5, 1), bottle(2, 6, 2, 6), emptyList(), emptyList()), moves(0, 5, 2, 6, 2, 5, 3, 2, 3, 6, 0, 3, 0, 2, 4, 5, 1, 4, 1, 6, 4, 1, 4, 5, 1, 4)),
                level(9, 5, listOf(bottle(8, 5, 1, 2, 6), bottle(5, 5, 2, 5, 2), bottle(1, 3, 1, 8, 3), bottle(3, 8, 2, 1, 8), bottle(3, 6, 1, 2, 6), bottle(5, 8, 6, 3, 6), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 4, 6, 4, 7, 0, 4, 0, 1, 3, 0, 3, 4, 3, 7, 3, 0, 2, 3, 2, 0, 5, 6, 5, 3, 5, 6, 5, 0, 1, 5, 1, 7, 1, 5, 2, 1, 2, 3, 4, 1, 4, 6, 1, 2, 3, 4)),
                level(10, 5, listOf(bottle(2, 1, 5, 5, 7), bottle(1, 2, 4, 6, 6), bottle(1, 2, 4, 4, 6), bottle(4, 5, 7, 7, 4), bottle(5, 5, 6, 1, 7), bottle(2, 6, 2, 7, 1), emptyList(), emptyList()), moves(0, 6, 0, 7, 4, 6, 4, 0, 1, 4, 3, 1, 3, 6, 3, 7, 1, 3, 5, 0, 5, 6, 1, 5, 0, 1, 5, 0, 4, 5, 4, 7, 5, 4, 2, 4, 2, 3, 2, 0, 0, 5, 1, 2)),
                level(11, 5, listOf(bottle(7, 4, 5, 4, 5), bottle(3, 6, 0, 0, 0), bottle(3, 7, 3, 7, 5), bottle(3, 0, 6, 6, 4), bottle(6, 4, 3, 7, 5), bottle(0, 6, 7, 4, 5), emptyList(), emptyList()), moves(0, 6, 0, 7, 0, 6, 0, 7, 2, 6, 2, 0, 3, 7, 4, 6, 5, 6, 4, 0, 2, 4, 2, 0, 4, 2, 4, 5, 5, 7, 5, 0, 5, 3, 1, 5, 1, 4, 3, 4, 3, 5, 1, 2, 2, 3)),
                level(12, 5, listOf(bottle(7, 2, 7, 4, 3), bottle(4, 5, 0, 3, 5), bottle(2, 4, 0, 3, 0), bottle(4, 5, 7, 5, 2), bottle(0, 3, 4, 7, 3), bottle(7, 2, 2, 0, 5), emptyList(), emptyList()), moves(0, 6, 0, 7, 4, 6, 0, 4, 3, 0, 1, 3, 1, 6, 2, 1, 2, 6, 1, 2, 3, 1, 4, 3, 4, 7, 4, 6, 2, 4, 2, 7, 0, 2, 3, 0, 1, 3, 1, 7, 3, 1, 5, 1, 5, 4, 5, 2, 0, 5, 3, 7)),
                level(13, 5, listOf(bottle(6, 8, 6, 4, 8), bottle(6, 0, 7, 7, 2), bottle(3, 7, 6, 3, 4), bottle(2, 7, 4, 0, 4), bottle(2, 8, 4, 3, 8), bottle(3, 7, 2, 3, 0), bottle(0, 0, 6, 8, 2), emptyList(), emptyList()), moves(0, 7, 0, 8, 2, 8, 3, 8, 4, 7, 2, 4, 0, 2, 0, 7, 5, 3, 0, 2, 4, 0, 4, 8, 4, 7, 1, 4, 5, 0, 5, 4, 5, 1, 6, 4, 6, 7, 0, 5, 6, 0, 2, 0, 1, 2, 1, 6, 3, 6, 3, 8, 0, 1, 2, 0, 3, 0, 2, 5, 3, 4)),
                level(14, 5, listOf(bottle(1, 8, 6, 8, 1), bottle(2, 5, 5, 7, 7), bottle(1, 7, 6, 2, 0), bottle(0, 8, 8, 2, 5), bottle(0, 7, 5, 1, 5), bottle(6, 8, 1, 0, 2), bottle(0, 7, 6, 6, 2), emptyList(), emptyList()), moves(0, 7, 1, 8, 3, 1, 4, 1, 5, 3, 2, 5, 6, 2, 7, 4, 1, 7, 2, 1, 3, 1, 0, 3, 0, 2, 3, 0, 5, 3, 4, 5, 4, 7, 4, 8, 3, 4, 0, 3, 5, 0, 5, 3, 2, 5, 6, 5, 2, 6, 6, 8, 0, 2, 4, 6)),
                level(15, 5, listOf(bottle(4, 0, 2, 7, 6), bottle(7, 0, 5, 2, 5), bottle(2, 8, 7, 8, 5), bottle(0, 6, 4, 8, 4), bottle(4, 7, 6, 2, 6), bottle(8, 5, 6, 5, 8), bottle(4, 7, 0, 2, 0), emptyList(), emptyList()), moves(0, 7, 0, 8, 4, 7, 4, 0, 4, 7, 8, 4, 1, 8, 1, 0, 1, 8, 2, 8, 5, 2, 5, 8, 5, 7, 5, 8, 2, 5, 4, 2, 3, 4, 3, 5, 3, 4, 3, 7, 1, 3, 2, 1, 2, 5, 0, 2, 0, 3, 6, 3, 6, 2, 6, 3, 6, 1, 0, 4, 4, 6)),
                level(16, 5, listOf(bottle(7, 5, 5, 3, 3), bottle(6, 3, 0, 5, 6), bottle(8, 0, 0, 6, 8), bottle(8, 7, 0, 7, 8), bottle(5, 7, 4, 8, 0), bottle(4, 3, 7, 3, 4), bottle(6, 4, 5, 4, 6), emptyList(), emptyList()), moves(0, 7, 1, 8, 1, 0, 4, 1, 2, 4, 2, 8, 1, 2, 1, 7, 6, 1, 5, 6, 5, 7, 1, 8, 2, 1, 3, 2, 3, 5, 3, 1, 4, 2, 5, 3, 5, 7, 4, 5, 4, 3, 0, 4, 3, 0, 2, 3, 6, 5, 6, 4, 6, 5, 6, 8))
            )
    )

    private fun createNaDneLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "На дне",
            rank = 4,
            levels = listOf(
                level(1, 5, listOf(bottle(7, 4, 3, 4, 4), bottle(5, 6, 5, 7, 7), bottle(6, 3, 4, 3, 6), bottle(3, 5, 4, 6, 3), bottle(7, 5, 6, 7, 5), emptyList(), emptyList()), moves(0, 5, 0, 6, 0, 5, 1, 0, 3, 6, 2, 3, 2, 6, 2, 5, 2, 6, 3, 2, 3, 5, 3, 1, 3, 6, 4, 1, 4, 0, 4, 2, 1, 4, 1, 2, 4, 1, 0, 4)),
                level(2, 5, listOf(bottle(8, 7, 3, 8, 8), bottle(6, 1, 7, 3, 6), bottle(3, 1, 7, 7, 6), bottle(1, 8, 8, 6, 1), bottle(3, 1, 3, 7, 6), emptyList(), emptyList()), moves(0, 5, 1, 6, 0, 1, 2, 6, 0, 2, 4, 6, 0, 5, 2, 0, 3, 2, 3, 6, 3, 5, 2, 3, 1, 2, 1, 0, 4, 0, 1, 3, 1, 6, 4, 2, 4, 3, 2, 4)),
                level(3, 5, listOf(bottle(6, 6, 1, 5, 2), bottle(1, 2, 5, 7, 6), bottle(1, 7, 6, 5, 2), bottle(5, 1, 7, 2, 2), bottle(1, 5, 6, 7, 7), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 5, 2, 6, 1, 2, 3, 5, 1, 3, 1, 6, 1, 5, 0, 1, 2, 0, 2, 3, 1, 2, 3, 1, 4, 1, 4, 0, 3, 2, 4, 3, 2, 4, 3, 6)),
                level(4, 5, listOf(bottle(7, 3, 1, 3, 3), bottle(7, 6, 3, 1, 1), bottle(7, 3, 6, 7, 2), bottle(1, 2, 2, 6, 7), bottle(2, 6, 2, 1, 6), emptyList(), emptyList()), moves(0, 5, 0, 6, 0, 5, 1, 6, 1, 5, 3, 0, 1, 3, 0, 1, 3, 0, 2, 3, 2, 1, 2, 0, 2, 5, 1, 2, 4, 0, 4, 6, 4, 3, 4, 0, 3, 4, 3, 6)),
                level(5, 5, listOf(bottle(6, 4, 2, 5, 6), bottle(2, 2, 3, 0, 5), bottle(3, 6, 2, 4, 6), bottle(0, 4, 3, 5, 5), bottle(5, 4, 3, 3, 0), bottle(4, 0, 2, 0, 6), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 2, 6, 3, 7, 4, 1, 3, 4, 3, 2, 1, 3, 5, 6, 5, 3, 0, 5, 2, 0, 2, 5, 2, 6, 1, 2, 4, 2, 5, 1, 5, 3, 0, 4, 4, 5, 0, 6, 4, 7)),
                level(6, 5, listOf(bottle(8, 7, 8, 5, 2), bottle(8, 8, 2, 0, 5), bottle(0, 0, 2, 8, 3), bottle(3, 7, 2, 5, 0), bottle(5, 7, 3, 5, 3), bottle(7, 2, 7, 3, 0), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 3, 1, 3, 7, 6, 3, 1, 6, 1, 3, 0, 1, 5, 6, 4, 5, 4, 7, 5, 4, 0, 5, 0, 1, 3, 0, 3, 5, 2, 3, 2, 1, 4, 3, 2, 0, 2, 6, 5, 4, 5, 0, 4, 5, 4, 7)),
                level(7, 5, listOf(bottle(1, 3, 1, 2, 8), bottle(3, 3, 2, 8, 4), bottle(0, 1, 2, 8, 4), bottle(0, 0, 3, 2, 3), bottle(0, 1, 4, 4, 8), bottle(0, 1, 2, 4, 8), emptyList(), emptyList()), moves(0, 6, 0, 7, 4, 6, 1, 4, 1, 6, 1, 7, 3, 1, 3, 7, 3, 1, 5, 6, 2, 5, 2, 6, 2, 7, 2, 0, 2, 3, 4, 2, 5, 2, 5, 7, 4, 0, 0, 5, 0, 1, 5, 0, 3, 4, 4, 5)),
                level(8, 5, listOf(bottle(4, 1, 2, 3, 3), bottle(3, 3, 5, 1, 2), bottle(1, 4, 1, 2, 7), bottle(4, 5, 4, 5, 7), bottle(1, 3, 7, 7, 7), bottle(4, 2, 5, 5, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 1, 0, 5, 7, 1, 5, 1, 6, 2, 1, 2, 7, 0, 2, 3, 1, 4, 1, 4, 6, 2, 4, 2, 0, 2, 4, 3, 2, 3, 0, 3, 2, 5, 2, 5, 7, 0, 3, 3, 5)),
                level(9, 6, listOf(bottle(0, 7, 7, 2, 0, 8), bottle(0, 5, 0, 5, 2, 8), bottle(8, 8, 0, 5, 2, 4), bottle(5, 2, 7, 5, 8, 4), bottle(7, 4, 7, 4, 5, 0), bottle(7, 4, 2, 8, 4, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 6, 1, 0, 4, 7, 1, 4, 1, 7, 4, 1, 2, 4, 2, 0, 2, 1, 2, 7, 3, 4, 3, 2, 3, 1, 2, 6, 1, 2, 1, 7, 0, 1, 0, 3, 0, 7, 4, 0, 3, 4, 3, 1, 2, 3, 5, 1, 5, 0, 5, 6, 5, 1, 5, 0, 4, 5, 4, 0, 4, 5)),
                level(10, 6, listOf(bottle(0, 7, 7, 4, 1, 6), bottle(8, 4, 6, 4, 8, 4), bottle(6, 4, 0, 8, 6, 7), bottle(1, 8, 1, 0, 0, 7), bottle(6, 0, 7, 1, 8, 6), bottle(1, 4, 7, 1, 0, 8), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 0, 4, 6, 1, 4, 0, 1, 2, 0, 2, 6, 3, 0, 4, 2, 4, 7, 4, 0, 4, 3, 4, 6, 0, 4, 3, 0, 3, 7, 2, 3, 2, 0, 1, 2, 1, 6, 1, 2, 3, 1, 5, 1, 5, 0, 5, 3, 5, 4, 3, 7, 2, 3, 5, 3, 2, 6, 5, 7)),
                level(11, 6, listOf(bottle(4, 6, 3, 2, 0, 0), bottle(3, 8, 2, 8, 6, 2), bottle(2, 6, 4, 0, 2, 0), bottle(3, 8, 8, 3, 3, 4), bottle(8, 3, 6, 0, 6, 2), bottle(8, 4, 6, 4, 0, 4), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 2, 6, 2, 7, 2, 6, 3, 2, 0, 3, 0, 1, 2, 0, 1, 2, 4, 7, 4, 2, 4, 6, 4, 2, 3, 4, 3, 1, 4, 3, 1, 4, 1, 7, 1, 4, 1, 3, 2, 1, 2, 7, 5, 0, 5, 6, 5, 0, 5, 1, 5, 0, 4, 5)),
                level(12, 6, listOf(bottle(4, 7, 1, 5, 8, 4), bottle(5, 7, 7, 6, 5, 6), bottle(4, 8, 6, 4, 4, 5), bottle(1, 7, 8, 6, 4, 8), bottle(5, 8, 1, 7, 6, 1), bottle(7, 5, 1, 1, 8, 6), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 0, 2, 6, 1, 2, 1, 0, 1, 2, 3, 7, 3, 6, 3, 2, 3, 7, 1, 3, 0, 1, 4, 0, 5, 4, 5, 7, 0, 5, 3, 0, 5, 3, 5, 1, 0, 5, 0, 6, 2, 0, 4, 0, 4, 5, 4, 3, 2, 4, 4, 7, 1, 4, 2, 6)),
                level(13, 6, listOf(bottle(8, 4, 7, 7, 5, 5), bottle(4, 5, 4, 4, 1, 0), bottle(0, 1, 7, 7, 4, 3), bottle(8, 0, 1, 4, 8, 8), bottle(8, 0, 7, 3, 5, 3), bottle(1, 3, 8, 7, 3, 0), bottle(1, 1, 5, 5, 0, 3), emptyList(), emptyList()), moves(0, 7, 1, 8, 5, 8, 4, 5, 4, 7, 2, 4, 6, 4, 6, 8, 6, 7, 1, 6, 2, 1, 0, 2, 1, 0, 1, 7, 0, 1, 3, 0, 3, 1, 3, 6, 3, 8, 0, 3, 2, 0, 2, 6, 2, 8, 4, 2, 4, 0, 4, 8, 5, 2, 5, 0, 5, 3, 5, 2, 3, 4, 5, 6)),
                level(14, 6, listOf(bottle(7, 2, 8, 7, 2, 8), bottle(3, 0, 8, 7, 3, 5), bottle(4, 0, 0, 2, 3, 5), bottle(0, 3, 0, 2, 3, 3), bottle(7, 4, 4, 7, 4, 8), bottle(2, 0, 8, 5, 7, 5), bottle(4, 5, 2, 8, 5, 4), emptyList(), emptyList()), moves(0, 7, 0, 8, 4, 7, 6, 4, 5, 6, 0, 5, 0, 7, 0, 8, 5, 0, 1, 5, 2, 5, 1, 2, 1, 0, 7, 1, 2, 7, 2, 8, 3, 7, 3, 8, 2, 3, 4, 2, 4, 0, 4, 2, 0, 4, 1, 0, 1, 3, 1, 7, 5, 1, 5, 0, 3, 5, 3, 7, 5, 3, 6, 1, 6, 0, 6, 5, 6, 1, 2, 6, 5, 8)),
                level(15, 6, listOf(bottle(0, 6, 5, 3, 0, 0), bottle(4, 2, 6, 2, 7, 7), bottle(3, 4, 7, 0, 0, 7), bottle(6, 4, 3, 5, 4, 3), bottle(5, 3, 7, 5, 4, 6), bottle(7, 6, 5, 6, 2, 2), bottle(2, 3, 5, 0, 2, 4), emptyList(), emptyList()), moves(0, 7, 1, 8, 2, 8, 2, 7, 2, 8, 3, 0, 2, 3, 5, 1, 4, 5, 6, 4, 2, 0, 1, 2, 1, 5, 1, 2, 3, 1, 4, 1, 3, 4, 0, 3, 0, 4, 5, 0, 4, 5, 4, 8, 3, 4, 3, 1, 0, 3, 6, 2, 6, 0, 0, 7, 4, 0, 5, 4, 5, 3, 6, 4, 6, 0, 2, 6, 5, 8)),
                level(16, 6, listOf(bottle(2, 8, 6, 1, 7, 8), bottle(5, 5, 6, 1, 2, 7), bottle(2, 8, 4, 8, 5, 7), bottle(1, 5, 7, 7, 2, 5), bottle(5, 7, 2, 6, 4, 4), bottle(6, 4, 6, 1, 8, 8), bottle(1, 1, 6, 4, 2, 4), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 8, 2, 8, 3, 2, 1, 3, 0, 1, 5, 7, 1, 5, 1, 0, 2, 1, 2, 7, 4, 2, 0, 4, 0, 7, 6, 2, 0, 6, 2, 0, 2, 7, 3, 2, 3, 8, 3, 1, 5, 3, 6, 2, 6, 0, 5, 6, 5, 0, 6, 5, 4, 5, 4, 2, 4, 8, 1, 4, 3, 6)),
                level(17, 6, listOf(bottle(0, 4, 6, 2, 5, 4), bottle(7, 7, 8, 2, 0, 7), bottle(5, 0, 0, 8, 7, 4), bottle(1, 1, 6, 6, 8, 6), bottle(8, 4, 1, 2, 1, 8), bottle(0, 7, 8, 2, 4, 0), bottle(2, 6, 7, 2, 1, 6), bottle(5, 5, 5, 4, 5, 1), emptyList(), emptyList()), moves(0, 8, 0, 9, 2, 8, 1, 2, 5, 1, 5, 8, 0, 5, 3, 0, 4, 3, 6, 0, 4, 6, 4, 5, 6, 4, 7, 4, 7, 9, 7, 8, 7, 9, 0, 7, 0, 8, 1, 0, 1, 6, 3, 1, 3, 7, 4, 3, 4, 8, 1, 4, 2, 1, 2, 4, 2, 0, 2, 9, 5, 2, 5, 4, 5, 1, 0, 5, 6, 2, 6, 1, 6, 7, 2, 6)),
                level(18, 6, listOf(bottle(4, 6, 8, 2, 6, 5), bottle(1, 1, 6, 3, 6, 1), bottle(3, 5, 8, 6, 2, 4), bottle(2, 5, 7, 4, 8, 4), bottle(3, 2, 1, 7, 5, 8), bottle(4, 4, 8, 3, 7, 1), bottle(5, 7, 8, 6, 3, 5), bottle(2, 1, 3, 7, 2, 7), emptyList(), emptyList()), moves(0, 8, 1, 9, 0, 1, 5, 9, 6, 8, 7, 5, 0, 7, 4, 0, 4, 8, 5, 4, 5, 6, 5, 0, 2, 5, 3, 5, 3, 0, 3, 5, 4, 3, 9, 4, 0, 9, 1, 0, 6, 1, 6, 0, 6, 9, 3, 6, 3, 8, 2, 3, 2, 0, 2, 9, 7, 3, 8, 2, 0, 8, 0, 5, 6, 0, 2, 6, 7, 0, 1, 2, 1, 8, 4, 1, 4, 3, 7, 2, 7, 1, 2, 4, 3, 7)),
                level(19, 6, listOf(bottle(3, 8, 5, 6, 8, 1), bottle(8, 3, 0, 6, 6, 4), bottle(5, 3, 1, 3, 8, 2), bottle(1, 8, 1, 4, 0, 0), bottle(0, 5, 6, 4, 2, 2), bottle(1, 2, 3, 4, 3, 2), bottle(5, 8, 4, 1, 2, 5), bottle(0, 0, 5, 4, 6, 6), emptyList(), emptyList()), moves(0, 8, 2, 9, 0, 2, 4, 9, 1, 4, 0, 1, 5, 9, 6, 0, 6, 9, 6, 8, 4, 6, 1, 4, 3, 1, 3, 6, 3, 8, 2, 3, 2, 5, 8, 2, 4, 8, 7, 8, 4, 0, 1, 4, 5, 1, 5, 7, 1, 5, 3, 1, 2, 3, 5, 2, 5, 9, 3, 5, 6, 3, 7, 3, 6, 1, 7, 0, 4, 7, 0, 6, 0, 1, 2, 0, 2, 6)),
                level(20, 6, listOf(bottle(5, 4, 3, 8, 5, 4), bottle(2, 1, 3, 3, 1, 1), bottle(0, 0, 2, 2, 0, 7), bottle(5, 3, 0, 7, 7, 8), bottle(7, 7, 8, 8, 0, 2), bottle(8, 8, 2, 5, 4, 5), bottle(2, 3, 4, 4, 1, 4), bottle(1, 1, 0, 5, 3, 7), emptyList(), emptyList()), moves(0, 8, 0, 9, 3, 0, 2, 3, 5, 9, 5, 8, 6, 8, 9, 5, 1, 9, 6, 9, 6, 8, 6, 1, 4, 6, 2, 4, 2, 6, 4, 2, 0, 4, 0, 1, 0, 8, 5, 0, 5, 6, 4, 5, 3, 4, 7, 4, 3, 2, 3, 7, 0, 3, 1, 0, 7, 0, 7, 3, 7, 2, 1, 7, 1, 6, 7, 9))
            )
    )

    private fun createMatreshkaLevels(): CatalogBar = CatalogBar(
            cityName = "Минск",
            streetName = "Зыбицкая",
            barName = "Матрешка",
            rank = 4,
            levels = listOf(
                level(1, 5, listOf(bottle(1, 2, 1, 1, 6), bottle(8, 6, 6, 5, 2), bottle(6, 6, 2, 1, 8), bottle(2, 5, 8, 2, 8), bottle(5, 8, 5, 1, 5), emptyList(), emptyList()), moves(0, 5, 0, 6, 1, 0, 4, 1, 4, 6, 1, 4, 1, 5, 2, 1, 2, 6, 2, 0, 2, 5, 3, 1, 3, 0, 3, 1, 4, 3, 4, 1, 3, 4, 0, 3, 0, 6)),
                level(2, 5, listOf(bottle(6, 4, 7, 7, 0), bottle(4, 6, 6, 5, 4), bottle(7, 5, 0, 6, 0), bottle(6, 0, 4, 5, 4), bottle(0, 5, 5, 7, 7), emptyList(), emptyList()), moves(0, 5, 0, 6, 1, 0, 2, 5, 3, 0, 1, 3, 2, 1, 2, 5, 3, 2, 3, 0, 3, 5, 1, 3, 0, 1, 0, 3, 2, 0, 4, 2, 4, 0, 2, 6, 4, 5)),
                level(3, 5, listOf(bottle(7, 2, 8, 7, 2), bottle(8, 7, 1, 3, 3), bottle(1, 1, 1, 3, 7), bottle(8, 2, 1, 2, 7), bottle(3, 8, 8, 2, 3), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 6, 3, 6, 3, 5, 4, 2, 4, 5, 4, 0, 2, 4, 1, 4, 1, 2, 3, 2, 1, 6, 3, 5, 0, 1, 0, 5, 0, 6, 1, 3)),
                level(4, 5, listOf(bottle(7, 7, 0, 3, 7), bottle(0, 8, 1, 8, 3), bottle(1, 3, 1, 3, 0), bottle(7, 0, 8, 8, 1), bottle(7, 1, 0, 3, 8), emptyList(), emptyList()), moves(0, 5, 0, 6, 1, 6, 2, 0, 2, 6, 3, 2, 1, 3, 1, 2, 3, 1, 0, 3, 0, 5, 1, 0, 4, 0, 3, 1, 4, 6, 4, 1, 2, 4, 2, 6, 4, 2, 3, 4, 4, 5)),
                level(5, 5, listOf(bottle(4, 1, 8, 1, 8), bottle(1, 0, 2, 6, 2), bottle(6, 0, 4, 0, 2), bottle(1, 4, 0, 2, 4), bottle(6, 0, 4, 6, 2), bottle(6, 8, 8, 1, 8), emptyList(), emptyList()), moves(0, 6, 0, 7, 0, 6, 0, 7, 3, 0, 1, 3, 5, 6, 5, 7, 5, 6, 1, 5, 2, 1, 4, 1, 4, 5, 4, 0, 4, 2, 4, 5, 1, 4, 3, 4, 1, 3, 1, 7, 2, 1, 2, 0, 2, 1, 3, 1, 3, 0, 2, 5, 3, 7)),
                level(6, 5, listOf(bottle(7, 6, 2, 8, 2), bottle(2, 1, 2, 6, 8), bottle(1, 4, 1, 7, 4), bottle(2, 6, 8, 7, 7), bottle(6, 8, 4, 6, 8), bottle(4, 7, 4, 1, 1), emptyList(), emptyList()), moves(0, 6, 0, 7, 0, 6, 1, 7, 0, 1, 3, 0, 3, 7, 1, 3, 1, 6, 4, 7, 4, 3, 2, 4, 2, 0, 2, 1, 4, 2, 4, 7, 3, 4, 5, 1, 5, 2, 5, 0, 2, 5, 1, 2, 1, 3, 3, 6)),
                level(7, 5, listOf(bottle(7, 5, 4, 4, 8), bottle(2, 4, 5, 1, 4), bottle(1, 8, 4, 1, 1), bottle(5, 7, 2, 2, 8), bottle(8, 7, 7, 2, 2), bottle(1, 5, 7, 5, 8), emptyList(), emptyList()), moves(0, 6, 1, 0, 1, 7, 2, 7, 3, 6, 5, 6, 1, 5, 1, 2, 3, 1, 4, 1, 3, 4, 5, 3, 5, 4, 5, 3, 5, 7, 0, 5, 0, 3, 4, 0, 2, 5, 2, 4, 2, 7, 4, 6)),
                level(8, 5, listOf(bottle(6, 3, 5, 6, 6), bottle(0, 0, 5, 2, 3), bottle(5, 2, 8, 0, 3), bottle(8, 6, 5, 3, 8), bottle(3, 6, 0, 0, 8), bottle(5, 2, 8, 2, 2), emptyList(), emptyList()), moves(0, 6, 3, 7, 2, 3, 4, 7, 2, 4, 2, 7, 5, 2, 5, 7, 2, 5, 0, 2, 1, 0, 3, 0, 3, 2, 3, 6, 3, 7, 5, 3, 1, 3, 1, 2, 4, 1, 4, 6, 0, 4, 0, 6, 2, 5)),
                level(9, 6, listOf(bottle(7, 0, 8, 8, 5, 7), bottle(8, 5, 6, 6, 3, 8), bottle(0, 7, 7, 0, 0, 8), bottle(6, 3, 5, 8, 0, 6), bottle(5, 3, 0, 5, 6, 5), bottle(6, 3, 3, 3, 7, 7), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 0, 2, 0, 4, 7, 3, 4, 3, 2, 5, 6, 1, 5, 4, 1, 4, 7, 2, 4, 2, 6, 4, 2, 5, 4, 1, 5, 1, 7, 0, 1, 0, 2, 3, 1, 0, 6, 4, 0, 3, 4, 3, 0, 3, 5, 4, 7)),
                level(10, 6, listOf(bottle(1, 4, 7, 2, 4, 4), bottle(7, 1, 8, 2, 3, 1), bottle(7, 1, 7, 3, 8, 7), bottle(8, 7, 2, 3, 2, 8), bottle(2, 3, 4, 4, 1, 8), bottle(8, 4, 2, 3, 1, 3), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 0, 3, 2, 3, 7, 5, 3, 1, 5, 1, 3, 7, 1, 3, 7, 1, 3, 2, 1, 2, 7, 0, 2, 0, 6, 4, 1, 4, 0, 4, 6, 4, 7, 3, 4, 3, 2, 1, 3, 1, 0, 2, 1, 2, 0, 5, 0, 5, 7, 5, 4, 5, 6, 1, 2, 3, 5)),
                level(11, 6, listOf(bottle(1, 5, 0, 1, 5, 0), bottle(1, 4, 6, 6, 1, 7), bottle(0, 5, 1, 4, 4, 7), bottle(4, 6, 4, 0, 0, 5), bottle(7, 4, 7, 1, 0, 5), bottle(7, 6, 6, 6, 7, 5), emptyList(), emptyList()), moves(0, 6, 0, 7, 3, 7, 3, 6, 4, 7, 4, 6, 0, 4, 0, 6, 0, 7, 4, 0, 1, 4, 1, 0, 2, 4, 2, 3, 2, 0, 2, 7, 5, 7, 2, 6, 1, 2, 1, 3, 0, 1, 5, 4, 5, 2, 4, 5, 3, 4, 3, 2, 4, 3, 4, 5)),
                level(12, 6, listOf(bottle(6, 4, 4, 1, 1, 6), bottle(2, 5, 2, 5, 1, 0), bottle(1, 4, 2, 4, 0, 5), bottle(4, 5, 6, 2, 5, 2), bottle(1, 0, 6, 0, 1, 2), bottle(6, 0, 5, 4, 0, 6), emptyList(), emptyList()), moves(0, 6, 1, 7, 1, 0, 2, 1, 2, 7, 5, 6, 5, 7, 2, 5, 3, 2, 3, 1, 2, 3, 2, 5, 0, 2, 5, 0, 1, 5, 3, 1, 3, 6, 5, 3, 5, 7, 5, 6, 3, 5, 0, 3, 0, 6, 1, 0, 1, 5, 4, 0, 0, 1, 4, 2, 4, 7, 4, 6, 4, 7, 2, 4)),
                level(13, 6, listOf(bottle(1, 3, 3, 7, 5, 8), bottle(4, 1, 7, 1, 4, 8), bottle(8, 2, 4, 2, 4, 2), bottle(3, 7, 4, 8, 5, 2), bottle(1, 1, 7, 4, 5, 8), bottle(3, 1, 8, 5, 7, 2), bottle(3, 2, 5, 5, 3, 7), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 7, 4, 7, 4, 8, 4, 1, 0, 4, 6, 4, 6, 0, 6, 8, 2, 6, 3, 6, 3, 8, 3, 7, 1, 3, 2, 3, 2, 6, 5, 6, 5, 4, 5, 8, 5, 7, 5, 1, 0, 5, 0, 1, 4, 0, 1, 4, 1, 0, 1, 4, 2, 1, 3, 1, 3, 0, 3, 5, 2, 3, 6, 3, 2, 7, 5, 6)),
                level(14, 6, listOf(bottle(4, 6, 7, 2, 2, 8), bottle(4, 7, 0, 7, 7, 8), bottle(0, 8, 4, 6, 6, 4), bottle(8, 8, 1, 2, 6, 2), bottle(4, 2, 7, 1, 6, 1), bottle(1, 6, 7, 0, 2, 0), bottle(8, 0, 1, 0, 4, 1), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 7, 0, 1, 3, 8, 3, 0, 3, 8, 4, 3, 4, 0, 3, 4, 3, 7, 0, 3, 2, 0, 2, 3, 2, 0, 2, 7, 5, 2, 5, 8, 5, 2, 1, 5, 1, 2, 5, 1, 5, 3, 4, 5, 6, 5, 6, 0, 6, 2, 6, 5, 6, 2, 6, 7, 1, 6, 4, 6, 4, 8, 0, 1, 1, 4)),
                level(15, 6, listOf(bottle(3, 0, 3, 2, 2, 6), bottle(0, 8, 5, 6, 1, 3), bottle(1, 8, 3, 5, 2, 1), bottle(5, 1, 6, 6, 2, 0), bottle(2, 6, 8, 0, 1, 6), bottle(8, 8, 0, 3, 1, 5), bottle(5, 5, 2, 0, 3, 8), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 0, 2, 1, 2, 8, 4, 7, 5, 2, 4, 5, 3, 4, 3, 8, 3, 7, 1, 3, 1, 7, 1, 2, 5, 3, 0, 5, 0, 4, 5, 0, 4, 5, 1, 4, 5, 1, 4, 5, 4, 7, 6, 5, 6, 0, 6, 1, 6, 4, 4, 8, 2, 6, 2, 0, 2, 5, 3, 2, 3, 6)),
                level(16, 6, listOf(bottle(3, 2, 0, 0, 5, 2), bottle(5, 8, 2, 0, 2, 8), bottle(1, 6, 8, 0, 1, 3), bottle(5, 0, 8, 1, 3, 1), bottle(6, 1, 8, 5, 6, 8), bottle(0, 3, 6, 2, 6, 6), bottle(3, 1, 3, 5, 5, 2), emptyList(), emptyList()), moves(0, 7, 0, 8, 6, 7, 6, 8, 2, 6, 3, 2, 3, 6, 2, 3, 0, 2, 0, 7, 0, 6, 2, 0, 1, 2, 1, 7, 1, 0, 1, 7, 1, 2, 4, 2, 1, 8, 2, 1, 4, 2, 4, 8, 4, 1, 3, 4, 3, 1, 3, 0, 3, 8, 5, 2, 5, 7, 5, 2, 6, 5, 4, 6, 2, 4, 6, 2, 5, 6, 0, 5)),
                level(17, 6, listOf(bottle(6, 0, 2, 2, 0, 1), bottle(8, 7, 1, 3, 8, 0), bottle(3, 6, 1, 1, 3, 1), bottle(1, 6, 7, 2, 6, 0), bottle(4, 7, 7, 8, 4, 4), bottle(2, 4, 3, 3, 7, 2), bottle(4, 3, 0, 7, 6, 0), bottle(6, 8, 2, 8, 8, 4), emptyList(), emptyList()), moves(0, 8, 0, 9, 1, 9, 2, 8, 3, 9, 5, 0, 6, 9, 3, 6, 3, 0, 3, 5, 6, 3, 5, 6, 2, 5, 2, 8, 2, 3, 5, 2, 4, 5, 1, 4, 1, 2, 1, 8, 6, 1, 6, 9, 6, 2, 5, 6, 0, 5, 0, 9, 3, 0, 3, 8, 1, 3, 4, 1, 4, 3, 7, 4, 4, 6, 7, 1, 7, 5, 7, 1, 0, 7)),
                level(18, 6, listOf(bottle(2, 0, 7, 6, 7, 3), bottle(5, 8, 0, 6, 3, 5), bottle(2, 5, 8, 0, 1, 6), bottle(5, 6, 6, 5, 8, 1), bottle(2, 3, 3, 7, 0, 3), bottle(2, 7, 1, 5, 0, 8), bottle(1, 2, 8, 3, 1, 7), bottle(8, 1, 0, 7, 6, 2), emptyList(), emptyList()), moves(0, 8, 0, 9, 2, 0, 3, 2, 4, 8, 5, 3, 4, 5, 4, 9, 6, 9, 8, 4, 0, 8, 0, 9, 5, 0, 1, 5, 1, 4, 1, 8, 1, 0, 3, 1, 3, 5, 3, 8, 5, 3, 2, 5, 2, 0, 2, 1, 6, 5, 3, 2, 0, 3, 7, 0, 7, 8, 7, 9, 7, 3, 5, 7, 5, 9, 0, 5, 4, 0, 6, 0, 6, 1, 6, 4, 7, 6, 1, 7, 2, 1, 2, 4, 4, 5)),
                level(19, 6, listOf(bottle(5, 0, 1, 8, 2, 8), bottle(6, 7, 7, 1, 2, 5), bottle(7, 7, 6, 8, 1, 7), bottle(7, 4, 4, 8, 5, 6), bottle(0, 0, 4, 4, 5, 2), bottle(5, 0, 2, 6, 6, 0), bottle(2, 1, 2, 4, 4, 8), bottle(1, 1, 6, 0, 5, 8), emptyList(), emptyList()), moves(0, 8, 0, 9, 0, 8, 4, 9, 1, 4, 1, 9, 0, 1, 5, 0, 3, 5, 6, 8, 7, 8, 3, 7, 3, 8, 6, 3, 6, 9, 6, 1, 6, 9, 5, 6, 5, 9, 0, 5, 4, 0, 7, 0, 7, 5, 7, 6, 1, 7, 2, 1, 2, 7, 2, 8, 2, 6, 1, 2, 1, 6, 3, 1, 4, 1, 5, 4, 0, 5, 2, 3)),
                level(20, 6, listOf(bottle(0, 4, 7, 2, 2, 4), bottle(7, 0, 7, 8, 6, 1), bottle(4, 5, 2, 1, 5, 7), bottle(2, 8, 5, 6, 0, 8), bottle(1, 5, 5, 8, 6, 6), bottle(0, 7, 0, 5, 4, 1), bottle(6, 4, 8, 4, 1, 1), bottle(7, 8, 2, 6, 0, 2), emptyList(), emptyList()), moves(0, 8, 1, 9, 5, 9, 5, 8, 6, 9, 6, 8, 3, 6, 7, 0, 3, 7, 1, 3, 1, 6, 2, 1, 2, 5, 2, 9, 0, 2, 0, 1, 0, 8, 7, 0, 3, 7, 3, 5, 3, 6, 2, 3, 5, 2, 5, 0, 5, 1, 0, 5, 2, 0, 2, 8, 6, 2, 6, 8, 4, 6, 7, 6, 7, 3, 7, 2, 4, 2, 4, 0, 4, 9, 1, 7, 1, 5, 1, 7))
            )
    )

    private fun createZolotoyLevLevels(): CatalogBar = CatalogBar(
            cityName = "Витебск",
            streetName = "Суворова",
            barName = "Золотой лев",
            rank = 1,
            levels = listOf(
                level(1, 3, listOf(bottle(2, 2, 6), bottle(6, 2, 6), emptyList(), emptyList()), moves(0, 2, 1, 2, 1, 0, 1, 2)),
                level(2, 3, listOf(bottle(1, 5, 5), bottle(5, 1, 1), emptyList(), emptyList()), moves(0, 2, 1, 0, 1, 2)),
                level(3, 3, listOf(bottle(0, 0, 7), bottle(7, 7, 0), emptyList(), emptyList()), moves(0, 2, 1, 0, 1, 2)),
                level(4, 3, listOf(bottle(4, 4, 8), bottle(8, 8, 4), emptyList(), emptyList()), moves(0, 2, 1, 0, 1, 2)),
                level(5, 3, listOf(bottle(5, 3, 7), bottle(5, 5, 7), bottle(3, 7, 3), emptyList(), emptyList()), moves(1, 3, 0, 3, 2, 0, 2, 3, 0, 2, 0, 1)),
                level(6, 3, listOf(bottle(0, 7, 7), bottle(0, 1, 7), bottle(1, 0, 1), emptyList(), emptyList()), moves(0, 3, 1, 3, 2, 1, 2, 0, 1, 2, 0, 1)),
                level(7, 3, listOf(bottle(4, 5, 3), bottle(4, 3, 5), bottle(4, 3, 5), emptyList(), emptyList()), moves(0, 3, 0, 4, 1, 4, 2, 4, 1, 2, 2, 3, 0, 1, 1, 2)),
                level(8, 3, listOf(bottle(5, 7, 7), bottle(5, 6, 6), bottle(7, 5, 6), emptyList(), emptyList()), moves(1, 3, 2, 3, 2, 1, 0, 2, 0, 1))
            )
    )

    private fun createMarvinLevels(): CatalogBar = CatalogBar(
            cityName = "Витебск",
            streetName = "Суворова",
            barName = "Марвин",
            rank = 2,
            levels = listOf(
                level(1, 3, listOf(bottle(3, 6, 3), bottle(7, 3, 7), bottle(6, 6, 7), emptyList(), emptyList()), moves(2, 3, 1, 3, 0, 1, 0, 2, 1, 0, 1, 3)),
                level(2, 3, listOf(bottle(2, 2, 3), bottle(3, 7, 7), bottle(2, 7, 3), emptyList(), emptyList()), moves(1, 3, 0, 1, 2, 1, 2, 3, 0, 2)),
                level(3, 3, listOf(bottle(4, 4, 2), bottle(2, 1, 4), bottle(1, 1, 2), emptyList(), emptyList()), moves(0, 3, 1, 0, 2, 3, 1, 2, 1, 3)),
                level(4, 3, listOf(bottle(1, 0, 1), bottle(7, 0, 7), bottle(1, 7, 0), emptyList(), emptyList()), moves(2, 3, 1, 2, 1, 3, 2, 1, 0, 2, 0, 3, 0, 2)),
                level(5, 3, listOf(bottle(6, 1, 6), bottle(7, 1, 5), bottle(6, 5, 7), bottle(7, 1, 5), emptyList(), emptyList()), moves(1, 4, 1, 5, 2, 1, 2, 4, 3, 4, 3, 5, 1, 3, 0, 2, 0, 5, 0, 2)),
                level(6, 3, listOf(bottle(1, 1, 2), bottle(3, 2, 1), bottle(3, 7, 3), bottle(2, 7, 7), emptyList(), emptyList()), moves(3, 4, 0, 3, 1, 0, 1, 3, 2, 1, 2, 4, 1, 2)),
                level(7, 3, listOf(bottle(6, 5, 3), bottle(1, 3, 3), bottle(5, 1, 6), bottle(6, 5, 1), emptyList(), emptyList()), moves(1, 4, 0, 4, 3, 1, 0, 3, 2, 0, 2, 1, 3, 2, 0, 3)),
                level(8, 3, listOf(bottle(1, 7, 1), bottle(7, 6, 7), bottle(6, 0, 1), bottle(0, 0, 6), emptyList(), emptyList()), moves(0, 4, 1, 0, 3, 1, 2, 4, 2, 3, 1, 2, 0, 1, 0, 4)),
                level(9, 4, listOf(bottle(5, 4, 0, 5), bottle(7, 4, 7, 5), bottle(0, 8, 8, 4), bottle(8, 5, 8, 4), bottle(7, 0, 7, 0), emptyList(), emptyList()), moves(0, 5, 0, 6, 1, 5, 2, 0, 3, 0, 3, 2, 3, 5, 2, 3, 4, 2, 4, 1, 4, 2, 2, 6, 0, 2, 0, 5, 1, 4, 1, 2, 1, 4)),
                level(10, 4, listOf(bottle(7, 8, 7, 3), bottle(3, 5, 3, 3), bottle(6, 8, 8, 7), bottle(5, 7, 5, 6), bottle(6, 8, 6, 5), emptyList(), emptyList()), moves(0, 5, 1, 5, 2, 0, 2, 6, 3, 2, 3, 1, 0, 3, 0, 6, 3, 0, 1, 3, 4, 3, 1, 5, 4, 2, 4, 6, 2, 4)),
                level(11, 4, listOf(bottle(2, 2, 0, 0), bottle(8, 2, 5, 3), bottle(5, 5, 5, 2), bottle(0, 3, 3, 8), bottle(8, 8, 3, 0), emptyList(), emptyList()), moves(0, 5, 2, 0, 1, 6, 1, 2, 1, 0, 3, 1, 3, 6, 4, 3, 4, 6, 1, 4, 3, 5)),
                level(12, 4, listOf(bottle(3, 7, 5, 7), bottle(4, 3, 3, 5), bottle(8, 5, 3, 7), bottle(8, 8, 5, 4), bottle(4, 4, 7, 8), emptyList(), emptyList()), moves(0, 5, 0, 6, 0, 5, 1, 6, 1, 0, 2, 5, 2, 0, 2, 6, 3, 1, 3, 6, 4, 2, 4, 5, 1, 4, 2, 3))
            )
    )

    private fun createDamaskLevels(): CatalogBar = CatalogBar(
            cityName = "Витебск",
            streetName = "Суворова",
            barName = "«Damask» Kafe-Bar",
            rank = 3,
            levels = listOf(
                level(1, 4, listOf(bottle(3, 6, 5, 5), bottle(0, 0, 3, 6), bottle(0, 6, 3, 3), bottle(5, 0, 5, 6), emptyList(), emptyList()), moves(0, 4, 0, 5, 1, 5, 1, 0, 2, 0, 2, 5, 3, 5, 3, 4, 3, 1, 1, 2, 3, 4)),
                level(2, 4, listOf(bottle(2, 2, 1, 1), bottle(2, 1, 0, 3), bottle(3, 2, 3, 3), bottle(1, 0, 0, 0), emptyList(), emptyList()), moves(3, 4, 0, 3, 1, 5, 1, 4, 1, 3, 2, 5, 2, 0, 0, 1, 2, 5)),
                level(3, 4, listOf(bottle(6, 3, 7, 8), bottle(7, 3, 8, 6), bottle(6, 8, 8, 6), bottle(7, 3, 3, 7), emptyList(), emptyList()), moves(0, 4, 3, 0, 3, 5, 0, 3, 0, 5, 1, 0, 1, 4, 1, 5, 1, 3, 2, 0, 2, 4, 0, 2)),
                level(4, 4, listOf(bottle(2, 5, 7, 7), bottle(5, 5, 2, 4), bottle(4, 2, 4, 4), bottle(2, 7, 7, 5), emptyList(), emptyList()), moves(0, 4, 1, 5, 2, 5, 1, 2, 0, 1, 3, 1, 3, 4, 2, 0, 0, 3, 2, 5)),
                level(5, 4, listOf(bottle(3, 3, 6, 7), bottle(3, 7, 6, 2), bottle(0, 6, 0, 7), bottle(0, 3, 7, 2), bottle(2, 2, 6, 0), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 5, 4, 2, 4, 6, 1, 4, 3, 4, 1, 6, 1, 3, 3, 5, 3, 0, 0, 1, 2, 3, 2, 6, 2, 3)),
                level(6, 4, listOf(bottle(8, 3, 0, 3), bottle(7, 3, 7, 2), bottle(0, 0, 7, 8), bottle(3, 2, 0, 2), bottle(7, 8, 2, 8), emptyList(), emptyList()), moves(0, 5, 1, 6, 3, 6, 0, 3, 0, 5, 2, 0, 2, 1, 3, 2, 3, 6, 4, 0, 4, 6, 4, 0, 1, 4, 1, 3, 1, 4, 3, 5)),
                level(7, 4, listOf(bottle(6, 1, 6, 1), bottle(6, 4, 8, 0), bottle(4, 0, 4, 8), bottle(0, 0, 8, 8), bottle(1, 1, 6, 4), emptyList(), emptyList()), moves(3, 5, 1, 3, 1, 5, 2, 5, 2, 1, 2, 3, 1, 2, 4, 2, 4, 1, 0, 4, 0, 1, 0, 4, 0, 1)),
                level(8, 4, listOf(bottle(6, 0, 8, 8), bottle(3, 6, 3, 8), bottle(6, 0, 3, 0), bottle(8, 4, 6, 4), bottle(4, 0, 4, 3), emptyList(), emptyList()), moves(0, 5, 0, 6, 1, 5, 2, 6, 1, 2, 1, 0, 2, 1, 4, 1, 2, 6, 3, 4, 3, 0, 0, 2, 4, 3, 4, 6, 3, 4, 3, 5)),
                level(9, 5, listOf(bottle(0, 8, 1, 6, 3), bottle(0, 8, 1, 1, 7), bottle(8, 6, 7, 0, 1), bottle(8, 7, 3, 0, 6), bottle(7, 7, 6, 0, 1), bottle(3, 6, 3, 3, 8), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 0, 3, 7, 4, 0, 4, 2, 7, 4, 0, 7, 5, 0, 5, 6, 4, 5, 1, 4, 1, 7, 0, 1, 2, 0, 2, 4, 3, 0, 3, 6, 3, 4, 1, 3, 0, 1, 2, 0, 5, 0, 2, 3, 5, 6)),
                level(10, 5, listOf(bottle(5, 3, 8, 5, 7), bottle(5, 4, 4, 4, 4), bottle(8, 7, 8, 4, 6), bottle(3, 5, 7, 3, 3), bottle(6, 5, 6, 8, 7), bottle(6, 6, 8, 3, 7), emptyList(), emptyList()), moves(1, 6, 0, 7, 0, 1, 4, 7, 0, 4, 3, 0, 3, 7, 3, 1, 0, 3, 5, 7, 5, 3, 4, 5, 2, 4, 2, 6, 0, 1, 5, 0, 2, 0, 2, 7, 0, 2, 4, 5, 4, 1, 4, 5)),
                level(11, 5, listOf(bottle(8, 2, 6, 2, 2), bottle(8, 2, 8, 1, 1), bottle(6, 4, 7, 7, 6), bottle(4, 1, 7, 7, 8), bottle(6, 8, 4, 4, 2), bottle(1, 6, 4, 1, 7), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 7, 4, 0, 5, 2, 6, 0, 1, 6, 3, 1, 5, 6, 4, 5, 4, 1, 4, 7, 0, 4, 1, 0, 1, 4, 0, 1, 2, 0, 3, 0, 3, 6, 2, 3, 5, 3, 5, 2, 2, 7, 5, 6)),
                level(12, 5, listOf(bottle(7, 0, 8, 3, 0), bottle(7, 2, 8, 3, 6), bottle(7, 6, 7, 6, 8), bottle(0, 3, 8, 0, 0), bottle(2, 7, 6, 2, 6), bottle(3, 3, 2, 8, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 0, 1, 2, 1, 7, 0, 1, 3, 0, 6, 0, 1, 6, 3, 6, 3, 7, 0, 3, 5, 1, 5, 6, 5, 1, 5, 7, 2, 5, 2, 0, 2, 5, 4, 5, 4, 1, 4, 5, 4, 0, 1, 4, 0, 1, 1, 2)),
                level(13, 5, listOf(bottle(5, 0, 5, 6, 0), bottle(6, 4, 5, 0, 0), bottle(6, 2, 2, 8, 8), bottle(5, 4, 4, 7, 4), bottle(4, 7, 2, 8, 2), bottle(5, 7, 8, 7, 8), bottle(6, 0, 2, 7, 6), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 7, 0, 1, 0, 7, 1, 0, 3, 1, 6, 8, 3, 6, 1, 3, 1, 8, 2, 1, 4, 2, 4, 1, 4, 2, 5, 1, 4, 5, 3, 4, 0, 3, 2, 0, 2, 8, 5, 2, 5, 1, 5, 2, 6, 2, 6, 0, 6, 7, 3, 5, 6, 8)),
                level(14, 5, listOf(bottle(3, 1, 6, 6, 6), bottle(7, 7, 1, 2, 0), bottle(8, 0, 8, 8, 1), bottle(1, 0, 3, 2, 3), bottle(6, 0, 8, 7, 3), bottle(6, 8, 3, 2, 7), bottle(0, 2, 2, 1, 7), emptyList(), emptyList()), moves(0, 7, 0, 8, 2, 8, 3, 0, 4, 0, 6, 4, 6, 8, 3, 6, 3, 0, 1, 3, 1, 6, 1, 8, 4, 1, 5, 1, 2, 4, 2, 3, 4, 2, 3, 4, 3, 8, 6, 3, 4, 6, 5, 3, 5, 0, 5, 2, 4, 5, 5, 7)),
                level(15, 5, listOf(bottle(5, 8, 2, 7, 1), bottle(3, 5, 7, 5, 7), bottle(1, 6, 3, 7, 6), bottle(6, 5, 1, 3, 8), bottle(2, 7, 5, 1, 6), bottle(1, 8, 2, 3, 8), bottle(3, 2, 2, 6, 8), emptyList(), emptyList()), moves(0, 7, 1, 0, 2, 8, 4, 8, 7, 4, 1, 7, 1, 2, 7, 1, 3, 7, 5, 7, 3, 5, 4, 3, 1, 4, 5, 1, 6, 7, 6, 8, 6, 5, 1, 6, 0, 1, 2, 1, 2, 6, 2, 8, 3, 2, 4, 3, 4, 1, 0, 4, 5, 4, 0, 5, 3, 0, 5, 7, 2, 5, 3, 8)),
                level(16, 5, listOf(bottle(4, 8, 4, 1, 4), bottle(0, 1, 5, 8, 4), bottle(6, 5, 1, 6, 1), bottle(1, 8, 4, 5, 6), bottle(0, 3, 3, 6, 3), bottle(3, 5, 0, 0, 6), bottle(5, 3, 8, 0, 8), emptyList(), emptyList()), moves(0, 7, 0, 8, 0, 7, 1, 7, 0, 1, 2, 8, 3, 2, 0, 7, 1, 0, 1, 3, 1, 8, 6, 0, 6, 1, 6, 0, 4, 6, 5, 4, 5, 1, 3, 5, 3, 7, 3, 0, 3, 8, 2, 3, 2, 8, 2, 5, 4, 2, 6, 4, 5, 6, 4, 5, 1, 4, 2, 3))
            )
    )

    private fun createAptekaBarLevels(): CatalogBar = CatalogBar(
            cityName = "Витебск",
            streetName = "Суворова",
            barName = "Аптека бар",
            rank = 4,
            levels = listOf(
                level(1, 5, listOf(bottle(8, 1, 6, 1, 2), bottle(2, 8, 8, 5, 8), bottle(1, 2, 6, 5, 5), bottle(1, 1, 2, 6, 5), bottle(2, 6, 8, 6, 5), emptyList(), emptyList()), moves(0, 5, 2, 6, 3, 6, 2, 3, 2, 5, 0, 2, 3, 0, 3, 5, 4, 6, 2, 3, 0, 2, 0, 3, 1, 0, 1, 6, 1, 0, 4, 2, 4, 0, 4, 2, 1, 4, 4, 5)),
                level(2, 5, listOf(bottle(0, 0, 4, 2, 4), bottle(5, 0, 2, 2, 5), bottle(0, 8, 8, 2, 4), bottle(8, 8, 5, 4, 5), bottle(4, 5, 2, 8, 0), emptyList(), emptyList()), moves(0, 5, 0, 6, 0, 5, 2, 5, 2, 6, 4, 0, 4, 2, 4, 6, 1, 4, 1, 6, 1, 0, 4, 1, 3, 1, 3, 4, 3, 1, 2, 3, 0, 2, 4, 5)),
                level(3, 5, listOf(bottle(4, 4, 2, 8, 7), bottle(7, 4, 6, 4, 6), bottle(8, 6, 8, 2, 2), bottle(7, 8, 7, 2, 2), bottle(8, 6, 6, 4, 7), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 0, 2, 6, 1, 2, 4, 5, 1, 4, 2, 1, 2, 6, 0, 2, 3, 2, 3, 5, 3, 6, 4, 0, 3, 5, 4, 3, 1, 3, 1, 0, 1, 5, 4, 6)),
                level(4, 5, listOf(bottle(0, 1, 5, 1, 5), bottle(7, 1, 3, 3, 0), bottle(5, 0, 0, 7, 7), bottle(7, 3, 1, 1, 5), bottle(5, 3, 7, 3, 0), emptyList(), emptyList()), moves(0, 5, 0, 6, 0, 5, 0, 6, 1, 0, 3, 5, 3, 6, 3, 1, 2, 3, 2, 0, 4, 0, 2, 5, 1, 2, 1, 6, 4, 2, 4, 1, 4, 2, 1, 3, 4, 5)),
                level(5, 5, listOf(bottle(4, 1, 1, 1, 7), bottle(8, 7, 4, 8, 4), bottle(4, 4, 2, 7, 8), bottle(1, 2, 2, 8, 7), bottle(5, 2, 1, 5, 7), bottle(2, 5, 8, 5, 5), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 0, 3, 6, 1, 3, 1, 0, 1, 6, 2, 1, 2, 6, 4, 6, 3, 1, 2, 3, 0, 2, 4, 0, 4, 7, 3, 4, 3, 7, 5, 0, 5, 1, 5, 0, 4, 5, 0, 4)),
                level(6, 5, listOf(bottle(2, 0, 0, 4, 2), bottle(0, 0, 6, 2, 3), bottle(0, 6, 7, 7, 3), bottle(6, 3, 7, 2, 2), bottle(4, 6, 3, 7, 4), bottle(4, 7, 3, 6, 4), emptyList(), emptyList()), moves(0, 6, 0, 7, 3, 6, 4, 7, 3, 4, 1, 3, 1, 6, 2, 3, 5, 7, 1, 5, 0, 1, 0, 6, 3, 0, 5, 3, 5, 0, 5, 2, 5, 7, 2, 5, 4, 5, 4, 0, 2, 3, 4, 3, 1, 2, 4, 7)),
                level(7, 5, listOf(bottle(6, 1, 0, 1, 4), bottle(4, 0, 6, 0, 4), bottle(5, 6, 5, 2, 2), bottle(2, 1, 5, 4, 0), bottle(1, 4, 6, 6, 5), bottle(2, 1, 0, 5, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 6, 1, 0, 3, 0, 3, 6, 4, 3, 1, 4, 0, 1, 0, 7, 4, 0, 4, 6, 4, 7, 1, 4, 1, 6, 2, 1, 2, 3, 2, 0, 3, 2, 3, 7, 5, 1, 5, 2, 5, 4, 5, 7, 1, 3, 3, 5)),
                level(8, 5, listOf(bottle(6, 6, 4, 2, 0), bottle(0, 4, 3, 6, 0), bottle(3, 6, 3, 2, 4), bottle(4, 3, 2, 0, 7), bottle(2, 4, 7, 7, 7), bottle(3, 6, 2, 0, 7), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 6, 2, 0, 7, 2, 3, 7, 3, 6, 2, 3, 4, 7, 5, 7, 0, 4, 1, 0, 1, 2, 4, 1, 3, 4, 3, 2, 1, 3, 5, 1, 5, 4, 5, 0, 1, 6, 2, 5, 2, 0, 2, 5)),
                level(9, 6, listOf(bottle(0, 8, 8, 5, 1, 8), bottle(2, 5, 8, 8, 2, 7), bottle(5, 1, 2, 0, 0, 7), bottle(7, 1, 5, 0, 1, 0), bottle(1, 5, 7, 5, 8, 2), bottle(0, 2, 7, 1, 7, 2), emptyList(), emptyList()), moves(0, 6, 1, 7, 2, 7, 3, 2, 0, 3, 4, 1, 4, 6, 0, 4, 0, 6, 2, 0, 1, 2, 1, 6, 1, 4, 2, 1, 3, 2, 3, 0, 4, 3, 4, 7, 3, 4, 2, 3, 4, 2, 3, 4, 5, 1, 5, 3, 5, 4, 5, 3, 5, 1, 0, 5, 3, 7)),
                level(10, 6, listOf(bottle(4, 6, 4, 0, 0, 2), bottle(8, 4, 6, 1, 1, 4), bottle(2, 6, 8, 6, 0, 4), bottle(0, 0, 2, 8, 1, 2), bottle(1, 1, 8, 6, 2, 8), bottle(0, 1, 4, 2, 6, 8), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 0, 2, 0, 2, 7, 3, 6, 3, 1, 4, 3, 4, 6, 2, 4, 2, 3, 2, 4, 2, 6, 0, 2, 4, 0, 5, 4, 5, 0, 5, 6, 2, 5, 0, 2, 5, 0, 1, 5, 1, 2, 1, 0, 4, 1, 5, 4, 3, 1, 3, 6, 3, 5, 5, 7)),
                level(11, 6, listOf(bottle(5, 5, 5, 6, 4, 4), bottle(5, 7, 4, 5, 6, 6), bottle(7, 7, 1, 1, 4, 7), bottle(6, 6, 1, 1, 7, 2), bottle(2, 1, 5, 2, 6, 2), bottle(4, 2, 2, 1, 4, 7), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 1, 0, 1, 6, 2, 1, 2, 6, 5, 1, 5, 6, 2, 5, 1, 2, 0, 1, 5, 0, 3, 5, 3, 2, 3, 0, 4, 5, 4, 3, 3, 7, 4, 5, 4, 1, 4, 0, 5, 4, 5, 6)),
                level(12, 6, listOf(bottle(2, 2, 5, 3, 0, 4), bottle(7, 2, 0, 4, 2, 0), bottle(3, 7, 5, 0, 5, 3), bottle(7, 0, 5, 2, 7, 4), bottle(5, 5, 7, 4, 0, 2), bottle(3, 4, 7, 4, 3, 3), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 2, 0, 3, 6, 4, 1, 4, 7, 4, 6, 4, 3, 2, 4, 2, 7, 2, 4, 3, 2, 1, 3, 1, 6, 1, 7, 3, 1, 3, 4, 3, 7, 2, 3, 0, 2, 0, 4, 1, 0, 5, 2, 5, 6, 5, 1, 5, 6, 1, 3, 2, 5)),
                level(13, 6, listOf(bottle(6, 5, 5, 7, 7, 4), bottle(2, 4, 3, 3, 2, 4), bottle(3, 3, 2, 5, 5, 0), bottle(5, 2, 2, 0, 6, 7), bottle(6, 7, 6, 3, 5, 4), bottle(4, 2, 7, 4, 0, 3), bottle(7, 6, 0, 0, 6, 0), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 7, 3, 8, 4, 7, 4, 0, 5, 4, 2, 5, 2, 0, 2, 1, 4, 2, 3, 4, 5, 3, 5, 7, 5, 8, 1, 5, 1, 2, 1, 7, 5, 1, 5, 7, 3, 5, 3, 1, 0, 3, 4, 0, 4, 8, 6, 5, 6, 0, 6, 5, 6, 0, 0, 4, 6, 8)),
                level(14, 6, listOf(bottle(1, 3, 1, 3, 7, 0), bottle(4, 0, 5, 3, 0, 8), bottle(0, 5, 1, 8, 0, 4), bottle(4, 3, 1, 4, 7, 3), bottle(7, 7, 5, 4, 1, 8), bottle(1, 3, 7, 5, 8, 4), bottle(5, 8, 0, 5, 8, 7), emptyList(), emptyList()), moves(0, 7, 0, 8, 3, 0, 3, 8, 2, 3, 2, 7, 1, 2, 1, 7, 0, 1, 4, 2, 0, 4, 1, 0, 5, 3, 6, 8, 5, 6, 1, 5, 1, 7, 3, 1, 4, 3, 4, 1, 4, 5, 4, 8, 0, 4, 3, 0, 3, 4, 1, 3, 2, 1, 2, 0, 5, 2, 5, 8, 5, 4, 0, 5, 6, 1, 6, 2, 6, 7, 6, 1, 2, 6, 2, 7)),
                level(15, 6, listOf(bottle(6, 7, 3, 4, 8, 4), bottle(8, 0, 0, 8, 4, 7), bottle(7, 0, 7, 1, 6, 4), bottle(1, 6, 8, 0, 6, 3), bottle(1, 8, 1, 6, 3, 3), bottle(4, 7, 8, 4, 1, 0), bottle(0, 1, 3, 7, 6, 3), emptyList(), emptyList()), moves(0, 7, 0, 8, 0, 7, 2, 7, 3, 0, 2, 3, 4, 0, 3, 4, 5, 3, 2, 5, 1, 2, 1, 7, 1, 8, 3, 1, 3, 8, 4, 3, 5, 4, 5, 7, 5, 8, 5, 2, 5, 7, 3, 5, 4, 3, 4, 8, 3, 4, 0, 3, 2, 0, 1, 2, 1, 8, 6, 3, 6, 5, 6, 0, 6, 3, 6, 4, 2, 6, 0, 2, 0, 5)),
                level(16, 6, listOf(bottle(0, 7, 0, 3, 8, 1), bottle(0, 3, 5, 5, 8, 7), bottle(3, 5, 1, 1, 0, 3), bottle(1, 7, 8, 0, 7, 7), bottle(2, 8, 2, 8, 7, 2), bottle(5, 5, 2, 3, 1, 1), bottle(2, 8, 2, 0, 5, 3), emptyList(), emptyList()), moves(0, 7, 0, 8, 2, 0, 5, 7, 0, 5, 0, 2, 1, 0, 1, 8, 3, 0, 2, 3, 2, 7, 2, 1, 5, 2, 4, 5, 4, 0, 4, 8, 5, 4, 1, 5, 1, 2, 6, 2, 6, 5, 3, 1, 3, 8, 6, 1, 4, 6, 4, 8, 6, 4, 6, 8, 4, 6, 0, 4, 3, 4, 0, 1, 3, 7)),
                level(17, 6, listOf(bottle(8, 3, 1, 0, 4, 8), bottle(4, 5, 0, 4, 3, 0), bottle(4, 1, 7, 2, 2, 5), bottle(2, 3, 5, 8, 0, 4), bottle(5, 3, 2, 0, 0, 1), bottle(7, 1, 1, 3, 1, 8), bottle(4, 7, 2, 7, 5, 3), bottle(7, 8, 5, 7, 8, 2), emptyList(), emptyList()), moves(0, 8, 0, 9, 1, 0, 3, 9, 3, 0, 3, 8, 2, 3, 5, 8, 4, 5, 6, 1, 6, 3, 7, 2, 7, 8, 6, 7, 2, 6, 2, 7, 5, 2, 1, 5, 1, 9, 0, 1, 2, 0, 2, 9, 0, 2, 5, 0, 5, 2, 7, 5, 3, 7, 3, 0, 6, 3, 6, 5, 6, 9, 0, 6, 0, 8, 1, 0, 4, 0, 4, 3, 4, 6, 1, 4, 7, 4, 7, 8, 1, 9, 5, 7)),
                level(18, 6, listOf(bottle(0, 3, 7, 5, 5, 1), bottle(2, 7, 8, 2, 5, 3), bottle(1, 3, 0, 1, 8, 3), bottle(4, 8, 8, 5, 4, 7), bottle(8, 4, 3, 2, 4, 1), bottle(3, 1, 2, 8, 0, 0), bottle(1, 0, 7, 5, 4, 5), bottle(0, 2, 2, 4, 7, 7), emptyList(), emptyList()), moves(0, 8, 0, 9, 3, 0, 4, 8, 3, 4, 6, 3, 7, 0, 6, 7, 9, 6, 4, 9, 7, 9, 4, 7, 1, 4, 1, 3, 1, 7, 2, 4, 1, 2, 0, 1, 0, 4, 5, 0, 2, 5, 2, 8, 2, 0, 4, 2, 4, 9, 5, 4, 5, 7, 5, 8, 2, 5, 2, 8, 1, 2, 7, 1, 0, 7, 3, 0, 3, 4, 6, 0, 6, 2, 6, 7, 3, 9, 6, 8)),
                level(19, 6, listOf(bottle(7, 2, 0, 5, 7, 0), bottle(4, 8, 3, 4, 4, 8), bottle(2, 8, 6, 5, 6, 3), bottle(5, 5, 2, 3, 2, 6), bottle(4, 4, 8, 4, 7, 6), bottle(7, 6, 6, 3, 8, 7), bottle(0, 8, 0, 2, 7, 5), bottle(3, 0, 3, 2, 0, 5), emptyList(), emptyList()), moves(0, 8, 0, 9, 5, 9, 1, 5, 6, 0, 6, 9, 7, 0, 7, 8, 6, 7, 6, 8, 5, 6, 2, 5, 3, 2, 3, 7, 3, 5, 7, 3, 5, 7, 2, 5, 4, 5, 4, 9, 4, 1, 6, 4, 6, 8, 3, 6, 0, 3, 2, 3, 0, 8, 0, 6, 0, 9, 5, 0, 2, 0, 4, 2, 5, 9, 2, 5, 2, 6, 1, 4, 1, 2, 1, 5, 1, 4, 7, 2, 7, 8, 2, 7)),
                level(20, 6, listOf(bottle(5, 2, 3, 1, 1, 7), bottle(1, 7, 4, 7, 2, 3), bottle(5, 7, 1, 6, 8, 6), bottle(3, 4, 4, 8, 3, 4), bottle(6, 2, 3, 7, 6, 5), bottle(8, 8, 8, 8, 2, 1), bottle(5, 1, 6, 2, 7, 4), bottle(2, 3, 5, 4, 6, 5), emptyList(), emptyList()), moves(0, 8, 0, 9, 1, 0, 5, 9, 1, 5, 1, 8, 3, 1, 3, 0, 6, 1, 6, 8, 5, 6, 3, 5, 1, 3, 1, 8, 1, 9, 3, 1, 0, 3, 6, 0, 2, 6, 2, 5, 2, 6, 2, 9, 2, 8, 4, 2, 4, 6, 4, 8, 4, 3, 4, 0, 6, 4, 6, 9, 7, 2, 7, 4, 7, 1, 7, 2, 7, 3, 0, 7, 0, 2, 2, 6))
            )
    )

    private fun createKianuLevels(): CatalogBar = CatalogBar(
            cityName = "Москва",
            streetName = "Патрики",
            barName = "Киану",
            rank = 1,
            levels = listOf(
                level(1, 3, listOf(bottle(1, 3, 3), bottle(1, 1, 3), emptyList(), emptyList()), moves(0, 2, 1, 2, 0, 1)),
                level(2, 3, listOf(bottle(7, 3, 3), bottle(7, 7, 3), emptyList(), emptyList()), moves(0, 2, 1, 2, 0, 1)),
                level(3, 3, listOf(bottle(0, 8, 8), bottle(0, 0, 8), emptyList(), emptyList()), moves(0, 2, 1, 2, 0, 1)),
                level(4, 3, listOf(bottle(1, 7, 1), bottle(1, 7, 7), emptyList(), emptyList()), moves(1, 2, 0, 1, 0, 2, 0, 1)),
                level(5, 3, listOf(bottle(8, 8, 1), bottle(5, 8, 1), bottle(5, 1, 5), emptyList(), emptyList()), moves(0, 3, 1, 3, 1, 0, 2, 1, 2, 3, 1, 2)),
                level(6, 3, listOf(bottle(1, 8, 3), bottle(1, 8, 1), bottle(8, 3, 3), emptyList(), emptyList()), moves(2, 3, 0, 3, 0, 2, 1, 0, 1, 2, 0, 1)),
                level(7, 3, listOf(bottle(4, 4, 7), bottle(6, 6, 7), bottle(7, 6, 4), emptyList(), emptyList()), moves(0, 3, 2, 0, 1, 3, 2, 1, 2, 3)),
                level(8, 3, listOf(bottle(5, 5, 8), bottle(8, 8, 6), bottle(6, 5, 6), emptyList(), emptyList()), moves(1, 3, 0, 1, 2, 3, 2, 0, 2, 3))
            )
    )

    private fun createBronLevels(): CatalogBar = CatalogBar(
            cityName = "Москва",
            streetName = "Патрики",
            barName = "Bro&n",
            rank = 2,
            levels = listOf(
                level(1, 3, listOf(bottle(4, 5, 8), bottle(5, 8, 5), bottle(4, 8, 4), emptyList(), emptyList()), moves(0, 3, 1, 0, 1, 3, 0, 1, 2, 0, 2, 3, 0, 2)),
                level(2, 3, listOf(bottle(3, 5, 3), bottle(5, 3, 1), bottle(1, 5, 1), emptyList(), emptyList()), moves(1, 3, 0, 1, 2, 3, 0, 2, 1, 0, 2, 1, 2, 3)),
                level(3, 3, listOf(bottle(1, 2, 1), bottle(2, 4, 1), bottle(2, 4, 4), emptyList(), emptyList()), moves(2, 3, 0, 4, 0, 2, 1, 0, 1, 3, 0, 4, 1, 2)),
                level(4, 3, listOf(bottle(2, 3, 5), bottle(5, 3, 5), bottle(2, 3, 2), emptyList(), emptyList()), moves(0, 3, 0, 4, 1, 3, 1, 4, 1, 3, 2, 0, 2, 4, 0, 2)),
                level(5, 3, listOf(bottle(1, 5, 1), bottle(0, 3, 1), bottle(3, 3, 5), bottle(0, 0, 5), emptyList(), emptyList()), moves(2, 4, 3, 4, 0, 5, 0, 4, 1, 0, 1, 2, 0, 5, 1, 3)),
                level(6, 3, listOf(bottle(0, 4, 4), bottle(8, 6, 4), bottle(0, 0, 8), bottle(6, 6, 8), emptyList(), emptyList()), moves(0, 4, 1, 4, 1, 5, 2, 1, 3, 1, 0, 2, 3, 5)),
                level(7, 3, listOf(bottle(7, 3, 5), bottle(3, 7, 8), bottle(3, 8, 7), bottle(5, 8, 5), emptyList(), emptyList()), moves(0, 4, 1, 5, 2, 1, 2, 5, 0, 2, 1, 0, 1, 2, 3, 4, 3, 5, 3, 4)),
                level(8, 3, listOf(bottle(4, 3, 6), bottle(3, 4, 6), bottle(5, 4, 5), bottle(5, 6, 3), emptyList(), emptyList()), moves(0, 4, 1, 4, 3, 0, 3, 4, 2, 3, 1, 2, 0, 1, 2, 0, 2, 3)),
                level(9, 4, listOf(bottle(8, 1, 1, 1), bottle(3, 1, 0, 0), bottle(3, 5, 0, 3), bottle(3, 5, 0, 8), bottle(8, 8, 5, 5), emptyList(), emptyList()), moves(0, 5, 4, 6, 3, 0, 0, 4, 1, 0, 1, 5, 2, 1, 2, 0, 3, 0, 2, 3, 3, 6, 1, 2, 2, 3)),
                level(10, 4, listOf(bottle(8, 8, 8, 0), bottle(6, 6, 8, 6), bottle(0, 0, 3, 1), bottle(1, 6, 1, 0), bottle(3, 3, 3, 1), emptyList(), emptyList()), moves(4, 5, 2, 5, 2, 4, 0, 2, 3, 2, 3, 5, 1, 3, 1, 0, 3, 1, 3, 5)),
                level(11, 4, listOf(bottle(1, 1, 4, 5), bottle(5, 1, 5, 4), bottle(0, 3, 3, 0), bottle(0, 1, 4, 3), bottle(4, 0, 3, 5), emptyList(), emptyList()), moves(0, 5, 1, 0, 1, 5, 3, 6, 4, 5, 4, 6, 2, 4, 2, 6, 4, 2, 0, 4, 3, 4, 1, 0, 3, 0, 1, 5, 2, 3)),
                level(12, 4, listOf(bottle(2, 0, 8, 5), bottle(2, 3, 8, 2), bottle(0, 5, 0, 5), bottle(3, 2, 0, 3), bottle(8, 3, 5, 8), emptyList(), emptyList()), moves(0, 5, 0, 6, 2, 5, 2, 0, 2, 5, 0, 2, 1, 0, 1, 6, 3, 1, 3, 2, 3, 0, 1, 3, 0, 1, 4, 6, 4, 5, 4, 3, 4, 6))
            )
    )

    private fun createZoykaLevels(): CatalogBar = CatalogBar(
            cityName = "Москва",
            streetName = "Патрики",
            barName = "Зойка",
            rank = 4,
            levels = listOf(
                level(1, 5, listOf(bottle(0, 7, 5, 5, 5), bottle(3, 7, 7, 1, 1), bottle(5, 7, 0, 1, 1), bottle(1, 5, 0, 0, 3), bottle(3, 3, 7, 3, 0), emptyList(), emptyList()), moves(0, 5, 0, 6, 4, 0, 3, 4, 3, 0, 3, 5, 1, 3, 2, 3, 2, 0, 1, 2, 2, 6, 2, 5, 4, 1, 4, 6, 1, 4)),
                level(2, 5, listOf(bottle(6, 3, 4, 3, 4), bottle(0, 6, 1, 0, 4), bottle(0, 3, 6, 1, 1), bottle(3, 3, 6, 1, 0), bottle(4, 6, 1, 0, 4), emptyList(), emptyList()), moves(0, 5, 0, 6, 0, 5, 0, 6, 1, 5, 4, 5, 1, 4, 2, 1, 2, 0, 2, 6, 3, 2, 4, 2, 3, 4, 3, 0, 3, 6, 1, 3, 4, 3, 1, 0, 4, 0, 1, 2, 4, 5)),
                level(3, 5, listOf(bottle(4, 7, 3, 3, 1), bottle(3, 7, 1, 1, 7), bottle(0, 0, 4, 3, 4), bottle(1, 7, 4, 7, 0), bottle(3, 0, 0, 4, 1), emptyList(), emptyList()), moves(0, 5, 1, 6, 1, 5, 1, 6, 0, 1, 0, 6, 2, 0, 2, 1, 2, 0, 3, 2, 3, 6, 3, 0, 3, 6, 4, 3, 4, 0, 4, 2, 1, 4, 3, 5)),
                level(4, 5, listOf(bottle(0, 6, 0, 7, 8), bottle(3, 7, 3, 7, 7), bottle(0, 6, 0, 6, 3), bottle(8, 0, 3, 7, 6), bottle(8, 8, 3, 8, 6), emptyList(), emptyList()), moves(0, 5, 0, 6, 1, 6, 2, 1, 3, 2, 3, 6, 1, 3, 1, 6, 3, 1, 3, 0, 3, 5, 2, 3, 0, 2, 0, 3, 2, 0, 2, 3, 4, 3, 0, 2, 4, 5, 4, 1, 4, 5)),
                level(5, 5, listOf(bottle(3, 3, 8, 3, 6), bottle(7, 1, 3, 4, 7), bottle(6, 7, 8, 8, 1), bottle(8, 6, 4, 4, 8), bottle(1, 1, 3, 7, 6), bottle(6, 4, 7, 4, 1), emptyList(), emptyList()), moves(0, 6, 0, 7, 3, 0, 4, 6, 1, 4, 1, 3, 7, 1, 3, 7, 3, 6, 0, 3, 1, 0, 2, 1, 2, 3, 4, 2, 4, 0, 1, 4, 5, 4, 2, 1, 5, 7, 5, 1, 5, 7, 2, 5, 5, 6)),
                level(6, 5, listOf(bottle(6, 4, 5, 6, 7), bottle(4, 7, 6, 2, 4), bottle(1, 4, 1, 7, 6), bottle(4, 5, 1, 1, 5), bottle(2, 2, 2, 6, 5), bottle(5, 7, 7, 2, 1), emptyList(), emptyList()), moves(0, 6, 0, 7, 2, 7, 2, 6, 3, 0, 2, 3, 1, 2, 4, 0, 4, 7, 1, 4, 1, 7, 1, 6, 2, 1, 3, 2, 5, 2, 5, 4, 5, 6, 3, 5, 0, 5, 0, 1, 0, 7, 1, 3)),
                level(7, 5, listOf(bottle(4, 7, 7, 2, 4), bottle(0, 3, 6, 6, 4), bottle(3, 7, 0, 2, 0), bottle(2, 7, 3, 3, 3), bottle(2, 6, 7, 0, 6), bottle(6, 4, 4, 0, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 6, 4, 1, 2, 4, 2, 7, 4, 2, 0, 4, 5, 7, 0, 6, 2, 0, 4, 2, 1, 4, 3, 1, 5, 0, 5, 6, 4, 5, 4, 7, 2, 4, 1, 2, 3, 4, 0, 1, 3, 7)),
                level(8, 5, listOf(bottle(8, 3, 1, 1, 7), bottle(2, 8, 6, 6, 7), bottle(7, 3, 2, 6, 7), bottle(6, 2, 6, 3, 1), bottle(3, 8, 1, 1, 2), bottle(2, 3, 7, 8, 8), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 6, 2, 6, 2, 1, 3, 7, 0, 3, 4, 2, 4, 7, 4, 0, 3, 4, 5, 0, 5, 6, 5, 4, 2, 5, 2, 4, 2, 6, 1, 2, 1, 0, 3, 2, 3, 1, 1, 5, 2, 3)),
                level(9, 6, listOf(bottle(1, 1, 2, 1, 3, 5), bottle(2, 8, 3, 7, 8, 1), bottle(2, 2, 3, 3, 3, 5), bottle(8, 8, 5, 3, 7, 7), bottle(5, 2, 1, 5, 2, 7), bottle(8, 8, 7, 1, 7, 5), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 0, 2, 6, 2, 7, 5, 6, 4, 5, 4, 2, 4, 6, 0, 4, 0, 2, 4, 0, 4, 2, 4, 6, 3, 4, 3, 7, 3, 6, 1, 3, 1, 4, 1, 7, 1, 3, 1, 2, 5, 4, 5, 0, 5, 4, 3, 5)),
                level(10, 6, listOf(bottle(4, 7, 6, 3, 0, 5), bottle(6, 3, 5, 4, 3, 6), bottle(4, 5, 7, 4, 7, 7), bottle(4, 0, 7, 0, 5, 0), bottle(0, 0, 5, 3, 6, 4), bottle(3, 5, 7, 6, 6, 3), emptyList(), emptyList()), moves(0, 6, 0, 7, 3, 7, 3, 6, 3, 7, 2, 3, 4, 2, 1, 4, 0, 1, 4, 0, 1, 4, 1, 2, 1, 6, 4, 1, 4, 6, 5, 1, 4, 7, 1, 4, 0, 1, 5, 1, 0, 3, 2, 0, 2, 5, 2, 6, 0, 2, 3, 0, 3, 7, 5, 0, 5, 6, 2, 3, 4, 5)),
                level(11, 6, listOf(bottle(3, 3, 2, 1, 1, 0), bottle(6, 6, 2, 3, 1, 1), bottle(3, 0, 4, 4, 4, 1), bottle(3, 6, 0, 1, 6, 6), bottle(2, 2, 0, 0, 6, 4), bottle(3, 4, 0, 2, 4, 2), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 7, 2, 7, 5, 0, 5, 2, 0, 5, 1, 0, 5, 1, 5, 6, 2, 5, 2, 6, 0, 2, 1, 0, 3, 1, 3, 7, 3, 6, 3, 1, 2, 3, 5, 2, 4, 2, 4, 1, 4, 6, 0, 4, 3, 5)),
                level(12, 6, listOf(bottle(2, 5, 4, 8, 7, 3), bottle(2, 3, 3, 2, 5, 8), bottle(2, 7, 4, 7, 3, 5), bottle(4, 8, 7, 4, 3, 5), bottle(7, 8, 3, 4, 8, 7), bottle(8, 2, 2, 5, 4, 5), emptyList(), emptyList()), moves(0, 6, 0, 7, 1, 0, 2, 1, 2, 6, 2, 7, 4, 7, 4, 0, 4, 2, 4, 6, 0, 4, 0, 2, 1, 0, 3, 0, 3, 6, 3, 2, 3, 7, 3, 4, 2, 3, 2, 7, 1, 2, 1, 6, 5, 0, 5, 3, 1, 2, 0, 1, 5, 1, 5, 0, 4, 5, 0, 2, 4, 7)),
                level(13, 6, listOf(bottle(5, 3, 7, 7, 5, 3), bottle(5, 4, 7, 4, 8, 3), bottle(8, 1, 3, 3, 0, 0), bottle(4, 4, 7, 5, 0, 5), bottle(0, 8, 0, 1, 7, 8), bottle(7, 3, 5, 1, 1, 4), bottle(1, 8, 1, 0, 4, 8), emptyList(), emptyList()), moves(0, 7, 1, 7, 1, 8, 3, 0, 4, 8, 5, 1, 6, 8, 6, 1, 2, 6, 2, 7, 2, 5, 2, 8, 1, 2, 1, 4, 2, 1, 3, 2, 0, 3, 4, 0, 6, 2, 6, 4, 6, 8, 4, 6, 5, 6, 3, 5, 4, 2, 4, 8, 2, 4, 1, 2, 5, 1, 5, 7, 3, 5, 0, 5, 0, 7, 0, 1, 2, 3)),
                level(14, 6, listOf(bottle(1, 5, 7, 7, 1, 7), bottle(0, 2, 6, 1, 2, 2), bottle(3, 3, 6, 7, 2, 5), bottle(3, 0, 1, 5, 0, 5), bottle(0, 1, 3, 7, 5, 6), bottle(3, 6, 6, 0, 6, 0), bottle(2, 3, 2, 1, 5, 7), emptyList(), emptyList()), moves(0, 7, 0, 8, 0, 7, 2, 0, 3, 0, 5, 3, 4, 5, 4, 0, 4, 7, 6, 7, 6, 0, 6, 8, 1, 6, 1, 8, 2, 6, 2, 7, 2, 1, 4, 2, 4, 8, 5, 1, 4, 5, 0, 4, 0, 8, 1, 0, 6, 1, 6, 2, 1, 6, 3, 1, 3, 4, 3, 8, 3, 1, 5, 1, 5, 0, 2, 3, 3, 5)),
                level(15, 6, listOf(bottle(0, 0, 8, 0, 7, 2), bottle(8, 3, 4, 1, 0, 2), bottle(8, 3, 4, 0, 8, 3), bottle(1, 8, 4, 2, 3, 7), bottle(7, 4, 8, 4, 7, 4), bottle(3, 0, 2, 1, 1, 1), bottle(3, 7, 1, 7, 2, 2), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 7, 0, 1, 3, 8, 2, 3, 0, 2, 1, 0, 6, 7, 6, 8, 5, 6, 5, 7, 5, 0, 3, 5, 3, 7, 4, 3, 4, 8, 3, 4, 2, 3, 2, 0, 4, 2, 3, 4, 1, 3, 6, 3, 6, 8, 5, 6, 1, 5, 1, 6, 2, 5, 2, 6, 4, 1, 4, 5, 1, 2, 4, 8)),
                level(16, 6, listOf(bottle(8, 3, 2, 3, 1, 6), bottle(7, 6, 8, 1, 3, 1), bottle(4, 2, 4, 2, 2, 7), bottle(3, 7, 6, 4, 8, 4), bottle(8, 4, 8, 2, 8, 6), bottle(7, 6, 2, 7, 3, 1), bottle(3, 1, 1, 7, 4, 6), emptyList(), emptyList()), moves(0, 7, 0, 8, 1, 8, 0, 1, 4, 7, 5, 8, 6, 7, 3, 6, 3, 4, 6, 3, 2, 6, 0, 2, 1, 0, 1, 8, 4, 1, 5, 0, 5, 6, 2, 5, 3, 2, 3, 7, 6, 3, 6, 8, 0, 6, 1, 0, 1, 7, 3, 1, 3, 6, 4, 3, 4, 0, 2, 4, 2, 3, 4, 2, 5, 3, 5, 7, 0, 4, 1, 5)),
                level(17, 6, listOf(bottle(8, 3, 7, 8, 2, 6), bottle(2, 5, 4, 1, 6, 7), bottle(4, 2, 2, 1, 5, 7), bottle(3, 2, 7, 8, 5, 7), bottle(1, 4, 5, 6, 7, 8), bottle(4, 3, 1, 1, 4, 5), bottle(6, 3, 3, 8, 6, 2), bottle(6, 8, 4, 3, 1, 5), emptyList(), emptyList()), moves(0, 8, 0, 9, 4, 0, 1, 4, 1, 8, 6, 9, 6, 8, 0, 6, 2, 0, 3, 0, 2, 3, 2, 1, 9, 2, 0, 9, 4, 9, 4, 8, 3, 4, 7, 4, 7, 1, 0, 7, 3, 0, 3, 9, 2, 3, 6, 0, 7, 6, 7, 2, 7, 0, 7, 8, 4, 7, 4, 2, 1, 4, 1, 2, 1, 7, 3, 1, 5, 7, 6, 3, 6, 8, 5, 2, 5, 4, 5, 3, 2, 5)),
                level(18, 6, listOf(bottle(4, 6, 0, 2, 2, 2), bottle(8, 7, 3, 8, 8, 3), bottle(3, 7, 7, 8, 6, 0), bottle(2, 3, 0, 7, 0, 6), bottle(4, 5, 5, 6, 2, 6), bottle(5, 5, 0, 7, 5, 8), bottle(3, 7, 4, 8, 3, 6), bottle(4, 4, 2, 0, 5, 4), emptyList(), emptyList()), moves(0, 8, 0, 9, 2, 9, 2, 0, 3, 0, 3, 9, 4, 0, 4, 8, 4, 0, 5, 2, 5, 4, 5, 3, 5, 9, 4, 5, 7, 4, 7, 5, 7, 9, 7, 8, 4, 7, 0, 4, 6, 4, 1, 6, 0, 7, 3, 0, 3, 9, 6, 3, 1, 6, 1, 3, 1, 0, 2, 1, 6, 1, 6, 7, 2, 0, 6, 0, 3, 2, 2, 6, 3, 8)),
                level(19, 6, listOf(bottle(4, 3, 1, 4, 6, 7), bottle(7, 4, 2, 1, 6, 2), bottle(6, 3, 2, 3, 8, 5), bottle(6, 4, 8, 5, 3, 1), bottle(2, 1, 3, 8, 1, 5), bottle(5, 8, 7, 8, 2, 1), bottle(8, 5, 6, 7, 5, 7), bottle(2, 7, 6, 4, 3, 4), emptyList(), emptyList()), moves(0, 8, 0, 9, 6, 8, 4, 6, 3, 4, 7, 0, 3, 7, 6, 3, 6, 8, 6, 9, 2, 6, 3, 6, 2, 3, 7, 2, 0, 7, 4, 0, 3, 4, 5, 0, 1, 5, 1, 9, 7, 3, 7, 9, 8, 7, 6, 8, 4, 6, 2, 4, 5, 2, 5, 6, 5, 7, 5, 6, 5, 8, 7, 5, 2, 7, 4, 2, 4, 1, 4, 7, 2, 4, 2, 9, 0, 2, 0, 4, 1, 2, 1, 7, 1, 0, 3, 0, 1, 5, 3, 9)),
                level(20, 6, listOf(bottle(1, 5, 3, 3, 4, 3), bottle(1, 1, 7, 2, 7, 5), bottle(4, 0, 4, 8, 7, 5), bottle(8, 2, 0, 2, 3, 5), bottle(0, 0, 4, 1, 0, 7), bottle(3, 2, 2, 2, 1, 5), bottle(7, 4, 4, 7, 8, 0), bottle(8, 3, 8, 8, 5, 1), emptyList(), emptyList()), moves(0, 8, 0, 9, 0, 8, 1, 0, 2, 0, 1, 2, 3, 0, 3, 8, 1, 3, 2, 1, 4, 1, 5, 0, 6, 4, 2, 6, 2, 9, 4, 2, 5, 4, 3, 5, 2, 3, 7, 4, 2, 9, 5, 2, 5, 8, 0, 5, 7, 5, 4, 0, 4, 9, 3, 4, 3, 2, 6, 3, 7, 3, 7, 8, 3, 7, 1, 3, 0, 1, 6, 3, 6, 9, 3, 6))
            )
    )

    val bars: List<CatalogBar> = listOf(
        createUnderLevels(),
        createKoyotLevels(),
        createZodiacLevels(),
        createRyadomLevels(),
        createBelyNalyvLevels(),
        createGorkyLevels(),
        createNaDneLevels(),
        createMatreshkaLevels(),
        createZolotoyLevLevels(),
        createMarvinLevels(),
        createDamaskLevels(),
        createAptekaBarLevels(),
        createKianuLevels(),
        createBronLevels(),
        createZoykaLevels()
    )

    private val barsByName: Map<String, CatalogBar> = bars.associateBy { it.barName }

    val totalLevelsCount: Int = bars.sumOf { it.levels.size }

    val allLevels: List<Pair<CatalogBar, LevelLayout>> =
        bars.flatMap { bar -> bar.levels.map { level -> bar to level } }

    fun bar(barName: String): CatalogBar =
        barsByName[barName] ?: error("Unknown bar: $barName")

    fun levelsForBar(barName: String): List<LevelLayout> =
        bar(barName).levels.map { layout ->
            layout.copy(initialBottles = layout.initialBottles.map { it.toList() })
        }

    fun level(barName: String, levelNumber: Int): LevelLayout =
        levelsForBar(barName)[levelNumber - 1]
}
