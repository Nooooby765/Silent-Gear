package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

/**
 * 由 modular_materials JSON 构建的 coating 材料载体。不进入 {@link PartRegistry}，注册名带前缀
 * 以避免与 legacy {@code coating_gold} 共用 {@link ItemPartData} 缓存。
 */
public class ModularCoatingMaterial extends PartCoating {
    private final ResourceLocation materialId;

    public ModularCoatingMaterial(ResourceLocation materialId) {
        super(new ResourceLocation(materialId.getNamespace(), "modular_coating_material_" + materialId.getPath()),
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
