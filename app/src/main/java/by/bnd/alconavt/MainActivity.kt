package by.bnd.alconavt

import android.app.Activity
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

class SharedPreferencesGameStateStore(
    context: Context,
    prefsName: String = "alconavt_game_state"
) : GameStateStore {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun save(state: PersistedGameState) {
        prefs.edit().putString(KEY_STATE, state.serialize()).apply()
    }

    override fun load(): PersistedGameState? {
        val raw = prefs.getString(KEY_STATE, null) ?: return null
        return PersistedGameState.deserialize(raw)
    }

    override fun clear() {
        prefs.edit().remove(KEY_STATE).apply()
    }

    companion object {
        private const val KEY_STATE = "serialized_game_state"
    }
}

class MainActivity : Activity() {

    private lateinit var stateStore: GameStateStore
    private lateinit var game: GameRunModule
    private lateinit var backgroundBackdropView: BarBackdropView
    private lateinit var mapScreen: LinearLayout
    private lateinit var mapBackButtonView: TextView
    private lateinit var mapPathView: TextView
    private lateinit var mapStaminaView: TextView
    private lateinit var mapStateLabelView: TextView
    private lateinit var mapCanvasView: PixelMapCanvasView
    private lateinit var mapNoticeView: TextView
    private lateinit var neonSignView: TextView
    private lateinit var counterBackButtonView: TextView
    private lateinit var counterPathView: TextView
    private lateinit var counterScreen: LinearLayout
    private lateinit var filmstripScreen: LinearLayout
    private lateinit var filmstripBackButtonView: TextView
    private lateinit var filmstripPathView: TextView
    private lateinit var filmstripStaminaView: TextView
    private lateinit var filmstripStateLabelView: TextView
    private lateinit var filmstripCardsRow: LinearLayout
    private lateinit var cardSheetScrollView: ScrollView
    private lateinit var cardSheetContent: LinearLayout
    private lateinit var defeatScreenLayout: LinearLayout
    private lateinit var defeatTitleView: TextView
    private lateinit var newGameButtonView: TextView
    private lateinit var staminaView: TextView
    private lateinit var stateLabelView: TextView
    private lateinit var bannerLayout: LinearLayout
    private lateinit var bannerTitleView: TextView
    private lateinit var bannerStarsView: TextView
    private lateinit var bannerMovesView: TextView
    private lateinit var bannerToLevelsButtonView: TextView
    private lateinit var rackContainer: LinearLayout
    private lateinit var restartButtonView: TextView
    private lateinit var undoButtonView: TextView
    private lateinit var movesValueView: TextView
    private lateinit var movesLabelView: TextView
    private var backCallback: OnBackInvokedCallback? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var animationPhaseRad: Float = 0f
    private val ambianceRunnable = object : Runnable {
        override fun run() {
            game.onClockTimeMillis(System.currentTimeMillis())
            val snap = game.snapshot()
            staminaView.text = snap.enduranceText
            stateLabelView.text = snap.stateLabel
            mapStaminaView.text = snap.enduranceText
            mapStateLabelView.text = snap.stateLabel
            filmstripStaminaView.text = snap.filmstrip.enduranceText
            filmstripStateLabelView.text = snap.filmstrip.stateLabel
            updateAmbianceFrame(snap)
            uiHandler.postDelayed(this, 60L)
        }
    }

    private data class TopBarViews(
        val container: LinearLayout,
        val backButton: TextView,
        val pathView: TextView,
        val staminaView: TextView,
        val stateLabelView: TextView,
        val neonSignView: TextView? = null
    )

    private fun buildTopBar(includeNeonSign: Boolean = false): TopBarViews {
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val backButton = TextView(this).apply {
            text = "Назад"
            setTextColor(Color.parseColor("#FF5AD4"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            val padH = dp(8)
            val padV = dp(6)
            setPadding(padH, padV, padH, padV)
            background = strokeBox(Color.parseColor("#FF5AD4"))
            setOnClickListener {
                game.tapBack()
                render()
            }
        }
        val pathView = TextView(this).apply {
            setTextColor(Color.parseColor(if (includeNeonSign) "#F6F1E8" else "#FF5AD4"))
            textSize = if (includeNeonSign) 12f else 15f
            typeface = Typeface.MONOSPACE
        }
        val signView: TextView? = if (includeNeonSign) {
            TextView(this).apply {
                text = game.barName.uppercase()
                setTextColor(Color.parseColor("#FF5AD4"))
                textSize = 15f
                typeface = Typeface.MONOSPACE
            }
        } else {
            null
        }
        val centerView: View = if (signView != null) {
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(signView)
                addView(pathView)
            }
        } else {
            pathView
        }
        val enduranceGroup = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        val staminaTextView = TextView(this).apply {
            setTextColor(Color.parseColor("#FFE56A"))
            textSize = 20f
            typeface = Typeface.MONOSPACE
        }
        val stateTextView = TextView(this).apply {
            setTextColor(Color.parseColor("#79F6FF"))
            textSize = 16f
            typeface = Typeface.MONOSPACE
            val padH = dp(8)
            val padV = dp(2)
            setPadding(padH, padV, padH, padV)
        }
        enduranceGroup.addView(staminaTextView)
        enduranceGroup.addView(
            stateTextView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = dp(8)
            }
        )
        topBar.addView(
            backButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dp(8)
            }
        )
        topBar.addView(
            centerView,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
        topBar.addView(
            enduranceGroup,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        return TopBarViews(
            container = topBar,
            backButton = backButton,
            pathView = pathView,
            staminaView = staminaTextView,
            stateLabelView = stateTextView,
            neonSignView = signView
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stateStore = SharedPreferencesGameStateStore(applicationContext)
        game = GameRunModule.coldStartFrom(
            store = stateStore,
            atClockMillis = System.currentTimeMillis()
        )

        val rootFrame = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#100E14"))
        }

        backgroundBackdropView = BarBackdropView(this)
        rootFrame.addView(
            backgroundBackdropView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Экран карты на весь экран (ScreenId.WORLD_MAP, CITY_MAP, STREET_MAP) без колонки ступеней и свободного зума
        mapScreen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)
            visibility = View.GONE
        }

        val mapTopBar = buildTopBar(includeNeonSign = false)
        mapBackButtonView = mapTopBar.backButton
        mapPathView = mapTopBar.pathView
        mapStaminaView = mapTopBar.staminaView
        mapStateLabelView = mapTopBar.stateLabelView
        mapScreen.addView(
            mapTopBar.container,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        mapCanvasView = PixelMapCanvasView(
            context = this,
            onCityTapped = { cityName ->
                game.tapCity(cityName)
                render()
            },
            onStreetTapped = { streetName ->
                game.tapStreet(streetName)
                render()
            },
            onBarTapped = { barName ->
                game.tapBar(barName)
                render()
            }
        )
        mapScreen.addView(
            mapCanvasView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                topMargin = dp(8)
            }
        )

        mapNoticeView = TextView(this).apply {
            setTextColor(Color.parseColor("#C8C2B4"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            visibility = View.GONE
        }
        mapScreen.addView(
            mapNoticeView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        // Плёнка уровней бара (ScreenId.LEVEL_FILMSTRIP)
        filmstripScreen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)
            visibility = View.GONE
        }

        val filmstripTopBar = buildTopBar(includeNeonSign = false)
        filmstripBackButtonView = filmstripTopBar.backButton
        filmstripPathView = filmstripTopBar.pathView
        filmstripStaminaView = filmstripTopBar.staminaView
        filmstripStateLabelView = filmstripTopBar.stateLabelView
        filmstripScreen.addView(
            filmstripTopBar.container,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        filmstripCardsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padV = dp(16)
            setPadding(0, padV, 0, padV)
        }
        val filmstripScrollView = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(
                filmstripCardsRow,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL
                )
            )
        }
        filmstripScreen.addView(
            filmstripScrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        counterScreen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)
        }

        // Верхняя полоса прилавка: кнопка «Назад», путь (улица · бар · уровень), запас/потолок и подпись состояния
        val counterTopBar = buildTopBar(includeNeonSign = true)
        counterBackButtonView = counterTopBar.backButton
        neonSignView = counterTopBar.neonSignView!!
        counterPathView = counterTopBar.pathView
        staminaView = counterTopBar.staminaView
        stateLabelView = counterTopBar.stateLabelView
        counterScreen.addView(
            counterTopBar.container,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Плашка «Победа» на том же прилавке со звёздами, числом ходов и кнопкой «К уровням»
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
        bannerStarsView = TextView(this).apply {
            setTextColor(Color.parseColor("#FFE56A"))
            textSize = 20f
            gravity = Gravity.CENTER
        }
        bannerMovesView = TextView(this).apply {
            setTextColor(Color.parseColor("#79F6FF"))
            textSize = 16f
            gravity = Gravity.CENTER
        }
        bannerToLevelsButtonView = createActionButton().apply {
            setOnClickListener {
                game.tapToLevels()
                render()
            }
        }
        bannerLayout.addView(bannerTitleView)
        bannerLayout.addView(bannerStarsView)
        bannerLayout.addView(bannerMovesView)
        bannerLayout.addView(
            bannerToLevelsButtonView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )
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
                game.restartRun()
                render()
            }
        }
        undoButtonView = createActionButton().apply {
            setOnClickListener {
                game.undoMove()
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
            mapScreen,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        rootFrame.addView(
            filmstripScreen,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
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

        // Экран «Персонаж погиб» с единственной игровой кнопкой «Новая игра»
        defeatScreenLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#100E14"))
            val pad = dp(24)
            setPadding(pad, pad, pad, pad)
            visibility = View.GONE
        }
        defeatTitleView = TextView(this).apply {
            text = "Персонаж погиб"
            setTextColor(Color.parseColor("#FF5AD4"))
            textSize = 26f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
        }
        newGameButtonView = createActionButton().apply {
            text = "Новая игра"
            textSize = 18f
            val hPad = dp(20)
            val vPad = dp(12)
            setPadding(hPad, vPad, hPad, vPad)
            setOnClickListener {
                game.startNewGame()
                render()
            }
        }
        defeatScreenLayout.addView(
            defeatTitleView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(24)
            }
        )
        defeatScreenLayout.addView(
            newGameButtonView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        rootFrame.addView(
            defeatScreenLayout,
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
        val snap = game.snapshot()
        return when (snap.screen) {
            ScreenId.DRINK_CARD,
            ScreenId.COUNTER,
            ScreenId.VICTORY_BANNER,
            ScreenId.LEVEL_FILMSTRIP,
            ScreenId.STREET_MAP,
            ScreenId.CITY_MAP,
            ScreenId.DEFEAT -> {
                game.systemBack()
                render()
                true
            }
            ScreenId.WORLD_MAP -> {
                game.systemBack()
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        game.resumeApp(System.currentTimeMillis())
        render()
        uiHandler.removeCallbacks(ambianceRunnable)
        uiHandler.post(ambianceRunnable)
    }

    override fun onPause() {
        uiHandler.removeCallbacks(ambianceRunnable)
        game.minimizeApp(System.currentTimeMillis())
        super.onPause()
    }

    override fun onStop() {
        game.closeApp(System.currentTimeMillis())
        super.onStop()
    }

    override fun onDestroy() {
        uiHandler.removeCallbacks(ambianceRunnable)
        if (Build.VERSION.SDK_INT >= 33) {
            backCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
        }
        super.onDestroy()
    }

    private fun updateAmbianceFrame(snap: CounterSnapshot) {
        animationPhaseRad += 0.12f
        val swayLevel = snap.backgroundSwayLevel
        val pulseLevel = snap.neonPulseLevel
        // Только фоновый рисунок и декоративная вывеска пульсируют/качаются; карта, числа, кнопки, бутылки и порции неподвижны
        val swayAmplitudePx = dp(swayLevel * 2).toFloat()
        val dx = sin(animationPhaseRad) * swayAmplitudePx
        val dy = sin(animationPhaseRad * 0.7f) * (swayAmplitudePx * 0.5f)
        backgroundBackdropView.translationX = dx
        backgroundBackdropView.translationY = dy
        val pulseDelta = 0.08f * pulseLevel
        val pulseAlpha = (1f - pulseDelta * (0.5f + 0.5f * sin(animationPhaseRad * 1.4f))).coerceIn(0.35f, 1f)
        neonSignView.alpha = pulseAlpha
    }

    private fun render() {
        game.onClockTimeMillis(System.currentTimeMillis())
        val snap = game.snapshot()
        staminaView.text = snap.enduranceText
        stateLabelView.text = snap.stateLabel
        mapStaminaView.text = snap.enduranceText
        mapStateLabelView.text = snap.stateLabel
        mapPathView.text = snap.pathText
        mapBackButtonView.visibility = if (snap.hasBackButton) View.VISIBLE else View.GONE
        mapBackButtonView.text = snap.backButtonLabel ?: "Назад"
        val notice = snap.worldMapNotice
        if (!notice.isNullOrBlank() && snap.screen == ScreenId.WORLD_MAP) {
            mapNoticeView.text = notice
            mapNoticeView.visibility = View.VISIBLE
        } else {
            mapNoticeView.text = ""
            mapNoticeView.visibility = View.GONE
        }
        mapCanvasView.bind(snap)
        neonSignView.text = snap.barName.uppercase()
        counterPathView.text = snap.pathText
        counterBackButtonView.text = snap.backButtonLabel ?: "Назад"
        filmstripBackButtonView.text = snap.backButtonLabel ?: "Назад"
        filmstripPathView.text = snap.filmstrip.pathText
        filmstripStaminaView.text = snap.filmstrip.enduranceText
        filmstripStateLabelView.text = snap.filmstrip.stateLabel
        updateAmbianceFrame(snap)

        val banner = snap.victoryBanner
        if (banner != null) {
            bannerLayout.visibility = View.VISIBLE
            bannerTitleView.text = banner.title
            bannerStarsView.text = "★".repeat(banner.stars) + "☆".repeat((3 - banner.stars).coerceAtLeast(0))
            bannerMovesView.text = "${snap.movesLabel}: ${banner.moves}"
            bindActionButton(bannerToLevelsButtonView, banner.toLevelsButton)
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
        val undo = snap.undoButton
        if (undo != null) {
            undoButtonView.visibility = View.VISIBLE
            bindActionButton(undoButtonView, undo)
        } else {
            undoButtonView.visibility = View.GONE
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

                // Тап по стеклу выбирает бутылку для перелива и лист не открывает
                val bottleView = BottleView(
                    context = this,
                    capacity = snap.capacity,
                    bottle = bottle
                ).apply {
                    setOnClickListener {
                        game.tapBottleGlass(currentIndex)
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
                            game.tapBottleName(currentIndex)
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

            // Фиксированная высота ряда клеток, чтобы клетка в 1 ряду и клетка в 2 рядах имели одинаковую высоту,
            // а стекло вместимости 6 было выше и шире стекла вместимости 3.
            val rowParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(210)
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
            rackContainer.addView(rowLayout, rowParams)
        }

        val card = snap.drinkCard
        val defeat = snap.defeatScreen
        when {
            snap.screen == ScreenId.DEFEAT -> {
                mapScreen.visibility = View.GONE
                filmstripScreen.visibility = View.GONE
                counterScreen.visibility = View.GONE
                cardSheetScrollView.visibility = View.GONE
                defeatScreenLayout.visibility = View.VISIBLE
                defeatTitleView.text = defeat?.title ?: "Персонаж погиб"
                bindActionButton(
                    newGameButtonView,
                    defeat?.newGameButton ?: ActionButtonSnapshot(title = "Новая игра", enabled = true)
                )
            }
            snap.screen == ScreenId.WORLD_MAP ||
                snap.screen == ScreenId.CITY_MAP ||
                snap.screen == ScreenId.STREET_MAP -> {
                filmstripScreen.visibility = View.GONE
                counterScreen.visibility = View.GONE
                cardSheetScrollView.visibility = View.GONE
                defeatScreenLayout.visibility = View.GONE
                mapScreen.visibility = View.VISIBLE
            }
            snap.screen == ScreenId.LEVEL_FILMSTRIP -> {
                mapScreen.visibility = View.GONE
                counterScreen.visibility = View.GONE
                cardSheetScrollView.visibility = View.GONE
                defeatScreenLayout.visibility = View.GONE
                filmstripScreen.visibility = View.VISIBLE
                renderFilmstripCards(snap.levelCards)
            }
            snap.screen == ScreenId.DRINK_CARD && card != null -> {
                mapScreen.visibility = View.GONE
                filmstripScreen.visibility = View.GONE
                counterScreen.visibility = View.GONE
                defeatScreenLayout.visibility = View.GONE
                cardSheetScrollView.visibility = View.VISIBLE
                renderDrinkCardSheet(card)
            }
            else -> {
                mapScreen.visibility = View.GONE
                filmstripScreen.visibility = View.GONE
                cardSheetScrollView.visibility = View.GONE
                defeatScreenLayout.visibility = View.GONE
                counterScreen.visibility = View.VISIBLE
            }
        }
    }

    private fun renderFilmstripCards(cards: List<LevelCardSnapshot>) {
        filmstripCardsRow.removeAllViews()
        cards.forEach { card ->
            val strokeColor = when {
                !card.unlocked -> Color.parseColor("#3A3644")
                card.bestStars > 0 -> Color.parseColor("#FFE56A")
                else -> Color.parseColor("#79F6FF")
            }
            val bgColor = when {
                !card.unlocked -> Color.parseColor("#18161C")
                card.bestStars > 0 -> Color.parseColor("#241E0E")
                else -> Color.parseColor("#12363A")
            }
            val textColor = when {
                !card.unlocked -> Color.parseColor("#6D6878")
                card.bestStars > 0 -> Color.parseColor("#FFE56A")
                else -> Color.parseColor("#79F6FF")
            }

            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                val pad = dp(10)
                setPadding(pad, pad, pad, pad)
                background = GradientDrawable().apply {
                    setColor(bgColor)
                    setStroke(dp(2), strokeColor)
                }
                alpha = if (card.bright) 1f else 0.55f
                isClickable = card.unlocked
                if (card.unlocked) {
                    setOnClickListener {
                        if (game.tapLevelCard(card.levelNumber)) {
                            render()
                        }
                    }
                }
            }

            val numView = TextView(this).apply {
                text = card.levelNumber.toString()
                setTextColor(textColor)
                textSize = 26f
                typeface = Typeface.MONOSPACE
                gravity = Gravity.CENTER
            }
            val statusText = when {
                !card.unlocked -> "закрыт"
                card.bestStars > 0 -> "★".repeat(card.bestStars)
                else -> "открыт"
            }
            val statusView = TextView(this).apply {
                text = statusText
                setTextColor(textColor)
                textSize = 14f
                typeface = Typeface.MONOSPACE
                gravity = Gravity.CENTER
            }

            cardLayout.addView(
                numView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
            cardLayout.addView(
                statusView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            val params = LinearLayout.LayoutParams(dp(104), dp(140)).apply {
                rightMargin = dp(8)
            }
            filmstripCardsRow.addView(cardLayout, params)
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
                game.tapBack()
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

private fun drinkPixelColor(drink: Drink): Int = (0xFF000000.toInt() or drink.pixelColorRgb)

private data class MapHitTarget(
    val x: Float,
    val y: Float,
    val radius: Float,
    val action: () -> Unit
)

private class PixelMapCanvasView(
    context: Context,
    private val onCityTapped: (String) -> Unit,
    private val onStreetTapped: (String) -> Unit,
    private val onBarTapped: (String) -> Unit
) : View(context) {

    private var snapshot: CounterSnapshot? = null
    private val hitTargets = mutableListOf<MapHitTarget>()

    private val quietFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1A1822")
    }
    private val quietStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#3A3846")
    }
    private val ghostFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#121016")
    }
    private val ghostStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#322F3A")
    }
    private val waterFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#142028")
    }
    private val waterStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#3D5C66")
    }
    private val brightMarkFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#10262A")
    }
    private val brightStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#79F6FF")
    }
    private val dimMarkFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#14121A")
    }
    private val dimStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#3C3846")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
    }
    private val reusablePath = Path()

    fun bind(snap: CounterSnapshot) {
        snapshot = snap
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val tx = event.x
            val ty = event.y
            val hit = hitTargets.minByOrNull { hypot((tx - it.x).toDouble(), (ty - it.y).toDouble()) }
            if (hit != null && hypot((tx - hit.x).toDouble(), (ty - hit.y).toDouble()) <= hit.radius) {
                hit.action()
                return true
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hitTargets.clear()
        val snap = snapshot ?: return
        if (width <= 0 || height <= 0) return

        when (snap.screen) {
            ScreenId.WORLD_MAP -> drawWorldMap(canvas, snap)
            ScreenId.CITY_MAP -> drawCityMap(canvas, snap)
            ScreenId.STREET_MAP -> drawStreetMap(canvas, snap)
            else -> Unit
        }
    }

    private fun toScreen(vbW: Float, vbH: Float, x: Float, y: Float): Pair<Float, Float> {
        val pad = 16f
        val availW = (width - pad * 2f).coerceAtLeast(1f)
        val availH = (height - pad * 2f).coerceAtLeast(1f)
        val scale = min(availW / vbW, availH / vbH)
        val offsetX = (width - vbW * scale) / 2f
        val offsetY = (height - vbH * scale) / 2f
        return (offsetX + x * scale) to (offsetY + y * scale)
    }

    private fun drawPolygon(
        canvas: Canvas,
        vbW: Float,
        vbH: Float,
        pts: List<Pair<Float, Float>>,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        if (pts.isEmpty()) return
        reusablePath.reset()
        pts.forEachIndexed { idx, (vx, vy) ->
            val (sx, sy) = toScreen(vbW, vbH, vx, vy)
            if (idx == 0) reusablePath.moveTo(sx, sy) else reusablePath.lineTo(sx, sy)
        }
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)
        canvas.drawPath(reusablePath, strokePaint)
    }

    private fun projectWorld(lon: Float, lat: Float): Pair<Float, Float> {
        val west = 21.6f
        val east = 40.8f
        val south = 50.2f
        val north = 61.15f
        val x = (lon - west) / (east - west) * 100f
        val y = (north - lat) / (north - south) * 100.5f
        return x to y
    }

    private fun drawWorldMap(canvas: Canvas, snap: CounterSnapshot) {
        val vbW = 100f
        val vbH = 100.5f
        val density = resources.displayMetrics.density

        fun ring(coords: List<Pair<Float, Float>>): List<Pair<Float, Float>> =
            coords.map { (lon, lat) -> projectWorld(lon, lat) }

        val frame = ring(listOf(21.6f to 50.2f, 40.8f to 50.2f, 40.8f to 61.15f, 21.6f to 61.15f))
        val poland = ring(listOf(21.6f to 51.9f, 23.175f to 52.287f, 24.91f to 54.0f, 21.6f to 54.0f))
        val lithuania = ring(listOf(21.6f to 54.0f, 24.91f to 54.0f, 26.594f to 55.668f, 25.2f to 56.05f, 21.6f to 56.05f))
        val latvia = ring(listOf(21.6f to 56.05f, 25.2f to 56.05f, 26.594f to 55.668f, 28.118f to 56.146f, 27.6f to 57.6f, 21.6f to 57.6f))
        val estonia = ring(listOf(21.6f to 57.6f, 27.6f to 57.6f, 28.2f to 59.5f, 21.6f to 59.5f))
        val ukraine = ring(listOf(21.6f to 50.2f, 34.2f to 50.2f, 31.763f to 52.101f, 30.545f to 51.265f, 23.605f to 51.518f, 23.175f to 52.287f, 21.6f to 51.9f))
        val belarus = ring(listOf(23.175f to 52.287f, 23.605f to 51.518f, 30.545f to 51.265f, 31.763f to 52.101f, 32.710f to 53.371f, 30.456f to 55.787f, 28.118f to 56.146f, 26.594f to 55.668f))

        drawPolygon(canvas, vbW, vbH, frame, quietFillPaint, quietStrokePaint)
        drawPolygon(canvas, vbW, vbH, poland, ghostFillPaint, ghostStrokePaint)
        drawPolygon(canvas, vbW, vbH, lithuania, ghostFillPaint, ghostStrokePaint)
        drawPolygon(canvas, vbW, vbH, latvia, ghostFillPaint, ghostStrokePaint)
        drawPolygon(canvas, vbW, vbH, estonia, ghostFillPaint, ghostStrokePaint)
        drawPolygon(canvas, vbW, vbH, ukraine, ghostFillPaint, ghostStrokePaint)
        drawPolygon(canvas, vbW, vbH, belarus, quietFillPaint, quietStrokePaint)

        val borderLabels = listOf(
            Triple(22.4f, 53.0f, "Польша"),
            Triple(22.5f, 55.0f, "Литва"),
            Triple(23.0f, 56.9f, "Латвия"),
            Triple(23.4f, 58.5f, "Эстония"),
            Triple(26.0f, 50.85f, "Украина"),
            Triple(24.7f, 55.2f, "Беларусь"),
            Triple(36.4f, 58.6f, "Россия")
        )
        textPaint.color = Color.parseColor("#3E3C48")
        textPaint.textSize = 10f * density
        textPaint.textAlign = Paint.Align.LEFT
        for ((lon, lat, label) in borderLabels) {
            val (vx, vy) = projectWorld(lon, lat)
            val (sx, sy) = toScreen(vbW, vbH, vx, vy)
            canvas.drawText(label, sx, sy, textPaint)
        }

        val cityCoords = mapOf(
            "Минск" to Triple(27.562f, 53.902f, "above"),
            "Витебск" to Triple(30.207f, 55.193f, "above"),
            "Москва" to Triple(37.617f, 55.751f, "above"),
            "Могилёв" to Triple(30.345f, 53.917f, "right"),
            "Гомель" to Triple(30.975f, 52.434f, "below"),
            "Смоленск" to Triple(32.045f, 54.783f, "right"),
            "Ленинград" to Triple(30.314f, 59.939f, "below")
        )
        val worldCities = snap.worldMap?.cities.orEmpty()
        for (city in worldCities) {
            val (lon, lat, place) = cityCoords[city.name] ?: continue
            val (vx, vy) = projectWorld(lon, lat)
            val (sx, sy) = toScreen(vbW, vbH, vx, vy)
            val radius = (if (city.bright) 7f else 4.5f) * density
            canvas.drawCircle(
                sx,
                sy,
                radius,
                if (city.bright) brightMarkFillPaint else dimMarkFillPaint
            )
            canvas.drawCircle(
                sx,
                sy,
                radius,
                if (city.bright) brightStrokePaint else dimStrokePaint
            )

            textPaint.color = if (city.bright) Color.parseColor("#79F6FF") else Color.parseColor("#5C5868")
            textPaint.textSize = 13f * density
            when (place) {
                "right" -> {
                    textPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(city.name, sx + 10f * density, sy + 4f * density, textPaint)
                }
                "below" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText(city.name, sx, sy + 18f * density, textPaint)
                }
                else -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText(city.name, sx, sy - 11f * density, textPaint)
                }
            }

            val fraction = city.fractionText
            if (fraction != null) {
                textPaint.color = Color.parseColor("#FFE56A")
                textPaint.textSize = 11f * density
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(fraction, sx, sy + 18f * density, textPaint)
            }

            hitTargets.add(
                MapHitTarget(
                    x = sx,
                    y = sy,
                    radius = 28f * density,
                    action = { onCityTapped(city.name) }
                )
            )
        }
    }

    private fun drawCityMap(canvas: Canvas, snap: CounterSnapshot) {
        val density = resources.displayMetrics.density
        val dimmedStreets = snap.cityMap?.streets?.filter { it.dimmed }.orEmpty()
        when (snap.cityName) {
            "Витебск" -> {
                val vbW = 100f
                val vbH = 100f
                val shape = listOf(
                    6f to 42f, 10f to 18f, 34f to 6f, 62f to 12f, 90f to 36f,
                    96f to 58f, 70f to 92f, 36f to 100f, 12f to 74f
                )
                drawPolygon(canvas, vbW, vbH, shape, dimMarkFillPaint, dimStrokePaint)

                val dimmedGeom = listOf(
                    Triple(22f to 62f, 40f to 78f, 18f to 84f),
                    Triple(36f to 48f, 52f to 52f, 26f to 45f)
                )
                dimmedStreets.forEachIndexed { idx, street ->
                    val (start, end, labelPos) = dimmedGeom.getOrElse(idx) { dimmedGeom.last() }
                    val (dx1, dy1) = toScreen(vbW, vbH, start.first, start.second)
                    val (dx2, dy2) = toScreen(vbW, vbH, end.first, end.second)
                    canvas.drawLine(dx1, dy1, dx2, dy2, dimStrokePaint)
                    textPaint.color = Color.parseColor("#6D6878")
                    textPaint.textSize = 12f * density
                    textPaint.textAlign = Paint.Align.LEFT
                    val (dlx, dly) = toScreen(vbW, vbH, labelPos.first, labelPos.second)
                    canvas.drawText(street.name, dlx, dly, textPaint)
                }

                val (sx1, sy1) = toScreen(vbW, vbH, 54f, 28f)
                val (sx2, sy2) = toScreen(vbW, vbH, 54f, 70f)
                canvas.drawLine(sx1, sy1, sx2, sy2, brightStrokePaint)
                textPaint.color = Color.parseColor("#79F6FF")
                textPaint.textSize = 15f * density
                textPaint.textAlign = Paint.Align.LEFT
                val (slx, sly) = toScreen(vbW, vbH, 58f, 46f)
                canvas.drawText(snap.cityMap?.playableStreet?.name ?: "Суворова", slx, sly, textPaint)

                val midX = (sx1 + sx2) / 2f
                val midY = (sy1 + sy2) / 2f
                hitTargets.add(
                    MapHitTarget(
                        x = midX,
                        y = midY,
                        radius = 48f * density,
                        action = { onStreetTapped("Суворова") }
                    )
                )
            }
            "Москва" -> {
                val vbW = 100f
                val vbH = 110f
                val shape = listOf(
                    18f to 98f, 28f to 78f, 16f to 58f, 10f to 40f, 22f to 18f, 48f to 8f,
                    74f to 6f, 98f to 30f, 100f to 58f, 84f to 74f, 58f to 86f, 30f to 96f
                )
                val zelenograd = listOf(34f to 0f, 48f to 0f, 50f to 7f, 36f to 9f)
                drawPolygon(canvas, vbW, vbH, shape, dimMarkFillPaint, dimStrokePaint)
                drawPolygon(canvas, vbW, vbH, zelenograd, dimMarkFillPaint, dimStrokePaint)

                val dimmedGeom = listOf(
                    Triple(14f to 50f, 30f to 58f, 10f to 46f),
                    Triple(44f to 24f, 58f to 38f, 36f to 20f)
                )
                dimmedStreets.forEachIndexed { idx, street ->
                    val (start, end, labelPos) = dimmedGeom.getOrElse(idx) { dimmedGeom.last() }
                    val (dx1, dy1) = toScreen(vbW, vbH, start.first, start.second)
                    val (dx2, dy2) = toScreen(vbW, vbH, end.first, end.second)
                    canvas.drawLine(dx1, dy1, dx2, dy2, dimStrokePaint)
                    textPaint.color = Color.parseColor("#6D6878")
                    textPaint.textSize = 12f * density
                    textPaint.textAlign = Paint.Align.LEFT
                    val (dlx, dly) = toScreen(vbW, vbH, labelPos.first, labelPos.second)
                    canvas.drawText(street.name, dlx, dly, textPaint)
                }

                val (px, py) = toScreen(vbW, vbH, 68f, 26f)
                canvas.drawCircle(px, py, 10f * density, brightMarkFillPaint)
                canvas.drawCircle(px, py, 10f * density, brightStrokePaint)
                textPaint.color = Color.parseColor("#79F6FF")
                textPaint.textSize = 15f * density
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(snap.cityMap?.playableStreet?.name ?: "Патрики", px, py - 14f * density, textPaint)

                hitTargets.add(
                    MapHitTarget(
                        x = px,
                        y = py,
                        radius = 40f * density,
                        action = { onStreetTapped("Патрики") }
                    )
                )
            }
            else -> {
                val vbW = 100f
                val vbH = 64f
                val shape = listOf(
                    0f to 48f, 8f to 28f, 28f to 12f, 55f to 6f, 78f to 10f, 100f to 34f,
                    96f to 58f, 74f to 86f, 42f to 100f, 16f to 84f, 2f to 64f
                )
                drawPolygon(canvas, vbW, vbH, shape, dimMarkFillPaint, dimStrokePaint)

                val dimmedGeom = listOf(
                    Triple(18f to 40f, 28f to 46f, 10f to 40f),
                    Triple(30f to 48f, 46f to 44f, 28f to 54f)
                )
                dimmedStreets.forEachIndexed { idx, street ->
                    val (start, end, labelPos) = dimmedGeom.getOrElse(idx) { dimmedGeom.last() }
                    val (dx1, dy1) = toScreen(vbW, vbH, start.first, start.second)
                    val (dx2, dy2) = toScreen(vbW, vbH, end.first, end.second)
                    canvas.drawLine(dx1, dy1, dx2, dy2, dimStrokePaint)
                    textPaint.color = Color.parseColor("#6D6878")
                    textPaint.textSize = 12f * density
                    textPaint.textAlign = Paint.Align.LEFT
                    val (dlx, dly) = toScreen(vbW, vbH, labelPos.first, labelPos.second)
                    canvas.drawText(street.name, dlx, dly, textPaint)
                }

                val (sx1, sy1) = toScreen(vbW, vbH, 46f, 30f)
                val (sx2, sy2) = toScreen(vbW, vbH, 32f, 22f)
                canvas.drawLine(sx1, sy1, sx2, sy2, brightStrokePaint)
                textPaint.color = Color.parseColor("#79F6FF")
                textPaint.textSize = 15f * density
                textPaint.textAlign = Paint.Align.LEFT
                val (slx, sly) = toScreen(vbW, vbH, 24f, 18f)
                canvas.drawText(snap.cityMap?.playableStreet?.name ?: "Зыбицкая", slx, sly, textPaint)

                val midX = (sx1 + sx2) / 2f
                val midY = (sy1 + sy2) / 2f
                hitTargets.add(
                    MapHitTarget(
                        x = midX,
                        y = midY,
                        radius = 48f * density,
                        action = { onStreetTapped("Зыбицкая") }
                    )
                )
            }
        }
    }

    private fun along(a: Pair<Float, Float>, b: Pair<Float, Float>, t: Float): Pair<Float, Float> =
        (a.first + (b.first - a.first) * t) to (a.second + (b.second - a.second) * t)

    private fun drawStreetMap(canvas: Canvas, snap: CounterSnapshot) {
        val density = resources.displayMetrics.density
        val bars = snap.streetMap?.bars.orEmpty()
        when (snap.streetName) {
            "Суворова" -> {
                val vbW = 100f
                val vbH = 100f
                val (x1, y1) = toScreen(vbW, vbH, 28f, 8f)
                val (x2, y2) = toScreen(vbW, vbH, 28f, 94f)
                canvas.drawLine(x1, y1, x2, y2, dimStrokePaint)
                textPaint.color = Color.parseColor("#6D6878")
                textPaint.textSize = 12f * density
                textPaint.textAlign = Paint.Align.LEFT
                val (nx, ny) = toScreen(vbW, vbH, 34f, 12f)
                canvas.drawText("север", nx, ny, textPaint)

                bars.forEachIndexed { idx, bar ->
                    val vy = 20f + idx * 20f
                    val (bx, by) = toScreen(vbW, vbH, 28f, vy)
                    canvas.drawCircle(bx, by, 9f * density, brightMarkFillPaint)
                    canvas.drawCircle(bx, by, 9f * density, brightStrokePaint)
                    textPaint.color = Color.parseColor("#79F6FF")
                    textPaint.textSize = 14f * density
                    textPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(bar.name, bx + 14f * density, by + 5f * density, textPaint)
                    hitTargets.add(
                        MapHitTarget(
                            x = bx,
                            y = by,
                            radius = 30f * density,
                            action = { onBarTapped(bar.name) }
                        )
                    )
                }
            }
            "Патрики" -> {
                val vbW = 100f
                val vbH = 100f
                // Четыре улицы вокруг пруда (~130×130 м), контур повёрнут примерно на 40° от меридиана:
                // 0: Малая Бронная, 1: Большой Патриарший, 2: Малый Патриарший, 3: Ермолаевский
                val ring = listOf(42f to 16f, 84f to 38f, 58f to 86f, 16f to 58f)
                val pond = listOf(50f to 34f, 64f to 48f, 50f to 64f, 36f to 50f)
                for (i in ring.indices) {
                    val p = ring[i]
                    val q = ring[(i + 1) % ring.size]
                    val (x1, y1) = toScreen(vbW, vbH, p.first, p.second)
                    val (x2, y2) = toScreen(vbW, vbH, q.first, q.second)
                    canvas.drawLine(x1, y1, x2, y2, dimStrokePaint)
                }
                drawPolygon(canvas, vbW, vbH, pond, waterFillPaint, waterStrokePaint)

                val surrounding = snap.streetMap?.surroundingStreets.orEmpty()
                val streetLabelPositions = listOf(
                    Triple(66f, 22f, Paint.Align.CENTER), // Малая Бронная (северо-восток)
                    Triple(78f, 70f, Paint.Align.LEFT),   // Большой Патриарший (юго-восток)
                    Triple(26f, 82f, Paint.Align.CENTER), // Малый Патриарший (юго-запад)
                    Triple(20f, 30f, Paint.Align.RIGHT)   // Ермолаевский (северо-запад)
                )
                textPaint.color = Color.parseColor("#6D6878")
                textPaint.textSize = 10.5f * density
                surrounding.forEachIndexed { idx, sName ->
                    val (lx, ly, align) = streetLabelPositions.getOrElse(idx) { streetLabelPositions.last() }
                    val (sx, sy) = toScreen(vbW, vbH, lx, ly)
                    textPaint.textAlign = align
                    canvas.drawText(sName, sx, sy, textPaint)
                }

                // Киану на Малой Бронной (сторона 0), Bro&n и Зойка на Большом Патриаршем (сторона 1)
                val spots = listOf(0.42f to 0, 0.32f to 1, 0.72f to 1)
                bars.forEachIndexed { idx, bar ->
                    val (t, side) = spots.getOrElse(idx) { 0.5f to 0 }
                    val (vx, vy) = along(ring[side], ring[(side + 1) % ring.size], t)
                    val (bx, by) = toScreen(vbW, vbH, vx, vy)
                    canvas.drawCircle(bx, by, 9f * density, brightMarkFillPaint)
                    canvas.drawCircle(bx, by, 9f * density, brightStrokePaint)
                    textPaint.color = Color.parseColor("#79F6FF")
                    textPaint.textSize = 14f * density
                    textPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(bar.name, bx + 12f * density, by - 4f * density, textPaint)
                    hitTargets.add(
                        MapHitTarget(
                            x = bx,
                            y = by,
                            radius = 30f * density,
                            action = { onBarTapped(bar.name) }
                        )
                    )
                }
            }
            else -> {
                val vbW = 100f
                val vbH = 84f
                val nw = 8f to 20f
                val se = 94f to 74f
                val (x1, y1) = toScreen(vbW, vbH, nw.first, nw.second)
                val (x2, y2) = toScreen(vbW, vbH, se.first, se.second)
                canvas.drawLine(x1, y1, x2, y2, dimStrokePaint)
                textPaint.color = Color.parseColor("#6D6878")
                textPaint.textSize = 12f * density
                textPaint.textAlign = Paint.Align.LEFT
                val (nx, ny) = toScreen(vbW, vbH, 6f, 12f)
                canvas.drawText("северо-запад (западный конец)", nx, ny, textPaint)

                // Все восемь баров сгруппированы у западного конца штриха и идут с запада на восток
                bars.forEachIndexed { idx, bar ->
                    val (vx, vy) = along(nw, se, 0.04f + idx * 0.065f)
                    val (bx, by) = toScreen(vbW, vbH, vx, vy)
                    canvas.drawCircle(bx, by, 6.5f * density, brightMarkFillPaint)
                    canvas.drawCircle(bx, by, 6.5f * density, brightStrokePaint)
                    textPaint.color = Color.parseColor("#79F6FF")
                    textPaint.textSize = 11f * density
                    textPaint.textAlign = Paint.Align.CENTER
                    val labelOffsetY = if (idx % 2 == 0) -12f * density else 19f * density
                    canvas.drawText(bar.name, bx, by + labelOffsetY, textPaint)
                    hitTargets.add(
                        MapHitTarget(
                            x = bx,
                            y = by,
                            radius = 18f * density,
                            action = { onBarTapped(bar.name) }
                        )
                    )
                }
            }
        }
    }
}

private class BarBackdropView(context: Context) : View(context) {
    private val shelfPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#1D1926")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val stepY = (height / 6f).coerceAtLeast(24f)
        var y = stepY
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, shelfPaint)
            y += stepY
        }
    }
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
    private val rubyEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#A8243C")
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

        val density = resources.displayMetrics.density
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

        // Общее горлышко единого размера для всех вместимостей сверху внутри габарита стекла
        val neckHeight = (14f * density).coerceAtMost(glassTotalHeight * 0.22f)
        val neckWidth = (24f * density).coerceAtMost(glassTotalWidth * 0.65f)
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
                if (drink == Drink.GUINNESS_DRAUGHT) {
                    canvas.drawRect(rect, rubyEdgePaint)
                }
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
    private val rubyEdgePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.parseColor("#A8243C")
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
                    if (illustration.subtleRubyEdge) {
                        val touchesRim =
                            (c > 0 && rowStr[c - 1] == '2') ||
                                (c + 1 < rowStr.length && rowStr[c + 1] == '2') ||
                                (r > 0 && mask[r - 1].getOrNull(c) == '2') ||
                                (r + 1 < rows && mask[r + 1].getOrNull(c) == '2')
                        if (touchesRim) {
                            canvas.drawRect(cellRect, rubyEdgePaint)
                        }
                    }
                }
            }
        }
    }
}
