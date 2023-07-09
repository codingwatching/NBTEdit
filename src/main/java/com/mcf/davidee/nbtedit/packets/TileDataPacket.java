package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TileDataPacket extends TileRequestPacket {
	/**
	 * The nbt data of the tileEntity.
	 */
	protected CompoundNBT tag;

	/**
	 * Required default constructor.
	 */
	public TileDataPacket() {
	}

	public TileDataPacket(BlockPos pos, CompoundNBT tag) {
		super(pos);
		this.tag = tag;
	}

	public static TileDataPacket fromBytes(PacketBuffer buf) {
		BlockPos pos = BlockPos.of(buf.readLong());
		CompoundNBT tag = NBTHelper.readNbtFromBuffer(buf);
		return new TileDataPacket(pos, tag);
	}

	public void toBytes(PacketBuffer buf) {
		super.toBytes(buf);
		NBTHelper.writeToBuffer(this.tag, buf);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		NBTEdit.proxy.openEditGUI(this.pos, this.tag);
	}
}
