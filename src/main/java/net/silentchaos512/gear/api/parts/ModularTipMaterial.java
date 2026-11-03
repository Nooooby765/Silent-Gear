package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

/**
 * 由 modular_materials JSON 构建的 tip 材料载体。注册名刻意带前缀，避免与 legacy tip 部件
 * 共用 {@link ItemPartData} 的未分级缓存；实际 NBT 材料 key 与显示名使用 {@link #getMaterialId()}。
 * 该实例不进入 {@link PartRegistry}，故不会占用 item+meta 单映射。
 */
public class ModularTipMaterial extends PartTip {
    private final ResourceLocation materialId;

    public ModularTipMaterial(ResourceLocation materialId) {
        super(new ResourceLocation(materialId.getNamespace(), "modular_tip_material_" + materialId.getPath()),
                PartOrigins.USER_DEFINED, false);
        this.materialId = materialId;
    }

    public ResourceLocation getMaterialId() {
        return materialId;
    }

    @Override
    public String getTranslationKey(@Nullable ItemPartData part) {
        return ModularMaterialNames.genericKey(materialId);
    }

    @Override
    public ITextComponent getDisplayName(@Nullable ItemPartData part, ItemStack gear) {
        return new TextComponentString(ModularMaterialNames.resolve(materialId, getCraftingStack(), gear));
    }

    @Override
    public String getTranslatedName(@Nullable ItemPartData part, ItemStack gear) {
        return ModularMaterialNames.resolve(materialId, getCraftingStack(), gear);
    }
}
