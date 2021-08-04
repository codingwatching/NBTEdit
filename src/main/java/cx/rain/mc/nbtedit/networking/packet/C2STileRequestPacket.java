package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class C2STileRequestPacket {
	/**
	 * The position of the tileEntity requested.
	 */
	private BlockPos pos;

	// AS: Default constructor is no longer need.
	public C2STileRequestPacket(ByteBuf buf) {
		pos = BlockPos.of(buf.readLong());
	}

	public C2STileRequestPacket(BlockPos posIn) {
		pos = posIn;
	}

	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.asLong());
	}

	public void handler(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();
			NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() + " requested TileEntity at " +
					pos.getX() + " " + pos.getY() + " " + pos.getZ() + ".");

			NBTEditNetworking.getInstance().sendTileNBTToClient(player, pos);
		});
		context.get().setPacketHandled(true);
	}
}
