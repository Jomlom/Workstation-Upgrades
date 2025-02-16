package com.jomlom.workstationupgrades;

import com.jomlom.workstationupgrades.init.ScreenHandlerTypeInit;
import com.jomlom.workstationupgrades.screen.ReinforcedFurnaceScreen;
import com.jomlom.workstationupgrades.screenhandler.ReinforcedFurnaceScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.screen.ScreenHandlerType;

public class WorkstationUpgradesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// bind screen to handler
		HandledScreens.register(ScreenHandlerTypeInit.REINFORCED_FURNACE, ReinforcedFurnaceScreen::new);

	}
}