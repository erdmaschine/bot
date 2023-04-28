package erdmaschine.bot.model

import java.io.File

val Propaganda = object {}.javaClass.getResource("/propaganda/")?.file?.let { dir ->
    File(dir).walk()
        .filter { it.isFile }
        .associate { file -> file.nameWithoutExtension to file.name }
} ?: emptyMap()
