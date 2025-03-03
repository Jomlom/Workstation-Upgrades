package com.jomlom.workstationupgrades.screenhandler;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.jomlom.workstationupgrades.blockentity.ReinforcedFurnaceEntity;
import com.jomlom.workstationupgrades.init.ScreenHandlerTypeInit;
import com.jomlom.workstationupgrades.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

public class ReinforcedFurnaceScreenHandler extends ScreenHandler {

    final Inventory inventory;
    private final ReinforcedFurnaceEntity blockEntity;
    private final ScreenHandlerContext context;

    // client constructor
    public ReinforcedFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (ReinforcedFurnaceEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    // main constructor - directly called from server
    public ReinforcedFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, ReinforcedFurnaceEntity blockEntity) {

        super(ScreenHandlerTypeInit.REINFORCED_FURNACE, syncId);

        this.blockEntity = blockEntity;
        this.inventory = blockEntity;
        this.context = ScreenHandlerContext.create(this.blockEntity.getWorld(), this.blockEntity.getPos());

        DefaultedList<ItemStack> inventory = this.blockEntity.getInventory();

        addBlockEntityInventory(inventory);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addPlayerHotbar(PlayerInventory playerInventory){
        for (int column = 0; column < 9; column++){
            addSlot(new Slot(playerInventory, column, 8 + (column * 18), 160));
        }
    }

    private void addPlayerInventory(PlayerInventory playerInventory){
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + (row * 9) + column, 8 + (column * 18), 102 + (row * 18)));
            }
        }
    }

    private void addBlockEntityInventory(DefaultedList<ItemStack> inventory){
        // Add input slot index 0
        addSlot(new Slot(this.inventory, 0, 52, 20) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return blockEntity.isValid(0, stack);
            }
        });
        // Add fuel slots indexes 1 & 2
        addSlot(new Slot(this.inventory, 1, 43, 57) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return blockEntity.isValid(1, stack);
            }
        });
        addSlot(new Slot(this.inventory, 2, 61, 57) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return blockEntity.isValid(2, stack);
            }
        });
        // Add output slot index 3
        addSlot(new Slot(this.inventory, 3, 112, 39) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return blockEntity.isValid(3, stack);
            }
        });
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        int blockEntityInvSize = this.blockEntity.getInventory().size();
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = getSlot(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack inSlot = slot.getStack();
            newStack = inSlot.copy();

            if (slotIndex < blockEntityInvSize) {
                if (!insertItem(inSlot, blockEntityInvSize, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(inSlot, 0, blockEntityInvSize - 1, false)) {
                return ItemStack.EMPTY;
            }

            if (inSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            }
        }

        blockEntity.markDirty();
        sendContentUpdates();

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, WorkstationUpgrades.ModBlocks.REINFORCED_FURNACE);
    }

    public ReinforcedFurnaceEntity getBlockEntity() {
        return this.blockEntity;
    }
}
