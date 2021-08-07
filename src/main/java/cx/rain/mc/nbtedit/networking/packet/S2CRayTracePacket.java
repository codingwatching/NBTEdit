package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import cx.rain.mc.nbtedit.utility.PlayerMessageHelper;
import cx.rain.mc.nbtedit.utility.RayTraceHelper;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRayTracePacket {
	public S2CRayTracePacket() {
	}

	public S2CRayTracePacket(ByteBuf buf) {
	}

	public void toBytes(ByteBuf buf) {
	}

	public void handler(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(RayTraceHelper::RayTraceBlockOrEntity);
		context.get().setPacketHandled(true);
	}
}
