package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.nbt.NBTTree;
import com.mcf.davidee.nbtedit.packets.EntitySavePacket;
import com.mcf.davidee.nbtedit.packets.TileSavePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiEditNBTTree extends Screen {

	public final int entityOrX, y, z;
	private boolean entity;
	protected String screenTitle;
	private GuiNBTTree guiTree;

	public GuiEditNBTTree(int entity, CompoundNBT tag) {
		super(StringTextComponent.EMPTY);
		this.entity = true;
		entityOrX = entity;
		y = 0;
		z = 0;
		screenTitle = "NBTEdit -- EntityId #" + entityOrX;
		guiTree = new GuiNBTTree(new NBTTree(tag));
	}

	public GuiEditNBTTree(BlockPos pos, CompoundNBT tag) {
		super(StringTextComponent.EMPTY);
		this.entity = false;
		entityOrX = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		screenTitle = "NBTEdit -- TileEntity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
		guiTree = new GuiNBTTree(new NBTTree(tag));
	}

	@Override
	public void init(Minecraft minecraft, int width, int height) {
		super.init(minecraft, width, height);
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.buttons.clear();
		this.children.clear();
		addButton(new Button(width / 4 - 100, this.height - 27, 40, 20, new StringTextComponent("Save"), this::quitWithSave));
		addButton(new Button(width * 3 / 4 - 100, this.height - 27, 40, 20, new StringTextComponent("Quit"), this::quitWithoutSaving));
		guiTree.initGUI(width, height, height - 35);
		this.setFocused(guiTree);
	}

	@Override
	public void render(MatrixStack matrixStack, int mx, int my, float particleTicks) {
		this.renderBackground(matrixStack);
		guiTree.render(matrixStack, mx, my, particleTicks);
		drawCenteredString(matrixStack, minecraft.font, this.screenTitle, this.width / 2, 5, 16777215);
		if (guiTree.getWindow() == null) {
			super.render(matrixStack, mx, my, particleTicks);
		} else {
			super.render(matrixStack, -1, -1, particleTicks);
		}
	}

	@Override
	public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
		GuiEditNBT window = guiTree.getWindow();
		if (window != null) {
			return window.keyPressed(key, p_231046_2_, p_231046_3_);
		} else {
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				if (guiTree.isEditingSlot()) {
					guiTree.stopEditingSlot();
				} else {
					quitWithoutSaving(null);
				}
			} else {
				return guiTree.keyPressed(key, p_231046_2_, p_231046_3_);
			}
		}
		return super.keyPressed(key, p_231046_2_, p_231046_3_);
	}

	@Override
	public boolean charTyped(char p_231042_1_, int p_231042_2_) {
		GuiEditNBT window = guiTree.getWindow();
		if (window != null) {
			return window.charTyped(p_231042_1_, p_231042_2_);
		} else {
			return guiTree.charTyped(p_231042_1_, p_231042_2_);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (super.mouseClicked(mx, my, button)) {
			return true;
		}
		/*if (guiTree.getWindow() == null) {
			return super.mouseClicked(mx, my, button);
		}*/
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return guiTree.mouseClicked(mx, my, button);
		}
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			guiTree.rightClick(mx, my);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (guiTree.scrollBarActive()) {
			guiTree.shift((int) scroll * guiTree.Y_GAP);
		}
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double xDistance, double yDistance) {
		if (guiTree.scrollBarActive() && guiTree.scrolling()) {
			guiTree.shift((int) -yDistance);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, xDistance, yDistance);
	}

	@Override
	public void tick() {
		if (!minecraft.player.isAlive()) {
			quitWithoutSaving(null);
		} else {
			guiTree.tick();
		}
	}

	@Override
	public void onClose() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	private void quitWithSave(Button p_onPress_1_) {
		if (entity) {
			NBTEdit.NETWORK.INSTANCE.sendToServer(new EntitySavePacket(entityOrX, guiTree.getNBTTree().toNBTTagCompound()));
		} else {
			NBTEdit.NETWORK.INSTANCE.sendToServer(new TileSavePacket(new BlockPos(entityOrX, y, z), guiTree.getNBTTree().toNBTTagCompound()));
		}
		this.onClose();
	}

	private void quitWithoutSaving(Button p_onPress_1_) {
		this.onClose();
	}

	public Entity getEntity() {
		return entity ? minecraft.level.getEntity(entityOrX) : null;
	}

	public boolean isTileEntity() {
		return !entity;
	}

	public int getBlockX() {
		return entity ? 0 : entityOrX;
	}

}
