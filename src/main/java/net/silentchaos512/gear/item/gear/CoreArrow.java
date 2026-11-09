package net.silentchaos512.gear.item.gear;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.ModularFletchingPart;
import net.silentchaos512.gear.api.parts.PartDataList;
import net.silentchaos512.gear.api.parts.PartFletching;
import net.silentchaos512.gear.api.parts.PartMain;
import net.silentchaos512.gear.api.parts.PartRod;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;
import net.silentchaos512.gear.entity.EntitySilentArrow;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Modular arrow: consumable and stackable (vanilla arrow stack size, no durability).
 * Damage/speed/accuracy are computed once in {@link #onConstructed(ItemStack)} and cached in stack NBT.
 */
public class CoreArrow extends ItemArrow implements ICoreTool {
    public static final String NAME = "arrow";
    public static final String NBT_ROOT = "SilentGearArrow";
    public static final String NBT_COLORS = "Colors";
    public static final String NBT_DAMAGE = "Damage";
    public static final String NBT_SPEED = "Speed";
    public static final String NBT_ACCURACY = "Accuracy";

    private static final Set<ItemStat> RELEVANT_STATS = ImmutableSet.of(
            CommonItemStats.RANGED_DAMAGE,
            CommonItemStats.RANGED_SPEED,
            CommonItemStats.RARITY);

    // Upstream turns one main material's durability into a stack size of arrows.
    private static final float DURABILITY_PER_ARROW = 32.5f;

    @Override
    public String getGearClass() {
        return NAME;
    }

    @Override
    public GearType getGearType() {
        return GearType.ARROW;
    }

    @Override
    public Set<ItemStat> getRelevantStats(ItemStack stack) {
        return RELEVANT_STATS;
    }

    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.arrow;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"#", "/", "@"};
    }

    @Override
    public boolean usesUniqueId() {
        return false;
    }

    @Override
    public boolean canReceiveUpgrades() {
        return false;
    }

    //region Arrow entity

    @Override
    public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
        return new EntitySilentArrow(worldIn, shooter, stack);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, EntityPlayer player) {
        return player.capabilities.isCreativeMode
                || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bow) > 0;
    }

    //endregion

    //region Precomputed data

    @Override
    public void onConstructed(ItemStack stack) {
        if (stack.isEmpty()) return;

        PartDataList parts = GearData.getConstructionParts(stack);
        ItemPartData rod = null;
        ItemPartData main = null;
        ItemPartData fletching = null;
        for (ItemPartData part : parts) {
            if (rod == null && part.getPart() instanceof PartRod) rod = part;
            if (main == null && part.getPart() instanceof PartMain) main = part;
            if (fletching == null && part.getPart() instanceof PartFletching) fletching = part;
        }

        NBTTagCompound tag = stack.getOrCreateSubCompound(NBT_ROOT);
        int[] colors = new int[]{0xFFFFFF, 0xFFFFFF, 0xFFFFFF};
        colors[0] = GearHelper.getPartDisplayColor(rod, GearType.ARROW);
        colors[1] = GearHelper.getPartDisplayColor(main, GearType.ARROW);
        colors[2] = GearHelper.getPartDisplayColor(fletching, GearType.ARROW);
        tag.setIntArray(NBT_COLORS, colors);

        tag.setFloat(NBT_DAMAGE, GearData.getStat(stack, CommonItemStats.RANGED_DAMAGE));

        float speed = 1.0f;
        if (fletching != null) {
            speed += CommonItemStats.RANGED_SPEED.compute(0f,
                    fletching.getStatModifiers(stack, CommonItemStats.RANGED_SPEED));
        }
        tag.setFloat(NBT_SPEED, MathHelper.clamp(speed, 0.5f, 1.5f));

        float accuracy = fletching != null ? ModularFletchingPart.getProjectileAccuracy(fletching) : 1.0f;
        tag.setFloat(NBT_ACCURACY, MathHelper.clamp(accuracy, 0.5f, 2.0f));

        float durability = GearData.getStat(stack, CommonItemStats.DURABILITY);
        stack.setCount(MathHelper.clamp(Math.round(durability / DURABILITY_PER_ARROW), 1, 64));
    }

    //endregion

    //region Standard overrides

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        GearClientHelper.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return GearHelper.getItemStackDisplayName(stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab))
            GearHelper.getSubItems(this, tab, items);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        // Intentionally empty: arrows are not graded and must keep identical NBT so they stack.
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
    public EnumRarity getRarity(ItemStack stack) {
        return GearHelper.getRarity(stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return GearClientHelper.hasEffect(stack);
    }

    //endregion
}
