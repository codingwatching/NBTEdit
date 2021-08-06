package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.utility.nbt.ClipboardStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.util.Mth;

public class GuiSaveSlotButton extends Gui {
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");
	private static final int X_SIZE = 14, HEIGHT = 20, MAX_WIDTH = 150, MIN_WIDTH = 82, GAP = 3;
	private final Minecraft mc;
	public final ClipboardStates.Clipboard save;
	private final int rightX;

	private int x;
	private int y;
	private int width;
	private String text;
	private boolean xVisible;

	private int tickCount;

	public GuiSaveSlotButton(ClipboardStates.Clipboard save, int rightX, int y) {
		super(Minecraft.getInstance());

		this.save = save;
		this.rightX = rightX;
		this.y = y;
		mc = Minecraft.getInstance();
		xVisible = !save.tag.isEmpty();
		text = (save.tag.isEmpty() ? "Save " : "Load ") + save.name;	// Todo: AS: I18n here.
		tickCount = -1;
		updatePosition();
	}

	public Minecraft getMinecraft() {
		return mc;
	}

	public void draw(PoseStack stack, int xIn, int yIn) {
		int textColor = ((inBounds(xIn, yIn))) ? 16777120 : 0xffffff;
		renderVanillaButton(stack, x, y, 0, 66, width, HEIGHT);
		drawCenteredString(stack, getMinecraft().font, text, x + width / 2, y + 6, textColor);
		if (tickCount != -1 && tickCount / 6 % 2 == 0) {
			getMinecraft().font.drawShadow(stack, "_",
					x + (width + getMinecraft().font.width(text)) / 2 + 1, y + 6, 0xffffff);
		}

		if (xVisible) {
			textColor = ((inBoundsOfX(xIn, yIn))) ? 16777120 : 0xffffff;
			renderVanillaButton(stack, leftBoundOfX(), topBoundOfX(), 0, 66, X_SIZE, X_SIZE);
			drawCenteredString(stack, getMinecraft().font, "x",
					x - GAP - X_SIZE / 2, y + 6, textColor);
		}
	}

	private void renderVanillaButton(PoseStack stack, int x, int y, int u, int v, int width, int height) {
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.textureManager.bindForSetup(TEXTURE);

		//Top Left
		blit(stack, x, y, u, v, width / 2, height / 2);
		//Top Right 
		blit(stack, x + width / 2, y, u + 200 - width / 2, v, width / 2, height / 2);
		//Bottom Left
		blit(stack, x, y + height / 2, u, v + 20 - height / 2, width / 2, height / 2);
		//Bottom Right
		blit(stack, x + width / 2, y + height / 2, u + 200 - width / 2, v + 20 - height / 2, width / 2, height / 2);
	}

	private int leftBoundOfX() {
		return x - X_SIZE - GAP;
	}

	private int topBoundOfX() {
		return y + (HEIGHT - X_SIZE) / 2;
	}

	public boolean inBoundsOfX(double xIn, double yIn) {
		int buttonX = leftBoundOfX();
		int buttonY = topBoundOfX();
		return xVisible && xIn >= buttonX && yIn >= buttonY && xIn < buttonX + X_SIZE && yIn < buttonY + X_SIZE;
	}

	public boolean inBounds(double xIn, double yIn) {
		return xIn >= x && yIn >= y && xIn < x + width && yIn < y + HEIGHT;
	}

	private void updatePosition() {
		width = getMinecraft().font.width(text) + 24;
		if (width % 2 == 1)
			++width;
		width = Mth.clamp(width, MIN_WIDTH, MAX_WIDTH);
		x = rightX - width;
	}

	public void reset() {
		xVisible = false;
		save.tag = new CompoundTag();
		text = "Save " + save.name;
		updatePosition();
	}

	public void saved() {
		xVisible = true;
		text = "Load " + save.name;
		updatePosition();
	}

	public void keyTyped(char c, int key) {
		if (key == InputConstants.KEY_BACKSPACE) {
			backSpace();
		}

		if (Character.isDigit(c) || Character.isLetter(c)) {
			save.name += c;
			text = (save.tag.isEmpty() ? "Save " : "Load ") + save.name;	// Todo: AS: I18n here.
			updatePosition();
		}
	}


	public void backSpace() {
		if (save.name.length() > 0) {
			save.name = save.name.substring(0, save.name.length() - 1);
			text = (save.tag.isEmpty() ? "Save " : "Load ") + save.name;	// Todo: AS: I18n here.
			updatePosition();
		}
	}

	public void startEditing() {
		tickCount = 0;
	}

	public void stopEditing() {
		tickCount = -1;
	}

	public void update() {
		++tickCount;
	}
}
