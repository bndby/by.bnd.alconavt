package by.bnd.alconavt

enum class Drink(
    val title: String,
    val strength: String,
    val colorDescription: String,
    val drinkingCulture: String,
    val history: String,
    val facts: List<String>
) {
    GUINNESS_DRAUGHT(
        title = "Guinness Draught",
        strength = "4,2%",
        colorDescription = "тёмный рубин, на просвет почти чёрный",
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
        colorDescription = "светло-золотой",
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
        colorDescription = "ярко-оранжевый",
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
        colorDescription = "фиолетовый",
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
        colorDescription = "кремово-бежевый",
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
        colorDescription = "светло-розовый",
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
        colorDescription = "ярко-синий",
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
        colorDescription = "прозрачный алый",
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
        colorDescription = "жёлто-зелёный",
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
    val colorDescription: String,
    val pixelMask: List<String> = PIXEL_BOTTLE_MASK,
    val sharedPixelSilhouette: Boolean = true,
    val hasHouseMark: Boolean = false,
    val filledToNeck: Boolean = true,
    val staticGlowOfDrinkColor: Boolean = true,
    val hasManufacturerPhoto: Boolean = false
)

enum class ScreenId {
    FILM,
    COUNTER,
    DRINK_CARD,
    VICTORY_BANNER
}

data class DrinkCardSnapshot(
    val drink: Drink,
    val illustration: BottleIllustrationSnapshot,
    val name: String,
    val strength: String,
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

data class BottleSnapshot(
    val portions: List<Drink>,
    val freeSpaces: Int,
    val selected: Boolean,
    val completedDrinkName: String? = null
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
    val bottles: List<BottleSnapshot>,
    val moves: Int,
    val movesLabel: String,
    val stamina: Int,
    val staminaCeiling: Int,
    val restartButton: ActionButtonSnapshot?,
    val undoButton: ActionButtonSnapshot,
    val victoryBanner: VictoryBannerSnapshot?,
    val screen: ScreenId = ScreenId.COUNTER,
    val drinkCard: DrinkCardSnapshot? = null
)

class LevelSession(
    private val capacity: Int,
    initialBottles: List<List<Drink>>
) {
    private val startBottles: List<List<Drink>> = initialBottles.map { it.toList() }
    private val bottles: List<MutableList<Drink>> = startBottles.map { it.toMutableList() }
    private val history: MutableList<List<List<Drink>>> = mutableListOf()
    private var selectedIndex: Int? = null
    private var moves: Int = 0
    private var won: Boolean = isSolved()
    private var openedDrinkCard: Drink? = null
    private var onCounter: Boolean = true

    fun tapBottle(index: Int) {
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
        if (!onCounter) return
        if (index !in bottles.indices) return
        val completed = completedDrinkAt(index) ?: return
        openedDrinkCard = completed
    }

    fun tapBack() {
        if (openedDrinkCard != null) {
            openedDrinkCard = null
        } else if (onCounter) {
            leaveCounter()
        }
    }

    fun systemBack() {
        tapBack()
    }

    fun leaveCounter() {
        openedDrinkCard = null
        selectedIndex = null
        onCounter = false
    }

    fun enterCounter() {
        openedDrinkCard = null
        selectedIndex = null
        onCounter = true
    }

    fun undoMove() {
        if (!onCounter || openedDrinkCard != null) return
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
        if (!onCounter || openedDrinkCard != null) return
        if (won || history.isEmpty()) return
        startBottles.forEachIndexed { index, portions ->
            bottles[index].clear()
            bottles[index].addAll(portions)
        }
        history.clear()
        moves = 0
        selectedIndex = null
    }

    private fun completedDrinkAt(index: Int): Drink? {
        val portions = bottles[index]
        return if (portions.size == capacity && portions.isNotEmpty() && portions.all { it == portions[0] }) {
            portions[0]
        } else {
            null
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
        history.add(bottles.map { it.toList() })
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
        val cardDrink = openedDrinkCard
        val canUndoOrRestart = !won && history.isNotEmpty()
        return CounterSnapshot(
            capacity = capacity,
            bottles = bottles.mapIndexed { idx, portions ->
                BottleSnapshot(
                    portions = portions.toList(),
                    freeSpaces = capacity - portions.size,
                    selected = selectedIndex == idx,
                    completedDrinkName = completedDrinkAt(idx)?.title
                )
            },
            moves = moves,
            movesLabel = "ходы",
            stamina = 120,
            staminaCeiling = 120,
            restartButton = if (won) null else ActionButtonSnapshot(title = "Сначала", enabled = canUndoOrRestart),
            undoButton = ActionButtonSnapshot(title = "Отмена хода", enabled = canUndoOrRestart),
            victoryBanner = if (won) VictoryBannerSnapshot(title = "Победа", moves = moves) else null,
            screen = when {
                !onCounter -> ScreenId.FILM
                cardDrink != null -> ScreenId.DRINK_CARD
                won -> ScreenId.VICTORY_BANNER
                else -> ScreenId.COUNTER
            },
            drinkCard = if (onCounter) cardDrink?.toCardSnapshot() else null
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
