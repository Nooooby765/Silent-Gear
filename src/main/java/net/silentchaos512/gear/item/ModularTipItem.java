package net.silentchaos512.gear.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.MaterialGrade;
import net.silentchaos512.gear.api.parts.ModularTipPart;
import net.silentchaos512.gear.api.parts.PartMain;
import net.silentchaos512.gear.api.parts.PartTip;
import net.silentchaos512.gear.api.traits.Trait;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.lib.item.IColoredItem;
import net.silentchaos512.lib.util.Color;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * One item representing modular tips of any material; the part key is stored in NBT, so only a
 * single item and part need to be registered.
 */
public class ModularTipItem extends Item implements IColoredItem {
    public static ItemStack createStack(@Nullable ItemPartData material) {
        ItemPart source = getSource(material);
        if (source == null || source.getRegistryName() == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(ModItems.modularTip);
        NBTTagCompound tags = new NBTTagCompound();
        tags.setString(ModularTipPart.NBT_MATERIAL, ModularTipPart.getMaterialKey(source));
        // 合成时一次性解析颜色，渲染期只读 int，避免每帧解析注册名
        tags.setInteger(ModularTipPart.NBT_COLOR, ModularTipPart.getSourceColor(source));
        stack.setTagCompound(tags);
        if (material.getGrade() != MaterialGrade.NONE)
            material.getGrade().setGradeOnStack(stack);
        return stack;
    }

    @Nullable
    private static ItemPart getSource(@Nullable ItemPartData material) {
        if (material == null || material.getPart() instanceof ModularTipPart) return null;
        ItemPart part = material.getPart();
        if (part instanceof PartTip) return part;
        // 主材料仍可作为模块化尖端材料（Material 写 main_* key），旧存档兼容路径依赖此行为。
        if (part instanceof PartMain) return part;
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IItemColor getColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex != 0) return Color.VALUE_WHITE;
            NBTTagCompound tags = stack.getTagCompound();
            if (tags == null) return Color.VALUE_WHITE;
            int color = tags.getInteger(ModularTipPart.NBT_COLOR);
            return color == 0 ? Color.VALUE_WHITE : color;
        };
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ItemPart source = ModularTipPart.getSource(stack);
        // 与工具 tooltip/JEI 使用同一“材料 + 尖端”拼接，避免物品栏再显示固定名“模块化尖端”
        return source == null
                ? super.getItemStackDisplayName(stack)
                : ModularTipPart.getDisplayNameForMaterial(source, stack).getUnformattedText();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        ItemPart source = ModularTipPart.getSource(stack);
        if (source == null) {
            tooltip.add(TextFormatting.RED + SilentGear.i18n.itemSubText("modular_tip", "noMaterial"));
        } else {
            String materialName = source.getDisplayName(ItemPartData.instance(source), ItemStack.EMPTY)
                    .getUnformattedComponentText();
            tooltip.add("" + source.getNameColor() + TextFormatting.ITALIC
                    + SilentGear.i18n.itemSubText("modular_tip", "material", materialName));
            // 仅在悬停时解析，按名称排序展示材料 traits
            Map<Trait, Integer> traits = source.getTraits(ItemStack.EMPTY, ItemPartData.instance(source));
            List<Map.Entry<Trait, Integer>> entries = new ArrayList<>(traits.entrySet());
            entries.sort(Comparator.comparing(entry -> entry.getKey().getName().toString()));
            for (Map.Entry<Trait, Integer> entry : entries) {
                tooltip.add(entry.getKey().getNameColor()
                        + entry.getKey().getTranslatedName(entry.getValue()));
            }
        }
    }
}
