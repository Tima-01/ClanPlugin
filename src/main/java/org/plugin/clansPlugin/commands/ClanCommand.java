package org.plugin.clansPlugin.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.managers.ClanManager;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;

import java.util.ArrayList;
import java.util.List;

public class ClanCommand implements CommandExecutor {

    private final ClansPlugin plugin;
    private final ClanManager clanManager;
    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;

    public ClanCommand(ClansPlugin plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.pdm = plugin.getPlayerDataManager();
        this.territoryManager = plugin.getTerritoryManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        if (args.length >= 1) {
            String sub = args[0].toLowerCase();

            // ====== УДАЛЕНИЕ БАЗЫ ======
            if (sub.equals("deletebase") && args.length == 1) {
                String clanName = pdm.getPlayerClan(player.getName());
                if (clanName == null) {
                    player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                    return true;
                }

                String leader = pdm.getClanLeader(clanName);
                if (!player.getName().equals(leader)) {
                    player.sendMessage(ChatColor.RED + "Только лидер клана может удалять базу.");
                    return true;
                }

                if (territoryManager.getClanChunks(clanName).isEmpty()) {
                    player.sendMessage(ChatColor.RED + "У вашего клана нет базы для удаления.");
                    return true;
                }

                territoryManager.deleteClanChunks(clanName);
                player.sendMessage(ChatColor.GREEN + "База клана успешно удалена!");
                return true;
            }

            // ====== СОЗДАНИЕ БАЗЫ ======
            if (sub.equals("createbase") && args.length == 1) {
                String clanName = pdm.getPlayerClan(player.getName());
                if (clanName == null) {
                    player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                    return true;
                }
                String leader = pdm.getClanLeader(clanName);
                if (!player.getName().equals(leader)) {
                    player.sendMessage(ChatColor.RED + "Только лидер клана может создавать базу.");
                    return true;
                }

                if (!territoryManager.getClanChunks(clanName).isEmpty()) {
                    player.sendMessage(ChatColor.RED + "База для этого клана уже установлена.");
                    return true;
                }

                // Задаём фиксированную точку спавна (как в вашем коде)
                Location basePoint = new Location(player.getWorld(), 1340, 68, 300);
                Location loc = player.getLocation();
                if (loc.distance(basePoint) < 1000) {
                    player.sendMessage(ChatColor.RED + "База должна быть не ближе 1000 блоков от точки (1340,68,300).");
                    return true;
                }

                int chunkX = loc.getChunk().getX();
                int chunkZ = loc.getChunk().getZ();

                // Проверка пересечения с другими базами
                if (territoryManager.isOverlapping(chunkX, chunkZ)) {
                    player.sendMessage(ChatColor.RED + "База слишком близко к базе другого клана.");
                    return true;
                }

                // Формируем список 6×6 чанков вокруг текущего
                List<String> chunks = new ArrayList<>();
                for (int dx = -3; dx < 3; dx++) {
                    for (int dz = -3; dz < 3; dz++) {
                        chunks.add((chunkX + dx) + "," + (chunkZ + dz));
                    }
                }

                territoryManager.setClanChunks(clanName, chunks);
                player.sendMessage(ChatColor.GREEN + "База клана успешно установлена!");
                return true;
            }

            // ====== НАЗНАЧЕНИЕ ЛИДЕРА /clan setleader <игрок> ======
            if (sub.equals("setleader") && args.length == 2) {
                if (!player.hasPermission("clan.admin")) {
                    player.sendMessage(ChatColor.RED + "У тебя нет прав использовать эту команду.");
                    return true;
                }
                String targetName = args[1];
                String targetClan = pdm.getPlayerClan(targetName);
                if (targetClan == null) {
                    player.sendMessage(ChatColor.RED + "Игрок не состоит в клане.");
                    return true;
                }

                pdm.setClanLeader(targetClan, targetName);
                pdm.savePlayerData();
                player.sendMessage(ChatColor.GREEN + "Игрок " + targetName + " назначен лидером клана " + targetClan + ".");
                return true;
            }

            // ====== ПОДКОМАНДЫ С args.length == 1 ======
            if (args.length == 1) {
                switch (sub) {
                    case "info" -> {
                        if (!pdm.isPlayerInClan(player.getName())) {
                            player.sendMessage(ChatColor.RED + "Ты ещё не в клане.");
                            return true;
                        }
                        String clanName = pdm.getPlayerClan(player.getName());
                        List<String> members = pdm.getClanMembers(clanName);
                        String leader = pdm.getClanLeader(clanName);

                        player.sendMessage(ChatColor.GOLD + "=====[ Инфо о клане ]=====");
                        player.sendMessage(ChatColor.YELLOW + "Клан: " + ChatColor.AQUA + clanName);
                        if (leader != null) {
                            player.sendMessage(ChatColor.YELLOW + "Лидер: " + ChatColor.LIGHT_PURPLE + leader);
                        }
                        player.sendMessage(ChatColor.YELLOW + "Участники: ");
                        for (String member : members) {
                            player.sendMessage(ChatColor.GRAY + "- " + member);
                        }
                        player.sendMessage(ChatColor.GOLD + "=========================");
                        return true;
                    }

                    case "join" -> {
                        if (pdm.isPlayerInClan(player.getName())) {
                            player.sendMessage(ChatColor.RED + "Ты уже состоишь в клане. Сначала выйди с помощью /clan leave.");
                            return true;
                        }
                        // Этот метод откроет GUI (см. слушатель PlayerJoinListener)
                        // Можно просто повторно вызвать открытие GUI, как и при первом заходе
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                // Открываем меню кланов:
                                // Вызовем статический метод, либо вынесем в отдельный класс-утилиту
                                // Сделаем вызов: ClanInventory.openClanMenu(player);
                                // Но для простоты покажем здесь:
                                List<String> clans = clanManager.getClans();
                                int total = clans.size();
                                int startIndex = (9 - total) / 2;
                                org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 9, "Выбери клан");
                                org.bukkit.Material[] bannerColors = {
                                        org.bukkit.Material.RED_BANNER,
                                        org.bukkit.Material.BLUE_BANNER,
                                        org.bukkit.Material.GREEN_BANNER,
                                        org.bukkit.Material.YELLOW_BANNER,
                                        org.bukkit.Material.PURPLE_BANNER,
                                        org.bukkit.Material.BLACK_BANNER,
                                        org.bukkit.Material.WHITE_BANNER,
                                        org.bukkit.Material.ORANGE_BANNER,
                                        org.bukkit.Material.CYAN_BANNER
                                };
                                for (int i = 0; i < total; i++) {
                                    String clan = clans.get(i);
                                    org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(bannerColors[i % bannerColors.length]);
                                    org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                                    meta.setDisplayName(ChatColor.AQUA + clan);
                                    item.setItemMeta(meta);
                                    gui.setItem(startIndex + i, item);
                                }
                                player.openInventory(gui);
                            });
                        }, 20L);
                        return true;
                    }

                    case "leave" -> {
                        if (!pdm.isPlayerInClan(player.getName())) {
                            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                            return true;
                        }
                        pdm.removePlayerFromClan(player.getName());
                        pdm.savePlayerData();
                        player.sendMessage(ChatColor.YELLOW + "Ты покинул клан.");
                        return true;
                    }

                    case "chat" -> {
                        if (!pdm.isPlayerInClan(player.getName())) {
                            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                            return true;
                        }
                        // Здесь переключаем видимость clanChatToggles (в pdm)
                        if (pdm.isClanChatToggled(player.getName())) {
                            pdm.setClanChatToggled(player.getName(), false);
                            player.sendMessage(ChatColor.GRAY + "Клан-чат выключен.");
                        } else {
                            pdm.setClanChatToggled(player.getName(), true);
                            player.sendMessage(ChatColor.GREEN + "Клан-чат включён.");
                        }
                        pdm.savePlayerData();
                        return true;
                    }

                    case "help" -> {
                        player.sendMessage(ChatColor.GOLD + "=====[ Команды клана ]=====");
                        player.sendMessage(ChatColor.YELLOW + "/clan info" + ChatColor.WHITE + " - Информация о твоем клане");
                        player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.WHITE + " - Покинуть клан");
                        player.sendMessage(ChatColor.YELLOW + "/clan setleader <игрок>" + ChatColor.WHITE + " - Назначить лидера клана");
                        player.sendMessage(ChatColor.YELLOW + "/clan chat" + ChatColor.WHITE + " - Включить/выключить видимость клан-чата");
                        player.sendMessage(ChatColor.YELLOW + "/chatcl <сообщение>" + ChatColor.WHITE + " - Отправить сообщение в клан-чат");
                        player.sendMessage(ChatColor.YELLOW + "/clan help" + ChatColor.WHITE + " - Показать это сообщение");
                        player.sendMessage(ChatColor.YELLOW + "/clan join" + ChatColor.WHITE + " - Выбрать и вступить в клан");
                        player.sendMessage(ChatColor.YELLOW + "/clan createbase" + ChatColor.WHITE + " - Создать клановую базу (только лидер)");
                        player.sendMessage(ChatColor.YELLOW + "/clanchat toggle" + ChatColor.WHITE + " - Включить/выключить режим отправки сообщений в клан-чат");
                        player.sendMessage(ChatColor.YELLOW + "/clan deletebase" + ChatColor.WHITE + " - Удалить базу клана (только лидер)");
                        player.sendMessage(ChatColor.GOLD + "============================");
                        return true;
                    }

                    default -> {
                        player.sendMessage(ChatColor.RED + "Неизвестная команда. Используй: /clan help");
                        return true;
                    }
                }
            }

            // Если мы сюда попали, значит: либо неизвестная подкоманда, либо неправильное число аргументов
            player.sendMessage(ChatColor.RED + "Использование: /clan <info|leave|chat|help|join|createbase|deletebase|setleader>");
            return true;
        }

        player.sendMessage(ChatColor.RED + "Использование: /clan <info|leave|chat|help>");
        return true;
    }
}

