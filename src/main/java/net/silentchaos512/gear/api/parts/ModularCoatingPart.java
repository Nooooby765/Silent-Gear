package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.Trait;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 模块化涂层。材料只存于物品 NBT 的 Material；单个 part 承载所有材料，数值、traits、颜色
 * 全部来自 modular_materials JSON 构建的独立材料对象。
 */
public class ModularCoatingPart extends PartCoating {
    public static final String NBT_MATERIAL = "Material";
    public static final String NBT_COLOR = "Color";

    public ModularCoatingPart(ResourceLocation name, PartOrigins origin) {
        super(name, origin);
    }

    @Nullable
    public static PartCoating getSource(@Nullable ItemPartData part) {
        return part == null ? null : getSource(part.getCraftingItem());
    }

    /**
     * Resolves the coating part key from the modular material table; unknown keys resolve to null
     * so old NBT referencing removed legacy parts is skipped safely.
     */
    @Nullable
    public static PartCoating getSource(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return null;
        String key = stack.getTagCompound().getString(NBT_MATERIAL);
        if (key.isEmpty()) return null;

        // 数据驱动涂层材料；旧存档的 coating_gold key 命中 JSON 材料 id。
        ItemPart material = ModularMaterialRegistry.get(key, PartType.COATING);
        if (material instanceof PartCoating) return (PartCoating) material;

        ItemPart part = PartRegistry.get(key);
        if (part instanceof ModularCoatingPart) return null;
        if (part instanceof PartCoating) return (PartCoating) part;
        return null;
    }

    /**
     * Blueprint material resolver. Only called while matching/crafting, never during rendering.
     */
    @Nullable
    public static ItemPartData getBlueprintMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;

        ItemPart material = ModularMaterialRegistry.find(stack, PartType.COATING);
        if (material instanceof PartCoating && material.getType() == PartType.COATING)
            return ItemPartData.instance(material);

        ItemPart part = PartRegistry.get(stack);
        // 严格类型门禁：tip/main 等非涂层类型绝不进入 coating 路径。
        if (part == null || part.getType() != PartType.COATING && part.getType() != PartType.MAIN)
            return null;
        if (part instanceof ModularCoatingPart) return null;
        if (part.getType() == PartType.COATING) return ItemPartData.instance(part);
        return null;
    }

    /**
     * 蓝图材料过滤：只接受 COATING 类型的材料对象。
     */
    public static boolean hasCoating(ItemPart part) {
        return part.getType() == PartType.COATING
                && part instanceof PartCoating && !(part instanceof ModularCoatingPart);
    }

    public static int getSourceColor(PartCoating source) {
        return source.getDisplayProperties(GearType.KNIFE).getTextureColor();
    }

    /**
     * NBT 中应写入的材料 key：数据驱动涂层使用 JSON 材料 id。
     */
    public static String getMaterialKey(PartCoating source) {
        if (source instanceof ModularCoatingMaterial)
            return ((ModularCoatingMaterial) source).getMaterialId().toString();
        ResourceLocation name = source.getRegistryName();
        return name == null ? "" : name.toString();
    }

    @Override
    public boolean requiresCraftingItem() {
        return true;
    }

    @Override
    public Collection<StatInstance> getStatModifiers(ItemStack gear, ItemStat stat, ItemPartData part) {
        PartCoating source = getSource(part);
        return source == null ? Collections.emptyList()
                : source.getStatModifiers(gear, stat, ItemPartData.instance(source));
    }

    @Override
    public Map<Trait, Integer> getTraits(ItemStack gear, ItemPartData part) {
        PartCoating source = getSource(part);
        return source == null ? super.getTraits(gear, part)
                : source.getTraits(gear, ItemPartData.instance(source));
    }

    @Override
    public int getColor(ItemPartData part, ItemStack gear, int animationFrame) {
        ItemStack stack = part.getCraftingItem();
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            int color = stack.getTagCompound().getInteger(NBT_COLOR);
            if (color != 0) return color;
        }
        PartCoating source = getSource(part);
        return source != null ? source.getColor(part, gear, animationFrame)
                : super.getColor(part, gear, animationFrame);
    }

    @Override
    public ITextComponent getDisplayName(@Nullable ItemPartData part, ItemStack gear) {
        PartCoating source = part == null ? null : getSource(part);
        if (source == null) return super.getDisplayName(part, gear);
        String materialName = source.getDisplayName(ItemPartData.instance(source), gear).getUnformattedComponentText();
        return new TextComponentTranslation("part.silentgear.modular_coating.name", materialName);
    }
}
