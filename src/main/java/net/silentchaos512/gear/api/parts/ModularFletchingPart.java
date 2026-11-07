package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.Trait;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Fletching whose stats, traits and color come from an independent material stored in NBT.
 * Registered once; all fletching materials share this instance.
 */
public class ModularFletchingPart extends PartFletching {
    public static final String NBT_MATERIAL = "Material";
    public static final String NBT_COLOR = "Color";

    public ModularFletchingPart(ResourceLocation name, PartOrigins origin) {
        super(name, origin);
    }

    @Nullable
    public static ItemPart getSource(@Nullable ItemPartData part) {
        return part == null ? null : getSource(part.getCraftingItem());
    }

    @Nullable
    public static ItemPart getSource(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) return null;
        String key = tags.getString(NBT_MATERIAL);
        if (key.isEmpty()) return null;
        return ModularMaterialRegistry.get(key, PartType.FLETCHING);
    }

    @Nullable
    public static ItemPartData getBlueprintMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ItemPart material = ModularMaterialRegistry.find(stack, PartType.FLETCHING);
        if (material instanceof ModularFletchingMaterial && material.getType() == PartType.FLETCHING)
            return ItemPartData.instance(material);
        return null;
    }

    public static boolean isValidBlueprintMaterial(ItemPart part) {
        return part instanceof ModularFletchingMaterial && part.getType() == PartType.FLETCHING;
    }

    public static int getSourceColor(ItemPart source) {
        return source.getDisplayProperties(GearType.ARROW).getTextureColor();
    }

    public static String getMaterialKey(ItemPart source) {
        if (source instanceof ModularFletchingMaterial)
            return ((ModularFletchingMaterial) source).getMaterialId().toString();
        return "";
    }

    public static String getMaterialPath(ItemPart source) {
        if (source instanceof ModularFletchingMaterial)
            return ((ModularFletchingMaterial) source).getMaterialId().getPath();
        return "";
    }

    /**
     * Projectile accuracy multiplier used by {@code CoreArrow} when the arrow is fired.
     */
    public static float getProjectileAccuracy(@Nullable ItemPartData part) {
        ItemPart source = getSource(part);
        return source instanceof ModularFletchingMaterial
                ? ((ModularFletchingMaterial) source).getAccuracy() : 1.0f;
    }

    @Override
    public Collection<StatInstance> getStatModifiers(ItemStack gear, ItemStat stat, ItemPartData part) {
        ItemPart source = getSource(part);
        if (source == null) return Collections.emptyList();
        return source.getStatModifiers(gear, stat, ItemPartData.instance(source));
    }

    @Override
    public Map<Trait, Integer> getTraits(ItemStack gear, ItemPartData part) {
        ItemPart source = getSource(part);
        return source == null ? super.getTraits(gear, part) : source.getTraits(gear, ItemPartData.instance(source));
    }

    @Override
    public int getColor(ItemPartData part, ItemStack gear, int animationFrame) {
        ItemStack stack = part.getCraftingItem();
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            int color = stack.getTagCompound().getInteger(NBT_COLOR);
            if (color != 0) return color;
        }
        ItemPart source = getSource(part);
        return source != null ? source.getColor(part, gear, animationFrame)
                : super.getColor(part, gear, animationFrame);
    }

    @Override
    public String getModelIndex(ItemPartData part, int animationFrame) {
        String index = super.getModelIndex(part, animationFrame);
        String materialPath = getMaterialPathFromNbt(part);
        return materialPath.isEmpty() ? index : index + "_" + materialPath;
    }

    private static String getMaterialPathFromNbt(ItemPartData part) {
        ItemStack stack = part.getCraftingItem();
        if (stack.isEmpty() || !stack.hasTagCompound()) return "";
        String key = stack.getTagCompound().getString(NBT_MATERIAL);
        int colon = key.indexOf(':');
        return colon >= 0 ? key.substring(colon + 1) : key;
    }

    @Override
    public ITextComponent getDisplayName(@Nullable ItemPartData part, ItemStack gear) {
        ItemPart source = part == null ? null : getSource(part);
        if (source == null) return super.getDisplayName(part, gear);
        return getDisplayNameForMaterial(source, gear);
    }

    @Override
    public TextFormatting getDisplayNameColor(@Nullable ItemPartData part, ItemStack gear) {
        ItemPart source = part == null ? null : getSource(part);
        return source != null ? source.getNameColor() : super.getDisplayNameColor(part, gear);
    }

    @Override
    public String getTranslatedName(@Nullable ItemPartData part, ItemStack gear) {
        return getDisplayName(part, gear).getUnformattedText();
    }

    public static ITextComponent getDisplayNameForMaterial(ItemPart source, ItemStack gear) {
        String materialName = source.getDisplayName(ItemPartData.instance(source), gear)
                .getUnformattedComponentText();
        return new TextComponentTranslation("part.silentgear.modular_fletching.name", materialName);
    }
}
