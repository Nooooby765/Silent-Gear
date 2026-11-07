package net.silentchaos512.gear.item.gear;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.PartMain;
import net.silentchaos512.gear.api.parts.PartRod;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * 模块化钓鱼竿。抛竿/收竿/漂浮物/钓鱼判定全部复用原版 {@link ItemFishingRod}，
 * 这里只通过 Forge 的按 ItemStack 钩子覆盖耐久、附魔、修理、稀有度与 traits。
 * 战利品仍走原版 {@code WeightedRandomFishable}/loot 机制。
 */
public class CoreFishingRod extends ItemFishingRod implements ICoreItem {
    public static final String NBT_COLORS = "Colors";
    private static final Set<ItemStat> RELEVANT_STATS = ImmutableSet.of(
            CommonItemStats.DURABILITY,
            CommonItemStats.ENCHANTABILITY,
            CommonItemStats.RARITY
    );

    public CoreFishingRod() {
        super();
        setMaxStackSize(1);
    }

    //region Standard gear overrides

    @Nonnull
    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.fishingRod;
    }

    @Override
    public String getGearClass() {
        return "fishing_rod";
    }

    @Override
    public GearType getGearType() {
        return GearType.FISHING_ROD;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[0];
    }

    @Override
    public Set<ItemStat> getRelevantStats(@Nonnull ItemStack stack) {
        return RELEVANT_STATS;
    }

    @Override
    public boolean matchesRecipe(Collection<ItemStack> parts) {
        // 钓鱼竿直接由蓝图产物构造，不需要额外组装配方。
        return false;
    }

    @Override
    public ItemPartData[] getRenderParts(ItemStack stack) {
        ItemPartData rod = null;
        ItemPartData main = null;
        for (ItemPartData part : GearData.getConstructionParts(stack)) {
            if (rod == null && part.getPart() instanceof PartRod) rod = part;
            else if (main == null && part.getPart() instanceof PartMain) main = part;
        }
        if (rod == null) rod = ItemPartData.instance(net.silentchaos512.gear.item.ToolRods.WOOD.getPart());
        if (main == null) main = getPrimaryPart(stack);
        return new ItemPartData[]{rod, main};
    }

    @Override
    public void onConstructed(ItemStack stack) {
        ItemPartData[] parts = getRenderParts(stack);
        int[] colors = new int[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            colors[i] = GearHelper.getPartDisplayColor(parts[i], GearType.FISHING_ROD);
        }
        stack.setTagInfo(NBT_COLORS, new NBTTagIntArray(colors));
    }

    public static int[] getColors(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound() || !stack.getTagCompound().hasKey(NBT_COLORS, 11))
            return new int[]{0xFFFFFF, 0xFFFFFF};
        return stack.getTagCompound().getIntArray(NBT_COLORS);
    }

    //endregion

    //region Item hooks

    @Override
    public void addInformation(ItemStack stack, World worldIn, java.util.List<String> tooltip, ITooltipFlag flagIn) {
        GearClientHelper.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return GearHelper.getIsRepairable(toRepair, repair);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return GearData.getStatInt(stack, CommonItemStats.ENCHANTABILITY);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return GearHelper.getItemStackDisplayName(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Math.max(1, GearData.getStatInt(stack, CommonItemStats.DURABILITY));
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, GearHelper.calcDamageClamped(stack, damage));
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return GearHelper.getRarity(stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return GearClientHelper.hasEffect(stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        GearHelper.getSubItems(this, tab, items);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        GearHelper.onUpdate(stack, world, entity, itemSlot, isSelected);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return GearClientHelper.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    //endregion
}
