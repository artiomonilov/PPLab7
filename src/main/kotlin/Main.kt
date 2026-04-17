import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class SyslogRecord(
    val timestamp: String,
    val hostname: String,
    val appName: String,
    val pid: String?,
    val logEntry: String
)

fun main() {
    val file = File("syslog")

    if (!file.exists()) {
        println("Fisierul 'syslog' nu a fost gasit in director. Adauga-l conform instructiunilor (sudo cp /var/log/syslog .)")
        return
    }

    val regex = """^([A-Z][a-z]{2}\s+\d+\s+\d{2}:\d{2}:\d{2})\s+(\S+)\s+([^:\[]+)(?:\[(\d+)\])?:\s+(.*)$""".toRegex()

    val recordsList = file.useLines { lines ->
        lines.mapNotNull { line ->
            val matchResult = regex.find(line)
            if (matchResult != null) {
                val (timestamp, hostname, appName, pid, logEntry) = matchResult.destructured
                SyslogRecord(
                    timestamp = timestamp.trim(),
                    hostname = hostname.trim(),
                    appName = appName.trim(),
                    pid = pid.takeIf { it.isNotEmpty() },
                    logEntry = logEntry.trim()
                )
            } else {

                null
            }
        }.toList()
    }

    val recordsSequence = recordsList.asSequence()

    val groupedByApp = recordsSequence.groupBy { it.appName }

    val sortedLogsByApp = groupedByApp.mapValues { (_, records) ->
        records.sortedBy { it.logEntry }
    }

    println("=== Intrari ordonate crescator dupa LOG_ENTRY ===")
    sortedLogsByApp.forEach { (app, records) ->
        println("\nAplicatie: ${app}")
        records.forEach {
            println("  -> ${it.logEntry}")
        }
    }

    println("\n\n=== Inregistrarile care specifica un PID pentru fiecare aplicatie ===")

    groupedByApp.forEach { (appName, records) ->
        val recordsWithPid = records.filter { it.pid != null }
        
        if (recordsWithPid.isNotEmpty()) {
            println("\nAplicatie: $appName")
            recordsWithPid.forEach { record ->
                println("  [PID: ${record.pid}] ${record.logEntry}")
            }
        }
    }
}
