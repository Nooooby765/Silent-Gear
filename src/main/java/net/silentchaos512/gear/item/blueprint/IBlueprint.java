package net.silentchaos512.gear.item.blueprint;

import net.minecraft.item.ItemStack;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.PartType;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IBlueprint {

    ItemStack getCraftingResult(ItemStack blueprint, Collection<ItemStack> parts);

    int getMaterialCost(ItemStack blueprint);

    default boolean isSingleUse(ItemStack blueprint) {
        return false;
    }

    /**
     * Whether the given part is a valid material for this blueprint. Legacy blueprints accept
     * anything (the old behavior), while part blueprints only accept main materials.
     */
    default boolean isValidMaterial(ItemPart part) {
        return true;
    }

    /**
     * Number of optional parts of the given type this blueprint accepts, in addition to the
     * required main material count. Legacy blueprints accept none.
     */
    default int getOptionalPartCount(PartType type) {
        return 0;
    }

    /**
     * Resolves a crafting stack to a part for this blueprint. Legacy blueprints use the part
     * registry directly; part blueprints may override this to accept independent materials
     * (for example redstone dust as a tip material) without registering them as parts.
     */
    @Nullable
    default ItemPartData getMaterial(ItemStack stack) {
        return ItemPartData.fromStack(stack);
    }
}
