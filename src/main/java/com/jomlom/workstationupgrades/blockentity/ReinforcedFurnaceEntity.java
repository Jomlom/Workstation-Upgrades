package com.jomlom.workstationupgrades.blockentity;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.jomlom.workstationupgrades.block.ReinforcedFurnace;
import com.jomlom.workstationupgrades.inventory.ReinforcedFurnaceInventory;
import com.jomlom.workstationupgrades.network.BlockPosPayload;
import com.jomlom.workstationupgrades.screenhandler.ReinforcedFurnaceScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ReinforcedFurnaceEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPosPayload> {

    public static final Text TITLE = Text.translatable("container." + WorkstationUpgrades.MOD_ID + ".reinforced_furnace");

    private int totalSmeltingTime = 20*3;;
    private int smeltingTime = totalSmeltingTime;  // The current time for smelting.
    private int currentFuelTime = 0;  // Time left for the current fuel.
    private int totalFuelTime;  // Total time for the two fuel items.
    private boolean isSmelting;  // Whether the furnace is currently smelting.

    private final ReinforcedFurnaceInventory inventory = new ReinforcedFurnaceInventory(this);

    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);

    public ReinforcedFurnaceEntity (BlockPos pos, BlockState state){

        super(WorkstationUpgrades.ModBlocks.REINFORCED_FURNACE_ENTITY, pos, state);

    }

    public static void tick(World world, BlockPos pos, BlockState state, ReinforcedFurnaceEntity entity) {
        // client only code
        if (world.isClient) {
            spawnFrontParticles(world, pos, state);
            spawnBackParticles(world, pos, state);
        }
        // server only code
        else {

            spawnSounds(world, pos, state);

            // Check if the furnace should be lit based on fuel time or smelting state
            boolean wasLit = state.get(ReinforcedFurnace.LIT);
            boolean shouldBeLit = (entity.currentFuelTime > 0) || entity.isSmelting;

            if (wasLit != shouldBeLit) {
                world.setBlockState(pos, state.with(ReinforcedFurnace.LIT, shouldBeLit), Block.NOTIFY_ALL);
            }

            if (entity.currentFuelTime > 0) {
                entity.currentFuelTime--;
            }

            ItemStack input = entity.getInventory().getStack(0);  // get the input stack

            // check if the furnace is smelting
            if (entity.isSmelting) {
                entity.tickSmelting(input);
            } else {
                if (entity.readyToSmelt(input)) {
                    if (!(entity.currentFuelTime > 0)) {
                        entity.consumeFuel();
                    }
                    entity.startSmelting();
                }
            }

            entity.sync();
        }
    }

    private static void spawnFrontParticles(World world, BlockPos pos, BlockState state) {
        if (!state.get(ReinforcedFurnace.LIT)) {
            return; // No particles if not lit
        }

        if (world.random.nextFloat() < 0.2f) {
            // Furnace direction (facing)
            Direction direction = state.get(ReinforcedFurnace.FACING);

            // Determine the coordinates for the front face based on furnace direction
            double offsetX = 0.5 + (Math.random() - 0.5) * 0.6; // Random X offset (-0.3 to 0.3)
            double offsetY = 0.15 + (Math.random() - 0.5) * 0.25; // Random Y offset (-0.45 to -0.25)
            double offsetZ = 0.5 + (Math.random() - 0.5) * 0.6; // Random Z offset (-0.3 to 0.3)

            // Adjust the position based on the direction the furnace is facing
            switch (direction) {
                case NORTH: offsetZ = -0.05; break;
                case EAST: offsetX = 1.05; break;
                case SOUTH: offsetZ = 1.05; break;
                case WEST: offsetX = -0.05; break;
                default: break;
            }

            // Spawn the fire particle (no velocity)
            world.addParticle(ParticleTypes.FLAME, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 0.0, 0.0, 0.0);

            // Spawn the smoke particle (with upward velocity)
            world.addParticle(ParticleTypes.SMOKE, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 0.0, 0.015, 0.0);
        }

    }

    private static void spawnBackParticles(World world, BlockPos pos, BlockState state) {
        if (!state.get(ReinforcedFurnace.LIT)) {
            return; // No particles if not lit
        }

        if (world.random.nextFloat() < 0.75f) {
            // Furnace direction (facing)
            Direction direction = state.get(ReinforcedFurnace.FACING);

            // Determine the coordinates for the front face based on furnace direction
            double offsetX = 0.5 + (Math.random() - 0.5) * 0.4; // Random X offset
            double offsetY = 0.85 + (Math.random() - 0.5) * 0.2; // Random Y offset
            double offsetZ = 0.5 + (Math.random() - 0.5) * 0.4; // Random Z offset

            // Adjust the position based on the direction the furnace is facing
            switch (direction) {
                case NORTH: offsetZ = 1.05; offsetX+= 0.2; break;
                case EAST: offsetX = -0.05; offsetZ+= 0.2; break;
                case SOUTH: offsetZ = -0.05; offsetX-= 0.2;break;
                case WEST: offsetX = 1.05; offsetZ-= 0.2; break;
                default: break;
            }

            if (world.random.nextFloat() < 0.2f) {
                // Spawn the fire particle (no velocity)
                world.addParticle(ParticleTypes.FLAME, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 0.0, 0.0, 0.0);
            }

            // Spawn the smoke particle (with upward velocity)
            world.addParticle(ParticleTypes.SMOKE, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 0.0, 0.025, 0.0);
        }
    }

    private static void spawnSounds (World world, BlockPos pos, BlockState state) {
        if (!state.get(ReinforcedFurnace.LIT)) {
            return; // No sounds if not lit
        }

        if (world.random.nextFloat() < 0.03f) {
            world.playSound(null, pos, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.6F, 0.6F);
        }
    }


    private boolean readyToSmelt(ItemStack input) {
        return (currentFuelTime > 0 || hasFuel()) && canSmelt(input);
    }

    private void tickSmelting(ItemStack input) {
        if (this.canSmelt(input) && smeltingTime > 0){
            if (this.currentFuelTime > 0){
                smeltingTime--;
            }
            else if (this.hasFuel()){
                this.consumeFuel();
                smeltingTime--;
            }
            else if (smeltingTime < totalSmeltingTime) {
                smeltingTime++;
            }
            else {
                resetSmelting();
            }
        }
        else if (this.canSmelt(input) && smeltingTime == 0){
            completeSmelting();
            if (this.readyToSmelt(input)) {
                if (currentFuelTime == 0){
                    this.consumeFuel();
                }
                this.startSmelting();
            }
            else {
                this.resetSmelting();
            }
        }
        else {
            resetSmelting();
        }
    }

    // check if fuel is available
    private boolean hasFuel() {
        ItemStack fuelSlot1 = this.inventory.getStack(1);  // fuel slot 1
        ItemStack fuelSlot2 = this.inventory.getStack(2);  // fuel slot 2
        return !fuelSlot1.isEmpty() && !fuelSlot2.isEmpty() &&
                canUseFuel(fuelSlot1) && canUseFuel(fuelSlot2);
    }

    // Check if the item can be used as fuel
    private boolean canUseFuel(ItemStack stack) {
        return world.getFuelRegistry().getFuelTicks(stack) > 0;
    }

    private void startSmelting() {
        this.isSmelting = true;
        this.smeltingTime = this.totalSmeltingTime;
    }

    // consume fuel from the slots
    private void consumeFuel() {
        ItemStack fuel1 = this.inventory.getStack(1);
        ItemStack fuel2 = this.inventory.getStack(2);

        this.totalFuelTime = (world.getFuelRegistry().getFuelTicks(fuel1) + world.getFuelRegistry().getFuelTicks(fuel2)) / 2;
        this.currentFuelTime = this.totalFuelTime;

        ItemStack newFuel1 = fuel1.copy();
        newFuel1.decrement(1);
        ItemStack newFuel2 = fuel2.copy();
        newFuel2.decrement(1);

        this.inventory.setStack(1, newFuel1);
        if (this.inventory.getStack(1).isEmpty()){
            this.inventory.setStack(1, ItemStack.EMPTY);
        }
        this.inventory.setStack(2, newFuel2);
        if (this.inventory.getStack(2).isEmpty()){
            this.inventory.setStack(2, ItemStack.EMPTY);
        }
    }

    // reset smelting if interrupted
    private void resetSmelting() {
        this.isSmelting = false;
        this.smeltingTime = this.totalSmeltingTime;
    }

    // complete smelting, handle output and fuel consumption
    private void completeSmelting() {

        ItemStack input = this.inventory.getStack(0);  // The input slot
        ItemStack output = this.inventory.getStack(3); // The output slot

        if (!input.isEmpty() && canSmelt(input)) {
            ItemStack result = getSmeltingResult(input);  // Determine the result based on the input

            // If the output is empty or matches the result, proceed
            if (output.isEmpty() || ((output.getItem() == result.getItem()) && output.getCount() < output.getMaxCount())) {

                if (output.isEmpty()) {
                    this.inventory.setStack(3, result.copy());
                } else {
                    output.increment(1);  // Increment count for the smelted item
                }

                ItemStack newInput = input.copy();
                newInput.decrement(1);
                this.inventory.setStack(0, newInput);
                if (this.inventory.getStack(0).isEmpty()){
                    this.inventory.setStack(0, ItemStack.EMPTY);
                }

                smeltingTime = totalSmeltingTime;

            }
        }
    }

    // check if the item can be smelted
    private boolean canSmelt(ItemStack input) {

        if (input.isEmpty()) {
            return false; // No input to smelt
        }

        // get the smelting result
        ItemStack result = getSmeltingResult(input);

        // check if the result is valid (not empty)
        if (result.isEmpty()) {
            return false; // no valid recipe found
        }

        // check the output slot (either empty or matches the result)
        ItemStack output = this.inventory.getStack(3); // The output slot

        if (output.isEmpty()) {
            return true; // output slot is empty so we can smelt and place the result
        }

        // check if the output matches the expected result and there is space for more
        return (output.getItem() == result.getItem()) && (output.getCount() < output.getMaxCount());
    }


    public ItemStack getSmeltingResult(ItemStack inputStack) {
        // Ensure we are on the server side, as the following code requires a ServerWorld
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;

            // Now you can safely use the ServerWorld
            SingleStackRecipeInput recipeInput = new SingleStackRecipeInput(inputStack);

            // Use the recipe match getter to find a matching recipe
            RecipeType<? extends AbstractCookingRecipe> recipeType = RecipeType.SMELTING; // Adjust as needed
            RecipeEntry<? extends AbstractCookingRecipe> recipeEntry = serverWorld.getRecipeManager()
                    .getFirstMatch(recipeType, recipeInput, serverWorld)
                    .orElse(null);

            if (recipeEntry != null) {
                AbstractCookingRecipe recipe = recipeEntry.value();
                return recipe.craft(recipeInput, serverWorld.getRegistryManager());
            }
        }
        // If we're on the client side, or no recipe is found, return an empty stack
        return ItemStack.EMPTY;
    }

    // read & write methods for persistent storage
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){

        Inventories.writeNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
        nbt.putInt("SmeltingTime", this.smeltingTime); // Save current smelting time
        nbt.putInt("TotalSmeltingTime", this.totalSmeltingTime); // Save total smelting time
        nbt.putInt("CurrentFuelTime", this.currentFuelTime); // Save current fuel time
        nbt.putInt("TotalFuelTime", this.totalFuelTime); // Save total fuel time
        nbt.putBoolean("IsSmelting", this.isSmelting); // Save smelting state

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){

        super.readNbt(nbt, registryLookup);

        this.smeltingTime = nbt.getInt("SmeltingTime"); // Restore current smelting time
        this.totalSmeltingTime = nbt.getInt("TotalSmeltingTime"); // Restore total smelting time
        this.currentFuelTime = nbt.getInt("CurrentFuelTime"); // Restore current fuel time
        this.totalFuelTime = nbt.getInt("TotalFuelTime"); // Restore total fuel time
        this.isSmelting = nbt.getBoolean("IsSmelting"); // Restore smelting state
        Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);

    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ReinforcedFurnaceScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        var nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isClient){
            sync();
        }
    }

    private void sync(){
        if (world instanceof ServerWorld serverWorld){
            serverWorld.getChunkManager().markForUpdate(pos);
            serverWorld.getPlayers(player -> player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 64)
                    .forEach(player -> player.networkHandler.sendPacket(toUpdatePacket()));
        }
    }


    public InventoryStorage getInventoryProvider(Direction direction){
        return this.inventoryStorage;
    }

    public ReinforcedFurnaceInventory getInventory(){
        return this.inventory;
    }

    public float getFuelFloat() {
        return totalFuelTime > 0 ? (float) currentFuelTime / totalFuelTime : 0.0f;
    }

    public float getSmeltFloat() {
        return totalSmeltingTime > 0 ? 1 - ((float) smeltingTime / totalSmeltingTime) : 0.0f;
    }
}
