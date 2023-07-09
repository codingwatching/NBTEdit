package com.mcf.davidee.nbtedit.forge;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.gui.GuiEditNBTTree;
import com.mcf.davidee.nbtedit.nbt.SaveStates;
import com.mcf.davidee.nbtedit.packets.EntityRequestPacket;
import com.mcf.davidee.nbtedit.packets.TileRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.*;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class ClientProxy extends CommonProxy {

	public static KeyBinding NBTEditKey;

	@Override
	public void registerInformation() {
		MinecraftForge.EVENT_BUS.register(this);
		SaveStates save = NBTEdit.getSaveStates();
		save.load();
		save.save();
		NBTEditKey = new KeyBinding("NBTEdit Shortcut", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
		ClientRegistry.registerKeyBinding(NBTEditKey);
	}

	@Override
	public File getMinecraftDirectory() {
		return Minecraft.getInstance().gameDirectory;
	}

	@Override
	public void openEditGUI(final int entityID, final CompoundNBT tag) {
		Minecraft.getInstance().submit(() -> Minecraft.getInstance().setScreen(new GuiEditNBTTree(entityID, tag)));
	}

	@Override
	public void openEditGUI(final BlockPos pos, final CompoundNBT tag) {
		Minecraft.getInstance().submit(() -> Minecraft.getInstance().setScreen(new GuiEditNBTTree(pos, tag)));
	}

	@Override
	public void sendMessage(PlayerEntity player, String message, TextFormatting color) {
		IFormattableTextComponent component = new StringTextComponent(message);
		component.withStyle(color);
		Minecraft.getInstance().gui.getChat().addMessage(component);
	}

	@SubscribeEvent
	public void onKey(InputEvent.KeyInputEvent event) {
		if (NBTEditKey.isDown()) {
			RayTraceResult pos = Minecraft.getInstance().hitResult;
			if (pos != null) {
				if (pos.getType() == RayTraceResult.Type.ENTITY) {
					EntityRayTraceResult entityRay = (EntityRayTraceResult) pos;
					NBTEdit.NETWORK.INSTANCE.sendToServer(new EntityRequestPacket(entityRay.getEntity().getId()));
				} else if (pos.getType() == RayTraceResult.Type.BLOCK) {
					BlockRayTraceResult blockRay = (BlockRayTraceResult) pos;
					NBTEdit.NETWORK.INSTANCE.sendToServer(new TileRequestPacket(blockRay.getBlockPos()));
				} else {
					this.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
				}
			}
		}
	}
}
