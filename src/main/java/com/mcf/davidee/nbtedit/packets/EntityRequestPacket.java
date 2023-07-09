package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public class EntityRequestPacket {
	/**
	 * The id of the entity being requested.
	 */
	protected int entityID;

	/**
	 * Required default constructor.
	 */
	public EntityRequestPacket() {
	}

	public EntityRequestPacket(int entityID) {
		this.entityID = entityID;
	}

	public static EntityRequestPacket fromBytes(ByteBuf buf) {
		return new EntityRequestPacket(buf.readInt());
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityID);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity player = ctx.get().getSender();
		NBTEdit.log(Level.TRACE, player.getGameProfile().getName() + " requested entity with Id #" + this.entityID);
		ctx.get().enqueueWork(() -> NBTEdit.NETWORK.sendEntity(player, this.entityID));
	}
}
