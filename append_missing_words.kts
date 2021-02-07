import java.io.File

val confPath = args[1 + args.indexOf("-conf")]
val tablePath = args[1 + args.indexOf("-table")]

val confLines = File(confPath).readLines()
val tableLines = File(tablePath).readLines()

val vowels = confLines[3].split('=')[1].trim('"').split(',')
val consonants = listOf("") + confLines[4].split('=')[1].trim('"').split(',')
val wordMap = tableLines.map { line ->
    val splitted = line.split(' ')
    val word = splitted[0]
    val vowel = splitted.last()
    val consonant = if (splitted.size > 2) splitted[1] else ""
    (consonant to vowel) to word
}.toMap()

val allPairs = consonants.flatMap { c -> vowels.map { v -> c to v } }
val missingPairs = allPairs.filter { wordMap[it] == null }
val appendingTableLines = allPairs.flatMap { pair ->
    val existingName = wordMap[pair]
    val (consonant, vowel) = pair
    if (vowel in listOf("i0", "ir")) return@flatMap listOf()

    val consonantAlias = when (consonant.getOrNull(1)) {
        'y' -> consonant.replace('y', 'i')
        'w' -> {
            if (consonant.first() !in "nl") consonant.replace('w', 'u')
            else null
        }
        'v' -> {
            if (consonant.first() !in "nl") consonant.replace("v", "u")
            else null
        }
        else -> null
    }
    val nameAlias = consonantAlias?.let { it + vowel }

    listOfNotNull(
        nameAlias?.let { "$nameAlias $consonant $vowel" },
        "$consonant$vowel $consonant $vowel".takeIf { existingName != "$consonant$vowel" }
    )
        .filter { line -> line.split(" ")[0] !in wordMap.values }
        .filter { line -> line.split(" ")[0].count { it == 'i' } <= 1 }
}
val allTableLines = tableLines + appendingTableLines
val sortedAllTableLines = allTableLines.sortedBy {
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
val outputTablePath = tablePath + ".new"
File(outputTablePath).writeText(sortedAllTableLines.joinToString("\n"))
