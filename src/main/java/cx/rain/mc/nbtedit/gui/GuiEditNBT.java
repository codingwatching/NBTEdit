package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.Node;
import cx.rain.mc.nbtedit.utility.nbt.ParseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiEditNBT extends Gui {

	public static final ResourceLocation WINDOW_TEXTURE = new ResourceLocation(NBTEdit.MODID, "textures/gui/window.png");

	public static final int WIDTH = 178, HEIGHT = 93;

	private Minecraft mc = Minecraft.getInstance();
	private Node<NamedNBT> node;
	private Tag nbt;
	private boolean canEditText, canEditValue;
	private GuiNBTTree parent;

	private int x, y;


	private GuiTextField key, value;
	private Button save, cancel;
	private String kError, vError;

	private GuiCharacterButton newLine, section;


	public GuiEditNBT(GuiNBTTree parent, Node<NamedNBT> node, boolean editText, boolean editValue) {
		super(Minecraft.getInstance());

		this.parent = parent;
		this.node = node;
		this.nbt = node.getObject().getTag();
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
		this.key = new GuiTextField(mc.font, x + 46, y + 18, 116, 15, false);
		this.value = new GuiTextField(mc.font, x + 46, y + 44, 116, 15, true);

		key.setText(sKey);
		key.setEnableBackgroundDrawing(false);
		key.func_82265_c(canEditText);
		value.setMaxStringLength(256);
		value.setText(sValue);
		value.setEnableBackgroundDrawing(false);
		value.func_82265_c(canEditValue);
		save = new Button(x + 9, y + 62, 75, 20,
				new TextComponent("Save"), this::onSave);	// Todo: AS: I18n here.
		
		if (!key.isFocused() && !value.isFocused()) {
			if (canEditText) {
				key.setFocused(true);
			}
			else if (canEditValue) {
				value.setFocused(true);
			}
		}

		section.setEnabled(value.isFocused());
		newLine.setEnabled(value.isFocused());
		cancel = new Button(x + 93, y + 62, 75, 20,
				new TextComponent("Cancel"), this::onCancel);	// Todo: AS: I18n here.
	}

	protected void onSave(Button button) {
		click(button.x, button.y);
		saveAndQuit();
	}

	protected void onCancel(Button button) {
		click(button.x, button.y);
		parent.closeWindow();
	}

	public void click(double xIn, double yIn) {
		if (newLine.inBounds(xIn, yIn) && value.isFocused()) {
			value.writeText("\n");
			checkValidInput();
		} else if (section.inBounds(xIn, yIn) && value.isFocused()) {
			value.writeText("" + NBTHelper.SECTION_SIGN);
			checkValidInput();
		} else {
			key.mouseClicked(xIn, yIn, 0);
			value.mouseClicked(xIn, yIn, 0);
			section.setEnabled(value.isFocused());
			newLine.setEnabled(value.isFocused());
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

	public void draw(PoseStack stack, int xIn, int yIn) {
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/nbtedit_textures/nbteditwindow.png"));
		mc.textureManager.bindForSetup(WINDOW_TEXTURE);

		GL11.glColor4f(1, 1, 1, 1);
		blit(stack, x, y, 0, 0, WIDTH, HEIGHT);
		if (!canEditText) {
			fill(stack, x + 42, y + 15, x + 169, y + 31, 0x80000000);
		}
		if (!canEditValue) {
			fill(stack, x + 42, y + 41, x + 169, y + 57, 0x80000000);
		}

		key.drawTextBox(stack);
		value.drawTextBox(stack);

		save.renderButton(stack, xIn, yIn, 0);
		cancel.renderButton(stack, xIn, yIn, 0);

		if (kError != null) {
			drawCenteredString(mc.font, stack, kError, x + WIDTH / 2, y + 4, 0xFF0000);
		}
		if (vError != null) {
			drawCenteredString(mc.font, stack, vError, x + WIDTH / 2, y + 32, 0xFF0000);
		}

		newLine.draw(xIn, yIn);
		section.draw(xIn, yIn);
	}

	public void drawCenteredString(Font par1FontRenderer, PoseStack stack, String par2Str, int par3, int par4, int par5) {
		par1FontRenderer.draw(stack, par2Str, par3 - par1FontRenderer.width(par2Str) / 2, par4, par5);
	}

	public void update() {
		value.updateCursorCounter();
		key.updateCursorCounter();
	}

	public void charTyped(char character, int keyId) {
		if (keyId == InputConstants.KEY_ESCAPE) {
			parent.closeWindow();
		} else if (keyId == InputConstants.KEY_TAB) {
			if (key.isFocused() && canEditValue) {
				key.setFocused(false);
				value.setFocused(true);
			} else if (value.isFocused() && canEditText) {
				key.setFocused(true);
				value.setFocused(false);
			}
			section.setEnabled(value.isFocused());
			newLine.setEnabled(value.isFocused());
		} else if (keyId == InputConstants.KEY_RETURN) {
			checkValidInput();
			if (save.isActive())
				saveAndQuit();
		} else {
			key.textBoxCharTyped(character, keyId);
			value.textBoxCharTyped(character, keyId);
			checkValidInput();
		}
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
			Tag base = node.getObject().getTag();
			if (base != nbt && node.getObject().getName().equals(key.getText()))
				return false;
		}
		return true;
	}

	private static void setValidValue(Node<NamedNBT> node, String value) {
		NamedNBT named = node.getObject();
		Tag base = named.getTag();

		if (base instanceof ByteTag) {
			named.setTag(ByteTag.valueOf(ParseHelper.parseByte(value)));
		}
		if (base instanceof ShortTag) {
			named.setTag(ShortTag.valueOf(ParseHelper.parseShort(value)));
		}
		if (base instanceof IntTag) {
			named.setTag(IntTag.valueOf(ParseHelper.parseInt(value)));
		}
		if (base instanceof LongTag) {
			named.setTag(LongTag.valueOf(ParseHelper.parseLong(value)));
		}
		if (base instanceof FloatTag) {
			named.setTag(FloatTag.valueOf(ParseHelper.parseFloat(value)));
		}
		if (base instanceof DoubleTag) {
			named.setTag(DoubleTag.valueOf(ParseHelper.parseDouble(value)));
		}
		if (base instanceof ByteArrayTag) {
			named.setTag(new ByteArrayTag(ParseHelper.parseByteArray(value)));
		}
		if (base instanceof IntArrayTag) {
			named.setTag(new IntArrayTag(ParseHelper.parseIntArray(value)));
		}
		if (base instanceof StringTag) {
			named.setTag(StringTag.valueOf(value));
		}
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

	private static String getValue(Tag base) {
		switch (base.getId()) {
			case 7:
				String s = "";
				for (byte b : ((ByteArrayTag) base).getAsByteArray()) {
					s += b + " ";
				}
				return s;
			case 9:
				return "TagList";
			case 10:
				return "TagCompound";
			case 11:
				String i = "";
				for (int a : ((IntArrayTag) base).getAsIntArray()) {
					i += a + " ";
				}
				return i;
			default:
				return NBTHelper.toString(base);
		}
	}
}
