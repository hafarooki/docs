package com.miclesworkshop.docs

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent as Text
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.min

fun File.parseDoc(index: Int): List<List<BaseComponent>> {
    val text = Files.readAllLines(Paths.get(path)).joinToString("\n")
    val pages = text.split("\n>>>")
    val page = pages[min(pages.size - 1, index)]
    val msgs = mutableListOf<List<BaseComponent>>()
    msgs += listOf(Text("$GOLD$nameWithoutExtension$GRAY (Page ${index + 1}/${pages.size})"))

    for (line in page.split("\n")) {
        msgs += when {
            line.startsWith("#") -> listOf(Text("$BOLD$line"))
            line == ">>>" -> listOf(Text(""))
            line.startsWith("IMG") -> {
                val split = line.split(" ").toMutableList()
                split.removeAt(0)
                val url = split[0]; split.removeAt(0)
                assert(split.size >= 3) { "Error while parsing IMG: Does not follow format 'IMG <URL> <text...>' ($line)" }
                val component = Text(split.joinToString(" ")).color(LIGHT_PURPLE)
                component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(Text("Click to open image: $url")))
                component.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
                listOf(Text("[").color(DARK_PURPLE), component, Text("]").color(DARK_PURPLE))
            }
            else -> listOf(Text(line).color(YELLOW))
        }
    }
    return msgs
}

private fun Text.color(color: ChatColor): Text {
    this.color = color; return this
}
