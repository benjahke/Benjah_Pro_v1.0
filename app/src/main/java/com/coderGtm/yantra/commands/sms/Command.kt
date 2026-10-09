package com.coderGtm.yantra.commands.sms

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

/** Uses Android's SMS composer; the user reviews and sends the message. */
class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "sms",
        helpTitle = "SMS",
        description = "Open the SMS composer: sms [number] [message]"
    )

    override fun execute(command: String) {
        val trimmed = command.trim()
        val separator = trimmed.indexOfFirst { it.isWhitespace() }
        val rest = if (separator < 0) "" else trimmed.substring(separator).trim()
        val number = rest.takeWhile { !it.isWhitespace() }
        val body = rest.drop(number.length).trim()
        val uri = if (number.isBlank()) Uri.parse("smsto:") else Uri.parse("smsto:${Uri.encode(number)}")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        if (body.isNotBlank()) intent.putExtra("sms_body", body)
        try {
            terminal.activity.startActivity(intent)
        } catch (_: Exception) {
            output("No SMS app is available on this device.", terminal.theme.errorTextColor)
        }
    }
}
