package cx.rain.mc.nbtedit.gui;

import cx.rain.mc.nbtedit.nbt.NBTTree;
import cx.rain.mc.nbtedit.networking.packet.C2SEntityNBTSavePacket;
import cx.rain.mc.nbtedit.networking.packet.C2STileNBTSavePacket;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import cx.rain.mc.nbtedit.NBTEdit;

import java.io.IOException;

public class GuiEditNBTTree extends Screen {
	public final int entityOrX, y, z;
	private boolean entity;
	protected String screenTitle;
	private GuiNBTTree guiTree;

	public GuiEditNBTTree(int entity, CompoundTag tag) {
		super();
		this.entity = true;
		entityOrX = entity;
		y = 0;
		z = 0;
		screenTitle = "NBTEdit -- EntityId #" + entityOrX;
		guiTree = new GuiNBTTree(new NBTTree(tag));
	}

	public GuiEditNBTTree(BlockPos pos, CompoundTag tag) {
		this.entity = false;
		entityOrX = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		screenTitle = "NBTEdit -- TileEntity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
		guiTree = new GuiNBTTree(new NBTTree(tag));
	}

	@SuppressWarnings("unchecked")
	public void initGui() {
		minecraft.keyboardHandler.setSendRepeatsToGui(true);
		buttonList.clear();
		guiTree.initGUI(width, height, height - 35);
		this.buttonList.add(new GuiButton(1, width / 4 - 100, this.height - 27, "Save"));
		this.buttonList.add(new GuiButton(0, width * 3 / 4 - 100, this.height - 27, "Quit"));
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void keyTyped(char par1, int key) {
		GuiEditNBT window = guiTree.getWindow();
		if (window != null)
			window.keyTyped(par1, key);
		else {
			if (key == 1) {
				if (guiTree.isEditingSlot())
					guiTree.stopEditingSlot();
				else
					quitWithoutSaving();
			} else if (key == Keyboard.KEY_DELETE)
				guiTree.deleteSelected();
			else if (key == Keyboard.KEY_RETURN)
				guiTree.editSelected();
			else if (key == Keyboard.KEY_UP)
				guiTree.arrowKeyPressed(true);
			else if (key == Keyboard.KEY_DOWN)
				guiTree.arrowKeyPressed(false);
			else
				guiTree.keyTyped(par1, key);
		}
	}

	protected void mouseClicked(int x, int y, int t) throws IOException {
		if (guiTree.getWindow() == null)
			super.mouseClicked(x, y, t);
		if (t == 0)
			guiTree.mouseClicked(x, y);
		if (t == 1)
			guiTree.rightClick(x, y);
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int ofs = Mouse.getEventDWheel();

		if (ofs != 0) {
			guiTree.shift((ofs >= 1) ? 6 : -6);
		}

	}

	protected void actionPerformed(GuiButton b) {
		if (b.enabled) {
			switch (b.id) {
				case 1:
					quitWithSave();
					break;
				default:
					quitWithoutSaving();
					break;
			}
		}
	}

	public void updateScreen() {
		if (!mc.player.isEntityAlive())
			quitWithoutSaving();
		else
			guiTree.updateScreen();
	}

	private void quitWithSave() {
		if (entity)
			NBTEdit.NETWORK.INSTANCE.sendToServer(new C2SEntityNBTSavePacket(entityOrX, guiTree.getNBTTree().toCompoundTag()));
		else
			NBTEdit.NETWORK.INSTANCE.sendToServer(new C2STileNBTSavePacket(new BlockPos(entityOrX, y, z), guiTree.getNBTTree().toCompoundTag()));
		mc.displayGuiScreen(null);
		mc.setIngameFocus();

	}

	private void quitWithoutSaving() {
		mc.displayGuiScreen(null);
	}

	public void drawScreen(int x, int y, float par3) {
		this.drawDefaultBackground();
		guiTree.draw(x, y);
		this.drawCenteredString(mc.fontRenderer, this.screenTitle, this.width / 2, 5, 16777215);
		if (guiTree.getWindow() == null)
			super.drawScreen(x, y, par3);
		else
			super.drawScreen(-1, -1, par3);
	}

	public boolean doesGuiPauseGame() {
		return true;
	}

	public Entity getEntity() {
		return entity ? mc.world.getEntityByID(entityOrX) : null;
	}

	public boolean isTileEntity() {
		return !entity;
	}

	public int getBlockX() {
		return entity ? 0 : entityOrX;
	}

}
