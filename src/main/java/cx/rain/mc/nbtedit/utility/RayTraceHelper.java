package cx.rain.mc.nbtedit.utility;

import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import cx.rain.mc.nbtedit.networking.packet.C2SEntityRequestPacket;
import cx.rain.mc.nbtedit.networking.packet.C2STileRequestPacket;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RayTraceHelper {
    public static void RayTraceBlockOrEntity() {
        HitResult result = Minecraft.getInstance().hitResult;
        if (result != null) {
            if (result.getType() == HitResult.Type.ENTITY) {
                NBTEditNetworking.getInstance().getChannel().sendToServer(
                        new C2SEntityRequestPacket(((EntityHitResult) result).getEntity().getUUID(), false));
            } else if (result.getType() == HitResult.Type.BLOCK) {
                NBTEditNetworking.getInstance().getChannel().sendToServer(
                        new C2STileRequestPacket(((BlockHitResult) result).getBlockPos()));
            } else {
                PlayerMessageHelper.sendMessageToCurrent(ChatFormatting.RED, TranslateKeys.MESSAGE_NO_ANY_TARGET);
            }
        }
    }
}
