package com.mushroom.midnight.client.render;

import com.mushroom.midnight.Midnight;
import com.mushroom.midnight.client.model.ModelCrystalBug;
import com.mushroom.midnight.common.entity.creature.EntityCrystalBug;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderCrystalBug extends RenderLiving<EntityCrystalBug> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Midnight.MODID, "textures/entities/crystal_bug.png");

    public RenderCrystalBug(RenderManager rendermanager) {
        super(rendermanager, new ModelCrystalBug(), 0.2f);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityCrystalBug entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityCrystalBug entity, float partialTicks) {
        if (entity.isStanding()) {
            GlStateManager.translate(0f, 0.3f, 0f);
        } else {
            GlStateManager.translate(0f, 0.4f, 0f);
        }
        GlStateManager.scale(0.3f, 0.3f, 0.3f);
    }

    @Override
    protected float getDeathMaxRotation(EntityCrystalBug entity) {
        return 180f;
    }

    @Override
    public void setLightmap(EntityCrystalBug entityLivingIn) {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
    }
}
