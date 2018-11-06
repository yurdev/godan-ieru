import java.io.File
import java.io.InputStream

fun main(args: Array<String>) {
    val edict2Rows = readFileToList("dict/edict2")
    val freqRows = readFileToList("dict/internet-jp-forms.num.txt")
    val godanVerbs = getGodans(edict2Rows)
    val godanIeru = getGodanIeru(godanVerbs)
    val freqMap = createFreqMap(freqRows)
    val godanIeruEntries = ieruGodansWithFrequency(godanIeru, freqMap)
    val clenedGodanIeru = removeCompoundsAndSort(godanIeruEntries)
}

fun readFileToList(fileName: String): MutableList<String> {
    val lineList = mutableListOf<String>()
    val inputStream: InputStream = File(fileName).inputStream()

    inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
    //lineList.forEach{println(">  " + it)}
    println("Read file $fileName, ${lineList.size} rows done.")
    return lineList
}

fun getGodans(edictLines: List<String>): MutableList<String> {
    val list = mutableListOf<String>()
    var count = 0
    edictLines.forEach {
        if (it.contains("v5")) {
            ++count
            list.add(it)
        }
    }
    println("Godan total count = $count")
    return list
}

val regexIeruEnding = """[いしちにひみりえせてねへめれ]る]""".toRegex()
fun getGodanIeru(lines: List<String>): MutableList<String> {
    val list = mutableListOf<String>()
    var count = 0
    lines.forEach{
        if (regexIeruEnding.containsMatchIn(input = it)) {
            ++count
            list.add(it)
            //println(it)
        }
    }
    println("Godan ieru's count = $count")
    return list
}

data class DictEntry(var entry: String, var verb: String, var freq: Float)
val regexVerb = """^([^\s;(]+)""".toRegex() // first word in dict entry separator
fun ieruGodansWithFrequency(lines: List<String>, freqMap: HashMap<String, Float>): MutableList<DictEntry> {
    val outList = mutableListOf<DictEntry>()
    lines.forEach{
        val matchRes = regexVerb.find(it)
        if (null != matchRes) {
            val verb = matchRes.groupValues[1]
            //println("Verb: $verb, len = ${verb.length}")
            val freq = freqMap[verb] ?: 0f
            outList.add(DictEntry(it, verb, freq))
        } else {
            println("ERROR: can't find entry verb in $it")
        }
    }
    outList.forEach {
        //println("${it.freq}: ${it.entry}")
    }
    return outList
}

fun removeCompoundsAndSort(entries: MutableList<DictEntry>): MutableList<DictEntry> {
    entries.sortBy { it.verb.length }
    val outList = mutableListOf<DictEntry>()
    entries.forEach {
        if (it.verb.length == 2) outList.add(it)
        else {
            if (!it.hasSuffixFrom(outList)) outList.add(it)
        }
    }
    outList.sortByDescending { it.freq }
    outList.forEach {
        if (it.entry.contains("(P)"))
            println("${it.entry}${it.freq}/")
    }
    return outList
}

fun DictEntry.hasSuffixFrom(entries: MutableList<DictEntry>): Boolean {
    entries.forEach {
        if (this.verb.endsWith(it.verb)) return true
    }
    return false
}

fun createFreqMap(freqList: MutableList<String>): HashMap<String, Float> {
    val map = HashMap<String, Float>()
    var line = 0
    freqList.forEach {
        ++line
        val elems = it.split(" ")
        if (elems.size == 3) {
            map[elems[2]] = elems[1].toFloat()
        } else {
            //println("Error: invalid line(#$line): $it")
        }
    }
    return map
}

fun String.contains(subStr: String): Boolean { return this.indexOf(subStr) >= 0 }
fun String.contains(subStr1: String, subStr2: String): Boolean { return this.indexOf(subStr1) >= 0 && this.indexOf(subStr2) >= 0 }
