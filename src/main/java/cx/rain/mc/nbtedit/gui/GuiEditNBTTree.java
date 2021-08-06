package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.utility.nbt.NBTTree;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import cx.rain.mc.nbtedit.networking.packet.C2SEntityNBTSavePacket;
import cx.rain.mc.nbtedit.networking.packet.C2STileNBTSavePacket;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.TranslatableComponent;

import java.util.UUID;

public class GuiEditNBTTree extends Screen {
	protected UUID uuid;
	protected BlockPos pos;

	protected boolean isMe;

	protected boolean isEntity;

	protected String screenTitle;
	protected GuiNBTTree guiNbtTree;

	public GuiEditNBTTree(UUID uuidIn, CompoundTag tag, boolean isMeIn) {
		super(new TranslatableComponent(TranslateKeys.TITLE_NBTEDIT_ENTITY_GUI.getKey(), uuidIn));
		isEntity = true;
		uuid = uuidIn;
		isMe = isMeIn;

		guiNbtTree = new GuiNBTTree(new NBTTree(tag));
	}

	public GuiEditNBTTree(BlockPos posIn, CompoundTag tag) {
		super(new TranslatableComponent(TranslateKeys.TITLE_NBTEDIT_TILE_GUI.getKey(),
				posIn.getX(), posIn.getY(), posIn.getZ()));
		isEntity = false;
		pos	= posIn;

		guiNbtTree = new GuiNBTTree(new NBTTree(tag));
	}

	@Override
	protected void init() {
		super.init();

		setMinecraft(Minecraft.getInstance());

		getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
		renderables.clear();
		guiNbtTree.init(width, height, height - 35);
		renderables.add(new Button(width / 4 - 100, height - 27, width / 2, 27,
				new TranslatableComponent(TranslateKeys.BUTTON_SAVE.getKey()), this::onSaveButtonClicked));
		renderables.add(new Button(width * 3 / 4 - 100, height - 27, width / 2, 27,
				new TranslatableComponent(TranslateKeys.BUTTON_QUIT.getKey()), this::onQuitButtonClicked));
	}

	@Override
	public void onClose() {
		super.onClose();

		assert getMinecraft() != null;
		getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean charTyped(char character, int keyId) {
		var window = guiNbtTree.getWindow();
		if (window != null) {
			window.charTyped(character, keyId);
		} else {
			if (keyId == InputConstants.PRESS) {
				if (guiNbtTree.isEditingSlot()) {
					guiNbtTree.stopEditingSlot();
				} else {
					quit(false);
				}
			} else if (keyId == InputConstants.KEY_DELETE) {
				guiNbtTree.deleteSelected();
			} else if (keyId == InputConstants.KEY_RETURN) {
				guiNbtTree.editSelected();
			} else if (keyId == InputConstants.KEY_UP) {
				guiNbtTree.arrowKeyPressed(true);
			} else if (keyId == InputConstants.KEY_DOWN) {
				guiNbtTree.arrowKeyPressed(false);
			} else {
				guiNbtTree.keyTyped(character, keyId);
			}
		}

		return super.charTyped(character, keyId);
	}

	@Override
	public boolean mouseClicked(double x, double y, int keyId) {
		if (guiNbtTree.getWindow() == null) {
			return super.mouseClicked(x, y, keyId);
		}
		if (keyId == InputConstants.MOUSE_BUTTON_LEFT) {
			guiNbtTree.mouseClicked(x, y);
		}
		if (keyId == InputConstants.MOUSE_BUTTON_RIGHT) {
			guiNbtTree.rightClick(x, y);
		}
		return true;
	}

	// FIXME: 2021/8/6 Why these methods not used?
//	public void handleMouseInput() throws IOException {
//		super.handleMouseInput();
//		int ofs = Mouse.getEventDWheel();
//
//		if (ofs != 0) {
//			guiNbtTree.shift((ofs >= 1) ? 6 : -6);
//		}
//
//	}

	@Override
	public void tick() {
		super.tick();

		if (!getMinecraft().player.isAlive()) {
			quit(false);
		}

		guiNbtTree.updateScreen();
	}

	protected void onSaveButtonClicked(Button button) {
		quit(true);
	}

	protected void onQuitButtonClicked(Button button) {
		quit(false);
	}

	protected void quit(boolean saving) {
		if (saving) {
			if (isEntity) {
				NBTEditNetworking.getInstance().getChannel().sendToServer(
						new C2SEntityNBTSavePacket(uuid, guiNbtTree.getNBTTree().toCompound(), isMe));
			} else {
				NBTEditNetworking.getInstance().getChannel().sendToServer(
						new C2STileNBTSavePacket(pos, guiNbtTree.getNBTTree().toCompound()));
			}
		}

		assert getMinecraft() != null;
		getMinecraft().setScreen(null);
		getMinecraft().cursorEntered();
	}

	@Override
	public void render(PoseStack stack, int x, int y, float partialTick) {
		renderBackground(stack);
		guiNbtTree.draw(stack, x, y);
		drawCenteredString(stack, font, getTitle(), width / 2, 5, 19777215);

		if (guiNbtTree.getWindow() == null) {
			super.render(stack, x, y, partialTick);
		} else {
			super.render(stack, -1, -1, partialTick);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public UUID getEntityUuid() {
		return uuid;
	}

	public boolean isTileEntity() {
		return !isEntity;
	}

	public BlockPos getBlockPos() {
		if (isEntity) {
			throw new UnsupportedOperationException("Cannot get block position of an entity!");
		}

		return pos;
	}

	@Override
	public Minecraft getMinecraft() {
		return super.getMinecraft();
	}

	protected void setMinecraft(Minecraft mc) {
		minecraft = mc;
	}
}
