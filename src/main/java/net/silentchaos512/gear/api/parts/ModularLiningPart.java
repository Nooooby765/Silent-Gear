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
 * 护甲衬里。衬里本身不渲染，NBT 中的材料 key 只用于属性/traits/颜色解析。
 */
public class ModularLiningPart extends PartLining {
    public static final String NBT_MATERIAL = "Material";
    public static final String NBT_COLOR = "Color";

    public ModularLiningPart(ResourceLocation name, PartOrigins origin) {
        super(name, origin);
    }

    @Nullable
    public static PartLining getSource(@Nullable ItemPartData part) {
        return part == null ? null : getSource(part.getCraftingItem());
    }

    @Nullable
    public static PartLining getSource(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) return null;
        String key = tags.getString(NBT_MATERIAL);
        if (key.isEmpty()) return null;

        ItemPart material = ModularMaterialRegistry.get(key, PartType.LINING);
        if (material instanceof PartLining && !(material instanceof ModularLiningPart)) {
            return (PartLining) material;
        }
        ItemPart part = PartRegistry.get(key);
        return part instanceof PartLining && !(part instanceof ModularLiningPart) ? (PartLining) part : null;
    }

    /**
     * 蓝图材料解析：毛料/皮革/亚麻线只允许解析为 LINING 材料，未登记材料返回 null。
     */
    @Nullable
    public static ItemPartData getBlueprintMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ItemPart material = ModularMaterialRegistry.find(stack, PartType.LINING);
        if (material instanceof PartLining && !(material instanceof ModularLiningPart))
            return ItemPartData.instance(material);
        return null;
    }

    public static boolean isValidBlueprintMaterial(ItemPart part) {
        return part instanceof PartLining && !(part instanceof ModularLiningPart);
    }

    public static int getSourceColor(PartLining source) {
        return source.getDisplayProperties(GearType.ARMOR).getTextureColor();
    }

    public static String getMaterialKey(PartLining source) {
        if (source instanceof ModularLiningMaterial)
            return ((ModularLiningMaterial) source).getMaterialId().toString();
        ResourceLocation name = source.getRegistryName();
        return name == null ? "" : name.toString();
    }

    @Override
    public Collection<StatInstance> getStatModifiers(ItemStack gear, ItemStat stat, ItemPartData part) {
        PartLining source = getSource(part);
        if (source == null) return Collections.emptyList();
        return source.getStatModifiers(gear, stat, ItemPartData.instance(source));
    }

    @Override
    public Map<Trait, Integer> getTraits(ItemStack gear, ItemPartData part) {
        PartLining source = getSource(part);
        return source == null ? super.getTraits(gear, part) : source.getTraits(gear, ItemPartData.instance(source));
    }

    @Override
    public int getColor(ItemPartData part, ItemStack gear, int animationFrame) {
        ItemStack stack = part.getCraftingItem();
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            int color = stack.getTagCompound().getInteger(NBT_COLOR);
            if (color != 0) return color;
        }
        PartLining source = getSource(part);
        return source != null ? source.getColor(part, gear, animationFrame)
                : super.getColor(part, gear, animationFrame);
    }

    @Override
    public String getModelIndex(ItemPartData part, int animationFrame) {
        String index = super.getModelIndex(part, animationFrame);
        ItemStack stack = part.getCraftingItem();
        if (stack.isEmpty() || !stack.hasTagCompound()) return index;
        String key = stack.getTagCompound().getString(NBT_MATERIAL);
        if (key.isEmpty()) return index;
        int colon = key.indexOf(':');
        return index + "_" + (colon >= 0 ? key.substring(colon + 1) : key);
    }

    @Override
    public ITextComponent getDisplayName(@Nullable ItemPartData part, ItemStack gear) {
        PartLining source = part == null ? null : getSource(part);
        if (source == null) return super.getDisplayName(part, gear);
        return getDisplayNameForMaterial(source, gear);
    }

    @Override
    public TextFormatting getDisplayNameColor(@Nullable ItemPartData part, ItemStack gear) {
        PartLining source = part == null ? null : getSource(part);
        return source != null ? source.getNameColor() : super.getDisplayNameColor(part, gear);
    }

    @Override
    public String getTranslatedName(@Nullable ItemPartData part, ItemStack gear) {
        return getDisplayName(part, gear).getUnformattedText();
    }

    public static ITextComponent getDisplayNameForMaterial(ItemPart source, ItemStack gear) {
        String materialName = source.getDisplayName(ItemPartData.instance(source), gear)
                .getUnformattedComponentText();
        return new TextComponentTranslation("part.silentgear.modular_lining.name", materialName);
    }
}
