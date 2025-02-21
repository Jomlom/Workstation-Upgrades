package com.jomlom.workstationupgrades.block;

import com.jomlom.workstationupgrades.blockentity.ReinforcedFurnaceEntity;
import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;


public class ReinforcedFurnace extends BlockWithEntity {

    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public ReinforcedFurnace(Settings settings){
        super(settings.luminance(state -> state.get(LIT) ? 15 : 0));
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()) // Fix method name
                .with(LIT, false);
    }


    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec(){
        return createCodec(ReinforcedFurnace::new);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return WorkstationUpgrades.ModBlocks.REINFORCED_FURNACE_ENTITY.instantiate(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
        if (!world.isClient){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof ReinforcedFurnaceEntity reinforcedFurnaceEntity)) {
                return super.onUse(state, world, pos, player, hit);
            }
            else {
                // if player is sneaking pass so other action can happen e.g. eating or placing block
                if (player.isSneaking()){
                    return ActionResult.PASS;
                }
                // functionality here
                player.openHandledScreen(reinforcedFurnaceEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ReinforcedFurnaceEntity){
                ItemScatterer.spawn(world, pos, ((ReinforcedFurnaceEntity)blockEntity).getInventory());
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    // make the block tick
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, WorkstationUpgrades.ModBlocks.REINFORCED_FURNACE_ENTITY, ReinforcedFurnaceEntity::tick);
    }
}
