package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SEntityRequestPacket {
	/**
	 * The UUID of the entity being requested.
	 */
	private UUID uuid;

	private boolean isMe;

	public C2SEntityRequestPacket(UUID uuidIn, boolean isMeIn) {
		uuid = uuidIn;
		isMe = isMeIn;
	}

	public C2SEntityRequestPacket(ByteBuf buf) {
		var packetBuf = new FriendlyByteBuf(buf);
		uuid = packetBuf.readUUID();
		isMe = packetBuf.readBoolean();
	}

	public void toBytes(ByteBuf buf) {
		var packetBuf = new FriendlyByteBuf(buf);
		packetBuf.writeUUID(uuid);
		packetBuf.writeBoolean(isMe);
	}

	public void handler(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();
			NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
					" requested entity with UUID " + uuid + ".");
			NBTEditNetworking.getInstance().openEntityEditGUIResponse(player, uuid, isMe);
		});
		context.get().setPacketHandled(true);
	}
}
