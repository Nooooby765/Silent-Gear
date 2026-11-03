package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.gear.SilentGear;

/**
 * Resolves the localized name shown for modular tip/coating materials. Names are no longer
 * maintained per material file ({@code material.silentgear.tip_*}). Instead, the matching
 * {@link PartMain} is used when it exists (iron/gold/diamond/emerald), otherwise the generic
 * {@code material.<namespace>.<base id>.name} key, then the vanilla item name.
 */
final class ModularMaterialNames {
    private static final String TIP_PREFIX = "tip_";
    private static final String COATING_PREFIX = "coating_";

    private ModularMaterialNames() {
    }

    static String baseId(ResourceLocation materialId) {
        String path = materialId.getPath();
        if (path.startsWith(TIP_PREFIX)) return path.substring(TIP_PREFIX.length());
        if (path.startsWith(COATING_PREFIX)) return path.substring(COATING_PREFIX.length());
        return path;
    }

    static String genericKey(ResourceLocation materialId) {
        return genericKey(materialId.getNamespace(), baseId(materialId));
    }

    static String genericKey(String namespace, String base) {
        return "material." + namespace + "." + base + ".name";
    }

    static String resolve(ResourceLocation materialId, ItemStack craftingStack, ItemStack gear) {
        String base = baseId(materialId);
        // 1. 同类 PartMain（main_iron/main_gold/main_diamond/main_emerald 等）。
        ItemPart main = PartRegistry.get(new ResourceLocation(materialId.getNamespace(), "main_" + base));
        if (main instanceof PartMain)
            return main.getDisplayName(ItemPartData.instance(main), gear).getUnformattedComponentText();

        // 2. 自定义材料可能由任意物品映射到 PartMain。
        main = PartRegistry.get(craftingStack);
        if (main instanceof PartMain)
            return main.getDisplayName(ItemPartData.instance(main), gear).getUnformattedComponentText();

        // 3. 无主材料的 1.12 物品（红石/荧石/青金石/石英）走通用键。
        String key = genericKey(materialId.getNamespace(), base);
        String translated = SilentGear.i18n.translate(key);
        if (!key.equals(translated)) return translated;

        // 4. 原版物品名，最后兜底 base id。
        if (!craftingStack.isEmpty()) return craftingStack.getDisplayName();
        return base;
    }
}
