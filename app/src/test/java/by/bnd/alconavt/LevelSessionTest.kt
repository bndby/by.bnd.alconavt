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
        assertEquals(ActionButtonSnapshot(title = "Сначала", enabled = false), snap.restartButton)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = false), snap.undoButton)
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
    fun `отмена хода целиком возвращает порции до последнего переливания включая частичный перелив и снимает выбор`() {
        val session = LevelSession(
            capacity = 4,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.CAMPARI, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )

        // Частичный перелив: из 3 порций GUINNESS в бутылку 1 помещаются только 2, а 1 остаётся в бутылке 0.
        // Обратный обычный ход из бутылки 1 в бутылку 0 перелил бы все 3 порции GUINNESS (но в бутылке 0 только 2 свободных места),
        // а в случае перелива в бутылку с другим напитком сверху и вовсе был бы запрещён;
        // отмена хода обязана точно вернуть 2 порции обратно.
        session.tapBottle(0)
        session.tapBottle(1)

        val afterPour = session.snapshot()
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT), afterPour.bottles[0].portions)
        assertEquals(
            listOf(Drink.CAMPARI, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
            afterPour.bottles[1].portions
        )
        assertEquals(1, afterPour.moves)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = true), afterPour.undoButton)
        assertEquals(ActionButtonSnapshot(title = "Сначала", enabled = true), afterPour.restartButton)

        // Выбираем бутылку перед отменой — отмена должна снять выбор
        session.tapBottle(0)
        assertTrue(session.snapshot().bottles[0].selected)

        session.undoMove()

        val afterUndo = session.snapshot()
        assertEquals(
            listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
            afterUndo.bottles[0].portions
        )
        assertEquals(
            listOf(Drink.CAMPARI, Drink.GUINNESS_DRAUGHT),
            afterUndo.bottles[1].portions
        )
        assertEquals(0, afterUndo.moves)
        assertFalse(afterUndo.bottles[0].selected)
        assertFalse(afterUndo.bottles[1].selected)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = false), afterUndo.undoButton)
        assertEquals(ActionButtonSnapshot(title = "Сначала", enabled = false), afterUndo.restartButton)
    }

    @Test
    fun `отмена возвращает порции даже когда обратный перенос обычным ходом недопустим и доходит последовательными отменами до старта`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )

        // Ход 1: GUINNESS -> 2 (в бутылке 0 сверху остался APEROL, поэтому обычным ходом вернуть GUINNESS в 0 нельзя)
        session.tapBottle(0)
        session.tapBottle(2)
        // Ход 2: PILSNER -> 3 (в бутылке 1 сверху остался CAMPARI)
        session.tapBottle(1)
        session.tapBottle(3)

        // Выбор бутылки и недопустимый перелив не входят в историю отмены
        session.tapBottle(2) // выбираем 2 (GUINNESS)
        session.tapBottle(0) // недопустимый перелив на APEROL
        assertEquals(2, session.snapshot().moves)
        assertTrue(session.snapshot().bottles[2].selected)

        // Первая отмена возвращает ход 2 (PILSNER обратно на CAMPARI в бутылку 1) и снимает выбор с 2
        session.undoMove()
        val afterFirstUndo = session.snapshot()
        assertEquals(1, afterFirstUndo.moves)
        assertFalse(afterFirstUndo.bottles[2].selected)
        assertEquals(listOf(Drink.APEROL), afterFirstUndo.bottles[0].portions)
        assertEquals(listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL), afterFirstUndo.bottles[1].portions)
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT), afterFirstUndo.bottles[2].portions)
        assertEquals(emptyList<Drink>(), afterFirstUndo.bottles[3].portions)
        assertTrue(afterFirstUndo.undoButton.enabled)
        assertTrue(afterFirstUndo.restartButton!!.enabled)

        // Вторая отмена возвращает ход 1 (GUINNESS обратно на APEROL в бутылку 0) — стартовая расстановка
        session.undoMove()
        val atStart = session.snapshot()
        assertEquals(0, atStart.moves)
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT), atStart.bottles[0].portions)
        assertEquals(listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL), atStart.bottles[1].portions)
        assertEquals(emptyList<Drink>(), atStart.bottles[2].portions)
        assertEquals(emptyList<Drink>(), atStart.bottles[3].portions)
        assertFalse(atStart.undoButton.enabled)
        assertFalse(atStart.restartButton!!.enabled)

        // Повторный вызов undoMove при пустой истории ничего не меняет и не уводит счётчик в минус
        session.tapBottle(0) // выбрали бутылку 0 на старте
        session.undoMove()
        val afterNoopUndo = session.snapshot()
        assertEquals(0, afterNoopUndo.moves)
        assertTrue(afterNoopUndo.bottles[0].selected)
    }

    @Test
    fun `кнопка Сначала одним шагом возвращает стартовую расстановку, обнуляет счётчик, очищает историю и снимает выбор`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )

        // Пока история пуста, нажатие Сначала ничего не меняет (и выбор не сбрасывает, так как кнопка не нажимается)
        session.tapBottle(0)
        session.restartRun()
        assertTrue(session.snapshot().bottles[0].selected)
        session.tapBottle(0) // снимаем выбор

        // Делаем два хода и выбираем бутылку
        session.tapBottle(0)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(2)
        assertEquals(2, session.snapshot().moves)
        assertTrue(session.snapshot().bottles[2].selected)

        // Нажимаем «Сначала»
        session.restartRun()
        val afterRestart = session.snapshot()
        assertEquals(0, afterRestart.moves)
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT), afterRestart.bottles[0].portions)
        assertEquals(listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL), afterRestart.bottles[1].portions)
        assertEquals(emptyList<Drink>(), afterRestart.bottles[2].portions)
        assertEquals(emptyList<Drink>(), afterRestart.bottles[3].portions)
        assertTrue(afterRestart.bottles.none { it.selected })
        assertEquals(ActionButtonSnapshot(title = "Сначала", enabled = false), afterRestart.restartButton)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = false), afterRestart.undoButton)

        // Брошенные переливания вернуть отменой нельзя
        session.undoMove()
        val afterUndoAttempt = session.snapshot()
        assertEquals(0, afterUndoAttempt.moves)
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT), afterUndoAttempt.bottles[0].portions)
        assertEquals(emptyList<Drink>(), afterUndoAttempt.bottles[2].portions)
    }

    @Test
    fun `после победы кнопка Сначала отсутствует на прилавке а Отмена хода тусклая и не откатывает победу`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )

        session.tapBottle(1)
        session.tapBottle(0)

        val won = session.snapshot()
        assertNotNull(won.victoryBanner)
        assertEquals(1, won.moves)
        assertNull(won.restartButton)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = false), won.undoButton)

        session.undoMove()
        session.restartRun()
        val afterAttempts = session.snapshot()
        assertNotNull(afterAttempts.victoryBanner)
        assertEquals(1, afterAttempts.moves)
        assertEquals(List(3) { Drink.GUINNESS_DRAUGHT }, afterAttempts.bottles[0].portions)
        assertEquals(emptyList<Drink>(), afterAttempts.bottles[1].portions)
    }

    @Test
    fun `тупик без допустимых переливаний не завершает заход и позволяет выйти через отмену хода или Сначала`() {
        val strictDeadEnd = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL, Drink.BOLS_BLUE_CURACAO),
                listOf(Drink.APEROL, Drink.CAMPARI, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.PILSNER_URQUELL, Drink.APEROL, Drink.CAMPARI),
                emptyList()
            )
        )

        // Ход: из 2 (верх CAMPARI) в пустую 3
        strictDeadEnd.tapBottle(2)
        strictDeadEnd.tapBottle(3)

        // Теперь бутылки:
        // 0: [GUINNESS, PILSNER, BOLS] (3/3, верх BOLS)
        // 1: [APEROL, CAMPARI, GUINNESS] (3/3, верх GUINNESS)
        // 2: [PILSNER, APEROL] (2/3, верх APEROL)
        // 3: [CAMPARI] (1/3, верх CAMPARI)
        // Все 4 верхушки разные (BOLS, GUINNESS, APEROL, CAMPARI) и пустых бутылок нет — ни одного допустимого хода нет!
        val inDeadEnd = strictDeadEnd.snapshot()
        assertNull(inDeadEnd.victoryBanner)
        assertEquals(1, inDeadEnd.moves)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = true), inDeadEnd.undoButton)
        assertEquals(ActionButtonSnapshot(title = "Сначала", enabled = true), inDeadEnd.restartButton)

        // Выход из тупика через «Отмена хода»
        strictDeadEnd.undoMove()
        val afterUndoFromDeadEnd = strictDeadEnd.snapshot()
        assertEquals(0, afterUndoFromDeadEnd.moves)
        assertEquals(emptyList<Drink>(), afterUndoFromDeadEnd.bottles[3].portions)
        assertEquals(
            listOf(Drink.PILSNER_URQUELL, Drink.APEROL, Drink.CAMPARI),
            afterUndoFromDeadEnd.bottles[2].portions
        )

        // Снова заходим в тот же тупик и выходим через «Сначала»
        strictDeadEnd.tapBottle(2)
        strictDeadEnd.tapBottle(3)
        assertEquals(1, strictDeadEnd.snapshot().moves)

        strictDeadEnd.restartRun()
        val afterRestartFromDeadEnd = strictDeadEnd.snapshot()
        assertEquals(0, afterRestartFromDeadEnd.moves)
        assertEquals(emptyList<Drink>(), afterRestartFromDeadEnd.bottles[3].portions)
        assertFalse(afterRestartFromDeadEnd.undoButton.enabled)
        assertFalse(afterRestartFromDeadEnd.restartButton!!.enabled)
    }
}
