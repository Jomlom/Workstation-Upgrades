package com.jomlom.workstationupgrades;

import com.jomlom.workstationupgrades.block.AdvancedCraftingTable;
import com.jomlom.workstationupgrades.block.ReinforcedFurnace;
import com.jomlom.workstationupgrades.blockentity.AdvancedCraftingTableEntity;
import com.jomlom.workstationupgrades.blockentity.ReinforcedFurnaceEntity;
import com.jomlom.workstationupgrades.init.ScreenHandlerTypeInit;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkstationUpgrades implements ModInitializer {

	public static final String MOD_ID = "workstationupgrades";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Initializing Workstation Upgrades");

		ModBlocks.initialize();
		ScreenHandlerTypeInit.initialize();

	}

	public static class ModBlocks {

		public static void initialize(){

			ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(
					itemGroup -> {itemGroup.add(ModBlocks.REINFORCED_FURNACE.asItem());}
			);

			ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(
					itemGroup -> {itemGroup.add(ModBlocks.ADVANCED_CRAFTING_TABLE.asItem());}
			);

		}

		//region Reinforced Furnace
		public static final RegistryKey<Block> REINFORCED_FURNACE_KEY = RegistryKey.of(
				RegistryKeys.BLOCK,
				Identifier.of(MOD_ID, "reinforced_furnace")
		);

		public static final Block REINFORCED_FURNACE = register(
				new ReinforcedFurnace(AbstractBlock.Settings.create().registryKey(REINFORCED_FURNACE_KEY)
						.sounds(BlockSoundGroup.ANVIL)
						.requiresTool()
						.strength(10f)
						.resistance(20f)
				),
				REINFORCED_FURNACE_KEY,
				true
		);

		public static final BlockEntityType<ReinforcedFurnaceEntity> REINFORCED_FURNACE_ENTITY
				= register("reinforced_furnace", ReinforcedFurnaceEntity::new, ModBlocks.REINFORCED_FURNACE);
		//endregion

		//region Advanced Crafting Table
		public static final RegistryKey<Block> ADVANCED_CRAFTING_TABLE_KEY = RegistryKey.of(
				RegistryKeys.BLOCK,
				Identifier.of(MOD_ID, "advanced_crafting_table")
		);

		public static final Block ADVANCED_CRAFTING_TABLE = register(
				new AdvancedCraftingTable(AbstractBlock.Settings.create().registryKey(ADVANCED_CRAFTING_TABLE_KEY)
						.sounds(BlockSoundGroup.GILDED_BLACKSTONE)
						.requiresTool()
						.strength(3.5f)
						.resistance(10f)
				),
				ADVANCED_CRAFTING_TABLE_KEY,
				true
		);

		public static final BlockEntityType<AdvancedCraftingTableEntity> ADVANCED_CRAFTING_TABLE_ENTITY
				= register("advanced_crafting_table", AdvancedCraftingTableEntity::new, ModBlocks.ADVANCED_CRAFTING_TABLE);

		//endregion

		private static <T extends BlockEntity> BlockEntityType<T> register(
				String name,
				FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
				Block... blocks
		) {
			Identifier id = Identifier.of(MOD_ID, name);
			return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
		}

		public static Block register(Block block, RegistryKey<Block> blockKey, boolean shouldRegister){
			if (shouldRegister){
				RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
				BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
				Registry.register(Registries.ITEM, itemKey, blockItem);
			}
			return Registry.register(Registries.BLOCK, blockKey, block);
		}


	}
}