package net.silentchaos512.gear.api.parts;

import net.minecraft.util.ResourceLocation;

/**
 * 护甲衬里部件。衬里不参与护甲模型渲染，只作为可选部件提供属性/traits。
 */
public class PartLining extends PartUpgrade {
    public PartLining(ResourceLocation registryName, PartOrigins origin) {
        super(registryName, origin);
    }

    protected PartLining(ResourceLocation registryName, PartOrigins origin, boolean loadResources) {
        super(registryName, origin, loadResources);
    }

    @Override
    public PartType getType() {
        return PartType.LINING;
    }

    @Override
    public IPartPosition getPartPosition() {
        return PartPositions.LINING;
    }

    @Override
    public boolean replacesExisting() {
        return true;
    }

    @Override
    public String getTypeName() {
        return "lining";
    }

    @Override
    public boolean requiresCraftingItem() {
        // 数据驱动衬里材料只由模块化衬里间接引用，不存在可合成物品
        return false;
    }
}
