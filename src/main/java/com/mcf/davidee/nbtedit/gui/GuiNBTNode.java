package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.Node;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

/**
 * Individual node displayed in the tree.
 * Renders the Icon and the Text name of the nbt node.
 */
public class GuiNBTNode extends Widget {
	public static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(NBTEdit.MODID, "textures/gui/widgets.png");
	private final Minecraft minecraft = Minecraft.getInstance();

	private Node<NamedNBT> node;
	private GuiNBTTree tree;

	private String displayString;

	public GuiNBTNode(GuiNBTTree tree, Node<NamedNBT> node, int x, int y) {
		super(x, y, 200, 0, StringTextComponent.EMPTY);
		this.tree = tree;
		this.node = node;
		this.height = minecraft.font.lineHeight;
		updateDisplay();
	}

	private boolean inBounds(double mx, double my) {
		return mx >= x && my >= y && mx < width + x && my < height + y;
	}

	private boolean inHideShowBounds(double mx, double my) {
		return mx >= x - 9 && my >= y && mx < x && my < y + height;
	}

	public boolean shouldDrawChildren() {
		return node.shouldDrawChildren();
	}

	public boolean clicked(double mx, double my) {
		return inBounds(mx, my);
	}

	public boolean hideShowClicked(double mx, double my) {
		if (node.hasChildren() && inHideShowBounds(mx, my)) {
			node.setDrawChildren(!node.shouldDrawChildren());
			return true;
		}
		return false;
	}

	public Node<NamedNBT> getNode() {
		return node;
	}

	public void shift(int dy) {
		y += dy;
	}

	public void updateDisplay() {
		displayString = NBTStringHelper.getNBTNameSpecial(node.getObject());
		width = minecraft.font.width(displayString) + 12;
	}

	public void render(MatrixStack matrixStack, int mx, int my, float partialTicks) {
		boolean selected = tree.getFocused() == node;
		boolean hover = inBounds(mx, my);
		boolean chHover = inHideShowBounds(mx, my);
		int color = selected ? 0xff : hover ? 16777120 : (node.hasParent()) ? 14737632 : -6250336;

		minecraft.getTextureManager().bind(WIDGET_TEXTURE);

		if (selected) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			AbstractGui.fill(matrixStack, x + 11, y, x + width, y + height, Integer.MIN_VALUE);
		}
		if (node.hasChildren()) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiUtils.drawTexturedModalRect(matrixStack, x - 9, y, (node.shouldDrawChildren()) ? 9 : 0, (chHover) ? height : 0, 9, height, 0);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GuiUtils.drawTexturedModalRect(matrixStack, x + 1, y, (node.getObject().getNBT().getId() - 1) * 9, 18, 9, 9, 0);
		drawString(matrixStack, minecraft.font, displayString, x + 11, y + (this.height - 8) / 2, color);
	}

	public boolean shouldDraw(int top, int bottom) {
		return y + height >= top && y <= bottom;
	}
}
