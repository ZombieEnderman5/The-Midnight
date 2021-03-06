package com.mushroom.midnight.common.block;

import com.mushroom.midnight.client.IModelProvider;
import com.mushroom.midnight.common.registry.ModTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockMidnightOre extends Block implements IModelProvider {
    public BlockMidnightOre(int harvestLevel) {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(ModTabs.BUILDING_TAB);
        this.setHarvestLevel("pickaxe", harvestLevel);
    }
}
