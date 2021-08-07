package cx.rain.mc.nbtedit.keybinding;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import cx.rain.mc.nbtedit.networking.packet.C2SEntityRequestPacket;
import cx.rain.mc.nbtedit.networking.packet.C2STileRequestPacket;
import cx.rain.mc.nbtedit.utility.PlayerMessageHelper;
import cx.rain.mc.nbtedit.utility.RayTraceHelper;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NBTEdit.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class OnNBTEditShortcut {
    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (NBTEditKeyBindings.NBTEDIT_SHORTCUT.consumeClick()) {
            RayTraceHelper.RayTraceBlockOrEntity();
        }
    }
}
