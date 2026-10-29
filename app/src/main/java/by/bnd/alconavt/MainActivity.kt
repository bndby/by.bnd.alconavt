package by.bnd.alconavt

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var session: LevelSession
    private lateinit var staminaView: TextView
    private lateinit var bannerLayout: LinearLayout
    private lateinit var bannerTitleView: TextView
    private lateinit var bannerMovesView: TextView
    private lateinit var rackContainer: LinearLayout
    private lateinit var movesValueView: TextView
    private lateinit var movesLabelView: TextView
    private lateinit var sheetOverlay: LinearLayout
    private lateinit var sheetTitleView: TextView

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

        val root = LinearLayout(this).apply {
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
        root.addView(
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
        root.addView(
            bannerLayout,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        // Прилавок с рядами клеток бутылок
        rackContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        root.addView(
            rackContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Нижняя полоса со счётчиком «ходы» (без подсказки и без строки, объясняющей жест)
        val footer = LinearLayout(this).apply {
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
        footer.addView(movesValueView)
        footer.addView(movesLabelView)
        root.addView(
            footer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        rootFrame.addView(
            root,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Лист карточки напитка поверх экрана уровня
        sheetOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#100E14"))
            val pad = dp(20)
            setPadding(pad, pad, pad, pad)
            visibility = View.GONE
        }
        val backFromSheet = TextView(this).apply {
            text = "Назад"
            setTextColor(Color.parseColor("#FF5AD4"))
            textSize = 16f
            val pad = dp(8)
            setPadding(pad, pad, pad, pad)
            setOnClickListener {
                session.closeDrinkSheet()
                render()
            }
        }
        sheetTitleView = TextView(this).apply {
            setTextColor(Color.parseColor("#FFE56A"))
            textSize = 22f
        }
        sheetOverlay.addView(
            backFromSheet,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        sheetOverlay.addView(
            sheetTitleView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )
        rootFrame.addView(
            sheetOverlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(rootFrame)
        render()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (session.snapshot().openedDrinkSheet != null) {
            session.closeDrinkSheet()
            render()
            return
        }
        @Suppress("DEPRECATION")
        super.onBackPressed()
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

        val sheet = snap.openedDrinkSheet
        if (sheet != null) {
            sheetOverlay.visibility = View.VISIBLE
            sheetTitleView.text = sheet.title
        } else {
            sheetOverlay.visibility = View.GONE
        }

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

                val nameView = TextView(this).apply {
                    val drinkName = bottle.completedDrinkName
                    text = drinkName ?: ""
                    visibility = if (drinkName != null) View.VISIBLE else View.INVISIBLE
                    setTextColor(Color.parseColor("#FFE56A"))
                    textSize = 11f
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    minHeight = dp(28)
                    val padH = dp(4)
                    val padV = dp(2)
                    setPadding(padH, padV, padH, padV)
                    if (drinkName != null) {
                        setOnClickListener {
                            session.tapBottleName(currentIndex)
                            render()
                        }
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
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
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

        // Свободные места видны сверху, порции стоят стопкой снизу вверх
        for (slotFromBottom in 0 until capacity) {
            val slotBottom = innerBottom - slotFromBottom * (slotHeight + gap)
            val slotTop = slotBottom - slotHeight
            rect.set(innerLeft, slotTop, innerRight, slotBottom)
            val drink = bottle.portions.getOrNull(slotFromBottom)
            if (drink != null) {
                val color = drinkColor(drink)
                slotPaint.color = color
                canvas.drawRect(rect, slotPaint)
                slotGlowPaint.color = drinkGlowColor(drink)
                canvas.drawRect(rect, slotGlowPaint)
            } else {
                canvas.drawRect(rect, emptySlotPaint)
            }
        }
    }

    private fun drinkColor(drink: Drink): Int = when (drink) {
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

    private fun drinkGlowColor(drink: Drink): Int = when (drink) {
        Drink.GUINNESS_DRAUGHT -> Color.parseColor("#B83258")
        else -> Color.argb(96, 255, 255, 255)
    }
}
