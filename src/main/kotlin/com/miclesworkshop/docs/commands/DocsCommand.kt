package com.miclesworkshop.docs.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import com.miclesworkshop.docs.DocsPlugin
import com.miclesworkshop.docs.parseDoc
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.ChatColor.*
import org.bukkit.command.CommandSender
import java.io.File
import kotlin.math.max

private fun CommandSender.listDocsInFolder(rootFolder: File, indents: Int, childPath: String = "") {
    // TODO: Replace this with an inventory menu
    val folder = if (childPath == "") rootFolder else File(rootFolder, childPath)
    assert(folder.exists() && folder.isDirectory)
    val indent = String(CharArray(indents)).replace("\u0000", "  ")
    sendMessage("$indent$GOLD${folder.name}:")
    val folders = mutableListOf<String>()
    val files = mutableListOf<String>()
    for (file in folder.listFiles()) when {
        file.isDirectory -> folders.add(file.name)
        file.extension == "txt" -> files.add(file.nameWithoutExtension)
        else -> System.out.println("$file is not a TXT file or a directory!")
    }
    for (folderName in folders) listDocsInFolder(rootFolder, indents + 1, "$childPath$folderName/")
    for (fileName in files) {
        val component = TextComponent(" [$fileName]")
        component.color = YELLOW
        component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("View Page (Click Me!)")))
        component.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/docs view $childPath$fileName 1")
        spigot().sendMessage(TextComponent(indent), component)
    }
}

@CommandAlias("docs")
class DocsCommand(private val plugin: DocsPlugin) : BaseCommand() {
    @Default
    @Subcommand("list")
    fun onList(sender: CommandSender) {
//        plugin.async {
        val folder = plugin.docsFolder
        sender.listDocsInFolder(folder, 0)
//        }
    }

    @Subcommand("view")
    fun onView(sender: CommandSender, doc: String, page: Int) {
//        plugin.async {
        val file = File(plugin.docsFolder, "$doc.txt")
        System.out.println(file.path)
        if (!file.exists() || !file.isFile) throw InvalidCommandArgument("Doc not found! For a list of docs, use /docs")
        val lines = file.parseDoc(max(0, page - 1))
        for (line in lines) {
            val component = TextComponent()
            line.forEach { component.addExtra(it) }
            sender.spigot().sendMessage(component)
        }
//        }
    }
}