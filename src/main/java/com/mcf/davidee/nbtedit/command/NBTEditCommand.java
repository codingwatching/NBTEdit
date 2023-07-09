package com.mcf.davidee.nbtedit.command;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.packets.MouseOverPacket;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.Level;

public class NBTEditCommand extends CommandBase {
	public static final NBTEditCommand INSTANCE = new NBTEditCommand();

	public NBTEditCommand() {
		super("nbtedit");
	}

	@Override
	public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
		builder
			.executes(this::mouse)
			.then(argument("entity", EntityArgument.entity())
				.executes(this::entity)
			)
			.then(argument("tile", BlockPosArgument.blockPos())
				.executes(this::tile)
			);
	}

	public int mouse(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = context.getSource().getPlayerOrException();

		NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/nbtedit\"");
		NBTEdit.NETWORK.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MouseOverPacket());
		return 1;
	}

	public int entity(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		Entity target = EntityArgument.getEntity(context, "entity");

		int entityID = target.getId();
		NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/nbtedit " + entityID + "\"");
		NBTEdit.NETWORK.sendEntity(player, entityID);
		return 1;
	}

	public int tile(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "tile");

		NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/nbtedit " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "\"");
		NBTEdit.NETWORK.sendTile(player, pos);
		return 1;
	}
}
