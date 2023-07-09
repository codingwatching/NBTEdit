package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.network.play.server.SUpdateHealthPacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public class EntitySavePacket extends EntityDataPacket {

	/**
	 * Required default constructor.
	 */
	public EntitySavePacket() {
	}

	public EntitySavePacket(int entityID, CompoundNBT tag) {
		super(entityID, tag);
	}

	public static EntitySavePacket fromBytes(ByteBuf buf) {
		int entityID = buf.readInt();
		CompoundNBT tag = NBTHelper.readNbtFromBuffer(buf);
		return new EntitySavePacket(entityID, tag);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(() -> {
			Entity entity = player.getLevel().getEntity(this.entityID);
			if (entity != null && NBTEdit.proxy.checkPermission(player)) {
				try {
					GameType preGameType = null;
					if (entity instanceof ServerPlayerEntity)
						preGameType = ( (ServerPlayerEntity) entity ).gameMode.getGameModeForPlayer();
					entity.load(this.tag);
					NBTEdit.log(Level.TRACE, player.getGameProfile().getName() + " edited a tag -- Entity ID #" + this.entityID);
					NBTEdit.logTag(this.tag);
					if (entity instanceof ServerPlayerEntity) {//Update player info
						// This is fairly hacky. Consider swapping to an event driven system, where classes can register to
						// receive entity edit events and provide feedback/send packets as necessary.
						ServerPlayerEntity targetPlayer = (ServerPlayerEntity) entity;
						targetPlayer.refreshContainer(targetPlayer.inventoryMenu);
						GameType type = targetPlayer.gameMode.getGameModeForPlayer();
						if (preGameType != type) {
							targetPlayer.setGameMode(type);
						}
						targetPlayer.connection.send(new SUpdateHealthPacket(targetPlayer.getHealth(),
								targetPlayer.getFoodData().getFoodLevel(), targetPlayer.getFoodData().getSaturationLevel()));
						targetPlayer.connection.send(new SSetExperiencePacket(targetPlayer.experienceProgress,
								targetPlayer.totalExperience, targetPlayer.experienceLevel));
						targetPlayer.onUpdateAbilities();
						targetPlayer.setPos(targetPlayer.xo, targetPlayer.yo, targetPlayer.zo);
					}
					NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
				} catch (Throwable t) {
					NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Entity", TextFormatting.RED);
					NBTEdit.log(Level.WARN, player.getGameProfile().getName() + " edited a tag and caused an exception");
					NBTEdit.logTag(this.tag);
					NBTEdit.throwing("EntityNBTPacket", "Handler.onMessage", t);
				}
			} else {
				NBTEdit.proxy.sendMessage(player, "Save Failed - Entity does not exist", TextFormatting.RED);
			}
		});
	}
}
