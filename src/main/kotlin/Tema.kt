import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



data class HistoryLogRecord(
    val timestamp: LocalDateTime,
    val command: String
) : Comparable<HistoryLogRecord> {
    override fun compareTo(other: HistoryLogRecord): Int {
        return this.timestamp.compareTo(other.timestamp)
    }
}


fun <T : Comparable<T>> maxRecord(first: T, second: T): T {
    return if (first > second) first else second
}




fun <V> replaceRecord(map: MutableMap<*, V>, oldRecord: V, newRecord: V) {


    val mutablePointers = map as MutableMap<Any?, V>


    val keysToUpdate = mutablePointers.filterValues { it == oldRecord }.keys


    for (key in keysToUpdate) {
        mutablePointers[key] = newRecord
    }
}

fun main() {
    val file = File("history.log")

    if (!file.exists()) {
        println("Fisierul 'history.log' nu a fost gasit.")
        return
    }


    val rawContent = file.readText().trim()
    val rawBlocks = rawContent.split("\n\n").filter { it.isNotBlank() }

    val last50Blocks = rawBlocks.takeLast(50)


    val startDateRegex = """Start-Date:\s+(.+)""".toRegex()
    val commandRegex = """Commandline:\s+(.+)""".toRegex()


    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")


    val logMap = mutableMapOf<LocalDateTime, HistoryLogRecord>()

    for (block in last50Blocks) {
        val dateMatch = startDateRegex.find(block)
        val cmdMatch = commandRegex.find(block)

        if (dateMatch != null && cmdMatch != null) {
            val dateStr = dateMatch.groupValues[1].trim()
            val command = cmdMatch.groupValues[1].trim()


            val cleanDate = dateStr.replace("  ", " ")
            
            try {

                val timestamp = LocalDateTime.parse(cleanDate, dateFormatter)


                val record = HistoryLogRecord(timestamp, command)


                logMap[timestamp] = record
            } catch (e: Exception) {
                println("Avertisment: Linia continand data [$cleanDate] are un format invalid.")
            }
        }
    }

    println("=== Inregistrarile extrase ===")
    logMap.forEach { (key, value) ->
        println("Key: $key -> Value: $value")
    }


    if (logMap.size >= 2) {
        val keys = logMap.keys.toList()
        val record1 = logMap[keys[keys.size - 2]]!!
        val record2 = logMap[keys[keys.size - 1]]!!
        
        println("\n=== Comparam 2 Inregistrari (Cel mai recent timestamp este MAX) ===")
        println("R1: $record1")
        println("R2: $record2")
        println("MAX=> ${maxRecord(record1, record2)}")

        
        println("\n=== Test Inlocuire (Proiectie) ===")
        val replacementRecord = HistoryLogRecord(record1.timestamp, "apt-get INCURCAT_DE_TEST")
        println("Se inlocuieste [$record1] \nCU -> [$replacementRecord]")
        
        replaceRecord(logMap, record1, replacementRecord)
        
        println("\n=== Map-ul rezultat dupa Inlocuire ===")
        logMap.forEach { (key, value) ->
            println("Key: $key -> Value: $value")
        }
    }
}