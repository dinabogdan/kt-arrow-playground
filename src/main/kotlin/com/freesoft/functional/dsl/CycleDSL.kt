package com.freesoft.functional.dsl

import com.freesoft.functional.dsl.BarType.FLAT
import com.freesoft.functional.dsl.Brake.DISK
import com.freesoft.functional.dsl.Material.ALUMINIUM
import com.freesoft.functional.dsl.Material.CARBON

/*
* I will create a DSL for representing the following XML
* <bicycle description="Fast carbon commuter">
    <bar material="ALUMINIUM" type="FLAT">
    </bar>
    <frame material="CARBON">
        <wheel brake="DISK" material="ALUMINIUM">
        </wheel>
    </frame>
    <fork material="CARBON">
        <wheel brake="DISK" material="ALUMINIUM">
        </wheel>
    </fork>
</bicycle>
*
* */

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

@DslMarker
annotation class ElementMarker

@ElementMarker
abstract class Part(private val name: String) : Element {
    private val children = arrayListOf<Element>()
    protected val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initElement(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }

    private fun renderAttributes(): String = buildString {
        attributes.forEach { attr, value -> append(" $attr=\"$value\"") }
    }

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<$name${renderAttributes()}>\n")
        children.forEach { c -> c.render(builder, indent + "\t") }
        builder.append("$indent</$name>\n")
    }

    override fun toString(): String = buildString {
        render(this, "")
    }
}

enum class Material {
    CARBON, STEEL, TITANIUM, ALUMINIUM
}

enum class BarType {
    DROP, FLAT, TT, BULLHORN
}

enum class Brake {
    RIM, DISK
}

abstract class PartWithMaterial(name: String) : Part(name) {
    var material: Material
        get() = Material.valueOf(attributes["material"]!!)
        set(value) {
            attributes["material"] = value.name
        }
}

class Bicycle : Part("bicycle") {
    fun description(description: String) {
        attributes["description"] = description
    }

    fun frame(init: Frame.() -> Unit) = initElement(Frame(), init)
    fun fork(init: Fork.() -> Unit) = initElement(Fork(), init)
    fun bar(init: Bar.() -> Unit) = initElement(Bar(), init)
}

class Frame : PartWithMaterial("frame") {
    fun backWheel(init: Wheel.() -> Unit) = initElement(Wheel(), init)
}

class Wheel : PartWithMaterial("wheel") {
    var brake: Brake
        get() = Brake.valueOf(attributes["brake"]!!)
        set(value) {
            attributes["brake"] = value.name
        }
}

class Bar : PartWithMaterial("bar") {
    var barType: BarType
        get() = BarType.valueOf(attributes["type"]!!)
        set(value) {
            attributes["type"] = value.name
        }
}

class Fork : PartWithMaterial("fork") {
    fun frontWheel(init: Wheel.() -> Unit) = initElement(Wheel(), init)
}

fun bicycle(init: Bicycle.() -> Unit): Bicycle {
    val cycle = Bicycle()
    cycle.init()
    return cycle
}


fun main(args: Array<String>) {
    val commuter = bicycle {
        description("Fast carbon commuter")
        bar {
            barType = FLAT
            material = ALUMINIUM
        }
        frame {
            material = CARBON
            backWheel {
                material = ALUMINIUM
                brake = DISK
            }
        }
        fork {
            material = CARBON
            frontWheel {
                material = ALUMINIUM
                brake = DISK
            }
        }
    }
    println(commuter)
}