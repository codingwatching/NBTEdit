package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;

import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.opengl.GL11;

public class GuiTextField extends Gui {
	private final Font font;

	private final int xPos, yPos;
	private final int width, height;

	private String text = "";
	private int maxStringLength = 32;
	private int cursorCounter;

	private boolean isFocused = false;

	private boolean isEnabled = true;

	private int field_73816_n = 0;	// Todo: AS: What is it mean?

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

	public GuiTextField(Font fontIn, int x, int y, int w, int h, boolean allowSectionIn) {
		super(Minecraft.getInstance());
		font = fontIn;
		xPos = x;
		yPos = y;
		width = w;
		height = h;
		allowSection = allowSectionIn;
	}

	/**
	 * Increments the cursor counter
	 */
	public void updateCursorCounter() {
		++cursorCounter;
	}

	/**
	 * Sets the text of the textbox.
	 */
	public void setText(String par1Str) {
		if (par1Str.length() > maxStringLength) {
			text = par1Str.substring(0, maxStringLength);
		} else {
			text = par1Str;
		}

		setCursorPositionEnd();
	}

	/**
	 * Returns the text beign edited on the textbox.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return returns the text between the cursor and selectionEnd
	 */
	public String getSelectedtext() {
		var var1 = Math.min(cursorPosition, selectionEnd);
		var var2 = Math.max(cursorPosition, selectionEnd);
		return text.substring(var1, var2);
	}

	/**
	 * replaces selected text, or inserts text at the position on the cursor
	 */
	public void writeText(String par1Str) {
		var var2 = "";
		var var3 = CharacterFilter.filerAllowedCharacters(par1Str, allowSection);
		var var4 = Math.min(cursorPosition, selectionEnd);
		var var5 = Math.max(cursorPosition, selectionEnd);
		var var6 = maxStringLength - text.length() - (var4 - selectionEnd);

		if (text.length() > 0) {
			var2 = var2 + text.substring(0, var4);
		}

		int var8;

		if (var6 < var3.length()) {
			var2 = var2 + var3.substring(0, var6);
			var8 = var6;
		} else {
			var2 = var2 + var3;
			var8 = var3.length();
		}

		if (text.length() > 0 && var5 < text.length()) {
			var2 = var2 + text.substring(var5);
		}

		text = var2;
		moveCursorBy(var4 - selectionEnd + var8);
	}

	/**
	 * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
	 * the cursor.
	 */
	public void deleteWords(int par1) {
		if (text.length() != 0) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				deleteFromCursor(getNthWordFromCursor(par1) - cursorPosition);
			}
		}
	}

	/**
	 * delete the selected text, otherwise deletes characters from either side of the cursor. params: delete num
	 */
	public void deleteFromCursor(int par1) {
		if (text.length() != 0) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				var var2 = par1 < 0;
				var var3 = var2 ? cursorPosition + par1 : cursorPosition;
				var var4 = var2 ? cursorPosition : cursorPosition + par1;
				String var5 = "";

				if (var3 >= 0) {
					var5 = text.substring(0, var3);
				}

				if (var4 < text.length()) {
					var5 = var5 + text.substring(var4);
				}

				text = var5;

				if (var2) {
					moveCursorBy(par1);
				}
			}
		}
	}

	/**
	 * see @getNthNextWordFromPos() params: N, position
	 */
	public int getNthWordFromCursor(int par1) {
		return getNthWordFromPos(par1, getCursorPosition());
	}

	/**
	 * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
	 */
	public int getNthWordFromPos(int par1, int par2) {
		return getNthWordFromPos(par1, getCursorPosition(), true);
	}

	public int getNthWordFromPos(int par1, int par2, boolean par3) {
		int var4 = par2;
		boolean var5 = par1 < 0;
		int var6 = Math.abs(par1);

		for (int var7 = 0; var7 < var6; ++var7) {
			if (var5) {
				while (par3 && var4 > 0 && text.charAt(var4 - 1) == 32) {
					--var4;
				}

				while (var4 > 0 && text.charAt(var4 - 1) != 32) {
					--var4;
				}
			} else {
				int var8 = text.length();
				var4 = text.indexOf(32, var4);

				if (var4 == -1) {
					var4 = var8;
				} else {
					while (par3 && var4 < var8 && text.charAt(var4) == 32) {
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
	public void moveCursorBy(int par1) {
		setCursorPosition(selectionEnd + par1);
	}

	/**
	 * sets the position of the cursor to the provided index
	 */
	public void setCursorPosition(int par1) {
		cursorPosition = par1;
		var var2 = text.length();

		if (cursorPosition < 0) {
			cursorPosition = 0;
		}

		if (cursorPosition > var2) {
			cursorPosition = var2;
		}

		setSelectionPos(cursorPosition);
	}

	/**
	 * sets the cursors position to the beginning
	 */
	public void setCursorPositionZero() {
		setCursorPosition(0);
	}

	/**
	 * sets the cursors position to after the text
	 */
	public void setCursorPositionEnd() {
		setCursorPosition(text.length());
	}

	/**
	 * Call this method from you GuiScreen to process the keys into textbox.
	 */
	public boolean textBoxCharTyped(char par1, int par2) {
		if (isEnabled && isFocused) {
			switch (par1) {
				case 1:
					setCursorPositionEnd();
					setSelectionPos(0);
					return true;
				case 3:
					minecraft.keyboardHandler.setClipboard(getSelectedtext());
					return true;
				case 22:
					writeText(minecraft.keyboardHandler.getClipboard());
					return true;
				case 24:
					minecraft.keyboardHandler.setClipboard(getSelectedtext());
					writeText("");
					return true;
				default:
					switch (par2) {
						case 14:
							if (Screen.hasControlDown()) {
								deleteWords(-1);
							} else {
								deleteFromCursor(-1);
							}

							return true;
						case 199:
							if (Screen.hasControlDown()) {
								setSelectionPos(0);
							} else {
								setCursorPositionZero();
							}

							return true;
						case 203:
							if (Screen.hasShiftDown()) {
								if (Screen.hasControlDown()) {
									setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
								} else {
									setSelectionPos(getSelectionEnd() - 1);
								}
							} else if (Screen.hasControlDown()) {
								setCursorPosition(getNthWordFromCursor(-1));
							} else {
								moveCursorBy(-1);
							}

							return true;
						case 205:
							if (Screen.hasShiftDown()) {
								if (Screen.hasControlDown()) {
									setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
								} else {
									setSelectionPos(getSelectionEnd() + 1);
								}
							} else if (Screen.hasControlDown()) {
								setCursorPosition(getNthWordFromCursor(1));
							} else {
								moveCursorBy(1);
							}

							return true;
						case 207:
							if (Screen.hasShiftDown()) {
								setSelectionPos(text.length());
							} else {
								setCursorPositionEnd();
							}

							return true;
						case 211:
							if (Screen.hasControlDown()) {
								deleteWords(1);
							} else {
								deleteFromCursor(1);
							}

							return true;
						default:
							if (SharedConstants.isAllowedChatCharacter(par1)) {
								writeText(Character.toString(par1));
								return true;
							} else {
								return false;
							}
					}
			}
		} else {
			return false;
		}
	}

	/**
	 * Args: x, y, buttonClicked
	 */
	public void mouseClicked(double par1, double par2, int par3) {
		// Todo: AS: Check it. If it crashed, change "\u00A7" to another character.
		String displayString = text.replace(NBTHelper.SECTION_SIGN, '\u00A7');
		boolean var4 = par1 >= xPos && par1 < xPos + width && par2 >= yPos && par2 < yPos + height;

		setFocused(isEnabled && var4);

		if (isFocused && par3 == 0) {
			int var5 = (int) par1 - xPos;

			if (enableBackgroundDrawing) {
				var5 -= 4;
			}

			String var6 = font.plainSubstrByWidth(displayString.substring(field_73816_n), getWidth());
			setCursorPosition(font.plainSubstrByWidth(var6, var5).length() + field_73816_n);
		}
	}

	/**
	 * Draws the textbox
	 */
	public void drawTextBox(PoseStack stack) {
		// Todo: AS: Check it. If it crashed, change "\u00A7" to another character.
		String textToDisplay = text.replace(NBTHelper.SECTION_SIGN, '\u00A7');
		if (getVisible()) {
			if (getEnableBackgroundDrawing()) {
				fill(stack, xPos - 1, yPos - 1, xPos + width + 1, yPos + height + 1, -6250336);
				fill(stack, xPos, yPos, xPos + width, yPos + height, -16777216);
			}

			int var1 = isEnabled ? enabledColor : disabledColor;
			int var2 = cursorPosition - field_73816_n;
			int var3 = selectionEnd - field_73816_n;
			String var4 = font.plainSubstrByWidth(textToDisplay.substring(field_73816_n), getWidth());
			boolean var5 = var2 >= 0 && var2 <= var4.length();
			boolean var6 = isFocused && cursorCounter / 6 % 2 == 0 && var5;
			int var7 = enableBackgroundDrawing ? xPos + 4 : xPos;
			int var8 = enableBackgroundDrawing ? yPos + (height - 8) / 2 : yPos;
			int var9 = var7;

			if (var3 > var4.length()) {
				var3 = var4.length();
			}

			if (var4.length() > 0) {
				String var10 = var5 ? var4.substring(0, var2) : var4;
				var9 = font.drawShadow(stack, var10, var7, var8, var1);
			}

			boolean var13 = cursorPosition < text.length() || text.length() >= getMaxStringLength();
			int var11 = var9;

			if (!var5) {
				var11 = var2 > 0 ? var7 + width : var7;
			} else if (var13) {
				var11 = var9 - 1;
				--var9;
			}

			if (var4.length() > 0 && var5 && var2 < var4.length()) {
				font.drawShadow(stack, var4.substring(var2), var9, var8, var1);
			}

			if (var6) {
				if (var13) {
					Gui.fill(stack, var11, var8 - 1, var11 + 1,
							var8 + 1 + font.lineHeight, -3092272);
				} else {
					font.drawShadow(stack, "_", var11, var8, var1);
				}
			}

			if (var3 != var2) {
				int var12 = var7 + font.width(var4.substring(0, var3));
				drawCursorVertical(var11, var8 - 1, var12 - 1, var8 + 1 + font.lineHeight);
			}
		}
	}

	/**
	 * draws the vertical line cursor in the textbox
	 */
	private void drawCursorVertical(int par1, int par2, int par3, int par4) {
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

		var tesselator = Tesselator.getInstance();
		var bufferBuilder = tesselator.getBuilder();
		GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager._disableTexture();
		GlStateManager._enableColorLogicOp();
		GlStateManager._logicOp(GL11.GL_OR_REVERSE);

		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(par1, par4, 0.0D);
		bufferBuilder.vertex(par3, par4, 0.0D);
		bufferBuilder.vertex(par3, par2, 0.0D);
		bufferBuilder.vertex(par1, par2, 0.0D);
		tesselator.end();
		GlStateManager._disableColorLogicOp();
		GlStateManager._enableTexture();
	}

	public void setMaxStringLength(int par1) {
		maxStringLength = par1;

		if (text.length() > par1) {
			text = text.substring(0, par1);
		}
	}

	/**
	 * returns the maximum number of character that can be contained in this textbox
	 */
	public int getMaxStringLength() {
		return maxStringLength;
	}

	/**
	 * returns the current position of the cursor
	 */
	public int getCursorPosition() {
		return cursorPosition;
	}

	/**
	 * get enable drawing background and outline
	 */
	public boolean getEnableBackgroundDrawing() {
		return enableBackgroundDrawing;
	}

	/**
	 * enable drawing background and outline
	 */
	public void setEnableBackgroundDrawing(boolean par1) {
		enableBackgroundDrawing = par1;
	}

	/**
	 * Sets the text colour for this textbox (disabled text will not use this colour)
	 */
	public void setTextColor(int par1) {
		enabledColor = par1;
	}

	public void func_82266_h(int par1) {
		disabledColor = par1;
	}

	/**
	 * setter for the focused field
	 */
	public void setFocused(boolean par1) {
		if (par1 && !isFocused) {
			cursorCounter = 0;
		}

		isFocused = par1;
	}

	/**
	 * getter for the focused field
	 */
	public boolean isFocused() {
		return isFocused;
	}

	public void func_82265_c(boolean par1) {
		isEnabled = par1;
	}

	/**
	 * the side of the selection that is not the cursor, maye be the same as the cursor
	 */
	public int getSelectionEnd() {
		return selectionEnd;
	}

	/**
	 * returns the width of the textbox depending on if the the box is enabled
	 */
	public int getWidth() {
		return getEnableBackgroundDrawing() ? width - 8 : width;
	}

	/**
	 * Sets the position of the selection anchor (i.e. position the selection was started at)
	 */
	public void setSelectionPos(int par1) {
		// Todo: AS: Check it. If it crashed, change "\u00A7" to another character.
		var displayString = text.replace(NBTHelper.SECTION_SIGN, '\u00A7');
		var var2 = displayString.length();

		if (par1 > var2) {
			par1 = var2;
		}

		if (par1 < 0) {
			par1 = 0;
		}

		selectionEnd = par1;

		if (font != null) {
			if (field_73816_n > var2) {
				field_73816_n = var2;
			}

			int var3 = getWidth();
			String var4 = font.plainSubstrByWidth(displayString.substring(field_73816_n), var3);
			int var5 = var4.length() + field_73816_n;

			if (par1 == field_73816_n) {
				field_73816_n -= font.plainSubstrByWidth(displayString, var3, true).length();
			}

			if (par1 > var5) {
				field_73816_n += par1 - var5;
			} else if (par1 <= field_73816_n) {
				field_73816_n -= field_73816_n - par1;
			}

			if (field_73816_n < 0) {
				field_73816_n = 0;
			}

			if (field_73816_n > var2) {
				field_73816_n = var2;
			}
		}
	}


	/**
	 * @return {@code true} if this textbox is visible
	 */
	public boolean getVisible() {
		return visible;
	}

	/**
	 * Sets whether or not this textbox is visible
	 */
	public void setVisible(boolean par1) {
		visible = par1;
	}
}
