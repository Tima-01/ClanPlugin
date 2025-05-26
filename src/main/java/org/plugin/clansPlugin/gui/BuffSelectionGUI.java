package org.plugin.clansPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugin.clansPlugin.buffs.ClanBuff;
import org.plugin.clansPlugin.managers.ClanBuffManager;

import java.util.Arrays;

public class BuffSelectionGUI {

    private final ClanBuffManager buffManager;
    private final String clanId;
    private final Player leader;
    private final Inventory gui;
    private static final int CUSTOM_MODEL_DATA = 1001; // Константа для CustomModelData

    public BuffSelectionGUI(ClanBuffManager buffManager, String clanId, Player leader) {
        this.buffManager = buffManager;
        this.clanId = clanId;
        this.leader = leader;
        this.gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Выбор духа клана");
        init();
    }

    private void init() {
        ClanBuff[] buffs = ClanBuff.values();
        int startSlot = (9 - buffs.length) / 2;

        for (int i = 0; i < buffs.length; i++) {
            ClanBuff buff = buffs[i];
            ItemStack item = new ItemStack(getMaterialForBuff(buff));
            ItemMeta meta = item.getItemMeta();

            // Устанавливаем CustomModelData
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + buff.getDisplayName());
                meta.setCustomModelData(CUSTOM_MODEL_DATA); // Вот эта строка добавляет CustomModelData

                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Основной: " + ChatColor.YELLOW +
                                buff.getPrimaryEffect().getName() + " " + (buff.getPrimaryAmplifier() + 1),
                        ChatColor.GRAY + "Дополнительный: " + ChatColor.YELLOW +
                                buff.getSecondaryEffect().getName() + " " + (buff.getSecondaryAmplifier() + 1)
                ));

                item.setItemMeta(meta);
            }

            gui.setItem(startSlot + i, item);
        }
    }

    private Material getMaterialForBuff(ClanBuff buff) {
        switch (buff) {
            case WOLF: return Material.WOLF_SPAWN_EGG;
            case ARKHAR: return Material.GOAT_SPAWN_EGG;
            case SNOW_LEOPARD: return Material.OCELOT_SPAWN_EGG;
            case EAGLE: return Material.PARROT_SPAWN_EGG;
            case HORSE: return Material.HORSE_SPAWN_EGG;
            default: return Material.BONE;
        }
    }

    public void open() {
        leader.openInventory(gui);
    }
}