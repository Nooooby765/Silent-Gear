package net.silentchaos512.gear.api.parts;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.silentchaos512.gear.api.event.GetStatModifierEvent;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.Trait;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A tip that derives its stats, traits and color from an independent tip material stored in NBT.
 * The part itself is registered once; all materials share the same part instance.
 */
public class ModularTipPart extends PartTip {
    public static final String NBT_MATERIAL = "Material";
    public static final String NBT_COLOR = "Color";

    public ModularTipPart(ResourceLocation name, PartOrigins origin) {
        super(name, origin);
    }

    @Nullable
    public static ItemPart getSource(@Nullable ItemPartData part) {
        return part == null ? null : getSource(part.getCraftingItem());
    }

    /**
     * Resolves the tip material key from the modular material table, then the main material
     * fallback for old NBT. Unknown keys resolve to null and are skipped safely.
     */
    @Nullable
    public static ItemPart getSource(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) return null;
        String key = tags.getString(NBT_MATERIAL);
        if (key.isEmpty()) return null;

        // 数据驱动材料优先。旧存档的 main_* 仍走下方主材料回退。
        ItemPart material = ModularMaterialRegistry.get(key, PartType.TIP);
        if (material != null) return material;

        ItemPart part = PartRegistry.get(key);
        if (part instanceof ModularTipPart) return null;
        if (part instanceof PartTip) return part;
        // 旧存档的 main_* key 仍作为主材料回退；tip_* 已从注册表移除时自然返回 null。
        if (part instanceof PartMain) return part;
        return null;
    }

    /**
     * Blueprint material resolver. This is only called while matching/crafting, never during
     * rendering. Materials with a tip definition are resolved from the modular material table; the
     * registry fallback keeps user-defined PartTip/PartMain materials working.
     */
    @Nullable
    public static ItemPartData getBlueprintMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;

        // 独立材料表优先：红石粉/萤石粉/青金石/下界石英等不经过 PartRegistry。
        ItemPart material = ModularMaterialRegistry.find(stack, PartType.TIP);
        if (material != null && material.getType() == PartType.TIP) return ItemPartData.instance(material);

        ItemPart part = PartRegistry.get(stack);
        // 严格类型门禁：涂层/其它类型绝不进入 tip 路径。
        if (part == null || part.getType() == PartType.COATING) return null;
        if (part instanceof ModularTipPart) return null;
        if (part.getType() == PartType.TIP) return ItemPartData.instance(part);
        if (part.getType() == PartType.MAIN) return ItemPartData.instance(part);
        return null;
    }

    /**
     * Blueprint material filter: TIP materials, plus MAIN as fallback, but never the modular tip
     * part itself or any coating.
     */
    public static boolean isValidBlueprintMaterial(ItemPart part) {
        return part.getType() == PartType.TIP && !(part instanceof ModularTipPart)
                || part.getType() == PartType.MAIN;
    }

    /**
     * 独立物品图标使用的材料颜色。取 knife 档位的纹理色，既有 tip 材料都定义了该档位；
     * 缺失时由 {@link ItemPart#getDisplayProperties(GearType)} 回退到 all。
     */
    public static int getSourceColor(ItemPart source) {
        return source.getDisplayProperties(GearType.KNIFE).getTextureColor();
    }

    /**
     * NBT 中应写入的材料 key。数据驱动材料使用 JSON 中的材料 id（与旧存档 key 一致），
     * legacy 部件使用其注册名。
     */
    public static String getMaterialKey(ItemPart source) {
        if (source instanceof ModularTipMaterial)
            return ((ModularTipMaterial) source).getMaterialId().toString();
        ResourceLocation name = source.getRegistryName();
        return name == null ? "" : name.toString();
    }

    public static String getMaterialPath(ItemPart source) {
        if (source instanceof ModularTipMaterial)
            return ((ModularTipMaterial) source).getMaterialId().getPath();
        ResourceLocation name = source.getRegistryName();
        return name == null ? "" : name.getPath();
    }

    @Override
    public Collection<StatInstance> getStatModifiers(ItemStack gear, ItemStat stat, ItemPartData part) {
        List<StatInstance> mods = new ArrayList<>();
        ItemPart source = getSource(part);
        if (source instanceof PartTip) {
            return source.getStatModifiers(gear, stat, ItemPartData.instance(source));
        }
        if (source instanceof PartMain) {
            float value = stat.compute(0f, source.getStatModifiers(gear, stat, ItemPartData.instance(source)));
            if (stat.isAffectedByGrades())
                value *= 1f + part.getGrade().bonusPercent / 100f;
            StatInstance modifier = createModifier(stat, value);
            if (modifier != null)
                mods.add(modifier);
        }

        GetStatModifierEvent event = new GetStatModifierEvent(part, stat, mods);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getModifiers();
    }

    @Nullable
    private static StatInstance createModifier(ItemStat stat, float value) {
        float result;
        StatInstance.Operation op = StatInstance.Operation.ADD;

        if (stat == CommonItemStats.DURABILITY) {
            result = Math.max(8f, Math.round(value * 0.20f));
        } else if (stat == CommonItemStats.HARVEST_SPEED) {
            result = Math.min(3f, value * 0.25f);
        } else if (stat == CommonItemStats.HARVEST_LEVEL) {
            if (value <= 0f) return null;
            result = value;
            op = StatInstance.Operation.MAX;
        } else if (stat == CommonItemStats.MELEE_DAMAGE
                || stat == CommonItemStats.MAGIC_DAMAGE
                || stat == CommonItemStats.RANGED_DAMAGE) {
            result = value * 0.5f;
        } else if (stat == CommonItemStats.ATTACK_SPEED || stat == CommonItemStats.RANGED_SPEED) {
            result = Math.max(0f, value * 0.5f);
        } else if (stat == CommonItemStats.ENCHANTABILITY) {
            result = Math.round(0.5f * (value - 10f));
        } else if (stat == CommonItemStats.RARITY) {
            result = MathHelper.clamp(Math.round(value * 0.5f), 0, 20);
        } else {
            return null;
        }

        return new StatInstance("modular_tip_" + stat.getName(), result, op);
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
        ItemPart source = getSource(part);
        return source == null ? index : index + "_" + getMaterialPath(source);
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

    /**
     * 材料名 + 类型后缀的统一拼接点。材料 lang 只提供裸材料名（如 Emerald / 绿宝石），
     * 避免与这里的 Tip/尖端 后缀重复。
     */
    public static ITextComponent getDisplayNameForMaterial(ItemPart source, ItemStack gear) {
        String materialName = source.getDisplayName(ItemPartData.instance(source), gear)
                .getUnformattedComponentText();
        return new TextComponentTranslation("part.silentgear.modular_tip.name", materialName);
    }
}
