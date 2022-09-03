package by.varyvoda.lemer

import by.varyvoda.lemer.binding.FieldSliderBinding
import by.varyvoda.lemer.domain.LemerGenerator
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.BarChart
import javafx.scene.chart.XYChart
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import java.text.DecimalFormat
import java.util.concurrent.CompletableFuture
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.streams.toList

const val n = 16

val maxM = 2.0.pow(n.toDouble()).toInt()

class HelloController {
    @FXML
    private lateinit var aField: TextField

    @FXML
    private lateinit var aSlider: Slider

    @FXML
    private lateinit var rField: TextField

    @FXML
    private lateinit var rSlider: Slider

    @FXML
    private lateinit var mField: TextField

    @FXML
    private lateinit var mSlider: Slider

    @FXML
    private lateinit var chart: BarChart<String, Double>

    @FXML
    private lateinit var chartProgress: ProgressIndicator

    @FXML
    private lateinit var mathField: TextField

    @FXML
    private lateinit var mathProgress: ProgressIndicator

    @FXML
    private lateinit var dField: TextField

    @FXML
    private lateinit var dProgress: ProgressIndicator

    @FXML
    private lateinit var sigmaField: TextField

    @FXML
    private lateinit var sigmaProgress: ProgressIndicator

    @FXML
    private lateinit var pField: TextField

    @FXML
    private lateinit var pProgress: ProgressIndicator

    @FXML
    private lateinit var lField: TextField

    @FXML
    private lateinit var lProgress: ProgressIndicator

    private lateinit var aBinding: FieldSliderBinding

    private lateinit var rBinding: FieldSliderBinding

    private lateinit var mBinding: FieldSliderBinding

    private var calculation: CompletableFuture<Void>? = null

    @FXML
    fun initialize() {
        chart.isLegendVisible = false
        chart.animated = false
        chart.xAxis.isAutoRanging = true
        chart.xAxis.animated = false
        chart.yAxis.isAutoRanging = true
        chart.yAxis.animated = false

        aBinding = FieldSliderBinding(aField, aSlider, 2, maxM - 1)
        rBinding = FieldSliderBinding(rField, rSlider, 1, maxM - 1)
        mBinding = FieldSliderBinding(mField, mSlider, 2, maxM)

        run()

        aBinding.valueProperty().addListener { _, _, _ -> run() }
        rBinding.valueProperty().addListener { _, _, _ -> run() }
        mBinding.valueProperty().addListener { _, _, _ -> run() }

        chart.sceneProperty().addListener { _, _, scene ->
            scene.addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ENTER) {
                    run()
                }
            }
            scene.addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.R && it.isShiftDown) {
                    aBinding.random()
                    rBinding.random()
                    mBinding.random()
                    run()
                    it.consume()
                }
            }
        }
    }

    private fun run() {
        val generator = LemerGenerator(aBinding.value, rBinding.value, mBinding.value)
        val toFixed2 = DecimalFormat("#.##")

        calculation?.cancel(true)
        allInProgress()
        calculation = CompletableFuture.runAsync {
            val n = 50_000
            val values = generator.batch(n)
            try {
                val minMax = values.stream().mapToDouble { it }.summaryStatistics()
                val varietyRange = minMax.max - minMax.min
                val intervalCount = 20
                val intervalLength = varietyRange / intervalCount
                if (intervalLength == 0.0) {
                    Platform.runLater { setCharts(emptyList()) }
                    return@runAsync
                }

                var previousMath = 0.0
                val maths =
                    IntStream.range(0, values.size)
                        .mapToObj {
                            val math = previousMath * (n - 1) / n + values[it] / n
                            previousMath = math
                            return@mapToObj math
                        }
                        .toList()
                val sortedValues = values.sortedBy { it }

                var intervalStart = sortedValues.first()
                var currentInterval = mutableListOf<Double>()
                val intervals = mutableListOf<List<Double>>()
                maths.forEach {
                    if (it < intervalStart + intervalLength) {
                        currentInterval.add(it)
                    } else {
                        intervals.add(currentInterval)
                        currentInterval = mutableListOf(it)
                        intervalStart = it
                    }
                }
                intervals.add(currentInterval)

                val histograms =
                    intervals
                        .filter { it.isNotEmpty() }
                        .map { interval ->
                            val intervalExtremes =
                                interval.stream()
                                    .mapToDouble { it }
                                    .summaryStatistics()
                            val mCount = maths.stream()
                                .filter { it >= intervalExtremes.min && it <= intervalExtremes.max }
                                .count()
                            val c = mCount.toDouble() / n
                            val fromX = interval.first()
                            val toX = interval.last()

                            return@map XYChart.Data(
                                "${toFixed2.format(fromX)} - ${toFixed2.format(toX)}",
                                c
                            )
                        }
                        .toList()

                Platform.runLater {
                    setCharts(histograms)
                }
            } finally {
                Platform.runLater { disable(chartProgress) }
            }
            val math = values.sum() * 1 / n
            Platform.runLater {
                mathField.text = math.toString()
                disable(mathProgress)
            }

            val d = values.reduce { acc, d -> acc + (d - math).pow(2) } * 1 / n
            Platform.runLater {
                dField.text = d.toString()
                disable(dProgress)
            }

            val sigma = sqrt(d)
            Platform.runLater {
                sigmaField.text = sigma.toString()
                disable(sigmaProgress)
            }


            val v = 1_000_000
            val periodGenerator = generator.duplicate()
            IntStream.range(0, v).forEach { periodGenerator.next() }
            val xV = periodGenerator.next()

            periodGenerator.reset()
            var i1: Int? = null
            var i2: Int? = null
            Stream.iterate(0) { it + 1 }
                .takeWhile { i1 == null || i2 == null }
                .forEach {
                    if (xV == periodGenerator.next()) {
                        if (i1 == null) i1 = it
                        else i2 = it
                    }
                }
            try {
                val p = i2!! - i1!!
                Platform.runLater { pField.text = p.toString() }

                periodGenerator.reset()
                val cache = mutableListOf<Double>()
                var i3: Int? = null
                Stream.iterate(0) { it + 1 }
                    .takeWhile { i3 == null }
                    .peek { cache.add(periodGenerator.next()) }
                    .skip(p.toLong())
                    .forEach {
                        if (cache[it - p] == periodGenerator.next()) {
                            i3 = it - p
                        }
                    }
                Platform.runLater { lField.text = (i3!! + p).toString() }
            } catch (e: NullPointerException) {
                Platform.runLater {
                    pField.text = "Failed"
                    lField.text = "Failed"
                }
            } finally {
                Platform.runLater {
                    disable(pProgress)
                    disable(lProgress)
                }
            }
        }
    }

    private fun setCharts(charts: List<XYChart.Data<String, Double>>) {
        chart.data.setAll(listOf(XYChart.Series(FXCollections.observableArrayList(charts))))
    }

    private fun allInProgress() {
        enable(chartProgress)
        enable(mathProgress)
        enable(dProgress)
        enable(sigmaProgress)
        enable(pProgress)
        enable(lProgress)
    }

    private fun enable(progressIndicator: ProgressIndicator) {
        progressIndicator.isVisible = true
        progressIndicator.progress = ProgressIndicator.INDETERMINATE_PROGRESS
    }

    private fun disable(progressIndicator: ProgressIndicator) {
        progressIndicator.isVisible = false
        progressIndicator.progress = 0.0
    }
}