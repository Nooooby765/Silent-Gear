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
import net.silentchaos512.gear.api.parts.ModularCoatingPart;
import net.silentchaos512.gear.api.parts.PartCoating;
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
 * 模块化涂层的独立物品。材料存于 NBT，颜色在合成时预解析为 int，渲染期只读 int。
 */
public class ModularCoatingItem extends Item implements IColoredItem {
    public static ItemStack createStack(@Nullable ItemPartData material) {
        PartCoating source = getSource(material);
        if (source == null || source.getRegistryName() == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(ModItems.modularCoating);
        NBTTagCompound tags = new NBTTagCompound();
        tags.setString(ModularCoatingPart.NBT_MATERIAL, ModularCoatingPart.getMaterialKey(source));
        tags.setInteger(ModularCoatingPart.NBT_COLOR, ModularCoatingPart.getSourceColor(source));
        stack.setTagCompound(tags);
        if (material.getGrade() != MaterialGrade.NONE)
            material.getGrade().setGradeOnStack(stack);
        return stack;
    }

    @Nullable
    private static PartCoating getSource(@Nullable ItemPartData material) {
        if (material == null || material.getPart() instanceof ModularCoatingPart) return null;
        ItemPart part = material.getPart();
        return part instanceof PartCoating ? (PartCoating) part : null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IItemColor getColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex != 0) return Color.VALUE_WHITE;
            NBTTagCompound tags = stack.getTagCompound();
            if (tags == null) return Color.VALUE_WHITE;
            int color = tags.getInteger(ModularCoatingPart.NBT_COLOR);
            return color == 0 ? Color.VALUE_WHITE : color;
        };
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        PartCoating source = ModularCoatingPart.getSource(stack);
        // 与工具 tooltip/JEI 使用同一“材料 + 覆层”拼接，避免物品栏再显示固定名“模块化覆层”
        return source == null
                ? super.getItemStackDisplayName(stack)
                : ModularCoatingPart.getDisplayNameForMaterial(source, stack).getUnformattedText();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        PartCoating source = ModularCoatingPart.getSource(stack);
        if (source != null) {
            String materialName = source.getDisplayName(ItemPartData.instance(source), ItemStack.EMPTY).getUnformattedComponentText();
            tooltip.add("" + source.getNameColor() + TextFormatting.ITALIC
                    + SilentGear.i18n.itemSubText("modular_coating", "material", materialName));
            // 仅在悬停时解析，按名称排序展示材料 traits
            Map<Trait, Integer> traits = source.getTraits(ItemStack.EMPTY, ItemPartData.instance(source));
            List<Map.Entry<Trait, Integer>> entries = new ArrayList<>(traits.entrySet());
            entries.sort(Comparator.comparing(entry -> entry.getKey().getName().toString()));
            for (Map.Entry<Trait, Integer> entry : entries) {
                tooltip.add(entry.getKey().getNameColor()
                        + entry.getKey().getTranslatedName(entry.getValue()));
            }
        } else {
            tooltip.add(TextFormatting.ITALIC
                    + SilentGear.i18n.itemSubText("modular_coating", "noMaterial"));
        }
    }
}
