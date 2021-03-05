package jako

import java.lang.reflect.Parameter

class TypescriptParameter(val owner: TypescriptFunction, private val param: Parameter): TypescriptIdent() {
    override val rawName: String = param.name
    override var name: String = rawName

    val isEnum: Boolean
        get() = param.type.isEnum

    override val rawType: String = when {
        isEnum -> param.type.name
        isObject((param.parameterizedType.javaClass.superclass ?: param.parameterizedType.javaClass).typeName) -> "any"
        else -> param.type.typeName
    }

    override var type: String
        get() {
            val typeName = rawType

            if (isObject(typeName)) {
                return "any"
            }

            if (isPrimitiveType(typeName)) {
                return getHintType(typeName)
            }

            return typeName
                .replace("$", "**")
                .replace('.', '$')
                .replace("**", "$$")
        }
        set(_) {}

    override fun toString(): String {
        return "$name: $type"
    }
}