package net.silentchaos512.gear.client;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.entity.EntitySilentArrow;

public class RenderSilentArrow extends RenderArrow<EntitySilentArrow> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SilentGear.MOD_ID, "textures/entity/arrow.png");

    public RenderSilentArrow(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySilentArrow entity) {
        return TEXTURE;
    }
}
