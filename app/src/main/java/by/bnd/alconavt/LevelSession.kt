package by.bnd.alconavt

enum class Drink(
    val title: String,
    val strength: String,
    val enduranceCost: Int,
    val colorName: String,
    val colorDescription: String,
    val pixelColorRgb: Int,
    val drinkingCulture: String,
    val history: String,
    val facts: List<String>
) {
    GUINNESS_DRAUGHT(
        title = "Guinness Draught",
        strength = "4,2%",
        enduranceCost = 1,
        colorName = "тёмный рубин",
        colorDescription = "тёмный рубин, на просвет почти чёрный",
        pixelColorRgb = 0x8A2340,
        drinkingCulture = "Чистый сухой бокал и налив в два приёма под 45 градусов; дома из банки — чистый пинтовый бокал. К пинте — пирог со стейком и чеддером.",
        history = "31 декабря 1759 года Артур Гиннесс подписал аренду пивоварни St. James’s Gate на девять тысяч лет; Guinness Draught впервые сварен в 1959 году к двухсотлетию.",
        facts = listOf(
            "В банке виджет с азотом даёт кремовую шапку.",
            "Фильтрацию рыбьим клеем убрали, и напиток назван подходящим для веганов.",
            "В составе указан азот."
        )
    ),
    PILSNER_URQUELL(
        title = "Pilsner Urquell",
        strength = "4,4%",
        enduranceCost = 1,
        colorName = "светло-золотой",
        colorDescription = "светло-золотой",
        pixelColorRgb = 0xF0C84A,
        drinkingCulture = "Длинный узкий бокал на ножке; в танковых пабах — кружка с ручкой. К пиву — чешский гуляш из говядины с хлебными или картофельными кнедликами. Три налива: Hladinka к жирному, Šnyt к гуляшу или бургеру, Mlíko к десерту.",
        history = "5 октября 1842 года Йозеф Гролл сварил первую варку из мягкой пльзеньской воды, жатецкого хмеля и моравского ячменя.",
        facts = listOf(
            "Рецепт и способ варки с 1842 года дом не менял.",
            "Золотая крышка бутылки отмечает ворота старого завода.",
            "У Hladinka три пальца пены, Šnyt — две доли пива и три доли пены, Mlíko — почти один стакан пены."
        )
    ),
    APEROL(
        title = "Aperol",
        strength = "11%",
        enduranceCost = 2,
        colorName = "ярко-оранжевый",
        colorDescription = "ярко-оранжевый",
        pixelColorRgb = 0xFF6A1A,
        drinkingCulture = "Большой винный бокал со льдом и долькой апельсина. К спритцу — чичетти: кростини, польпетте, аранчини, тараллини, трамеццини, торинези, мясная нарезка, брускетта, маринованные оливки и выдержанные сыры.",
        history = "В 1919 году братья Луиджи и Сильвио Барбьери выпустили аперитив после проб с 1912 года; сначала его пили в Падуе и Венеции; рецепт спритца дом относит к 1950-м.",
        facts = listOf(
            "Рецепт дом называет секретным и неизменным.",
            "Бутылки 70 сл и 1 л.",
            "Спритц — 3 доли Prosecco D.O.C., 2 доли Aperol и 1 доля содовой."
        )
    ),
    GIFFARD_CREME_DE_VIOLETTE(
        title = "Giffard Crème de Violette",
        strength = "16%",
        enduranceCost = 2,
        colorName = "фиолетовый",
        colorDescription = "фиолетовый",
        pixelColorRgb = 0x7A3DFF,
        drinkingCulture = "Отдельного бокала нет; аперитив с сухим белым, розовым или игристым и коктейли. Отдельных блюд к ликёру нет; фиалку дом упоминает в кондитерских изделиях.",
        history = "Дом Эмиля Жиффара с жаркого лета 1885 года и мятного Menthe-Pastille, Анже, семейная компания; стиль крем-де-виолетт — XIX век.",
        facts = listOf(
            "Натуральный аромат фиалки, Франция.",
            "Коктейли Aviation и Blue Moon.",
            "Без лактозы, без глютена, подходит веганам, без консерванта."
        )
    ),
    BAILEYS_ORIGINAL_IRISH_CREAM(
        title = "Baileys Original Irish Cream",
        strength = "17%",
        enduranceCost = 2,
        colorName = "кремово-бежевый",
        colorDescription = "кремово-бежевый",
        pixelColorRgb = 0xDFC79C,
        drinkingCulture = "Чистым со льдом, имя бокала не называется. Блюда: полить мороженое, испечь торт, сандей из ванильного мороженого, Baileys и шоколадных крендельков.",
        history = "К 1974 году в рецепте ирландские сливки, ирландский виски, другие спирты, шоколад и ваниль; дом называет напиток первым сливочным ликёром.",
        facts = listOf(
            "Аллергены — молоко и молочные продукты, включая лактозу.",
            "Дом говорит примерно о 200 миллионах литров свежего ирландского молока в год.",
            "Молоко в основном с небольших семейных ферм."
        )
    ),
    GIFFARD_LICHI_LI(
        title = "Giffard Lichi-Li",
        strength = "18%",
        enduranceCost = 2,
        colorName = "светло-розовый",
        colorDescription = "светло-розовый",
        pixelColorRgb = 0xF7A8C4,
        drinkingCulture = "Чистым на колотом льду, в лонг-дринке или коктейле; отдельного бокала нет.",
        history = "Личи родом из Китая и растёт также в Индии, на Мадагаскаре и в Таиланде. 1885 год — основание дома, не дата Lichi-Li.",
        facts = listOf(
            "Аромат личи, Франция; вкус цветочный и слегка ванильный.",
            "Без лактозы, без глютена, для веганов, без консерванта.",
            "Срок настоя плодов от 48 часов до 3 месяцев — общий способ дома, не отдельный срок личи."
        )
    ),
    BOLS_BLUE_CURACAO(
        title = "Bols Blue Curaçao",
        strength = "21%",
        enduranceCost = 2,
        colorName = "ярко-синий",
        colorDescription = "ярко-синий",
        pixelColorRgb = 0x2F6BFF,
        drinkingCulture = "Бокал самого ликёра не назван; для Blue Hawaii — хайбол.",
        history = "Дистиллерия ‘t Lootsje в Амстердаме в 1575 году; в 1920-е восьмигранная бутылка и Blue Curacao; в 1926 году первая запись о Crème de Ciel; в 1933 году первая этикетка Bols Blue.",
        facts = listOf(
            "Бутылка 70 сл, подходит веганам.",
            "Вкус апельсиновой цедры и растительного настоя, в носе апельсин и мандарин.",
            "Blue Hawaii дом датирует 1953 годом и приписывает Гарри Йи."
        )
    ),
    CAMPARI(
        title = "Campari",
        strength = "25%",
        enduranceCost = 2,
        colorName = "прозрачный алый",
        colorDescription = "прозрачный алый",
        pixelColorRgb = 0xE10600,
        drinkingCulture = "Negroni в рокс-тамблере со льдом и долькой апельсина, равные доли Campari, лондонского сухого джина и сладкого красного вермута.",
        history = "В 1860 году Гаспаре Кампари составил жидкость, которую дом с тех пор не менял; в 1867 году открыл Caffè Campari в Галерее Виктора Эммануила II, в тот же год родился Давиде Кампари; в 1915 году Давиде открыл бар Camparino.",
        facts = listOf(
            "Дом говорит, что без последнего ингредиента рецепт развалится.",
            "Negroni входит в список IBA, категория The Unforgettable.",
            "Campari назван красным сердцем и Americano, и Negroni."
        )
    ),
    CHARTREUSE_VERTE(
        title = "Chartreuse Verte",
        strength = "55%",
        enduranceCost = 3,
        colorName = "жёлто-зелёный",
        colorDescription = "жёлто-зелёный",
        pixelColorRgb = 0xC6E04A,
        drinkingCulture = "Дижестив, охлаждённый до 12–13 °C или со льдом; имя бокала не названо. С XIX века ликёр кладут в сладкие и солёные рецепты. Green Chaud — горячий шоколад с ложкой ликёра, взбитыми сливками и какао.",
        history = "В 1840 году из растительного эликсира сделали «ликёр здоровья»; отец Гарнье дал имя Chartreuse Verte и в 1852 году зарегистрировал марку; ликёр делают картезианцы на дистиллерии Эгюнуар в Антр-де-Гье.",
        facts = listOf(
            "130 растений, коры, корней, пряностей и цветов; формулу знают только два человека в монастыре Гранд-Шартрёз.",
            "Травы растирают в монастыре, цвет даёт мацерация, затем выдержка в дубовых чанах.",
            "Бутылки 3, 20, 35, 70 и 300 сл, на горле печать Гранд-Шартрёз."
        )
    )
}

val PIXEL_BOTTLE_MASK: List<String> = listOf(
    "00022000",
    "00021000",
    "00021000",
    "00222200",
    "02111120",
    "02111120",
    "02111120",
    "02111120",
    "02111120",
    "02111120",
    "02111120",
    "00222200"
)

data class BottleIllustrationSnapshot(
    val drink: Drink,
    val colorName: String = drink.colorName,
    val colorDescription: String,
    val fillColorRgb: Int = drink.pixelColorRgb,
    val glowColorRgb: Int = drink.pixelColorRgb,
    val pixelMask: List<String> = PIXEL_BOTTLE_MASK,
    val sharedPixelSilhouette: Boolean = true,
    val hasHouseMark: Boolean = false,
    val filledToNeck: Boolean = true,
    val hasStaticGlow: Boolean = true,
    val staticGlowOfDrinkColor: Boolean = true,
    val subtleRubyEdge: Boolean = drink == Drink.GUINNESS_DRAUGHT,
    val hasFoamHeadLayer: Boolean = false,
    val hasManufacturerPhoto: Boolean = false
)

enum class ScreenId {
    FILM,
    COUNTER,
    DRINK_CARD,
    VICTORY_BANNER,
    DEFEAT
}

enum class IntoxicationState(
    val label: String,
    val backgroundSwayLevel: Int,
    val neonPulseLevel: Int
) {
    SOBER(label = "Трезв", backgroundSwayLevel = 1, neonPulseLevel = 1),
    TIPSY(label = "Навеселе", backgroundSwayLevel = 2, neonPulseLevel = 2),
    DRUNK(label = "Пьян", backgroundSwayLevel = 3, neonPulseLevel = 3),
    WASTED(label = "В хлам", backgroundSwayLevel = 4, neonPulseLevel = 4)
}

fun intoxicationStateFor(stamina: Int, staminaCeiling: Int): IntoxicationState? {
    if (stamina <= 0 || staminaCeiling <= 0) return null
    val lhs = stamina.toLong() * 4L
    val ceil = staminaCeiling.toLong()
    return when {
        lhs >= ceil * 3L -> IntoxicationState.SOBER
        lhs >= ceil * 2L -> IntoxicationState.TIPSY
        lhs >= ceil -> IntoxicationState.DRUNK
        else -> IntoxicationState.WASTED
    }
}

data class DefeatScreenSnapshot(
    val title: String = "Персонаж погиб",
    val newGameButtonLabel: String = "Новая игра",
    val newGameButton: ActionButtonSnapshot = ActionButtonSnapshot(title = newGameButtonLabel, enabled = true),
    val buttons: List<ActionButtonSnapshot> = listOf(newGameButton)
)

data class DrinkCardSnapshot(
    val drink: Drink,
    val illustration: BottleIllustrationSnapshot,
    val name: String,
    val strength: String,
    val colorName: String = drink.colorName,
    val drinkingCulture: String,
    val history: String,
    val facts: List<String>,
    val sectionsTopToBottom: List<String> = listOf(
        "рисунок бутылки",
        "имя",
        "крепость",
        "культура употребления",
        "история",
        "факты"
    ),
    val backLabel: String = "Назад",
    val backButtonLabel: String = backLabel
)

data class DrinkSheetSnapshot(
    val drink: Drink,
    val title: String
)

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
    val completedDrinkName: String? = null,
    val charged: Boolean = false,
    val paid: Boolean = charged,
    val glass: GlassGeometrySnapshot = GlassGeometrySnapshot(
        insideCell = true,
        sharedNeck = true,
        fitMode = GlassFitMode.WITH_CELL_MARGIN,
        widthFraction = 0.78f,
        heightFraction = 0.76f,
        hasCellMargin = true,
        reachesCellEdge = false
    )
)

data class ActionButtonSnapshot(
    val title: String,
    val enabled: Boolean
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
    val currentEndurance: Int = stamina,
    val maxEndurance: Int = staminaCeiling,
    val enduranceText: String = "$stamina/$staminaCeiling",
    val intoxicationState: IntoxicationState? = intoxicationStateFor(stamina, staminaCeiling),
    val stateLabel: String = intoxicationState?.label ?: "",
    val hasSeparateIntoxicationBar: Boolean = false,
    val hasCharacterOnScreen: Boolean = false,
    val bottlesAndPortionsStationary: Boolean = true,
    val backgroundSwayLevel: Int = intoxicationState?.backgroundSwayLevel ?: 0,
    val neonPulseLevel: Int = intoxicationState?.neonPulseLevel ?: 0,
    val recoveryProgressMillis: Long = 0L,
    val recoveryRemainderMillis: Long = recoveryProgressMillis,
    val recoveryRemainderMinutes: Int = (recoveryProgressMillis / 60_000L).toInt(),
    val restartButton: ActionButtonSnapshot?,
    val undoButton: ActionButtonSnapshot?,
    val victoryBanner: VictoryBannerSnapshot?,
    val defeatScreen: DefeatScreenSnapshot? = null,
    val newGameButton: ActionButtonSnapshot? = defeatScreen?.newGameButton,
    val newGameButtonLabel: String? = defeatScreen?.newGameButtonLabel,
    val canStartLevel: Boolean = stamina >= 1,
    val chargedBottleIndices: Set<Int> = emptySet(),
    val screen: ScreenId = ScreenId.COUNTER,
    val drinkCard: DrinkCardSnapshot? = null,
    val openedDrinkSheet: DrinkSheetSnapshot? = null
)

class LevelSession(
    private val capacity: Int,
    initialBottles: List<List<Drink>>,
    initialStamina: Int = 120,
    initialCeiling: Int = 120,
    val barName: String = "Under",
    initialRecoveryProgressMillis: Long = 0L
) {
    private val startBottles: List<List<Drink>> = initialBottles.map { it.toList() }
    private val bottles: List<MutableList<Drink>> = startBottles.map { it.toMutableList() }
    private val history: MutableList<List<List<Drink>>> = mutableListOf()
    private val chargedBottles: MutableSet<Int> = mutableSetOf()
    private var selectedIndex: Int? = null
    private var moves: Int = 0
    private var stamina: Int = initialStamina.coerceAtLeast(0)
    private var staminaCeiling: Int = initialCeiling.coerceAtLeast(1)
    private var recoveryRemainderMillis: Long = initialRecoveryProgressMillis.coerceIn(0L, 299_999L)
    private var lastClockTimeMillis: Long? = null
    private var defeated: Boolean = stamina == 0
    private var won: Boolean = !defeated && isSolved()
    private var openedDrinkCard: Drink? = null
    private var onCounter: Boolean = !defeated

    fun tapBottle(index: Int) {
        tapBottleGlass(index)
    }

    fun tapBottleGlass(index: Int) {
        if (defeated) return
        if (!onCounter) return
        if (openedDrinkCard != null) return
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

    fun tapBottleName(index: Int) {
        if (defeated) return
        if (!onCounter) return
        if (index !in bottles.indices) return
        val completed = completedDrinkOf(bottles[index]) ?: return
        openedDrinkCard = completed
    }

    fun closeDrinkSheet() {
        openedDrinkCard = null
    }

    fun tapBack() {
        if (defeated) return
        if (openedDrinkCard != null) {
            openedDrinkCard = null
        } else if (onCounter) {
            leaveCounter()
        }
    }

    fun systemBack() {
        if (defeated) return
        tapBack()
    }

    fun leaveCounter() {
        if (defeated) return
        openedDrinkCard = null
        selectedIndex = null
        onCounter = false
    }

    fun enterCounter() {
        if (!canStartLevel()) return
        openedDrinkCard = null
        selectedIndex = null
        onCounter = true
    }

    fun canStartLevel(): Boolean = !defeated && stamina >= 1

    fun startLevel(): Boolean {
        if (!canStartLevel()) return false
        openedDrinkCard = null
        selectedIndex = null
        onCounter = true
        return true
    }

    fun advanceTimeMillis(deltaMs: Long) {
        if (defeated || deltaMs <= 0L) return
        if (stamina >= staminaCeiling) return
        val unitDurationMs = 300_000L
        var remainingTime = deltaMs
        while (remainingTime > 0L && stamina < staminaCeiling) {
            val neededForNextUnit = unitDurationMs - recoveryRemainderMillis
            if (remainingTime >= neededForNextUnit) {
                remainingTime -= neededForNextUnit
                recoveryRemainderMillis = 0L
                stamina++
            } else {
                recoveryRemainderMillis += remainingTime
                remainingTime = 0L
            }
        }
    }

    fun onClockTimeMillis(epochMs: Long) {
        val prev = lastClockTimeMillis
        lastClockTimeMillis = epochMs
        if (prev != null && epochMs > prev) {
            advanceTimeMillis(epochMs - prev)
        }
    }

    fun onAppClosed(atClockMillis: Long) {
        onClockTimeMillis(atClockMillis)
    }

    fun onAppReopened(atClockMillis: Long) {
        onClockTimeMillis(atClockMillis)
    }

    fun advanceTimeMinutes(minutes: Int) {
        if (minutes <= 0) return
        advanceTimeMillis(minutes.toLong() * 60_000L)
    }

    fun applyBarBonus(bonusUnits: Int) {
        if (defeated || bonusUnits <= 0) return
        staminaCeiling += bonusUnits
        stamina = (stamina + bonusUnits).coerceAtMost(staminaCeiling)
    }

    fun startNewGame() {
        stamina = 120
        staminaCeiling = 120
        recoveryRemainderMillis = 0L
        lastClockTimeMillis = null
        defeated = false
        startBottles.forEachIndexed { index, portions ->
            bottles[index].clear()
            bottles[index].addAll(portions)
        }
        history.clear()
        chargedBottles.clear()
        moves = 0
        selectedIndex = null
        openedDrinkCard = null
        won = isSolved()
        onCounter = true
    }

    fun undoMove() {
        if (defeated || !onCounter || openedDrinkCard != null) return
        if (won || history.isEmpty()) return
        val previous = history.removeAt(history.lastIndex)
        previous.forEachIndexed { index, portions ->
            bottles[index].clear()
            bottles[index].addAll(portions)
        }
        moves--
        selectedIndex = null
    }

    fun restartRun() {
        if (defeated || !onCounter || openedDrinkCard != null) return
        if (won || history.isEmpty()) return
        startBottles.forEachIndexed { index, portions ->
            bottles[index].clear()
            bottles[index].addAll(portions)
        }
        history.clear()
        moves = 0
        selectedIndex = null
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
        history.add(bottles.map { it.toList() })
        val movedPortions = MutableList(amountToMove) { src.removeAt(src.lastIndex) }.asReversed()
        dst.addAll(movedPortions)
        moves++
        selectedIndex = null
        val completed = completedDrinkOf(dst)
        if (completed != null && chargedBottles.add(toIndex)) {
            stamina = (stamina - completed.enduranceCost).coerceAtLeast(0)
        }
        if (stamina == 0) {
            defeated = true
            won = false
            openedDrinkCard = null
            return
        }
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
                charged = idx in chargedBottles,
                glass = glass
            )
        }
        val columnsPerRow = when {
            bottleSnapshots.size <= 4 -> 4
            bottleSnapshots.size <= 6 -> 3
            else -> 5
        }
        val rows = if (bottleSnapshots.isEmpty()) emptyList() else bottleSnapshots.chunked(columnsPerRow)
        val cardDrink = if (onCounter && !defeated) openedDrinkCard else null
        val canUndoOrRestart = !defeated && !won && history.isNotEmpty()
        val defeatSnap = if (defeated) DefeatScreenSnapshot() else null
        return CounterSnapshot(
            capacity = capacity,
            glass = glass,
            bottles = bottleSnapshots,
            columnsPerRow = columnsPerRow,
            rowCount = rows.size,
            rows = rows,
            moves = moves,
            movesLabel = "ходы",
            stamina = stamina,
            staminaCeiling = staminaCeiling,
            recoveryProgressMillis = recoveryRemainderMillis,
            restartButton = if (defeated || won) null else ActionButtonSnapshot(title = "Сначала", enabled = canUndoOrRestart),
            undoButton = if (defeated) null else ActionButtonSnapshot(title = "Отмена хода", enabled = canUndoOrRestart),
            victoryBanner = if (!defeated && won) VictoryBannerSnapshot(title = "Победа", moves = moves) else null,
            defeatScreen = defeatSnap,
            canStartLevel = !defeated && stamina >= 1,
            chargedBottleIndices = chargedBottles.toSet(),
            screen = when {
                defeated -> ScreenId.DEFEAT
                !onCounter -> ScreenId.FILM
                cardDrink != null -> ScreenId.DRINK_CARD
                won -> ScreenId.VICTORY_BANNER
                else -> ScreenId.COUNTER
            },
            drinkCard = cardDrink?.toCardSnapshot(),
            openedDrinkSheet = cardDrink?.let { DrinkSheetSnapshot(drink = it, title = it.title) }
        )
    }

    private fun Drink.toCardSnapshot(): DrinkCardSnapshot = DrinkCardSnapshot(
        drink = this,
        illustration = BottleIllustrationSnapshot(
            drink = this,
            colorDescription = colorDescription
        ),
        name = title,
        strength = strength,
        drinkingCulture = drinkingCulture,
        history = history,
        facts = facts
    )
}
