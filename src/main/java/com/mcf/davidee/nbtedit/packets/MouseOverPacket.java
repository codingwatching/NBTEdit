package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MouseOverPacket {

	/**
	 * Required default constructor.
	 */
	public MouseOverPacket() {
	}

	public static MouseOverPacket fromBytes(PacketBuffer buf) {
		return new MouseOverPacket();
	}

	public void toBytes(PacketBuffer buf) {
		//Nothing to write.
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		RayTraceResult ray = Minecraft.getInstance().hitResult;
		if (ray != null) {
			if (ray.getType() == RayTraceResult.Type.ENTITY) {
				EntityRayTraceResult entityRay = (EntityRayTraceResult) ray;
				NBTEdit.NETWORK.INSTANCE.reply(new EntityRequestPacket(entityRay.getEntity().getId()), ctx.get());
			} else if (ray.getType() == RayTraceResult.Type.BLOCK) {
				BlockRayTraceResult blockRay = (BlockRayTraceResult) ray;
				NBTEdit.NETWORK.INSTANCE.reply(new TileRequestPacket(blockRay.getBlockPos()), ctx.get());
			} else {
				NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
			}
		}
	}
}
