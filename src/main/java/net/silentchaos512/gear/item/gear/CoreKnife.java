package net.silentchaos512.gear.item.gear;

import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;

import javax.annotation.Nonnull;

/** A short, fast blade. Mentally grouped with daggers/katana. */
public class CoreKnife extends CoreSword {
    @Nonnull
    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.knife;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"#", "/"};
    }

    @Override
    public String getGearClass() {
        return "knife";
    }

    @Override
    public GearType getGearType() {
        return GearType.KNIFE;
    }
}
