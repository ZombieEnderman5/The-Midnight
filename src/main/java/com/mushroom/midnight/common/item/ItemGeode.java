package com.mushroom.midnight.common.item;

import com.mushroom.midnight.client.IModelProvider;
import com.mushroom.midnight.common.entity.projectile.EntityThrownGeode;
import com.mushroom.midnight.common.registry.ModTabs;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemGeode extends Item implements IModelProvider {
    public ItemGeode() {
        super();
        setCreativeTab(ModTabs.MIDNIGHT_ITEMS);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new ItemGeode.DispenserBehavior());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!player.capabilities.isCreativeMode) {
            heldItem.shrink(1);
        }

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            EntityThrownGeode geode = new EntityThrownGeode(world, player);
            geode.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(geode);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
    }

    private static class DispenserBehavior extends BehaviorProjectileDispense {
        @Override
        protected IProjectile getProjectileEntity(World world, IPosition pos, ItemStack stack) {
            return new EntityThrownGeode(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
