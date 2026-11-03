package net.silentchaos512.gear.api.parts;

import net.minecraft.util.ResourceLocation;

/**
 * 涂层部件。涂层不参与工具模型渲染，只作为独立升级部件提供属性/traits。
 */
public class PartCoating extends PartUpgrade {
    public PartCoating(ResourceLocation registryName, PartOrigins origin) {
        super(registryName, origin);
    }

    protected PartCoating(ResourceLocation registryName, PartOrigins origin, boolean loadResources) {
        super(registryName, origin, loadResources);
    }

    @Override
    public PartType getType() {
        return PartType.COATING;
    }

    @Override
    public IPartPosition getPartPosition() {
        return PartPositions.COATING;
    }

    @Override
    public boolean replacesExisting() {
        return true;
    }

    @Override
    public String getTypeName() {
        return "coating";
    }

    @Override
    public boolean requiresCraftingItem() {
        // legacy coating 只由模块化涂层间接引用，不存在可合成物品
        return false;
    }
}
