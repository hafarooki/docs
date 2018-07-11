package com.miclesworkshop.docs

import co.aikar.commands.BaseCommand
import org.bukkit.plugin.java.JavaPlugin
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.CommandHelp
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.eclipse.jgit.api.Git
import java.io.File

class DocsPlugin : JavaPlugin() {
    private val commandManager = PaperCommandManager(this)
    val gitRoot = File(dataFolder, "repository")
    val gitRootOld = File(dataFolder, "repository_old")
    val gitRootTmp = File(dataFolder, "repository_tmp")

    override fun onEnable() {
        gitRoot.mkdirs()
        commandManager.registerCommand(DocsCommand(this))
        commandManager.enableUnstableAPI("help")
    }

    override fun onDisable() {
        commandManager.unregisterCommands()
    }

    fun async(function: () -> Unit) = server.scheduler.runTaskAsynchronously(this, function)
}

@Suppress("unused")
@CommandAlias("docs")
class DocsCommand(val plugin: DocsPlugin) : BaseCommand() {
    @HelpCommand
    fun onHelp(sender: CommandSender, help: CommandHelp) = help.showHelp()

    @Subcommand("origin")
    @Description("Changes git origin URL and clones")
    @CommandPermission("docs.origin")
    fun onOrigin(sender: CommandSender, url: String) {
        plugin.gitRootTmp.mkdirs()
        try {
            Git.cloneRepository().setURI(url).setDirectory(plugin.gitRootTmp).call()
        } catch (e: Exception) {
            plugin.gitRootTmp.deleteRecursively()
            e.printStackTrace()
            throw InvalidCommandArgument("Error occurred while attempting to clone: '${e.message}'. See console for stacktrace.")
        }
        try {
            plugin.gitRoot.renameTo(plugin.gitRootOld)
            plugin.gitRootTmp.renameTo(plugin.gitRoot)
            plugin.gitRootOld.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
            throw InvalidCommandArgument("Something went wrong. Please see console.")
        }
    }

    @Subcommand("update")
    @Description("updates the repo")
    @CommandPermission("docs.update")
    fun onUpdate(sender: CommandSender) {
        val git: Git
        try {
            git = Git.open(plugin.gitRoot)
        } catch (e: Exception) {
            e.printStackTrace()
            throw InvalidCommandArgument("Failed to update: Error while loading repository. You may have to use /docs origin first. Error: '${e.message}'. See console for stacktrace.")
        }
    }
}