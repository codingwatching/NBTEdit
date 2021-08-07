package cx.rain.mc.nbtedit.data;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.data.provider.language.LanguageProviderENUS;
import cx.rain.mc.nbtedit.data.provider.language.LanguageProviderZHCN;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = NBTEdit.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeClient()) {
        }

        if (event.includeServer()) {
            generator.addProvider(new LanguageProviderENUS(generator));
            generator.addProvider(new LanguageProviderZHCN(generator));
        }
    }
}