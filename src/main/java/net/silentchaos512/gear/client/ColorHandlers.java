package net.silentchaos512.gear.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.item.gear.CoreArrow;
import net.silentchaos512.gear.item.gear.CoreFishingRod;
import net.silentchaos512.gear.item.gear.CoreShield;
import net.silentchaos512.gear.util.GearData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ColorHandlers {

    public static Map<String, Integer[]> gearColorCache = new HashMap<>();

    public static void init() {
        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();

        List<Item> gearItems = new ArrayList<>();
        gearItems.addAll(ModItems.toolClasses.values().stream().map(ICoreItem::getItem).collect(Collectors.toList()));
        gearItems.addAll(ModItems.armorClasses.values().stream().map(ICoreItem::getItem).collect(Collectors.toList()));

        // Tools/Armor - mostly used for broken color, but colors could be changed at any time
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (!(stack.getItem() instanceof ICoreItem) || tintIndex < 0)
                return 0xFFFFFF;

            String modelKey = GearData.getCachedModelKey(stack, 0);
            Integer[] colors = gearColorCache.get(modelKey);
            return colors != null && tintIndex < colors.length ? colors[tintIndex] : 0xFFFFFF;
        }, gearItems.toArray(new Item[0]));

        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            String modelKey = ModItems.toolHead.getModelKey(stack);
            Integer[] colors = gearColorCache.get(modelKey);
            return colors != null && tintIndex >= 0 && tintIndex < colors.length ? colors[tintIndex] : 0xFFFFFF;
        }, ModItems.toolHead);

        // Modular shield: 0 = rod/frame, 1 = main plate. Colors are written at construction, so
        // rendering only reads an int array. Single "Color" is the pre-Colors fallback.
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex < 0 || tintIndex > 1) return 0xFFFFFF;
            NBTTagCompound tag = stack.getSubCompound(CoreShield.NBT_ROOT);
            if (tag == null) return 0xFFFFFF;
            if (tag.hasKey(CoreShield.NBT_COLORS)) {
                int[] colors = tag.getIntArray(CoreShield.NBT_COLORS);
                return tintIndex < colors.length ? colors[tintIndex] : 0xFFFFFF;
            }
            return tintIndex == 1 && tag.hasKey(CoreShield.NBT_COLOR) ? tag.getInteger(CoreShield.NBT_COLOR) : 0xFFFFFF;
        }, ModItems.shield);

        // Modular fishing rod: only layers 0 (rod) and 1 (main) are tinted; the highlight and
        // string layers stay white.
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex < 0 || tintIndex > 1) return 0xFFFFFF;
            int[] colors = CoreFishingRod.getColors(stack);
            return tintIndex < colors.length ? colors[tintIndex] : 0xFFFFFF;
        }, ModItems.fishingRod);

        // Modular arrow: layer 2 is the white highlight; layer 3 (fletching) uses array index 2.
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex < 0) return 0xFFFFFF;
            if (tintIndex == 2) return 0xFFFFFF;
            int index = tintIndex >= 3 ? 2 : tintIndex;
            NBTTagCompound tag = stack.getSubCompound(CoreArrow.NBT_ROOT);
            if (tag == null || !tag.hasKey(CoreArrow.NBT_COLORS)) return 0xFFFFFF;
            int[] colors = tag.getIntArray(CoreArrow.NBT_COLORS);
            return index < colors.length ? colors[index] : 0xFFFFFF;
        }, ModItems.arrow);
    }
}
