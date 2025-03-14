package com.jomlom.workstationupgrades;

import com.jomlom.workstationupgrades.init.ScreenHandlerTypeInit;
import com.jomlom.workstationupgrades.screen.AdvancedCraftingTableScreen;
import com.jomlom.workstationupgrades.screen.ReinforcedFurnaceScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class WorkstationUpgradesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		HandledScreens.register(ScreenHandlerTypeInit.REINFORCED_FURNACE, ReinforcedFurnaceScreen::new);

		HandledScreens.register(ScreenHandlerTypeInit.ADVANCED_CRAFTING_TABLE, AdvancedCraftingTableScreen::new);

	}
}