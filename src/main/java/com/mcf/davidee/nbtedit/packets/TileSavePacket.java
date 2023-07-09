package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public class TileSavePacket extends TileDataPacket {

	/**
	 * Required default constructor.
	 */
	public TileSavePacket() {
	}

	public TileSavePacket(BlockPos pos, CompoundNBT tag) {
		this.pos = pos;
		this.tag = tag;
	}

	public static TileSavePacket fromBytes(PacketBuffer buf) {
		BlockPos pos = BlockPos.of(buf.readLong());
		CompoundNBT tag = NBTHelper.readNbtFromBuffer(buf);
		return new TileSavePacket(pos, tag);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(() -> {
			ServerWorld level = player.getLevel();
			BlockState state = level.getBlockState(this.pos);
			TileEntity te = level.getBlockEntity(this.pos);
			if (te != null && NBTEdit.proxy.checkPermission(player)) {
				try {
					te.load(state, this.tag);
					te.setChanged();// Ensures changes gets saved to disk later on.
					if (te.hasLevel() && te.getLevel() instanceof ServerWorld) {
						//TODO Figure out what this changed to in 1.16. if needed. -Jay
						//((ServerWorld) te.getLevel()).getPlayerChunkMap().markBlockForUpdate(packet.pos);// Broadcast changes.
					}
					NBTEdit.log(Level.TRACE, player.getGameProfile().getName() + " edited a tag -- Tile Entity at " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ());
					NBTEdit.logTag(this.tag);
					NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
				} catch (Throwable t) {
					NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Tile Entity", TextFormatting.RED);
					NBTEdit.log(Level.WARN, player.getGameProfile().getName() + " edited a tag and caused an exception");
					NBTEdit.logTag(this.tag);
					NBTEdit.throwing("TileNBTPacket", "Handler.onMessage", t);
				}
			} else {
				NBTEdit.log(Level.WARN, player.getGameProfile().getName() + " tried to edit a non-existent TileEntity at " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ());
				NBTEdit.proxy.sendMessage(player, "cSave Failed - There is no TileEntity at " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ(), TextFormatting.RED);
			}
		});
	}
}
