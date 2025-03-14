package com.jomlom.workstationupgrades.screenhandler;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.jomlom.workstationupgrades.blockentity.AdvancedCraftingTableEntity;
import com.jomlom.workstationupgrades.init.ScreenHandlerTypeInit;
import com.jomlom.workstationupgrades.network.BlockPosPayload;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdvancedCraftingScreenHandler extends AbstractCraftingScreenHandler implements RecipeBookInventoryProvider {

    private final ScreenHandlerContext context;
    private final AdvancedCraftingTableEntity blockEntity;
    private boolean filling;
    private final PlayerEntity player;
    private boolean finishedCreating = false;

    // Client constructor
    public AdvancedCraftingScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (AdvancedCraftingTableEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    // Main constructor
    public AdvancedCraftingScreenHandler(int syncId, PlayerInventory playerInventory, AdvancedCraftingTableEntity blockEntity) {
        super(ScreenHandlerTypeInit.ADVANCED_CRAFTING_TABLE, syncId, 3, 3);
        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(this.blockEntity.getWorld(), this.blockEntity.getPos());
        this.player = playerInventory.player;

        this.addResultSlot(getPlayer(), 124, 35);
        this.addInputSlots(30, 17);
        for (int i = 0; i < 9; i++) {
            this.slots.get(i+1).setStack(blockEntity.getStack(i));
        }
        this.addPlayerSlots(playerInventory, 8, 84);
        finishedCreating = true;
        if (this.blockEntity.getWorld() instanceof ServerWorld serverWorld) {
            updateResult(this, serverWorld, getPlayer(), this.craftingInventory, this.craftingResultInventory, null);
        }
    }

    protected static void updateResult(AdvancedCraftingScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe) {
        CraftingRecipeInput craftingRecipeInput = craftingInventory.createRecipeInput();
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
        ItemStack resultStack = ItemStack.EMPTY;

        Optional<RecipeEntry<CraftingRecipe>> optionalRecipe = world.getServer().getRecipeManager()
                .getFirstMatch(RecipeType.CRAFTING, craftingRecipeInput, world, recipe);

        if (optionalRecipe.isPresent()) {
            RecipeEntry<CraftingRecipe> recipeEntry = optionalRecipe.get();
            CraftingRecipe craftingRecipe = recipeEntry.value();
            if (resultInventory.shouldCraftRecipe(serverPlayerEntity, recipeEntry)) {
                ItemStack craftedItem = craftingRecipe.craft(craftingRecipeInput, world.getRegistryManager());
                if (craftedItem.isItemEnabled(world.getEnabledFeatures())) {
                    resultStack = craftedItem;
                }
            }
        }
        resultInventory.setStack(0, resultStack);
        handler.setPreviousTrackedSlot(0, resultStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, resultStack));
    }

    @Override
    public void onContentChanged(net.minecraft.inventory.Inventory inventory) {
        if (!this.filling && this.finishedCreating) {
            this.context.run((world, pos) -> {
                if (world instanceof ServerWorld serverWorld) {
                    updateResult(this, serverWorld, getPlayer(), this.craftingInventory, this.craftingResultInventory, null);
                    for (int i = 0; i < 9; i++) {
                        blockEntity.setStack(i, this.slots.get(i+1).getStack());
                    }
                }
            });
        }
    }

    @Override
    public void onInputSlotFillStart() {
        this.filling = true;
    }

    @Override
    public void onInputSlotFillFinish(ServerWorld world, RecipeEntry<CraftingRecipe> recipe) {
        this.filling = false;
        updateResult(this, world, getPlayer(), this.craftingInventory, this.craftingResultInventory, recipe);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, WorkstationUpgrades.ModBlocks.ADVANCED_CRAFTING_TABLE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slot);
        if (clickedSlot != null && clickedSlot.hasStack()) {
            ItemStack itemStack2 = clickedSlot.getStack();
            itemStack = itemStack2.copy();
            if (slot == 0) { // Crafting result slot
                this.context.run((world, pos) -> itemStack2.getItem().onCraftByPlayer(itemStack2, world, player));
                if (!this.insertItem(itemStack2, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                clickedSlot.onQuickTransfer(itemStack2, itemStack);
            } else if (slot >= 10 && slot < 46) { // Player inventory
                if (!this.insertItem(itemStack2, 1, 10, false)) {
                    if (slot < 37) {
                        if (!this.insertItem(itemStack2, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(itemStack2, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(itemStack2, 10, 46, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            clickedSlot.onTakeItem(player, itemStack2);
            if (slot == 0) {
                player.dropItem(itemStack2, false);
            }
        }
        return itemStack;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.craftingResultInventory && super.canInsertIntoSlot(stack, slot);
    }

    public Slot getOutputSlot() {
        return this.slots.get(0);
    }

    public List<Slot> getInputSlots() {
        return this.slots.subList(1, 10);
    }

    public RecipeBookType getCategory() {
        return RecipeBookType.CRAFTING;
    }

    protected PlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public List<Inventory> getInventoriesForAutofill() {
    List<Inventory> inventories = new ArrayList<>();
        int reachRadius = 5;
        BlockPos origin = this.blockEntity.getPos();

        // Iterate over blocks in a cube around the origin.
        BlockPos.iterate(origin.add(-reachRadius, -reachRadius, -reachRadius),
                        origin.add(reachRadius, reachRadius, reachRadius))
                .forEach(pos -> {
                    if (!pos.equals(origin)) { // Exclude the block we're accessing from
                        BlockEntity nearbyBlockEntity = this.blockEntity.getWorld().getBlockEntity(pos);
                        if (nearbyBlockEntity instanceof Inventory inv) {
                            System.out.println("Detected nearby inventory at " + pos + ": " + inv);
                            // Debug: Log each slot's contents.
                            for (int i = 0; i < inv.size(); i++) {
                                System.out.println("Slot " + i + ": " + inv.getStack(i));
                            }
                            inventories.add(inv);
                        }
                    }
                });
        inventories.add(player.getInventory());
        return inventories;
    }
}