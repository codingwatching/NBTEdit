package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.LogHelper;
import cx.rain.mc.nbtedit.utility.PermissionHelper;
import cx.rain.mc.nbtedit.utility.PlayerMessageHelper;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public class C2STileNBTSavePacket {
	/**
	 * The position of the TileEntity.
	 */
	protected BlockPos pos;

	/**
	 * The NBT data of the TileEntity.
	 */
	protected CompoundTag tag;

	public C2STileNBTSavePacket(ByteBuf buf) {
		var packetBuf = new FriendlyByteBuf(buf);
		pos = packetBuf.readBlockPos();
		tag = packetBuf.readNbt();
	}

	public C2STileNBTSavePacket(BlockPos posIn, CompoundTag tagIn) {
		pos = posIn;
		tag = tagIn;
	}

	public void toBytes(ByteBuf buf) {
		var packetBuf = new FriendlyByteBuf(buf);
		packetBuf.writeBlockPos(pos);
		packetBuf.writeNbt(tag);
	}

	public void handler(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			final ServerPlayer player = context.get().getSender();
			player.getServer().execute(() -> {
				BlockEntity tile = player.getCommandSenderWorld().getBlockEntity(pos);
				if (tile != null && PermissionHelper.checkPermission(player)) {
					try {
						tile.load(tag);
						tile.setChanged();	// Ensure changes gets saved to disk later on. (AS: In MCP it is markDirty.)
						if (tile.hasLevel() && tile.getLevel() instanceof ServerLevel) {
							((ServerLevel) tile.getLevel()).getChunkSource().blockChanged(pos);	// Broadcast changes.
						}

						NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName().getString() +
								" successfully edited the tag of a TileEntity at " +
								pos.getX() + " " +
								pos.getY() + " " +
								pos.getZ() + ".");
						LogHelper.logNBTTag(NBTEdit.getInstance().getInternalLogger(), Level.INFO, tag);

						PlayerMessageHelper.sendMessage(player, TranslateKeys.MESSAGE_SAVED);
						// Todo: AS: I18n below.
						// "Your changes have been saved."
					} catch (Exception ex) {
						PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
								TranslateKeys.MESSAGE_SAVE_FAILED_INVALID_NBT);
						// Todo: AS: I18n below.
						// "Save Failed - Invalid NBT format for Tile Entity."

						NBTEdit.getInstance().getInternalLogger().error("Player " + player.getName().getString() +
								" edited the tag of TileEntity and caused an exception.");
						LogHelper.logNBTTag(NBTEdit.getInstance().getInternalLogger(), Level.ERROR, tag);
						NBTEdit.getInstance().getLog().error(new RuntimeException(ex));
					}
				} else {
					NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
							" tried to edit a non-existent TileEntity at " +
							pos.getX() + " " + pos.getY() + " " + pos.getZ() + ".");

					PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
							TranslateKeys.MESSAGE_SAVE_FAILED_NO_LONGER_HAS_TILE, pos.getX(), pos.getY(), pos.getZ());
					// Todo: AS: I18n below.
					// "Save Failed - There is no TileEntity at x y z."
				}
			});
		});
		context.get().setPacketHandled(true);
	}
}
