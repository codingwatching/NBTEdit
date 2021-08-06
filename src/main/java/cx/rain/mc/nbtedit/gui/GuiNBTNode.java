package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiNBTNode extends Gui {
	public static final ResourceLocation WIDGET_TEXTURE =
			new ResourceLocation(NBTEdit.MODID, "textures/gui/widgets.png");

	private Node<NamedNBT> node;
	private GuiNBTTree tree;

	protected int width;
	protected int height;
	protected int x;
	protected int y;

	private String displayString;


	public GuiNBTNode(GuiNBTTree tree, Node<NamedNBT> node, int x, int y) {
		super(Minecraft.getInstance());

		this.tree = tree;
		this.node = node;
		this.x = x;
		this.y = y;
		height = getMinecraft().font.lineHeight;
		updateDisplay();
	}

	private Minecraft getMinecraft() {
		return minecraft;
	}

	private boolean inBounds(double xIn, double yIn) {
		return xIn >= x && yIn >= y && xIn < width + x && yIn < height + y;
	}

	private boolean inHideShowBounds(double xIn, double yIn) {
		return xIn >= x - 9 && yIn >= y && xIn < x && yIn < y + height;
	}

	public boolean shouldDrawChildren() {
		return node.shouldDrawChildren();
	}

	public boolean clicked(double xIn, double yIn) {
		return inBounds(xIn, yIn);
	}

	public boolean hideShowClicked(double xIn, double yIn) {
		if (node.hasChildren() && inHideShowBounds(xIn, yIn)) {
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
		displayString = NBTHelper.getNBTNameSpecial(node.getObject());
		width = getMinecraft().font.width(displayString) + 12;
	}

	public void draw(PoseStack stack, int xIn, int yIn) {
		boolean selected = tree.getFocused() == node;
		boolean hover = inBounds(xIn, yIn);
		boolean chHover = inHideShowBounds(xIn, yIn);
		int color = selected ? 0xff : hover ? 16777120 : (node.hasParent()) ? 14737632 : -6250336;

		getMinecraft().textureManager.bindForSetup(WIDGET_TEXTURE);

		if (selected) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Gui.fill(stack, x + 11, y, x + width, y + height, Integer.MIN_VALUE);
		}
		if (node.hasChildren()) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			blit(stack, x - 9, y, (node.shouldDrawChildren()) ? 9 : 0, (chHover) ? height : 0, 9, height);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		blit(stack, x + 1, y, (node.getObject().getTag().getId() - 1) * 9, 18, 9, 9);
		drawString(stack, getMinecraft().font, displayString, x + 11, y + (this.height - 8) / 2, color);
	}

	public boolean shouldDraw(int top, int bottom) {
		return y + height >= top && y <= bottom;
	}
}
