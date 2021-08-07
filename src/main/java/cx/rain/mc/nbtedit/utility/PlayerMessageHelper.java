package cx.rain.mc.nbtedit.utility;

import cx.rain.mc.nbtedit.utility.translation.TranslatableLanguage;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerMessageHelper {
    public static void sendMessage(Player player, TranslateKeys key, Object... args) {
        player.sendMessage(new TextComponent(String.format(
                TranslatableLanguage.get().getOrDefault(key.getKey()), args)), player.getUUID());

    }

    public static void sendMessage(Player player, ChatFormatting style, TranslateKeys key, Object... args) {
        player.sendMessage(new TextComponent(String.format(
                TranslatableLanguage.get().getOrDefault(key.getKey()), args)).withStyle(style), player.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendMessageToCurrent(ChatFormatting style, TranslateKeys key, Object... args) {
        var player = Minecraft.getInstance().player;
        player.sendMessage(new TranslatableComponent(key.getKey(), args).withStyle(style), player.getUUID());
    }
}
