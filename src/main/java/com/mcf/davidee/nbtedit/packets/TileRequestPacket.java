package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public class TileRequestPacket {
	/**
	 * The position of the tileEntity requested.
	 */
	protected BlockPos pos;

	/**
	 * Required default constructor.
	 */
	public TileRequestPacket() {
	}

	public TileRequestPacket(BlockPos pos) {
		this.pos = pos;
	}

	public static TileRequestPacket fromBytes(PacketBuffer buf) {
		return new TileRequestPacket(BlockPos.of(buf.readLong()));
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeLong(this.pos.asLong());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity player = ctx.get().getSender();

		ctx.get().enqueueWork(() -> NBTEdit.NETWORK.sendTile(player, pos));
		NBTEdit.log(Level.TRACE, player.getGameProfile().getName() + " requested tileEntity at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

	}

}
