package net.silentchaos512.gear.item.gear;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;

import javax.annotation.Nonnull;

/** A slow, heavy blunt weapon. */
public class CoreMace extends CoreSword {
    @Nonnull
    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.mace;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"#", "#", "/"};
    }

    @Override
    public String getGearClass() {
        return "mace";
    }

    @Override
    public GearType getGearType() {
        return GearType.MACE;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        // Slower but heavier: give a small stun benefit like the dagger's vulnerability tweak.
        target.hurtResistantTime = Math.max(0, target.hurtResistantTime - 5);
        return super.hitEntity(stack, target, attacker);
    }
}
