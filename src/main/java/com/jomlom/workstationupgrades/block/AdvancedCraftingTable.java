package com.jomlom.workstationupgrades.block;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.jomlom.workstationupgrades.blockentity.AdvancedCraftingTableEntity;
import com.jomlom.workstationupgrades.blockentity.ReinforcedFurnaceEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AdvancedCraftingTable extends BlockWithEntity {

    public AdvancedCraftingTable(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return WorkstationUpgrades.ModBlocks.ADVANCED_CRAFTING_TABLE_ENTITY.instantiate(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec(){
        return createCodec(AdvancedCraftingTable::new);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof AdvancedCraftingTableEntity advancedCraftingTableEntity)) {
                return super.onUse(state, world, pos, player, hit);
            }
            else {
                // if player is sneaking pass so other action can happen e.g. eating or placing block
                if (player.isSneaking()){
                    return ActionResult.PASS;
                }
                player.openHandledScreen(advancedCraftingTableEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos,
                                   BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null) {
                // Optionally, drop items from your AdvancedCraftingTableEntity's inventory.
                // For a crafting table, vanilla doesn't drop anything, but you might:
                // ItemScatterer.spawn(world, pos, ((AdvancedCraftingTableEntity) blockEntity).getInventory());
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
