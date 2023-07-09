package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityDataPacket extends EntityRequestPacket {
	/**
	 * The nbt data of the entity.
	 */
	protected CompoundNBT tag;

	/**
	 * Required default constructor.
	 */
	public EntityDataPacket() {
	}

	public EntityDataPacket(int entityID, CompoundNBT tag) {
		super(entityID);
		this.tag = tag;
	}

	public static EntityDataPacket fromBytes(ByteBuf buf) {
		int entityID = buf.readInt();
		CompoundNBT tag = NBTHelper.readNbtFromBuffer(buf);
		return new EntityDataPacket(entityID, tag);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		NBTHelper.writeToBuffer(this.tag, buf);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		NBTEdit.proxy.openEditGUI(this.entityID, this.tag);
	}
}
