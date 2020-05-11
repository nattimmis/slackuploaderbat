package net.gouline.slackuploader

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import okhttp3.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val cmd = Commands()

    val commander = JCommander(cmd)
    commander.setProgramName("slack-uploader")

    try {
        commander.parse(*args)
    } catch (e: ParameterException) {
        error(e.message)
    }

    if (cmd.help) {
        commander.usage()
    } else if (cmd.token == null) {
        error("No token provided.")
    } else if (cmd.title == null) {
        error("No title provided.")
    } else if (cmd.channels.isEmpty()) {
        error("No channels provided.")
    } else if (cmd.files.isEmpty()) {
        error("No files provided.")
    } else {
        val file = File(cmd.files.first())
        if (!file.exists()) {
            error("File not found.")
        } else {
            SlackClient(cmd.token!!).upload(file, cmd.title!!, cmd.channels)
        }
    }
}

/**
 * Minimal Slack client.
 */
class SlackClient(val token: String) {

    companion object {
        private const val BASE_URL = "https://slack.com/api"
        private const val FILES_UPLOAD_URL = "$BASE_URL/files.upload"
    }

    private val client = OkHttpClient()

    /**
     * Uploads file to channels.
     */
    fun upload(file: File, title: String, channels: List<String>) {
        val response = execute(FILES_UPLOAD_URL, {
            it.addFormDataPart("title", title)
                    .addFormDataPart("channels", channels.joinToString(","))
                    .addFormDataPart("file", file.name, RequestBody.create(null, file))
        })
        if (response.isSuccessful) {
            success(response.body().string())
        } else {
            error("Request failed: $response")
        }
    }

    private inline fun execute(url: String, f: (MultipartBody.Builder) -> Unit): Response {
        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", token)
                .apply { f(this) }
                .build()
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        return client.newCall(request).execute()
    }
}

/**
 * CLI input commands.
 */
class Commands {
    @Parameter(names = arrayOf("--help", "-h"), description = "Displays this usage.", help = true)
    var help = false

    @field:Parameter(names = arrayOf("--token", "-a"), description = "Authentication token (requires scope 'files:write:user').", required = true)
    var token: String? = null

    @field:Parameter(names = arrayOf("--title", "-t"), description = "Title of file.", required = true)
    var title: String? = null

    @field:Parameter(names = arrayOf("--channel", "-c"), description = "Channel names or IDs where the file will be shared.", required = true)
    var channels: List<String> = ArrayList()

    @field:Parameter(description = "FILE", required = true)
    var files: List<String> = ArrayList()
}

/**
 * Prints success message.
 */
private fun success(message: String?) {
    println("SUCCESS: $message")
    exitProcess(0)
}

/**
 * Prints error message and quits.
 */
private fun error(message: String?) {
    println("ERROR: $message")
    exitProcess(1)
}
