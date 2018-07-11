package com.miclesworkshop.docs

import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent as Text
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.min

fun parseDoc(file: File, pagePath: String, index: Int): List<List<BaseComponent>> {
    val text = Files.readAllLines(Paths.get(file.path)).joinToString("\n").replace("\\n", "\n")
    val pages = text.split("\n>>>")
    val page = pages[min(pages.size - 1, index)]
    val lines = mutableListOf<List<BaseComponent>>()

    lines += listOf(Text("$GOLD${file.nameWithoutExtension}$GRAY (Page ${index + 1}/${pages.size})"))

    // Go through every line
    for (line in page.split("\n")) {
        // Handle different line beginnings
        lines += when {
            // Title
            line.startsWith("#") -> listOf(Text("$BOLD${line.removePrefix("#").trim()}").clr(DARK_AQUA))

            // Image
            line.startsWith("IMG", ignoreCase = true) -> {
                val split = line.split(" ").toMutableList()
                split.removeAt(0)
                val url = split[0]; split.removeAt(0)
                assert(split.size >= 3) { "Error while parsing IMG: Does not follow format 'IMG <URL> <text...>' ($line)" }
                val component = Text("Image: " + split.joinToString(" ")).clr(LIGHT_PURPLE).url(url, "Click to view image")
                listOf(Text("[").clr(DARK_PURPLE), component, Text("]").clr(DARK_PURPLE))
            }

            // URL
            line.startsWith("URL", ignoreCase = true) -> {
                val split = line.split(" ").toMutableList()
                split.removeAt(0)
                val url = split[0]; split.removeAt(0)
                assert(split.size >= 3) { "Error while parsing URL: Does not follow format 'URL <URL> <text...>' ($line)" }
                val component = Text("Link: " + split.joinToString(" ")).clr(AQUA).underline().url(url, "Click to open page")
                listOf(Text("[").clr(DARK_AQUA), component, Text("]").clr(DARK_AQUA))

            }

            // Raw line
            else -> listOf(Text(line))
        }
    }

    // Back button
    if (index > 0) lines += listOf(
            Text(" [").clr(DARK_GREEN),
            Text("<--").cmd("/docs view $pagePath $index", "Previous Page"),
            Text("]").clr(DARK_GREEN))

    // Next button
    if (index < pages.size - 1) lines += listOf(
            Text(" [").clr(DARK_GREEN),
            Text("-->").cmd("/docs view $pagePath ${index + 2}", "Next Page"),
            Text("]").clr(DARK_GREEN))
    return lines
}

