package com.jomlom.workstationupgrades.network;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record BlockPosPayload(BlockPos pos) implements CustomPayload {

    public static final Id<BlockPosPayload> ID = new Id<>(Identifier.of(WorkstationUpgrades.MOD_ID, "block_pos"));

    public static final PacketCodec<RegistryByteBuf, BlockPosPayload> PACKET_CODEC =
        PacketCodec.tuple(BlockPos.PACKET_CODEC, BlockPosPayload::pos, BlockPosPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
