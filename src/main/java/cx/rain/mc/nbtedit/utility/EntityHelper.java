package cx.rain.mc.nbtedit.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @OnlyIn(Dist.CLIENT)
    public static Entity getEntityByUuidClient(int id) {
        return Minecraft.getInstance().player.level.getEntity(id);
    }
}
