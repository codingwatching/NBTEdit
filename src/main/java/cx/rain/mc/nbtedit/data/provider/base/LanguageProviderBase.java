package cx.rain.mc.nbtedit.data.provider.base;

import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fmllegacy.RegistryObject;

public abstract class LanguageProviderBase extends LanguageProvider {
    public LanguageProviderBase(DataGenerator gen, String modid, String locale) {
        super(gen, modid, locale);
    }

    public void addItemGroup(CreativeModeTab group, String name) {
        add(((TranslatableComponent) group.getDisplayName()).getKey(), name);
    }

    public void addTooltip(RegistryObject<? extends Item> item, int line, String tooltip) {
        String name = item.getId().getPath();
        add("tooltip." + name + "." + line, tooltip);
    }

    public void addKey(TranslateKeys key, String string) {
        add(key.getKey(), string);
    }
}
