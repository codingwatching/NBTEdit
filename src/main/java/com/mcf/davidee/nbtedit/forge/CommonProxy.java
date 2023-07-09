package com.mcf.davidee.nbtedit.forge;

import com.mcf.davidee.nbtedit.NBTEdit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

import java.io.File;

public class CommonProxy {
	public void registerInformation() {
	}

	public File getMinecraftDirectory() {
		return new File(".");
	}

	public void openEditGUI(int entityID, CompoundNBT tag) {
	}

	public void openEditGUI(BlockPos pos, CompoundNBT tag) {
	}

	public void sendMessage(PlayerEntity player, String message, TextFormatting color) {
		if (player != null) {
			IFormattableTextComponent component = new StringTextComponent(message);
			component.withStyle(color);
			player.sendMessage(component, Util.NIL_UUID);
		}
	}

	public boolean checkPermission(PlayerEntity player) {
		return NBTEdit.opOnly ? PermissionAPI.hasPermission(player, "nbtedit.edit") : player.isCreative();
	}
}
