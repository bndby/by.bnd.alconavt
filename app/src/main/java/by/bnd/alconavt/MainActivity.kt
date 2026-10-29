package by.bnd.alconavt

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#100E14"))
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

        // Прилавок с бутылками
        rackContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
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

        setContentView(root)
        render()
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

        movesValueView.text = snap.moves.toString()
        movesLabelView.text = snap.movesLabel

        rackContainer.removeAllViews()
        snap.bottles.forEachIndexed { index, bottle ->
            val bottleView = BottleView(
                context = this,
                capacity = snap.capacity,
                bottle = bottle
            ).apply {
                setOnClickListener {
                    session.tapBottle(index)
                    render()
                }
            }
            val lp = LinearLayout.LayoutParams(0, dp(220), 1f).apply {
                val margin = dp(6)
                setMargins(margin, margin, margin, margin)
            }
            rackContainer.addView(bottleView, lp)
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
        strokeWidth = 6f
    }
    private val slotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val emptySlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#3A3544")
    }
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pad = 12f
        val left = pad
        val top = pad
        val right = width - pad
        val bottom = height - pad

        borderPaint.color = if (bottle.selected) {
            Color.parseColor("#79F6FF")
        } else {
            Color.parseColor("#6A6478")
        }
        rect.set(left, top, right, bottom)
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

        if (capacity <= 0) return
        val innerPad = 10f
        val gap = 8f
        val innerLeft = left + innerPad
        val innerRight = right - innerPad
        val innerTop = top + innerPad
        val innerBottom = bottom - innerPad
        val totalHeight = innerBottom - innerTop
        val slotHeight = (totalHeight - gap * (capacity - 1)) / capacity

        // Свободные места видны сверху, порции стоят стопкой снизу вверх
        for (slotFromBottom in 0 until capacity) {
            val slotBottom = innerBottom - slotFromBottom * (slotHeight + gap)
            val slotTop = slotBottom - slotHeight
            rect.set(innerLeft, slotTop, innerRight, slotBottom)
            val drink = bottle.portions.getOrNull(slotFromBottom)
            if (drink != null) {
                slotPaint.color = drinkColor(drink)
                canvas.drawRect(rect, slotPaint)
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
}
