package cx.rain.mc.nbtedit.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.gui.NBTEditGui;
import cx.rain.mc.nbtedit.gui.component.button.SpecialCharacterButton;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import cx.rain.mc.nbtedit.utility.nbt.NBTNode;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.ParseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class EditSubWindowComponent extends AbstractWidget {
    public static final ResourceLocation WINDOW_TEXTURE =
            new ResourceLocation(NBTEdit.MODID, "textures/gui/window.png");
    public static final int WIDTH = 178;
    public static final int HEIGHT = 93;

    protected CompoundTag nbt;
    protected NBTEditGui gui;
    protected NBTNode<NamedNBT> node;
    protected boolean canEditName;
    protected boolean canEditValue;

    protected EditBox nameField;
    protected EditBox valueField;

    protected Button saveButton;
    protected Button cancelButton;

    protected SpecialCharacterButton colorButton;
    protected SpecialCharacterButton newLineButton;

    protected String nameError;
    protected String valueError;

    public EditSubWindowComponent(NBTEditGui parent, NBTNode<NamedNBT> nodeIn,
                                  boolean canEditNameIn, boolean canEditValueIn,
                                  int x, int y) {
        super(x, y, 178, 93, new TextComponent(""));

        gui = parent;
        node = nodeIn;
        canEditName = canEditNameIn;
        canEditValue = canEditValueIn;

        colorButton = new SpecialCharacterButton((byte) 0, x + width - 1, y + 34, this::onColorButtonClicked);
        newLineButton = new SpecialCharacterButton((byte) 1, x + width - 1, y + 50, this::onNewLineButtonClicked);

        String name = (nameField == null) ? node.get().getName() : nameField.getValue();
        String value = (valueField == null) ? getValue(nbt) : valueField.getValue();

        nameField = new EditBox(getMinecraft().font, x + 46, y + 18, 116, 15, new TextComponent("Name"));
        valueField = new EditBox(getMinecraft().font, x + 46, y + 44, 116, 15, new TextComponent("Value"));

        nameField.setValue(name);
        nameField.active = canEditName;

        valueField.setMaxLength(256);
        valueField.setValue(value);
        valueField.active = canEditValue;

        saveButton = new Button(x + 9, y + 62, 75, 20,
                new TextComponent("Save"), this::onSaveButtonClicked);	// Todo: AS: I18n here.

        if (!nameField.isFocused() && !valueField.isFocused()) {
            if (canEditName) {
                nameField.setFocus(true);
            }
            else if (canEditValue) {
                valueField.setFocus(true);
            }
        }

        colorButton.active = valueField.isFocused();
        newLineButton.active = valueField.isFocused();

        cancelButton = new Button(x + 93, y + 62, 75, 20,
                new TextComponent("Cancel"), this::onCancelButtonClicked);	// Todo: AS: I18n here.

    }

    protected void onSaveButtonClicked(Button button) {
        nameField.mouseClicked(button.x, button.y, 0);
        valueField.mouseClicked(button.x, button.y, 0);
        saveAndQuit();
    }

    protected void onCancelButtonClicked(Button button) {
        nameField.mouseClicked(button.x, button.y, 0);
        valueField.mouseClicked(button.x, button.y, 0);
        gui.closeSubWindow();
    }


    protected void onNewLineButtonClicked(Button button) {
        valueField.insertText("\n");
        isValidInput();
    }

    protected void onColorButtonClicked(Button button) {
        valueField.insertText("" + NBTHelper.SECTION_SIGN);
        isValidInput();
    }

    private void saveAndQuit() {
        if (canEditName) {
            node.get().setName(nameField.getValue());
        }
        setValidValue(node, valueField.getValue());
        gui.onNodeModified(node);
        gui.closeSubWindow();
    }

    protected Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        getMinecraft().textureManager.bindForSetup(WINDOW_TEXTURE);

        blit(stack, x, y, 0, 0, width, height);
        if (!canEditName) {
            fill(stack, x + 42, y + 15, x + 169, y + 31, 0x80000000);
        }
        if (!canEditValue) {
            fill(stack, x + 42, y + 41, x + 169, y + 57, 0x80000000);
        }

        nameField.render(stack, mouseX, mouseY, partialTicks);
        valueField.render(stack, mouseX, mouseY, partialTicks);

        saveButton.renderButton(stack, mouseX, mouseY, partialTicks);
        cancelButton.renderButton(stack, mouseX, mouseY, partialTicks);

        if (nameError != null) {
            drawCenteredString(stack, getMinecraft().font, nameError, x + width / 2, y + 4, 0xFF0000);
        }
        if (valueError != null) {
            drawCenteredString(stack, getMinecraft().font, nameError, x + width / 2, y + 32, 0xFF0000);
        }

        colorButton.renderButton(stack, mouseX, mouseY, partialTicks);
        newLineButton.renderButton(stack, mouseX, mouseY, partialTicks);
    }

    public void update() {
        nameField.tick();
        valueField.tick();
    }

    public void setWindowTop(int xIn, int yIn) {
        x = xIn;
        y = yIn;
    }

    @Override
    public void updateNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.HINT, "NBTEdit sub-window.");
    }

    private void isValidInput() {
        boolean isValid = true;
        nameError = null;
        valueError = null;
        if (canEditName && !isNameValid()) {
            isValid = false;
            nameError = "Duplicate Tag Name";
        }
        try {
            isValueValid(valueField.getValue(), nbt.getId());
        } catch (NumberFormatException e) {
            valueError = e.getMessage();
            isValid = false;
        }
        saveButton.active = isValid;
    }

    private boolean isNameValid() {
        for (var n : node.getParent().getChildren()) {
            Tag base = n.get().getTag();
            if (base != nbt && n.get().getName().equals(nameField.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static void isValueValid(String value, byte type) throws NumberFormatException {
        switch (type) {
            case 1 -> ParseHelper.parseByte(value);
            case 2 -> ParseHelper.parseShort(value);
            case 3 -> ParseHelper.parseInt(value);
            case 4 -> ParseHelper.parseLong(value);
            case 5 -> ParseHelper.parseFloat(value);
            case 6 -> ParseHelper.parseDouble(value);
            case 7 -> ParseHelper.parseByteArray(value);
            case 11 -> ParseHelper.parseIntArray(value);
        }
    }

    private static String getValue(Tag tag) {
        switch (tag.getId()) {
            case 7:
                var s = new StringBuilder();
                for (byte b : ((ByteArrayTag) tag).getAsByteArray()) {
                    s.append(b).append(" ");
                }
                return s.toString();
            case 9:
                return "TagList";
            case 10:
                return "TagCompound";
            case 11:
                var i = new StringBuilder();
                for (int a : ((IntArrayTag) tag).getAsIntArray()) {
                    i.append(a).append(" ");
                }
                return i.toString();
            default:
                return NBTHelper.toString(tag);
        }
    }

    protected static void setValidValue(NBTNode<NamedNBT> node, String value) {
        NamedNBT named = node.get();
        Tag tag = named.getTag();

        if (tag instanceof ByteTag) {
            named.setTag(ByteTag.valueOf(ParseHelper.parseByte(value)));
        }
        if (tag instanceof ShortTag) {
            named.setTag(ShortTag.valueOf(ParseHelper.parseShort(value)));
        }
        if (tag instanceof IntTag) {
            named.setTag(IntTag.valueOf(ParseHelper.parseInt(value)));
        }
        if (tag instanceof LongTag) {
            named.setTag(LongTag.valueOf(ParseHelper.parseLong(value)));
        }
        if (tag instanceof FloatTag) {
            named.setTag(FloatTag.valueOf(ParseHelper.parseFloat(value)));
        }
        if (tag instanceof DoubleTag) {
            named.setTag(DoubleTag.valueOf(ParseHelper.parseDouble(value)));
        }
        if (tag instanceof ByteArrayTag) {
            named.setTag(new ByteArrayTag(ParseHelper.parseByteArray(value)));
        }
        if (tag instanceof IntArrayTag) {
            named.setTag(new IntArrayTag(ParseHelper.parseIntArray(value)));
        }
        if (tag instanceof StringTag) {
            named.setTag(StringTag.valueOf(value));
        }
    }
}
