package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class GuiTextField extends Widget {
	private final FontRenderer fontRenderer;

	private final int xPos, yPos;
	private final int width, height;


	private String text = "";
	private int maxStringLength = 32;
	private int cursorCounter;

	private boolean isFocused = false;


	private boolean isEnabled = true;
	private int field_73816_n = 0;
	private int cursorPosition = 0;

	/**
	 * other selection position, maybe the same as the cursor
	 */
	private int selectionEnd = 0;
	private int enabledColor = 14737632;
	private int disabledColor = 7368816;

	/**
	 * True if this textbox is visible
	 */
	private boolean visible = true;
	private boolean enableBackgroundDrawing = true;
	private boolean allowSection;

	public GuiTextField(FontRenderer fontRenderer, int x, int y, int w, int h, boolean allowSection) {
		super(x, y, w, h, StringTextComponent.EMPTY);
		this.fontRenderer = fontRenderer;
		this.xPos = x;
		this.yPos = y;
		this.width = w;
		this.height = h;
		this.allowSection = allowSection;
	}

	/**
	 * Increments the cursor counter
	 */
	public void updateCursorCounter() {
		++this.cursorCounter;
	}

	/**
	 * Sets the text of the textbox.
	 */
	public void setText(String par1Str) {
		if (par1Str.length() > this.maxStringLength) {
			this.text = par1Str.substring(0, this.maxStringLength);
		} else {
			this.text = par1Str;
		}

		this.moveCursorToEnd();
	}

	/**
	 * Returns the text beign edited on the textbox.
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * @return returns the text between the cursor and selectionEnd
	 */
	public String getHighlighted() {
		int var1 = Math.min(this.cursorPosition, this.selectionEnd);
		int var2 = Math.max(this.cursorPosition, this.selectionEnd);
		return this.text.substring(var1, var2);
	}

	/**
	 * replaces selected text, or inserts text at the position on the cursor
	 */
	public void insertText(String par1Str) {
		String var2 = "";
		String var3 = CharacterFilter.filerAllowedCharacters(par1Str, allowSection);
		int var4 = Math.min(this.cursorPosition, this.selectionEnd);
		int var5 = Math.max(this.cursorPosition, this.selectionEnd);
		int var6 = this.maxStringLength - this.text.length() - (var4 - this.selectionEnd);

		if (this.text.length() > 0) {
			var2 = var2 + this.text.substring(0, var4);
		}

		int var8;

		if (var6 < var3.length()) {
			var2 = var2 + var3.substring(0, var6);
			var8 = var6;
		} else {
			var2 = var2 + var3;
			var8 = var3.length();
		}

		if (this.text.length() > 0 && var5 < this.text.length()) {
			var2 = var2 + this.text.substring(var5);
		}

		this.text = var2;
		this.moveCursor(var4 - this.selectionEnd + var8);
	}

	private void deleteText(int p_212950_1_) {
		if (Screen.hasControlDown()) {
			this.deleteWords(p_212950_1_);
		} else {
			this.deleteChars(p_212950_1_);
		}

	}

	/**
	 * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
	 * the cursor.
	 */
	public void deleteWords(int par1) {
		if (this.text.length() != 0) {
			if (this.selectionEnd != this.cursorPosition) {
				this.insertText("");
			} else {
				this.deleteChars(this.getNthWordFromCursor(par1) - this.cursorPosition);
			}
		}
	}

	/**
	 * delete the selected text, otherwise deletes characters from either side of the cursor. params: delete num
	 */
	public void deleteChars(int par1) {
		if (this.text.length() != 0) {
			if (this.selectionEnd != this.cursorPosition) {
				this.insertText("");
			} else {
				boolean var2 = par1 < 0;
				int var3 = var2 ? this.cursorPosition + par1 : this.cursorPosition;
				int var4 = var2 ? this.cursorPosition : this.cursorPosition + par1;
				String var5 = "";

				if (var3 >= 0) {
					var5 = this.text.substring(0, var3);
				}

				if (var4 < this.text.length()) {
					var5 = var5 + this.text.substring(var4);
				}

				this.text = var5;

				if (var2) {
					this.moveCursor(par1);
				}
			}
		}
	}

	/**
	 * see @getNthNextWordFromPos() params: N, position
	 */
	public int getNthWordFromCursor(int par1) {
		return this.getNthWordFromPos(par1, this.getCursorPosition());
	}

	/**
	 * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
	 */
	public int getNthWordFromPos(int par1, int par2) {
		return this.func_73798_a(par1, this.getCursorPosition(), true);
	}

	public int func_73798_a(int par1, int par2, boolean par3) {
		int var4 = par2;
		boolean var5 = par1 < 0;
		int var6 = Math.abs(par1);

		for (int var7 = 0; var7 < var6; ++var7) {
			if (var5) {
				while (par3 && var4 > 0 && this.text.charAt(var4 - 1) == 32) {
					--var4;
				}

				while (var4 > 0 && this.text.charAt(var4 - 1) != 32) {
					--var4;
				}
			} else {
				int var8 = this.text.length();
				var4 = this.text.indexOf(32, var4);

				if (var4 == -1) {
					var4 = var8;
				} else {
					while (par3 && var4 < var8 && this.text.charAt(var4) == 32) {
						++var4;
					}
				}
			}
		}

		return var4;
	}

	/**
	 * Moves the text cursor by a specified number of characters and clears the selection
	 */
	public void moveCursor(int amount) {
		this.moveCursorTo(this.selectionEnd + amount);
	}

	/**
	 * sets the position of the cursor to the provided index
	 */
	public void moveCursorTo(int index) {
		this.cursorPosition = index;
		int var2 = this.text.length();

		if (this.cursorPosition < 0) {
			this.cursorPosition = 0;
		}

		if (this.cursorPosition > var2) {
			this.cursorPosition = var2;
		}

		this.setHighlightPos(this.cursorPosition);
	}

	/**
	 * sets the cursors position to the beginning
	 */
	public void moveCursorToStart() {
		this.moveCursorTo(0);
	}

	/**
	 * sets the cursors position to after the text
	 */
	public void moveCursorToEnd() {
		this.moveCursorTo(this.text.length());
	}

	/**
	 * Call this method from you GuiScreen to process the keys into textbox.
	 */

	public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
		if (this.isEnabled && this.isFocused) {
			if (Screen.isSelectAll(key)) {
				this.moveCursorToEnd();
				this.setHighlightPos(0);
				return true;
			} else if (Screen.isCopy(key)) {
				Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
				return true;
			} else if (Screen.isPaste(key)) {
				this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
				return true;
			} else if (Screen.isCut(key)) {
				Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
				this.insertText("");
				return true;
			} else {
				switch(key) {
					case GLFW.GLFW_KEY_BACKSPACE:
						this.deleteText(-1);
						return true;
					case GLFW.GLFW_KEY_INSERT:
					case GLFW.GLFW_KEY_DOWN:
					case GLFW.GLFW_KEY_UP:
					case GLFW.GLFW_KEY_PAGE_UP:
					case GLFW.GLFW_KEY_PAGE_DOWN:
						return false;
					case GLFW.GLFW_KEY_DELETE:
						this.deleteText(1);
						return true;
					case GLFW.GLFW_KEY_RIGHT:
						if (Screen.hasShiftDown()) {
							if (Screen.hasControlDown()) {
								this.setHighlightPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
							} else {
								this.setHighlightPos(this.getSelectionEnd() + 1);
							}
						} else if (Screen.hasControlDown()) {
							this.moveCursorTo(this.getNthWordFromCursor(1));
						} else {
							this.moveCursor(1);
						}
						return true;
					case GLFW.GLFW_KEY_LEFT:
						if (Screen.hasShiftDown()) {
							if (Screen.hasControlDown()) {
								this.setHighlightPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
							} else {
								this.setHighlightPos(this.getSelectionEnd() - 1);
							}
						} else if (Screen.hasControlDown()) {
							this.moveCursorTo(this.getNthWordFromCursor(-1));
						} else {
							this.moveCursor(-1);
						}

						return true;
					case GLFW.GLFW_KEY_HOME:
						if (Screen.hasShiftDown()) {
							this.setHighlightPos(0);
						} else {
							this.moveCursorToStart();
						}
						return true;
					case GLFW.GLFW_KEY_END:
						if (Screen.hasShiftDown()) {
							this.setHighlightPos(this.text.length());
						} else {
							this.moveCursorToEnd();
						}
						return true;
					default:
						return false;
				}
			}
		}
		return false;
	}

	@Override
	public boolean charTyped(char p_231042_1_, int p_231042_2_) {
		if (SharedConstants.isAllowedChatCharacter(p_231042_1_)) {
			this.insertText(Character.toString(p_231042_1_));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Args: x, y, buttonClicked
	 */
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		String displayString = text.replace(NBTStringHelper.SECTION_SIGN, '?');
		boolean inBounds = mx >= this.xPos && mx < this.xPos + this.width && my >= this.yPos && my < this.yPos + this.height;

		this.setFocused(this.isEnabled && inBounds);

		if (this.isFocused && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			int var5 = (int) mx - this.xPos;

			if (this.enableBackgroundDrawing) {
				var5 -= 4;
			}

			String var6 = this.fontRenderer.plainSubstrByWidth(displayString.substring(this.field_73816_n), this.getWidth());
			this.moveCursorTo(this.fontRenderer.plainSubstrByWidth(var6, var5).length() + this.field_73816_n);
			return true;
		}
		return inBounds;
	}

	/**
	 * Draws the textbox
	 */
	public void drawTextBox(MatrixStack matrixStack) {
		String textToDisplay = text.replace(NBTStringHelper.SECTION_SIGN, '?');
		if (this.getVisible()) {
			if (this.getEnableBackgroundDrawing()) {
				fill(matrixStack, this.xPos - 1, this.yPos - 1, this.xPos + this.width + 1, this.yPos + this.height + 1, -6250336);
				fill(matrixStack, this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.height, -16777216);
			}

			int var1 = this.isEnabled ? this.enabledColor : this.disabledColor;
			int var2 = this.cursorPosition - this.field_73816_n;
			int var3 = this.selectionEnd - this.field_73816_n;
			String var4 = this.fontRenderer.plainSubstrByWidth(textToDisplay.substring(this.field_73816_n), this.getWidth());
			boolean var5 = var2 >= 0 && var2 <= var4.length();
			boolean var6 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && var5;
			int var7 = this.enableBackgroundDrawing ? this.xPos + 4 : this.xPos;
			int var8 = this.enableBackgroundDrawing ? this.yPos + (this.height - 8) / 2 : this.yPos;
			int var9 = var7;

			if (var3 > var4.length()) {
				var3 = var4.length();
			}

			if (var4.length() > 0) {
				String var10 = var5 ? var4.substring(0, var2) : var4;
				var9 = this.fontRenderer.drawShadow(matrixStack, var10, var7, var8, var1);
			}

			boolean var13 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
			int var11 = var9;

			if (!var5) {
				var11 = var2 > 0 ? var7 + this.width : var7;
			} else if (var13) {
				var11 = var9 - 1;
				--var9;
			}

			if (var4.length() > 0 && var5 && var2 < var4.length()) {
				this.fontRenderer.drawShadow(matrixStack, var4.substring(var2), var9, var8, var1);
			}

			if (var6) {
				if (var13) {
					fill(matrixStack, var11, var8 - 1, var11 + 1, var8 + 1 + this.fontRenderer.lineHeight, -3092272);
				} else {
					this.fontRenderer.drawShadow(matrixStack, "_", var11, var8, var1);
				}
			}

			if (var3 != var2) {
				int var12 = var7 + this.fontRenderer.width(var4.substring(0, var3));
				this.drawCursorVertical(matrixStack, var11, var8 - 1, var12 - 1, var8 + 1 + this.fontRenderer.lineHeight);
			}
		}
	}

	/**
	 * draws the vertical line cursor in the textbox
	 */
	private void drawCursorVertical(MatrixStack matrixStack, int par1, int par2, int par3, int par4) {
		int var5;

		if (par1 < par3) {
			var5 = par1;
			par1 = par3;
			par3 = var5;
		}

		if (par2 < par4) {
			var5 = par2;
			par2 = par4;
			par4 = var5;
		}
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tex = tessellator.getBuilder();
		GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

		tex.begin(7, DefaultVertexFormats.POSITION);
		tex.vertex(par1, par4, 0.0D).endVertex();
		tex.vertex(par3, par4, 0.0D).endVertex();
		tex.vertex(par3, par2, 0.0D).endVertex();
		tex.vertex(par1, par2, 0.0D).endVertex();
		tessellator.end();
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}

	public void setMaxStringLength(int par1) {
		this.maxStringLength = par1;

		if (this.text.length() > par1) {
			this.text = this.text.substring(0, par1);
		}
	}

	/**
	 * returns the maximum number of character that can be contained in this textbox
	 */
	public int getMaxStringLength() {
		return this.maxStringLength;
	}

	/**
	 * returns the current position of the cursor
	 */
	public int getCursorPosition() {
		return this.cursorPosition;
	}

	/**
	 * get enable drawing background and outline
	 */
	public boolean getEnableBackgroundDrawing() {
		return this.enableBackgroundDrawing;
	}

	/**
	 * enable drawing background and outline
	 */
	public void setEnableBackgroundDrawing(boolean par1) {
		this.enableBackgroundDrawing = par1;
	}

	/**
	 * Sets the text colour for this textbox (disabled text will not use this colour)
	 */
	public void setTextColor(int par1) {
		this.enabledColor = par1;
	}

	public void func_82266_h(int par1) {
		this.disabledColor = par1;
	}

	/**
	 * setter for the focused field
	 */
	public void setFocused(boolean par1) {
		if (par1 && !this.isFocused) {
			this.cursorCounter = 0;
		}

		this.isFocused = par1;
	}

	/**
	 * getter for the focused field
	 */
	public boolean isFocused() {
		return this.isFocused;
	}

	public void setEditable(boolean par1) {
		this.isEnabled = par1;
	}

	/**
	 * the side of the selection that is not the cursor, maye be the same as the cursor
	 */
	public int getSelectionEnd() {
		return this.selectionEnd;
	}

	/**
	 * returns the width of the textbox depending on if the the box is enabled
	 */
	public int getWidth() {
		return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
	}

	/**
	 * Sets the position of the selection anchor (i.e. position the selection was started at)
	 */
	public void setHighlightPos(int par1) {
		String displayString = text.replace(NBTStringHelper.SECTION_SIGN, '?');
		int var2 = displayString.length();

		if (par1 > var2) {
			par1 = var2;
		}

		if (par1 < 0) {
			par1 = 0;
		}

		this.selectionEnd = par1;

		if (this.fontRenderer != null) {
			if (this.field_73816_n > var2) {
				this.field_73816_n = var2;
			}

			int var3 = this.getWidth();
			String var4 = this.fontRenderer.plainSubstrByWidth(displayString.substring(this.field_73816_n), var3);
			int var5 = var4.length() + this.field_73816_n;

			if (par1 == this.field_73816_n) {
				this.field_73816_n -= this.fontRenderer.plainSubstrByWidth(displayString, var3, true).length();
			}

			if (par1 > var5) {
				this.field_73816_n += par1 - var5;
			} else if (par1 <= this.field_73816_n) {
				this.field_73816_n -= this.field_73816_n - par1;
			}

			if (this.field_73816_n < 0) {
				this.field_73816_n = 0;
			}

			if (this.field_73816_n > var2) {
				this.field_73816_n = var2;
			}
		}
	}


	/**
	 * @return {@code true} if this textbox is visible
	 */
	public boolean getVisible() {
		return this.visible;
	}

	/**
	 * Sets whether or not this textbox is visible
	 */
	public void setVisible(boolean par1) {
		this.visible = par1;
	}
}
