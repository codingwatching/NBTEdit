package cx.rain.mc.nbtedit.utility;

import cx.rain.mc.nbtedit.config.NBTEditConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public class PermissionHelper {
    public static boolean checkPermission(CommandSourceStack source) {
        if (source.getEntity() instanceof Player player) {
            return checkPermission(player);
        }
        return false;
    }

    public static boolean checkPermission(Player player) {
        if (player.getServer().isSingleplayer()) {
            return true;
        } else {
            if (NBTEditConfigs.OP_ONLY.get()) {
                var entry = player.getServer().getPlayerList().getOps()
                        .get(player.getGameProfile());
                return entry != null;
            } else {
                return player.isCreative();
            }
        }
    }
}
