package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.EntityHelper;
import cx.rain.mc.nbtedit.utility.LogHelper;
import cx.rain.mc.nbtedit.utility.PermissionHelper;
import cx.rain.mc.nbtedit.utility.PlayerMessageHelper;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SEntityNBTSavePacket {
	/**
	 * The id of the entity being edited.
	 */
	protected UUID uuid;
	/**
	 * The nbt data of the entity.
	 */
	protected CompoundTag tag;

	protected int id;
	protected boolean isMe;

	public C2SEntityNBTSavePacket(ByteBuf buf) {
		var packetBuf = new FriendlyByteBuf(buf);
		uuid = packetBuf.readUUID();
		id = packetBuf.readInt();
		tag = packetBuf.readNbt();
		isMe = packetBuf.readBoolean();
	}

	public C2SEntityNBTSavePacket(UUID uuidIn, int idIn, CompoundTag tagIn, boolean isMeIn) {
		uuid = uuidIn;
		tag = tagIn;
		id = idIn;
		isMe = isMeIn;
	}

	public void toBytes(ByteBuf buf) {
		var packetBuf = new FriendlyByteBuf(buf);
		packetBuf.writeUUID(uuid);
		packetBuf.writeInt(id);
		packetBuf.writeNbt(tag);
		packetBuf.writeBoolean(isMe);
	}

	public void handler(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			final ServerPlayer player = context.get().getSender();
			player.getServer().execute(() -> {
				Entity entity = EntityHelper.getEntityByUuid(player.getServer(), uuid);
				if (entity != null && PermissionHelper.checkPermission(player)) {
					try {
						GameType prevGameMode = null;
						if (entity instanceof ServerPlayer) {
							prevGameMode = ((ServerPlayer) entity).gameMode.getGameModeForPlayer();
						}
						entity.load(tag);
						NBTEdit.getInstance().getInternalLogger().info("Player" + player.getName().getString() +
								" edited the tag of Entity with UUID " + uuid + " .");
						LogHelper.logNBTTag(NBTEdit.getInstance().getInternalLogger(), Level.INFO, tag);

						if (entity instanceof ServerPlayer) {
							// Fixme: AS: Bad impl.
							// Update player info
							// This is fairly hacky.
							// Consider swapping to an event driven system, where classes can register to
							// receive entity edit events and provide feedback/send packets as necessary.
							ServerPlayer targetPlayer = (ServerPlayer) entity;
							targetPlayer.initMenu(targetPlayer.inventoryMenu);
							GameType gameMode = targetPlayer.gameMode.getGameModeForPlayer();
							if (prevGameMode != gameMode) {
								targetPlayer.setGameMode(gameMode);
							}
							targetPlayer.connection.send(new ClientboundSetHealthPacket(targetPlayer.getHealth(),
									targetPlayer.getFoodData().getFoodLevel(),
									targetPlayer.getFoodData().getSaturationLevel()));
							targetPlayer.connection.send(new ClientboundSetExperiencePacket(
									targetPlayer.experienceProgress,
									targetPlayer.totalExperience,
									targetPlayer.experienceLevel));

							targetPlayer.onUpdateAbilities();
							targetPlayer.setPos(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
						}
						PlayerMessageHelper.sendMessage(player, TranslateKeys.MESSAGE_SAVED);
						// Todo: AS: I18n
						// "Your changes have been saved"
					} catch (Exception ex) {
						PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
								TranslateKeys.MESSAGE_SAVE_FAILED_INVALID_NBT);

						NBTEdit.getInstance().getLog().error("Player " + player.getName().getString() +
								" edited the tag of an entity and caused an exception.");
						LogHelper.logNBTTag(NBTEdit.getInstance().getInternalLogger(), Level.ERROR, tag);
						NBTEdit.getInstance().getLog().error(new RuntimeException(ex));
					}
				} else {
					PlayerMessageHelper.sendMessage(player, ChatFormatting.RED,
							TranslateKeys.MESSAGE_SAVE_FAILED_ENTITY_NOT_EXISTS);
				}
			});
		});
		context.get().setPacketHandled(true);
	}
}
