package net.silentchaos512.gear.item.blueprint;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.PartDataList;
import net.silentchaos512.gear.api.parts.PartMain;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.lib.item.IColoredItem;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.lib.util.I18nHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A lightweight blueprint/template for standalone parts (tips, rods) that do not have a gear item.
 * Material restriction is handled through {@link IBlueprint#isValidMaterial}.
 */
public class PartBlueprint extends Item implements IBlueprint, IColoredItem {
    private static final int COLOR_OUTLINE_BLUEPRINT = 0xF1F1B9;
    private static final int COLOR_OUTLINE_TEMPLATE = 0xBAF2E2;

    private final boolean singleUse;
    private final PartType partType;
    private final int materialCost;
    private final Predicate<ItemPart> materialFilter;
    private final Function<PartDataList, ItemStack> craftingHandler;
    private final Function<ItemStack, ItemPartData> materialResolver;

    public PartBlueprint(boolean singleUse, PartType partType, int materialCost, Function<PartDataList, ItemStack> craftingHandler) {
        this(singleUse, partType, materialCost, part -> part instanceof PartMain, craftingHandler, ItemPartData::fromStack);
    }

    public PartBlueprint(boolean singleUse, PartType partType, int materialCost,
                         Predicate<ItemPart> materialFilter, Function<PartDataList, ItemStack> craftingHandler) {
        this(singleUse, partType, materialCost, materialFilter, craftingHandler, ItemPartData::fromStack);
    }

    public PartBlueprint(boolean singleUse, PartType partType, int materialCost,
                         Predicate<ItemPart> materialFilter, Function<PartDataList, ItemStack> craftingHandler,
                         Function<ItemStack, ItemPartData> materialResolver) {
        this.singleUse = singleUse;
        this.partType = partType;
        this.materialCost = materialCost;
        this.materialFilter = materialFilter;
        this.craftingHandler = craftingHandler;
        this.materialResolver = materialResolver;
        if (!singleUse)
            setContainerItem(this);
    }

    @Override
    public ItemStack getCraftingResult(ItemStack blueprint, Collection<ItemStack> parts) {
        if (isDisabled())
            return ItemStack.EMPTY;

        PartDataList list = PartDataList.of();
        for (ItemStack stack : parts) {
            ItemPartData data = getMaterial(stack);
            if (data != null)
                list.add(data);
        }
        if (list.size() != materialCost)
            return ItemStack.EMPTY;

        for (ItemPartData part : list) {
            if (part.getPart().isBlacklisted()
                    || !isPartTypeAllowed(part.getPart())
                    || !isValidMaterial(part.getPart()))
                return ItemStack.EMPTY;
        }

        return craftingHandler.apply(list);
    }

    /**
     * Hard gate for the blueprint's declared part type. TIP blueprints additionally accept MAIN
     * as a fallback; COATING blueprints accept MAIN only when the material filter confirms a
     * legacy coating mapping. This prevents tip materials from being used as coatings and vice
     * versa.
     */
    private boolean isPartTypeAllowed(ItemPart part) {
        PartType type = part.getType();
        if (type == partType) return true;
        if (partType == PartType.TIP) return type == PartType.MAIN;
        return type == PartType.MAIN && materialFilter.test(part);
    }

    @Override
    @Nullable
    public ItemPartData getMaterial(ItemStack stack) {
        return materialResolver.apply(stack);
    }

    @Override
    public int getMaterialCost(ItemStack blueprint) {
        return materialCost;
    }

    @Override
    public boolean isSingleUse(ItemStack blueprint) {
        return singleUse;
    }

    @Override
    public boolean isValidMaterial(ItemPart part) {
        return isPartTypeAllowed(part) && materialFilter.test(part);
    }

    public boolean isDisabled() {
        return singleUse && !Config.blueprintTypes.allowTemplate()
                || !singleUse && !Config.blueprintTypes.allowBlueprint();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        I18nHelper i18n = SilentGear.i18n;
        list.add(i18n.itemSubText("part_blueprint", "materialAmount", materialCost));

        if (isDisabled()) {
            list.add(TextFormatting.DARK_RED + i18n.itemSubText("part_blueprint", "disabled"));
        } else if (singleUse) {
            list.add(TextFormatting.RED + i18n.itemSubText("part_blueprint", "singleUse"));
        } else {
            list.add(TextFormatting.GREEN + i18n.itemSubText("part_blueprint", "multiUse"));
        }

        list.add(TextFormatting.ITALIC + i18n.itemSubText("part_blueprint", "desc." + partType.getName()));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isDisabled())
            super.getSubItems(tab, items);
    }

    @Override
    public IItemColor getColorHandler() {
        return (stack, tintIndex) -> tintIndex == 0 ? Color.VALUE_WHITE
                : singleUse ? COLOR_OUTLINE_TEMPLATE : COLOR_OUTLINE_BLUEPRINT;
    }
}
