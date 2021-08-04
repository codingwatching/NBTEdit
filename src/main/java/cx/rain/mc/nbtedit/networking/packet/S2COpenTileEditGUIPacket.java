package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenTileEditGUIPacket {
    private BlockPos pos;

    // AS: Default constructor is no longer need.
    public S2COpenTileEditGUIPacket(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        pos = packetBuf.readBlockPos();
    }

    public S2COpenTileEditGUIPacket(BlockPos posIn) {
        pos = posIn;
    }

    public void toBytes(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        packetBuf.writeBlockPos(pos);
    }

    public void handler(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
                    " requested to edit a TileEntity at " +
                    pos.getX() + " " + pos.getY() + " " + pos.getZ() + ".");

            NBTEditNetworking.getInstance().sendTileNBTToClient(player, pos);
        });
        context.get().setPacketHandled(true);
    }
}
