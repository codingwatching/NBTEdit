package cx.rain.mc.nbtedit.utility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class EntityHelper {
    public static Entity getEntityByUuid(MinecraftServer server, UUID uuid) {
        Entity result = null;
        for (var level : server.getAllLevels()) {
            var entity = level.getEntity(uuid);
            if (entity != null) {
                result = entity;
            }
        }
        return result;
    }
}
