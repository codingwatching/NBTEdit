package com.mcf.davidee.nbtedit.command;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Created by Jay113355 on 7/12/2022.
 */
public abstract class CommandBase {
	private final String commandName;
	private final String permissionNode;

	public CommandBase(String commandName) {
		this.commandName = commandName;
		this.permissionNode = NBTEdit.MODID + ".command." + this.commandName();
	}

	public String commandName() {
		return commandName;
	}

	public abstract void buildCommand(LiteralArgumentBuilder<CommandSource> builder);

	public Collection<String> aliases() {
		return Collections.emptySet();
	}

	public boolean hasSubPermission(CommandSource source, String subPermission) {
		return hasPermission(source, permissionNode + "." + subPermission, 4);
	}

	public boolean hasSubPermission(CommandSource source, String subPermission, int level) {
		return hasPermission(source, permissionNode + "." + subPermission, level);
	}

	public static boolean hasPermission(CommandSource source, String permission) {
		return hasPermission(source, permission, 4);
	}

	public static boolean hasPermission(CommandSource source, String permission, int level) {
		if (source.getEntity() instanceof ServerPlayerEntity) {
			return PermissionAPI.hasPermission((PlayerEntity) source.getEntity(), permission);
		} else return source.hasPermission(level);
	}

	@Nonnull
	public static <T> T requireObj(@Nullable T obj, String endMessage) throws CommandException {
		if (obj != null) {
			return obj;
		} else throw getException(endMessage);
	}

	@Nonnull
	public static <O, R extends O> R requireIs(@Nullable O obj, Class<R> is, String endMessage) throws CommandException {
		if (is.isInstance(obj)) {
			return (R) obj;
		} else throw getException(endMessage);
	}

	public static void requireTrue(boolean obj, String endMessage) throws CommandException {
		if (!obj) {
			endCommand(endMessage);
		}
	}

	@Nonnull
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static <T> T requireOpt(Optional<T> opt, String endMessage) throws CommandException {
		if (opt.isPresent()) {
			return opt.get();
		} else throw getException(endMessage);
	}

	@Nonnull
	public static ItemStack requireItem(@Nullable ItemStack stack, String message) throws CommandException {
		if (stack == null || stack.isEmpty()) {
			throw getException(message);
		}
		return stack;
	}

	/**
	 * @param message The later part of a usage message, e.g. Usage: /commandName %message%
	 * @return throws CommandException with a usage message.
	 */
	public int sendUsage(String message) {
		throw getException("Usage: /" + this.commandName() + " " + message);
	}

	/**
	 * Terminates the command with a message
	 * @param message The message to send to the player.
	 * @throws CommandException 100% of the time.
	 */
	public static void endCommand(String message) throws CommandException {
		throw getException(message);
	}

	public static CommandException getException(String message) {
		return new CommandException(message(message)) {
			@Override
			public synchronized Throwable fillInStackTrace() {
				return this;
			}
		};
	}

	public static CompletableFuture<Suggestions> suggestOnline(CommandContext<CommandSource> context, final SuggestionsBuilder builder) {
		ISuggestionProvider provider = context.getSource();
		ISuggestionProvider.suggest(provider.getOnlinePlayerNames(), builder);
		return builder.buildFuture();
	}

	public static IFormattableTextComponent message(String text) {
		return message(text, TextFormatting.RED);
	}

	public static IFormattableTextComponent message(String text, TextFormatting color) {
		IFormattableTextComponent message = new StringTextComponent(text);
		message.withStyle(color);
		return message;
	}

	/**
	 * Shortcut for command building
	 */
	protected static LiteralArgumentBuilder<CommandSource> literal(String literal) {
		return LiteralArgumentBuilder.literal(literal);
	}

	/**
	 * Shortcut for command building
	 */
	protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(String argumentName, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(argumentName, type);
	}

	/**
	 * Shortcut for command building
	 */
	protected static Predicate<CommandSource> permission(String permission) {
		return source -> hasPermission(source, permission);
	}

	/**
	 * Shortcut for command building
	 */
	protected Predicate<CommandSource> subPermission(String permission) {
		return source -> hasSubPermission(source, permission);
	}
}
