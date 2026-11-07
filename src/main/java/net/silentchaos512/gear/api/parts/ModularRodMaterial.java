package net.silentchaos512.gear.api.parts;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

/**
 * 由 modular_materials JSON 构建的杆材料载体。与 tip/coating 材料相同，该实例不注册进
 * {@link PartRegistry}，只在材料表中按 id 查询；显示名优先复用对应 {@link PartMain}。
 */
public class ModularRodMaterial extends PartRod {
    private final ResourceLocation materialId;
    private final String baseId;

    public ModularRodMaterial(ResourceLocation materialId) {
        super(new ResourceLocation(materialId.getNamespace(), "modular_rod_material_" + materialId.getPath()),
                PartOrigins.USER_DEFINED, false);
        this.materialId = materialId;
        this.baseId = ModularMaterialNames.baseId(materialId);
    }

    public ResourceLocation getMaterialId() {
        return materialId;
    }

    public String getBaseId() {
        return baseId;
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
