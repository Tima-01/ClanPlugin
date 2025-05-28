package org.plugin.clansPlugin.commands;


import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugin.clansPlugin.ClansPlugin;
import org.plugin.clansPlugin.gui.BuffSelectionGUI;
import org.plugin.clansPlugin.managers.ClanManager;
import org.plugin.clansPlugin.managers.PlayerDataManager;
import org.plugin.clansPlugin.managers.TerritoryManager;
import org.plugin.clansPlugin.managers.VoteManager;

import java.util.Arrays;
import java.util.List;

public class ClanCommand implements CommandExecutor {

    private final ClansPlugin plugin;
    private final ClanManager clanManager;
    private final PlayerDataManager pdm;
    private final TerritoryManager territoryManager;
    private final VoteManager voteManager;

    public ClanCommand(ClansPlugin plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.pdm = plugin.getPlayerDataManager();
        this.territoryManager = plugin.getTerritoryManager();
        this.voteManager = plugin.getVoteManager();

    }
    private DyeColor getClanBannerColor(String clanName) {
        return switch (clanName.toLowerCase()) {
            case "бугу" -> DyeColor.RED;
            case "саруу" -> DyeColor.GREEN;
            case "кыпчак" -> DyeColor.BLUE;
            case "саяк" -> DyeColor.YELLOW;
            case "сарыбагыш" -> DyeColor.PURPLE;
            default -> DyeColor.WHITE;
        };
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

                if (territoryManager.getClanTerritory(clanName) == null) {
                    player.sendMessage(ChatColor.RED + "У вашего клана нет базы для удаления.");
                    return true;
                }

                territoryManager.deleteClanTerritory(clanName);
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

                if (territoryManager.getClanTerritory(clanName) != null) {
                    player.sendMessage(ChatColor.RED + "База для этого клана уже установлена.");
                    return true;
                }

                // Задаём фиксированную точку спавна
                Location basePoint = new Location(player.getWorld(), 1340, 68, 300);
                Location loc = player.getLocation();
                if (loc.distance(basePoint) < 1000) {
                    player.sendMessage(ChatColor.RED + "База должна быть не ближе 1000 блоков от точки (1340,68,300).");
                    return true;
                }

                int chunkX = loc.getChunk().getX();
                int chunkZ = loc.getChunk().getZ();
                int territorySize = 6; // Размер территории 6x6 чанков

                // Проверка пересечения с другими базами
                if (territoryManager.isOverlapping(chunkX, chunkZ, territorySize)) {
                    player.sendMessage(ChatColor.RED + "База слишком близко к базе другого клана.");
                    return true;
                }

                // Создаем квадратную территорию
                territoryManager.createSquareTerritory(clanName, loc, territorySize);
                player.sendMessage(ChatColor.GREEN + "База клана успешно установлена!");
                return true;
            }

            // ====== FRIENDLYFIRE ======
            if (args.length >= 2 && args[0].equalsIgnoreCase("friendlyfire")) {
                String action = args[1];
                String playerName = player.getName();
                String clan = plugin.getPlayerDataManager().getPlayerClan(playerName);

                if (clan == null) {
                    player.sendMessage("Вы не состоите в клане.");
                    return true;
                }
                // Получаем лидера из players.yml
                String leader = plugin.getPlayerDataManager().getClanLeader(clan);
                boolean isAdmin = player.hasPermission("clans.admin");

                if (!isAdmin && !playerName.equalsIgnoreCase(leader)) {
                    player.sendMessage("Только лидер клана или админ может изменять настройки.");
                    return true;
                }

                boolean enable = action.equalsIgnoreCase("on");
                player.sendMessage("Огонь по своим для клана " + clan + " теперь " + (enable ? "ВКЛЮЧЕН" : "ВЫКЛЮЧЕН"));
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

                        // Координаты базы
                        Location base = territoryManager.getClanBaseCenter(clanName);
                        if (base != null) {
                            String coords = base.getWorld().getName() + " [" + base.getBlockX() + ", " + base.getBlockY() + ", " + base.getBlockZ() + "]";
                            player.sendMessage(ChatColor.YELLOW + "База: " + ChatColor.AQUA + coords);

                            // Размер территории в чанках
                            int baseSideLength = 6;
                            int membersPerExpansion = 1;
                            int memberCount = members.size();
                            int sideLength = baseSideLength + (memberCount / membersPerExpansion);
                            int totalChunks = sideLength * sideLength;

                            player.sendMessage(ChatColor.YELLOW + "Размер территории: " + ChatColor.AQUA + sideLength + "x" + sideLength +
                                    ChatColor.GRAY + " (" + totalChunks + " чанков)");
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "База: " + ChatColor.RED + "не установлена");
                        }

                        player.sendMessage(ChatColor.GOLD + "=========================");
                        return true;
                    }


                    case "reload" -> {
                        if (!player.hasPermission("clan.admin")) {
                            player.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
                            return true;
                        }

                        plugin.getClanManager().reloadClans();
                        plugin.getPlayerDataManager().initPlayerFile();
                        plugin.getTerritoryManager().initTerritoryFile();

                        player.sendMessage(ChatColor.GREEN + "ClansPlugin: Все конфигурации были перезагружены.");
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

                        String clanName = pdm.getPlayerClan(player.getName());
                        String currentLeader = pdm.getClanLeader(clanName);

                        pdm.removePlayerFromClan(player.getName());

                        // Если этот игрок был лидером — удалить лидерство
                        if (player.getName().equalsIgnoreCase(currentLeader)) {
                            pdm.setClanLeader(clanName, null);
                            player.sendMessage(ChatColor.RED + "Ты был лидером. Лидерство клана снято.");
                        }

                        // === СЖАТИЕ ТЕРРИТОРИИ ПОСЛЕ ВЫХОДА ===
                        int updatedSize = pdm.getClanMembers(clanName).size();
                        if (updatedSize > 0) {
                            // Рассчитываем новый размер территории на основе количества участников
                            int newTerritorySize = Math.max(4, (int) Math.sqrt(updatedSize * 2) + 2);
                            int[] currentTerritory = territoryManager.getClanTerritory(clanName);
                            if (currentTerritory != null) {
                                // Получаем центр текущей территории
                                int centerX = (currentTerritory[0] + currentTerritory[2]) / 2;
                                int centerZ = (currentTerritory[1] + currentTerritory[3]) / 2;
                                Location center = new Location(player.getWorld(), centerX << 4, 0, centerZ << 4);

                                // Удаляем старую территорию и создаем новую с обновленным размером
                                territoryManager.deleteClanTerritory(clanName);
                                territoryManager.createSquareTerritory(clanName, center, newTerritorySize);
                            }
                        } else {
                            // Если в клане не осталось участников, удаляем территорию
                            territoryManager.deleteClanTerritory(clanName);
                        }

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
                    case "setbuff" -> {
                        String clanName = pdm.getPlayerClan(player.getName());

                        if (clanName == null) {
                            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                            return true;
                        }

                        String leader = pdm.getClanLeader(clanName);
                        if (!player.getName().equalsIgnoreCase(leader)) {
                            player.sendMessage(ChatColor.RED + "Только лидер клана может назначать баффы.");
                            return true;
                        }

                        // Открываем GUI выбора баффа
                        BuffSelectionGUI gui = new BuffSelectionGUI(plugin.getClanBuffManager(), clanName, player);
                        gui.open();

                        return true;
                    }

                    case "createflag" -> {
                        String clanName = pdm.getPlayerClan(player.getName());
                        if (clanName == null) {
                            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                            return true;
                        }

                        String leader = pdm.getClanLeader(clanName);
                        if (!player.getName().equals(leader)) {
                            player.sendMessage(ChatColor.RED + "Только лидер клана может устанавливать флаги.");
                            return true;
                        }

                        if (territoryManager.getClanTerritory(clanName) == null) {
                            player.sendMessage(ChatColor.RED + "Ваш клан должен иметь основную базу перед установкой флагов.");
                            return true;
                        }

                        Location flagLocation = player.getLocation();

                        // Проверяем, что блок под ногами подходит для установки флага
                        if (!flagLocation.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                            player.sendMessage(ChatColor.RED + "Флаг можно установить только на твердую поверхность.");
                            return true;
                        }

                        if (territoryManager.addFlagTerritory(clanName, flagLocation)) {
                            player.sendMessage(ChatColor.GREEN + "Флаг успешно установлен!");

                            // Эффекты
                            player.getWorld().playSound(flagLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                                    flagLocation.add(0.5, 1.5, 0.5), 30, 0.5, 0.5, 0.5, 0.1);
                        } else {
                            player.sendMessage(ChatColor.RED + "Невозможно установить флаг здесь.");
                        }
                        return true;
                    }

                    case "removeflag" -> {
                        String clanName = pdm.getPlayerClan(player.getName());
                        if (clanName == null) {
                            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                            return true;
                        }

                        String leader = pdm.getClanLeader(clanName);
                        if (!player.getName().equals(leader)) {
                            player.sendMessage(ChatColor.RED + "Только лидер клана может удалять флаги.");
                            return true;
                        }

                        Block targetBlock = player.getTargetBlockExact(5);
                        if (targetBlock == null || !(targetBlock.getState() instanceof Banner)) {
                            player.sendMessage(ChatColor.RED + "Посмотрите на флаг, который хотите удалить.");
                            return true;
                        }

                        if (territoryManager.removeClanFlag(targetBlock.getLocation())) {
                            player.sendMessage(ChatColor.GREEN + "Флаг успешно удален!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Это не флаг вашего клана!");
                        }
                        return true;
                    }
                    case "territories" -> {
                        String clanName = pdm.getPlayerClan(player.getName());
                        if (clanName == null) {
                            player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                            return true;
                        }

                        player.sendMessage(ChatColor.GOLD + "===== Территории клана " + clanName + " =====");

                        // Основная территория
                        int[] mainTerritory = territoryManager.getClanTerritory(clanName);
                        if (mainTerritory != null) {
                            player.sendMessage(ChatColor.YELLOW + "Основная база: " +
                                    ChatColor.WHITE + Arrays.toString(mainTerritory));
                        }

                        // Территории флагов
                        List<int[]> flagTerritories = territoryManager.getAllClanTerritories(clanName);
                        flagTerritories.removeIf(t -> Arrays.equals(t, mainTerritory));

                        if (!flagTerritories.isEmpty()) {
                            player.sendMessage(ChatColor.YELLOW + "Территории флагов:");
                            for (int[] territory : flagTerritories) {
                                player.sendMessage(ChatColor.GRAY + "- " + Arrays.toString(territory));
                            }
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "У клана нет дополнительных флагов.");
                        }

                        return true;
                    }
                    case "help" -> {
                        player.sendMessage(ChatColor.GOLD + "=====[ Команды клана ]=====");
                        player.sendMessage(ChatColor.YELLOW + "/clan info" + ChatColor.WHITE + " - Информация о твоем клане");
                        player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.WHITE + " - Покинуть клан");
                        player.sendMessage(ChatColor.YELLOW + "/clan help" + ChatColor.WHITE + " - Показать это сообщение");
                        player.sendMessage(ChatColor.YELLOW + "/clan join" + ChatColor.WHITE + " - Выбрать и вступить в клан");
                        player.sendMessage(ChatColor.YELLOW + "/votel <игрок>" + ChatColor.WHITE + " - Проголосовать за участника клана");
                        player.sendMessage(ChatColor.YELLOW + "/clan territories" + ChatColor.WHITE + " - Показать всю территорию клана");

                        player.sendMessage(ChatColor.GOLD + "=====[ Чат клана ]=====");
                        player.sendMessage(ChatColor.YELLOW + "/clanchat toggle" + ChatColor.WHITE + " - Включить/выключить режим отправки сообщений в клан-чат");
                        player.sendMessage(ChatColor.YELLOW + "/clan chat" + ChatColor.WHITE + " - Включить/выключить видимость клан-чата");
                        player.sendMessage(ChatColor.YELLOW + "/chatcl <сообщение>" + ChatColor.WHITE + " - Отправить сообщение в клан-чат");

                        player.sendMessage(ChatColor.GOLD + "=====[ Команды лидера ]=====");
                        player.sendMessage(ChatColor.YELLOW + "/clan createbase" + ChatColor.WHITE + " - Создать клановую базу");
                        player.sendMessage(ChatColor.YELLOW + "/clan deletebase" + ChatColor.WHITE + " - Удалить базу клана");
                        player.sendMessage(ChatColor.YELLOW + "/removeplayer <игрок>" + ChatColor.WHITE + " - Удалить участника из клана");
                        player.sendMessage(ChatColor.YELLOW + "/clan setbuff" + ChatColor.WHITE + " - Выбор баффа клана");
                        player.sendMessage(ChatColor.YELLOW + "/clan createflag" + ChatColor.WHITE + " - Установить флаг для расширения территории");
                        player.sendMessage(ChatColor.YELLOW + "/clan removeflag" + ChatColor.WHITE + " - Удалить флаг");

                        player.sendMessage(ChatColor.GOLD + "=====[ Команды админа (потом сделаю отображение только админам) ]=====");
                        player.sendMessage(ChatColor.YELLOW + "/clan reload" + ChatColor.WHITE + " - Перезагрузка конфигов");
                        player.sendMessage(ChatColor.YELLOW + "/endtvote <клан>" + ChatColor.WHITE + " - Досрочное успешное завершение голосования");
                        player.sendMessage(ChatColor.YELLOW + "/startvote <клан>" + ChatColor.WHITE + " - Начать голосование за нового лидера");
                        player.sendMessage(ChatColor.YELLOW + "/clan removeleader <клан>" + ChatColor.WHITE + " - Удалить лидера у клана");
                        player.sendMessage(ChatColor.YELLOW + "/clan setleader <игрок>" + ChatColor.WHITE + " - Назначить лидера клана");
                        player.sendMessage(ChatColor.YELLOW + "/addplayer <игрок> <клан>" + ChatColor.WHITE + " - Добавить участника в клан");
                        player.sendMessage(ChatColor.YELLOW + "/removeplayer <игрок>" + ChatColor.WHITE + " - Удалить участника из клана");
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

