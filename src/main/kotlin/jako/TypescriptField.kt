package jako

import java.lang.reflect.Field

class TypescriptField(val owner: TypescriptClass, private val field: Field): TypescriptIdent() {
    override val rawName: String = field.name
    override var name: String = rawName
    override val rawType: String = field.genericType.typeName
    override var type: String = rawType
        get() {
            val uniqueTypeName = field.replace("$", "$$").replace('.', '$')
            return if (uniqueTypeName == owner.name)
                "this"
            else
                uniqueTypeName
        }

    init {
        owner.addDependency(type)
    }

    override fun toString(): String {
        val dc = getArrayDimensionCount(type)
        if (dc > 0) {
            var pt = getHintType(type.split('[').first()) + " "
            for (i in 1..dc) {
                pt += "[]"
            }
            return "public $name: $pt"
        }

        return "public $name: ${getHintType(type)}"
    }


}