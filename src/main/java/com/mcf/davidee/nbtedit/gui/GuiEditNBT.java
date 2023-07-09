package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.Node;
import com.mcf.davidee.nbtedit.nbt.ParseHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * The node editor popup window.
 */
public class GuiEditNBT extends Widget {

	public static final ResourceLocation WINDOW_TEXTURE = new ResourceLocation(NBTEdit.MODID, "textures/gui/window.png");

	public static final int WIDTH = 178, HEIGHT = 93;

	private final Minecraft minecraft = Minecraft.getInstance();
	private Node<NamedNBT> node;
	private INBT nbt;
	private boolean canEditText, canEditValue;
	private GuiNBTTree parent;

	private GuiTextField key, value;
	private Button save, cancel;
	private String kError, vError;

	private GuiCharacterButton newLine, section;

	public GuiEditNBT(GuiNBTTree parent, Node<NamedNBT> node, boolean editText, boolean editValue) {
		super(0, 0, WIDTH, HEIGHT, StringTextComponent.EMPTY);
		this.parent = parent;
		this.node = node;
		this.nbt = node.getObject().getNBT();
		canEditText = editText;
		canEditValue = editValue;
	}

	public void initGUI(int x, int y) {
		this.x = x;
		this.y = y;

		section = new GuiCharacterButton((byte) 0, x + WIDTH - 1, y + 34);
		newLine = new GuiCharacterButton((byte) 1, x + WIDTH - 1, y + 50);
		String sKey = (key == null) ? node.getObject().getName() : key.getText();
		String sValue = (value == null) ? getValue(nbt) : value.getText();
		this.key = new GuiTextField(minecraft.font, x + 46, y + 18, 116, 15, false);
		this.value = new GuiTextField(minecraft.font, x + 46, y + 44, 116, 15, true);

		key.setText(sKey);
		key.setEnableBackgroundDrawing(false);
		key.setEditable(canEditText);
		value.setMaxStringLength(256);
		value.setText(sValue);
		value.setEnableBackgroundDrawing(false);
		value.setEditable(canEditValue);
		save = new Button(x + 9, y + 62, 75, 20, new StringTextComponent("Save"), button -> saveAndQuit());
		if (!key.isFocused() && !value.isFocused()) {
			if (canEditText) {
				key.setFocused(true);
			} else if (canEditValue) {
				value.setFocused(true);
			}
		}
		section.setEnabled(value.isFocused());
		newLine.setEnabled(value.isFocused());
		cancel = new Button(x + 93, y + 62, 75, 20, new StringTextComponent("Cancel"), button -> parent.closeWindow());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (newLine.inBounds(mx, my) && value.isFocused()) {
			value.insertText("\n");
			checkValidInput();
			return true;
		} else if (section.inBounds(mx, my) && value.isFocused()) {
			value.insertText("" + NBTStringHelper.SECTION_SIGN);
			checkValidInput();
			return true;
		} else if (save.mouseClicked(mx, my, button)) {
			save.onPress();
			return true;
		} else if (cancel.mouseClicked(mx, my, button)) {
			cancel.onPress();
			return true;
		} else {
			boolean retur = false;
			if (key.mouseClicked(mx, my, button)) {
				value.setFocused(false);
				retur = true;
			}
			if (value.mouseClicked(mx, my, button)) {
				key.setFocused(false);
				retur = true;
			}
			section.setEnabled(value.isFocused());
			newLine.setEnabled(value.isFocused());
			return retur || super.mouseClicked(mx, my, button);
		}
	}

	private void saveAndQuit() {
		if (canEditText) {
			node.getObject().setName(key.getText());
		}
		setValidValue(node, value.getText());
		parent.nodeEdited(node);
		parent.closeWindow();
	}

	@Override
	public void render(MatrixStack matrixStack, int mx, int my, float particleTicks) {
		minecraft.getTextureManager().bind(WINDOW_TEXTURE);

		GL11.glColor4f(1, 1, 1, 1);
		GuiUtils.drawTexturedModalRect(matrixStack, x, y, 0, 0, WIDTH, HEIGHT, 0);
		if (!canEditText)
			fill(matrixStack, x + 42, y + 15, x + 169, y + 31, 0x80000000);
		if (!canEditValue)
			fill(matrixStack, x + 42, y + 41, x + 169, y + 57, 0x80000000);
		key.drawTextBox(matrixStack);
		value.drawTextBox(matrixStack);

		save.render(matrixStack, mx, my, 0);
		cancel.render(matrixStack, mx, my, 0);

		if (kError != null)
			drawCenteredString(matrixStack, minecraft.font, kError, x + WIDTH / 2, y + 4, 0xFF0000);
		if (vError != null)
			drawCenteredString(matrixStack, minecraft.font, vError, x + WIDTH / 2, y + 32, 0xFF0000);

		newLine.render(matrixStack, mx, my, particleTicks);
		section.render(matrixStack, mx, my, particleTicks);
	}

	public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, int par3, int par4, int par5) {
		fontRenderer.draw(matrixStack, text, par3 - fontRenderer.width(text) / 2f, par4, par5);
	}

	public void update() {
		value.updateCursorCounter();
		key.updateCursorCounter();
	}

	@Override
	public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			parent.closeWindow();
			return true;
		} else if (key == GLFW.GLFW_KEY_TAB) {
			if (this.key.isFocused() && canEditValue) {
				this.key.setFocused(false);
				value.setFocused(true);
			} else if (value.isFocused() && canEditText) {
				this.key.setFocused(true);
				value.setFocused(false);
			}
			section.setEnabled(value.isFocused());
			newLine.setEnabled(value.isFocused());
		} else if (key == GLFW.GLFW_KEY_ENTER) {
			checkValidInput();
			if (save.active) {
				saveAndQuit();
			}
		} else {
			this.key.keyPressed(key, p_231046_2_, p_231046_3_);
			value.keyPressed(key, p_231046_2_, p_231046_3_);
			checkValidInput();
		}
		return super.keyPressed(key, p_231046_2_, p_231046_3_);
	}

	@Override
	public boolean charTyped(char p_231042_1_, int p_231042_2_) {
		if (this.key.isFocused()) {
			boolean handled = key.charTyped(p_231042_1_, p_231042_2_);
			checkValidInput();
			return handled;
		} else if (value.isFocused()) {
			boolean handled = value.charTyped(p_231042_1_, p_231042_2_);
			checkValidInput();
			return handled;
		}
		return false;
	}

	private void checkValidInput() {
		boolean valid = true;
		kError = null;
		vError = null;
		if (canEditText && !validName()) {
			valid = false;
			kError = "Duplicate Tag Name";
		}
		try {
			validValue(value.getText(), nbt.getId());
			valid &= true;
		} catch (NumberFormatException e) {
			vError = e.getMessage();
			valid = false;
		}
		save.active = valid;
	}

	private boolean validName() {
		for (Node<NamedNBT> node : this.node.getParent().getChildren()) {
			INBT base = node.getObject().getNBT();
			if (base != nbt && node.getObject().getName().equals(key.getText()))
				return false;
		}
		return true;
	}

	private static void setValidValue(Node<NamedNBT> node, String value) {
		NamedNBT named = node.getObject();
		INBT base = named.getNBT();

		if (base instanceof ByteNBT)
			named.setNBT(ByteNBT.valueOf(ParseHelper.parseByte(value)));
		if (base instanceof ShortNBT)
			named.setNBT(ShortNBT.valueOf(ParseHelper.parseShort(value)));
		if (base instanceof IntNBT)
			named.setNBT(IntNBT.valueOf(ParseHelper.parseInt(value)));
		if (base instanceof LongNBT)
			named.setNBT(LongNBT.valueOf(ParseHelper.parseLong(value)));
		if (base instanceof FloatNBT)
			named.setNBT(FloatNBT.valueOf(ParseHelper.parseFloat(value)));
		if (base instanceof DoubleNBT)
			named.setNBT(DoubleNBT.valueOf(ParseHelper.parseDouble(value)));
		if (base instanceof ByteArrayNBT)
			named.setNBT(new ByteArrayNBT(ParseHelper.parseByteArray(value)));
		if (base instanceof IntArrayNBT)
			named.setNBT(new IntArrayNBT(ParseHelper.parseIntArray(value)));
		if (base instanceof StringNBT)
			named.setNBT(StringNBT.valueOf(value));
	}

	private static void validValue(String value, byte type) throws NumberFormatException {
		switch (type) {
			case 1:
				ParseHelper.parseByte(value);
				break;
			case 2:
				ParseHelper.parseShort(value);
				break;
			case 3:
				ParseHelper.parseInt(value);
				break;
			case 4:
				ParseHelper.parseLong(value);
				break;
			case 5:
				ParseHelper.parseFloat(value);
				break;
			case 6:
				ParseHelper.parseDouble(value);
				break;
			case 7:
				ParseHelper.parseByteArray(value);
				break;
			case 11:
				ParseHelper.parseIntArray(value);
				break;
		}
	}

	private static String getValue(INBT base) {
		switch (base.getId()) {
			case 7:
				StringBuilder s = new StringBuilder();
				for (byte b : ((ByteArrayNBT) base).getAsByteArray()) {
					s.append(b).append(" ");
				}
				return s.toString().trim();
			case 9:
				return "TagList";
			case 10:
				return "TagCompound";
			case 11:
				StringBuilder i = new StringBuilder();
				for (int a : ((IntArrayNBT) base).getAsIntArray()) {
					i.append(a).append(" ");
				}
				return i.toString().trim();
			case 12:
				StringBuilder j = new StringBuilder();
				for (long a : ((LongArrayNBT) base).getAsLongArray()) {
					j.append(a).append(" ");
				}
				return j.toString().trim();
			default:
				return NBTStringHelper.toString(base);
		}
	}

}
