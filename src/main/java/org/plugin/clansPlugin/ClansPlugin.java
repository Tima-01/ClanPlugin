package org.plugin.clansPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClansPlugin extends JavaPlugin implements Listener {
    private final List<String> clanChatToggles = new ArrayList<>();
    private final List<String> clanChatSendMode = new ArrayList<>();

    private List<String> clans;
    private File playerFile;
    private YamlConfiguration playerData;


    @Override
    public void onEnable() {
        saveResource("clans.yml", false);
        reloadClans();
        initPlayerFile();

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ClanPlugin включен.");
    }

    private void reloadClans() {
        clans = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "clans.yml")).getStringList("clans");
    }

    private void initPlayerFile() {
        playerFile = new File(getDataFolder(), "players.yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerFile);
    }

    private void savePlayerData() {
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (clanChatSendMode.contains(player.getName())) {
            String clan = playerData.getString("players." + player.getName());
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                return;
            }

            String message = event.getMessage();
            String formatted = ChatColor.DARK_AQUA + "[Клан: " + clan + "] " +
                    ChatColor.GRAY + player.getName() + ": " +
                    ChatColor.WHITE + message;

            event.setCancelled(true); // отменяем обычный чат

            for (Player p : Bukkit.getOnlinePlayers()) {
                String pClan = playerData.getString("players." + p.getName());
                if (clan.equals(pClan)) {
                    p.sendMessage(formatted);
                }
            }
        }
    }

    private void openClanMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Выбери клан");

        Material[] bannerColors = {
                Material.RED_BANNER,
                Material.BLUE_BANNER,
                Material.GREEN_BANNER,
                Material.YELLOW_BANNER,
                Material.PURPLE_BANNER,
                Material.BLACK_BANNER,
                Material.WHITE_BANNER,
                Material.ORANGE_BANNER,
                Material.CYAN_BANNER
        };

        int total = clans.size();
        int startIndex = (9 - total) / 2; // Центрирование

        for (int i = 0; i < total; i++) {
            String clanName = clans.get(i);
            Material bannerMaterial = bannerColors[i % bannerColors.length];

            ItemStack item = new ItemStack(bannerMaterial);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + clanName);
            item.setItemMeta(meta);

            gui.setItem(startIndex + i, item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!playerData.contains("players." + player.getName())) {
            Bukkit.getScheduler().runTaskLater(this, () -> openClanMenu(player), 20L); // задержка 1 сек
        } else {
            player.sendMessage(ChatColor.YELLOW + "Ты в клане: " + playerData.getString("players." + player.getName()));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) return;
        Player player = (Player) clicker;

        if (!event.getView().getTitle().equals("Выбери клан")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String clan = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (!clans.contains(clan)) return;

        String current = playerData.getString("players." + player.getName());
        if (clan.equals(current)) {
            player.sendMessage(ChatColor.YELLOW + "Ты уже состоишь в этом клане.");
            player.closeInventory();
            return;
        }

        playerData.set("players." + player.getName(), clan);
        savePlayerData();
        player.sendMessage(ChatColor.GREEN + "Ты вступил в клан: " + clan);
        player.closeInventory();
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("chatcl")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Использование: /chatcl <сообщение>");
                return true;
            }

            String clan = playerData.getString("players." + player.getName());
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                return true;
            }

            String message = String.join(" ", args);
            String formatted = ChatColor.DARK_AQUA + "[Клан: " + clan + "] " +
                    ChatColor.GRAY + player.getName() + ": " +
                    ChatColor.WHITE + message;

            for (Player p : Bukkit.getOnlinePlayers()) {
                String pClan = playerData.getString("players." + p.getName());
                if (clan.equals(pClan) && clanChatToggles.contains(p.getName())) {
                    p.sendMessage(formatted);
                }
            }



            return true;
        }
        if (command.getName().equalsIgnoreCase("clanchat")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                String name = player.getName();
                if (clanChatSendMode.contains(name)) {
                    clanChatSendMode.remove(name);
                    player.sendMessage(ChatColor.GRAY + "Режим клан-чата выключен. Сообщения отправляются в обычный чат.");
                } else {
                    if (!playerData.contains("players." + name)) {
                        player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                        return true;
                    }
                    clanChatSendMode.add(name);
                    player.sendMessage(ChatColor.GREEN + "Режим клан-чата включён. Все сообщения будут отправляться в клан.");
                }
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Использование: /clanchat toggle");
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("clan")) {
            if (args.length >= 1) {
                String sub = args[0].toLowerCase();

                if (sub.equals("setleader")) {
                    if (!player.hasPermission("clan.admin")) {
                        player.sendMessage(ChatColor.RED + "У тебя нет прав использовать эту команду.");
                        return true;
                    }

                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "Использование: /clan setleader <игрок>");
                        return true;
                    }

                    String targetName = args[1];
                    String targetClan = playerData.getString("players." + targetName);
                    if (targetClan == null) {
                        player.sendMessage(ChatColor.RED + "Игрок не состоит в клане.");
                        return true;
                    }

                    playerData.set("leaders." + targetClan, targetName);
                    savePlayerData();
                    player.sendMessage(ChatColor.GREEN + "Игрок " + targetName + " назначен лидером клана " + targetClan + ".");
                    return true;
                }

                // Остальные команды, которые используют args.length == 1
                if (args.length == 1) {
                    switch (sub) {
                        case "info" -> {
                            if (!playerData.contains("players." + player.getName())) {
                                player.sendMessage(ChatColor.RED + "Ты ещё не в клане.");
                                return true;
                            }

                            String clanName = playerData.getString("players." + player.getName());
                            List<String> members = new ArrayList<>();
                            if (playerData.getConfigurationSection("players") != null) {
                                for (String name : playerData.getConfigurationSection("players").getKeys(false)) {
                                    if (clanName.equals(playerData.getString("players." + name))) {
                                        members.add(name);
                                    }
                                }
                            }

                            String leader = playerData.getString("leaders." + clanName);
                            player.sendMessage(ChatColor.GOLD + "=====[ Инфо о клане ]=====");
                            player.sendMessage(ChatColor.YELLOW + "Клан: " + ChatColor.AQUA + clanName);
                            if (leader != null)
                                player.sendMessage(ChatColor.YELLOW + "Лидер: " + ChatColor.LIGHT_PURPLE + leader);
                            player.sendMessage(ChatColor.YELLOW + "Участники: ");
                            for (String member : members) {
                                player.sendMessage(ChatColor.GRAY + "- " + member);
                            }
                            player.sendMessage(ChatColor.GOLD + "=========================");
                        }

                        case "join" -> {
                            if (playerData.contains("players." + player.getName())) {
                                player.sendMessage(ChatColor.RED + "Ты уже состоишь в клане. Сначала выйди с помощью /clan leave.");
                                return true;
                            }
                            openClanMenu(player);
                        }

                        case "leave" -> {
                            if (!playerData.contains("players." + player.getName())) {
                                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                                return true;
                            }
                            playerData.set("players." + player.getName(), null);
                            savePlayerData();
                            player.sendMessage(ChatColor.YELLOW + "Ты покинул клан.");
                        }

                        case "chat" -> {
                            if (!playerData.contains("players." + player.getName())) {
                                player.sendMessage(ChatColor.RED + "Ты не состоишь в клане.");
                                return true;
                            }

                            String name = player.getName();
                            if (clanChatToggles.contains(name)) {
                                clanChatToggles.remove(name);
                                player.sendMessage(ChatColor.GRAY + "Клан-чат выключен.");
                            } else {
                                clanChatToggles.add(name);
                                player.sendMessage(ChatColor.GREEN + "Клан-чат включён.");
                            }
                        }

                        case "help" -> {
                            player.sendMessage(ChatColor.GOLD + "=====[ Команды клана ]=====");
                            player.sendMessage(ChatColor.YELLOW + "/clan info" + ChatColor.WHITE + " - Информация о твоем клане");
                            player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.WHITE + " - Покинуть клан");
                            player.sendMessage(ChatColor.YELLOW + "/clan setleader <игрок>" + ChatColor.WHITE + " - Назначить лидера клана (только для админов)");
                            player.sendMessage(ChatColor.YELLOW + "/clan chat" + ChatColor.WHITE + " - Включить/выключить видимость клан-чата");
                            player.sendMessage(ChatColor.YELLOW + "/chatcl <сообщение>" + ChatColor.WHITE + " - Отправить сообщение в клан-чат");
                            player.sendMessage(ChatColor.YELLOW + "/clan help" + ChatColor.WHITE + " - Показать это сообщение");
                            player.sendMessage(ChatColor.YELLOW + "/clan join" + ChatColor.WHITE + " - Выбрать и вступить в клан");
                            player.sendMessage(ChatColor.YELLOW + "/clanchat toggle" + ChatColor.WHITE + " - Включить/выключить режим отправки сообщений в клан-чат");
                            player.sendMessage(ChatColor.GOLD + "============================");
                        }

                        default -> player.sendMessage(ChatColor.RED + "Неизвестная команда. Используй: /clan help");
                    }
                    return true;
                }
            }


            player.sendMessage(ChatColor.RED + "Использование: /clan <info|leave|chat|help>");
            return true;
        }

        return false;
    }
}
