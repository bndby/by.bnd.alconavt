package by.bnd.alconavt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSessionTest {

    @Test
    fun `начальный снимок прилавка показывает порции снизу вверх, свободные места над жидкостью, полный запас 120 из 120 и счётчик ходов`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )

        val snap = session.snapshot()

        assertEquals(3, snap.capacity)
        assertEquals(2, snap.bottles.size)
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL), snap.bottles[0].portions)
        assertEquals(1, snap.bottles[0].freeSpaces)
        assertFalse(snap.bottles[0].selected)

        assertEquals(emptyList<Drink>(), snap.bottles[1].portions)
        assertEquals(3, snap.bottles[1].freeSpaces)
        assertFalse(snap.bottles[1].selected)

        assertEquals(0, snap.moves)
        assertEquals("ходы", snap.movesLabel)
        assertEquals(120, snap.stamina)
        assertEquals(120, snap.staminaCeiling)
        assertNull(snap.victoryBanner)
    }

    @Test
    fun `первый тап по непустой бутылке выбирает её без сдвига, повторный тап снимает выбор, тап по пустой без выбора ничего не выбирает`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )

        // Тап по пустой бутылке без выбора ничего не выбирает
        session.tapBottle(1)
        val afterEmptyTap = session.snapshot()
        assertFalse(afterEmptyTap.bottles[0].selected)
        assertFalse(afterEmptyTap.bottles[1].selected)
        assertEquals(0, afterEmptyTap.moves)

        // Первый тап по непустой бутылке выбирает её и не сдвигает ни бутылки, ни порции
        session.tapBottle(0)
        val afterSelect = session.snapshot()
        assertTrue(afterSelect.bottles[0].selected)
        assertFalse(afterSelect.bottles[1].selected)
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL), afterSelect.bottles[0].portions)
        assertEquals(emptyList<Drink>(), afterSelect.bottles[1].portions)
        assertEquals(0, afterSelect.moves)

        // Повторный тап по ней снимает выбор
        session.tapBottle(0)
        val afterDeselect = session.snapshot()
        assertFalse(afterDeselect.bottles[0].selected)
        assertFalse(afterDeselect.bottles[1].selected)
        assertEquals(0, afterDeselect.moves)
    }

    @Test
    fun `второй тап по другой бутылке переливает верхнюю непрерывную стопку одного напитка за один ход и сохраняет порядок`() {
        val session = LevelSession(
            capacity = 4,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList(),
                listOf(Drink.GUINNESS_DRAUGHT)
            )
        )

        // Перелив верхней стопки (2 порции Guinness) в пустую бутылку
        session.tapBottle(0)
        session.tapBottle(1)

        val afterFirstPour = session.snapshot()
        assertEquals(listOf(Drink.APEROL), afterFirstPour.bottles[0].portions)
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT), afterFirstPour.bottles[1].portions)
        assertEquals(1, afterFirstPour.moves)
        assertFalse(afterFirstPour.bottles[0].selected)
        assertFalse(afterFirstPour.bottles[1].selected)

        // Перелив на тот же напиток сверху
        session.tapBottle(2)
        session.tapBottle(1)

        val afterSecondPour = session.snapshot()
        assertEquals(emptyList<Drink>(), afterSecondPour.bottles[2].portions)
        assertEquals(
            listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
            afterSecondPour.bottles[1].portions
        )
        assertEquals(2, afterSecondPour.moves)
    }

    @Test
    fun `из трёх верхних порций при двух свободных местах уходят две, одна остаётся в исходной — частичный перелив это один ход`() {
        val session = LevelSession(
            capacity = 6,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )

        session.tapBottle(0)
        session.tapBottle(1)

        val snap = session.snapshot()
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT), snap.bottles[0].portions)
        assertEquals(4, snap.bottles[0].freeSpaces)
        assertEquals(List(6) { Drink.GUINNESS_DRAUGHT }, snap.bottles[1].portions)
        assertEquals(0, snap.bottles[1].freeSpaces)
        assertEquals(1, snap.moves)
        assertFalse(snap.bottles[0].selected)
        assertFalse(snap.bottles[1].selected)
    }

    @Test
    fun `недопустимый второй тап на другой напиток или в полную бутылку не меняет расстановку и счётчик и оставляет выбор на исходной бутылке`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.GUINNESS_DRAUGHT),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )

        // Выбираем бутылку 0 (сверху PILSNER_URQUELL)
        session.tapBottle(0)

        // Второй тап по бутылке 1 (сверху другой напиток GUINNESS_DRAUGHT) — недопустим
        session.tapBottle(1)
        val afterDifferentDrink = session.snapshot()
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL), afterDifferentDrink.bottles[0].portions)
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT), afterDifferentDrink.bottles[1].portions)
        assertEquals(0, afterDifferentDrink.moves)
        assertTrue(afterDifferentDrink.bottles[0].selected)
        assertFalse(afterDifferentDrink.bottles[1].selected)

        // Второй тап по бутылке 2 (тот же напиток, но нет свободных мест) — недопустим
        session.tapBottle(2)
        val afterFullBottle = session.snapshot()
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL), afterFullBottle.bottles[0].portions)
        assertEquals(List(3) { Drink.PILSNER_URQUELL }, afterFullBottle.bottles[2].portions)
        assertEquals(0, afterFullBottle.moves)
        assertTrue(afterFullBottle.bottles[0].selected)
        assertFalse(afterFullBottle.bottles[2].selected)

        // Следующий тап по допустимой пустой бутылке 3 сразу переливает с оставшегося выбора на бутылке 0
        session.tapBottle(3)
        val afterValidTarget = session.snapshot()
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT), afterValidTarget.bottles[0].portions)
        assertEquals(listOf(Drink.PILSNER_URQUELL), afterValidTarget.bottles[3].portions)
        assertEquals(1, afterValidTarget.moves)
        assertFalse(afterValidTarget.bottles[0].selected)
    }

    @Test
    fun `победа наступает когда каждая бутылка пуста или заполнена одним напитком до вместимости и показывает плашку Победа с числом ходов`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )

        // 1: 0 -> 2 (GUINNESS в пустую)
        session.tapBottle(0)
        session.tapBottle(2)
        assertNull(session.snapshot().victoryBanner)

        // 2: 0 -> 3 (PILSNER в пустую)
        session.tapBottle(0)
        session.tapBottle(3)
        assertNull(session.snapshot().victoryBanner)

        // 3: 1 -> 3 (PILSNER на PILSNER; в бутылке 3 теперь 2 x PILSNER, один напиток, но ещё не до вместимости)
        session.tapBottle(1)
        session.tapBottle(3)
        assertNull(session.snapshot().victoryBanner)

        // 4: 1 -> 2 (GUINNESS на GUINNESS)
        session.tapBottle(1)
        session.tapBottle(2)
        assertNull(session.snapshot().victoryBanner)

        // 5: 0 -> 2 (последний GUINNESS собирает бутылку 2 до вместимости 3)
        session.tapBottle(0)
        session.tapBottle(2)
        assertNull(session.snapshot().victoryBanner)

        // 6: 1 -> 3 (последний PILSNER собирает бутылку 3 до вместимости 3)
        session.tapBottle(1)
        session.tapBottle(3)

        val won = session.snapshot()
        assertEquals(6, won.moves)
        assertEquals(120, won.stamina)
        assertEquals(120, won.staminaCeiling)
        assertEquals(emptyList<Drink>(), won.bottles[0].portions)
        assertEquals(emptyList<Drink>(), won.bottles[1].portions)
        assertEquals(List(3) { Drink.GUINNESS_DRAUGHT }, won.bottles[2].portions)
        assertEquals(List(3) { Drink.PILSNER_URQUELL }, won.bottles[3].portions)

        val banner = won.victoryBanner
        assertNotNull(banner)
        assertEquals("Победа", banner!!.title)
        assertEquals(6, banner.moves)

        // После победы тапы по бутылкам больше не выбирают и не меняют прилавок
        session.tapBottle(2)
        val afterTapOnWon = session.snapshot()
        assertFalse(afterTapOnWon.bottles[2].selected)
        assertEquals(6, afterTapOnWon.moves)
    }

    @Test
    fun `расклад рядов прилавка делит 4 бутылки в 1 ряд, 5 и 6 по 3 в 2 ряда, 7-10 по 5 в 2 ряда`() {
        val bottle = listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT)

        val snap4 = LevelSession(capacity = 3, initialBottles = List(2) { bottle } + List(2) { emptyList() }).snapshot()
        assertEquals(4, snap4.columnsPerRow)
        assertEquals(1, snap4.rowCount)
        assertEquals(listOf(4), snap4.rows.map { it.size })

        val snap5 = LevelSession(capacity = 3, initialBottles = List(3) { bottle } + List(2) { emptyList() }).snapshot()
        assertEquals(3, snap5.columnsPerRow)
        assertEquals(2, snap5.rowCount)
        assertEquals(listOf(3, 2), snap5.rows.map { it.size })

        val snap6 = LevelSession(capacity = 3, initialBottles = List(4) { bottle } + List(2) { emptyList() }).snapshot()
        assertEquals(3, snap6.columnsPerRow)
        assertEquals(2, snap6.rowCount)
        assertEquals(listOf(3, 3), snap6.rows.map { it.size })

        val snap7 = LevelSession(capacity = 4, initialBottles = List(5) { bottle + Drink.APEROL } + List(2) { emptyList() }).snapshot()
        assertEquals(5, snap7.columnsPerRow)
        assertEquals(2, snap7.rowCount)
        assertEquals(listOf(5, 2), snap7.rows.map { it.size })

        val snap8 = LevelSession(capacity = 5, initialBottles = List(6) { bottle + listOf(Drink.APEROL, Drink.CAMPARI) } + List(2) { emptyList() }).snapshot()
        assertEquals(5, snap8.columnsPerRow)
        assertEquals(2, snap8.rowCount)
        assertEquals(listOf(5, 3), snap8.rows.map { it.size })

        val snap9 = LevelSession(capacity = 5, initialBottles = List(7) { bottle + listOf(Drink.APEROL, Drink.CAMPARI) } + List(2) { emptyList() }).snapshot()
        assertEquals(5, snap9.columnsPerRow)
        assertEquals(2, snap9.rowCount)
        assertEquals(listOf(5, 4), snap9.rows.map { it.size })

        val snap10 = LevelSession(capacity = 6, initialBottles = List(8) { List(6) { Drink.GUINNESS_DRAUGHT } } + List(2) { emptyList() }).snapshot()
        assertEquals(5, snap10.columnsPerRow)
        assertEquals(2, snap10.rowCount)
        assertEquals(listOf(5, 5), snap10.rows.map { it.size })
    }

    @Test
    fun `стекло вместе с общим горлышком остаётся внутри клетки, оставляет запас при вместимости 3, доходит до края при 6, а при 4 и 5 лежит между ними по ширине и высоте`() {
        val bottle = listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL)

        val snap3 = LevelSession(capacity = 3, initialBottles = List(2) { bottle } + List(2) { emptyList() }).snapshot()
        val snap4 = LevelSession(capacity = 4, initialBottles = List(4) { bottle } + List(2) { emptyList() }).snapshot()
        val snap5 = LevelSession(capacity = 5, initialBottles = List(6) { bottle } + List(2) { emptyList() }).snapshot()
        val snap6 = LevelSession(capacity = 6, initialBottles = List(8) { bottle } + List(2) { emptyList() }).snapshot()

        // Стекло вместе с общим горлышком всегда внутри клетки, форма одна на все бутылки уровня
        for (snap in listOf(snap3, snap4, snap5, snap6)) {
            assertTrue(snap.glass.insideCell)
            assertTrue(snap.glass.sharedNeck)
            assertTrue(snap.glass.widthFraction in 0.5f..1.0f)
            assertTrue(snap.glass.heightFraction in 0.5f..1.0f)
            assertTrue(snap.bottles.all { it.glass == snap.glass })
        }

        // На уровне из 4 бутылок вместимостью 3 вокруг стекла остаётся запас
        assertEquals(GlassFitMode.WITH_CELL_MARGIN, snap3.glass.fitMode)
        assertTrue(snap3.glass.hasCellMargin)
        assertFalse(snap3.glass.reachesCellEdge)
        assertTrue(snap3.glass.widthFraction < 1.0f)
        assertTrue(snap3.glass.heightFraction < 1.0f)

        // На уровне из 10 бутылок вместимостью 6 стекло доходит до края клетки
        assertEquals(GlassFitMode.EDGE_TO_EDGE, snap6.glass.fitMode)
        assertFalse(snap6.glass.hasCellMargin)
        assertTrue(snap6.glass.reachesCellEdge)
        assertEquals(1.0f, snap6.glass.widthFraction, 0.0001f)
        assertEquals(1.0f, snap6.glass.heightFraction, 0.0001f)

        // Вместимости 4 и 5 по высоте и ширине строго лежат между 3 и 6
        assertEquals(GlassFitMode.INTERMEDIATE, snap4.glass.fitMode)
        assertEquals(GlassFitMode.INTERMEDIATE, snap5.glass.fitMode)
        assertTrue(snap3.glass.widthFraction < snap4.glass.widthFraction)
        assertTrue(snap4.glass.widthFraction < snap5.glass.widthFraction)
        assertTrue(snap5.glass.widthFraction < snap6.glass.widthFraction)
        assertTrue(snap3.glass.heightFraction < snap4.glass.heightFraction)
        assertTrue(snap4.glass.heightFraction < snap5.glass.heightFraction)
        assertTrue(snap5.glass.heightFraction < snap6.glass.heightFraction)
    }

    @Test
    fun `на обоих крайних прилавках (4 бутылки по 3 и 10 бутылок по 6) порции различимы, перелив в том числе частичный работает, тап по имени собранной бутылки открывает лист, а тап по стеклу выбирает бутылку`() {
        // 1. Короткий прилавок: 4 бутылки вместимостью 3
        val shortCounter = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL, Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )

        val shortInitial = shortCounter.snapshot()
        assertEquals(1, shortInitial.rowCount)
        assertEquals(4, shortInitial.columnsPerRow)
        assertEquals(listOf(4), shortInitial.rows.map { it.size })
        assertTrue(shortInitial.glass.insideCell)
        assertTrue(shortInitial.glass.hasCellMargin)
        assertEquals(GlassFitMode.WITH_CELL_MARGIN, shortInitial.glass.fitMode)
        // Порции различимы, пока бутылки смешаны — имени собранного напитка нет
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT), shortInitial.bottles[0].portions)
        assertNull(shortInitial.bottles[0].completedDrinkName)
        assertNull(shortInitial.openedDrinkSheet)

        // Частичный перелив на коротком прилавке:
        // Сначала перельём 2 верхних Aperol из бутылки 1 в пустую бутылку 2
        shortCounter.tapBottle(1)
        shortCounter.tapBottle(2)
        // Теперь перельём 1 Guinness из бутылки 1 на 2 Guinness в бутылке 0? В бутылке 0 нет места.
        // Перельём 2 верхних Guinness из бутылки 0 в пустую бутылку 3 (там 2 Guinness, 1 свободно)
        shortCounter.tapBottle(0)
        shortCounter.tapBottle(3)
        // В бутылке 0 остался 1 Aperol; перельём его на 2 Aperol в бутылке 2 -> бутылка 2 собрана (3 Aperol)!
        shortCounter.tapBottle(0)
        shortCounter.tapBottle(2)

        val shortAfterAssemble = shortCounter.snapshot()
        assertEquals(3, shortAfterAssemble.moves)
        assertEquals("Aperol", shortAfterAssemble.bottles[2].completedDrinkName)
        assertNull(shortAfterAssemble.bottles[3].completedDrinkName)

        // Тап по имени собранной бутылки открывает лист напитка
        shortCounter.tapBottleName(2)
        val shortWithSheet = shortCounter.snapshot()
        assertNotNull(shortWithSheet.openedDrinkSheet)
        assertEquals(Drink.APEROL, shortWithSheet.openedDrinkSheet!!.drink)
        assertEquals("Aperol", shortWithSheet.openedDrinkSheet!!.title)
        assertFalse(shortWithSheet.bottles[2].selected)

        // Закрываем лист и проверяем, что тап по стеклу собранной бутылки выбирает её для перелива (и не открывает лист)
        shortCounter.closeDrinkSheet()
        shortCounter.tapBottleGlass(2)
        val shortGlassSelected = shortCounter.snapshot()
        assertNull(shortGlassSelected.openedDrinkSheet)
        assertTrue(shortGlassSelected.bottles[2].selected)

        // 2. Длинный прилавок: 10 бутылок вместимостью 6 (два ряда по пять)
        val longCounter = LevelSession(
            capacity = 6,
            initialBottles = listOf(
                // 0: снизу 3 Campari, сверху 3 Guinness
                listOf(
                    Drink.CAMPARI, Drink.CAMPARI, Drink.CAMPARI,
                    Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT
                ),
                // 1: 4 Guinness и 2 свободных места после выливания 2 Campari в пустую
                listOf(
                    Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT,
                    Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT,
                    Drink.CAMPARI, Drink.CAMPARI
                ),
                listOf(
                    Drink.PILSNER_URQUELL, Drink.APEROL, Drink.PILSNER_URQUELL,
                    Drink.APEROL, Drink.PILSNER_URQUELL, Drink.APEROL
                ),
                listOf(
                    Drink.APEROL, Drink.PILSNER_URQUELL, Drink.APEROL,
                    Drink.PILSNER_URQUELL, Drink.APEROL, Drink.PILSNER_URQUELL
                ),
                listOf(
                    Drink.GIFFARD_CREME_DE_VIOLETTE, Drink.BAILEYS_ORIGINAL_IRISH_CREAM,
                    Drink.GIFFARD_CREME_DE_VIOLETTE, Drink.BAILEYS_ORIGINAL_IRISH_CREAM,
                    Drink.GIFFARD_CREME_DE_VIOLETTE, Drink.BAILEYS_ORIGINAL_IRISH_CREAM
                ),
                listOf(
                    Drink.BAILEYS_ORIGINAL_IRISH_CREAM, Drink.GIFFARD_CREME_DE_VIOLETTE,
                    Drink.BAILEYS_ORIGINAL_IRISH_CREAM, Drink.GIFFARD_CREME_DE_VIOLETTE,
                    Drink.BAILEYS_ORIGINAL_IRISH_CREAM, Drink.GIFFARD_CREME_DE_VIOLETTE
                ),
                listOf(
                    Drink.GIFFARD_LICHI_LI, Drink.BOLS_BLUE_CURACAO,
                    Drink.GIFFARD_LICHI_LI, Drink.BOLS_BLUE_CURACAO,
                    Drink.GIFFARD_LICHI_LI, Drink.BOLS_BLUE_CURACAO
                ),
                listOf(
                    Drink.BOLS_BLUE_CURACAO, Drink.GIFFARD_LICHI_LI,
                    Drink.BOLS_BLUE_CURACAO, Drink.GIFFARD_LICHI_LI,
                    Drink.BOLS_BLUE_CURACAO, Drink.GIFFARD_LICHI_LI
                ),
                emptyList(),
                emptyList()
            )
        )

        val longInitial = longCounter.snapshot()
        assertEquals(2, longInitial.rowCount)
        assertEquals(5, longInitial.columnsPerRow)
        assertEquals(listOf(5, 5), longInitial.rows.map { it.size })
        assertTrue(longInitial.glass.insideCell)
        assertTrue(longInitial.glass.reachesCellEdge)
        assertFalse(longInitial.glass.hasCellMargin)
        assertEquals(GlassFitMode.EDGE_TO_EDGE, longInitial.glass.fitMode)

        // Ход 1 на длинном прилавке: убираем 2 верхних Campari из бутылки 1 в пустую бутылку 8 (во втором ряду)
        longCounter.tapBottle(1)
        longCounter.tapBottle(8)

        // Ход 2 на длинном прилавке: частичный перелив!
        // В бутылке 0 сверху 3 порции Guinness, а в бутылке 1 сейчас 4 порции Guinness и 2 свободных места.
        // Переливаются 2 порции, 1 остаётся в бутылке 0, а бутылка 1 становится собранной (6 Guinness Draught).
        longCounter.tapBottle(0)
        longCounter.tapBottle(1)

        val longAfterPartial = longCounter.snapshot()
        assertEquals(2, longAfterPartial.moves)
        assertEquals(
            listOf(Drink.CAMPARI, Drink.CAMPARI, Drink.CAMPARI, Drink.GUINNESS_DRAUGHT),
            longAfterPartial.bottles[0].portions
        )
        assertEquals(2, longAfterPartial.bottles[0].freeSpaces)
        assertEquals(List(6) { Drink.GUINNESS_DRAUGHT }, longAfterPartial.bottles[1].portions)
        assertEquals(0, longAfterPartial.bottles[1].freeSpaces)
        assertEquals("Guinness Draught", longAfterPartial.bottles[1].completedDrinkName)
        assertEquals(listOf(Drink.CAMPARI, Drink.CAMPARI), longAfterPartial.rows[1][3].portions)

        // Тап по имени собранной бутылки на длинном прилавке открывает лист
        longCounter.tapBottleName(1)
        val longWithSheet = longCounter.snapshot()
        assertNotNull(longWithSheet.openedDrinkSheet)
        assertEquals(Drink.GUINNESS_DRAUGHT, longWithSheet.openedDrinkSheet!!.drink)
        assertEquals("Guinness Draught", longWithSheet.openedDrinkSheet!!.title)
        assertFalse(longWithSheet.bottles[1].selected)

        // Закрываем лист, тап по стеклу собранной бутылки выбирает её (и лист не открывает)
        longCounter.closeDrinkSheet()
        longCounter.tapBottleGlass(1)
        val longGlassSelected = longCounter.snapshot()
        assertNull(longGlassSelected.openedDrinkSheet)
        assertTrue(longGlassSelected.bottles[1].selected)

        // Если отлить из собранной бутылки в пустую бутылку 9, имя пропадает
        longCounter.tapBottleGlass(9)
        val longAfterDisassemble = longCounter.snapshot()
        assertNull(longAfterDisassemble.bottles[1].completedDrinkName)
        assertEquals("Guinness Draught", longAfterDisassemble.bottles[9].completedDrinkName)
    }
}
