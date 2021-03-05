package jako

import java.lang.reflect.Method

class TypescriptFunction(val owner: TypescriptClass, private val method: Method): TypescriptIdent(), TypescriptGeneric {
    override var isGeneric: Boolean = method.typeParameters.isNotEmpty()
    override val genericNames: List<String> = method.typeParameters.map {
        it.name
    }

    override val rawName: String = method.name
    override var name: String = rawName
        get() {
            return field.replace("$", "$$").replace('.', '$')
        }

    override val rawType: String = method.genericReturnType.typeName

    // return type
    override var type: String = rawType
        get() {
            if (isPrimitiveType(field)) {
                return getFinalType(field)
            }

            val fullType = """(.*?)<(.*)>""".toRegex().matchEntire(field)
            if (fullType != null) {
                // todo: optimize addDependency call count.

                val mainType = fullType.groupValues[1].toUniqueName()

                owner.addDependency(mainType)
                isGeneric = fullType.groupValues[2].find { it == '?' } != null
                val genericType = fullType.groupValues[2]
                    .replace('?', 'T')
                    .split(",")
                    .map {
                        val ft = it.trim().toUniqueName()

                        if (ft != "T")
                            owner.addDependency(ft)

                        ft
                    }
                return "$mainType<${genericType.joinToString()}>"
            }

            val uniqueTypeName = when {
                method.returnType.isEnum -> method.returnType.name
                else -> field
            }.toUniqueName()

            return if (uniqueTypeName == owner.name)
                "this"
            else
                uniqueTypeName
        }

    private val parameters = method.parameters.map { TypescriptParameter(this, it) }

    init {
        val t = type
        if (!isGeneric) {
            owner.addDependency(t)
        }

        parameters.forEach {
            owner.addDependency(it.type)
        }
    }

    override fun toString(): String {
        val t = type
        val dc = getArrayDimensionCount(t)
        val generic = if (isGeneric) "<T>" else ""
        var out = "public $name$generic(${parameters.joinToString()}): "

        val t2 = if (dc > 0) {
            if (t == owner.type) {
                "this"
            } else {
                var pt = getHintType(t.split('[').first()) + " "
                for (i in 1..dc) {
                    pt += "[]"
                }
                pt
            }
        } else {
            if (rawType.toUniqueName() == owner.name) {
                "this"
            } else getHintType(t)
        }

        out += t2

        return out
    }

}