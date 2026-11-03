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

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
 * Modular trident, available only when the Trident mod is installed.
 * <p>
 * Stats, traits, blueprints, templates, repairing and salvage all flow through the normal Silent
 * Gear systems; the Trident mod only supplies the throwable entity and its rendering. The item
 * itself is a {@link CoreSword} subclass, so melee behaviour is identical to the other modular
 * weapons.
 * <p>
 * Rendering: the mod's mixins ask an {@link ITrident} for its GUI and hand models. Returning the
 * Silent Gear layered model makes the inventory icon material-colored (standard {@code ToolModel}
 * + item color handlers), while the mod's own renderer takes over for the hand, dropped and
 * thrown-entity views, where its 3D model is used.
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

    /**
     * Resolves the otherwise ambiguous default inherited from {@code ICoreItem} and
     * {@code ITrident}.
     */
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
        // Delegates to the Trident mod so behavior (riptide checks, durability checks, active
        // hand) stays in sync with the mod instead of being duplicated here.
        return ITrident.super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        // Throws the mod's EntityTrident, which already handles damage, loyalty/impaling and
        // returning logic.
        ITrident.super.onPlayerStoppedUsing(stack, world, living, timeLeft);
    }
}
