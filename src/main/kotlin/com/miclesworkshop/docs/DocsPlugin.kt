package com.miclesworkshop.docs

import co.aikar.commands.BaseCommand
import org.bukkit.plugin.java.JavaPlugin
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.CommandHelp
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import com.google.common.io.Files
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.util.FileUtils
import java.io.File

class DocsPlugin : JavaPlugin() {
    private val commandManager by lazy { PaperCommandManager(this) }
    val gitRoot = File(dataFolder, "repository")
    val gitRootOld = File(dataFolder, "repository_old")
    val gitRootTmp = File(dataFolder, "repository_tmp")

    override fun onEnable() {
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
        val tmpDir = plugin.gitRootTmp
        tmpDir.deleteRecursively()   // delete in case it is left over for some reason
        tmpDir.mkdirs()
        tmpDir.deleteOnExit()        // make sure it's deleted when the program exits
        try {
            sender.sendStatus("Cloning")
            Git.cloneRepository().setURI(url).setDirectory(tmpDir).call().close()
        } catch (e: Exception) {
            tmpDir.deleteRecursively()
            e.printStackTrace()
            throw InvalidCommandArgument("Error occurred while attempting to clone: '${e.message}'. See console for stacktrace.")
        }
        sender.sendOK()
        try {
            sender.sendStatus("Copying files")

            if(plugin.gitRoot.exists()) plugin.gitRoot.move(plugin.gitRootOld)
            tmpDir.move(plugin.gitRoot)
            if(plugin.gitRootOld.exists()) plugin.gitRootOld.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
            throw InvalidCommandArgument("Something went wrong. Please see console.")
        }
        sender.sendOK()
        sender.sendFinish("Changed origin to $url and cloned. Files: ${plugin.gitRoot.list()?.joinToString()
                ?: "none"}")
    }

    @Subcommand("update")
    @Description("updates the repo")
    @CommandPermission("docs.update")
    fun onUpdate(sender: CommandSender) {
        val git: Git
        try {
            sender.sendStatus("Checking local repository")
            git = Git.open(plugin.gitRoot)
        } catch (e: Exception) {
            e.printStackTrace()
            throw InvalidCommandArgument("Failed to update: Error while loading repository. You may have to use /docs origin first. Error: '${e.message}'. See console for stacktrace.")
        }
        sender.sendOK()
        val result: PullResult
        try {
            sender.sendStatus("Pulling changes")
            result = git.pull().call()
            git.close()
            if (!result.isSuccessful) throw Exception(result.fetchResult.messages)
        } catch (e: Exception) {
            e.printStackTrace()
            throw InvalidCommandArgument("Failed to pull: ${e.message}. See console for stacktrace.")
        }
        sender.sendOK()
        sender.sendFinish("Pulled changes. Result: $result")
    }

    private fun File.move(newFile: File) {
        setReadable(true)
        setWritable(true)
        newFile.deleteRecursively()
        renameTo(newFile)
    }

    private fun CommandSender.sendStatus(status: String) = sendMessage("$DARK_AQUA$status...")

    private fun CommandSender.sendOK() = sendMessage("$GREEN -> OK")

    private fun CommandSender.sendFinish(message: String) = sendMessage("$AQUA$message")
}