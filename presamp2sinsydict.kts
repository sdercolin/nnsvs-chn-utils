import java.io.File

val inputPath = args[1 + args.indexOf("-input")]
val inputFile = File(inputPath).takeIf { it.isFile && it.extension == "ini" }
    ?: throw Exception("Input is not a `.ini` file")

val inputLines = inputFile.readLines()

// Read vowels
val vowelMap = mutableMapOf<String, String>()
val vowelLabelIndex = inputLines.indexOf("[VOWEL]")
for (i in vowelLabelIndex + 1 until inputLines.size) {
    val line = inputLines[i].takeUnless { it.startsWith("[") } ?: break
    val vowel = line.split('=')[0]
    line.split('=')[2].split(',').forEach {
        vowelMap[it] = vowel
    }
}

// Read consonants
val consonantMap = mutableMapOf<String, String>()
val consonantLabelIndex = inputLines.indexOf("[CONSONANT]")
for (i in consonantLabelIndex + 1 until inputLines.size) {
    val line = inputLines[i].takeUnless { it.startsWith("[") } ?: break
    val consonant = line.split('=')[0]
    line.split('=')[1].split(',').forEach {
        consonantMap[it] = consonant
    }
}

// Process for output
val vowels = vowelMap.values.toSet()
vowels.forEach { vowelMap[it] = it }
val consonants = consonantMap.values.toSet()
val words = (vowelMap.keys + consonantMap.keys).toSet()
    .sorted()
    .map { word ->
        listOfNotNull(word, consonantMap[word], vowelMap[word]).joinToString(" ")
    }

// Output conf
val outputConfPath = inputPath + ".conf"
var conf = """
    MACRON="-"
    PHONEME_CL="cl"
    VOWEL_REDUCTION="'"
    VOWELS="${vowels.joinToString(",")}"
    CONSONANTS="${consonants.joinToString(",")}"
""".trimIndent()
File(outputConfPath).writeText(conf)

// Output table
val outputTablePath = inputPath + ".table"
File(outputTablePath).writeText(words.joinToString("\n"))
