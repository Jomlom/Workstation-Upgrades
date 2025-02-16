package com.jomlom.workstationupgrades.init;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.jomlom.workstationupgrades.network.BlockPosPayload;
import com.jomlom.workstationupgrades.screenhandler.ReinforcedFurnaceScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ScreenHandlerTypeInit {

    public static final ScreenHandlerType<ReinforcedFurnaceScreenHandler> REINFORCED_FURNACE =
            register("reinforced_furnace", ReinforcedFurnaceScreenHandler::new, BlockPosPayload.PACKET_CODEC);

    public static <T extends ScreenHandler, D extends CustomPayload> ExtendedScreenHandlerType<T, D>
    register(
            String name,
            ExtendedScreenHandlerType.ExtendedFactory<T, D> factory,
            PacketCodec<? super RegistryByteBuf, D> packetCodec
    ){
        return Registry.register(Registries.SCREEN_HANDLER, Identifier.of(WorkstationUpgrades.MOD_ID, name), new ExtendedScreenHandlerType<>(factory, packetCodec));
    }

    public static void initialize(){
    }


}
