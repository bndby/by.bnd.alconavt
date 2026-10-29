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
}
