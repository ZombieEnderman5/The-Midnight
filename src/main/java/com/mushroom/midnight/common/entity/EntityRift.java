package com.mushroom.midnight.common.entity;

import com.mushroom.midnight.Midnight;
import com.mushroom.midnight.client.particle.RiftParticleSystem;
import com.mushroom.midnight.common.capability.RiftCooldownCapability;
import com.mushroom.midnight.common.entity.creature.EntityRifter;
import com.mushroom.midnight.common.registry.ModDimensions;
import com.mushroom.midnight.common.world.MidnightTeleporter;
import com.mushroom.midnight.common.world.BridgeManager;
import com.mushroom.midnight.common.world.GlobalBridgeManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EntityRift extends Entity implements IEntityAdditionalSpawnData {
    public static final int OPEN_TIME = 20;
    public static final int CLOSE_SPEED = 2;
    public static final int CLOSE_TIME = OPEN_TIME / CLOSE_SPEED;

    public static final int UNSTABLE_TIME = 100;

    public static final int LIFETIME = 2400;

    public static final float PULL_RADIUS = 8.0F;
    public static final float PULL_INTENSITY = 5.0F;

    public static final double MAX_PULL_VELOCITY = 1.2;

    private RiftGeometry geometry;

    private RiftBridge bridge;

    private RiftParticleSystem particleSystem;

    private boolean spawnedRifter;

    private boolean invalid;

    public EntityRift(World world) {
        super(world);
        this.setSize(1.8F, 4.0F);
        this.noClip = true;
        this.initGeometry(new Random().nextLong());
        if (world.isRemote) {
            this.particleSystem = new RiftParticleSystem(this);
        }
    }

    private void initGeometry(long seed) {
        this.geometry = new RiftGeometry(seed, this.width, this.height);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        if (this.invalid) {
            this.setDead();
            return;
        }

        RiftBridge bridge = this.getBridge();

        super.onUpdate();

        if (!this.world.isRemote) {
            if (!bridge.exists) {
                this.setDead();
                return;
            }

            if (this.world.provider.getDimensionType() != ModDimensions.MIDNIGHT) {
                this.updateOverworldBehavior();
            }

            if (bridge.open.getTimer() >= OPEN_TIME && !this.wasUsed()) {
                this.teleportEntities();
            }
        } else if (this.particleSystem != null) {
            this.particleSystem.updateParticles(this.world.rand);
        }
    }

    public void acceptBridge(RiftBridge riftBridge) {
        this.bridge = riftBridge;
        this.bridge.accept(this);
    }

    private void updateOverworldBehavior() {
        if (this.world.isDaytime()) {
            this.getBridge().unstable.set(true);
        }

        if (this.isUnstable() && !this.wasUsed()) {
            this.pullEntities();
        }

        if (this.world.getGameRules().getBoolean("doMobSpawning")) {
            if (!this.spawnedRifter && !this.isUnstable() && this.world.rand.nextInt(40) == 0) {
                AxisAlignedBB existingRifterBounds = this.getEntityBoundingBox().grow(16.0);
                List<EntityRifter> existingRifters = this.world.getEntitiesWithinAABB(EntityRifter.class, existingRifterBounds);
                if (existingRifters.isEmpty()) {
                    this.trySpawnRifter();
                }
            }
        }
    }

    private void trySpawnRifter() {
        for (int attempts = 0; attempts < 4; attempts++) {
            float theta = (float) (this.world.rand.nextFloat() * Math.PI * 2.0F);
            float offsetX = -MathHelper.sin(theta) * this.width * 0.9F;
            float offsetZ = MathHelper.cos(theta) * this.width * 0.9F;

            EntityRifter rifter = new EntityRifter(this.world);
            rifter.setPositionAndRotation(this.posX + offsetX, this.posY, this.posZ + offsetZ, (float) Math.toDegrees(theta), 0.0F);

            if (rifter.isNotColliding()) {
                this.world.spawnEntity(rifter);
                this.spawnedRifter = true;
                return;
            }
        }
    }

    private void pullEntities() {
        double pullIntensity = this.getPullIntensity();

        AxisAlignedBB pullBounds = this.getEntityBoundingBox().grow(PULL_RADIUS);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, pullBounds);
        for (Entity entity : entities) {
            if (!(entity instanceof EntityRift)) {
                this.pullEntity(pullIntensity, entity);
            }
        }
    }

    public void pullEntity(double pullIntensity, Entity entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying) {
            return;
        }

        double deltaX = this.posX - entity.posX;
        double deltaY = (this.posY + this.height / 2.0F) - (entity.posY + entity.height / 2.0F);
        double deltaZ = this.posZ - entity.posZ;

        double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

        if (distanceSq > 0.0) {
            double distance = Math.sqrt(distanceSq);
            double intensity = MathHelper.clamp(pullIntensity / distanceSq, 0.0F, 1.0F);

            double velocityX = entity.motionX + (deltaX / distance) * intensity;
            double velocityY = entity.motionY + (deltaY / distance) * intensity;
            double velocityZ = entity.motionZ + (deltaZ / distance) * intensity;

            Vector3d velocity = new Vector3d(velocityX, velocityY, velocityZ);
            if (velocity.lengthSquared() > MAX_PULL_VELOCITY * MAX_PULL_VELOCITY) {
                velocity.normalize();
                velocity.scale(MAX_PULL_VELOCITY);
            }

            entity.motionX = velocity.x;
            entity.motionY = velocity.y;
            entity.motionZ = velocity.z;

            entity.fallDistance = 0.0F;
        }
    }

    public double getPullIntensity() {
        if (this.wasUsed()) {
            return 0.0;
        }
        return Math.pow((double) this.getBridge().unstable.getTimer() / UNSTABLE_TIME, 5.0) * PULL_INTENSITY;
    }

    private void teleportEntities() {
        AxisAlignedBB bounds = this.getEntityBoundingBox().grow(-0.4);
        DimensionType endpointDimension = this.getEndpointDimension();

        List<Entity> entities = this.world.getEntitiesInAABBexcluding(this, bounds, entity -> {
            if (entity.isRiding()) {
                return false;
            }
            RiftCooldownCapability cooldown = entity.getCapability(Midnight.riftCooldownCap, null);
            if (cooldown != null && !cooldown.isReady()) {
                return false;
            }
            return !(entity instanceof EntityRift);
        });

        Set<Entity> recursedEntities = new HashSet<>();
        for (Entity entity : entities) {
            recursedEntities.add(entity);
            recursedEntities.addAll(entity.getRecursivePassengers());
            if (entity instanceof IRiftTraveler) {
                recursedEntities.addAll(((IRiftTraveler) entity).getAdditionalTeleportEntities());
            }
        }

        for (Entity entity : recursedEntities) {
            if (entity instanceof IRiftTraveler) {
                ((IRiftTraveler) entity).onEnterRift(this);
            }
            entity.dismountRidingEntity();
            entity.changeDimension(endpointDimension.getId(), new MidnightTeleporter(this));
        }
    }

    public boolean isOpen() {
        return this.getBridge().open.get();
    }

    public boolean isUnstable() {
        return this.getBridge().unstable.get();
    }

    public boolean wasUsed() {
        return this.getBridge().used;
    }

    public RiftGeometry getGeometry() {
        return this.geometry;
    }

    @Nullable
    public RiftParticleSystem getParticleSystem() {
        return this.particleSystem;
    }

    @Nonnull
    public RiftBridge getBridge() {
        if (!this.world.isRemote && this.bridge == null) {
            this.bridge = this.createBridge();
        }
        return this.bridge;
    }

    private RiftBridge createBridge() {
        BridgeManager trackerHandler = GlobalBridgeManager.getServer();

        RiftAttachment attachment = new RiftAttachment(this.getPosition(), this.rotationYaw);
        RiftBridge bridge = trackerHandler.createBridge(attachment);
        bridge.accept(this);
        trackerHandler.addBridge(bridge);

        return bridge;
    }

    public DimensionType getEndpointDimension() {
        if (this.world.provider.getDimensionType() == ModDimensions.MIDNIGHT) {
            return DimensionType.OVERWORLD;
        } else {
            return ModDimensions.MIDNIGHT;
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setLong("geometry_seed", this.geometry.getSeed());
        compound.setBoolean("spawned_rifter", this.spawnedRifter);
        if (this.bridge != null) {
            compound.setInteger("bridge_id", this.bridge.getId());
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.initGeometry(compound.getLong("geometry_seed"));
        this.spawnedRifter = compound.getBoolean("spawned_rifter");
        if (compound.hasKey("bridge_id")) {
            this.invalid = this.initBridge(compound.getInteger("bridge_id"));
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(this.invalid);

        if (!this.invalid) {
            buffer.writeLong(this.geometry.getSeed());

            RiftBridge bridge = this.getBridge();
            buffer.writeInt(bridge.getId());
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.invalid = buffer.readBoolean();
        if (!this.invalid) {
            this.initGeometry(buffer.readLong());
            this.initBridge(buffer.readInt());
        }
    }

    private boolean initBridge(int bridgeId) {
        BridgeManager tracker = GlobalBridgeManager.get(this.world.isRemote);

        RiftBridge bridge = tracker.getBridge(bridgeId);
        if (bridge == null) {
            if (this.world.isRemote) {
                bridge = new RiftBridge(bridgeId, new RiftAttachment(this.getPosition(), this.rotationYaw));
                GlobalBridgeManager.getClient().addBridge(bridge);
            } else {
                return true;
            }
        }

        this.acceptBridge(bridge);

        return false;
    }
}
