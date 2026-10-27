package net.silentchaos512.gear.item.gear;

import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;

import javax.annotation.Nonnull;

/** A long reach polearm-like blade. */
public class CoreSpear extends CoreSword {
    @Nonnull
    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.spear;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"#", "/", "/"};
    }

    @Override
    public String getGearClass() {
        return "spear";
    }

    @Override
    public GearType getGearType() {
        return GearType.SPEAR;
    }
}
