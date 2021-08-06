package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class GuiCharacterButton extends Gui {
	public static final int WIDTH = 14, HEIGHT = 14;

	private Minecraft mc = Minecraft.getInstance();
	private byte id;
	private int x, y;
	private boolean enabled;

	public GuiCharacterButton(byte id, int x, int y) {
		super(Minecraft.getInstance());
		this.id = id;
		this.x = x;
		this.y = y;
	}

	public void draw(PoseStack stack, int mx, int my) {
		mc.textureManager.bindForSetup(GuiNBTNode.WIDGET_TEXTURE);
		if (inBounds(mx, my))
			Gui.fill(stack, x, y, x + WIDTH, y + HEIGHT, 0x80ffffff);

		if (enabled) {
			GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			GlStateManager._clearColor(0.5F, 0.5F, 0.5F, 1.0F);
		}

		blit(stack, x, y, id * WIDTH, 27, WIDTH, HEIGHT);
	}

	public void setEnabled(boolean aFlag) {
		enabled = aFlag;
	}

	public boolean inBounds(double mx, double my) {
		return enabled && mx >= x && my >= y && mx < x + WIDTH && my < y + HEIGHT;
	}

	public byte getId() {
		return id;
	}
}
 