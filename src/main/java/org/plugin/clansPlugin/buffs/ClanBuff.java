package org.plugin.clansPlugin.buffs;

import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;

public enum ClanBuff {
    WOLF(
            "Дух волка",
            ChatColor.WHITE + "Сила и Регенерация",
            PotionEffectType.STRENGTH, 1,
            PotionEffectType.REGENERATION, 0
    ),
    ARKHAR(
            "Дух архара",
            ChatColor.WHITE + "Защита и сила прыжка",
            PotionEffectType.RESISTANCE, 0,
            PotionEffectType.JUMP_BOOST, 1
    ),
    SNOW_LEOPARD(
            "Дух снежного барса",
            ChatColor.WHITE + "Скорость и сила",
            PotionEffectType.SPEED, 1,
            PotionEffectType.STRENGTH, 0
    ),
    EAGLE(
            "Дух беркута",
            ChatColor.WHITE + "Скорость и ночное зрение",
            PotionEffectType.SPEED, 1,
            PotionEffectType.NIGHT_VISION, 0
    ),
    HORSE(
            "Дух коня",
            ChatColor.WHITE + "Скорость и жизненная сила",
            PotionEffectType.SPEED, 1,
            PotionEffectType.ABSORPTION, 1
    );

    private final String displayName;
    private final String description;
    private final PotionEffectType primaryEffect;
    private final int primaryAmplifier;
    private final PotionEffectType secondaryEffect;
    private final int secondaryAmplifier;

    ClanBuff(String displayName, String description,
             PotionEffectType primaryEffect, int primaryAmplifier,
             PotionEffectType secondaryEffect, int secondaryAmplifier) {
        this.displayName = displayName;
        this.description = description;
        this.primaryEffect = primaryEffect;
        this.primaryAmplifier = primaryAmplifier;
        this.secondaryEffect = secondaryEffect;
        this.secondaryAmplifier = secondaryAmplifier;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public PotionEffectType getPrimaryEffect() { return primaryEffect; }
    public int getPrimaryAmplifier() { return primaryAmplifier; }
    public PotionEffectType getSecondaryEffect() { return secondaryEffect; }
    public int getSecondaryAmplifier() { return secondaryAmplifier; }
}