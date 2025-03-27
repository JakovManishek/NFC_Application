package com.example.myapplication

import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.nfc.Tag
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * Главная активность приложения, реализующая NFC-считывание и поиск пути между комнатами.
 */
class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // region Constants

    /**
     * Соответствие ID NFC-меток номерам комнат.
     */
    private val nfcIdMap: Map<String, String> = mapOf(
        "297851516128" to "207",
        "2920324750516128" to "214",
        "2924451516128" to "215",
        "292102051516128" to "223"
    )

    /**
     * Граф путей между комнатами и точками навигации.
     * Формат: "точка" -> список пар (вес, следующая точка)
     */
    private val navigationGraph: Map<String, List<Pair<Double, String>>> = createNavigationGraph()

    /**
     * Координаты точек для отображения на карте.
     */
    private val coordinatesMap: Map<String, Pair<Int, Int>> = createCoordinatesMap()

    // endregion

    // region UI Elements

    private lateinit var startRoomTextView: TextView
    private lateinit var endRoomEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var lineView: LineView

    // endregion

    // region Properties

    private var startRoom: String? = null
    private var endRoom: String? = null

    // Настройки отображения пути
    private val pathDisplaySettings = PathDisplaySettings(
        offsetX = 720f,
        offsetY = 155f,
        scaleX = 0.73f,
        scaleY = 0.72f,
        pathColor = Color.RED,
        pathWidth = 5f
    )

    // endregion

    // region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupNfcReader()
        setupSearchButton()
    }

    // endregion

    // region NFC Callbacks

    override fun onTagDiscovered(tag: Tag?) {
        tag?.let {
            val tagId = it.id.joinToString("") { byte -> (byte.toInt() and 0xFF).toString() }
            runOnUiThread {
                startRoom = nfcIdMap[tagId]
                startRoomTextView.text = startRoom ?: "Неизвестная метка"
            }
        }
    }

    // endregion

    // region Private Methods

    private fun initViews() {
        startRoomTextView = findViewById(R.id.text1)
        endRoomEditText = findViewById(R.id.editText)
        searchButton = findViewById(R.id.btnSearch)
        lineView = findViewById(R.id.lineView)
    }

    private fun setupNfcReader() {
        NfcAdapter.getDefaultAdapter(this)?.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A,
            null
        )
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            endRoom = endRoomEditText.text.toString()
            startRoom?.let { start ->
                findAndDisplayPath(start, endRoom ?: return@setOnClickListener)
            }
        }
    }

    private fun findAndDisplayPath(start: String, end: String) {
        val shortestPath = findShortestPath(navigationGraph, start, end)
        shortestPath?.let { path ->
            val points = path.flatMap { room ->
                coordinatesMap[room]?.let { coords ->
                    listOf(
                        pathDisplaySettings.offsetX + pathDisplaySettings.scaleX * coords.first,
                        pathDisplaySettings.offsetY + pathDisplaySettings.scaleY * coords.second
                    )
                } ?: emptyList()
            }.toFloatArray()

            lineView.setPath(points, pathDisplaySettings.pathColor, pathDisplaySettings.pathWidth)
        }
    }

    // endregion

    // region Pathfinding

    /**
     * Поиск кратчайшего пути между двумя точками с использованием BFS.
     */
    private fun findShortestPath(
        graph: Map<String, List<Pair<Double, String>>>,
        start: String,
        end: String
    ): List<String>? {
        val queue: Queue<String> = LinkedList()
        val visited = mutableSetOf<String>()
        val predecessors = mutableMapOf<String, String?>()

        queue.add(start)
        visited.add(start)
        predecessors[start] = null

        while (queue.isNotEmpty()) {
            val current = queue.poll()

            if (current == end) {
                return reconstructPath(predecessors, start, end)
            }

            graph[current]?.forEach { (_, nextRoom) ->
                if (!visited.contains(nextRoom)) {
                    queue.add(nextRoom)
                    visited.add(nextRoom)
                    predecessors[nextRoom] = current
                }
            }
        }

        return null
    }

    /**
     * Восстановление пути из карты предшественников.
     */
    private fun reconstructPath(
        predecessors: Map<String, String?>,
        start: String,
        end: String
    ): List<String> {
        val path = mutableListOf<String>()
        var current: String? = end

        while (current != null) {
            path.add(current)
            current = predecessors[current]
        }

        return path.reversed()
    }

    // endregion

    // region View Classes

    /**
     * Кастомная View для отображения пути на карте.
     */
    class LineView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : View(context, attrs) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var pathPoints: FloatArray? = null

        /**
         * Устанавливает точки пути и его стиль.
         */
        fun setPath(points: FloatArray, color: Int, width: Float) {
            paint.color = color
            paint.strokeWidth = width
            this.pathPoints = points
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            pathPoints?.let { points ->
                if (points.size >= 4) {
                    for (i in 0 until points.size - 2 step 2) {
                        canvas.drawLine(
                            points[i], points[i + 1],
                            points[i + 2], points[i + 3],
                            paint
                        )
                    }
                }
            }
        }
    }

    // endregion

    // region Data Initialization

    private data class PathDisplaySettings(
        val offsetX: Float,
        val offsetY: Float,
        val scaleX: Float,
        val scaleY: Float,
        val pathColor: Int,
        val pathWidth: Float
    )

    private fun createNavigationGraph(): Map<String, List<Pair<Double, String>>> {
        return mapOf(
            "201" to listOf(1.0 to "k-201"),
            "202" to listOf(1.0 to "k-202"),
            "203" to listOf(1.0 to "k-203"),
            "204" to listOf(1.0 to "k-204"),
            "205" to listOf(1.0 to "k-205"),
            "206" to listOf(1.0 to "k-206"),
            "207" to listOf(1.0 to "k-207"),
            "208" to listOf(1.0 to "k-208"),
            "211" to listOf(1.0 to "k-211"),
            "212" to listOf(1.0 to "k-212"),
            "213" to listOf(1.0 to "k-213"),
            "214" to listOf(1.0 to "k-214"),
            "215" to listOf(1.0 to "k-215"),
            "216" to listOf(1.0 to "k-216"),
            "217" to listOf(1.0 to "k-217"),
            "218" to listOf(1.0 to "k-218"),
            "219" to listOf(1.0 to "k-219"),
            "220" to listOf(1.0 to "k-220"),
            "221" to listOf(1.0 to "k-221"),
            "222" to listOf(1.0 to "k-222"),
            "223" to listOf(1.0 to "k-223"),
            "224" to listOf(1.0 to "k-224"),
            "225" to listOf(1.0 to "k-225"),
            "226" to listOf(1.0 to "k-226"),
            "227" to listOf(1.0 to "k-227"),
            "228" to listOf(1.0 to "k-228"),
            "229" to listOf(1.0 to "k-229"),
            "wc1" to listOf(1.0 to "k-wc1"),
            "wc2" to listOf(1.0 to "k-wc2"),

            "k-201" to listOf(7.0 to "k-2", 1.0 to "k-202", 1.0 to "201"),
            "k-202" to listOf(1.0 to "k-201", 0.6 to "k-5", 1.0 to "202"),
            "k-203" to listOf(0.4 to "k-5", 2.0 to "k-204", 1.0 to "203"),
            "k-204" to listOf(2.0 to "k-203", 1.0 to "k-205", 1.0 to "204"),
            "k-205" to listOf(1.0 to "k-204", 1.0 to "k-206", 1.0 to "205"),
            "k-206" to listOf(1.0 to "k-205", 1.0 to "k-207", 1.0 to "206"),
            "k-207" to listOf(1.0 to "k-206", 0.5 to "k-208", 1.0 to "207"),
            "k-208" to listOf(0.5 to "k-207", 5.0 to "k-4", 1.0 to "208"),
            "k-211" to listOf(1.0 to "k-4", 1.0 to "k-212", 1.0 to "211"),
            "k-212" to listOf(1.0 to "k-211", 1.0 to "k-213", 1.0 to "212"),
            "k-213" to listOf(1.0 to "k-212", 0.6 to "k-216", 1.0 to "213"),
            "k-214" to listOf(0.6 to "k-216", 1.0 to "k-215", 1.0 to "214"),
            "k-215" to listOf(1.0 to "k-214", 3.5 to "k-wc2", 1.0 to "215"),
            "k-216" to listOf(0.6 to "k-213", 0.6 to "k-214", 1.0 to "216"),
            "k-217" to listOf(1.5 to "k-1", 0.5 to "k-wc1", 1.0 to "217"),
            "k-218" to listOf(0.5 to "k-228", 0.5 to "k-219", 1.0 to "218"),
            "k-219" to listOf(0.5 to "k-218", 0.5 to "k-220", 1.0 to "219"),
            "k-220" to listOf(0.5 to "k-219", 0.2 to "k-227", 1.0 to "220"),
            "k-221" to listOf(0.6 to "k-225", 0.3 to "k-226", 1.0 to "221"),
            "k-222" to listOf(0.3 to "k-224", 1.0 to "k-225", 1.0 to "222"),
            "k-223" to listOf(1.5 to "k-224", 1.0 to "223"),
            "k-224" to listOf(1.5 to "k-223", 0.3 to "k-222", 1.0 to "224"),
            "k-225" to listOf(1.0 to "k-222", 0.6 to "k-221", 1.0 to "225"),
            "k-226" to listOf(0.3 to "k-221", 1.5 to "k-227", 1.0 to "226"),
            "k-227" to listOf(0.2 to "k-220", 1.5 to "k-226", 1.0 to "227"),
            "k-228" to listOf(0.5 to "k-218", 0.5 to "k-wc1", 1.0 to "228"),
            "k-229" to listOf(0.5 to "k-217", 0.5 to "k-wc1", 1.0 to "229"),

            "k-wc1" to listOf(0.5 to "k-228", 0.5 to "k-229", 1.0 to "wc1"),
            "k-wc2" to listOf(1.5 to "k-5", 3.5 to "k-215", 1.0 to "wc2"),
            "k-1" to listOf(1.5 to "k-217", 1.0 to "k-2"),
            "k-2" to listOf(1.0 to "k-1", 5.0 to "k-3", 7.0 to "k-201"),
            "k-3" to listOf(5.0 to "k-2"),
            "k-4" to listOf(1.0 to "k-211", 5.0 to "k-208"),
            "k-5" to listOf(0.6 to "k-202", 0.4 to "k-203", 1.5 to "k-wc2")
        )
    }

    private fun createCoordinatesMap(): Map<String, Pair<Int, Int>> {
        return mapOf(
            "201" to Pair(1300, 283),
            "202" to Pair(1383, 283),
            "203" to Pair(1463, 283),
            "204" to Pair(1615, 283),
            "205" to Pair(1695, 283),
            "206" to Pair(1779, 283),
            "207" to Pair(1863, 283),
            "208" to Pair(1921, 424),
            "211" to Pair(1787, 812),
            "212" to Pair(1703, 812),
            "213" to Pair(1620, 812),
            "214" to Pair(1513, 812),
            "215" to Pair(1343, 756),
            "216" to Pair(1562, 614),
            "217" to Pair(701, 368),
            "218" to Pair(558, 368),
            "219" to Pair(525, 368),
            "220" to Pair(493, 368),
            "221" to Pair(340, 368),
            "222" to Pair(175, 368),
            "223" to Pair(28, 178),
            "224" to Pair(150, 178),
            "225" to Pair(280, 178),
            "226" to Pair(360, 178),
            "227" to Pair(480, 178),
            "228" to Pair(588, 178),
            "229" to Pair(665, 178),
            "k-201" to Pair(1300, 376),
            "k-202" to Pair(1383, 376),
            "k-203" to Pair(1463, 376),
            "k-204" to Pair(1615, 376),
            "k-205" to Pair(1695, 376),
            "k-206" to Pair(1779, 376),
            "k-207" to Pair(1863, 376),
            "k-208" to Pair(1863, 424),
            "k-211" to Pair(1787, 756),
            "k-212" to Pair(1703, 756),
            "k-213" to Pair(1620, 756),
            "k-214" to Pair(1513, 756),
            "k-215" to Pair(1433, 756),
            "k-216" to Pair(1562, 756),
            "k-217" to Pair(701, 279),
            "k-218" to Pair(558, 279),
            "k-219" to Pair(525, 279),
            "k-220" to Pair(493, 279),
            "k-221" to Pair(340, 279),
            "k-222" to Pair(175, 279),
            "k-223" to Pair(28, 279),
            "k-224" to Pair(150, 279),
            "k-225" to Pair(280, 279),
            "k-226" to Pair(360, 279),
            "k-227" to Pair(480, 279),
            "k-228" to Pair(558, 279),
            "k-229" to Pair(665, 279),
            "k-1" to Pair(812, 279),
            "k-2" to Pair(812, 376),
            "k-3" to Pair(812, 729),
            "k-4" to Pair(1863, 756),
            "k-5" to Pair(1433, 376),
            "wc1" to Pair(628, 368),
            "wc2" to Pair(1349, 503),
            "k-wc1" to Pair(628, 279),
            "k-wc2" to Pair(1433, 503)
        )
    }

    // endregion
}