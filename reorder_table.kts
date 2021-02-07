import java.io.File

val confPath = args[1 + args.indexOf("-conf")]
val tablePath = args[1 + args.indexOf("-table")]

val confLines = File(confPath).readLines()
val tableLines = File(tablePath).readLines()

val vowels = confLines[3].split('=')[1].trim('"').split(',')
val consonants = listOf("") + confLines[4].split('=')[1].trim('"').split(',')

val sortedAllTableLines = tableLines.sortedBy {
    val splitted = it.split(' ')
    val vowel = splitted.last()
    val consonant = if (splitted.size > 2) splitted[1] else ""
    val cIndex = consonants.indexOf(consonant)
    val vIndex = vowels.indexOf(vowel)
    require(cIndex >= 0) {
        "cIndex of $splitted is minus"
    }
    require(vIndex >= 0) {
        "vIndex of $splitted is minus"
    }
    cIndex * tableLines.size + vIndex
}

// Check duplicates
val duplicates = sortedAllTableLines.groupBy { it.split(" ")[0] }.filter { it.value.size > 1 }
if (duplicates.isNotEmpty()) {
    throw Exception("Duplicated key in table: " + duplicates.keys.joinToString())
}

// Output table
val outputTablePath = tablePath + ".sorted"
File(outputTablePath).writeText(sortedAllTableLines.joinToString("\n"))
