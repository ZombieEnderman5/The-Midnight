package com.mushroom.midnight.client.render;

import com.mushroom.midnight.Midnight;
import com.mushroom.midnight.client.model.ModelStinger;
import com.mushroom.midnight.common.entity.creature.EntityStinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderStinger extends RenderLiving<EntityStinger> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Midnight.MODID, "textures/entities/stinger.png");

    public RenderStinger(RenderManager rendermanager) {
        super(rendermanager, new ModelStinger(), 0.15f);
    }

    @Override
    public void doRender(EntityStinger entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityStinger entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityStinger entity, float partialTicks) {
        float scale = 0.3f + (entity.getGrowingAge() * 0.1f);
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected float getDeathMaxRotation(EntityStinger entity) {
        return 180f;
    }
}
