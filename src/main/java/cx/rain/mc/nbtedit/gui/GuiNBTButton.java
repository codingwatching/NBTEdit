package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class GuiNBTButton extends Gui {
	public static final int WIDTH = 9;
	public static final int HEIGHT = 9;

	private Minecraft mc = Minecraft.getInstance();

	private byte id;
	private int x;
	private int y;
	private boolean enabled;

	private long hoverTime;

	public GuiNBTButton(byte id, int x, int y) {
		super(Minecraft.getInstance());

		this.id = id;
		this.x = x;
		this.y = y;
	}

	public void draw(PoseStack stack, int xIn, int yIn) {
		mc.textureManager.bindForSetup(GuiNBTNode.WIDGET_TEXTURE);

		if (inBounds(xIn, yIn)) {//checks if the mouse is over the button
			Gui.fill(stack, x, y, x + WIDTH, y + HEIGHT, 0x80ffffff);//draw a grayish background
			if (hoverTime == -1)
				hoverTime = System.currentTimeMillis();
		} else
			hoverTime = -1;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (enabled)
			blit(stack, x, y, (id - 1) * 9, 18, WIDTH, HEIGHT);//Draw the texture

		if (hoverTime != -1 && System.currentTimeMillis() - hoverTime > 300) {
			drawToolTip(stack, xIn, yIn);
		}
	}

	private void drawToolTip(PoseStack stack, int xIn, int yIn) {
		String s = NBTHelper.getButtonName(id);
		int width = mc.font.width(s);
		fill(stack, xIn + 4, yIn + 7, xIn + 5 + width, yIn + 17, 0xff000000);
		mc.font.draw(stack, s, xIn + 5, yIn + 8, 0xffffff);
	}

	public void setEnabled(boolean aFlag) {
		enabled = aFlag;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean inBounds(double xIn, double yIn) {
		return enabled && xIn >= x && yIn >= y && xIn < x + WIDTH && yIn < y + HEIGHT;
	}

	public byte getId() {
		return id;
	}
}
