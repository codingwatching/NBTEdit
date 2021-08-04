package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class S2COpenEntityEditGUIPacket {
    private UUID uuid;
    private boolean isMe;

    public S2COpenEntityEditGUIPacket(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        uuid = packetBuf.readUUID();
        isMe = packetBuf.readBoolean();
    }

    public S2COpenEntityEditGUIPacket(UUID uuidIn, boolean isMeIn) {
        uuid = uuidIn;
        isMe = isMeIn;
    }

    public void toBytes(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        packetBuf.writeUUID(uuid);
        packetBuf.writeBoolean(isMe);
    }

    public void handler(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
                    " requested to edit an Entity with UUID " + uuid + " .");

            NBTEditNetworking.getInstance().sendEntityToClient(player, uuid, isMe);
        });
        context.get().setPacketHandled(true);
    }
}
