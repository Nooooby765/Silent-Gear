package net.silentchaos512.gear.item.gear;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.config.ConfigOptionEquipment;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A combined pickaxe + axe + shovel. Effective on stone, wood, and dirt-like materials.
 * No per-tick work; all logic delegated to GearHelper.
 */
public class CorePaxel extends ItemAxe implements ICoreTool {
    private static final Set<Material> BASE_EFFECTIVE_MATERIALS = ImmutableSet.of(
            Material.ROCK, Material.IRON, Material.GLASS, Material.PACKED_ICE,
            Material.GOURD, Material.WOOD, Material.LEAVES, Material.PLANTS, Material.VINE,
            Material.GROUND, Material.SAND, Material.SNOW, Material.CLAY, Material.CRAFTED_SNOW);

    private final Set<String> toolClasses = new HashSet<>();

    public CorePaxel() {
        super(Objects.requireNonNull(GearData.FAKE_MATERIAL), 0f, 0f);
        // Registers the vanilla harvest classes this tool can fulfil.
        GearHelper.setHarvestLevel(this, "pickaxe", 0, this.toolClasses);
        GearHelper.setHarvestLevel(this, "axe", 0, this.toolClasses);
        GearHelper.setHarvestLevel(this, "shovel", 0, this.toolClasses);
    }

    @Nonnull
    @Override
    public ConfigOptionEquipment getConfig() {
        return Config.paxel;
    }

    @Override
    public String[] getAlternativeRecipe() {
        return new String[]{"###", " # ", " / "};
    }

    @Override
    public String getGearClass() {
        return "paxel";
    }

    @Override
    public GearType getGearType() {
        return GearType.PAXEL;
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack tool) {
        return canHarvestBlock(state, GearData.getStatInt(tool, CommonItemStats.HARVEST_LEVEL));
    }

    @Override
    public boolean canHarvestBlock(IBlockState state) {
        return canHarvestBlock(state, 10);
    }

    private boolean canHarvestBlock(IBlockState state, int toolLevel) {
        if (state.getBlock().getHarvestLevel(state) > toolLevel) return false;
        return BASE_EFFECTIVE_MATERIALS.contains(state.getMaterial());
    }

    //region Standard tool overrides

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        GearClientHelper.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return GearHelper.getAttributeModifiers(slot, stack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return GearHelper.getDestroySpeed(stack, state, BASE_EFFECTIVE_MATERIALS);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState state) {
        return GearHelper.getHarvestLevel(stack, toolClass, state, BASE_EFFECTIVE_MATERIALS);
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
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, GearHelper.calcDamageClamped(stack, damage));
        if (GearHelper.isBroken(stack)) GearData.recalculateStats(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return GearData.getStatInt(stack, CommonItemStats.DURABILITY);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return GearHelper.getRarity(stack);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return GearHelper.isBroken(stack) ? ImmutableSet.of() : ImmutableSet.copyOf(this.toolClasses);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return GearClientHelper.hasEffect(stack);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return GearHelper.hitEntity(stack, target, attacker);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        GearHelper.getSubItems(this, tab, items);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        return GearHelper.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
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
