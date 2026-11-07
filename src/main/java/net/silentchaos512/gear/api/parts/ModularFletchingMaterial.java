package net.silentchaos512.gear.api.parts;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

/**
 * Fletching material built from {@code modular_materials/fletching_*.json}. Like the rod and tip
 * materials, instances are not registered in {@link PartRegistry}; they are looked up by id.
 */
public class ModularFletchingMaterial extends PartFletching {
    private final ResourceLocation materialId;
    private final String baseId;
    private float accuracy = 1.0f;

    public ModularFletchingMaterial(ResourceLocation materialId) {
        super(new ResourceLocation(materialId.getNamespace(), "modular_fletching_material_" + materialId.getPath()),
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

    /**
     * Projectile accuracy multiplier. 1.12 has no accuracy stat, so the value is read from the
     * optional {@code fletching} object and only used when the arrow is fired.
     */
    public float getAccuracy() {
        return accuracy;
    }

    @Override
    public void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        if (json.has("fletching") && json.get("fletching").isJsonObject()) {
            JsonObject fletching = json.getAsJsonObject("fletching");
            if (fletching.has("accuracy"))
                this.accuracy = fletching.get("accuracy").getAsFloat();
        }
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
