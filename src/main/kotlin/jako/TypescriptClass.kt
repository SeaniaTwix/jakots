package jako

import com.github.skjolber.indent.IndentBuilder
import java.io.File
import java.nio.charset.Charset

class TypescriptClass(private val cls: Class<*>): TypescriptIdent(), TypescriptGeneric {
    private var dependencies = mutableListOf<String>()

    override val rawName: String = cls.name
    override var name: String = rawName
        get() {
            return field.toUniqueName()
        }

    override val rawType: String = cls.name
    override var type: String = rawType

    override val isGeneric = cls.typeParameters.isNotEmpty()
    override val genericNames: List<String> = cls.typeParameters.map {
        if (it.name == "?")
            "any"
        else {
            addDependency(it.name)
            it.name
        }
    }

    var properties = cls.fields.map { TypescriptField(this, it) }
    val methods = cls.methods.map { TypescriptFunction(this, it) }

    fun addDependency(typeName: String) {
        var nt = getElementType(typeName)

        val r = """(.*)<.*>""".toRegex().matchEntire(nt)
        if (r != null) {
            nt = r.groupValues[1]
        }

        if (isPrimitiveType(nt) || typeName == "this") return

        if (dependencies.indexOf(nt) < 0) {
            dependencies.add(nt)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()

        if (dependencies.isNotEmpty()) {
            builder.append("/* dependencies */\n")
            for (dependency in dependencies) {
                builder.append("import $dependency from './$dependency'\n")
            }
            builder.append("/* dependenceis end */\n\n")
        }

        val generic = if (isGeneric) {
            "<${genericNames.joinToString()}>"
        } else ""

        builder.append("declare class${notifier()} $name$generic {")

        if (properties.isNotEmpty()) {
            indent.append(builder, 1)
            builder.append("/* fields */")
            properties.forEach {
                indent.append(builder, 1)
                builder.append(it)
            }
        }

        if (methods.isNotEmpty()) {
            indent.append(builder, 1)
            builder.append("/* methods */")
            methods.forEach {
                indent.append(builder, 1)
                builder.append(it)
            }
        }

        indent.append(builder, 0)
        builder.append("}\n\n")
        builder.append("export default $name")

        return builder.toString()
    }

    private fun notifier(): String {
        return if (cls.isEnum)
            " /* enum: Because java enums can have functions. */"
        else
            " /* ${name.split('$').last()} */"
    }

    fun export(path: String) {
        val file = File("$path/$name.d.ts")
        file.writeText(file.toString(), Charset.forName("UTF-8"))
    }

    companion object {
        val indent = IndentBuilder().withUnixLinebreak().withSpace(2).build()
    }

}