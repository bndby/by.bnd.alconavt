package by.bnd.alconavt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
        assertEquals(118, won.stamina)
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
    fun `имя напитка видно только под бутылкой заполненной им до вместимости и пропадает когда бутылку разбирают`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )

        // В начале ни одна бутылка не собрана до вместимости одним напитком
        val initial = session.snapshot()
        assertNull(initial.bottles[0].completedDrinkName)
        assertNull(initial.bottles[1].completedDrinkName)
        assertNull(initial.bottles[2].completedDrinkName)
        assertNull(initial.bottles[3].completedDrinkName)

        // Освобождаем верхний APEROL из бутылки 1 в пустую бутылку 2
        session.tapBottle(1)
        session.tapBottle(2)
        assertNull(session.snapshot().bottles[0].completedDrinkName)
        assertNull(session.snapshot().bottles[2].completedDrinkName)

        // Доливаем третий GUINNESS из бутылки 1 в бутылку 0 — теперь она собрана до вместимости 3
        session.tapBottle(1)
        session.tapBottle(0)
        val afterCompleted = session.snapshot()
        assertEquals("Guinness Draught", afterCompleted.bottles[0].completedDrinkName)
        assertNull(afterCompleted.bottles[1].completedDrinkName)
        assertNull(afterCompleted.bottles[2].completedDrinkName)
        assertNull(afterCompleted.bottles[3].completedDrinkName)

        // Переливаем всю собранную бутылку 0 в пустую бутылку 3: бутылка 0 теряет имя, бутылка 3 получает имя
        session.tapBottle(0)
        session.tapBottle(3)
        val afterPourToEmpty = session.snapshot()
        assertNull(afterPourToEmpty.bottles[0].completedDrinkName)
        assertEquals("Guinness Draught", afterPourToEmpty.bottles[3].completedDrinkName)
    }

    @Test
    fun `тап по имени собранной бутылки открывает лист напитка, а тап по стеклу той же бутылки выбирает её для перелива и лист не открывает`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.APEROL, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )

        // Начальный экран — прилавок, лист закрыт
        val initial = session.snapshot()
        assertEquals(ScreenId.COUNTER, initial.screen)
        assertNull(initial.drinkCard)

        // Тап по стеклу собранной бутылки 0 выбирает её для перелива и лист не открывает
        session.tapBottle(0)
        val afterGlassTap = session.snapshot()
        assertEquals(ScreenId.COUNTER, afterGlassTap.screen)
        assertTrue(afterGlassTap.bottles[0].selected)
        assertNull(afterGlassTap.drinkCard)

        // Повторный тап по стеклу снимает выбор
        session.tapBottle(0)
        assertFalse(session.snapshot().bottles[0].selected)

        // Тап по имени несобранной или пустой бутылки лист не открывает
        session.tapBottleName(1)
        assertNull(session.snapshot().drinkCard)
        session.tapBottleName(2)
        assertNull(session.snapshot().drinkCard)

        // Тап по имени собранной бутылки 0 открывает лист этого напитка
        session.tapBottleName(0)
        val onSheet = session.snapshot()
        assertEquals(ScreenId.DRINK_CARD, onSheet.screen)
        val card = onSheet.drinkCard
        assertNotNull(card)
        assertEquals(Drink.GUINNESS_DRAUGHT, card!!.drink)
        assertEquals("Guinness Draught", card.name)
    }

    @Test
    fun `лист напитка читается сверху вниз — рисунок бутылки без знака дома залитый до горлышка со статичным свечением, имя, крепость с запятой, культура одним блоком, история и факты без кадра производителя`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                List(3) { Drink.GUINNESS_DRAUGHT },
                List(3) { Drink.APEROL },
                emptyList(),
                emptyList()
            )
        )

        session.tapBottleName(0)
        val guinnessCard = session.snapshot().drinkCard!!

        assertEquals(
            listOf("рисунок бутылки", "имя", "крепость", "культура употребления", "история", "факты"),
            guinnessCard.sectionsTopToBottom
        )
        assertTrue(guinnessCard.illustration.sharedPixelSilhouette)
        assertFalse(guinnessCard.illustration.hasHouseMark)
        assertTrue(guinnessCard.illustration.filledToNeck)
        assertTrue(guinnessCard.illustration.staticGlowOfDrinkColor)
        assertFalse(guinnessCard.illustration.hasManufacturerPhoto)
        assertEquals("тёмный рубин, на просвет почти чёрный", guinnessCard.illustration.colorDescription)

        assertEquals("Guinness Draught", guinnessCard.name)
        assertEquals("4,2%", guinnessCard.strength)
        assertEquals(
            "Чистый сухой бокал и налив в два приёма под 45 градусов; дома из банки — чистый пинтовый бокал. К пинте — пирог со стейком и чеддером.",
            guinnessCard.drinkingCulture
        )
        assertEquals(
            "31 декабря 1759 года Артур Гиннесс подписал аренду пивоварни St. James’s Gate на девять тысяч лет; Guinness Draught впервые сварен в 1959 году к двухсотлетию.",
            guinnessCard.history
        )
        assertEquals(
            listOf(
                "В банке виджет с азотом даёт кремовую шапку.",
                "Фильтрацию рыбьим клеем убрали, и напиток назван подходящим для веганов.",
                "В составе указан азот."
            ),
            guinnessCard.facts
        )
        assertEquals("Назад", guinnessCard.backLabel)
        assertEquals("Назад", guinnessCard.backButtonLabel)

        // Другой напиток использует тот же силуэт рисунка бутылки, но залитый своим цветом, и процент крепости из пула
        session.tapBottleName(1)
        val aperolCard = session.snapshot().drinkCard!!
        assertEquals(guinnessCard.illustration.pixelMask, aperolCard.illustration.pixelMask)
        assertEquals("ярко-оранжевый", aperolCard.illustration.colorDescription)
        assertEquals("Aperol", aperolCard.name)
        assertEquals("11%", aperolCard.strength)
        assertEquals(
            "Большой винный бокал со льдом и долькой апельсина. К спритцу — чичетти: кростини, польпетте, аранчини, тараллини, трамеццини, торинези, мясная нарезка, брускетта, маринованные оливки и выдержанные сыры.",
            aperolCard.drinkingCulture
        )
        assertEquals(
            "В 1919 году братья Луиджи и Сильвио Барбьери выпустили аперитив после проб с 1912 года; сначала его пили в Падуе и Венеции; рецепт спритца дом относит к 1950-м.",
            aperolCard.history
        )
        assertEquals(
            listOf(
                "Рецепт дом называет секретным и неизменным.",
                "Бутылки 70 сл и 1 л.",
                "Спритц — 3 доли Prosecco D.O.C., 2 доли Aperol и 1 доля содовой."
            ),
            aperolCard.facts
        )
    }

    @Test
    fun `Назад и системная Назад закрывают лист и возвращают на тот же прилавок без изменения расстановки, счётчика, выбора бутылки и истории отмены`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                listOf(Drink.PILSNER_URQUELL),
                emptyList()
            )
        )

        // На старте пока история пуста: если бутылка уже собрана, открытие и закрытие карточки не добавляют шаг в историю отмены
        val startCompletedSession = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                List(3) { Drink.GUINNESS_DRAUGHT },
                listOf(Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )
        assertFalse(startCompletedSession.snapshot().undoButton!!.enabled)
        startCompletedSession.tapBottleName(0)
        startCompletedSession.tapBack()
        assertFalse(startCompletedSession.snapshot().undoButton!!.enabled)
        assertEquals(0, startCompletedSession.snapshot().moves)

        // Собираем бутылку 0 за два хода и выбираем бутылку 2
        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(0)
        session.tapBottle(2)

        val beforeOpen = session.snapshot()
        assertEquals(ScreenId.COUNTER, beforeOpen.screen)
        assertEquals(2, beforeOpen.moves)
        assertTrue(beforeOpen.bottles[2].selected)
        assertEquals("Guinness Draught", beforeOpen.bottles[0].completedDrinkName)

        // Открываем лист и возвращаемся экранной кнопкой «Назад»
        session.tapBottleName(0)
        assertEquals(ScreenId.DRINK_CARD, session.snapshot().screen)
        assertNotNull(session.snapshot().drinkCard)

        session.tapBack()
        val afterScreenBack = session.snapshot()
        assertEquals(ScreenId.COUNTER, afterScreenBack.screen)
        assertNull(afterScreenBack.drinkCard)
        assertEquals(beforeOpen.bottles, afterScreenBack.bottles)
        assertEquals(2, afterScreenBack.moves)
        assertTrue(afterScreenBack.bottles[2].selected)

        // Снова открываем лист и возвращаемся системной «Назад»
        session.tapBottleName(0)
        assertEquals(ScreenId.DRINK_CARD, session.snapshot().screen)

        session.systemBack()
        val afterSystemBack = session.snapshot()
        assertEquals(ScreenId.COUNTER, afterSystemBack.screen)
        assertNull(afterSystemBack.drinkCard)
        assertEquals(beforeOpen.bottles, afterSystemBack.bottles)
        assertEquals(2, afterSystemBack.moves)
        assertTrue(afterSystemBack.bottles[2].selected)

        // Открытие и закрытие карточки не добавили шаг в историю отмены:
        // одна отмена сразу откатывает второй ход (сборку бутылки 0)
        session.undoMove()
        val afterUndo = session.snapshot()
        assertEquals(1, afterUndo.moves)
        assertNull(afterUndo.bottles[0].completedDrinkName)
    }

    @Test
    fun `на плашке победы имя собранной бутылки открывает лист и Назад возвращает на ту же плашку победы`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            )
        )

        session.tapBottle(1)
        session.tapBottle(0)

        val won = session.snapshot()
        assertEquals(ScreenId.VICTORY_BANNER, won.screen)
        assertNotNull(won.victoryBanner)
        assertEquals("Победа", won.victoryBanner!!.title)
        assertEquals(1, won.victoryBanner!!.moves)
        assertEquals("Guinness Draught", won.bottles[0].completedDrinkName)

        // Тап по имени собранной бутылки на плашке победы открывает лист
        session.tapBottleName(0)
        val onSheet = session.snapshot()
        assertEquals(ScreenId.DRINK_CARD, onSheet.screen)
        assertNotNull(onSheet.drinkCard)
        assertEquals("Guinness Draught", onSheet.drinkCard!!.name)

        // «Назад» возвращает на ту же плашку победы на прилавке
        session.tapBack()
        val afterBack = session.snapshot()
        assertEquals(ScreenId.VICTORY_BANNER, afterBack.screen)
        assertNull(afterBack.drinkCard)
        assertNotNull(afterBack.victoryBanner)
        assertEquals("Победа", afterBack.victoryBanner!!.title)
        assertEquals(1, afterBack.victoryBanner!!.moves)
        assertEquals(won.bottles, afterBack.bottles)

        // Системная «Назад» с листа тоже возвращает на ту же плашку победы
        session.tapBottleName(0)
        assertEquals(ScreenId.DRINK_CARD, session.snapshot().screen)
        session.systemBack()
        val afterSystemBack = session.snapshot()
        assertEquals(ScreenId.VICTORY_BANNER, afterSystemBack.screen)
        assertNull(afterSystemBack.drinkCard)
        assertNotNull(afterSystemBack.victoryBanner)
        assertEquals(1, afterSystemBack.victoryBanner!!.moves)
    }

    @Test
    fun `после ухода с прилавка лист недоступен, память открытых карточек не ведётся, а снова открыть лист можно только новой сборкой этого напитка до вместимости`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )

        // Открываем лист с собранной бутылки 0
        session.tapBottleName(0)
        assertEquals(ScreenId.DRINK_CARD, session.snapshot().screen)
        assertNotNull(session.snapshot().drinkCard)

        // Уходим с прилавка (или закрываем лист и уходим с прилавка): лист исчезает, вне прилавка недоступен
        session.leaveCounter()
        val afterLeave = session.snapshot()
        assertNotEquals(ScreenId.DRINK_CARD, afterLeave.screen)
        assertNull(afterLeave.drinkCard)
        session.tapBottleName(0)
        assertNull(session.snapshot().drinkCard)

        // Возвращаемся на прилавок: лист закрыт, выбор сброшен
        session.enterCounter()
        val afterReenter = session.snapshot()
        assertEquals(ScreenId.COUNTER, afterReenter.screen)
        assertNull(afterReenter.drinkCard)

        // Частичное выливание из собранной бутылки 0 в неполную бутылку 1 разбирает бутылку 0
        val uncompleteSession = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            )
        )
        uncompleteSession.tapBottleName(0)
        assertNotNull(uncompleteSession.snapshot().drinkCard)
        uncompleteSession.tapBack()

        // Частичный перелив из полной бутылки 0 в бутылку 1 разбирает бутылку 0 и не делает бутылку 1 однородной
        uncompleteSession.tapBottle(0)
        uncompleteSession.tapBottle(1)

        val broken = uncompleteSession.snapshot()
        assertNull(broken.bottles[0].completedDrinkName)
        assertNull(broken.bottles[1].completedDrinkName)
        assertNull(broken.drinkCard)

        // Памяти уже открытых карточек нет: ни с бутылки 0, ни с бутылки 1 лист больше не открывается
        uncompleteSession.tapBottleName(0)
        assertNull(uncompleteSession.snapshot().drinkCard)
        uncompleteSession.tapBottleName(1)
        assertNull(uncompleteSession.snapshot().drinkCard)

        // Снова собираем Guinness Draught обратно в бутылку 0 до вместимости — имя и лист снова доступны
        uncompleteSession.tapBottle(1)
        uncompleteSession.tapBottle(0)
        val rebuilt = uncompleteSession.snapshot()
        assertEquals("Guinness Draught", rebuilt.bottles[0].completedDrinkName)
        uncompleteSession.tapBottleName(0)
        assertEquals(ScreenId.DRINK_CARD, uncompleteSession.snapshot().screen)
        assertEquals("Guinness Draught", uncompleteSession.snapshot().drinkCard?.name)
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
        assertTrue(afterFirstUndo.undoButton!!.enabled)
        assertTrue(afterFirstUndo.restartButton!!.enabled)

        // Вторая отмена возвращает ход 1 (GUINNESS обратно на APEROL в бутылку 0) — стартовая расстановка
        session.undoMove()
        val atStart = session.snapshot()
        assertEquals(0, atStart.moves)
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT), atStart.bottles[0].portions)
        assertEquals(listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL), atStart.bottles[1].portions)
        assertEquals(emptyList<Drink>(), atStart.bottles[2].portions)
        assertEquals(emptyList<Drink>(), atStart.bottles[3].portions)
        assertFalse(atStart.undoButton!!.enabled)
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
        assertFalse(afterRestartFromDeadEnd.undoButton!!.enabled)
        assertFalse(afterRestartFromDeadEnd.restartButton!!.enabled)
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
        assertEquals(listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT), shortInitial.bottles[0].portions)
        assertNull(shortInitial.bottles[0].completedDrinkName)
        assertNull(shortInitial.openedDrinkSheet)
        assertNull(shortInitial.drinkCard)

        shortCounter.tapBottle(1)
        shortCounter.tapBottle(2)
        shortCounter.tapBottle(0)
        shortCounter.tapBottle(3)
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
        assertNotNull(shortWithSheet.drinkCard)
        assertEquals(Drink.APEROL, shortWithSheet.openedDrinkSheet!!.drink)
        assertEquals("Aperol", shortWithSheet.openedDrinkSheet!!.title)
        assertEquals("Aperol", shortWithSheet.drinkCard!!.name)
        assertFalse(shortWithSheet.bottles[2].selected)

        // Закрываем лист и проверяем, что тап по стеклу собранной бутылки выбирает её для перелива (и не открывает лист)
        shortCounter.closeDrinkSheet()
        shortCounter.tapBottleGlass(2)
        val shortGlassSelected = shortCounter.snapshot()
        assertNull(shortGlassSelected.openedDrinkSheet)
        assertNull(shortGlassSelected.drinkCard)
        assertTrue(shortGlassSelected.bottles[2].selected)

        // 2. Длинный прилавок: 10 бутылок вместимостью 6 (два ряда по пять)
        val longCounter = LevelSession(
            capacity = 6,
            initialBottles = listOf(
                listOf(
                    Drink.CAMPARI, Drink.CAMPARI, Drink.CAMPARI,
                    Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT
                ),
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

        longCounter.tapBottle(1)
        longCounter.tapBottle(8)

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

        longCounter.tapBottleName(1)
        val longWithSheet = longCounter.snapshot()
        assertNotNull(longWithSheet.openedDrinkSheet)
        assertNotNull(longWithSheet.drinkCard)
        assertEquals(Drink.GUINNESS_DRAUGHT, longWithSheet.openedDrinkSheet!!.drink)
        assertEquals("Guinness Draught", longWithSheet.openedDrinkSheet!!.title)
        assertEquals("Guinness Draught", longWithSheet.drinkCard!!.name)
        assertFalse(longWithSheet.bottles[1].selected)

        longCounter.closeDrinkSheet()
        longCounter.tapBottleGlass(1)
        val longGlassSelected = longCounter.snapshot()
        assertNull(longGlassSelected.openedDrinkSheet)
        assertNull(longGlassSelected.drinkCard)
        assertTrue(longGlassSelected.bottles[1].selected)

        longCounter.tapBottleGlass(9)
        val longAfterDisassemble = longCounter.snapshot()
        assertNull(longAfterDisassemble.bottles[1].completedDrinkName)
        assertEquals("Guinness Draught", longAfterDisassemble.bottles[9].completedDrinkName)
    }

    @Test
    fun `первая сборка бутылки в заходе списывает выносливость по цене напитка, простое переливание ничего не списывает, вместимость и бар цену не меняют`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )

        val initial = session.snapshot()
        assertEquals(120, initial.stamina)
        assertEquals(120, initial.staminaCeiling)
        assertEquals(120, initial.currentEndurance)
        assertEquals(120, initial.maxEndurance)
        assertEquals("120/120", initial.enduranceText)

        // Простое переливание без сборки бутылки до вместимости ничего не списывает
        session.tapBottle(0)
        session.tapBottle(2)
        val afterSimplePour = session.snapshot()
        assertEquals(120, afterSimplePour.stamina)
        assertEquals(120, afterSimplePour.currentEndurance)
        assertEquals("120/120", afterSimplePour.enduranceText)

        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(120, session.snapshot().currentEndurance)

        // Первая сборка Guinness Draught (цена 1) до вместимости 3 списывает 1
        session.tapBottle(1)
        session.tapBottle(0)
        val afterFirstCompleted = session.snapshot()
        assertEquals("Guinness Draught", afterFirstCompleted.bottles[0].completedDrinkName)
        assertEquals(119, afterFirstCompleted.stamina)
        assertEquals(119, afterFirstCompleted.currentEndurance)
        assertEquals(120, afterFirstCompleted.maxEndurance)
        assertEquals("119/120", afterFirstCompleted.enduranceText)

        // Цены всех 9 напитков: 1 (Guinness, Pilsner), 2 (Aperol, Violette, Baileys, Lichi-Li, Blue Curacao, Campari), 3 (Chartreuse)
        val expectedPrices = mapOf(
            Drink.GUINNESS_DRAUGHT to 1,
            Drink.PILSNER_URQUELL to 1,
            Drink.APEROL to 2,
            Drink.GIFFARD_CREME_DE_VIOLETTE to 2,
            Drink.BAILEYS_ORIGINAL_IRISH_CREAM to 2,
            Drink.GIFFARD_LICHI_LI to 2,
            Drink.BOLS_BLUE_CURACAO to 2,
            Drink.CAMPARI to 2,
            Drink.CHARTREUSE_VERTE to 3
        )
        for ((drink, expectedCost) in expectedPrices) {
            for (cap in listOf(3, 6)) {
                val s = LevelSession(
                    capacity = cap,
                    initialBottles = listOf(
                        List(cap - 1) { drink },
                        listOf(drink, Drink.PILSNER_URQUELL.takeIf { it != drink } ?: Drink.GUINNESS_DRAUGHT),
                        emptyList(),
                        emptyList()
                    )
                )
                s.tapBottle(1)
                s.tapBottle(2)
                assertEquals(120, s.snapshot().currentEndurance)
                s.tapBottle(1)
                s.tapBottle(0)
                assertEquals(120 - expectedCost, s.snapshot().currentEndurance)
            }
        }
    }

    @Test
    fun `повторная сборка той же бутылки в том же заходе ничего не списывает, две собранные бутылки одного напитка списывают дважды, а отмена хода и Сначала запас не возвращают и отметку списания не снимают`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )

        // Освобождаем верхний APEROL из бутылки 1 в 2 (без сборки — 120)
        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(120, session.snapshot().currentEndurance)
        assertFalse(session.snapshot().bottles[0].charged)

        // Собираем бутылку 0 напитком Chartreuse Verte (цена 3): 120 -> 117, бутылка 0 помечена
        session.tapBottle(1)
        session.tapBottle(0)
        val afterFirstAssemble = session.snapshot()
        assertEquals(117, afterFirstAssemble.currentEndurance)
        assertTrue(afterFirstAssemble.bottles[0].charged)
        assertFalse(afterFirstAssemble.bottles[1].charged)
        assertFalse(afterFirstAssemble.bottles[3].charged)

        // Переливаем собранную бутылку 0 целиком в пустую бутылку 3: вторая бутылка того же напитка списывает ещё 3 (117 -> 114)
        session.tapBottle(0)
        session.tapBottle(3)
        val afterSecondBottleAssemble = session.snapshot()
        assertEquals(114, afterSecondBottleAssemble.currentEndurance)
        assertTrue(afterSecondBottleAssemble.bottles[0].charged)
        assertTrue(afterSecondBottleAssemble.bottles[3].charged)

        // Возвращаем обратно из бутылки 3 в уже отмеченную бутылку 0: повторная сборка той же бутылки в том же заходе ничего не списывает (остаётся 114)
        session.tapBottle(3)
        session.tapBottle(0)
        val afterReassembleSameBottle = session.snapshot()
        assertEquals(114, afterReassembleSameBottle.currentEndurance)
        assertEquals("Chartreuse Verte", afterReassembleSameBottle.bottles[0].completedDrinkName)

        // Отмена хода не возвращает потраченный запас и не снимает отметки списания
        session.undoMove() // откат перелива 3 -> 0
        session.undoMove() // откат перелива 0 -> 3
        session.undoMove() // откат первой сборки 1 -> 0
        val afterUndos = session.snapshot()
        assertEquals(114, afterUndos.currentEndurance)
        assertNull(afterUndos.bottles[0].completedDrinkName)
        assertTrue(afterUndos.bottles[0].charged)
        assertTrue(afterUndos.bottles[3].charged)

        // Повторная сборка отмеченной бутылки 0 после отмены бесплатна (остаётся 114)
        session.tapBottle(1)
        session.tapBottle(0)
        assertEquals(114, session.snapshot().currentEndurance)
        assertEquals("Chartreuse Verte", session.snapshot().bottles[0].completedDrinkName)

        // «Сначала» не возвращает потраченный запас и не снимает отметки списания
        session.restartRun()
        val afterRestart = session.snapshot()
        assertEquals(0, afterRestart.moves)
        assertEquals(114, afterRestart.currentEndurance)
        assertTrue(afterRestart.bottles[0].charged)
        assertTrue(afterRestart.bottles[3].charged)
        assertEquals(setOf(0, 3), afterRestart.chargedBottleIndices)

        // Повторная сборка отмеченной бутылки 0 после «Сначала» бесплатна
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0)
        val afterRebuildPostRestart = session.snapshot()
        assertEquals(114, afterRebuildPostRestart.currentEndurance)
        assertEquals("Chartreuse Verte", afterRebuildPostRestart.bottles[0].completedDrinkName)

        // А первая сборка ещё не отмеченной бутылки 1 списывает 3 (114 -> 111)
        session.tapBottle(0)
        session.tapBottle(1)
        val afterThirdBottleCharged = session.snapshot()
        assertEquals(111, afterThirdBottleCharged.currentEndurance)
        assertTrue(afterThirdBottleCharged.bottles[1].charged)
    }

    @Test
    fun `запас опускается до 0 и ниже нуля не уходит, а списание до 0 открывает экран Персонаж погиб до проверки победы — если тот же ход собрал уровень и оставил 0, победы нет`() {
        // 1. Запас 1 и сборка Chartreuse Verte (цена 3) опускают запас ровно до 0, а не в минус, и открывают гибель без победы
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE),
                emptyList(),
                emptyList()
            ),
            initialStamina = 1
        )

        val beforeFatalMove = session.snapshot()
        assertEquals(1, beforeFatalMove.currentEndurance)
        assertEquals(ScreenId.COUNTER, beforeFatalMove.screen)
        assertTrue(beforeFatalMove.canStartLevel)

        // Ход собирает последнюю бутылку уровня, но списывает выносливость с 1 до 0
        session.tapBottle(1)
        session.tapBottle(0)

        val defeated = session.snapshot()
        assertEquals(0, defeated.stamina)
        assertEquals(0, defeated.currentEndurance)
        assertEquals("0/120", defeated.enduranceText)
        assertEquals(ScreenId.DEFEAT, defeated.screen)
        assertNull(defeated.victoryBanner)
        assertNotNull(defeated.defeatScreen)
        assertEquals("Персонаж погиб", defeated.defeatScreen!!.title)
        assertEquals("Новая игра", defeated.defeatScreen!!.newGameButtonLabel)
        assertEquals("Новая игра", defeated.newGameButtonLabel)
        assertEquals(ActionButtonSnapshot(title = "Новая игра", enabled = true), defeated.newGameButton)
        assertNull(defeated.restartButton)
        assertNull(defeated.undoButton)
    }

    @Test
    fun `на экране гибели одна кнопка Новая игра, Отмена хода и Сначала отсутствуют, системная Назад экран не закрывает и новую игру не начинает, ожидание не поднимает запас с нуля, а Новая игра ставит 120 из 120, снимает отметки списания и открывает живой прилавок со стартовой расстановкой`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                emptyList(),
                emptyList()
            ),
            initialStamina = 1,
            initialCeiling = 135
        )

        // Запас 1 позволяет начать уровень
        assertTrue(session.snapshot().canStartLevel)
        assertEquals(ScreenId.COUNTER, session.snapshot().screen)

        // Ход 1: APEROL из 1 в 2
        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(1, session.snapshot().currentEndurance)

        // Ход 2: первая сборка Guinness Draught (цена 1) при запасе 1 доводит запас до 0 -> экран гибели
        session.tapBottle(1)
        session.tapBottle(0)

        val onDefeat = session.snapshot()
        assertEquals(ScreenId.DEFEAT, onDefeat.screen)
        assertEquals(0, onDefeat.currentEndurance)
        assertFalse(onDefeat.canStartLevel)
        assertNull(onDefeat.restartButton)
        assertNull(onDefeat.undoButton)
        assertNull(onDefeat.victoryBanner)
        assertNotNull(onDefeat.defeatScreen)
        assertEquals("Персонаж погиб", onDefeat.defeatScreen!!.title)
        assertEquals(listOf("Новая игра"), onDefeat.defeatScreen!!.buttons.map { it.title })

        // На экране гибели перелив, отмена хода, Сначала, системная Назад, попытка войти в уровень и ожидание ничего не меняют
        session.tapBottle(0)
        session.tapBottle(3)
        session.tapBottleName(0)
        session.undoMove()
        session.restartRun()
        session.systemBack()
        session.tapBack()
        session.enterCounter()
        session.startLevel()
        session.advanceTimeMinutes(600)

        val stillDefeated = session.snapshot()
        assertEquals(ScreenId.DEFEAT, stillDefeated.screen)
        assertEquals(0, stillDefeated.currentEndurance)
        assertFalse(stillDefeated.canStartLevel)
        assertNull(stillDefeated.drinkCard)
        assertNull(stillDefeated.restartButton)
        assertNull(stillDefeated.undoButton)

        // «Новая игра» ставит запас и потолок 120/120, снимает отметки списания и незавершённый заход и снова открывает живой прилавок со стартовой расстановкой
        session.startNewGame()

        val afterNewGame = session.snapshot()
        assertEquals(ScreenId.COUNTER, afterNewGame.screen)
        assertEquals(120, afterNewGame.stamina)
        assertEquals(120, afterNewGame.staminaCeiling)
        assertEquals(120, afterNewGame.currentEndurance)
        assertEquals(120, afterNewGame.maxEndurance)
        assertEquals("120/120", afterNewGame.enduranceText)
        assertTrue(afterNewGame.canStartLevel)
        assertNull(afterNewGame.defeatScreen)
        assertNull(afterNewGame.victoryBanner)
        assertEquals(0, afterNewGame.moves)
        assertEquals(emptySet<Int>(), afterNewGame.chargedBottleIndices)
        assertTrue(afterNewGame.bottles.none { it.charged })
        assertTrue(afterNewGame.bottles.none { it.selected })
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT), afterNewGame.bottles[0].portions)
        assertEquals(listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL), afterNewGame.bottles[1].portions)
        assertEquals(emptyList<Drink>(), afterNewGame.bottles[2].portions)
        assertEquals(emptyList<Drink>(), afterNewGame.bottles[3].portions)
        assertEquals(ActionButtonSnapshot(title = "Сначала", enabled = false), afterNewGame.restartButton)
        assertEquals(ActionButtonSnapshot(title = "Отмена хода", enabled = false), afterNewGame.undoButton)

        // В новой игре первая сборка бутылки 0 снова списывает выносливость (120 -> 119), так как отметки списания сняты
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0)
        val afterAssembleInNewGame = session.snapshot()
        assertEquals(119, afterAssembleInNewGame.currentEndurance)
        assertTrue(afterAssembleInNewGame.bottles[0].charged)
    }

    @Test
    fun `уровень можно начать только при запасе хотя бы 1, а при запасе 1 первая сборка любого напитка доводит запас до 0`() {
        val zeroEnduranceSession = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            ),
            initialStamina = 0
        )

        assertFalse(zeroEnduranceSession.canStartLevel())
        assertFalse(zeroEnduranceSession.snapshot().canStartLevel)
        assertEquals(ScreenId.DEFEAT, zeroEnduranceSession.snapshot().screen)
        assertFalse(zeroEnduranceSession.startLevel())
        zeroEnduranceSession.enterCounter()
        assertEquals(ScreenId.DEFEAT, zeroEnduranceSession.snapshot().screen)

        // При запасе 1 уровень можно начать, и первая сборка любого из 9 напитков доводит запас до 0 и открывает гибель
        for (drink in Drink.entries) {
            val oneEnduranceSession = LevelSession(
                capacity = 3,
                initialBottles = listOf(
                    listOf(drink, drink),
                    listOf(drink, Drink.PILSNER_URQUELL.takeIf { it != drink } ?: Drink.GUINNESS_DRAUGHT),
                    emptyList(),
                    emptyList()
                ),
                initialStamina = 1
            )
            assertTrue(oneEnduranceSession.canStartLevel())
            assertTrue(oneEnduranceSession.snapshot().canStartLevel)
            assertTrue(oneEnduranceSession.startLevel())
            assertEquals(ScreenId.COUNTER, oneEnduranceSession.snapshot().screen)

            oneEnduranceSession.tapBottle(1)
            oneEnduranceSession.tapBottle(2)
            assertEquals(1, oneEnduranceSession.snapshot().currentEndurance)
            assertEquals(ScreenId.COUNTER, oneEnduranceSession.snapshot().screen)

            oneEnduranceSession.tapBottle(1)
            oneEnduranceSession.tapBottle(0)
            val snap = oneEnduranceSession.snapshot()
            assertEquals(0, snap.currentEndurance)
            assertEquals(ScreenId.DEFEAT, snap.screen)
            assertNull(snap.victoryBanner)
        }
    }

    @Test
    fun `в пуле девять напитков с точными именами, словесными цветами, крепостью и не сливающимися на тёмном экране пиксельными цветами`() {
        val drinks = Drink.entries
        assertEquals(9, drinks.size)

        val expectedPool = listOf(
            Triple("Guinness Draught", "тёмный рубин", "4,2%"),
            Triple("Pilsner Urquell", "светло-золотой", "4,4%"),
            Triple("Aperol", "ярко-оранжевый", "11%"),
            Triple("Giffard Crème de Violette", "фиолетовый", "16%"),
            Triple("Baileys Original Irish Cream", "кремово-бежевый", "17%"),
            Triple("Giffard Lichi-Li", "светло-розовый", "18%"),
            Triple("Bols Blue Curaçao", "ярко-синий", "21%"),
            Triple("Campari", "прозрачный алый", "25%"),
            Triple("Chartreuse Verte", "жёлто-зелёный", "55%")
        )

        assertEquals(
            expectedPool,
            drinks.map { Triple(it.title, it.colorName, it.strength) }
        )

        // У каждого напитка свой уникальный пиксельный цвет, отличный от чёрного и от тёмного фона #100E14
        val darkBackgroundRgb = 0x100E14
        val pixelColors = drinks.map { it.pixelColorRgb }
        assertEquals(9, pixelColors.toSet().size)
        assertFalse(pixelColors.contains(0x000000))
        assertFalse(pixelColors.contains(darkBackgroundRgb))

        // Все 36 пар напитков и пара с тёмным фоном заметно разведены в RGB-пространстве (евклидово расстояние >= 45)
        fun rgbDistance(c1: Int, c2: Int): Double {
            val r1 = (c1 shr 16) and 0xFF
            val g1 = (c1 shr 8) and 0xFF
            val b1 = c1 and 0xFF
            val r2 = (c2 shr 16) and 0xFF
            val g2 = (c2 shr 8) and 0xFF
            val b2 = c2 and 0xFF
            val dr = (r1 - r2).toDouble()
            val dg = (g1 - g2).toDouble()
            val db = (b1 - b2).toDouble()
            return kotlin.math.sqrt(dr * dr + dg * dg + db * db)
        }

        for (drink in drinks) {
            assertTrue(
                "Цвет ${drink.title} не должен сливаться с тёмным экраном",
                rgbDistance(drink.pixelColorRgb, darkBackgroundRgb) >= 80.0
            )
        }
        for (i in drinks.indices) {
            for (j in i + 1 until drinks.size) {
                val d = rgbDistance(drinks[i].pixelColorRgb, drinks[j].pixelColorRgb)
                assertTrue(
                    "Цвета ${drinks[i].title} и ${drinks[j].title} не должны сливаться (distance=$d)",
                    d >= 45.0
                )
            }
        }
    }

    @Test
    fun `рисунок бутылки на всех девяти листах имеет один и тот же силуэт без знака дома, залит до горлышка цветом напитка со статичным свечением, у Guinness Draught тихий рубиновый край без пенной шапки на рисунке и не зависит от уровня`() {
        val capacities = listOf(3, 4, 5, 6)
        val illustrationsByDrink = mutableMapOf<Drink, BottleIllustrationSnapshot>()

        for (drink in Drink.entries) {
            val illustrationsAcrossLevels = capacities.map { cap ->
                val bottleCount = when (cap) {
                    3 -> 4
                    4 -> 6
                    5 -> 8
                    else -> 10
                }
                val session = LevelSession(
                    capacity = cap,
                    initialBottles = listOf(
                        List(cap - 1) { drink },
                        listOf(drink)
                    ) + List(bottleCount - 2) { emptyList() }
                )
                session.tapBottle(1)
                session.tapBottle(0)
                session.tapBottleName(0)
                val card = session.snapshot().drinkCard
                assertNotNull("Карточка ${drink.title} должна открыться на уровне вместимостью $cap", card)
                card!!.illustration
            }

            // С какого уровня лист ни открыт, рисунок тот же
            val first = illustrationsAcrossLevels.first()
            for (ill in illustrationsAcrossLevels) {
                assertEquals(first, ill)
            }

            assertTrue(first.sharedPixelSilhouette)
            assertFalse(first.hasHouseMark)
            assertFalse(first.hasManufacturerPhoto)
            assertTrue(first.filledToNeck)
            assertTrue(first.hasStaticGlow)
            assertTrue(first.staticGlowOfDrinkColor)
            assertFalse(first.hasFoamHeadLayer)
            assertEquals(drink.colorName, first.colorName)
            assertEquals(drink.pixelColorRgb, first.fillColorRgb)
            assertEquals(drink.pixelColorRgb, first.glowColorRgb)

            // Силуэт состоит только из пустого фона '0', жидкости '1' (залитой до горлышка без отдельного слоя пены) и стекла '2'
            assertTrue(first.pixelMask.all { row -> row.all { ch -> ch == '0' || ch == '1' || ch == '2' } })
            // Горлышко (строки 1 и 2) и тело бутылки залиты одним и тем же кодом жидкости '1'
            assertTrue(first.pixelMask[1].contains('1'))
            assertTrue(first.pixelMask[2].contains('1'))
            assertTrue(first.pixelMask[4].contains('1'))

            if (drink == Drink.GUINNESS_DRAUGHT) {
                assertTrue(first.subtleRubyEdge)
                assertEquals("тёмный рубин", first.colorName)
            } else {
                assertFalse(first.subtleRubyEdge)
            }

            illustrationsByDrink[drink] = first
        }

        // У всех девяти напитков один и тот же пиксельный силуэт
        val commonMask = illustrationsByDrink.values.first().pixelMask
        for (ill in illustrationsByDrink.values) {
            assertEquals(commonMask, ill.pixelMask)
        }
    }

    @Test
    fun `сборка каждого из девяти напитков пула открывает его лист с точным именем, крепостью, цветом, культурой, историей и фактами без пустых заглушек и выдуманных сведений`() {
        fun assembleAndOpenCard(drink: Drink): DrinkCardSnapshot {
            val otherDrink = if (drink == Drink.GUINNESS_DRAUGHT) Drink.PILSNER_URQUELL else Drink.GUINNESS_DRAUGHT
            val session = LevelSession(
                capacity = 3,
                initialBottles = listOf(
                    listOf(drink, drink),
                    listOf(otherDrink, drink),
                    emptyList(),
                    emptyList()
                )
            )
            // Переливаем третий слой drink из бутылки 1 в бутылку 0, собирая полную бутылку
            session.tapBottle(1)
            session.tapBottle(0)
            assertEquals(drink.title, session.snapshot().bottles[0].completedDrinkName)

            session.tapBottleName(0)
            val snap = session.snapshot()
            assertEquals(ScreenId.DRINK_CARD, snap.screen)
            assertNotNull(snap.drinkCard)
            return snap.drinkCard!!
        }

        val cards = Drink.entries.associateWith { assembleAndOpenCard(it) }
        assertEquals(9, cards.size)

        // Общие инварианты для всех девяти листов: нет пустых заглушек там, где дом ничего не назвал
        val forbiddenPlaceholders = listOf("не указано", "нет данных", "н/д", "N/A", "TBD", "TODO", "неизвестно")
        for ((drink, card) in cards) {
            val fullText = listOf(card.name, card.strength, card.drinkingCulture, card.history).joinToString(" ") +
                " " + card.facts.joinToString(" ")
            assertTrue(card.drinkingCulture.isNotBlank())
            assertTrue(card.history.isNotBlank())
            assertEquals(3, card.facts.size)
            assertTrue(card.facts.all { it.isNotBlank() })
            for (placeholder in forbiddenPlaceholders) {
                assertFalse(
                    "На листе ${drink.title} не должно быть заглушки '$placeholder'",
                    fullText.contains(placeholder, ignoreCase = true)
                )
            }
        }

        // 1. Guinness Draught
        val guinness = cards.getValue(Drink.GUINNESS_DRAUGHT)
        assertEquals("Guinness Draught", guinness.name)
        assertEquals("4,2%", guinness.strength)
        assertEquals("тёмный рубин", guinness.colorName)
        assertEquals(
            "Чистый сухой бокал и налив в два приёма под 45 градусов; дома из банки — чистый пинтовый бокал. К пинте — пирог со стейком и чеддером.",
            guinness.drinkingCulture
        )
        assertEquals(
            "31 декабря 1759 года Артур Гиннесс подписал аренду пивоварни St. James’s Gate на девять тысяч лет; Guinness Draught впервые сварен в 1959 году к двухсотлетию.",
            guinness.history
        )
        assertEquals(
            listOf(
                "В банке виджет с азотом даёт кремовую шапку.",
                "Фильтрацию рыбьим клеем убрали, и напиток назван подходящим для веганов.",
                "В составе указан азот."
            ),
            guinness.facts
        )
        assertFalse(guinness.illustration.hasFoamHeadLayer)

        // 2. Pilsner Urquell
        val pilsner = cards.getValue(Drink.PILSNER_URQUELL)
        assertEquals("Pilsner Urquell", pilsner.name)
        assertEquals("4,4%", pilsner.strength)
        assertEquals("светло-золотой", pilsner.colorName)
        assertEquals(
            "Длинный узкий бокал на ножке; в танковых пабах — кружка с ручкой. К пиву — чешский гуляш из говядины с хлебными или картофельными кнедликами. Три налива: Hladinka к жирному, Šnyt к гуляшу или бургеру, Mlíko к десерту.",
            pilsner.drinkingCulture
        )
        assertEquals(
            "5 октября 1842 года Йозеф Гролл сварил первую варку из мягкой пльзеньской воды, жатецкого хмеля и моравского ячменя.",
            pilsner.history
        )
        assertEquals(
            listOf(
                "Рецепт и способ варки с 1842 года дом не менял.",
                "Золотая крышка бутылки отмечает ворота старого завода.",
                "У Hladinka три пальца пены, Šnyt — две доли пива и три доли пены, Mlíko — почти один стакан пены."
            ),
            pilsner.facts
        )

        // 3. Aperol
        val aperol = cards.getValue(Drink.APEROL)
        assertEquals("Aperol", aperol.name)
        assertEquals("11%", aperol.strength)
        assertEquals("ярко-оранжевый", aperol.colorName)
        assertEquals(
            "Большой винный бокал со льдом и долькой апельсина. К спритцу — чичетти: кростини, польпетте, аранчини, тараллини, трамеццини, торинези, мясная нарезка, брускетта, маринованные оливки и выдержанные сыры.",
            aperol.drinkingCulture
        )
        assertEquals(
            "В 1919 году братья Луиджи и Сильвио Барбьери выпустили аперитив после проб с 1912 года; сначала его пили в Падуе и Венеции; рецепт спритца дом относит к 1950-м.",
            aperol.history
        )
        assertEquals(
            listOf(
                "Рецепт дом называет секретным и неизменным.",
                "Бутылки 70 сл и 1 л.",
                "Спритц — 3 доли Prosecco D.O.C., 2 доли Aperol и 1 доля содовой."
            ),
            aperol.facts
        )

        // 4. Giffard Crème de Violette
        val violette = cards.getValue(Drink.GIFFARD_CREME_DE_VIOLETTE)
        assertEquals("Giffard Crème de Violette", violette.name)
        assertEquals("16%", violette.strength)
        assertEquals("фиолетовый", violette.colorName)
        assertEquals(
            "Отдельного бокала нет; аперитив с сухим белым, розовым или игристым и коктейли. Отдельных блюд к ликёру нет; фиалку дом упоминает в кондитерских изделиях.",
            violette.drinkingCulture
        )
        assertEquals(
            "Дом Эмиля Жиффара с жаркого лета 1885 года и мятного Menthe-Pastille, Анже, семейная компания; стиль крем-де-виолетт — XIX век.",
            violette.history
        )
        assertEquals(
            listOf(
                "Натуральный аромат фиалки, Франция.",
                "Коктейли Aviation и Blue Moon.",
                "Без лактозы, без глютена, подходит веганам, без консерванта."
            ),
            violette.facts
        )
        // Год именно этой бутылки на лист не ставится (упомянут только 1885 год дома и XIX век стиля)
        assertEquals(listOf("1885"), Regex("\\b\\d{4}\\b").findAll(violette.history).map { it.value }.toList())

        // 5. Baileys Original Irish Cream
        val baileys = cards.getValue(Drink.BAILEYS_ORIGINAL_IRISH_CREAM)
        assertEquals("Baileys Original Irish Cream", baileys.name)
        assertEquals("17%", baileys.strength)
        assertEquals("кремово-бежевый", baileys.colorName)
        assertEquals(
            "Чистым со льдом, имя бокала не называется. Блюда: полить мороженое, испечь торт, сандей из ванильного мороженого, Baileys и шоколадных крендельков.",
            baileys.drinkingCulture
        )
        assertEquals(
            "К 1974 году в рецепте ирландские сливки, ирландский виски, другие спирты, шоколад и ваниль; дом называет напиток первым сливочным ликёром.",
            baileys.history
        )
        assertEquals(
            listOf(
                "Аллергены — молоко и молочные продукты, включая лактозу.",
                "Дом говорит примерно о 200 миллионах литров свежего ирландского молока в год.",
                "Молоко в основном с небольших семейных ферм."
            ),
            baileys.facts
        )
        // Имён основателей на листе нет
        assertFalse(baileys.history.contains("Дэвид", ignoreCase = true))
        assertFalse(baileys.history.contains("Глюкман", ignoreCase = true))
        assertFalse(baileys.history.contains("Gluckman", ignoreCase = true))
        assertFalse(baileys.history.contains("Дago", ignoreCase = true))
        assertFalse(baileys.history.contains("МакФерсон", ignoreCase = true))

        // 6. Giffard Lichi-Li
        val lichi = cards.getValue(Drink.GIFFARD_LICHI_LI)
        assertEquals("Giffard Lichi-Li", lichi.name)
        assertEquals("18%", lichi.strength)
        assertEquals("светло-розовый", lichi.colorName)
        assertEquals(
            "Чистым на колотом льду, в лонг-дринке или коктейле; отдельного бокала нет.",
            lichi.drinkingCulture
        )
        assertEquals(
            "Личи родом из Китая и растёт также в Индии, на Мадагаскаре и в Таиланде. 1885 год — основание дома, не дата Lichi-Li.",
            lichi.history
        )
        assertEquals(
            listOf(
                "Аромат личи, Франция; вкус цветочный и слегка ванильный.",
                "Без лактозы, без глютена, для веганов, без консерванта.",
                "Срок настоя плодов от 48 часов до 3 месяцев — общий способ дома, не отдельный срок личи."
            ),
            lichi.facts
        )
        // Блюд на листе нет, год этого ликёра не ставится (только 1885 как основание дома)
        assertFalse(lichi.drinkingCulture.contains("блюд", ignoreCase = true))
        assertEquals(listOf("1885"), Regex("\\b\\d{4}\\b").findAll(lichi.history).map { it.value }.toList())

        // 7. Bols Blue Curaçao
        val bols = cards.getValue(Drink.BOLS_BLUE_CURACAO)
        assertEquals("Bols Blue Curaçao", bols.name)
        assertEquals("21%", bols.strength)
        assertEquals("ярко-синий", bols.colorName)
        assertEquals(
            "Бокал самого ликёра не назван; для Blue Hawaii — хайбол.",
            bols.drinkingCulture
        )
        assertEquals(
            "Дистиллерия ‘t Lootsje в Амстердаме в 1575 году; в 1920-е восьмигранная бутылка и Blue Curacao; в 1926 году первая запись о Crème de Ciel; в 1933 году первая этикетка Bols Blue.",
            bols.history
        )
        assertEquals(
            listOf(
                "Бутылка 70 сл, подходит веганам.",
                "Вкус апельсиновой цедры и растительного настоя, в носе апельсин и мандарин.",
                "Blue Hawaii дом датирует 1953 годом и приписывает Гарри Йи."
            ),
            bols.facts
        )
        // Блюд на листе нет, Aviation/Blue Moon для Bols не приписываются
        assertFalse(bols.drinkingCulture.contains("блюд", ignoreCase = true))
        assertFalse(bols.facts.joinToString(" ").contains("Aviation"))

        // 8. Campari
        val campari = cards.getValue(Drink.CAMPARI)
        assertEquals("Campari", campari.name)
        assertEquals("25%", campari.strength)
        assertEquals("прозрачный алый", campari.colorName)
        assertEquals(
            "Negroni в рокс-тамблере со льдом и долькой апельсина, равные доли Campari, лондонского сухого джина и сладкого красного вермута.",
            campari.drinkingCulture
        )
        assertEquals(
            "В 1860 году Гаспаре Кампари составил жидкость, которую дом с тех пор не менял; в 1867 году открыл Caffè Campari в Галерее Виктора Эммануила II, в тот же год родился Давиде Кампари; в 1915 году Давиде открыл бар Camparino.",
            campari.history
        )
        assertEquals(
            listOf(
                "Дом говорит, что без последнего ингредиента рецепт развалится.",
                "Negroni входит в список IBA, категория The Unforgettable.",
                "Campari назван красным сердцем и Americano, и Negroni."
            ),
            campari.facts
        )
        // Бокал чистого Campari и отдельные блюда не названы; спорный год Negroni (1919/1920) не ставится
        assertFalse(campari.drinkingCulture.contains("блюд", ignoreCase = true))
        assertFalse(campari.history.contains("1919"))
        assertFalse(campari.history.contains("1920"))

        // 9. Chartreuse Verte
        val chartreuse = cards.getValue(Drink.CHARTREUSE_VERTE)
        assertEquals("Chartreuse Verte", chartreuse.name)
        assertEquals("55%", chartreuse.strength)
        assertEquals("жёлто-зелёный", chartreuse.colorName)
        assertEquals(
            "Дижестив, охлаждённый до 12–13 °C или со льдом; имя бокала не названо. С XIX века ликёр кладут в сладкие и солёные рецепты. Green Chaud — горячий шоколад с ложкой ликёра, взбитыми сливками и какао.",
            chartreuse.drinkingCulture
        )
        assertEquals(
            "В 1840 году из растительного эликсира сделали «ликёр здоровья»; отец Гарнье дал имя Chartreuse Verte и в 1852 году зарегистрировал марку; ликёр делают картезианцы на дистиллерии Эгюнуар в Антр-де-Гье.",
            chartreuse.history
        )
        assertEquals(
            listOf(
                "130 растений, коры, корней, пряностей и цветов; формулу знают только два человека в монастыре Гранд-Шартрёз.",
                "Травы растирают в монастыре, цвет даёт мацерация, затем выдержка в дубовых чанах.",
                "Бутылки 3, 20, 35, 70 и 300 сл, на горле печать Гранд-Шартрёз."
            ),
            chartreuse.facts
        )
    }

    @Test
    fun `чистая победа без пустых оплаченных бутылок имеет лишнюю трату 0, даёт 3 звезды на плашке Победа и записывает 3 звезды уровня`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )

        assertEquals(0, session.snapshot().bestStars)
        assertEquals(0, session.snapshot().recordedStars)

        session.tapBottle(0)
        session.tapBottle(2)
        session.tapBottle(0)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(0)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(3)

        val won = session.snapshot()
        val banner = won.victoryBanner
        assertNotNull(banner)
        assertEquals("Победа", banner!!.title)
        assertEquals(0, banner.extraWaste)
        assertEquals(3, banner.stars)
        assertEquals(6, banner.moves)
        assertEquals(0, won.extraWaste)
        assertEquals(3, won.bestStars)
        assertEquals(3, won.recordedStars)
    }

    @Test
    fun `лишняя трата равна сумме цен оплаченных в заходе бутылок которые к победе пусты, даёт 2 звезды при трате 1 или 2 и 1 звезду при трате 3 и больше, а собранная разбитая и снова собранная бутылка в лишнюю трату не входит`() {
        // 1. Оплаченная бутылка Guinness Draught (цена 1) к победе пуста -> лишняя трата 1 -> 2 звезды
        val waste1Session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )
        // Ход 1: PILSNER из 1 в 2 -> бутылка 2 собрана (списано 1 за Pilsner)
        waste1Session.tapBottle(1)
        waste1Session.tapBottle(2)
        // Ход 2: GUINNESS из 1 в 0 -> бутылка 0 собрана (списано 1 за Guinness), но уровень уже собран!
        // Чтобы проверить пустую оплаченную бутылку до победы: возьмём расстановку, где бутылку собрали и вылили до победы.
        val wasteOne = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                listOf(Drink.APEROL, Drink.APEROL),
                emptyList()
            )
        )
        // Ход 1: APEROL из 1 в 3 (не собрана)
        wasteOne.tapBottle(1)
        wasteOne.tapBottle(3)
        // Ход 2: GUINNESS из 1 в 0 -> бутылка 0 собрана (Guinness, цена 1), в бутылке 3 лежит 1 APEROL, в 2 лежит 2 APEROL (ещё не победа)
        wasteOne.tapBottle(1)
        wasteOne.tapBottle(0)
        assertNull(wasteOne.snapshot().victoryBanner)
        // Ход 3: переливаем весь собранный GUINNESS из бутылки 0 в пустую бутылку 1 -> бутылка 0 теперь ПУСТА и оплачена (цена 1), бутылка 1 оплачена и полна
        wasteOne.tapBottle(0)
        wasteOne.tapBottle(1)
        assertNull(wasteOne.snapshot().victoryBanner)
        // Ход 4: APEROL из 3 в 2 -> собирает APEROL и завершает уровень
        wasteOne.tapBottle(3)
        wasteOne.tapBottle(2)

        val snapWaste1 = wasteOne.snapshot()
        assertNotNull(snapWaste1.victoryBanner)
        assertEquals(1, snapWaste1.extraWaste)
        assertEquals(1, snapWaste1.victoryBanner!!.extraWaste)
        assertEquals(2, snapWaste1.victoryBanner!!.stars)
        assertEquals(4, snapWaste1.victoryBanner!!.moves)
        assertEquals(2, snapWaste1.bestStars)

        // 2. Оплаченная бутылка Aperol (цена 2) к победе пуста -> лишняя трата 2 -> 2 звезды
        val wasteTwo = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.APEROL),
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )
        wasteTwo.tapBottle(1)
        wasteTwo.tapBottle(3) // GUINNESS -> 3
        wasteTwo.tapBottle(1)
        wasteTwo.tapBottle(0) // бутылка 0 собрана APEROL (цена 2)
        wasteTwo.tapBottle(0)
        wasteTwo.tapBottle(1) // перелили APEROL из 0 в 1 -> бутылка 0 пуста (оплачена ценой 2)
        wasteTwo.tapBottle(3)
        wasteTwo.tapBottle(2) // собираем GUINNESS в 2 -> победа

        val snapWaste2 = wasteTwo.snapshot()
        assertEquals(2, snapWaste2.extraWaste)
        assertEquals(2, snapWaste2.victoryBanner!!.extraWaste)
        assertEquals(2, snapWaste2.victoryBanner!!.stars)
        assertEquals(2, snapWaste2.bestStars)

        // 3. Оплаченная бутылка Chartreuse Verte (цена 3) к победе пуста -> лишняя трата 3 -> 1 звезда
        val wasteThree = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )
        wasteThree.tapBottle(1)
        wasteThree.tapBottle(3)
        wasteThree.tapBottle(1)
        wasteThree.tapBottle(0) // бутылка 0 собрана CHARTREUSE_VERTE (цена 3)
        wasteThree.tapBottle(0)
        wasteThree.tapBottle(1) // бутылка 0 пуста (трата 3)
        wasteThree.tapBottle(3)
        wasteThree.tapBottle(2) // победа

        val snapWaste3 = wasteThree.snapshot()
        assertEquals(3, snapWaste3.extraWaste)
        assertEquals(3, snapWaste3.victoryBanner!!.extraWaste)
        assertEquals(1, snapWaste3.victoryBanner!!.stars)
        assertEquals(1, snapWaste3.bestStars)

        // 4. Бутылка, собранная, разбитая (опустошённая) и к победе собранная снова, в лишнюю трату не входит -> лишняя трата 0 -> 3 звезды
        val rebuiltBottle = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )
        rebuiltBottle.tapBottle(1)
        rebuiltBottle.tapBottle(3) // 1 GUINNESS в 3
        rebuiltBottle.tapBottle(1)
        rebuiltBottle.tapBottle(0) // бутылка 0 собрана CHARTREUSE_VERTE (цена 3 списана с бутылки 0)
        // Разбиваем бутылку 0 частично в неполную бутылку 3 (напиток другой — нельзя), поэтому разобьём через отмену или частичный перелив:
        // В бутылке 2 лежит 2 GUINNESS, перельём 2 GUINNESS из 2 в 3 -> бутылка 3 собрана GUINNESS, а бутылка 2 пуста (не была оплачена!)
        // Но если мы перельём 2 порции CHARTREUSE из бутылки 0 в бутылку, где уже лежит 1 CHARTREUSE:
        val rebuiltPartial = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.APEROL, Drink.APEROL)
            )
        )
        // Ход 1: из 2 (2 GUINNESS) некуда, но из 1 (1 GUINNESS) можно на 2? В 2 уже 3 порции.
        // Сделаем проще: бутылка 0 собрана, затем отмена хода её разбирает, а потом её собирают снова к победе:
        rebuiltBottle.undoMove() // бутылка 0 снова неполная (2 CHARTREUSE), хотя остаётся оплаченной (charged)
        assertTrue(rebuiltBottle.snapshot().bottles[0].charged)
        // Снова собираем бутылку 0 и завершаем уровень так, что все оплаченные бутылки к победе полны
        rebuiltBottle.tapBottle(1)
        rebuiltBottle.tapBottle(0) // бутылка 0 снова собрана!
        rebuiltBottle.tapBottle(3)
        rebuiltBottle.tapBottle(2) // бутылка 2 собрана GUINNESS, бутылки 1 и 3 пусты и НЕ оплачены

        val snapRebuilt = rebuiltBottle.snapshot()
        assertEquals(0, snapRebuilt.extraWaste)
        assertEquals(0, snapRebuilt.victoryBanner!!.extraWaste)
        assertEquals(3, snapRebuilt.victoryBanner!!.stars)
        assertEquals(3, snapRebuilt.bestStars)

        // 4б. Сборка бутылки 0 обычным переливом, затем разборка обычным переливом в пустую и повторная сборка обратно к победе
        val rebuiltByPours = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )
        rebuiltByPours.tapBottle(1)
        rebuiltByPours.tapBottle(3) // 1 PILSNER в 3
        rebuiltByPours.tapBottle(1)
        rebuiltByPours.tapBottle(0) // бутылка 0 собрана GUINNESS (списано 1 с бутылки 0)
        rebuiltByPours.tapBottle(0)
        rebuiltByPours.tapBottle(4) // бутылка 0 полностью вылита в 4 (списано 1 с бутылки 4), бутылка 0 пуста
        rebuiltByPours.tapBottle(4)
        rebuiltByPours.tapBottle(0) // бутылка 4 вылита обратно в 0 -> бутылка 0 снова полна и в лишнюю трату не входит, а пуста только бутылка 4 (трата 1, а не 2!)
        rebuiltByPours.tapBottle(3)
        rebuiltByPours.tapBottle(2) // победа

        val snapRebuiltByPours = rebuiltByPours.snapshot()
        assertEquals(1, snapRebuiltByPours.extraWaste) // только пустая бутылка 4 (цена 1), а заново собранная бутылка 0 в трату не вошла
        assertEquals(2, snapRebuiltByPours.victoryBanner!!.stars)

        // 4в. Бутылка 0 собрана (цена 3), полностью опустошена через промежуточные неполные бутылки без оплаты других пустых бутылок, и снова собрана к победе -> лишняя трата 0 -> 3 звезды
        val rebuiltClean = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.APEROL, Drink.CHARTREUSE_VERTE),
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                emptyList(),
                emptyList()
            )
        )
        // Ход 1: из 1 (1 CHARTREUSE) в 0 -> бутылка 0 собрана CHARTREUSE (оплачена 3)
        rebuiltClean.tapBottle(1)
        rebuiltClean.tapBottle(0)
        assertTrue(rebuiltClean.snapshot().bottles[0].charged)
        // Ход 2: из 3 (1 APEROL) в 1 -> в 1 теперь 2 APEROL (не полна, не оплачена)
        rebuiltClean.tapBottle(3)
        rebuiltClean.tapBottle(1)
        // Ход 3: из 2 (2 GUINNESS) в 3 -> бутылка 3 собрана GUINNESS (оплачена 1) и останется полной до конца!
        rebuiltClean.tapBottle(2)
        rebuiltClean.tapBottle(3)
        // Ход 4: из 1 (2 APEROL) в 2 -> бутылка 2 собрана APEROL (оплачена 2) и останется полной до конца! Бутылка 1 теперь пуста и НЕ оплачена.
        rebuiltClean.tapBottle(1)
        rebuiltClean.tapBottle(2)
        // Ход 5: чтобы опустошить и заново собрать бутылку 0 без оплаты пустой бутылки:
        // Если нажать Сначала (возврат к старту сохраняет отметку списания на бутылке 0, опустошая собранное состояние), и затем чисто собрать уровень:
        rebuiltClean.restartRun()
        assertTrue(rebuiltClean.snapshot().bottles[0].charged)
        // Теперь решаем с нуля: бутылка 0 к победе снова собрана, а пустые бутылки 4 и 5 никогда не собирались
        rebuiltClean.tapBottle(1)
        rebuiltClean.tapBottle(0) // бутылка 0 снова собрана CHARTREUSE
        rebuiltClean.tapBottle(3)
        rebuiltClean.tapBottle(1) // 2 APEROL в 1
        rebuiltClean.tapBottle(2)
        rebuiltClean.tapBottle(3) // бутылка 3 собрана GUINNESS
        rebuiltClean.tapBottle(1)
        rebuiltClean.tapBottle(2) // бутылка 2 собрана APEROL -> победа!

        val snapRebuiltClean = rebuiltClean.snapshot()
        assertNotNull(snapRebuiltClean.victoryBanner)
        assertEquals(0, snapRebuiltClean.extraWaste)
        assertEquals(3, snapRebuiltClean.victoryBanner!!.stars)
        assertEquals(3, snapRebuiltClean.bestStars)
    }

    @Test
    fun `плашка Победа показывает звёзды и число оставшихся не отменённых ходов, отмена хода оценку не снижает и не мешает трём звёздам, а норматив расстановки на плашку не выводится и звёзды не считает`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            ),
            parMoves = 2
        )

        // Делаем лишние переливания без сборки и отменяем часть из них
        session.tapBottle(1)
        session.tapBottle(3) // ход 1: PILSNER в 3
        session.tapBottle(3)
        session.tapBottle(4) // ход 2: PILSNER из 3 в 4
        session.undoMove()   // отмена хода 2 -> счётчик ходов снова 1
        session.tapBottle(3)
        session.tapBottle(4) // ход 2
        session.tapBottle(4)
        session.tapBottle(3) // ход 3
        session.undoMove()   // отмена хода 3 -> счётчик ходов снова 2

        // Завершаем уровень за 4 оставшихся не отменённых хода (больше норматива 2!), но с лишней тратой 0
        session.tapBottle(4)
        session.tapBottle(2) // ход 3: бутылка 2 собрана PILSNER
        session.tapBottle(1)
        session.tapBottle(0) // ход 4: бутылка 0 собрана GUINNESS -> победа

        val won = session.snapshot()
        val banner = won.victoryBanner
        assertNotNull(banner)
        assertEquals("Победа", banner!!.title)
        assertEquals(3, banner.stars)
        assertEquals(4, banner.moves)
        assertEquals(0, banner.extraWaste)
        assertEquals(3, won.bestStars)
    }

    @Test
    fun `записанные звёзды уровня растут при более сильном повторе, не снижаются при слабом повторе, а гибель в ноль на победном ходе звёзды не записывает и уже записанные не стирает`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            ),
            initialStamina = 10
        )

        // Заход 1: победа с пустой оплаченной бутылкой Chartreuse Verte (лишняя трата 3 -> 1 звезда)
        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(0) // собрали бутылку 0 (Chartreuse, цена 3; stamina: 10 -> 7)
        session.tapBottle(0)
        session.tapBottle(1) // перелили в 1 (Chartreuse, цена 3; stamina: 7 -> 4), бутылка 0 пуста и оплачена
        session.tapBottle(3)
        session.tapBottle(2) // собрали бутылку 2 (Guinness, цена 1; stamina: 4 -> 3) -> победа

        val afterFirstWin = session.snapshot()
        assertEquals(1, afterFirstWin.victoryBanner!!.stars)
        assertEquals(1, afterFirstWin.bestStars)
        assertEquals(1, afterFirstWin.recordedStars)

        // Выходим с плашки победы и начинаем новый заход того же уровня со стартовой расстановки
        session.leaveCounter()
        assertTrue(session.startLevel())
        val secondRunStart = session.snapshot()
        assertNull(secondRunStart.victoryBanner)
        assertEquals(0, secondRunStart.moves)
        assertTrue(secondRunStart.bottles.none { it.charged })
        assertEquals(1, secondRunStart.bestStars)

        // Заход 2: чистая победа без пустых оплаченных бутылок (лишняя трата 0 -> 3 звезды; stamina: 3 -> 0? Нет: Chartreuse 3 + Guinness 1 = 4, поэтому поднимем запас временем до 10)
        session.advanceTimeMinutes(35) // +7 -> stamina = 10
        session.tapBottle(1)
        session.tapBottle(2) // собрали бутылку 2 (Guinness, цена 1; stamina: 10 -> 9)
        session.tapBottle(1)
        session.tapBottle(0) // собрали бутылку 0 (Chartreuse, цена 3; stamina: 9 -> 6) -> чистая победа!

        val afterBetterWin = session.snapshot()
        assertEquals(0, afterBetterWin.extraWaste)
        assertEquals(3, afterBetterWin.victoryBanner!!.stars)
        assertEquals(3, afterBetterWin.bestStars)
        assertEquals(3, afterBetterWin.recordedStars)

        // Заход 3: слабый повтор на 1 звезду (лишняя трата 3) — плашка показывает 1 звезду захода, но записанные звёзды уровня остаются 3
        session.advanceTimeMinutes(30) // stamina -> 12
        session.leaveCounter()
        assertTrue(session.startLevel())
        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(0) // списано 3 (12 -> 9)
        session.tapBottle(0)
        session.tapBottle(1) // списано 3 (9 -> 6), бутылка 0 пуста
        session.tapBottle(3)
        session.tapBottle(2) // списано 1 (6 -> 5) -> победа с 1 звездой

        val afterWorseReplay = session.snapshot()
        assertEquals(3, afterWorseReplay.extraWaste)
        assertEquals(1, afterWorseReplay.victoryBanner!!.stars)
        assertEquals(3, afterWorseReplay.bestStars)
        assertEquals(3, afterWorseReplay.recordedStars)

        // Заход 4: гибель в ноль на ходе, который собрал уровень — уже записанные 3 звезды НЕ стираются
        // Сейчас stamina = 5. Нам нужно, чтобы к последнему ходу осталось 3 и сборка Chartreuse (цена 3) опустила в 0:
        // Если в заходе 4 сперва собрать Guinness в 2 (цена 1: 5 -> 4), перелить в 3 (цена 1: 4 -> 3), и собрать Chartreuse в 0 (цена 3: 3 -> 0):
        session.leaveCounter()
        assertTrue(session.startLevel())
        session.tapBottle(1)
        session.tapBottle(2) // Guinness в 2 собран (5 -> 4)
        session.tapBottle(2)
        session.tapBottle(3) // Guinness в 3 собран (4 -> 3)
        session.tapBottle(1)
        session.tapBottle(0) // Chartreuse в 0 собирает уровень, но опускает запас 3 -> 0 (гибель!)

        val afterDefeatOnSolved = session.snapshot()
        assertEquals(ScreenId.DEFEAT, afterDefeatOnSolved.screen)
        assertEquals(0, afterDefeatOnSolved.currentEndurance)
        assertNull(afterDefeatOnSolved.victoryBanner)
        assertEquals(3, afterDefeatOnSolved.bestStars)
        assertEquals(3, afterDefeatOnSolved.recordedStars)

        // А на уровне, где звёзд ещё не было, гибель на ходе, который собрал уровень, звёзды не записывает (остаётся 0)
        val firstRunFatal = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            ),
            initialStamina = 1
        )
        firstRunFatal.tapBottle(1)
        firstRunFatal.tapBottle(0) // собирает бутылку 0, запас 1 -> 0 -> гибель
        val fatalSnap = firstRunFatal.snapshot()
        assertEquals(ScreenId.DEFEAT, fatalSnap.screen)
        assertNull(fatalSnap.victoryBanner)
        assertEquals(0, fatalSnap.bestStars)
        assertEquals(0, fatalSnap.recordedStars)

        // Новая игра стирает записанные звёзды вместе с остальным прогрессом
        session.startNewGame()
        val afterNewGame = session.snapshot()
        assertEquals(0, afterNewGame.bestStars)
        assertEquals(0, afterNewGame.recordedStars)
    }

    @Test
    fun `пока персонаж жив и запас ниже потолка, время часов телефона копится к следующей единице — каждые полные 5 минут дают одну единицу до потолка`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            ),
            initialStamina = 115,
            initialCeiling = 120
        )

        val initial = session.snapshot()
        assertEquals(115, initial.stamina)
        assertEquals(0L, initial.recoveryProgressMillis)
        assertEquals(0L, initial.recoveryRemainderMillis)
        assertEquals(0, initial.recoveryRemainderMinutes)

        // 3 минуты (180_000 мс) — ещё не полные 5 минут, запас 115, доля 180_000 мс (3 минуты)
        session.advanceTimeMillis(180_000L)
        val after3Min = session.snapshot()
        assertEquals(115, after3Min.stamina)
        assertEquals(180_000L, after3Min.recoveryProgressMillis)
        assertEquals(180_000L, after3Min.recoveryRemainderMillis)
        assertEquals(3, after3Min.recoveryRemainderMinutes)

        // Ещё 2 минуты (120_000 мс) — полные 5 минут дают +1 единицу (116), доля 0
        session.advanceTimeMillis(120_000L)
        val after5Min = session.snapshot()
        assertEquals(116, after5Min.stamina)
        assertEquals(0L, after5Min.recoveryProgressMillis)
        assertEquals(0, after5Min.recoveryRemainderMinutes)

        // Через метки времени часов телефона (onClockTimeMillis): сдвиг на 12 минут 30 секунд (750_000 мс) даёт +2 единицы (118) и долю 150_000 мс (2 минуты)
        session.onClockTimeMillis(1_000_000L)
        session.onClockTimeMillis(1_750_000L)
        val afterClock = session.snapshot()
        assertEquals(118, afterClock.stamina)
        assertEquals(150_000L, afterClock.recoveryProgressMillis)
        assertEquals(2, afterClock.recoveryRemainderMinutes)

        // Перевод на 30 минут вперёд доводит запас до потолка 120 и не превышает его
        session.advanceTimeMinutes(30)
        val atCeiling = session.snapshot()
        assertEquals(120, atCeiling.stamina)
        assertEquals(120, atCeiling.staminaCeiling)
    }

    @Test
    fun `на потолке новые единицы не копятся и новая доля не растёт, но уже накопленная доля короче 5 минут не стирается списанием, прибавкой бара и стоянием на потолке и продолжает копиться когда запас снова ниже потолка`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.APEROL),
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            ),
            initialStamina = 120,
            initialCeiling = 120
        )

        // 1. Стояние на потолке 120/120: новые единицы не копятся и доля остаётся 0
        session.advanceTimeMinutes(18)
        val atStartCeiling = session.snapshot()
        assertEquals(120, atStartCeiling.stamina)
        assertEquals(0L, atStartCeiling.recoveryProgressMillis)
        assertEquals(0, atStartCeiling.recoveryRemainderMinutes)

        // 2. Списываем 1 единицу сборкой Guinness Draught (120 -> 119) и копим 3 минуты (180_000 мс)
        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(119, session.snapshot().stamina)

        session.advanceTimeMinutes(3)
        val with3Min = session.snapshot()
        assertEquals(119, with3Min.stamina)
        assertEquals(180_000L, with3Min.recoveryProgressMillis)
        assertEquals(3, with3Min.recoveryRemainderMinutes)

        // 3. Списываем ещё 2 единицы сборкой Aperol (119 -> 117): накопленные 3 минуты не стираются!
        session.tapBottle(1)
        session.tapBottle(0)
        val afterSecondDeduction = session.snapshot()
        assertEquals(117, afterSecondDeduction.stamina)
        assertEquals(180_000L, afterSecondDeduction.recoveryProgressMillis)
        assertEquals(3, afterSecondDeduction.recoveryRemainderMinutes)

        // 4. Прибавка бара (+3 к запасу и потолку: 117/120 -> 120/123) не стирает накопленные 3 минуты
        session.applyBarBonus(3)
        val afterBarBonus = session.snapshot()
        assertEquals(120, afterBarBonus.stamina)
        assertEquals(123, afterBarBonus.staminaCeiling)
        assertEquals(180_000L, afterBarBonus.recoveryProgressMillis)
        assertEquals(3, afterBarBonus.recoveryRemainderMinutes)

        // 5. Ещё 2 минуты доводят долю до 5 минут -> +1 единица (120 -> 121), доля 0
        session.advanceTimeMinutes(2)
        val afterCompletingSavedFraction = session.snapshot()
        assertEquals(121, afterCompletingSavedFraction.stamina)
        assertEquals(0L, afterCompletingSavedFraction.recoveryProgressMillis)

        // 6. На 122/123 копим 4 минуты (240_000 мс), затем поднимаем до потолка 123/123 или стоим на потолке с уже лежащей долей:
        session.advanceTimeMinutes(5) // 121 -> 122
        session.advanceTimeMinutes(4) // 122/123 и доля 4 мин (240_000 мс)
        assertEquals(122, session.snapshot().stamina)
        assertEquals(240_000L, session.snapshot().recoveryProgressMillis)

        // Если запас поднят до потолка (или переведён на потолок с сохранением уже лежащей доли), стояние на потолке не стирает её и не растит выше
        // Проверим также через конструктор с initialRecoveryProgressMillis или подъём запаса до потолка
        val sessionAtCeilingWithSavedFraction = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            ),
            initialStamina = 120,
            initialCeiling = 120,
            initialRecoveryProgressMillis = 180_000L
        )
        // Стоим на потолке 25 минут: новая доля не растёт, но уже лежащие 3 минуты (180_000 мс) не стираются
        sessionAtCeilingWithSavedFraction.advanceTimeMinutes(25)
        assertEquals(120, sessionAtCeilingWithSavedFraction.snapshot().stamina)
        assertEquals(180_000L, sessionAtCeilingWithSavedFraction.snapshot().recoveryProgressMillis)

        // Когда запас снова падает ниже потолка (120 -> 119), эти 3 минуты продолжают копиться: через 2 минуты запас снова 120!
        sessionAtCeilingWithSavedFraction.tapBottle(1)
        sessionAtCeilingWithSavedFraction.tapBottle(0)
        assertEquals(119, sessionAtCeilingWithSavedFraction.snapshot().stamina)
        assertEquals(180_000L, sessionAtCeilingWithSavedFraction.snapshot().recoveryProgressMillis)

        sessionAtCeilingWithSavedFraction.advanceTimeMinutes(2)
        val recoveredFromSaved = sessionAtCeilingWithSavedFraction.snapshot()
        assertEquals(120, recoveredFromSaved.stamina)
        assertEquals(0L, recoveredFromSaved.recoveryProgressMillis)
    }

    @Test
    fun `восстановление идёт при закрытой игре и посреди уровня, а при нуле восстановление персонажа не поднимает`() {
        val session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                emptyList(),
                emptyList()
            ),
            initialStamina = 100,
            initialCeiling = 120
        )

        // 1. Посреди уровня: делаем ход, копим 7 минут (1 единица + 2 минуты доли)
        session.tapBottle(0)
        session.tapBottle(2)
        session.advanceTimeMinutes(7)
        val midLevel = session.snapshot()
        assertEquals(ScreenId.COUNTER, midLevel.screen)
        assertEquals(1, midLevel.moves)
        assertEquals(101, midLevel.stamina)
        assertEquals(120_000L, midLevel.recoveryProgressMillis)

        // 2. Вне прилавка (на плёнке) и при закрытой игре: время продолжает копиться от тех же 2 минут
        session.leaveCounter()
        assertEquals(ScreenId.FILM, session.snapshot().screen)
        session.advanceTimeMinutes(3) // 2 + 3 = 5 минут -> 102, доля 0
        assertEquals(102, session.snapshot().stamina)
        assertEquals(0L, session.snapshot().recoveryProgressMillis)

        // Закрытая игра на 23 минуты (onAppClosed / onAppReopened или по часам телефона): +4 единицы (106) и 3 минуты доли (180_000 мс)
        session.onAppClosed(atClockMillis = 10_000_000L)
        session.onAppReopened(atClockMillis = 10_000_000L + 23 * 60_000L)
        val afterReopen = session.snapshot()
        assertEquals(106, afterReopen.stamina)
        assertEquals(180_000L, afterReopen.recoveryProgressMillis)
        assertEquals(3, afterReopen.recoveryRemainderMinutes)

        // 3. При нуле (гибель) ни в открытой, ни в закрытой игре восстановление персонажа не поднимает
        val fatalSession = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            ),
            initialStamina = 1,
            initialCeiling = 120
        )
        fatalSession.advanceTimeMinutes(3) // накопили 3 минуты доли при живом персонаже
        assertEquals(180_000L, fatalSession.snapshot().recoveryProgressMillis)

        // Сборка доводит запас до 0 -> гибель
        fatalSession.tapBottle(1)
        fatalSession.tapBottle(0)
        assertEquals(ScreenId.DEFEAT, fatalSession.snapshot().screen)
        assertEquals(0, fatalSession.snapshot().stamina)

        // Любое время ожидания (в том числе при закрытой игре) не поднимает с нуля
        fatalSession.advanceTimeMinutes(60)
        fatalSession.onAppClosed(atClockMillis = 20_000_000L)
        fatalSession.onAppReopened(atClockMillis = 20_000_000L + 600 * 60_000L)
        val stillDead = fatalSession.snapshot()
        assertEquals(ScreenId.DEFEAT, stillDead.screen)
        assertEquals(0, stillDead.stamina)
        assertFalse(stillDead.canStartLevel)

        // А «Новая игра» стирает старую долю восстановления и ставит 120/120
        fatalSession.startNewGame()
        val freshGame = fatalSession.snapshot()
        assertEquals(120, freshGame.stamina)
        assertEquals(120, freshGame.staminaCeiling)
        assertEquals(0L, freshGame.recoveryProgressMillis)
    }

    @Test
    fun `подпись состояния берётся от доли запаса к текущему потолку без округления процентов заранее, при потолке 120 границы 90 60 и 30, 60 из 120 и 75 из 150 оба Навеселе, и обновляется при изменении запаса или потолка`() {
        fun stateOf(stamina: Int, ceiling: Int): CounterSnapshot = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            ),
            initialStamina = stamina,
            initialCeiling = ceiling
        ).snapshot()

        // При потолке 120 границы положительных состояний — 90, 60 и 30
        assertEquals("Трезв", stateOf(120, 120).stateLabel)
        assertEquals(IntoxicationState.SOBER, stateOf(120, 120).intoxicationState)
        assertEquals("Трезв", stateOf(90, 120).stateLabel)
        assertEquals(IntoxicationState.SOBER, stateOf(90, 120).intoxicationState)

        assertEquals("Навеселе", stateOf(89, 120).stateLabel)
        assertEquals(IntoxicationState.TIPSY, stateOf(89, 120).intoxicationState)
        assertEquals("Навеселе", stateOf(60, 120).stateLabel)
        assertEquals(IntoxicationState.TIPSY, stateOf(60, 120).intoxicationState)

        assertEquals("Пьян", stateOf(59, 120).stateLabel)
        assertEquals(IntoxicationState.DRUNK, stateOf(59, 120).intoxicationState)
        assertEquals("Пьян", stateOf(30, 120).stateLabel)
        assertEquals(IntoxicationState.DRUNK, stateOf(30, 120).intoxicationState)

        assertEquals("В хлам", stateOf(29, 120).stateLabel)
        assertEquals(IntoxicationState.WASTED, stateOf(29, 120).intoxicationState)
        assertEquals("В хлам", stateOf(1, 120).stateLabel)
        assertEquals(IntoxicationState.WASTED, stateOf(1, 120).intoxicationState)

        // Запасы 60/120 и 75/150 оба читаются как «Навеселе»
        assertEquals("Навеселе", stateOf(60, 120).stateLabel)
        assertEquals("Навеселе", stateOf(75, 150).stateLabel)
        assertEquals(stateOf(60, 120).intoxicationState, stateOf(75, 150).intoxicationState)

        // Без округления процентов заранее:
        // 93/125 = 74.4% -> строго меньше 75%, поэтому «Навеселе», хотя при округлении до ближайшего было бы 74%,
        // а 97/130 = 74.615% -> при округлении до целых процентов дало бы 75%, но без округления < 75% -> «Навеселе»!
        assertEquals("Навеселе", stateOf(97, 130).stateLabel)
        assertEquals("Трезв", stateOf(98, 130).stateLabel)
        // 64/129 = 49.61% -> при округлении дало бы 50%, но без округления < 50% -> «Пьян»!
        assertEquals("Пьян", stateOf(64, 129).stateLabel)
        assertEquals("Навеселе", stateOf(65, 129).stateLabel)
        // 32/129 = 24.806% -> при округлении дало бы 25%, но без округления < 25% -> «В хлам»!
        assertEquals("В хлам", stateOf(32, 129).stateLabel)
        assertEquals("Пьян", stateOf(33, 129).stateLabel)

        // Подпись обновляется при изменении запаса (списание, восстановление) или потолка (прибавка бара)
        val liveSession = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                emptyList(),
                emptyList()
            ),
            initialStamina = 90,
            initialCeiling = 120
        )
        assertEquals("Трезв", liveSession.snapshot().stateLabel)

        // Списание 1 единицы (90 -> 89 из 120) сразу переводит подпись в «Навеселе»
        liveSession.tapBottle(1)
        liveSession.tapBottle(2)
        liveSession.tapBottle(1)
        liveSession.tapBottle(0)
        assertEquals(89, liveSession.snapshot().stamina)
        assertEquals("Навеселе", liveSession.snapshot().stateLabel)

        // Восстановление 1 единицы за 5 минут (89 -> 90 из 120) возвращает «Трезв»
        liveSession.advanceTimeMinutes(5)
        assertEquals(90, liveSession.snapshot().stamina)
        assertEquals("Трезв", liveSession.snapshot().stateLabel)
    }

    @Test
    fun `на полосе прилавка видны запас с потолком и подпись состояния без отдельной шкалы опьянения и без персонажа на экране, фон качается и вывеска пульсирует сильнее от Трезв к В хлам, а бутылки порции числа кнопки и правила переливания от состояния не зависят`() {
        fun createSessionAt(stamina: Int): LevelSession = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL, Drink.APEROL),
                emptyList(),
                emptyList()
            ),
            initialStamina = stamina,
            initialCeiling = 120
        )

        val soberSnap = createSessionAt(120).snapshot()
        val tipsySnap = createSessionAt(80).snapshot()
        val drunkSnap = createSessionAt(45).snapshot()
        val wastedSnap = createSessionAt(15).snapshot()

        // 1. На полосе прилавка пара «запас/потолок» и подпись состояния; отдельной шкалы опьянения и персонажа нет
        assertEquals("120/120", soberSnap.enduranceText)
        assertEquals("Трезв", soberSnap.stateLabel)
        assertEquals("80/120", tipsySnap.enduranceText)
        assertEquals("Навеселе", tipsySnap.stateLabel)
        assertEquals("45/120", drunkSnap.enduranceText)
        assertEquals("Пьян", drunkSnap.stateLabel)
        assertEquals("15/120", wastedSnap.enduranceText)
        assertEquals("В хлам", wastedSnap.stateLabel)

        for (snap in listOf(soberSnap, tipsySnap, drunkSnap, wastedSnap)) {
            assertFalse(snap.hasSeparateIntoxicationBar)
            assertFalse(snap.hasCharacterOnScreen)
            assertTrue(snap.bottlesAndPortionsStationary)
        }

        // 2. Фон качается и декоративная вывеска пульсирует сильнее от «Трезв» к «В хлам»
        assertTrue(soberSnap.backgroundSwayLevel < tipsySnap.backgroundSwayLevel)
        assertTrue(tipsySnap.backgroundSwayLevel < drunkSnap.backgroundSwayLevel)
        assertTrue(drunkSnap.backgroundSwayLevel < wastedSnap.backgroundSwayLevel)

        assertTrue(soberSnap.neonPulseLevel < tipsySnap.neonPulseLevel)
        assertTrue(tipsySnap.neonPulseLevel < drunkSnap.neonPulseLevel)
        assertTrue(drunkSnap.neonPulseLevel < wastedSnap.neonPulseLevel)

        // 3. Бутылки, порции, кнопки, цвета напитков и правила переливания во всех четырёх состояниях абсолютно одинаковы
        val statesStamina = listOf(120, 80, 45, 15)
        val expectedLabels = listOf("Трезв", "Навеселе", "Пьян", "В хлам")

        val finalBottlesAcrossStates = statesStamina.zip(expectedLabels).map { (startStamina, expectedLabel) ->
            val s = createSessionAt(startStamina)
            assertEquals(expectedLabel, s.snapshot().stateLabel)

            // Делаем одну и ту же серию тапов и переливов
            s.tapBottle(0) // выбираем бутылку 0
            assertTrue(s.snapshot().bottles[0].selected)
            s.tapBottle(2) // перелив 2 x Guinness в пустую бутылку 2

            s.tapBottle(1)
            s.tapBottle(3) // перелив 2 x Aperol в пустую бутылку 3

            s.tapBottle(1)
            s.tapBottle(2) // доливаем 3-й Guinness в бутылку 2 (сборка Guinness, -1 выносливость)

            s.tapBottle(0)
            s.tapBottle(3) // доливаем 3-й Aperol в бутылку 3 (сборка Aperol, -2 выносливости -> Победа!)

            val endSnap = s.snapshot()
            assertEquals(4, endSnap.moves)
            assertEquals(startStamina - 3, endSnap.stamina)
            assertNotNull(endSnap.victoryBanner)
            assertEquals("Победа", endSnap.victoryBanner!!.title)
            assertEquals(4, endSnap.victoryBanner!!.moves)
            endSnap.bottles
        }

        val referenceBottles = finalBottlesAcrossStates.first()
        for (bottles in finalBottlesAcrossStates) {
            assertEquals(referenceBottles, bottles)
        }
    }

    @Test
    fun `у бара с первого визита открыт только первый уровень, победа даже с 1 звездой открывает непосредственно следующий, повторная победа в раннем уровне не открывает уровень через один, а гибель ничего не открывает и уже открытый уровень остаётся открытым`() {
        val level1 = LevelLayout(
            levelNumber = 1,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )
        val level2 = LevelLayout(
            levelNumber = 2,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.APEROL),
                listOf(Drink.APEROL, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )
        val level3 = LevelLayout(
            levelNumber = 3,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CAMPARI, Drink.CAMPARI),
                listOf(Drink.CAMPARI, Drink.BOLS_BLUE_CURACAO),
                listOf(Drink.BOLS_BLUE_CURACAO, Drink.BOLS_BLUE_CURACAO),
                emptyList()
            )
        )

        val session = LevelSession(
            levels = listOf(level1, level2, level3),
            streetName = "Зыбицкая",
            barName = "Under",
            startOnFilmstrip = true
        )

        // С первого визита открыт только первый уровень (ярко), уровни 2 и 3 закрыты (тусклые)
        val initialFilm = session.snapshot()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, initialFilm.screen)
        assertEquals(3, initialFilm.levelCards.size)
        assertEquals(listOf(true, false, false), initialFilm.levelCards.map { it.unlocked })
        assertEquals(listOf(true, false, false), initialFilm.levelCards.map { it.bright })
        assertEquals(listOf(0, 0, 0), initialFilm.levelCards.map { it.bestStars })

        // Тап по закрытым карточкам 2 и 3 уровень не начинает
        assertFalse(session.tapLevelCard(2))
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)
        assertFalse(session.tapLevelCard(3))
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)

        // Тап по открытому уровню 1 начинает заход; выигрываем его на 1 звезду (лишняя трата 3 за пустую бутылку 0 с Chartreuse Verte)
        assertTrue(session.tapLevelCard(1))
        assertEquals(ScreenId.COUNTER, session.snapshot().screen)
        assertEquals(1, session.snapshot().levelNumber)

        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(0) // собрали бутылку 0 (Chartreuse, цена 3)
        session.tapBottle(0)
        session.tapBottle(1) // перелили в 1 -> бутылка 0 пуста и оплачена (трата 3)
        session.tapBottle(3)
        session.tapBottle(2) // победа с 1 звездой!

        val afterLevel1Win = session.snapshot()
        assertEquals(ScreenId.VICTORY_BANNER, afterLevel1Win.screen)
        assertEquals(1, afterLevel1Win.victoryBanner!!.stars)
        assertEquals(listOf(true, true, false), afterLevel1Win.levelCards.map { it.unlocked })
        assertEquals(listOf(1, 0, 0), afterLevel1Win.levelCards.map { it.bestStars })

        // Возвращаемся на плёнку и снова проходим уровень 1 (повторная победа на 3 звезды): уровень 3 через непройденный уровень 2 НЕ открывается!
        session.tapToLevels()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)

        assertTrue(session.tapLevelCard(1))
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0) // чистая победа на 3 звезды в уровне 1
        assertEquals(3, session.snapshot().victoryBanner!!.stars)

        session.tapToLevels()
        val afterReplayLevel1 = session.snapshot()
        assertEquals(listOf(true, true, false), afterReplayLevel1.levelCards.map { it.unlocked })
        assertEquals(listOf(3, 0, 0), afterReplayLevel1.levelCards.map { it.bestStars })

        // Проходим уровень 2 на 3 звезды -> открывается уровень 3
        assertTrue(session.tapLevelCard(2))
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0)
        session.tapToLevels()
        assertEquals(listOf(true, true, true), session.snapshot().levelCards.map { it.unlocked })
        assertEquals(listOf(3, 3, 0), session.snapshot().levelCards.map { it.bestStars })

        // Худший повтор в уровне 2 (на 2 звезды) не закрывает уже открытый уровень 3 и не снижает лучшие звёзды уровня 2
        assertTrue(session.tapLevelCard(2))
        session.tapBottle(1)
        session.tapBottle(3)
        session.tapBottle(1)
        session.tapBottle(0)
        session.tapBottle(0)
        session.tapBottle(1) // трата 2 за Aperol -> 2 звезды
        session.tapBottle(3)
        session.tapBottle(2)
        assertEquals(2, session.snapshot().victoryBanner!!.stars)
        session.tapToLevels()
        assertEquals(listOf(true, true, true), session.snapshot().levelCards.map { it.unlocked })
        assertEquals(listOf(3, 3, 0), session.snapshot().levelCards.map { it.bestStars })

        // Гибель на победном ходе ничего не открывает
        val fatalSession = LevelSession(
            levels = listOf(level1, level2),
            initialStamina = 1,
            startOnFilmstrip = true
        )
        assertTrue(fatalSession.tapLevelCard(1))
        fatalSession.tapBottle(1)
        fatalSession.tapBottle(2) // тратит последнюю единицу выносливости 1 -> 0 -> гибель!
        val defeatedSnap = fatalSession.snapshot()
        assertEquals(ScreenId.DEFEAT, defeatedSnap.screen)
        assertEquals(listOf(true, false), defeatedSnap.levelCards.map { it.unlocked })
        assertEquals(listOf(0, 0), defeatedSnap.levelCards.map { it.bestStars })
    }

    @Test
    fun `Назад с прилавка и системная Назад открывают плёнку и сохраняют незавершённый заход — расстановку, счётчик, историю отмены и отметки списания без выбора бутылки, а на карточке плёнки незавершённый заход не обозначен и лучшие звёзды остаются`() {
        val level1 = LevelLayout(
            levelNumber = 1,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.APEROL),
                listOf(Drink.APEROL, Drink.APEROL),
                emptyList()
            )
        )
        val level2 = LevelLayout(
            levelNumber = 2,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CAMPARI, Drink.CAMPARI),
                listOf(Drink.CAMPARI, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )
        val session = LevelSession(levels = listOf(level1, level2))

        // Проходим уровень 1 на 3 звезды
        session.tapBottle(1)
        session.tapBottle(2) // Aperol собран в бутылке 2 (-2)
        session.tapBottle(1)
        session.tapBottle(0) // Guinness собран в бутылке 0 (-1) -> Победа на 3 звезды!
        assertEquals(ScreenId.VICTORY_BANNER, session.snapshot().screen)
        assertEquals(3, session.snapshot().bestStars)
        assertEquals("К уровням", session.snapshot().victoryBanner!!.toLevelsButtonLabel)
        assertEquals(ActionButtonSnapshot(title = "К уровням", enabled = true), session.snapshot().victoryBanner!!.toLevelsButton)

        // «К уровням» и системная «Назад» с плашки победы только открывают плёнку и НЕ начинают следующий уровень сами
        session.tapToLevels()
        val filmAfterWin = session.snapshot()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, filmAfterWin.screen)
        assertEquals(3, filmAfterWin.levelCards[0].bestStars)
        assertTrue(filmAfterWin.levelCards[1].unlocked)
        assertEquals(0, filmAfterWin.levelCards[1].bestStars)

        // Следующий тап по выигранному уровню 1 начинает НОВЫЙ заход со стартовой расстановки, и бутылки снова могут списывать выносливость
        val staminaBeforeReplay = filmAfterWin.stamina
        assertTrue(session.tapLevelCard(1))
        val replayStart = session.snapshot()
        assertEquals(ScreenId.COUNTER, replayStart.screen)
        assertEquals(0, replayStart.moves)
        assertNull(replayStart.victoryBanner)
        assertEquals(emptySet<Int>(), replayStart.chargedBottleIndices)
        assertEquals(level1.initialBottles, replayStart.bottles.map { it.portions })

        // Делаем 1 ход со сборкой бутылки 2 (Aperol, цена 2) и выбираем бутылку 1 без перелива
        session.tapBottle(1)
        session.tapBottle(2) // ход 1, списано 2 за бутылку 2
        session.tapBottle(1) // выбираем бутылку 1
        val midReplay = session.snapshot()
        assertEquals(1, midReplay.moves)
        assertEquals(staminaBeforeReplay - 2, midReplay.stamina)
        assertTrue(midReplay.bottles[2].charged)
        assertTrue(midReplay.bottles[1].selected)

        // Выходим экранной кнопкой «Назад» на плёнку: заход сохраняется, на карточке он НЕ обозначен, а лучшие звёзды (3) остаются!
        session.tapBack()
        val filmWithUnfinished = session.snapshot()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, filmWithUnfinished.screen)
        assertEquals(3, filmWithUnfinished.levelCards[0].bestStars)
        assertFalse(filmWithUnfinished.levelCards[0].showsUnfinishedRunIndicator)
        assertFalse(filmWithUnfinished.levelCards[0].unfinishedRunMarked)
        assertNull(filmWithUnfinished.restartButton)

        // Тап по уровню 1 продолжает незавершённый заход: расстановка, счётчик (1), история отмены и отметка списания на месте, а выбор бутылки снят!
        assertTrue(session.tapLevelCard(1))
        val resumed = session.snapshot()
        assertEquals(ScreenId.COUNTER, resumed.screen)
        assertEquals(1, resumed.moves)
        assertEquals(midReplay.bottles.map { it.portions }, resumed.bottles.map { it.portions })
        assertTrue(resumed.bottles.none { it.selected })
        assertTrue(resumed.bottles[2].charged)
        assertTrue(resumed.undoButton!!.enabled)
        assertTrue(resumed.restartButton!!.enabled)
        assertEquals(3, resumed.bestStars)

        // Системная «Назад» с прилавка тоже открывает плёнку и сохраняет заход
        session.tapBottle(0) // снова выбираем бутылку 0
        session.systemBack()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)
        assertTrue(session.selectLevel(1))
        val resumedAfterSystemBack = session.snapshot()
        assertEquals(1, resumedAfterSystemBack.moves)
        assertTrue(resumedAfterSystemBack.bottles.none { it.selected })
        assertTrue(resumedAfterSystemBack.bottles[2].charged)

        // История отмены работает после возвращения из плёнки
        session.undoMove()
        assertEquals(0, session.snapshot().moves)
        assertEquals(level1.initialBottles, session.snapshot().bottles.map { it.portions })
        assertTrue(session.snapshot().bottles[2].charged)
    }

    @Test
    fun `если отмены или Сначала уже вернули стартовую расстановку но отметки списания есть, выход на плёнку сохраняет заход и повторный вход не списывает отмеченные бутылки снова`() {
        val level1 = LevelLayout(
            levelNumber = 1,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList()
            )
        )
        val session = LevelSession(levels = listOf(level1))

        // 1. Случай с «Отмена хода» до стартовой расстановки:
        // Ход 1: Guinness из 1 в 2 -> бутылка 2 собрана (списана 1 единица: 120 -> 119, бутылка 2 отмечена)
        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(119, session.snapshot().stamina)
        assertTrue(session.snapshot().bottles[2].charged)

        // Отменяем единственный ход: на прилавке стартовая расстановка, счётчик 0, история пуста, но отметка списания на бутылке 2 есть!
        session.undoMove()
        val atStartWithCharge = session.snapshot()
        assertEquals(0, atStartWithCharge.moves)
        assertFalse(atStartWithCharge.undoButton!!.enabled)
        assertEquals(level1.initialBottles, atStartWithCharge.bottles.map { it.portions })
        assertTrue(atStartWithCharge.bottles[2].charged)

        // Выходим на плёнку и снова входим в уровень 1
        session.tapBack()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)
        assertTrue(session.tapLevelCard(1))

        val reenteredAfterUndo = session.snapshot()
        assertEquals(ScreenId.COUNTER, reenteredAfterUndo.screen)
        assertEquals(0, reenteredAfterUndo.moves)
        assertTrue(reenteredAfterUndo.bottles[2].charged)
        assertEquals(setOf(2), reenteredAfterUndo.chargedBottleIndices)

        // Повторно собираем бутылку 2 — выносливость НЕ списывается снова (остаётся 119)!
        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(119, session.snapshot().stamina)

        // 2. Случай с кнопкой «Сначала» до стартовой расстановки:
        session.restartRun()
        assertEquals(0, session.snapshot().moves)
        assertTrue(session.snapshot().bottles[2].charged)

        session.systemBack()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)
        assertTrue(session.tapLevelCard(1))

        val reenteredAfterRestart = session.snapshot()
        assertTrue(reenteredAfterRestart.bottles[2].charged)
        session.tapBottle(1)
        session.tapBottle(2)
        assertEquals(119, session.snapshot().stamina)
    }

    @Test
    fun `у разных уровней бара незавершённые заходы хранятся одновременно, не больше одного на уровень, а победа на одном уровне не стирает заход другого`() {
        val level1 = LevelLayout(
            levelNumber = 1,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )
        val level2 = LevelLayout(
            levelNumber = 2,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.APEROL, Drink.APEROL),
                listOf(Drink.APEROL, Drink.CAMPARI),
                listOf(Drink.CAMPARI, Drink.CAMPARI),
                emptyList()
            )
        )
        val level3 = LevelLayout(
            levelNumber = 3,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.BOLS_BLUE_CURACAO, Drink.BOLS_BLUE_CURACAO),
                listOf(Drink.BOLS_BLUE_CURACAO, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                emptyList()
            )
        )
        val session = LevelSession(levels = listOf(level1, level2, level3))

        // Сначала открываем уровни 2 и 3 победами в 1 и 2
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0)
        session.tapToLevels()

        assertTrue(session.tapLevelCard(2))
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0)
        session.tapToLevels()

        assertEquals(listOf(true, true, true), session.snapshot().levelCards.map { it.unlocked })

        // Начинаем повтор уровня 1, делаем 1 ход (сборка бутылки 2) и уходим на плёнку
        assertTrue(session.tapLevelCard(1))
        session.tapBottle(1)
        session.tapBottle(2)
        val level1MidBottles = session.snapshot().bottles.map { it.portions }
        session.tapBack()

        // Начинаем повтор уровня 2, делаем 1 ход (вылив в пустую бутылку 3 без сборки) и уходим на плёнку
        assertTrue(session.tapLevelCard(2))
        session.tapBottle(1)
        session.tapBottle(3)
        val level2MidBottles = session.snapshot().bottles.map { it.portions }
        session.tapBack()

        // Открываем уровень 3 и выигрываем его до конца
        assertTrue(session.tapLevelCard(3))
        session.tapBottle(1)
        session.tapBottle(2)
        session.tapBottle(1)
        session.tapBottle(0)
        assertEquals(ScreenId.VICTORY_BANNER, session.snapshot().screen)
        // Системная «Назад» с плашки победы открывает плёнку
        session.systemBack()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)

        // Возвращаемся в уровень 1 — его незавершённый заход на месте!
        assertTrue(session.tapLevelCard(1))
        val resumed1 = session.snapshot()
        assertEquals(1, resumed1.levelNumber)
        assertEquals(1, resumed1.moves)
        assertEquals(level1MidBottles, resumed1.bottles.map { it.portions })
        assertTrue(resumed1.bottles[2].charged)
        session.tapBack()

        // Возвращаемся в уровень 2 — его незавершённый заход тоже на месте!
        assertTrue(session.tapLevelCard(2))
        val resumed2 = session.snapshot()
        assertEquals(2, resumed2.levelNumber)
        assertEquals(1, resumed2.moves)
        assertEquals(level2MidBottles, resumed2.bottles.map { it.portions })
        assertTrue(resumed2.undoButton!!.enabled)
        session.tapBack()

        // А в выигранном уровне 3 следующий тап начинает новый заход со стартовой расстановки
        assertTrue(session.tapLevelCard(3))
        val fresh3 = session.snapshot()
        assertEquals(3, fresh3.levelNumber)
        assertEquals(0, fresh3.moves)
        assertEquals(level3.initialBottles, fresh3.bottles.map { it.portions })
        assertTrue(fresh3.bottles.none { it.charged })
    }

    @Test
    fun `на плёнке видны запас с потолком, подпись состояния и путь без кнопки Сначала, начать уровень при запасе 0 нельзя, а верхняя полоса прилавка называет улицу бар и номер уровня и постоянные расстановки бара решаемы`() {
        val session = LevelSession(
            streetName = "Зыбицкая",
            barName = "Under",
            initialStamina = 75,
            initialCeiling = 120,
            startOnFilmstrip = true
        )

        // На плёнке видна полоса «запас/потолок», состояние и путь, кнопки «Сначала» и «Отмена хода» отсутствуют
        val filmSnap = session.snapshot()
        assertEquals(ScreenId.LEVEL_FILMSTRIP, filmSnap.screen)
        assertEquals("75/120", filmSnap.enduranceText)
        assertEquals("Навеселе", filmSnap.stateLabel)
        assertEquals("Зыбицкая · Under", filmSnap.pathText)
        assertEquals("75/120", filmSnap.filmstrip.enduranceText)
        assertEquals("Навеселе", filmSnap.filmstrip.stateLabel)
        assertEquals("Зыбицкая · Under", filmSnap.filmstrip.pathText)
        assertNull(filmSnap.restartButton)
        assertNull(filmSnap.undoButton)
        assertNull(filmSnap.filmstrip.restartButton)
        assertEquals(8, filmSnap.levelCards.size)

        // Входим в уровень 1: верхняя полоса прилавка называет улицу, бар и номер уровня
        assertTrue(session.tapLevelCard(1))
        val counterSnap = session.snapshot()
        assertEquals(ScreenId.COUNTER, counterSnap.screen)
        assertEquals("Зыбицкая", counterSnap.streetName)
        assertEquals("Under", counterSnap.barName)
        assertEquals(1, counterSnap.levelNumber)
        assertEquals("Зыбицкая · Under · Уровень 1", counterSnap.pathText)
        assertEquals("75/120", counterSnap.enduranceText)
        assertEquals("Навеселе", counterSnap.stateLabel)

        // Проверяем все постоянные расстановки бара по умолчанию:
        // у каждой ровно две пустые бутылки, остальные полные и смешанные, все порции видимы, и прогон по порядку открывает и решает все уровни бара
        val solveAllSession = LevelSession(startOnFilmstrip = true)
        assertEquals(8, solveAllSession.levels.size)

        for ((idx, layout) in solveAllSession.levels.withIndex()) {
            val levelNum = idx + 1
            assertEquals(levelNum, layout.levelNumber)
            val emptyCount = layout.initialBottles.count { it.isEmpty() }
            assertEquals("На уровне $levelNum должно быть ровно 2 пустые бутылки", 2, emptyCount)
            val nonEmpty = layout.initialBottles.filter { it.isNotEmpty() }
            assertTrue(nonEmpty.all { it.size == layout.capacity })
            assertTrue(nonEmpty.all { it.toSet().size >= 2 })

            // До победы открыт текущий уровень, а следующий (если есть) ещё закрыт
            val filmBefore = solveAllSession.snapshot()
            assertEquals(ScreenId.LEVEL_FILMSTRIP, filmBefore.screen)
            assertTrue(filmBefore.levelCards[idx].unlocked)
            if (idx + 1 < solveAllSession.levels.size) {
                assertFalse(filmBefore.levelCards[idx + 1].unlocked)
            }

            assertTrue(solveAllSession.tapLevelCard(levelNum))
            assertEquals("Зыбицкая · Under · Уровень $levelNum", solveAllSession.snapshot().pathText)

            for ((from, to) in layout.solutionMoves) {
                solveAllSession.tapBottle(from)
                solveAllSession.tapBottle(to)
            }
            val wonSnap = solveAllSession.snapshot()
            assertEquals(ScreenId.VICTORY_BANNER, wonSnap.screen)
            assertEquals(layout.parMoves, wonSnap.moves)
            assertEquals(3, wonSnap.victoryBanner!!.stars)

            solveAllSession.tapToLevels()
            assertEquals(3, solveAllSession.snapshot().levelCards[idx].bestStars)
        }

        // При запасе 0 начать уровень с плёнки нельзя
        val zeroSession = LevelSession(initialStamina = 0, startOnFilmstrip = true)
        assertFalse(zeroSession.canStartLevel())
        assertFalse(zeroSession.tapLevelCard(1))
        assertEquals(ScreenId.DEFEAT, zeroSession.snapshot().screen)
    }

    @Test
    fun `пока хотя бы у одного уровня бара нет звезды бар ничего не начислил, в момент первой победы закрывающей все уровни бара начисляется округлённое по целым среднее со значением 0_5 вверх, а при повторе доплачивается разница и начисленное не уменьшается`() {
        fun makeLevel(num: Int): LevelLayout = LevelLayout(
            levelNumber = num,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.CHARTREUSE_VERTE, Drink.CHARTREUSE_VERTE),
                listOf(Drink.CHARTREUSE_VERTE, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            )
        )

        fun playLevelForStars(session: LevelSession, levelNum: Int, targetStars: Int) {
            if (session.snapshot().screen != ScreenId.LEVEL_FILMSTRIP) {
                session.tapToLevels()
            }
            assertTrue(session.tapLevelCard(levelNum))
            when (targetStars) {
                3 -> {
                    // Ход 1: Guinness (1) в бутылку 2 -> списано 1 (бутылка 2 полна)
                    session.tapBottle(1)
                    session.tapBottle(2)
                    // Ход 2: Chartreuse (3) в бутылку 0 -> списано 3 (бутылка 0 полна), лишняя трата 0 -> 3 звезды
                    session.tapBottle(1)
                    session.tapBottle(0)
                }
                2 -> {
                    // Ход 1: Guinness (1) в бутылку 2 -> списано 1 с бутылки 2
                    session.tapBottle(1)
                    session.tapBottle(2)
                    // Ход 2: перелить Guinness из 2 в 3 -> списано 1 с бутылки 3, бутылка 2 пуста (лишняя трата 1)
                    session.tapBottle(2)
                    session.tapBottle(3)
                    // Ход 3: Chartreuse (3) в бутылку 0 -> списано 3, победа с лишней тратой 1 -> 2 звезды
                    session.tapBottle(1)
                    session.tapBottle(0)
                }
                1 -> {
                    // Ход 1: Guinness в пустую бутылку 3 (без сборки)
                    session.tapBottle(1)
                    session.tapBottle(3)
                    // Ход 2: Chartreuse в бутылку 0 -> списано 3 с бутылки 0
                    session.tapBottle(1)
                    session.tapBottle(0)
                    // Ход 3: перелить Chartreuse из 0 в 1 -> списано 3 с бутылки 1, бутылка 0 пуста (лишняя трата 3)
                    session.tapBottle(0)
                    session.tapBottle(1)
                    // Ход 4: Guinness из 3 в 2 -> списано 1 с бутылки 2, победа с лишней тратой 3 -> 1 звезда
                    session.tapBottle(3)
                    session.tapBottle(2)
                }
            }
            assertEquals(ScreenId.VICTORY_BANNER, session.snapshot().screen)
            assertEquals(targetStars, session.snapshot().victoryBanner!!.stars)
        }

        // Разбор примера из спеки: 8 уровней со звёздами 3, 3, 3, 3, 2, 2, 1, 1 (среднее 18/8 = 2,25 -> прибавка 2),
        // затем повтор уровней 7 и 8 до 2 звёзд даёт 3, 3, 3, 3, 3, 3, 2, 2 (среднее 22/8 = 2,75 -> доплата +1 до 3)
        val eightLevels = (1..8).map { makeLevel(it) }
        val session = LevelSession(
            levels = eightLevels,
            streetName = "Зыбицкая",
            barName = "Under",
            initialStamina = 120,
            initialCeiling = 120,
            initialRecoveryProgressMillis = 180_000L, // 3 минуты накопленной доли восстановления
            startOnFilmstrip = true
        )

        val firstPassStars = listOf(3, 3, 3, 3, 2, 2, 1, 1)
        // Проходим первые 7 уровней: у 8-го уровня ещё нет звезды (0), поэтому бар ничего не начислил
        for (i in 0 until 7) {
            playLevelForStars(session, i + 1, firstPassStars[i])
            val snapAfterLevel = session.snapshot()
            assertEquals(0, snapAfterLevel.awardedBarBonus)
            assertEquals(0, snapAfterLevel.barAwardedBonus)
            assertEquals(mapOf("Under" to 0), snapAfterLevel.awardedBarBonuses)
            assertEquals(120, snapAfterLevel.staminaCeiling)
            assertEquals(180_000L, snapAfterLevel.recoveryProgressMillis)
        }

        // Снимок до первой полной победы (после 7 уровней, перед 8-м):
        val beforeEighth = session.snapshot()
        val staminaBeforeEighth = beforeEighth.stamina
        assertEquals(120, beforeEighth.staminaCeiling)
        assertEquals(0, beforeEighth.awardedBarBonus)
        assertEquals(listOf(3, 3, 3, 3, 2, 2, 1, 0), beforeEighth.levelCards.map { it.bestStars })

        // Проходим 8-й уровень на 1 звезду (тратит 3 + 3 + 1 = 7 единиц выносливости):
        // В момент победы звезда впервые есть у каждого из 8 уровней (3, 3, 3, 3, 2, 2, 1, 1 -> сумма 18, среднее 2,25 -> прибавка 2).
        // Списание за бутылки (-7) уже прошло, и сразу начисляется прибавка +2 к запасу и потолку!
        playLevelForStars(session, 8, 1)
        val afterFirstFullVictory = session.snapshot()
        assertEquals(listOf(3, 3, 3, 3, 2, 2, 1, 1), afterFirstFullVictory.levelCards.map { it.bestStars })
        assertEquals(2, afterFirstFullVictory.awardedBarBonus)
        assertEquals(2, afterFirstFullVictory.barAwardedBonus)
        assertEquals(2, afterFirstFullVictory.filmstrip.awardedBarBonus)
        assertEquals(mapOf("Under" to 2), afterFirstFullVictory.awardedBarBonuses)
        assertEquals(122, afterFirstFullVictory.staminaCeiling)
        assertEquals(122, afterFirstFullVictory.maxEndurance)
        assertEquals(staminaBeforeEighth - 7 + 2, afterFirstFullVictory.stamina)
        assertEquals(180_000L, afterFirstFullVictory.recoveryProgressMillis) // доля восстановления сохранена!

        // Повтор 5-го уровня с 2 до 3 звёзд: звёзды 3, 3, 3, 3, 3, 2, 1, 1 (сумма 19, среднее 2,375 -> округление 2, доплаты ещё нет)
        val staminaBefore5 = afterFirstFullVictory.stamina
        playLevelForStars(session, 5, 3) // тратит 4 выносливости
        val afterLevel5Replay = session.snapshot()
        assertEquals(listOf(3, 3, 3, 3, 3, 2, 1, 1), afterLevel5Replay.levelCards.map { it.bestStars })
        assertEquals(2, afterLevel5Replay.awardedBarBonus)
        assertEquals(122, afterLevel5Replay.staminaCeiling)
        assertEquals(staminaBefore5 - 4, afterLevel5Replay.stamina)

        // Повтор 6-го уровня с 2 до 3 звёзд: звёзды 3, 3, 3, 3, 3, 3, 1, 1 (сумма 20, среднее ровно 2,5 -> дробная часть 0,5 поднимает вверх до 3!).
        // Сразу после этого захода доплачивается разница +1 (с 2 до 3)!
        val staminaBefore6 = afterLevel5Replay.stamina
        playLevelForStars(session, 6, 3) // тратит 4 выносливости, доплата +1
        val afterHalfRoundingUp = session.snapshot()
        assertEquals(listOf(3, 3, 3, 3, 3, 3, 1, 1), afterHalfRoundingUp.levelCards.map { it.bestStars })
        assertEquals(3, afterHalfRoundingUp.awardedBarBonus)
        assertEquals(123, afterHalfRoundingUp.staminaCeiling)
        assertEquals(staminaBefore6 - 4 + 1, afterHalfRoundingUp.stamina)
        assertEquals(180_000L, afterHalfRoundingUp.recoveryProgressMillis)

        // Поднимаем уровни 7 и 8 до 2 звёзд -> 3, 3, 3, 3, 3, 3, 2, 2 (среднее 2,75 -> округление 3, уже начислено 3, больше не доплачивает)
        playLevelForStars(session, 7, 2)
        playLevelForStars(session, 8, 2)
        val after275 = session.snapshot()
        assertEquals(listOf(3, 3, 3, 3, 3, 3, 2, 2), after275.levelCards.map { it.bestStars })
        assertEquals(3, after275.awardedBarBonus)
        assertEquals(123, after275.staminaCeiling)

        // Слабый повтор 1-го уровня на 1 звезду не уменьшает ни записанные звёзды, ни уже начисленную прибавку бара
        val staminaBeforeWeakReplay = after275.stamina
        playLevelForStars(session, 1, 1) // тратит 7 выносливости
        val afterWeakReplay = session.snapshot()
        assertEquals(3, afterWeakReplay.awardedBarBonus)
        assertEquals(123, afterWeakReplay.staminaCeiling)
        assertEquals(staminaBeforeWeakReplay - 7, afterWeakReplay.stamina)
    }

    @Test
    fun `стоящий на потолке перед последней победой с бесплатными отмеченными бутылками оказывается на новом потолке, доля восстановления сохраняется, потолок равен 120 плюс сумма прибавок баров до 165 без отдельного капа, а ход опустивший запас до 0 прибавку не начисляет и персонажа от нуля не спасает`() {
        val level1 = LevelLayout(
            levelNumber = 1,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )
        val level2 = LevelLayout(
            levelNumber = 2,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                listOf(Drink.PILSNER_URQUELL, Drink.PILSNER_URQUELL),
                emptyList()
            )
        )

        // 1. Кто стоял на старом потолке в момент победы с прибавкой бара, оказывается на новом потолке, а доля пути к следующей единице сохраняется!
        val session = LevelSession(
            levels = listOf(level1, level2),
            barName = "Under",
            initialStamina = 120,
            initialCeiling = 120,
            initialRecoveryProgressMillis = 240_000L, // 4 минуты накопленной доли восстановления
            startOnFilmstrip = true
        )

        // Проходим уровень 1 на 3 звезды (списано 2: 120 -> 118, прибавки ещё нет, т.к. уровень 2 = 0 звёзд)
        assertTrue(session.tapLevelCard(1))
        session.tapBottle(1)
        session.tapBottle(2) // Pilsner (-1)
        session.tapBottle(1)
        session.tapBottle(0) // Guinness (-1) -> победа в 1-м уровне!
        session.tapToLevels()
        assertEquals(118, session.snapshot().stamina)
        assertEquals(120, session.snapshot().staminaCeiling)
        assertEquals(0, session.snapshot().awardedBarBonus)

        // Начинаем уровень 2, собираем обе бутылки и отменяем последний ход: бутылки уже отмечены (оплачены), затем восстанавливаем запас до потолка 120/120 с долей 2 минуты (120_000 мс)
        assertTrue(session.tapLevelCard(2))
        session.tapBottle(1)
        session.tapBottle(2) // бутылка 2 оплачена (-1: 118 -> 117)
        session.tapBottle(1)
        session.tapBottle(0) // но чтобы не победить сразу, сначала выльем Pilsner в 3!
        // Сделаем проще: перезапустим в новом экземпляре с 2 уровнями, где на 2-м уровне бутылки оплачены до победы
        val ceilingSession = LevelSession(
            levels = listOf(level1, level2),
            barName = "Under",
            initialStamina = 120,
            initialCeiling = 120,
            startOnFilmstrip = true
        )
        assertTrue(ceilingSession.tapLevelCard(1))
        ceilingSession.tapBottle(1)
        ceilingSession.tapBottle(2)
        ceilingSession.tapBottle(1)
        ceilingSession.tapBottle(0)
        ceilingSession.tapToLevels()

        assertTrue(ceilingSession.tapLevelCard(2))
        ceilingSession.tapBottle(1)
        ceilingSession.tapBottle(3) // 1 Pilsner в 3 (бесплатно)
        ceilingSession.tapBottle(1)
        ceilingSession.tapBottle(0) // бутылка 0 собрана Guinness (-1)
        ceilingSession.undoMove()   // откат сборки бутылки 0 (она уже оплачена!)
        ceilingSession.tapBottle(3)
        ceilingSession.tapBottle(2) // бутылка 2 собрана Pilsner (-1), но бутылка 0 ещё не собрана!
        // Сейчас обе собираемые бутылки (0 и 2) уже оплачены! Восстанавливаем выносливость до потолка 120/120 + накапливаем долю до потолка:
        ceilingSession.advanceTimeMinutes(15) // запас = 119 -> накапливаем 3 минуты (180_000 мс) перед последней единицей:
        // Проверим точно: было 120 - 2 (ур.1) - 2 (ур.2) = 116. За 15 минут стало 119 и доля 0.
        assertEquals(119, ceilingSession.snapshot().stamina)
        ceilingSession.advanceTimeMinutes(3) // 119/120 и доля 180_000 мс
        assertEquals(180_000L, ceilingSession.snapshot().recoveryProgressMillis)
        // Если игрок стоял на старом потолке 120/120 прямо перед победным ходом (оплаченная бутылка 0 собирается бесплатно):
        ceilingSession.advanceTimeMinutes(2) // 120/120
        assertEquals(120, ceilingSession.snapshot().stamina)
        assertEquals(120, ceilingSession.snapshot().staminaCeiling)

        // Победный ход собирает уже оплаченную бутылку 0 бесплатно, закрывает бар на (3, 3) -> прибавка +3:
        // Игрок стоял на старом потолке 120/120 и сразу оказывается на новом потолке 123/123!
        ceilingSession.tapBottle(1)
        ceilingSession.tapBottle(0)
        val atNewCeiling = ceilingSession.snapshot()
        assertEquals(ScreenId.VICTORY_BANNER, atNewCeiling.screen)
        assertEquals(3, atNewCeiling.awardedBarBonus)
        assertEquals(123, atNewCeiling.stamina)
        assertEquals(123, atNewCeiling.staminaCeiling)
        assertEquals("123/123", atNewCeiling.enduranceText)

        // Восстановление останавливается на новом потолке 123, а когда списываем и копим — идёт вплоть до нового потолка 123
        ceilingSession.advanceTimeMinutes(20)
        assertEquals(123, ceilingSession.snapshot().stamina)

        // 2. Потолок равен 120 + сумма уже начисленных прибавок всех 15 баров (до 165 без отдельного потолка сверху)
        val all15Bars = listOf(
            "Under", "Койот", "Zodiac", "Рядом", "Белый Налив", "Горький", "На дне", "Матрешка",
            "Золотой лев", "Марвин", "«Damask» Kafe-Bar", "Аптека бар",
            "Киану", "Bro&n", "Зойка"
        )
        // "Under" уже начислил 3 (123/123). Начисляем по 3 за остальные 14 баров:
        for (bar in all15Bars.drop(1)) {
            ceilingSession.applyBarBonus(3, targetBarName = bar)
        }
        val max165 = ceilingSession.snapshot()
        assertEquals(15, max165.awardedBarBonuses.size)
        assertEquals(45, max165.awardedBarBonuses.values.sum())
        assertEquals(165, max165.stamina)
        assertEquals(165, max165.staminaCeiling)
        assertEquals("165/165", max165.enduranceText)

        // 3. Ход, который собирает последний уровень бара, но опускает запас до 0, открывает гибель ДО победы:
        // звёзды не пишет, прибавку бара не начисляет (и не поднимает персонажа с 0!).
        val fatalBarSession = LevelSession(
            levels = listOf(level1, level2),
            barName = "Under",
            initialStamina = 3, // 2 единицы уйдёт на уровень 1 (останется 1), и 1 единица на уровень 2 опустит в 0
            initialCeiling = 120,
            startOnFilmstrip = true
        )
        assertTrue(fatalBarSession.tapLevelCard(1))
        fatalBarSession.tapBottle(1)
        fatalBarSession.tapBottle(2) // -1 (3 -> 2)
        fatalBarSession.tapBottle(1)
        fatalBarSession.tapBottle(0) // -1 (2 -> 1) -> победа в уровне 1 при запасе 1!
        fatalBarSession.tapToLevels()
        assertEquals(1, fatalBarSession.snapshot().stamina)
        assertEquals(0, fatalBarSession.snapshot().awardedBarBonus)

        // В уровне 2 делаем так, чтобы последняя сборка стоила 1 и одновременно решала весь уровень:
        // Сначала выльем Pilsner в 3 (бесплатно), соберём Guinness в 0 (-1 -> запас 0 на неполном уровне) — а чтобы именно ПОБЕДНЫЙ ход опустил в 0:
        // Возьмём уровень 2 с одной собираемой бутылкой в 1 ход!
        val oneBottleSolveLevel2 = LevelLayout(
            levelNumber = 2,
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.GUINNESS_DRAUGHT),
                emptyList(),
                emptyList()
            )
        )
        val fatalOnWinningMoveSession = LevelSession(
            levels = listOf(level1, oneBottleSolveLevel2),
            barName = "Under",
            initialStamina = 3,
            initialCeiling = 120,
            startOnFilmstrip = true
        )
        assertTrue(fatalOnWinningMoveSession.tapLevelCard(1))
        fatalOnWinningMoveSession.tapBottle(1)
        fatalOnWinningMoveSession.tapBottle(2) // 3 -> 2
        fatalOnWinningMoveSession.tapBottle(1)
        fatalOnWinningMoveSession.tapBottle(0) // 2 -> 1 (уровень 1 пройден на 3 звезды, уровень 2 открыт)
        fatalOnWinningMoveSession.tapToLevels()

        assertTrue(fatalOnWinningMoveSession.tapLevelCard(2))
        // Этот ход собирает единственную бутылку уровня 2 (и закрыл бы весь бар на 3 звезды), НО опускает запас с 1 до 0!
        fatalOnWinningMoveSession.tapBottle(1)
        fatalOnWinningMoveSession.tapBottle(0)

        val defeatedOnFinalBarMove = fatalOnWinningMoveSession.snapshot()
        assertEquals(ScreenId.DEFEAT, defeatedOnFinalBarMove.screen)
        assertNull(defeatedOnFinalBarMove.victoryBanner)
        assertEquals(0, defeatedOnFinalBarMove.stamina)
        assertEquals(120, defeatedOnFinalBarMove.staminaCeiling)
        assertEquals(0, defeatedOnFinalBarMove.awardedBarBonus)
        assertEquals(listOf(3, 0), defeatedOnFinalBarMove.levelCards.map { it.bestStars })

        // «Новая игра» сбрасывает и звёзды уровней, и начисленные прибавки баров, и потолок в 120/120
        ceilingSession.startNewGame()
        val afterReset = ceilingSession.snapshot()
        assertEquals(120, afterReset.stamina)
        assertEquals(120, afterReset.staminaCeiling)
        assertEquals(0, afterReset.awardedBarBonus)
        assertEquals(mapOf("Under" to 0), afterReset.awardedBarBonuses)
        assertEquals(listOf(0, 0), afterReset.levelCards.map { it.bestStars })
    }

    @Test
    fun `живой запуск открывает мир на весь экран без колонки ступеней без свободного зума и без ступени страны, с тихими границами без дальних кусков, яркими городами с дробью звёзд и тусклыми городами без дроби`() {
        val session = LevelSession.createLiveGame()
        val snap = session.snapshot()

        assertEquals(ScreenId.WORLD_MAP, snap.screen)
        assertEquals("120/120", snap.enduranceText)
        assertEquals("Трезв", snap.stateLabel)
        assertEquals("Мир", snap.pathText)
        assertFalse(snap.hasBackButton)
        assertNull(snap.backButton)
        assertNull(snap.backButtonLabel)
        assertNull(snap.worldMapNotice)

        val world = snap.worldMap
        assertNotNull(world)
        assertFalse(world!!.hasStepColumn)
        assertFalse(world.hasFreeZoom)
        assertFalse(world.countryIsStep)
        assertTrue(world.usesCustomPixelMap)
        assertFalse(world.usesThirdPartyTiles)
        assertTrue(world.northOnTop)
        assertFalse(world.citiesArtificiallySpread)
        assertEquals(
            listOf("Польша", "Литва", "Латвия", "Эстония", "Украина", "Беларусь — Россия"),
            world.quietBorders
        )
        assertEquals(
            listOf("Калининград", "Сахалин", "Чукотка", "Камчатка", "Новая Земля", "Крым"),
            world.excludedRegions
        )

        val brightCities = world.cities.filter { it.bright }
        assertEquals(listOf("Минск", "Витебск", "Москва"), brightCities.map { it.name })
        assertEquals(listOf("0/336", "0/168", "0/120"), brightCities.map { it.fractionText })
        assertEquals(listOf(0 to 336, 0 to 168, 0 to 120), brightCities.map { it.earnedStars to it.totalStars })

        val dimmedCities = world.cities.filter { !it.bright }
        assertEquals(listOf("Могилёв", "Гомель", "Смоленск", "Ленинград"), dimmedCities.map { it.name })
        assertTrue(dimmedCities.all { it.dimmed && it.fractionText == null && it.earnedStars == null && it.totalStars == null })

        // Тап по тусклому городу не меняет масштаб и сообщает, что уровни появятся позже
        for (dimmedName in listOf("Могилёв", "Гомель", "Смоленск", "Ленинград")) {
            assertFalse(session.tapCity(dimmedName))
            val afterDimTap = session.snapshot()
            assertEquals(ScreenId.WORLD_MAP, afterDimTap.screen)
            assertEquals("В городе $dimmedName уровни появятся позже.", afterDimTap.worldMapNotice)
            assertEquals("В городе $dimmedName уровни появятся позже.", afterDimTap.noticeText)
            assertEquals("В городе $dimmedName уровни появятся позже.", afterDimTap.worldMap!!.noticeText)
        }
    }

    @Test
    fun `тап по яркому городу открывает контур города с яркой игровой улицей и тусклыми прочими, а тап по яркой улице открывает контур улицы с барами по порядку хода`() {
        val session = LevelSession.createLiveGame()

        // 1. Минск -> Зыбицкая
        assertTrue(session.tapCity("Минск"))
        val minskSnap = session.snapshot()
        assertEquals(ScreenId.CITY_MAP, minskSnap.screen)
        assertEquals("Минск", minskSnap.cityName)
        assertEquals("Минск", minskSnap.pathText)
        assertTrue(minskSnap.hasBackButton)
        assertEquals("Назад", minskSnap.backButtonLabel)
        assertNotNull(minskSnap.cityMap)
        assertEquals("Зыбицкая", minskSnap.cityMap!!.playableStreet.name)
        assertTrue(minskSnap.cityMap!!.playableStreet.bright)
        assertTrue(minskSnap.cityMap!!.playableStreet.description.contains("400 м"))
        assertTrue(minskSnap.cityMap!!.streets.filter { !it.bright }.isNotEmpty())
        assertTrue(minskSnap.cityMap!!.streets.filter { !it.bright }.all { it.dimmed })

        // Тап по тусклой улице в городе не меняет масштаб
        val dimMinskStreet = minskSnap.cityMap!!.streets.first { !it.bright }.name
        assertFalse(session.tapStreet(dimMinskStreet))
        assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)

        // Тап по яркой улице Зыбицкая открывает контур улицы
        assertTrue(session.tapStreet("Зыбицкая"))
        val zybitskayaSnap = session.snapshot()
        assertEquals(ScreenId.STREET_MAP, zybitskayaSnap.screen)
        assertEquals("Минск · Зыбицкая", zybitskayaSnap.pathText)
        assertNotNull(zybitskayaSnap.streetMap)
        assertEquals("Зыбицкая", zybitskayaSnap.streetMap!!.streetName)
        assertEquals("с запада на восток", zybitskayaSnap.streetMap!!.walkingOrderDescription)
        assertEquals(
            listOf("Under", "Койот", "Zodiac", "Рядом", "Белый Налив", "Горький", "На дне", "Матрешка"),
            zybitskayaSnap.streetMap!!.bars.map { it.name }
        )
        assertEquals(
            listOf(1 to 8, 1 to 8, 2 to 12, 2 to 12, 3 to 16, 3 to 16, 4 to 20, 4 to 20),
            zybitskayaSnap.streetMap!!.bars.map { it.rank to it.levelCount }
        )
        assertEquals(112, zybitskayaSnap.streetMap!!.bars.sumOf { it.levelCount })

        // Возвращаемся на мир двумя шагами «Назад»
        session.tapBack()
        assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)
        session.tapBack()
        assertEquals(ScreenId.WORLD_MAP, session.snapshot().screen)

        // 2. Витебск -> Суворова
        assertTrue(session.tapCity("Витебск"))
        val vitebskSnap = session.snapshot()
        assertEquals(ScreenId.CITY_MAP, vitebskSnap.screen)
        assertEquals("Витебск", vitebskSnap.pathText)
        assertEquals("Суворова", vitebskSnap.cityMap!!.playableStreet.name)
        assertTrue(vitebskSnap.cityMap!!.playableStreet.bright)
        assertTrue(vitebskSnap.cityMap!!.playableStreet.description.contains("1,3 км"))
        assertFalse(session.tapStreet(vitebskSnap.cityMap!!.streets.first { !it.bright }.name))
        assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)

        assertTrue(session.tapStreet("Суворова"))
        val suvorovaSnap = session.snapshot()
        assertEquals(ScreenId.STREET_MAP, suvorovaSnap.screen)
        assertEquals("Витебск · Суворова", suvorovaSnap.pathText)
        assertEquals("с севера на юг", suvorovaSnap.streetMap!!.walkingOrderDescription)
        assertEquals(
            listOf("Золотой лев", "Марвин", "«Damask» Kafe-Bar", "Аптека бар"),
            suvorovaSnap.streetMap!!.bars.map { it.name }
        )
        assertEquals(
            listOf(1 to 8, 2 to 12, 3 to 16, 4 to 20),
            suvorovaSnap.streetMap!!.bars.map { it.rank to it.levelCount }
        )
        assertEquals(56, suvorovaSnap.streetMap!!.bars.sumOf { it.levelCount })

        session.systemBack()
        assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)
        session.systemBack()
        assertEquals(ScreenId.WORLD_MAP, session.snapshot().screen)

        // 3. Москва -> Патрики
        assertTrue(session.tapCity("Москва"))
        val moscowSnap = session.snapshot()
        assertEquals(ScreenId.CITY_MAP, moscowSnap.screen)
        assertEquals("Москва", moscowSnap.pathText)
        assertEquals("Патрики", moscowSnap.cityMap!!.playableStreet.name)
        assertTrue(moscowSnap.cityMap!!.playableStreet.bright)
        assertTrue(moscowSnap.cityMap!!.playableStreet.description.contains("верхней части основного массива"))
        assertFalse(session.tapStreet(moscowSnap.cityMap!!.streets.first { !it.bright }.name))
        assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)

        assertTrue(session.tapStreet("Патрики"))
        val patrikiSnap = session.snapshot()
        assertEquals(ScreenId.STREET_MAP, patrikiSnap.screen)
        assertEquals("Москва · Патрики", patrikiSnap.pathText)
        assertEquals("по контуру пруда", patrikiSnap.streetMap!!.walkingOrderDescription)
        assertEquals("130×130 м", patrikiSnap.streetMap!!.pondWaterSize)
        assertEquals(
            listOf("Малая Бронная", "Большой Патриарший", "Малый Патриарший", "Ермолаевский"),
            patrikiSnap.streetMap!!.surroundingStreets
        )
        assertEquals(
            listOf("Киану", "Bro&n", "Зойка"),
            patrikiSnap.streetMap!!.bars.map { it.name }
        )
        assertEquals(
            listOf(1 to 8, 2 to 12, 4 to 20),
            patrikiSnap.streetMap!!.bars.map { it.rank to it.levelCount }
        )
        assertEquals(40, patrikiSnap.streetMap!!.bars.sumOf { it.levelCount })
    }

    @Test
    fun `все пятнадцать баров доступны сразу с числом карточек по рангу и первым открытым уровнем, прогресс одного бара не открывает уровни другого, дробь города на мире считает сумму лучших звёзд своих баров, а Назад и Новая игра ведут по ступеням и возвращают мир 120 из 120`() {
        val session = LevelSession.createLiveGame()

        val expectedGeography = listOf(
            Triple(
                "Минск",
                "Зыбицкая",
                listOf(
                    "Under" to 8,
                    "Койот" to 8,
                    "Zodiac" to 12,
                    "Рядом" to 12,
                    "Белый Налив" to 16,
                    "Горький" to 16,
                    "На дне" to 20,
                    "Матрешка" to 20
                )
            ),
            Triple(
                "Витебск",
                "Суворова",
                listOf(
                    "Золотой лев" to 8,
                    "Марвин" to 12,
                    "«Damask» Kafe-Bar" to 16,
                    "Аптека бар" to 20
                )
            ),
            Triple(
                "Москва",
                "Патрики",
                listOf(
                    "Киану" to 8,
                    "Bro&n" to 12,
                    "Зойка" to 20
                )
            )
        )

        // 1. Все 15 баров доступны сразу, в каждом сперва открыт только уровень 1, число карточек совпадает с рангом (8, 12, 16, 20), и в каждом баре открывается первый уровень
        for ((cityName, streetName, bars) in expectedGeography) {
            assertTrue(session.tapCity(cityName))
            assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)
            assertTrue(session.tapStreet(streetName))
            assertEquals(ScreenId.STREET_MAP, session.snapshot().screen)

            for ((barName, expectedLevels) in bars) {
                assertTrue("Бар $barName должен открываться с улицы $streetName", session.tapBar(barName))
                val film = session.snapshot()
                assertEquals(ScreenId.LEVEL_FILMSTRIP, film.screen)
                assertEquals(cityName, film.cityName)
                assertEquals(streetName, film.streetName)
                assertEquals(barName, film.barName)
                assertEquals("$streetName · $barName", film.pathText)
                assertEquals(expectedLevels, film.levelCards.size)
                assertTrue(film.levelCards[0].unlocked)
                assertTrue(film.levelCards.drop(1).none { it.unlocked })
                assertTrue(film.levelCards.all { it.bestStars == 0 })

                // В каждом из 15 баров уже можно открыть первый уровень
                assertTrue("В баре $barName должен открываться уровень 1", session.tapLevelCard(1))
                val counter = session.snapshot()
                assertEquals(ScreenId.COUNTER, counter.screen)
                assertEquals("$streetName · $barName · Уровень 1", counter.pathText)

                // «Назад» с прилавка возвращает на плёнку, а «Назад» с плёнки — на улицу
                session.tapBack()
                assertEquals(ScreenId.LEVEL_FILMSTRIP, session.snapshot().screen)
                session.tapBack()
                assertEquals(ScreenId.STREET_MAP, session.snapshot().screen)
            }

            // «Назад» с улицы в город, из города на мир
            session.tapBack()
            assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)
            session.tapBack()
            assertEquals(ScreenId.WORLD_MAP, session.snapshot().screen)
        }

        // 2. Проходим уровень 1 в баре Under (Минск) на 3 звезды и уровень 1 в баре Киану (Москва) на 2 звезды:
        // в Under открывается уровень 2, но в Койоте и остальных барах открыт только уровень 1!
        assertTrue(session.tapCity("Минск"))
        assertTrue(session.tapStreet("Зыбицкая"))
        assertTrue(session.tapBar("Under"))
        assertTrue(session.tapLevelCard(1))
        val underL1 = session.snapshot()
        for ((from, to) in session.levels[0].solutionMoves) {
            session.tapBottle(from)
            session.tapBottle(to)
        }
        assertEquals(ScreenId.VICTORY_BANNER, session.snapshot().screen)
        assertEquals(3, session.snapshot().victoryBanner!!.stars)
        session.tapToLevels()
        assertEquals(listOf(true, true) + List(6) { false }, session.snapshot().levelCards.map { it.unlocked })
        assertEquals(3, session.snapshot().levelCards[0].bestStars)

        // Выходим на Зыбицкую и проверяем соседний бар Койот: прогресс Under не открыл уровень 2 в Койоте
        session.tapBack()
        assertEquals(ScreenId.STREET_MAP, session.snapshot().screen)
        assertTrue(session.tapBar("Койот"))
        val koyotFilm = session.snapshot()
        assertEquals("Койот", koyotFilm.barName)
        assertEquals(8, koyotFilm.levelCards.size)
        assertTrue(koyotFilm.levelCards[0].unlocked)
        assertTrue(koyotFilm.levelCards.drop(1).none { it.unlocked })
        assertEquals(0, koyotFilm.levelCards.sumOf { it.bestStars })

        // Поднимаемся системной «Назад» с плёнки Койота до самого мира: плёнка -> улица -> город -> мир
        session.systemBack()
        assertEquals(ScreenId.STREET_MAP, session.snapshot().screen)
        session.systemBack()
        assertEquals(ScreenId.CITY_MAP, session.snapshot().screen)
        session.systemBack()
        val worldAfterMinskWin = session.snapshot()
        assertEquals(ScreenId.WORLD_MAP, worldAfterMinskWin.screen)

        // На мире системная «Назад» и tapBack() не меняют экран и не сбрасывают прогресс
        val staminaBeforeWorldBack = worldAfterMinskWin.stamina
        session.systemBack()
        session.tapBack()
        val worldAfterBackOnWorld = session.snapshot()
        assertEquals(ScreenId.WORLD_MAP, worldAfterBackOnWorld.screen)
        assertEquals(staminaBeforeWorldBack, worldAfterBackOnWorld.stamina)

        // Дробь Минска на мире теперь 3/336, Витебска 0/168, Москвы 0/120
        val citiesAfterMinsk = worldAfterBackOnWorld.worldMap!!.cities.associateBy { it.name }
        assertEquals("3/336", citiesAfterMinsk.getValue("Минск").fractionText)
        assertEquals(3, citiesAfterMinsk.getValue("Минск").earnedStars)
        assertEquals("0/168", citiesAfterMinsk.getValue("Витебск").fractionText)
        assertEquals("0/120", citiesAfterMinsk.getValue("Москва").fractionText)

        // Проходим уровень 1 в баре Золотой лев (Витебск) и уровень 1 в баре Зойка (Москва)
        assertTrue(session.tapCity("Витебск"))
        assertTrue(session.tapStreet("Суворова"))
        assertTrue(session.tapBar("Золотой лев"))
        assertTrue(session.tapLevelCard(1))
        for ((from, to) in session.levels[0].solutionMoves) {
            session.tapBottle(from)
            session.tapBottle(to)
        }
        assertEquals(ScreenId.VICTORY_BANNER, session.snapshot().screen)
        session.tapToLevels()
        session.tapBack() // улица
        session.tapBack() // город
        session.tapBack() // мир

        assertTrue(session.tapCity("Москва"))
        assertTrue(session.tapStreet("Патрики"))
        assertTrue(session.tapBar("Зойка"))
        assertEquals(20, session.snapshot().levelCards.size)
        assertTrue(session.tapLevelCard(1))
        for ((from, to) in session.levels[0].solutionMoves) {
            session.tapBottle(from)
            session.tapBottle(to)
        }
        assertEquals(ScreenId.VICTORY_BANNER, session.snapshot().screen)
        session.tapToLevels()
        assertTrue(session.snapshot().levelCards[1].unlocked)
        session.tapBack() // улица
        session.tapBack() // город
        session.tapBack() // мир

        val worldWithAllThreeCities = session.snapshot().worldMap!!.cities.associateBy { it.name }
        assertEquals("3/336", worldWithAllThreeCities.getValue("Минск").fractionText)
        assertEquals("3/168", worldWithAllThreeCities.getValue("Витебск").fractionText)
        assertEquals("3/120", worldWithAllThreeCities.getValue("Москва").fractionText)

        // 3. Гибель и «Новая игра» с экрана гибели: открывает мир, 120/120, в каждом баре открыт только уровень 1, звёзд и прибавок нет
        val fatalLive = LevelSession.createLiveGame(initialStamina = 1, initialCeiling = 123)
        assertTrue(fatalLive.tapCity("Минск"))
        assertTrue(fatalLive.tapStreet("Зыбицкая"))
        assertTrue(fatalLive.tapBar("Under"))
        assertTrue(fatalLive.tapLevelCard(1))
        // Делаем ходы до первой сборки бутылки (цена 1 при запасе 1 -> 0 -> гибель)
        fatalLive.tapBottle(0)
        fatalLive.tapBottle(2)
        fatalLive.tapBottle(0)
        fatalLive.tapBottle(3)
        fatalLive.tapBottle(1)
        fatalLive.tapBottle(3)
        fatalLive.tapBottle(1)
        fatalLive.tapBottle(2)
        fatalLive.tapBottle(0)
        fatalLive.tapBottle(2) // собирает бутылку 2 -> выносливость 1 -> 0 -> гибель!
        assertEquals(ScreenId.DEFEAT, fatalLive.snapshot().screen)
        assertEquals(0, fatalLive.snapshot().stamina)

        // «Новая игра» с экрана гибели открывает мир, 120/120
        fatalLive.startNewGame()
        val afterDefeatNewGame = fatalLive.snapshot()
        assertEquals(ScreenId.WORLD_MAP, afterDefeatNewGame.screen)
        assertEquals(120, afterDefeatNewGame.stamina)
        assertEquals(120, afterDefeatNewGame.staminaCeiling)
        assertEquals("120/120", afterDefeatNewGame.enduranceText)
        assertEquals("Трезв", afterDefeatNewGame.stateLabel)
        assertEquals("Мир", afterDefeatNewGame.pathText)

        session.applyBarBonus(3)
        // Проверим и на основной сессии со звёздами в трёх городах после startNewGame():
        session.startNewGame()
        val afterNewGame = session.snapshot()
        assertEquals(ScreenId.WORLD_MAP, afterNewGame.screen)
        assertEquals(120, afterNewGame.stamina)
        assertEquals(120, afterNewGame.staminaCeiling)
        assertEquals("120/120", afterNewGame.enduranceText)
        assertEquals("Трезв", afterNewGame.stateLabel)
        assertEquals("Мир", afterNewGame.pathText)
        val resetCities = afterNewGame.worldMap!!.cities.filter { it.bright }
        assertEquals(listOf("0/336", "0/168", "0/120"), resetCities.map { it.fractionText })

        // В барах Under и Зойка снова открыт только первый уровень и звёзд нет
        assertTrue(session.tapCity("Минск"))
        assertTrue(session.tapStreet("Зыбицкая"))
        assertTrue(session.tapBar("Under"))
        assertEquals(listOf(true) + List(7) { false }, session.snapshot().levelCards.map { it.unlocked })
        assertEquals(0, session.snapshot().levelCards.sumOf { it.bestStars })
        session.openWorldMap()
        assertTrue(session.tapCity("Москва"))
        assertTrue(session.tapStreet("Патрики"))
        assertTrue(session.tapBar("Зойка"))
        assertEquals(listOf(true) + List(19) { false }, session.snapshot().levelCards.map { it.unlocked })
        assertEquals(0, session.snapshot().levelCards.sumOf { it.bestStars })
    }

    @Test
    fun `в каталоге 15 баров и 208 уровней с параметрами бутылок напитков и вместимости строго по таблице рангов`() {
        val expectedBarRanks = listOf(
            Triple("Минск", "Зыбицкая", listOf(
                "Under" to 1,
                "Койот" to 1,
                "Zodiac" to 2,
                "Рядом" to 2,
                "Белый Налив" to 3,
                "Горький" to 3,
                "На дне" to 4,
                "Матрешка" to 4
            )),
            Triple("Витебск", "Суворова", listOf(
                "Золотой лев" to 1,
                "Марвин" to 2,
                "«Damask» Kafe-Bar" to 3,
                "Аптека бар" to 4
            )),
            Triple("Москва", "Патрики", listOf(
                "Киану" to 1,
                "Bro&n" to 2,
                "Зойка" to 4
            ))
        )

        fun expectedRankParams(rank: Int, levelNumber: Int): Triple<Int, Int, Int> {
            val group = (levelNumber - 1) / 4
            return when (rank) {
                1 -> when (group) {
                    0 -> Triple(4, 2, 3)
                    1 -> Triple(5, 3, 3)
                    else -> error("Unexpected group $group for rank 1")
                }
                2 -> when (group) {
                    0 -> Triple(5, 3, 3)
                    1 -> Triple(6, 4, 3)
                    2 -> Triple(7, 5, 4)
                    else -> error("Unexpected group $group for rank 2")
                }
                3 -> when (group) {
                    0 -> Triple(6, 4, 4)
                    1 -> Triple(7, 5, 4)
                    2 -> Triple(8, 6, 5)
                    3 -> Triple(9, 7, 5)
                    else -> error("Unexpected group $group for rank 3")
                }
                4 -> when (group) {
                    0 -> Triple(7, 5, 5)
                    1 -> Triple(8, 6, 5)
                    2 -> Triple(8, 6, 6)
                    3 -> Triple(9, 7, 6)
                    4 -> Triple(10, 8, 6)
                    else -> error("Unexpected group $group for rank 4")
                }
                else -> error("Unexpected rank $rank")
            }
        }

        assertEquals(15, LevelCatalog.bars.size)
        assertEquals(208, LevelCatalog.totalLevelsCount)

        var totalLevels = 0
        val levelsByStreet = mutableMapOf<String, Int>()

        for ((cityName, streetName, barList) in expectedBarRanks) {
            for ((barName, rank) in barList) {
                val barLevels = LevelCatalog.levelsForBar(barName)
                val expectedCount = rank * 4 + 4
                assertEquals("Число уровней бара $barName", expectedCount, barLevels.size)
                totalLevels += barLevels.size
                levelsByStreet[streetName] = (levelsByStreet[streetName] ?: 0) + barLevels.size

                for (level in barLevels) {
                    val (expectedBottles, expectedDrinks, expectedCapacity) = expectedRankParams(rank, level.levelNumber)
                    assertEquals(
                        "Вместимость $barName ур. ${level.levelNumber}",
                        expectedCapacity,
                        level.capacity
                    )
                    assertEquals(
                        "Число бутылок $barName ур. ${level.levelNumber}",
                        expectedBottles,
                        level.initialBottles.size
                    )
                    val distinctDrinks = level.initialBottles.flatten().toSet()
                    assertEquals(
                        "Число разных напитков $barName ур. ${level.levelNumber}",
                        expectedDrinks,
                        distinctDrinks.size
                    )
                }
            }
        }

        assertEquals(208, totalLevels)
        assertEquals(112, levelsByStreet["Зыбицкая"])
        assertEquals(56, levelsByStreet["Суворова"])
        assertEquals(40, levelsByStreet["Патрики"])
    }

    @Test
    fun `у всех 208 уровней постоянная расстановка с двумя пустыми, смешанными полными и всеми 9 напитками`() {
        val seenArrangements = mutableSetOf<List<List<Drink>>>()
        val seenDrinksAcrossCatalog = mutableSetOf<Drink>()
        val underDrinks = mutableSetOf<Drink>()

        val session = LevelSession.createLiveGame()

        for (bar in LevelCatalog.bars) {
            assertTrue(session.tapCity(bar.cityName))
            assertTrue(session.tapStreet(bar.streetName))
            assertTrue(session.tapBar(bar.barName))

            for ((idx, level) in bar.levels.withIndex()) {
                val levelNum = idx + 1
                assertEquals(levelNum, level.levelNumber)
                assertTrue("Все порции уровня ${bar.barName} #$levelNum видны с начала", level.allPortionsVisible)
                assertTrue("Норматив должен совпадать с длиной проверенного решения", level.parMoves == level.solutionMoves.size && level.parMoves > 0)

                val emptyBottles = level.initialBottles.filter { it.isEmpty() }
                val nonEmptyBottles = level.initialBottles.filter { it.isNotEmpty() }
                assertEquals(
                    "В начале ровно 2 полностью пустые бутылки (${bar.barName} #$levelNum)",
                    2,
                    emptyBottles.size
                )
                assertEquals(
                    "Остальные бутылки заполнены (${bar.barName} #$levelNum)",
                    level.initialBottles.size - 2,
                    nonEmptyBottles.size
                )

                for ((bIdx, bottle) in nonEmptyBottles.withIndex()) {
                    assertEquals(
                        "Бутылка $bIdx заполнена до вместимости (${bar.barName} #$levelNum)",
                        level.capacity,
                        bottle.size
                    )
                    assertTrue(
                        "В непустой бутылке $bIdx минимум 2 разных напитка (${bar.barName} #$levelNum)",
                        bottle.distinct().size >= 2
                    )
                }

                val countsByDrink = mutableMapOf<Drink, Int>()
                for (bottle in nonEmptyBottles) {
                    for (drink in bottle) {
                        countsByDrink[drink] = (countsByDrink[drink] ?: 0) + 1
                    }
                }
                assertEquals(
                    "Число разных напитков без повтора вида равно числу непустых бутылок (${bar.barName} #$levelNum)",
                    nonEmptyBottles.size,
                    countsByDrink.size
                )
                for ((drink, count) in countsByDrink) {
                    assertTrue(drink in Drink.entries)
                    assertEquals(
                        "У напитка ${drink.title} порций ровно на одну полную бутылку (${bar.barName} #$levelNum)",
                        level.capacity,
                        count
                    )
                    seenDrinksAcrossCatalog.add(drink)
                    if (bar.barName == "Under") {
                        underDrinks.add(drink)
                    }
                }

                assertTrue(
                    "У каждой пары (${bar.barName}, $levelNum) своя уникальная стартовая расстановка",
                    seenArrangements.add(level.initialBottles)
                )
            }

            // Повтор и новый заход на первом уровне бара возвращают ту же сохранённую расстановку
            assertTrue(session.tapLevelCard(1))
            val firstStartPortions = session.snapshot().bottles.map { it.portions }
            assertEquals(bar.levels[0].initialBottles, firstStartPortions)
            assertTrue(session.snapshot().allPortionsVisible)
            assertTrue(session.snapshot().bottles.all { it.allPortionsVisible && it.hiddenPortionsCount == 0 })
            assertFalse(session.snapshot().hasHintButton)
            assertNull(session.snapshot().hintButton)

            val (firstFrom, firstTo) = bar.levels[0].solutionMoves.first()
            session.tapBottle(firstFrom)
            session.tapBottle(firstTo)
            assertEquals(1, session.snapshot().moves)

            session.restartRun()
            assertEquals(firstStartPortions, session.snapshot().bottles.map { it.portions })

            assertTrue(session.startNewRun(1))
            assertEquals(firstStartPortions, session.snapshot().bottles.map { it.portions })

            session.tapBack() // на плёнку
            session.tapBack() // на улицу
            session.tapBack() // в город
            session.tapBack() // на мир
        }

        assertEquals(208, seenArrangements.size)
        assertEquals(Drink.entries.toSet(), seenDrinksAcrossCatalog)
        assertTrue(
            "Крепкий напиток Chartreuse Verte (цена 3) встречается уже в первом баре Under",
            Drink.CHARTREUSE_VERTE in underDrinks
        )
    }

    @Test
    fun `прогон решений всех 208 уровней кончается победой за норматив и персонаж с 120 из 120 жив`() {
        val liveProgression = LevelSession.createLiveGame()
        var solvedCount = 0
        var sawShortCounter = false
        var sawLongCounter = false

        for (bar in LevelCatalog.bars) {
            assertEquals(ScreenId.WORLD_MAP, liveProgression.snapshot().screen)
            assertTrue(liveProgression.tapCity(bar.cityName))
            assertEquals(ScreenId.CITY_MAP, liveProgression.snapshot().screen)
            assertTrue(liveProgression.tapStreet(bar.streetName))
            assertEquals(ScreenId.STREET_MAP, liveProgression.snapshot().screen)
            assertTrue(liveProgression.tapBar(bar.barName))
            assertEquals(ScreenId.LEVEL_FILMSTRIP, liveProgression.snapshot().screen)
            assertEquals(bar.levels.size, liveProgression.snapshot().levelCards.size)

            for ((idx, level) in bar.levels.withIndex()) {
                val levelNum = idx + 1
                val filmSnap = liveProgression.snapshot()
                assertTrue(
                    "Уровень $levelNum в баре ${bar.barName} должен быть открыт",
                    filmSnap.levelCards[idx].unlocked
                )
                if (idx + 1 < bar.levels.size) {
                    assertFalse(
                        "Следующий уровень ${levelNum + 1} в баре ${bar.barName} закрыт до победы в текущем",
                        filmSnap.levelCards[idx + 1].unlocked
                    )
                }

                // Восполним выносливость временем до потолка перед заходом в сквозной навигации,
                // а также проверим отдельный чистый старт 120/120 для каждого из 208 уровней:
                liveProgression.advanceTimeMinutes(600)
                assertTrue(liveProgression.tapLevelCard(levelNum))

                val startSnap = liveProgression.snapshot()
                assertEquals(ScreenId.COUNTER, startSnap.screen)
                assertEquals("${bar.streetName} · ${bar.barName} · Уровень $levelNum", startSnap.pathText)
                assertEquals(level.capacity, startSnap.capacity)
                assertEquals(level.initialBottles, startSnap.bottles.map { it.portions })
                assertEquals(2, startSnap.bottles.count { it.portions.isEmpty() })
                assertTrue(startSnap.bottles.filter { it.portions.isNotEmpty() }.all {
                    it.portions.size == level.capacity && it.portions.distinct().size >= 2
                })
                assertTrue(startSnap.allPortionsVisible)
                assertFalse(startSnap.hasHintButton)
                assertNull(startSnap.hintButton)

                // Проверяем крайние прилавки: 4 бутылки вместимостью 3 и 10 бутылок вместимостью 6
                if (startSnap.bottles.size == 4 && startSnap.capacity == 3) {
                    sawShortCounter = true
                    assertEquals(4, startSnap.columnsPerRow)
                    assertEquals(1, startSnap.rowCount)
                    assertEquals(listOf(4), startSnap.rows.map { it.size })
                    assertEquals(GlassFitMode.WITH_CELL_MARGIN, startSnap.glass.fitMode)
                    assertTrue(startSnap.glass.hasCellMargin)
                    assertFalse(startSnap.glass.reachesCellEdge)
                }
                if (startSnap.bottles.size == 10 && startSnap.capacity == 6) {
                    sawLongCounter = true
                    assertEquals(5, startSnap.columnsPerRow)
                    assertEquals(2, startSnap.rowCount)
                    assertEquals(listOf(5, 5), startSnap.rows.map { it.size })
                    assertEquals(GlassFitMode.EDGE_TO_EDGE, startSnap.glass.fitMode)
                    assertFalse(startSnap.glass.hasCellMargin)
                    assertTrue(startSnap.glass.reachesCellEdge)
                }

                // Независимая проверка с полного запаса 120/120 для этого уровня:
                val singleRun = LevelSession(
                    levels = listOf(level),
                    streetName = bar.streetName,
                    barName = bar.barName,
                    initialStamina = 120,
                    initialCeiling = 120
                )
                for ((from, to) in level.solutionMoves) {
                    singleRun.tapBottle(from)
                    singleRun.tapBottle(to)
                }
                val singleWon = singleRun.snapshot()
                assertEquals(
                    "Одиночный прогон ${bar.barName} #$levelNum должен завершиться победой",
                    ScreenId.VICTORY_BANNER,
                    singleWon.screen
                )
                assertEquals(
                    "Число ходов в ${bar.barName} #$levelNum должно равняться нормативу",
                    level.parMoves,
                    singleWon.moves
                )
                assertEquals(level.parMoves, singleWon.victoryBanner!!.moves)
                assertTrue(
                    "После прогона ${bar.barName} #$levelNum с 120/120 персонаж должен остаться жив",
                    singleWon.stamina >= 1 && singleWon.defeatScreen == null
                )

                // Проходим этот же уровень в сквозной навигации по карте, чтобы открыть следующий уровень бара
                for ((from, to) in level.solutionMoves) {
                    liveProgression.tapBottle(from)
                    liveProgression.tapBottle(to)
                }
                val progWon = liveProgression.snapshot()
                assertEquals(ScreenId.VICTORY_BANNER, progWon.screen)
                assertEquals(level.parMoves, progWon.moves)
                assertTrue(progWon.stamina >= 1)

                // Повтор того же уровня после победы возвращает ровно ту же стартовую расстановку
                liveProgression.tapToLevels()
                assertEquals(ScreenId.LEVEL_FILMSTRIP, liveProgression.snapshot().screen)
                assertTrue(liveProgression.tapLevelCard(levelNum))
                assertEquals(level.initialBottles, liveProgression.snapshot().bottles.map { it.portions })
                liveProgression.tapBack()
                assertEquals(ScreenId.LEVEL_FILMSTRIP, liveProgression.snapshot().screen)

                solvedCount++
            }

            // Возвращаемся с плёнки бара на мир: плёнка -> улица -> город -> мир
            liveProgression.tapBack()
            assertEquals(ScreenId.STREET_MAP, liveProgression.snapshot().screen)
            liveProgression.tapBack()
            assertEquals(ScreenId.CITY_MAP, liveProgression.snapshot().screen)
            liveProgression.tapBack()
            assertEquals(ScreenId.WORLD_MAP, liveProgression.snapshot().screen)
        }

        assertEquals(208, solvedCount)
        assertTrue("В каталоге проверен крайний прилавок на 4 бутылки вместимостью 3", sawShortCounter)
        assertTrue("В каталоге проверен крайний прилавок на 10 бутылок вместимостью 6", sawLongCounter)
    }
}
