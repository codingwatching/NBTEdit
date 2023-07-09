package com.mcf.davidee.nbtedit.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

/**
 * The special color symbol and newline buttons for the text editor.
 */
public class GuiCharacterButton extends Widget {
	public static final int WIDTH = 14, HEIGHT = 14;

	private Minecraft mc = Minecraft.getInstance();
	private byte id;
	private boolean enabled;


	public GuiCharacterButton(byte id, int x, int y) {
		super(x, y, WIDTH, HEIGHT, StringTextComponent.EMPTY);
		this.id = id;
	}

	@Override
	public void render(MatrixStack matrixStack, int mx, int my, float particleTicks) {
		mc.getTextureManager().bind(GuiNBTNode.WIDGET_TEXTURE);
		if (inBounds(mx, my)) {
			Widget.fill(matrixStack, x, y, x + WIDTH, y + HEIGHT, 0x80ffffff);
		}

		if (enabled) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);

		GuiUtils.drawTexturedModalRect(matrixStack, x, y, id * WIDTH, 27, WIDTH, HEIGHT, 0);
	}

	public void setEnabled(boolean aFlag) {
		enabled = aFlag;
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
