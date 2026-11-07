package net.silentchaos512.gear.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.MaterialGrade;
import net.silentchaos512.gear.api.parts.ModularLiningMaterial;
import net.silentchaos512.gear.api.parts.ModularLiningPart;
import net.silentchaos512.gear.api.parts.ModularMaterialRegistry;
import net.silentchaos512.gear.api.parts.PartLining;
import net.silentchaos512.gear.api.parts.PartType;
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
 * 单件物品承载全部衬里材料；材料 id 与颜色在合成时写入 NBT。
 */
public class ModularLiningItem extends Item implements IColoredItem {
    public static ItemStack createStack(@Nullable ItemPartData material) {
        PartLining source = getSource(material);
        if (source == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(ModItems.modularLining);
        NBTTagCompound tags = new NBTTagCompound();
        tags.setString(ModularLiningPart.NBT_MATERIAL, ModularLiningPart.getMaterialKey(source));
        // 颜色只在构造期解析一次，渲染只读 NBT int。
        tags.setInteger(ModularLiningPart.NBT_COLOR, ModularLiningPart.getSourceColor(source));
        stack.setTagCompound(tags);
        if (material.getGrade() != MaterialGrade.NONE)
            material.getGrade().setGradeOnStack(stack);
        return stack;
    }

    @Nullable
    private static PartLining getSource(@Nullable ItemPartData material) {
        if (material == null) return null;
        ItemPart part = material.getPart();
        return part instanceof PartLining && !(part instanceof ModularLiningPart) ? (PartLining) part : null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IItemColor getColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex != 0) return Color.VALUE_WHITE;
            NBTTagCompound tags = stack.getTagCompound();
            if (tags == null) return Color.VALUE_WHITE;
            int color = tags.getInteger(ModularLiningPart.NBT_COLOR);
            return color == 0 ? Color.VALUE_WHITE : color;
        };
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        PartLining source = ModularLiningPart.getSource(stack);
        return source == null
                ? super.getItemStackDisplayName(stack)
                : ModularLiningPart.getDisplayNameForMaterial(source, stack).getUnformattedText();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (ItemPart part : ModularMaterialRegistry.getMaterials(PartType.LINING)) {
            if (part instanceof ModularLiningMaterial) {
                ItemStack stack = createStack(ItemPartData.instance(part));
                if (!stack.isEmpty()) items.add(stack);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        PartLining source = ModularLiningPart.getSource(stack);
        if (source == null) {
            tooltip.add(TextFormatting.RED + SilentGear.i18n.itemSubText("modular_lining", "noMaterial"));
        } else {
            String materialName = source.getDisplayName(ItemPartData.instance(source), ItemStack.EMPTY)
                    .getUnformattedComponentText();
            tooltip.add("" + source.getNameColor() + TextFormatting.ITALIC
                    + SilentGear.i18n.itemSubText("modular_lining", "material", materialName));
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
