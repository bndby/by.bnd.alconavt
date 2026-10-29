package by.bnd.alconavt

import android.app.Activity
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher

class MainActivity : Activity() {

    private lateinit var session: LevelSession
    private lateinit var counterScreen: LinearLayout
    private lateinit var cardSheetScrollView: ScrollView
    private lateinit var cardSheetContent: LinearLayout
    private lateinit var staminaView: TextView
    private lateinit var bannerLayout: LinearLayout
    private lateinit var bannerTitleView: TextView
    private lateinit var bannerMovesView: TextView
    private lateinit var rackContainer: LinearLayout
    private lateinit var restartButtonView: TextView
    private lateinit var undoButtonView: TextView
    private lateinit var movesValueView: TextView
    private lateinit var movesLabelView: TextView
    private var backCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = LevelSession(
            capacity = 3,
            initialBottles = listOf(
                listOf(Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT),
                listOf(Drink.PILSNER_URQUELL, Drink.GUINNESS_DRAUGHT, Drink.PILSNER_URQUELL),
                emptyList(),
                emptyList()
            )
        )

        val rootFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#100E14"))
        }

        counterScreen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)
        }

        // Верхняя полоса прилавка (запас/потолок)
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        staminaView = TextView(this).apply {
            setTextColor(Color.parseColor("#FFE56A"))
            textSize = 20f
        }
        topBar.addView(staminaView)
        counterScreen.addView(
            topBar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Плашка «Победа» на том же прилавке
        bannerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1A1720"))
            val pad = dp(12)
            setPadding(pad, pad, pad, pad)
            visibility = View.GONE
        }
        bannerTitleView = TextView(this).apply {
            setTextColor(Color.parseColor("#FFE56A"))
            textSize = 22f
            gravity = Gravity.CENTER
        }
        bannerMovesView = TextView(this).apply {
            setTextColor(Color.parseColor("#79F6FF"))
            textSize = 16f
            gravity = Gravity.CENTER
        }
        bannerLayout.addView(bannerTitleView)
        bannerLayout.addView(bannerMovesView)
        counterScreen.addView(
            bannerLayout,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        // Прилавок с рядами клеток бутылок (от 4 до 10 бутылок, вместимость от 3 до 6)
        rackContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        counterScreen.addView(
            rackContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Нижняя полоса прилавка: «Сначала», «Отмена хода» и счётчик ходов (без подсказки и без строки, объясняющей жест)
        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val actionsGroup = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        restartButtonView = createActionButton().apply {
            setOnClickListener {
                session.restartRun()
                render()
            }
        }
        undoButtonView = createActionButton().apply {
            setOnClickListener {
                session.undoMove()
                render()
            }
        }

        actionsGroup.addView(
            restartButtonView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dp(8)
            }
        )
        actionsGroup.addView(
            undoButtonView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val movesGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }
        movesValueView = TextView(this).apply {
            setTextColor(Color.parseColor("#79F6FF"))
            textSize = 22f
            gravity = Gravity.END
        }
        movesLabelView = TextView(this).apply {
            setTextColor(Color.parseColor("#6D6878"))
            textSize = 13f
            gravity = Gravity.END
        }
        movesGroup.addView(movesValueView)
        movesGroup.addView(movesLabelView)

        footer.addView(
            actionsGroup,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
        footer.addView(
            movesGroup,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        counterScreen.addView(
            footer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Лист карточки напитка на экране уровня
        cardSheetContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padH = dp(16)
            val padV = dp(16)
            setPadding(padH, padV, padH, dp(24))
        }
        cardSheetScrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#100E14"))
            isFillViewport = true
            visibility = View.GONE
            addView(
                cardSheetContent,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        rootFrame.addView(
            counterScreen,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        rootFrame.addView(
            cardSheetScrollView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        if (Build.VERSION.SDK_INT >= 33) {
            val callback = OnBackInvokedCallback {
                handleSystemBack()
            }
            backCallback = callback
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
        }

        setContentView(rootFrame)
        render()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!handleSystemBack()) {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    private fun handleSystemBack(): Boolean {
        val snap = session.snapshot()
        return if (snap.screen == ScreenId.DRINK_CARD) {
            session.systemBack()
            render()
            true
        } else {
            false
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= 33) {
            backCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
        }
        super.onDestroy()
    }

    private fun render() {
        val snap = session.snapshot()
        staminaView.text = "${snap.stamina}/${snap.staminaCeiling}"

        val banner = snap.victoryBanner
        if (banner != null) {
            bannerLayout.visibility = View.VISIBLE
            bannerTitleView.text = banner.title
            bannerMovesView.text = "${snap.movesLabel}: ${banner.moves}"
        } else {
            bannerLayout.visibility = View.GONE
        }

        val restart = snap.restartButton
        if (restart != null) {
            restartButtonView.visibility = View.VISIBLE
            bindActionButton(restartButtonView, restart)
        } else {
            restartButtonView.visibility = View.GONE
        }
        bindActionButton(undoButtonView, snap.undoButton)

        movesValueView.text = snap.moves.toString()
        movesLabelView.text = snap.movesLabel

        rackContainer.removeAllViews()
        var bottleGlobalIndex = 0
        snap.rows.forEach { rowBottles ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                weightSum = snap.columnsPerRow.toFloat()
            }

            rowBottles.forEach { bottle ->
                val currentIndex = bottleGlobalIndex++
                val cellLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                // Тап по стеклу выбирает бутылку для перелива и лист не открывает
                val bottleView = BottleView(
                    context = this,
                    capacity = snap.capacity,
                    bottle = bottle
                ).apply {
                    setOnClickListener {
                        session.tapBottleGlass(currentIndex)
                        render()
                    }
                }
                cellLayout.addView(
                    bottleView,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                )

                // Имя напитка видно только под бутылкой, заполненной им до вместимости
                val completedName = bottle.completedDrinkName
                val nameView = TextView(this).apply {
                    text = completedName ?: ""
                    visibility = if (completedName != null) View.VISIBLE else View.INVISIBLE
                    setTextColor(Color.parseColor("#FFE56A"))
                    textSize = 11f
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    minHeight = dp(28)
                    val padH = dp(4)
                    val padV = dp(2)
                    setPadding(padH, padV, padH, padV)
                    if (completedName != null) {
                        background = strokeBox(Color.parseColor("#FFE56A"))
                        isClickable = true
                        setOnClickListener {
                            session.tapBottleName(currentIndex)
                            render()
                        }
                    } else {
                        isClickable = false
                    }
                }
                cellLayout.addView(
                    nameView,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(4)
                    }
                )

                val cellMargin = dp(4)
                val cellParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                ).apply {
                    setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
                }
                rowLayout.addView(cellLayout, cellParams)
            }

            val rowParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
            rackContainer.addView(rowLayout, rowParams)
        }

        val card = snap.drinkCard
        if (snap.screen == ScreenId.DRINK_CARD && card != null) {
            counterScreen.visibility = View.GONE
            cardSheetScrollView.visibility = View.VISIBLE
            renderDrinkCardSheet(card)
        } else {
            cardSheetScrollView.visibility = View.GONE
            counterScreen.visibility = View.VISIBLE
        }
    }

    private fun renderDrinkCardSheet(card: DrinkCardSnapshot) {
        cardSheetContent.removeAllViews()

        // Верхняя полоса листа с кнопкой «Назад»
        val topHud = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val backButton = TextView(this).apply {
            text = card.backButtonLabel
            setTextColor(Color.parseColor("#FF5AD4"))
            textSize = 14f
            val padH = dp(10)
            val padV = dp(8)
            setPadding(padH, padV, padH, padV)
            background = strokeBox(Color.parseColor("#FF5AD4"))
            setOnClickListener {
                session.tapBack()
                render()
            }
        }
        topHud.addView(backButton)
        cardSheetContent.addView(
            topHud,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // 1. Рисунок бутылки (один пиксельный силуэт без знака дома, залитый до горлышка со статичным свечением)
        val illustrationView = PixelBottleIllustrationView(
            context = this,
            illustration = card.illustration
        )
        cardSheetContent.addView(
            illustrationView,
            LinearLayout.LayoutParams(dp(112), dp(160)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = dp(12)
                bottomMargin = dp(8)
            }
        )

        // 2. Имя напитка
        val nameView = TextView(this).apply {
            text = card.name
            setTextColor(Color.parseColor("#F6F1E8"))
            textSize = 22f
            typeface = Typeface.MONOSPACE
        }
        cardSheetContent.addView(nameView)

        // 3. Крепость (процент с запятой)
        val strengthView = TextView(this).apply {
            text = "Крепость ${card.strength}"
            setTextColor(Color.parseColor("#FFE56A"))
            textSize = 14f
            typeface = Typeface.MONOSPACE
        }
        cardSheetContent.addView(
            strengthView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        )

        // 4. Культура употребления одним блоком
        addCardSection(
            heading = "Культура употребления",
            bodyText = card.drinkingCulture
        )

        // 5. История
        addCardSection(
            heading = "История",
            bodyText = card.history
        )

        // 6. Факты
        addCardSection(
            heading = "Факты",
            bodyText = card.facts.joinToString(separator = "\n") { "• $it" }
        )
    }

    private fun addCardSection(heading: String, bodyText: String) {
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#3A3544"))
        }
        cardSheetContent.addView(
            divider,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(2)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(8)
            }
        )

        val headingView = TextView(this).apply {
            text = heading
            setTextColor(Color.parseColor("#79F6FF"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
        }
        cardSheetContent.addView(headingView)

        val bodyView = TextView(this).apply {
            text = bodyText
            setTextColor(Color.parseColor("#F6F1E8"))
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setLineSpacing(0f, 1.25f)
        }
        cardSheetContent.addView(
            bodyView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        )
    }

    private fun createActionButton(): TextView = TextView(this).apply {
        textSize = 14f
        gravity = Gravity.CENTER
        val hPad = dp(12)
        val vPad = dp(8)
        setPadding(hPad, vPad, hPad, vPad)
    }

    private fun bindActionButton(view: TextView, state: ActionButtonSnapshot) {
        view.text = state.title
        view.isEnabled = state.enabled
        view.isClickable = state.enabled
        if (state.enabled) {
            view.setTextColor(Color.parseColor("#79F6FF"))
            view.setBackgroundColor(Color.parseColor("#231F2C"))
            view.alpha = 1f
        } else {
            view.setTextColor(Color.parseColor("#555061"))
            view.setBackgroundColor(Color.parseColor("#17141D"))
            view.alpha = 0.5f
        }
    }

    private fun strokeBox(strokeColor: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(dp(2), strokeColor)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}

private fun drinkPixelColor(drink: Drink): Int = when (drink) {
    Drink.GUINNESS_DRAUGHT -> Color.parseColor("#8A2340")
    Drink.PILSNER_URQUELL -> Color.parseColor("#F0C84A")
    Drink.APEROL -> Color.parseColor("#FF6A1A")
    Drink.GIFFARD_CREME_DE_VIOLETTE -> Color.parseColor("#7A3DFF")
    Drink.BAILEYS_ORIGINAL_IRISH_CREAM -> Color.parseColor("#E6D2B5")
    Drink.GIFFARD_LICHI_LI -> Color.parseColor("#F3B6C8")
    Drink.BOLS_BLUE_CURACAO -> Color.parseColor("#2F6BFF")
    Drink.CAMPARI -> Color.parseColor("#E10600")
    Drink.CHARTREUSE_VERTE -> Color.parseColor("#C6E04A")
}

private class BottleView(
    context: Context,
    private val capacity: Int,
    private val bottle: BottleSnapshot
) : View(context) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val glassBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#16141C")
    }
    private val slotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val slotGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val emptySlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.parseColor("#3A3544")
    }
    private val rect = RectF()
    private val neckRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val strokeInset = borderPaint.strokeWidth / 2f
        val availWidth = (width - strokeInset * 2f).coerceAtLeast(1f)
        val availHeight = (height - strokeInset * 2f).coerceAtLeast(1f)

        // Стекло вместе с общим горлышком остаётся внутри клетки и масштабируется по вместимости:
        // при вместимости 3 оставляет запас вокруг, при 6 доходит до края клетки, 4 и 5 — между ними.
        val glassTotalWidth = availWidth * bottle.glass.widthFraction.coerceIn(0.5f, 1.0f)
        val glassTotalHeight = availHeight * bottle.glass.heightFraction.coerceIn(0.5f, 1.0f)

        val totalLeft = (width - glassTotalWidth) / 2f
        val totalRight = totalLeft + glassTotalWidth
        // Прижимаем бутылку к низу клетки (при вместимости 6 доходит и до верхнего, и до боковых краёв клетки)
        val totalBottom = height - strokeInset
        val totalTop = (totalBottom - glassTotalHeight).coerceAtLeast(strokeInset)

        // Общее горлышко сверху внутри габарита стекла
        val neckHeight = (glassTotalHeight * 0.12f).coerceIn(8f, 22f)
        val neckWidth = (glassTotalWidth * 0.44f).coerceAtLeast(12f)
        val neckLeft = (width - neckWidth) / 2f
        val neckRight = neckLeft + neckWidth
        val neckTop = totalTop
        val neckBottom = totalTop + neckHeight

        val bodyTop = neckBottom
        val bodyBottom = totalBottom
        val bodyLeft = totalLeft
        val bodyRight = totalRight

        borderPaint.color = when {
            bottle.selected -> Color.parseColor("#79F6FF")
            bottle.completedDrinkName != null -> Color.parseColor("#FFE56A")
            else -> Color.parseColor("#6A6478")
        }

        neckRect.set(neckLeft, neckTop, neckRight, neckBottom)
        rect.set(bodyLeft, bodyTop, bodyRight, bodyBottom)

        canvas.drawRoundRect(neckRect, 4f, 4f, glassBgPaint)
        canvas.drawRoundRect(neckRect, 4f, 4f, borderPaint)
        canvas.drawRoundRect(rect, 10f, 10f, glassBgPaint)
        canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

        if (capacity <= 0) return
        val innerPad = 6f
        val gap = 4f
        val innerLeft = bodyLeft + innerPad
        val innerRight = bodyRight - innerPad
        val innerTop = bodyTop + innerPad
        val innerBottom = bodyBottom - innerPad
        val totalSlotsHeight = (innerBottom - innerTop).coerceAtLeast(1f)
        val slotHeight = ((totalSlotsHeight - gap * (capacity - 1)) / capacity).coerceAtLeast(2f)

        // Свободные места видны сверху, порции стоят стопкой снизу вверх со статичным свечением цвета напитка
        for (slotFromBottom in 0 until capacity) {
            val slotBottom = innerBottom - slotFromBottom * (slotHeight + gap)
            val slotTop = slotBottom - slotHeight
            rect.set(innerLeft, slotTop, innerRight, slotBottom)
            val drink = bottle.portions.getOrNull(slotFromBottom)
            if (drink != null) {
                val color = drinkPixelColor(drink)
                slotPaint.color = color
                canvas.drawRect(rect, slotPaint)
                slotGlowPaint.color = color
                slotGlowPaint.alpha = 110
                canvas.drawRect(rect, slotGlowPaint)
            } else {
                canvas.drawRect(rect, emptySlotPaint)
            }
        }
    }
}

private class PixelBottleIllustrationView(
    context: Context,
    private val illustration: BottleIllustrationSnapshot
) : View(context) {

    private val rimPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#5C5668")
    }
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.OUTER)
    }
    private val cellRect = RectF()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mask = illustration.pixelMask
        if (mask.isEmpty()) return
        val rows = mask.size
        val cols = mask.maxOf { it.length }
        if (cols <= 0) return

        val pad = 16f
        val availW = (width - pad * 2).coerceAtLeast(1f)
        val availH = (height - pad * 2).coerceAtLeast(1f)
        val cellSize = minOf(availW / cols, availH / rows)
        val startX = (width - cellSize * cols) / 2f
        val startY = (height - cellSize * rows) / 2f

        val drinkColor = drinkPixelColor(illustration.drink)
        fillPaint.color = drinkColor
        glowPaint.color = drinkColor
        glowPaint.alpha = 140

        // Статичное свечение того же цвета поверх заливки до горлышка
        for (r in 0 until rows) {
            val rowStr = mask[r]
            for (c in rowStr.indices) {
                if (rowStr[c] == '1') {
                    val left = startX + c * cellSize
                    val top = startY + r * cellSize
                    cellRect.set(left, top, left + cellSize, top + cellSize)
                    canvas.drawRect(cellRect, glowPaint)
                }
            }
        }

        // Пиксельный силуэт бутылки без знака дома
        for (r in 0 until rows) {
            val rowStr = mask[r]
            for (c in rowStr.indices) {
                val ch = rowStr[c]
                if (ch == '0') continue
                val left = startX + c * cellSize
                val top = startY + r * cellSize
                cellRect.set(left, top, left + cellSize, top + cellSize)
                if (ch == '2') {
                    canvas.drawRect(cellRect, rimPaint)
                } else if (ch == '1') {
                    canvas.drawRect(cellRect, fillPaint)
                }
            }
        }
    }
}
