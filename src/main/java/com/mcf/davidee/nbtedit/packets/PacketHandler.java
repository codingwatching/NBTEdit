package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.Level;

import java.util.Optional;

/**
 * Created by Jay113355 on 6/28/2016.
 *
 */
public class PacketHandler {
	public static final ResourceLocation MAIN = new ResourceLocation(NBTEdit.MODID, "main");
	public SimpleChannel INSTANCE;
	private static int ID = 0;

	public void initialize() {
		INSTANCE = NetworkRegistry.newSimpleChannel(MAIN, () -> "1.0", s -> true, s -> true);
		registerPackets();
	}

	public void registerPackets() {
		if (ID != 0) {
			throw new IllegalStateException("Packets already registered!");
		}
		INSTANCE.registerMessage(ID++, MouseOverPacket.class, MouseOverPacket::toBytes, MouseOverPacket::fromBytes, MouseOverPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

		INSTANCE.registerMessage(ID++, TileRequestPacket.class, TileRequestPacket::toBytes, TileRequestPacket::fromBytes, TileRequestPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(ID++, TileDataPacket.class, TileDataPacket::toBytes, TileDataPacket::fromBytes, TileDataPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		INSTANCE.registerMessage(ID++, TileSavePacket.class, TileSavePacket::toBytes, TileSavePacket::fromBytes, TileSavePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

		INSTANCE.registerMessage(ID++, EntityRequestPacket.class, EntityRequestPacket::toBytes, EntityRequestPacket::fromBytes, EntityRequestPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(ID++, EntityDataPacket.class, EntityDataPacket::toBytes, EntityDataPacket::fromBytes, EntityDataPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		INSTANCE.registerMessage(ID++, EntitySavePacket.class, EntitySavePacket::toBytes, EntitySavePacket::fromBytes, EntitySavePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}

	/**
	 * Sends a TileEntity's nbt data to the player for editing.
	 *
	 * @param player The player to send the TileEntity to.
	 * @param pos    The block containing the TileEntity.
	 */
	public void sendTile(final ServerPlayerEntity player, final BlockPos pos) {
		if (NBTEdit.proxy.checkPermission(player)) {
			TileEntity te = player.getLevel().getBlockEntity(pos);
			if (te != null) {
				CompoundNBT tag = new CompoundNBT();
				te.save(tag);
				INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TileDataPacket(pos, tag));
			} else {
				NBTEdit.proxy.sendMessage(player, "Error - There is no TileEntity at "
						+ pos.getX() + ", " + pos.getY() + ", " + pos.getZ(), TextFormatting.RED);
			}
		}
	}

	/**
	 * Sends a Entity's nbt data to the player for editing.
	 *
	 * @param player   The player to send the Entity data to.
	 * @param entityId The id of the Entity.
	 */
	public void sendEntity(final ServerPlayerEntity player, final int entityId) {
		if (NBTEdit.proxy.checkPermission(player)) {
			Entity entity = player.getLevel().getEntity(entityId);
			if (entity instanceof PlayerEntity && entity != player && !NBTEdit.editOtherPlayers) {
				NBTEdit.proxy.sendMessage(player, "Error - You may not use NBTEdit on other Players", TextFormatting.RED);
				NBTEdit.log(Level.WARN, player.getGameProfile().getName() + " tried to use NBTEdit on another player, " + entity.getName());
				return;
			}
			if (entity != null) {
				CompoundNBT tag = new CompoundNBT();
				entity.saveWithoutId(tag);
				INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new EntityDataPacket(entityId, tag));
			} else {
				NBTEdit.proxy.sendMessage(player, "\"Error - Unknown EntityID #" + entityId, TextFormatting.RED);
			}
		}
	}
}
