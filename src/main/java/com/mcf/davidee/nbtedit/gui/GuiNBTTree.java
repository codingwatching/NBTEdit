package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mcf.davidee.nbtedit.nbt.NBTTree;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.Node;
import com.mcf.davidee.nbtedit.nbt.SaveStates;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;


/*
 * The main Gui class for NBTEdit. This implementation is messy, naive, and unoptimized, but it works.
 * This is from long before GuiLib (and is actually my motivation for GuiLib), but sadly I do not
 * have time to rewrite it.
 *
 * Issues:
 *    - Not extensible - a separate tree GUI class for GuiLib would be nice.
 *    - Naive/unoptimized - layout changes force an entire reload of the tree
 *    - Messy, good luck. Some buttons IDs are hardcoded.
 */
public class GuiNBTTree extends Widget {

	private final Minecraft minecraft = Minecraft.getInstance();

	private NBTTree tree;
	private final List<GuiNBTNode> nodes;
	private final GuiSaveSlotButton[] saves;
	private final GuiNBTButton[] buttons;

	private final int X_GAP = 10, START_X = 10, START_Y = 30;
	public final int Y_GAP = Minecraft.getInstance().font.lineHeight + 2;

	private int y, yClick, bottom, width, height, heightDiff, offset;
	private boolean scrolling = false;

	private Node<NamedNBT> focused;
	private int focusedSlotIndex;

	private GuiEditNBT window;

	public Node<NamedNBT> getFocused() {
		return focused;
	}

	public GuiSaveSlotButton getFocusedSaveSlot() {
		return ( focusedSlotIndex != -1 ) ? saves[focusedSlotIndex] : null;
	}

	public NBTTree getNBTTree() {
		return tree;
	}

	public GuiNBTTree(NBTTree tree) {
		super(0, 0, 0, 0, StringTextComponent.EMPTY);
		this.tree = tree;
		yClick = -1;
		focusedSlotIndex = -1;
		nodes = new ArrayList<>();
		buttons = new GuiNBTButton[16];
		saves = new GuiSaveSlotButton[7];
	}

	private int getHeightDifference() {
		return getContentHeight() - ( bottom - START_Y + 2 );
	}

	private int getContentHeight() {
		return Y_GAP * nodes.size();
	}

	public GuiEditNBT getWindow() {
		return window;
	}

	public void initGUI(int width, int height, int bottom) {
		this.width = width;
		this.height = height;
		this.bottom = bottom;
		yClick = -1;
		initGUI(false);
		if (window != null) {
			window.initGUI(( width - GuiEditNBT.WIDTH ) / 2, ( height - GuiEditNBT.HEIGHT ) / 2);
		}
	}

	public void tick() {
		if (window != null)
			window.update();
		if (focusedSlotIndex != -1)
			saves[focusedSlotIndex].update();
	}

	private void setFocused(Node<NamedNBT> toFocus) {
		if (toFocus == null) {
			for (GuiNBTButton b : buttons)
				b.setEnabled(false);
		} else if (toFocus.getObject().getNBT() instanceof CompoundNBT) {
			for (GuiNBTButton b : buttons)
				b.setEnabled(true);
			buttons[12].setEnabled(toFocus != tree.getRoot());
			buttons[11].setEnabled(toFocus.hasParent() && !( toFocus.getParent().getObject().getNBT() instanceof ListNBT ));
			buttons[13].setEnabled(true);
			buttons[14].setEnabled(toFocus != tree.getRoot());
			buttons[15].setEnabled(NBTEdit.clipboard != null);
		} else if (toFocus.getObject().getNBT() instanceof ListNBT) {
			if (toFocus.hasChildren()) {
				byte type = toFocus.getChildren().get(0).getObject().getNBT().getId();
				for (GuiNBTButton b : buttons)
					b.setEnabled(false);
				buttons[type - 1].setEnabled(true);
				buttons[12].setEnabled(true);
				buttons[11].setEnabled(!( toFocus.getParent().getObject().getNBT() instanceof ListNBT ));
				buttons[13].setEnabled(true);
				buttons[14].setEnabled(true);
				buttons[15].setEnabled(NBTEdit.clipboard != null && NBTEdit.clipboard.getNBT().getId() == type);
			} else
				for (GuiNBTButton b : buttons)
					b.setEnabled(true);
			buttons[11].setEnabled(!( toFocus.getParent().getObject().getNBT() instanceof ListNBT ));
			buttons[13].setEnabled(true);
			buttons[14].setEnabled(true);
			buttons[15].setEnabled(NBTEdit.clipboard != null);
		} else {
			for (GuiNBTButton b : buttons)
				b.setEnabled(false);
			buttons[12].setEnabled(true);
			buttons[11].setEnabled(true);
			buttons[13].setEnabled(true);
			buttons[14].setEnabled(true);
			buttons[15].setEnabled(false);
		}

		focused = toFocus;
		if (focused != null && focusedSlotIndex != -1) {
			stopEditingSlot();
		}
	}

	public void initGUI() {
		initGUI(false);
	}

	public void initGUI(boolean shiftToFocused) {
		y = START_Y;
		nodes.clear();
		addNodes(tree.getRoot(), START_X);
		addButtons();
		addSaveSlotButtons();
		if (focused != null) {
			if (!checkValidFocus(focused)) {
				setFocused(null);
			}
		}
		if (focusedSlotIndex != -1) {
			saves[focusedSlotIndex].startEditing();
		}
		heightDiff = getHeightDifference();
		if (heightDiff <= 0)
			offset = 0;
		else {
			if (offset < -heightDiff)
				offset = -heightDiff;
			if (offset > 0)
				offset = 0;
			for (GuiNBTNode node : nodes)
				node.shift(offset);
			if (shiftToFocused && focused != null) {
				shiftTo(focused);
			}
		}
	}

	private void addSaveSlotButtons() {
		SaveStates saveStates = NBTEdit.getSaveStates();
		for (int i = 0; i < 7; ++i)
			saves[i] = new GuiSaveSlotButton(saveStates.getSaveState(i), width - 24, 31 + i * 25);
	}

	private void addButtons() {
		int x = 18, y = 4;

		for (byte i = 14; i < 17; ++i) {
			buttons[i - 1] = new GuiNBTButton(i, x, y);
			x += 15;
		}

		x += 30;
		for (byte i = 12; i < 14; ++i) {
			buttons[i - 1] = new GuiNBTButton(i, x, y);
			x += 15;
		}

		x = 18;
		y = 17;
		for (byte i = 1; i < 12; ++i) {
			buttons[i - 1] = new GuiNBTButton(i, x, y);
			x += 9;
		}
	}


	private boolean checkValidFocus(Node<NamedNBT> fc) {
		for (GuiNBTNode node : nodes) { //Check all nodes
			if (node.getNode() == fc) {
				setFocused(fc);
				return true;
			}
		}
		return fc.hasParent() && checkValidFocus(fc.getParent());
	}

	private void addNodes(Node<NamedNBT> node, int x) {
		nodes.add(new GuiNBTNode(this, node, x, y));
		x += X_GAP;
		y += Y_GAP;

		if (node.shouldDrawChildren())
			for (Node<NamedNBT> child : node.getChildren())
				addNodes(child, x);
	}

	@Override
	public void render(MatrixStack matrixStack, int mx, int my, float particleTicks) {
		int cmx = mx, cmy = my;
		if (window != null) {
			cmx = -1;
			cmy = -1;
		}
		for (GuiNBTNode node : nodes) {
			if (node.shouldDraw(START_Y - 1, bottom))
				node.render(matrixStack, cmx, cmy, particleTicks);
		}
		overlayBackground(0, START_Y - 1, 255, 255);
		overlayBackground(bottom, height, 255, 255);
		for (GuiNBTButton but : buttons)
			but.render(matrixStack, cmx, cmy, particleTicks);
		for (GuiSaveSlotButton but : saves)
			but.render(matrixStack, cmx, cmy, particleTicks);
		drawScrollBar(matrixStack, cmx, cmy);
		if (window != null)
			window.render(matrixStack, mx, my, particleTicks);
	}

	private void drawScrollBar(MatrixStack matrixStack, int mx, int my) {
		if (this.scrollBarActive()) {
			/*if (Minecraft.getInstance().mouseHandler.isLeftPressed()) {
				if (yClick == -1) {
					if (mx >= width - 20 && mx < width && my >= START_Y - 1 && my < bottom) {
						yClick = my;
					}
				} else {
					float scrollMultiplier = 1.0F;
					int height = getHeightDifference();

					if (height < 1) {
						height = 1;
					}
					int length = ( bottom - ( START_Y - 1 ) ) * ( bottom - ( START_Y - 1 ) ) / getContentHeight();
					if (length < 32)
						length = 32;
					if (length > bottom - ( START_Y - 1 ) - 8)
						length = bottom - ( START_Y - 1 ) - 8;

					scrollMultiplier /= (float) ( this.bottom - ( START_Y - 1 ) - length ) / (float) height;


					shift((int) ( ( yClick - my ) * scrollMultiplier ));
					yClick = my;
				}
			} else
				yClick = -1;*/


			fill(matrixStack, width - 20, START_Y - 1, width, bottom, Integer.MIN_VALUE);


			int length = ( bottom - ( START_Y - 1 ) ) * ( bottom - ( START_Y - 1 ) ) / getContentHeight();
			if (length < 32)
				length = 32;
			if (length > bottom - ( START_Y - 1 ) - 8)
				length = bottom - ( START_Y - 1 ) - 8;
			int y = -offset * ( this.bottom - ( START_Y - 1 ) - length ) / heightDiff + ( START_Y - 1 );

			if (y < START_Y - 1)
				y = START_Y - 1;


			//	this.drawGradientRect(width-20,y,width,y+length,8421504, 12632256);
			//drawRect(width-20,y,width,y+length,0x80ffffff);
			GuiUtils.drawGradientRect(matrixStack.last().pose(), 0, width - 20, y, width, y + length, 0x80ffffff, 0x80333333);
		}
	}

	protected void overlayBackground(int par1, int par2, int par3, int par4) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuilder();
		minecraft.getTextureManager().bind(BACKGROUND_LOCATION);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float var6 = 32.0F;
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
		Color color = new Color(4210752);
		worldRenderer.vertex(0.0D, par2, 0.0D)
				.color(color.getRed(), color.getGreen(), color.getBlue(), par4)
				.uv(0.0F, ( (float) par2 / var6 ))
				.endVertex();
		worldRenderer.vertex(this.width, par2, 0.0D)
				.color(color.getRed(), color.getGreen(), color.getBlue(), par4)
				.uv(( (float) this.width / var6 ), ( (float) par2 / var6 ))
				.endVertex();
		worldRenderer.vertex(this.width, par1, 0.0D)
				.color(color.getRed(), color.getGreen(), color.getBlue(), par3)
				.uv(( (float) this.width / var6 ), ( (float) par1 / var6 ))
				.endVertex();
		worldRenderer.vertex(0.0D, par1, 0.0D)
				.color(color.getRed(), color.getGreen(), color.getBlue(), par3)
				.uv(0.0F, ( (float) par1 / var6 ))
				.endVertex();
		tessellator.end();
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		this.scrolling(false);
		if (window == null) {
			boolean reInit = false;

			for (GuiNBTNode node : nodes) {
				if (node.hideShowClicked(mx, my)) { // Check hide/show children buttons
					reInit = true;
					if (node.shouldDrawChildren())
						offset = ( START_Y + 1 ) - ( node.y ) + offset;
					break;
				}
			}
			if (!reInit) {
				for (GuiNBTButton guiNBTButton : buttons) { //Check top buttons
					if (guiNBTButton.inBounds(mx, my)) {
						buttonClicked(guiNBTButton);
						return true;
					}
				}
				for (GuiSaveSlotButton saveSlotButton : saves) {
					if (saveSlotButton.inBoundsOfX(mx, my)) {
						saveSlotButton.reset();
						NBTEdit.getSaveStates().save();
						minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
						return true;
					}
					if (saveSlotButton.inBounds(mx, my)) {
						saveButtonClicked(saveSlotButton);
						return true;
					}
				}
				if (my >= START_Y && mx <= width - 175) { //Check actual nodes, remove focus if nothing clicked
					Node<NamedNBT> newFocus = null;
					for (GuiNBTNode node : nodes) {
						if (node.clicked(mx, my)) {
							newFocus = node.getNode();
							break;
						}
					}
					if (focusedSlotIndex != -1) {
						stopEditingSlot();
					}
					setFocused(newFocus);
				}
				if (scrollBarActive() && mx >= width - 20 && mx < width && my >= START_Y - 1 && my < bottom) {
					this.scrolling(true);
					/*if (yClick == -1) {
						yClick = (int) my;
					} else {
						float scrollMultiplier = 1.0F;
						int height = getHeightDifference();

						if (height < 1) {
							height = 1;
						}
						int length = ( bottom - ( START_Y - 1 ) ) * ( bottom - ( START_Y - 1 ) ) / getContentHeight();
						if (length < 32)
							length = 32;
						if (length > bottom - ( START_Y - 1 ) - 8)
							length = bottom - ( START_Y - 1 ) - 8;

						scrollMultiplier /= (float) ( this.bottom - ( START_Y - 1 ) - length ) / (float) height;


						shift((int) ( ( yClick - my ) * scrollMultiplier ));
						yClick = (int) my;
					}*/
				}

			} else {
				initGUI();
			}
		} else {
			window.mouseClicked(mx, my, button);
		}
		return true;
	}

	private void saveButtonClicked(GuiSaveSlotButton button) {
		if (button.save.tag.isEmpty()) { //Copy into save slot
			Node<NamedNBT> obj = ( focused == null ) ? tree.getRoot() : focused;
			INBT base = obj.getObject().getNBT();
			String name = obj.getObject().getName();
			if (base instanceof ListNBT) {
				ListNBT list = new ListNBT();
				tree.addChildrenToList(obj, list);
				button.save.tag.put(name, list);
			} else if (base instanceof CompoundNBT) {
				CompoundNBT compound = new CompoundNBT();
				tree.addChildrenToTag(obj, compound);
				button.save.tag.put(name, compound);
			} else
				button.save.tag.put(name, base.copy());
			button.saved();
			NBTEdit.getSaveStates().save();
			minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		} else { //Paste into
			Map<String, INBT> nbtMap = NBTHelper.getMap(button.save.tag);
			if (nbtMap.isEmpty()) {
				NBTEdit.log(Level.WARN, "Unable to copy from save \"" + button.save.name + "\".");
				NBTEdit.log(Level.WARN, "The save is invalid - a valid save must only contain 1 core INBT");
			} else {
				if (focused == null)
					setFocused(tree.getRoot());

				Entry<String, INBT> firstEntry = nbtMap.entrySet().iterator().next();
				assert firstEntry != null;
				String name = firstEntry.getKey();
				INBT nbt = firstEntry.getValue().copy();
				if (focused == tree.getRoot() && nbt instanceof CompoundNBT && name.equals("ROOT")) {
					setFocused(null);
					tree = new NBTTree((CompoundNBT) nbt);
					initGUI();
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				} else if (canAddToParent(focused.getObject().getNBT(), nbt)) {
					focused.setDrawChildren(true);
					for (Iterator<Node<NamedNBT>> it = focused.getChildren().iterator(); it.hasNext(); ) { //Replace object with same name
						if (it.next().getObject().getName().equals(name)) {
							it.remove();
							break;
						}
					}
					Node<NamedNBT> node = insert(new NamedNBT(name, nbt));
					tree.addChildrenToTree(node);
					tree.sort(node);
					setFocused(node);
					initGUI(true);
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				}
			}
		}
	}

	private void buttonClicked(GuiNBTButton button) {
		if (button.getId() == 16)
			paste();
		else if (button.getId() == 15)
			cut();
		else if (button.getId() == 14)
			copy();
		else if (button.getId() == 13)
			deleteSelected();
		else if (button.getId() == 12)
			edit();
		else if (focused != null) {
			focused.setDrawChildren(true);
			List<Node<NamedNBT>> children = focused.getChildren();
			String type = NBTStringHelper.getButtonName(button.getId());

			if (focused.getObject().getNBT() instanceof ListNBT) {
				INBT nbt = NBTStringHelper.newTag(button.getId());
				if (nbt != null) {
					Node<NamedNBT> newNode = new Node<>(focused, new NamedNBT("", nbt));
					children.add(newNode);
					setFocused(newNode);
				}
			} else if (children.size() == 0) {
				setFocused(insert(type + "1", button.getId()));
			} else {
				for (int i = 1; i <= children.size() + 1; ++i) {
					String name = type + i;
					if (validName(name, children)) {
						setFocused(insert(name, button.getId()));
						break;
					}
				}
			}
			initGUI(true);
		}
	}

	private boolean validName(String name, List<Node<NamedNBT>> list) {
		for (Node<NamedNBT> node : list)
			if (node.getObject().getName().equals(name))
				return false;
		return true;
	}

	private Node<NamedNBT> insert(NamedNBT nbt) {
		Node<NamedNBT> newNode = new Node<>(focused, nbt);

		if (focused.hasChildren()) {
			List<Node<NamedNBT>> children = focused.getChildren();

			boolean added = false;
			for (int i = 0; i < children.size(); ++i) {
				if (NBTEdit.SORTER.compare(newNode, children.get(i)) < 0) {
					children.add(i, newNode);
					added = true;
					break;
				}
			}
			if (!added)
				children.add(newNode);
		} else
			focused.addChild(newNode);
		return newNode;
	}

	private Node<NamedNBT> insert(String name, byte type) {
		INBT nbt = NBTStringHelper.newTag(type);
		if (nbt != null)
			return insert(new NamedNBT(name, nbt));
		return null;
	}

	public void deleteSelected() {
		if (focused != null) {
			if (tree.delete(focused)) {
				Node<NamedNBT> oldFocused = focused;
				shiftFocus(true);
				if (focused == oldFocused)
					setFocused(null);
				initGUI();
			}
		}

	}

	public void editSelected() {
		if (focused != null) {
			INBT base = focused.getObject().getNBT();
			if (focused.hasChildren() && ( base instanceof CompoundNBT || base instanceof ListNBT )) {
				focused.setDrawChildren(!focused.shouldDrawChildren());
				int index;

				if (focused.shouldDrawChildren() && ( index = indexOf(focused) ) != -1)
					offset = ( START_Y + 1 ) - nodes.get(index).y + offset;

				initGUI();
			} else if (buttons[11].isEnabled()) {
				edit();
			}
		} else if (focusedSlotIndex != -1) {
			stopEditingSlot();
		}
	}

	private boolean canAddToParent(INBT parent, INBT child) {
		if (parent instanceof CompoundNBT)
			return true;
		if (parent instanceof ListNBT) {
			ListNBT list = (ListNBT) parent;
			return list.size() == 0 || list.getId() == child.getId();
		}
		return false;
	}

	private boolean canPaste() {
		return NBTEdit.clipboard != null && focused != null && canAddToParent(focused.getObject().getNBT(), NBTEdit.clipboard.getNBT());
	}

	private void paste() {
		if (NBTEdit.clipboard != null) {
			focused.setDrawChildren(true);

			NamedNBT namedNBT = NBTEdit.clipboard.copy();
			if (focused.getObject().getNBT() instanceof ListNBT) {
				namedNBT.setName("");
				Node<NamedNBT> node = new Node<>(focused, namedNBT);
				focused.addChild(node);
				tree.addChildrenToTree(node);
				tree.sort(node);
				setFocused(node);
			} else {
				String name = namedNBT.getName();
				List<Node<NamedNBT>> children = focused.getChildren();
				if (!validName(name, children)) {
					for (int i = 1; i <= children.size() + 1; ++i) {
						String n = name + "(" + i + ")";
						if (validName(n, children)) {
							namedNBT.setName(n);
							break;
						}
					}
				}
				Node<NamedNBT> node = insert(namedNBT);
				tree.addChildrenToTree(node);
				tree.sort(node);
				setFocused(node);
			}

			initGUI(true);
		}
	}

	private void copy() {
		if (focused != null) {
			NamedNBT namedNBT = focused.getObject();
			if (namedNBT.getNBT() instanceof ListNBT) {
				ListNBT list = new ListNBT();
				tree.addChildrenToList(focused, list);
				NBTEdit.clipboard = new NamedNBT(namedNBT.getName(), list);
			} else if (namedNBT.getNBT() instanceof CompoundNBT) {
				CompoundNBT compound = new CompoundNBT();
				tree.addChildrenToTag(focused, compound);
				NBTEdit.clipboard = new NamedNBT(namedNBT.getName(), compound);
			} else
				NBTEdit.clipboard = focused.getObject().copy();
			setFocused(focused);
		}
	}

	private void cut() {
		copy();
		deleteSelected();
	}

	private void edit() {
		INBT base = focused.getObject().getNBT();
		INBT parent = focused.getParent().getObject().getNBT();
		window = new GuiEditNBT(this, focused, !( parent instanceof ListNBT ), !( base instanceof CompoundNBT || base instanceof ListNBT ));
		window.initGUI(( width - GuiEditNBT.WIDTH ) / 2, ( height - GuiEditNBT.HEIGHT ) / 2);
	}

	public void nodeEdited(Node<NamedNBT> node) {
		Node<NamedNBT> parent = node.getParent();
		Collections.sort(parent.getChildren(), NBTEdit.SORTER);
		initGUI(true);
	}

	public void arrowKeyPressed(boolean up) {
		if (focused == null)
			shift(( up ) ? Y_GAP : -Y_GAP);
		else
			shiftFocus(up);
	}
	public void backspacePressed() {

	}

	private int indexOf(Node<NamedNBT> node) {
		for (int i = 0; i < nodes.size(); ++i) {
			if (nodes.get(i).getNode() == node) {
				return i;
			}
		}
		return -1;
	}

	private void shiftFocus(boolean up) {
		int index = indexOf(focused);
		if (index != -1) {
			index += ( up ) ? -1 : 1;
			if (index >= 0 && index < nodes.size()) {
				setFocused(nodes.get(index).getNode());
				shift(( up ) ? Y_GAP : -Y_GAP);
			}
		}
	}

	private void shiftTo(Node<NamedNBT> node) {
		int index = indexOf(node);
		if (index != -1) {
			GuiNBTNode gui = nodes.get(index);
			shift(( bottom + START_Y + 1 ) / 2 - ( gui.y + gui.getHeight() ));
		}
	}

	public void shift(int i) {
		if (heightDiff <= 0 || window != null)
			return;
		int dif = offset + i;
		if (dif > 0)
			dif = 0;
		if (dif < -heightDiff)
			dif = -heightDiff;
		for (GuiNBTNode node : nodes)
			node.shift(dif - offset);
		offset = dif;
	}

	public boolean scrollBarActive() {
		return heightDiff > 0;
	}

	public boolean scrolling() {
		return scrolling;
	}

	public void scrolling(boolean bool) {
		this.scrolling = bool;
	}

	public void closeWindow() {
		window = null;
	}

	public boolean isEditingSlot() {
		return focusedSlotIndex != -1;
	}

	public void stopEditingSlot() {
		saves[focusedSlotIndex].stopEditing();
		NBTEdit.getSaveStates().save();
		focusedSlotIndex = -1;
	}

	@Override
	public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
		if (focusedSlotIndex != -1 && key == GLFW.GLFW_KEY_BACKSPACE) {
			saves[focusedSlotIndex].backSpace();
			return true;
		}
		if (key == GLFW.GLFW_KEY_DELETE) {
			this.deleteSelected();
		} else if (key == GLFW.GLFW_KEY_ENTER) {
			this.editSelected();
		} else if (key == GLFW.GLFW_KEY_UP) {
			this.arrowKeyPressed(true);
		} else if (key == GLFW.GLFW_KEY_DOWN) {
			this.arrowKeyPressed(false);
		}
		return super.keyPressed(key, p_231046_2_, p_231046_3_);
	}

	@Override
	public boolean charTyped(char ch, int key) {
		if (focusedSlotIndex != -1) {
			saves[focusedSlotIndex].keyTyped(ch, key);
			return true;
		} else {
			if (key == 67 && Screen.hasControlDown()) {
				copy();
				return true;
			}
			if (key == 86 && Screen.hasControlDown() && canPaste()) {
				paste();
				return true;
			}
			if (key == 88 && Screen.hasControlDown()) {
				cut();
				return true;
			}
		}
		return false;
	}

	public void rightClick(double mx, double my) {
		for (int i = 0; i < 7; ++i) {
			if (saves[i].inBounds(mx, my)) {
				setFocused(null);
				if (focusedSlotIndex != -1) {
					if (focusedSlotIndex != i) {
						saves[focusedSlotIndex].stopEditing();
						NBTEdit.getSaveStates().save();
					} else //Already editing the correct one!
						return;
				}
				saves[i].startEditing();
				focusedSlotIndex = i;
				break;
			}
		}
	}
}
