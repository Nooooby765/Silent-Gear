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
import net.silentchaos512.gear.api.parts.ModularMaterialRegistry;
import net.silentchaos512.gear.api.parts.ModularRodMaterial;
import net.silentchaos512.gear.api.parts.ModularRodPart;
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
 * A single item representing modular rods of any rod material. The material id is stored in NBT
 * so only one item and one part need to be registered.
 */
public class ModularRodItem extends Item implements IColoredItem {
    public static ItemStack createStack(@Nullable ItemPartData material) {
        ItemPart source = getSource(material);
        if (source == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(ModItems.modularRod);
        NBTTagCompound tags = new NBTTagCompound();
        tags.setString(ModularRodPart.NBT_MATERIAL, ModularRodPart.getMaterialKey(source));
        // Resolve the color once at craft time; rendering only reads an int.
        tags.setInteger(ModularRodPart.NBT_COLOR, ModularRodPart.getSourceColor(source));
        stack.setTagCompound(tags);
        if (material.getGrade() != MaterialGrade.NONE)
            material.getGrade().setGradeOnStack(stack);
        return stack;
    }

    @Nullable
    private static ItemPart getSource(@Nullable ItemPartData material) {
        if (material == null) return null;
        ItemPart part = material.getPart();
        return part instanceof ModularRodMaterial ? part : null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IItemColor getColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex != 0) return Color.VALUE_WHITE;
            NBTTagCompound tags = stack.getTagCompound();
            if (tags == null) return Color.VALUE_WHITE;
            int color = tags.getInteger(ModularRodPart.NBT_COLOR);
            return color == 0 ? Color.VALUE_WHITE : color;
        };
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ItemPart source = ModularRodPart.getSource(stack);
        return source == null
                ? super.getItemStackDisplayName(stack)
                : ModularRodPart.getDisplayNameForMaterial(source, stack).getUnformattedText();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (ItemPart part : ModularMaterialRegistry.getMaterials(PartType.ROD)) {
            ItemStack stack = createStack(ItemPartData.instance(part));
            if (!stack.isEmpty()) items.add(stack);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        ItemPart source = ModularRodPart.getSource(stack);
        if (source == null) {
            tooltip.add(TextFormatting.RED + SilentGear.i18n.itemSubText("modular_rod", "noMaterial"));
        } else {
            String materialName = source.getDisplayName(ItemPartData.instance(source), ItemStack.EMPTY)
                    .getUnformattedComponentText();
            tooltip.add("" + source.getNameColor() + TextFormatting.ITALIC
                    + SilentGear.i18n.itemSubText("modular_rod", "material", materialName));
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
