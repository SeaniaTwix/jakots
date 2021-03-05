package jako

val numberTypes = listOf("int", "long", "float", "double", "byte", "short")

fun String.toUniqueName(): String {
    return this.replace("$", "$$").replace('.', '$')
}

abstract class TypescriptIdent {
    abstract val rawName: String
    abstract var name: String
    abstract val rawType: String
    abstract var type: String
        protected set
    abstract override fun toString(): String

    val uniqueName: String
        get() = name.replace("$", "__").replace(".", "$")

    /**
     * Primitive means perfect widely. Java class based number, string and more included
     */
    fun isPrimitiveType(): Boolean {
        return isPrimitiveType(type)
    }

    fun isPrimitiveType(t: String): Boolean {
        val del = if (t.count { it == '.' } > 0) '.'
        else '$'
        return when (val t2 = t.split(del).last().toLowerCase()) {
            "boolean", "any", "char", "string", "void" -> true
            else -> isNumberType(t2)
        }
    }

    fun isNumberType(t: String): Boolean {
        return numberTypes.contains(t)
    }

    fun isArray(t: String): Boolean {
        return t.indexOf("[]") >= 0
    }

    /**
     * return element type if given type is array, else return back parameter
     */
    fun getElementType(t: String): String {
        return if (t.indexOf('[') < 0)
            t
        else
            t.substring(0, t.indexOf('['))
    }

    fun getHintType(t: String): String {
        if (isPrimitiveType(t)) {
            val ft = getFinalType(t)
            return if (isNumberType(t)) {
                "number /* $ft */"
            } else {
                ft
            }
        }

        return t
    }

    fun getFinalType(): String {
        return getFinalType(type)
    }

    fun getFinalType(t: String): String {
        return t.split(".").last().toLowerCase()
    }

    open fun isObject(): Boolean {
        return isObject(getFinalType())
    }

    open fun isObject(t: String): Boolean {
        return getFinalType(t) == "object"
    }

    fun isClass(t: String): Boolean {
        return t.startsWith("java.lang.Class")
    }

    fun getArrayDimensionCount(t: String): Int {
        return t.split("[]").count() - 1
    }

}