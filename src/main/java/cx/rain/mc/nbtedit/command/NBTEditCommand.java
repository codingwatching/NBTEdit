package cx.rain.mc.nbtedit.command;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import cx.rain.mc.nbtedit.networking.packet.S2CRayTracePacket;
import cx.rain.mc.nbtedit.utility.PermissionHelper;
import com.mojang.brigadier.context.CommandContext;
import cx.rain.mc.nbtedit.utility.translation.TranslatableLanguage;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.NetworkDirection;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = NBTEdit.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NBTEditCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        var command = dispatcher.register(
                Commands.literal(NBTEdit.MODID)
                        .requires(PermissionHelper::checkPermission)
                        .executes(NBTEditCommand::onNBTEdit)
                        .then(Commands.argument("entity_id", UuidArgument.uuid())
                                .requires(PermissionHelper::checkPermission)
                                .executes(NBTEditCommand::onNBTEditEntity))
                        .then(Commands.argument("block_pos", BlockPosArgument.blockPos())
                                .requires(PermissionHelper::checkPermission)
                                .executes(NBTEditCommand::onNBTEditTileEntity))
                        .then(Commands.literal("me")
                                .requires(PermissionHelper::checkPermission)
                                .executes(NBTEditCommand::onNBTEditMe))
        );

        NBTEdit.getInstance().getLog().info("Registered command /nbtedit .");
    }

    public static int onNBTEdit(final CommandContext<CommandSourceStack> context) {
        if (!checkTwice(context)) {
            return 0;
        }

        assert context.getSource().getEntity() instanceof ServerPlayer;

        var player = (ServerPlayer) context.getSource().getEntity();

        NBTEditNetworking.getInstance().getChannel().sendTo(new S2CRayTracePacket(),
                player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);

        NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() + " issued command /nbtedit .");

        return 1;
    }

    public static int onNBTEditEntity(final CommandContext<CommandSourceStack> context) {
        if (!checkTwice(context)) {
            return 0;
        }

        assert context.getSource().getEntity() instanceof ServerPlayer;

        var player = (ServerPlayer) context.getSource().getEntity();
        var uuid = context.getArgument("entity_id", UUID.class);
        NBTEditNetworking.getInstance().sendEntityToClient(player, uuid, false);

        NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
                " issued command /nbtedit " + uuid + ".");

        return 1;
    }


    public static int onNBTEditMe(final CommandContext<CommandSourceStack> context) {
        if (!checkTwice(context)) {
            return 0;
        }

        assert context.getSource().getEntity() instanceof ServerPlayer;

        var player = (ServerPlayer) context.getSource().getEntity();
        NBTEditNetworking.getInstance().sendEntityToClient(player, player.getUUID(), true);

        NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
                " issued command /nbtedit with themselves.");

        return 1;
    }

    public static int onNBTEditTileEntity(final CommandContext<CommandSourceStack> context) {
        if (!checkTwice(context)) {
            return 0;
        }

        assert context.getSource().getEntity() instanceof ServerPlayer;

        var player = (ServerPlayer) context.getSource().getEntity();
        var pos = context.getArgument("block_pos", BlockPos.class);
        NBTEditNetworking.getInstance().sendTileNBTToClient(player, pos);

        NBTEdit.getInstance().getInternalLogger().info("Player " + player.getName() +
                " issued command /nbtedit " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + ".");

        return 1;
    }

    private static boolean checkTwice(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(new TextComponent(TranslatableLanguage.get()
                    .getOrDefault(TranslateKeys.MESSAGE_NOT_PLAYER.getKey())).withStyle(ChatFormatting.RED));
            return false;
        }

        if (!PermissionHelper.checkPermission(source)) {
            source.sendFailure(new TextComponent(TranslatableLanguage.get()
                    .getOrDefault(TranslateKeys.MESSAGE_NO_PERMISSION.getKey())).withStyle(ChatFormatting.RED));
            NBTEdit.getInstance().getInternalLogger().info(
                    "Player " + player.getName().getContents() + " tried use NBTEdit with no permission.");
            return false;
        }

        return true;
    }
}
