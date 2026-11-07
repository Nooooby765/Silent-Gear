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
 * A rod whose stats, traits and color come from an independent rod material stored in NBT.
 * The part is registered once; all rod materials share this single instance.
 */
public class ModularRodPart extends PartRod {
    public static final String NBT_MATERIAL = "Material";
    public static final String NBT_COLOR = "Color";

    public ModularRodPart(ResourceLocation name, PartOrigins origin) {
        super(name, origin);
    }

    @Nullable
    public static ItemPart getSource(@Nullable ItemPartData part) {
        return part == null ? null : getSource(part.getCraftingItem());
    }

    /**
     * Resolves the rod material from the immutable material table. There is deliberately no
     * PartRegistry fallback: only explicitly listed rod materials are valid.
     */
    @Nullable
    public static ItemPart getSource(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) return null;
        String key = tags.getString(NBT_MATERIAL);
        if (key.isEmpty()) return null;
        return ModularMaterialRegistry.get(key, PartType.ROD);
    }

    /**
     * Blueprint material resolver. Only materials present in the rod table are accepted; any
     * other main material makes the blueprint recipe fail instead of producing a weak rod.
     */
    @Nullable
    public static ItemPartData getBlueprintMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ItemPart material = ModularMaterialRegistry.find(stack, PartType.ROD);
        if (material instanceof ModularRodMaterial && material.getType() == PartType.ROD)
            return ItemPartData.instance(material);
        return null;
    }

    public static boolean isValidBlueprintMaterial(ItemPart part) {
        return part instanceof ModularRodMaterial && part.getType() == PartType.ROD;
    }

    /**
     * Icon color for the standalone modular rod item, taken from the material display map.
     */
    public static int getSourceColor(ItemPart source) {
        return source.getDisplayProperties(GearType.PICKAXE).getTextureColor();
    }

    public static String getMaterialKey(ItemPart source) {
        if (source instanceof ModularRodMaterial)
            return ((ModularRodMaterial) source).getMaterialId().toString();
        return "";
    }

    public static String getMaterialPath(ItemPart source) {
        if (source instanceof ModularRodMaterial)
            return ((ModularRodMaterial) source).getMaterialId().getPath();
        return "";
    }

    /**
     * Comparison used by the assembly check. Only the material base id is compared
     * (rod_iron vs main_iron), no registry lookups.
     */
    public static boolean isSameMainMaterial(ItemPartData rodPart, ItemPartData mainPart) {
        ItemPart source = getSource(rodPart);
        if (!(source instanceof ModularRodMaterial)) return true;
        if (!(mainPart.getPart() instanceof PartMain)) return true;

        ResourceLocation mainName = mainPart.getPart().getRegistryName();
        if (mainName == null) return true;
        String mainBase = mainName.getPath();
        if (mainBase.startsWith("main_")) mainBase = mainBase.substring("main_".length());
        return ((ModularRodMaterial) source).getBaseId().equals(mainBase);
    }

    /**
     * Returns false when a modular rod is present but its material does not match the first
     * PartMain in the assembly. Legacy rods and assemblies without a main pass through.
     */
    public static boolean hasMatchingMainMaterial(Collection<ItemPartData> data) {
        ItemPartData rod = null;
        ItemPartData main = null;
        for (ItemPartData part : data) {
            if (rod == null && part.getPart() instanceof ModularRodPart) rod = part;
            if (main == null && part.getPart() instanceof PartMain) main = part;
        }
        return rod == null || main == null || isSameMainMaterial(rod, main);
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

    /**
     * Reads the material path straight from NBT. No registry query and no ResourceLocation
     * allocation on the render path.
     */
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
        return new TextComponentTranslation("part.silentgear.modular_rod.name", materialName);
    }
}
