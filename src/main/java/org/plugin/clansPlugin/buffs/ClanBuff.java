package org.plugin.clansPlugin.buffs;

import org.bukkit.potion.PotionEffectType;

public enum ClanBuff {
    WOLF(
            "Дух волка",
            PotionEffectType.STRENGTH, 1,
            PotionEffectType.REGENERATION, 0
    ),
    ARKHAR(
            "Дух архара",
            PotionEffectType.RESISTANCE, 0,
            PotionEffectType.JUMP_BOOST, 1
    ),
    SNOW_LEOPARD(
            "Дух снежного барса",
            PotionEffectType.SPEED, 1,
            PotionEffectType.STRENGTH, 0
    ),
    EAGLE(
            "Дух беркута",
            PotionEffectType.SPEED, 1,
            PotionEffectType.NIGHT_VISION, 0
    ),
    HORSE(
            "Дух коня",
            PotionEffectType.SPEED, 1,
            PotionEffectType.ABSORPTION, 1
    );

    private final String displayName;
    private final PotionEffectType primaryEffect;
    private final int primaryAmplifier;
    private final PotionEffectType secondaryEffect;
    private final int secondaryAmplifier;

    ClanBuff(String displayName,
             PotionEffectType primaryEffect, int primaryAmplifier,
             PotionEffectType secondaryEffect, int secondaryAmplifier) {
        this.displayName = displayName;
        this.primaryEffect = primaryEffect;
        this.primaryAmplifier = primaryAmplifier;
        this.secondaryEffect = secondaryEffect;
        this.secondaryAmplifier = secondaryAmplifier;
    }

    public String getDisplayName() { return displayName; }
    public PotionEffectType getPrimaryEffect() { return primaryEffect; }
    public int getPrimaryAmplifier() { return primaryAmplifier; }
    public PotionEffectType getSecondaryEffect() { return secondaryEffect; }
    public int getSecondaryAmplifier() { return secondaryAmplifier; }
}