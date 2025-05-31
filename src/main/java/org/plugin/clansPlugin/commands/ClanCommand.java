package org.plugin.clansPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.commands.subcommands.*;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;
import org.plugin.clansPlugin.managers.VoteManager;
import org.plugin.clansPlugin.gui.BuffSelectionGUI;
import org.plugin.clansPlugin.managers.ClanBuffManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClanCommand implements CommandExecutor {

    private final ClansPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ClanCommand(ClansPlugin plugin) {
        this.plugin = plugin;

        PlayerDataManager pdm = plugin.getPlayerDataManager();
        TerritoryManager tm = plugin.getTerritoryManager();
        VoteManager vm = plugin.getVoteManager();
        ClanBuffManager cbm = plugin.getClanBuffManager(); // менеджер баффов

        // Регистрируем все SubCommand
        register(new SubCommandInfo(pdm, tm));
        register(new SubCommandJoin(plugin));
        register(new SubCommandLeave(pdm, tm));
        register(new SubCommandReload(plugin));
        register(new SubCommandChatToggle(pdm));

        // /clan setbuff
        register(new SubCommandSetBuff(pdm, plugin)); // внутри SubCommandSetBuff используется plugin.getClanBuffManager()

        // Базовые подкоманды по территории
        register(new SubCommandCreateBase(pdm, tm));
        register(new SubCommandDeleteBase(pdm, tm));
        register(new SubCommandSetLeader(pdm));
        register(new SubCommandTpBase(pdm, tm));

        // Флаги
        register(new SubCommandCreateFlag(pdm, tm));
        register(new SubCommandRemoveFlag(pdm, tm));
        register(new SubCommandTerritories(pdm, tm));

        // /clan help
        register(new SubCommandHelp());
    }

    private void register(SubCommand cmd) {
        for (String alias : cmd.getAliases()) {
            subCommands.put(alias.toLowerCase(), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманда только для игроков.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /clan <подкоманда> [аргументы]");
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand handler = subCommands.get(sub);
        if (handler == null) {
            player.sendMessage(ChatColor.RED + "Неизвестная команда. Используй: /clan help");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return handler.execute(player, subArgs);
    }
}
