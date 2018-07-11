package com.miclesworkshop.docs.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import com.miclesworkshop.docs.DocsPlugin
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import java.io.File

@CommandAlias("docs")
class DocsCommand(private val plugin: DocsPlugin) : BaseCommand() {
    @Default
    @Subcommand("list")
    fun onList(sender: CommandSender) {
        plugin.async {
            val folder = plugin.docsFolder
            sender.listDocsInFolder(folder, 0)
        }
    }

    @Subcommand("view")
    fun onView(sender: CommandSender) {

    }

    private fun CommandSender.listDocsInFolder(rootFolder: File, indents: Int, childPath: String = "") {
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
        for (folderName in folders) listDocsInFolder(rootFolder, indents + 1, "$childPath/$folderName")
        for(fileName in files) sendMessage("$indent$AQUA- $fileName")
    }
}