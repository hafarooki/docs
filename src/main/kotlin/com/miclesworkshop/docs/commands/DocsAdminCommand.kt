package com.miclesworkshop.docs.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import com.miclesworkshop.docs.DocsPlugin
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import java.io.File

@Suppress("unused")
@CommandAlias("docsadmin")
class DocsAdminCommand(val plugin: DocsPlugin) : BaseCommand() {
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

    private fun CommandSender.sendStatus(status: String) = sendMessage("${ChatColor.DARK_AQUA}$status...")

    private fun CommandSender.sendOK() = sendMessage("${ChatColor.GREEN} -> OK")

    private fun CommandSender.sendFinish(message: String) = sendMessage("${ChatColor.AQUA}$message")
}