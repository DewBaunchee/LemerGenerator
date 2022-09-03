package by.varyvoda.lemer.binding

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventHandler
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode

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
        valueProperty.addListener { _, _, new ->
            field.text = new.toString()
        }
        slider.onMouseReleased = EventHandler {
            field.text = slider.value.toString()
            update()
        }
        slider.min = min.toDouble()
        slider.max = max.toDouble()
        field.focusedProperty().addListener { _, _, focused ->
            if (!focused) {
                updateFromText()
            }
        }
        field.onKeyPressed = EventHandler {
            if (it.code == KeyCode.ENTER) {
                updateFromText()
            }
        }
        slider.value = min.toDouble()
        field.text = min.toString()
        update()
    }

    fun valueProperty(): IntegerProperty {
        return valueProperty
    }

    private fun update() {
        valueProperty.set(slider.value.toInt())
    }

    private fun updateFromText() {
        try {
            val value = field.text.toDouble()
            if (value < min) {
                field.text = min.toString()
                return
            }
            if (value > max) {
                field.text = max.toString()
                return
            }
            slider.value = value
            valueProperty.set(value.toInt())
        } catch (e: NumberFormatException) {
            field.text = slider.value.toInt().toString()
        }
    }

    fun random() {
        value = (min..max).random()
    }
}
