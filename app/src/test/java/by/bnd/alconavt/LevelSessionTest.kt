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
}
