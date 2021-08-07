package cx.rain.mc.nbtedit.utility.translation;

import com.google.common.collect.ImmutableMap;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.config.NBTEditConfigs;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class TranslatableLanguage extends Language {
    private static TranslatableLanguage INSTANCE;

    private Map<String, String> map;

    protected TranslatableLanguage(String localeIn) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        BiConsumer<String, String> biConsumer = builder::put;

        try {
            InputStream is = this.getClass()
                    .getResourceAsStream("/assets/" + NBTEdit.MODID + "/lang/" + localeIn + ".json");
            loadFromJson(is, biConsumer);
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        map = builder.build();
    }

    public static TranslatableLanguage get() {
        if (INSTANCE == null) {
            INSTANCE = new TranslatableLanguage(NBTEditConfigs.LANGUAGE.get());
        }
        return INSTANCE;
    }

    @Override
    public String getOrDefault(String key) {
        return map.getOrDefault(key, key);
    }

    @Override
    public boolean has(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return false;
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText text) {
        return (visitor) -> text.visit((style, string) ->
                StringDecomposer.iterateFormatted(string, style, visitor)
                        ? Optional.empty()
                        : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
    }
}
