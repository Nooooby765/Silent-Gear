/*
 * Silent Gear -- CoreTrident
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.gear.compat.trident;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.trident.Trident;
import net.minecraft.trident.item.ITrident;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.compat.trident.client.TridentClientModels;
import net.silentchaos512.gear.config.ConfigOptionEquipment;
import net.silentchaos512.gear.item.gear.CoreSword;

/**
 * Modular trident, available only when the Trident mod is installed. Stats, traits, blueprints,
 * templates, repairing and salvage use the normal Silent Gear systems; the Trident mod only
 * supplies the throwable entity and its rendering. The item is a {@link CoreSword} subclass.
 * <p>
 * The mod's mixins ask an {@link ITrident} for its GUI and hand models; returning the Silent Gear
 * layered model keeps the inventory icon material-colored ({@code ToolModel} + item colors).
 */
public class CoreTrident extends CoreSword implements ITrident {
    private static final int USE_DURATION = 72000;
    // Defined here rather than in the core GearType class: nothing but this integration needs it.
    private static final GearType GEAR_TYPE = GearType.getOrCreate(TridentIntegration.GEAR_CLASS, GearType.MELEE_WEAPON);

    private final ConfigOptionEquipment config;

    public CoreTrident() {
        this.config = new ConfigOptionEquipment(this).loadValue(null);
    }

    @Override
    public ConfigOptionEquipment getConfig() {
        return this.config;
    }

    @Override
    public String getGearClass() {
        return TridentIntegration.GEAR_CLASS;
    }

    @Override
    public GearType getGearType() {
        return GEAR_TYPE;
    }

    /** Resolves the ambiguous default inherited from {@code ICoreItem} and {@code ITrident}. */
    @Override
    public Item getItem() {
        return this;
    }

    /**
     * Trident-shaped alternative: one main part above one rod part.
     */
    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"#", "/"};
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModel() {
        return TridentClientModels.INVENTORY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getHandModel() {
        return TridentClientModels.INVENTORY;
    }

    /**
     * The Trident mod renders every {@link ITrident} item through its own {@code RenderItem}
     * mixin. When the item model is a built-in-renderer model (Silent Gear's layered tools are),
     * that mixin delegates the actual draw to this hook, which defaults to a no-op. That left the
     * trident invisible in hand, in item frames and in third person while the GUI icon (which goes
     * through RenderItem's own built-in-renderer path) worked.
     * <p>
     * Drawing through the item's {@link TileEntityItemStackRenderer} is exactly what the vanilla
     * and GUI paths do: the renderer resolves the model via {@code getItemModelWithOverrides}, so
     * the material textures, per-layer colors and the enchant glint all stay intact. The camera
     * transform has already been applied by the caller; the renderer works in model space like
     * {@code RenderItem.renderModel}.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void render(ItemStack stack, ItemCameraTransforms.TransformType type) {
        TileEntityItemStackRenderer renderer = stack.getItem().getTileEntityItemStackRenderer();
        if (renderer != null) {
            renderer.renderByItem(stack, 1f);
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return Trident.SPEAR;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        // Delegates to the Trident mod so riptide/durability/active-hand behavior stays in sync.
        return ITrident.super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        // Throws the mod's EntityTrident, which handles damage, loyalty/impaling and returning.
        ITrident.super.onPlayerStoppedUsing(stack, world, living, timeLeft);
    }
}
