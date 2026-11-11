package net.minecraft.trident.item;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Compile-time API stub for the Trident mod (LGPL-2.1).
 * <p>
 * Only the signatures referenced by Silent Gear are declared here. The stub is compiled into a
 * separate source set and is <b>never</b> included in the mod jar or on the runtime classpath:
 * at runtime the real interface from the Trident mod is used instead. Keeping this minimal and
 * signature-identical avoids bundling or recompiling the original mod's code.
 * <p>
 * Source of truth: Trident 1.12.2-1.0.5, {@code net.minecraft.trident.item.ITrident}.
 */
public interface ITrident {
    default ModelResourceLocation getModel() {
        return null;
    }

    default ModelResourceLocation getHandModel() {
        return null;
    }

    /**
     * Called by the Trident mod's {@code RenderItem} mixin instead of the normal model render when
     * the item's baked model is a built-in-renderer model (Silent Gear's layered gear model is).
     * Without an override nothing is drawn in hand, in item frames and in third person.
     */
    @SideOnly(Side.CLIENT)
    default void render(ItemStack stack, ItemCameraTransforms.TransformType type) {
    }

    default ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return null;
    }

    default void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
    }
}
