package cx.rain.mc.nbtedit.client;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.gui.screen.ScreenNBTEdit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NBTEdit.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NBTEditClient {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        var currentScreen = Minecraft.getInstance().screen;

        if (currentScreen instanceof ScreenNBTEdit nbtedit) {
            if ()
        }
    }
}
