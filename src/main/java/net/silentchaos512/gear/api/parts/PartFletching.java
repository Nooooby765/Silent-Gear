package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.StatInstance;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Projectile fletching. Only modular arrow materials provide this part; no vanilla tool accepts it.
 */
public class PartFletching extends ItemPart {
    public PartFletching(ResourceLocation name, PartOrigins origin) {
        super(name, origin);
    }

    protected PartFletching(ResourceLocation name, PartOrigins origin, boolean loadResources) {
        super(name, origin, loadResources);
    }

    @Override
    public PartType getType() {
        return PartType.FLETCHING;
    }

    @Override
    public IPartPosition getPartPosition() {
        return PartPositions.FLETCHING;
    }

    @Override
    public void addInformation(ItemPartData part, ItemStack gear, World world, @Nonnull List<String> tooltip, boolean advanced) {
        // Nothing
    }

    @Override
    public String getTypeName() {
        return "fletching";
    }

    @Override
    public StatInstance.Operation getDefaultStatOperation(ItemStat stat) {
        if (stat == CommonItemStats.RANGED_SPEED || stat == CommonItemStats.RARITY)
            return StatInstance.Operation.ADD;
        return StatInstance.Operation.MUL2;
    }
}
