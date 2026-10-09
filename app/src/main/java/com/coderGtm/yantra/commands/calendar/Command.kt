package com.coderGtm.yantra.commands.calendar

import android.content.Intent
import android.provider.CalendarContract
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

/** Terminal-first calendar view, with an option to open the device calendar. */
class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "calendar",
        helpTitle = "Calendar",
        description = "Show a monthly calendar or open your calendar app"
    )

    override fun execute(command: String) {
        val args = command.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (args.getOrNull(1)?.lowercase(Locale.ROOT) in listOf("open", "events")) {
            try {
                terminal.activity.startActivity(Intent(Intent.ACTION_VIEW).setData(CalendarContract.CONTENT_URI))
            } catch (_: Exception) {
                output("No calendar app is available.", terminal.theme.errorTextColor)
            }
            return
        }
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar)
        if (args.size >= 3) {
            val month = args[1].toIntOrNull()
            val year = args[2].toIntOrNull()
            if (month == null || year == null || month !in 1..12 || year !in 1..9999) {
                output("Usage: calendar [month 1-12] [year] | calendar open", terminal.theme.errorTextColor)
                return
            }
            target.set(Calendar.MONTH, month - 1)
            target.set(Calendar.YEAR, year)
        }
        target.set(Calendar.DAY_OF_MONTH, 1)
        val monthName = DateFormatSymbols.getInstance().months[target.get(Calendar.MONTH)]
        val year = target.get(Calendar.YEAR)
        val today = now.get(Calendar.DAY_OF_MONTH).takeIf {
            now.get(Calendar.MONTH) == target.get(Calendar.MONTH) && now.get(Calendar.YEAR) == year
        }
        val out = StringBuilder("$monthName $year\nMo Tu We Th Fr Sa Su\n")
        val firstDay = (target.get(Calendar.DAY_OF_WEEK) + 5) % 7
        repeat(firstDay) { out.append("   ") }
        val days = target.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..days) {
            out.append(if (day == today) "[${day.toString().padStart(2, ' ')}]" else day.toString().padStart(2, ' '))
            if ((firstDay + day) % 7 == 0) out.append('\n') else out.append(' ')
        }
        output(out.toString().trimEnd())
        output("Tip: type 'calendar open' to view events in your calendar app.", terminal.theme.warningTextColor)
    }
}
