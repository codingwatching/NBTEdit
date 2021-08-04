package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.utility.PlayerMessageHelper;
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
		context.get().enqueueWork(() -> {
			HitResult result = Minecraft.getInstance().hitResult;
			if (result != null) {
				if (result.getType() == HitResult.Type.ENTITY) {
					new C2SEntityRequestPacket(((EntityHitResult) result).getEntity().getUUID(), false);
				} else if (result.getType() == HitResult.Type.BLOCK) {
					new C2STileRequestPacket(((BlockHitResult) result).getBlockPos());
				} else {
					PlayerMessageHelper.sendMessageToCurrent(ChatFormatting.RED, TranslateKeys.MESSAGE_NO_ANY_TARGET);
					// Todo: AS: I18n below.
					// "Error - No tile or entity selected"
				}
			}
		});
		context.get().setPacketHandled(true);
	}
}
