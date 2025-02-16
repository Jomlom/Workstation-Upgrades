package com.jomlom.workstationupgrades.inventory;

import com.jomlom.workstationupgrades.blockentity.ReinforcedFurnaceEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class ReinforcedFurnaceInventory extends SimpleInventory {

    ReinforcedFurnaceEntity blockEntity;

    public ReinforcedFurnaceInventory(ReinforcedFurnaceEntity blockEntity){
        super(4);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {

        if (slot == 0) {
            return canBeSmelted(stack);
        } else if (slot == 1 || slot == 2) {
            boolean isFuelValid = isFuel(stack);
            return isFuelValid;
        } else if (slot == 3) {
            return false;
        }

        return true;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        blockEntity.markDirty();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = super.removeStack(slot, amount);
        blockEntity.markDirty();
        return stack;
    }

    @Override
    public void onOpen(PlayerEntity player) {
        super.onOpen(player);
        blockEntity.markDirty();
    }

    @Override
    public void onClose(PlayerEntity player) {
        super.onClose(player);
        blockEntity.markDirty();
    }

    @Override
    public void clear() {
        super.clear();
        blockEntity.markDirty();
    }

    private boolean isFuel(ItemStack stack) {
        if (blockEntity == null || blockEntity.getWorld() == null || blockEntity.getWorld().isClient) {
            return true;
        }
        return blockEntity.getWorld().getFuelRegistry().isFuel(stack);
    }

    private boolean canBeSmelted(ItemStack stack) {

        return true;

    }

}
