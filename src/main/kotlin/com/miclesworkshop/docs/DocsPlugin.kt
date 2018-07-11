package com.miclesworkshop.docs

import co.aikar.commands.BaseCommand
import org.bukkit.plugin.java.JavaPlugin
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.CommandHelp
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import com.google.common.io.Files
import com.miclesworkshop.docs.commands.DocsAdminCommand
import com.miclesworkshop.docs.commands.DocsCommand
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.util.FileUtils
import java.io.File

class DocsPlugin : JavaPlugin() {
    private val commandManager by lazy { PaperCommandManager(this) }
    val gitRoot
        get() = File(dataFolder, "repository")
    val gitRootOld
        get() = File(dataFolder, "repository_old")
    val gitRootTmp
        get() = File(dataFolder, "repository_tmp")
    val docsFolder
        get() = File(gitRoot, "docs")

    override fun onEnable() {
        commandManager.registerCommand(DocsAdminCommand(this))
        commandManager.registerCommand(DocsCommand(this))
        commandManager.enableUnstableAPI("help")
    }

    override fun onDisable() {
        commandManager.unregisterCommands()
    }

    fun async(function: () -> Unit) = server.scheduler.runTaskAsynchronously(this, function)
}