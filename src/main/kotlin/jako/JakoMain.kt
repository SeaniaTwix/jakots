package jako

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import com.xenomachina.argparser.ArgParser
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import kotlin.system.exitProcess

val terminal = Terminal()

fun check(srcDir: String, destDir: String) {
    if (!File(srcDir).exists()) {
        terminal.println("${red("[ERR ]")} $srcDir is not exists.")
        exitProcess(1)
    }

    val dest = File(destDir)
    if (dest.exists() && !dest.isDirectory) {
        terminal.println("${red("[ERR ]")} $srcDir is not a directory. Please give a directory path properly.")
        exitProcess(2)
    }

    if (!dest.exists()) {
        dest.mkdirs()
    }
}

fun compile(verbose: Boolean, srcDir: String, destDir: String) {
    check(srcDir, destDir)

    val e = getClassNamesFromSource(srcDir)
    var i = 1

    for (cls in e) {
        val c = try {
            Class.forName(cls)
        } catch (_: Error) {
            if (verbose)
                terminal.println("${yellow("[WARN]")} $cls is skipped. (${i++}/${e.size})")
            continue
        } catch (_: Exception) {
            if (verbose)
                terminal.println("${yellow("[WARN]")} $cls is skipped. (${i++}/${e.size})")
            continue
        }

        if (verbose)
            terminal.println("${cyan("[INFO]")} $cls is completed. (${i++}/${e.size})")

        val tc = TypescriptClass(c)
        tc.export(destDir)
    }
}

fun showHelp() {
    terminal.println("""
        |${brightCyan("JAKO")} 1.0.0
        |./jakots [-v|-h] -s [source path] -d [destination path] 
    """.trimIndent())
}

class JakoArgs(parser: ArgParser) {
    val verbose by parser
        .flagging("-v", "--verbose", help = "컴파일 로그를 터미널에 출력합니다.")
    val sourcePath by parser
        .storing("-s", "--source", help = "자바 base 구조를 알기 위해 src.zip 파일의 위치가 필요합니다.")
    val destPath by parser
        .storing("-d", "--dest", "--destination", help = "*.d.ts 파일을 출력할 디렉토리 위치입니다. 폴더가 없다면 만듭니다. 기본 값은 './out' 입니다.")
    val help by parser
        .flagging("-h", "-?", "--help", help = "자세한 도움말을 출력합니다.")

}

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::JakoArgs).run {
        if (help)
            showHelp()
        else
            compile(verbose, sourcePath, destPath)
    }
}

fun getClassNamesFromSource(sourceZipDir: String): List<String> {
    val srcFile = File(sourceZipDir)
    val input = FileInputStream(srcFile)
    val zip = ZipInputStream(input)
    var e = zip.nextEntry
    val r = "java.base/"
    val names = mutableListOf<String>()

    while (e != null) {
        val name = e.name

        if (name.startsWith(r)) {
            val cls = name
                .substring(r.length)
                .replace(File.separatorChar, '.')
                .replace(".java", "")
            names.add(cls)
        }

        e = zip.nextEntry
    }

    return names
}
