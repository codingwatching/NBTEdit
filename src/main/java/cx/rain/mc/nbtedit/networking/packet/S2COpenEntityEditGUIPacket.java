package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.gui.screen.NBTEditScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class S2COpenEntityEditGUIPacket {
    protected UUID uuid;
    protected int id;
    protected CompoundTag tag;
    protected boolean isMe;

    public S2COpenEntityEditGUIPacket(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        uuid = packetBuf.readUUID();
        id = packetBuf.readInt();
        tag = packetBuf.readNbt();
        isMe = packetBuf.readBoolean();
    }

    public S2COpenEntityEditGUIPacket(UUID uuidIn, int idIn, CompoundTag tagIn, boolean isMeIn) {
        uuid = uuidIn;
        id = idIn;
        tag = tagIn;
        isMe = isMeIn;
    }

    public void toBytes(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        packetBuf.writeUUID(uuid);
        packetBuf.writeInt(id);
        packetBuf.writeNbt(tag);
        packetBuf.writeBoolean(isMe);
    }

    public void handler(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            NBTEdit.getInstance().getInternalLogger().info("Player " + Minecraft.getInstance().player.getName().getString() +
                    " requested to edit an Entity with UUID " + uuid + " .");

            Minecraft.getInstance().setScreen(new NBTEditScreen(uuid, id, tag, isMe));
        });
        context.get().setPacketHandled(true);
    }
}
