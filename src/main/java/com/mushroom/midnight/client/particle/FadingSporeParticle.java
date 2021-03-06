package com.mushroom.midnight.client.particle;

import com.mushroom.midnight.common.helper.Helper;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class FadingSporeParticle extends Particle {
    private final float scaleMax;

    public FadingSporeParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.motionX = motionX;
        this.motionY = motionY + 0.01d;
        this.motionZ = motionZ;
        float[] rgbF = Helper.getRGBColorF(color);
        setRBGColorF(MathHelper.clamp(rgbF[0] + (this.rand.nextFloat() * 0.2f - 0.1f), 0f, 1f), MathHelper.clamp(rgbF[1] + (this.rand.nextFloat() * 0.2f - 0.1f), 0f, 1f), MathHelper.clamp(rgbF[2] + (this.rand.nextFloat() * 0.2f - 0.1f), 0f, 1f));
        this.particleAlpha = 1f;
        this.particleScale = 0f;
        this.scaleMax = world.rand.nextFloat() * 0.5f + 0.5f;
        this.particleMaxAge = (int) (8d / (Math.random() * 0.8d + 0.2d)) + 4;
        this.canCollide = false;
        this.particleGravity = 0f;
        setParticleTexture(MidnightParticleSprites.getSprite(MidnightParticleSprites.SpriteTypes.FADING_SPORE));
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (++this.particleAge >= this.particleMaxAge) {
            setExpired();
        } else {
            move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9d;
            this.motionZ *= 0.9d;
            float ratio = this.particleAge / (float) this.particleMaxAge;
            if (ratio <= 0.25f) {
                this.particleScale = this.scaleMax * ratio * 4f;
            } else {
                float ratio2 = ratio / 0.75f;
                this.particleAlpha = ratio2;
                this.particleScale = this.scaleMax * (1f - ratio2);
            }
        }
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        int skylight = 10;
        int blocklight = 5;
        return skylight << 20 | blocklight << 4;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {
        @Override
        @Nullable
        public Particle createParticle(int particleID, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int... params) {
            return new FadingSporeParticle(world, x, y, z, motionX, motionY, motionZ, params.length > 0 ? params[0] : 0xffffff);
        }
    }
}
