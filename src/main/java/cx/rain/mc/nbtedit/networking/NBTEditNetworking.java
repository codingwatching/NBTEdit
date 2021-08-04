package cx.rain.mc.nbtedit.networking;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.config.NBTEditConfigs;
import cx.rain.mc.nbtedit.networking.packet.*;
import cx.rain.mc.nbtedit.utility.EntityHelper;
import cx.rain.mc.nbtedit.utility.PermissionHelper;
import cx.rain.mc.nbtedit.utility.PlayerMessageHelper;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.UUID;

/**
 * Created by Jay113355 on 6/28/2016.
 */
public class NBTEditNetworking {
	private static NBTEditNetworking INSTANCE;
	private static SimpleChannel CHANNEL;

	public static final ResourceLocation CHANNEL_ID = new ResourceLocation(NBTEdit.MODID, NBTEdit.MODID);

	private static int ID = 0;

	public NBTEditNetworking() {
		INSTANCE = this;
		registerMessages();
	}

	private static int nextId() {
		return ID++;
	}

	public static NBTEditNetworking getInstance() {
		return INSTANCE;
	}

	public SimpleChannel getChannel() {
		return CHANNEL;
	}

	public void registerMessages() {
		CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID,
				() -> NBTEdit.VERSION,
				(version) -> version.equals(NBTEdit.VERSION),
				(version) -> version.equals(NBTEdit.VERSION)
		);

		CHANNEL.messageBuilder(S2CRayTracePacket.class, nextId())
				.encoder(S2CRayTracePacket::toBytes)
				.decoder(S2CRayTracePacket::new)
				.consumer(S2CRayTracePacket::handler)
				.add();

		CHANNEL.messageBuilder(C2STileRequestPacket.class, nextId())
				.encoder(C2STileRequestPacket::toBytes)
				.decoder(C2STileRequestPacket::new)
				.consumer(C2STileRequestPacket::handler)
				.add();

		CHANNEL.messageBuilder(C2STileNBTSavePacket.class, nextId())
				.encoder(C2STileNBTSavePacket::toBytes)
				.decoder(C2STileNBTSavePacket::new)
				.consumer(C2STileNBTSavePacket::handler)
				.add();

		CHANNEL.messageBuilder(S2COpenTileEditGUIPacket.class, nextId())
				.encoder(S2COpenTileEditGUIPacket::toBytes)
				.decoder(S2COpenTileEditGUIPacket::new)
				.consumer(S2COpenTileEditGUIPacket::handler)
				.add();

		CHANNEL.messageBuilder(C2SEntityRequestPacket.class, nextId())
				.encoder(C2SEntityRequestPacket::toBytes)
				.decoder(C2SEntityRequestPacket::new)
				.consumer(C2SEntityRequestPacket::handler)
				.add();

		CHANNEL.messageBuilder(C2SEntityNBTSavePacket.class, nextId())
				.encoder(C2SEntityNBTSavePacket::toBytes)
				.decoder(C2SEntityNBTSavePacket::new)
				.consumer(C2SEntityNBTSavePacket::handler)
				.add();

		CHANNEL.messageBuilder(S2COpenEntityEditGUIPacket.class, nextId())
				.encoder(S2COpenEntityEditGUIPacket::toBytes)
				.decoder(S2COpenEntityEditGUIPacket::new)
				.consumer(S2COpenEntityEditGUIPacket::handler)
				.add();

		NBTEdit.getInstance().getLog().info("Messages registered.");
	}

	/**
	 * Sends a TileEntity's nbt data to the player for editing.
	 *
	 * @param player The player to send the TileEntity to.
	 * @param pos    The block containing the TileEntity.
	 */
	public void sendTileNBTToClient(final ServerPlayer player, final BlockPos pos) {
		if (PermissionHelper.checkPermission(player)) {
			player.getServer().addTickable(() -> {
				BlockEntity tile = player.getCommandSenderWorld().getBlockEntity(pos);
				if (tile != null) {
					CompoundTag tag = new CompoundTag();
					tile.save(tag);
					CHANNEL.sendTo(new C2STileNBTSavePacket(pos, tag),
							player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
				} else {
					PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
							TranslateKeys.MESSAGE_NO_TARGET_TILE, pos.getX(), pos.getY(), pos.getZ());
					// Todo: AS: Add below to I18n.
					// "Error! There is no TileEntity at " + pos.getX() + " " +	pos.getY() + " " + pos.getZ() + "."
				}
			});
		}
	}

	/**
	 * Sends a Entity's nbt data to the player for editing.
	 *
	 * @param player   The player to send the Entity data to.
	 * @param uuid The UUID of the Entity.
	 */
	public void sendEntityToClient(final ServerPlayer player, final UUID uuid, boolean isMe) {
		if (PermissionHelper.checkPermission(player)) {
			player.getServer().addTickable(() -> {
				Entity entity;
				if (isMe) {
					entity = player;
				}
				entity = EntityHelper.getEntityByUuid(player.getServer(), uuid);

				if (entity instanceof Player && entity != player && !NBTEditConfigs.CAN_EDIT_OTHER_PLAYERS.get()) {
					PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
							TranslateKeys.MESSAGE_CANNOT_EDIT_OTHER_PLAYER);
					// Todo: AS: I18n below.
					// "Error - You may not use NBTEdit on other Players"
					NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
							" tried to use NBTEdit on another player: " + entity.getName() + " .");
					return;
				}

				if (entity != null) {
					CompoundTag tag = new CompoundTag();
					entity.save(tag);
					CHANNEL.sendTo(new C2SEntityNBTSavePacket(uuid, tag),
							player.connection.getConnection(), NetworkDirection.PLAY_TO_SERVER);
				} else {
					PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
							TranslateKeys.MESSAGE_UNKNOWN_ENTITY_ID);
					// Todo: AS: I18n below.
					// "Error - Unknown EntityID #" + entityId,"
				}
			});
		}
	}
}
