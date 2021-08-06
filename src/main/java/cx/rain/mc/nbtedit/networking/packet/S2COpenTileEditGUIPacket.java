package cx.rain.mc.nbtedit.networking.packet;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.gui.GuiEditNBTTree;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenTileEditGUIPacket {
    private BlockPos pos;
    private CompoundTag tag;

    // AS: Default constructor is no longer need.
    public S2COpenTileEditGUIPacket(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        pos = packetBuf.readBlockPos();
        tag = packetBuf.readNbt();
    }

    public S2COpenTileEditGUIPacket(BlockPos posIn, CompoundTag tagIn) {
        pos = posIn;
        tag = tagIn;
    }

    public void toBytes(ByteBuf buf) {
        var packetBuf = new FriendlyByteBuf(buf);
        packetBuf.writeBlockPos(pos);
        packetBuf.writeNbt(tag);
    }

    public void handler(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            NBTEdit.getInstance().getInternalLogger().info("Player " + Minecraft.getInstance().player.getName() +
                    " requested to edit a TileEntity at " +
                    pos.getX() + " " + pos.getY() + " " + pos.getZ() + ".");

            Minecraft.getInstance().setScreen(new GuiEditNBTTree(pos, tag));
        });
        context.get().setPacketHandled(true);
    }
}
