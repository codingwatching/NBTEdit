package cx.rain.mc.nbtedit.utility;

import com.mojang.authlib.GameProfile;
import cx.rain.mc.nbtedit.config.NBTEditConfigs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PermissionHelper {
    public static boolean checkPermission(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return checkPermission(player);
        }
        return false;
    }

    public static boolean checkPermission(ServerPlayer player) {
        if (NBTEditConfigs.OP_ONLY.get()) {
            var entry = player.getServer().getPlayerList().getOps()
                    .get(new GameProfile(player.getUUID(), player.getName().getContents()));
            return entry != null;
        } else {
            return player.isCreative();
        }
    }
}
