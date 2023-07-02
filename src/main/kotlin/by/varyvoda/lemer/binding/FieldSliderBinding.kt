package by.varyvoda.lemer.binding

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Slider
import javafx.scene.control.TextField

class FieldSliderBinding(
    private val field: TextField,
    private val slider: Slider,
    private val min: Int,
    private val max: Int
) {

    private val valueProperty = SimpleIntegerProperty()
    var value
        get() = valueProperty.get()
        set(value) = valueProperty.set(value)

    init {
        slider.min = min.toDouble()
        slider.max = max.toDouble()

        valueProperty.addListener { _, _, new ->
            field.text = new.toString()
        }
        field.textProperty().addListener { _, _, text ->
            try {
                val value = text.toDouble()
                if (value < min) {
                    field.text = min.toString()
                    return@addListener
                }
                if (value > max) {
                    field.text = max.toString()
                    return@addListener
                }
                slider.value = value
                valueProperty.set(value.toInt())
            } catch (e: NumberFormatException) {
                field.text = slider.value.toInt().toString()
            }
        }
        slider.valueProperty().addListener { _, _, value ->
            this.value = value.toInt()
        }

        slider.value = min.toDouble()
        field.text = min.toString()
    }

    fun valueProperty(): IntegerProperty {
        return valueProperty
    }

    fun random() {
        value = (min..max).random()
    }
}
