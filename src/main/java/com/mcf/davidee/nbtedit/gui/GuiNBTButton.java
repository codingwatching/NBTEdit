package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class GuiNBTButton extends Widget {

	public static final int WIDTH = 9, HEIGHT = 9;

	private final Minecraft minecraft = Minecraft.getInstance();

	private byte id;
	private int x, y;
	private boolean enabled;

	private long hoverTime;

	public GuiNBTButton(byte id, int x, int y) {
		super(x, y, WIDTH, HEIGHT, new StringTextComponent(""));
		this.id = id;
		this.x = x;
		this.y = y;
	}

	public void render(MatrixStack matrixStack, int mx, int my, float p_230430_4_) {

		if (inBounds(mx, my)) {//checks if the mouse is over the button
			AbstractGui.fill(matrixStack, x, y, x + WIDTH, y + HEIGHT, 0x80ffffff); //draw a grayish background
			//Gui.draw(matrixStack, x, y, x + WIDTH, y + HEIGHT, 0x80ffffff); //draw a grayish background
			if (hoverTime == -1)
				hoverTime = System.currentTimeMillis();
		} else
			hoverTime = -1;

		//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (enabled) {
			minecraft.getTextureManager().bind(GuiNBTNode.WIDGET_TEXTURE);
			GuiUtils.drawTexturedModalRect(matrixStack, x, y, (id - 1) * 9, 18, WIDTH, HEIGHT, 0);//Draw the texture
		}

		if (hoverTime != -1 && System.currentTimeMillis() - hoverTime > 300) {
			drawToolTip(matrixStack, mx, my);
		}

	}

	private void drawToolTip(MatrixStack matrixStack, int mx, int my) {
		String s = NBTStringHelper.getButtonName(id);
		int width = minecraft.font.width(s);
		AbstractGui.fill(matrixStack, mx + 4, my + 7, mx + 5 + width, my + 17, 0xff000000);
		minecraft.font.draw(matrixStack, s, mx + 5, my + 8, 0xffffff);
	}

	public void setEnabled(boolean aFlag) {
		enabled = aFlag;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean inBounds(int mx, int my) {
		return enabled && mx >= x && my >= y && mx < x + WIDTH && my < y + HEIGHT;
	}

	public boolean inBounds(double mx, double my) {
		return enabled && mx >= x && my >= y && mx < x + WIDTH && my < y + HEIGHT;
	}

	public byte getId() {
		return id;
	}
}
