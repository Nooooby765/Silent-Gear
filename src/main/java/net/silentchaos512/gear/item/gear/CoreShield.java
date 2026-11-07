package net.silentchaos512.gear.item.gear;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Modular shield. Blocking itself is entirely handled by vanilla (ItemShield + EnumAction.BLOCK).
 * This class only supplies per-stack stats and marks the item as a shield so that blocking can
 * damage it.
 */
public class CoreShield extends ItemShield implements ICoreTool {
    public static final String NAME = "shield";
    public static final String NBT_ROOT = "SilentGearShield";
    public static final String NBT_COLOR = "Color";
    public static final String NBT_COLORS = "Colors";

    // Same ratio as the upstream shield: 337/15 x the main material's armor durability.
    private static final float DURABILITY_FACTOR = 337f / 15f;
    private static final Set<ItemStat> RELEVANT_STATS = ImmutableSet.of(
            CommonItemStats.DURABILITY,
            CommonItemStats.ENCHANTABILITY,
            CommonItemStats.RARITY);

    @Override
    public String getGearClass() {
        return NAME;
    }

    @Override
    public GearType getGearType() {
        return GearType.SHIELD;
    }

    @Override
    public Set<ItemStat> getRelevantStats(ItemStack stack) {
        return RELEVANT_STATS;
    }

    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.shield;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"#/", "# "};
    }

    //region Per-stack stats

    @Override
    public int getMaxDamage(ItemStack stack) {
        // Never allow zero/negative max damage, which would make the shield unbreakable.
        return Math.max(1, Math.round(GearData.getStat(stack, CommonItemStats.ARMOR_DURABILITY) * DURABILITY_FACTOR));
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        if (GearHelper.isUnbreakable(stack)) return;
        if (!Config.gearBreaksPermanently)
            damage = MathHelper.clamp(damage, 0, getMaxDamage(stack));
        super.setDamage(stack, damage);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return GearData.getStatInt(stack, CommonItemStats.ENCHANTABILITY);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return GearHelper.getIsRepairable(toRepair, repair);
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity) {
        // Item's default only recognizes Items.SHIELD, which would prevent blocking from
        // consuming durability.
        return true;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        // Broken gear keeps existing, so explicitly stop blocking once durability is exhausted.
        return GearHelper.isBroken(stack) ? EnumAction.NONE : EnumAction.BLOCK;
    }

    //endregion

    //region Item overrides

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        GearHelper.onUpdate(stack, world, entity, itemSlot, isSelected);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (this.isInCreativeTab(tab))
            GearHelper.getSubItems(this, tab, subItems);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return GearHelper.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        GearClientHelper.addInformation(stack, world, tooltip, flag);
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return GearHelper.getRarity(stack);
    }

    @Override
    public void onConstructed(ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemPartData primary = getPrimaryPart(stack);
        ItemPartData rod = getRodPart(stack);
        int mainColor = GearHelper.getPartDisplayColor(primary, GearType.SHIELD);
        int rodColor = GearHelper.getPartDisplayColor(rod, GearType.SHIELD);

        NBTTagCompound tag = stack.getOrCreateSubCompound(NBT_ROOT);
        tag.setIntArray(NBT_COLORS, new int[]{rodColor, mainColor});
        // Keep the old single-color key for old saves and any legacy readers.
        tag.setInteger(NBT_COLOR, mainColor);
    }

    //endregion
}
