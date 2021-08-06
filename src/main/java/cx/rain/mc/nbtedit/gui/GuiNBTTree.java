package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.*;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import cx.rain.mc.nbtedit.utility.NBTIOHelper;
import cx.rain.mc.nbtedit.utility.nbt.NBTTree;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.NBTNode;
import cx.rain.mc.nbtedit.utility.nbt.ClipboardStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;

import java.awt.*;
import java.util.*;
import java.util.List;

/*
 * The main Gui class for NBTEdit. This implementation is messy, naive, and unoptimized, but it works.
 * This is from long before GuiLib (and is actually yIn motivation for GuiLib), but sadly I do not
 * have time to rewrite it.
 *
 * Issues:
 *    - Not extensible - a separate tree GUI class for GuiLib would be nice.
 *    - Naive/unoptimized - layout changes force an entire reload of the tree
 *    - Messy, good luck. Some of the button IDs are hardcoded.
 */
public class GuiNBTTree extends Gui {
    private NBTTree tree;
    private List<GuiNBTNode> nodes;
    private GuiSaveSlotButton[] saves;
    private GuiNBTButton[] buttons;

    private final int X_GAP = 10;
    private final int Y_GAP = getMinecraft().font.lineHeight + 2;
    private final int START_X = 10;
    private final int START_Y = 30;

    private int width;
    private int height;

    private int heightDiff;
    private int bottom;
    private int offset;

    private int y;
    private int yClick;

    private NBTNode<NamedNBT> focused;
    private int focusedSlotIndex;

    private GuiEditNBT window;

    public GuiNBTTree(NBTTree treeIn) {
        super(Minecraft.getInstance());

        tree = treeIn;
        yClick = -1;
        focusedSlotIndex = -1;
        nodes = new ArrayList<>();
        buttons = new GuiNBTButton[16];
        saves = new GuiSaveSlotButton[7];
    }

    protected Minecraft getMinecraft() {
        return minecraft;
    }

    public NBTTree getNBTTree() {
        return tree;
    }

    public NBTNode<NamedNBT> getFocused() {
        return focused;
    }

    public GuiSaveSlotButton getFocusedSaveSlot() {
        return (focusedSlotIndex != -1) ? saves[focusedSlotIndex] : null;
    }

    private int getHeightDifference() {
        return getContentHeight() - (bottom - START_Y + 2);
    }

    private int getContentHeight() {
        return Y_GAP * nodes.size();
    }

    public GuiEditNBT getWindow() {
        return window;
    }

    public void init(int widthIn, int heightIn, int bottomIn) {
        width = widthIn;
        height = heightIn;
        bottom = bottomIn;
        yClick = -1;
        init(false);

        if (window != null) {
            window.initGUI((width - GuiEditNBT.WIDTH) / 2, (height - GuiEditNBT.HEIGHT) / 2);
        }
    }

    public void updateScreen() {
        if (window != null) {
            window.update();
        }

        if (focusedSlotIndex != -1) {
            saves[focusedSlotIndex].update();
        }
    }

    private void setFocused(NBTNode<NamedNBT> toFocus) {
        if (toFocus == null) {
            for (var b : buttons) {
                b.setEnabled(false);
            }
        } else if (toFocus.get().getTag() instanceof CompoundTag) {
            for (var b : buttons) {
                b.setEnabled(true);
            }

            buttons[12].setEnabled(toFocus != tree.getRoot());
            buttons[11].setEnabled(toFocus.hasParent() &&
                    !(toFocus.getParent().get().getTag() instanceof ListTag));
            buttons[13].setEnabled(true);
            buttons[14].setEnabled(toFocus != tree.getRoot());
            buttons[15].setEnabled(NBTEdit.CLIPBOARD != null);
        } else if (toFocus.get().getTag() instanceof ListTag) {
            if (toFocus.hasChildren()) {
                var type = toFocus.getChildren().get(0).get().getTag().getId();
                for (var b : buttons) {
                    b.setEnabled(false);
                }

                buttons[type - 1].setEnabled(true);
                buttons[12].setEnabled(true);
                buttons[11].setEnabled(!(toFocus.getParent().get().getTag() instanceof ListTag));
                buttons[13].setEnabled(true);
                buttons[14].setEnabled(true);
                buttons[15].setEnabled(NBTEdit.CLIPBOARD != null && NBTEdit.CLIPBOARD.getTag().getId() == type);
            } else {
                for (var b : buttons) {
                    b.setEnabled(true);
                }
            }

            buttons[11].setEnabled(!(toFocus.getParent().get().getTag() instanceof ListTag));
            buttons[13].setEnabled(true);
            buttons[14].setEnabled(true);
            buttons[15].setEnabled(NBTEdit.CLIPBOARD != null);
        } else {
            for (var b : buttons) {
                b.setEnabled(false);
            }

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

    public void init() {
        init(false);
    }

    public void init(boolean shiftToFocused) {
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

        if (heightDiff <= 0) {
            offset = 0;
        } else {
            if (offset < -heightDiff) {
                offset = -heightDiff;
            }

            if (offset > 0) {
                offset = 0;
            }

            for (var node : nodes) {
                node.shift(offset);
            }

            if (shiftToFocused && focused != null) {
                shiftTo(focused);
            }
        }
    }

    private void addSaveSlotButtons() {
        ClipboardStates clipboardStates = NBTEdit.getClipboardSaves();
        for (var i = 0; i < 7; ++i) {
            saves[i] = new GuiSaveSlotButton(clipboardStates.getClipboard(i), width - 24, 31 + i * 25);
        }
    }

    private void addButtons() {
        var x = 18;
        var y = 4;

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


    private boolean checkValidFocus(NBTNode<NamedNBT> fc) {
        for (GuiNBTNode node : nodes) { //Check all nodes
            if (node.getNode() == fc) {
                setFocused(fc);
                return true;
            }
        }
        return fc.hasParent() && checkValidFocus(fc.getParent());
    }

    private void addNodes(NBTNode<NamedNBT> NBTNode, int x) {
        nodes.add(new GuiNBTNode(this, NBTNode, x, y));
        x += X_GAP;
        y += Y_GAP;

        if (NBTNode.shouldShowChildren())
            for (NBTNode<NamedNBT> child : NBTNode.getChildren())
                addNodes(child, x);
    }

    public void draw(PoseStack stack, int xIn, int yIn) {
        var xTemp = xIn;
        var yTemp = yIn;

        if (window != null) {
            xTemp = -1;
            yTemp = -1;
        }

        for (var node : nodes) {
            if (node.shouldDraw(START_Y - 1, bottom)) {
                node.draw(stack, xTemp, yTemp);
            }
        }

        overlayBackground(0, START_Y - 1, 255, 255);
        overlayBackground(bottom, height, 255, 255);
        for (var but : buttons) {
            but.draw(stack, xTemp, yTemp);
        }
        for (var but : saves) {
            but.draw(stack, xTemp, yTemp);
        }

        drawScrollBar(stack, xTemp, yTemp);
        if (window != null) {
            window.draw(stack, xIn, yIn);
        }
    }

    private void drawScrollBar(PoseStack stack, int xIn, int yIn) {
        if (heightDiff > 0) {
            if (InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), InputConstants.MOUSE_BUTTON_LEFT)) {
                if (yClick == -1) {
                    if (xIn >= width - 20 && xIn < width && yIn >= START_Y - 1 && yIn < bottom) {
                        yClick = yIn;
                    }
                } else {
                    float scrollMultiplier = 1.0F;
                    int height = getHeightDifference();

                    if (height < 1) {
                        height = 1;
                    }
                    int length = (bottom - (START_Y - 1)) * (bottom - (START_Y - 1)) / getContentHeight();
                    if (length < 32)
                        length = 32;
                    if (length > bottom - (START_Y - 1) - 8)
                        length = bottom - (START_Y - 1) - 8;

                    scrollMultiplier /= (float) (this.bottom - (START_Y - 1) - length) / (float) height;


                    shift((int) ((yClick - yIn) * scrollMultiplier));
                    yClick = yIn;
                }
            } else
                yClick = -1;

            fill(stack, width - 20, START_Y - 1, width, bottom, Integer.MIN_VALUE);

            int length = (bottom - (START_Y - 1)) * (bottom - (START_Y - 1)) / getContentHeight();
            if (length < 32)
                length = 32;
            if (length > bottom - (START_Y - 1) - 8)
                length = bottom - (START_Y - 1) - 8;
            int y = -offset * (this.bottom - (START_Y - 1) - length) / heightDiff + (START_Y - 1);

            if (y < START_Y - 1)
                y = START_Y - 1;


            //	this.drawGradientRect(width-20,y,width,y+length,8421504, 12632256);
            //drawRect(width-20,y,width,y+length,0x80ffffff);
            fillGradient(stack, width - 20, y, width, y + length, 0x80ffffff, 0x80333333);
        }
    }

    protected void overlayBackground(int par1, int par2, int par3, int par4) {
        var tesselator = Tesselator.getInstance();
        var worldRenderer = tesselator.getBuilder();
        getMinecraft().textureManager.bindForSetup(BACKGROUND_LOCATION);
//        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        var var6 = 32.0F;
        worldRenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Color color = new Color(4210752);
        worldRenderer.color(color.getRed(), color.getGreen(), color.getBlue(), par4);
        worldRenderer.vertex(0.0D, par2, 0.0D).uv(0.0f, ((float) par2 / var6));
        worldRenderer.vertex(this.width, par2, 0.0D).uv(((float) this.width / var6), ((float) par2 / var6));
        worldRenderer.color(color.getRed(), color.getGreen(), color.getBlue(), par3);
        worldRenderer.vertex(this.width, par1, 0.0D).uv(((float) this.width / var6), ((float) par1 / var6));
        worldRenderer.vertex(0.0D, par1, 0.0D).uv(0.0f, ((float) par1 / var6));
        tesselator.end();
    }

    public void mouseClicked(double xIn, double yIn) {
        if (window == null) {
            boolean reInit = false;

            for (GuiNBTNode node : nodes) {
                if (node.hideShowClicked(xIn, yIn)) { // Check hide/show children buttons
                    reInit = true;
                    if (node.shouldDrawChildren())
                        offset = (START_Y + 1) - (node.y) + offset;
                    break;
                }
            }
            if (!reInit) {
                for (GuiNBTButton button : buttons) { //Check top buttons
                    if (button.inBounds(xIn, yIn)) {
                        buttonClicked(button);
                        return;
                    }
                }
                for (GuiSaveSlotButton button : saves) {
                    if (button.inBoundsOfX(xIn, yIn)) {
                        button.reset();
                        NBTEdit.getClipboardSaves().save();
                        getMinecraft().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
                        return;
                    }
                    if (button.inBounds(xIn, yIn)) {
                        saveButtonClicked(button);
                        return;
                    }
                }
                if (yIn >= START_Y && xIn <= width - 175) { //Check actual nodes, remove focus if nothing clicked
                    NBTNode<NamedNBT> newFocus = null;
                    for (GuiNBTNode node : nodes) {
                        if (node.clicked(xIn, yIn)) {
                            newFocus = node.getNode();
                            break;
                        }
                    }
                    if (focusedSlotIndex != -1)
                        stopEditingSlot();
                    setFocused(newFocus);
                }
            } else
                init();
        } else
            window.click(xIn, yIn);
    }

    private void saveButtonClicked(GuiSaveSlotButton button) {
        if (button.save.tag.isEmpty()) { //Copy into save slot
            NBTNode<NamedNBT> obj = (focused == null) ? tree.getRoot() : focused;
            Tag base = obj.get().getTag();
            String name = obj.get().getName();
            if (base instanceof ListTag) {
                ListTag list = new ListTag();
                tree.addChild(obj, list);
                button.save.tag.put(name, list);
            } else if (base instanceof CompoundTag) {
                CompoundTag compound = new CompoundTag();
                tree.addChild(obj, compound);
                button.save.tag.put(name, compound);
            } else
                button.save.tag.put(name, base.copy());
            button.saved();
            NBTEdit.getClipboardSaves().save();
            getMinecraft().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        } else { //Paste into
            Map<String, Tag> nbtMap = NBTIOHelper.getMap(button.save.tag);
            if (nbtMap.isEmpty()) {
                NBTEdit.getInstance().getInternalLogger().warn("Unable to copy from save \"" + button.save.name + "\".");
                NBTEdit.getInstance().getInternalLogger().warn("The save may invalid, a valid save must only contain 1 core Tag");
            } else {
                if (focused == null)
                    setFocused(tree.getRoot());

                Map.Entry<String, Tag> firstEntry = nbtMap.entrySet().iterator().next();
                assert firstEntry != null;
                String name = firstEntry.getKey();
                Tag nbt = firstEntry.getValue().copy();
                if (focused == tree.getRoot() && nbt instanceof CompoundTag && name.equals("ROOT")) {
                    setFocused(null);
                    tree = new NBTTree((CompoundTag) nbt);
                    init();
                    getMinecraft().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
                } else if (canAddToParent(focused.get().getTag(), nbt)) {
                    focused.setShowChildren(true);
                    for (Iterator<NBTNode<NamedNBT>> it = focused.getChildren().iterator(); it.hasNext(); ) { //Replace object with same name
                        if (it.next().get().getName().equals(name)) {
                            it.remove();
                            break;
                        }
                    }
                    NBTNode<NamedNBT> NBTNode = insert(new NamedNBT(name, nbt));
                    tree.addChildrenToTree(NBTNode);
                    tree.sort(NBTNode);
                    setFocused(NBTNode);
                    init(true);
                    getMinecraft().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
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
            focused.setShowChildren(true);
            List<NBTNode<NamedNBT>> children = focused.getChildren();
            String type = NBTHelper.getButtonName(button.getId());

            if (focused.get().getTag() instanceof ListTag) {
                Tag nbt = NBTHelper.newTag(button.getId());
                if (nbt != null) {
                    NBTNode<NamedNBT> newNBTNode = new NBTNode<>(focused, new NamedNBT("", nbt));
                    children.add(newNBTNode);
                    setFocused(newNBTNode);
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
            init(true);
        }
    }

    private boolean validName(String name, List<NBTNode<NamedNBT>> list) {
        for (NBTNode<NamedNBT> NBTNode : list)
            if (NBTNode.get().getName().equals(name))
                return false;
        return true;
    }

    private NBTNode<NamedNBT> insert(NamedNBT nbt) {
        NBTNode<NamedNBT> newNBTNode = new NBTNode<>(focused, nbt);

        if (focused.hasChildren()) {
            List<NBTNode<NamedNBT>> children = focused.getChildren();

            boolean added = false;
            for (int i = 0; i < children.size(); ++i) {
                if (NBTEdit.SORTER.compare(newNBTNode, children.get(i)) < 0) {
                    children.add(i, newNBTNode);
                    added = true;
                    break;
                }
            }
            if (!added)
                children.add(newNBTNode);
        } else
            focused.addChild(newNBTNode);
        return newNBTNode;
    }

    private NBTNode<NamedNBT> insert(String name, byte type) {
        Tag nbt = NBTHelper.newTag(type);
        if (nbt != null)
            return insert(new NamedNBT(name, nbt));
        return null;
    }

    public void deleteSelected() {
        if (focused != null) {
            if (tree.delete(focused)) {
                NBTNode<NamedNBT> oldFocused = focused;
                shiftFocus(true);
                if (focused == oldFocused)
                    setFocused(null);
                init();
            }
        }

    }

    public void editSelected() {
        if (focused != null) {
            Tag base = focused.get().getTag();
            if (focused.hasChildren() && (base instanceof CompoundTag || base instanceof ListTag)) {
                focused.setShowChildren(!focused.shouldShowChildren());
                int index;

                if (focused.shouldShowChildren() && (index = indexOf(focused)) != -1)
                    offset = (START_Y + 1) - nodes.get(index).y + offset;

                init();
            } else if (buttons[11].isEnabled()) {
                edit();
            }
        } else if (focusedSlotIndex != -1) {
            stopEditingSlot();
        }
    }

    private boolean canAddToParent(Tag parent, Tag child) {
        if (parent instanceof CompoundTag)
            return true;
        if (parent instanceof ListTag) {
            ListTag list = (ListTag) parent;
            return list.size() == 0 || list.getElementType() == child.getId();
        }
        return false;
    }

    private boolean canPaste() {
        return NBTEdit.CLIPBOARD != null && focused != null && canAddToParent(focused.get().getTag(), NBTEdit.CLIPBOARD.getTag());
    }

    private void paste() {
        if (NBTEdit.CLIPBOARD != null) {
            focused.setShowChildren(true);

            NamedNBT namedNBT = NBTEdit.CLIPBOARD.copy();
            if (focused.get().getTag() instanceof ListTag) {
                namedNBT.setName("");
                NBTNode<NamedNBT> NBTNode = new NBTNode<>(focused, namedNBT);
                focused.addChild(NBTNode);
                tree.addChildrenToTree(NBTNode);
                tree.sort(NBTNode);
                setFocused(NBTNode);
            } else {
                String name = namedNBT.getName();
                List<NBTNode<NamedNBT>> children = focused.getChildren();
                if (!validName(name, children)) {
                    for (int i = 1; i <= children.size() + 1; ++i) {
                        String n = name + "(" + i + ")";
                        if (validName(n, children)) {
                            namedNBT.setName(n);
                            break;
                        }
                    }
                }
                NBTNode<NamedNBT> NBTNode = insert(namedNBT);
                tree.addChildrenToTree(NBTNode);
                tree.sort(NBTNode);
                setFocused(NBTNode);
            }

            init(true);
        }
    }

    private void copy() {
        if (focused != null) {
            NamedNBT namedNBT = focused.get();
            if (namedNBT.getTag() instanceof ListTag) {
                ListTag list = new ListTag();
                tree.addChild(focused, list);
                NBTEdit.CLIPBOARD = new NamedNBT(namedNBT.getName(), list);
            } else if (namedNBT.getTag() instanceof CompoundTag) {
                CompoundTag compound = new CompoundTag();
                tree.addChild(focused, compound);
                NBTEdit.CLIPBOARD = new NamedNBT(namedNBT.getName(), compound);
            } else
                NBTEdit.CLIPBOARD = focused.get().copy();
            setFocused(focused);
        }
    }

    private void cut() {
        copy();
        deleteSelected();
    }

    private void edit() {
        Tag base = focused.get().getTag();
        Tag parent = focused.getParent().get().getTag();
        window = new GuiEditNBT(this, focused, !(parent instanceof ListTag), !(base instanceof CompoundTag || base instanceof ListTag));
        window.initGUI((width - GuiEditNBT.WIDTH) / 2, (height - GuiEditNBT.HEIGHT) / 2);
    }

    public void nodeEdited(NBTNode<NamedNBT> NBTNode) {
        NBTNode<NamedNBT> parent = NBTNode.getParent();
        Collections.sort(parent.getChildren(), NBTEdit.SORTER);
        init(true);
    }

    public void arrowKeyPressed(boolean up) {
        if (focused == null)
            shift((up) ? Y_GAP : -Y_GAP);
        else
            shiftFocus(up);
    }

    private int indexOf(NBTNode<NamedNBT> NBTNode) {
        for (int i = 0; i < nodes.size(); ++i) {
            if (nodes.get(i).getNode() == NBTNode) {
                return i;
            }
        }
        return -1;
    }

    private void shiftFocus(boolean up) {
        int index = indexOf(focused);
        if (index != -1) {
            index += (up) ? -1 : 1;
            if (index >= 0 && index < nodes.size()) {
                setFocused(nodes.get(index).getNode());
                shift((up) ? Y_GAP : -Y_GAP);
            }
        }
    }

    private void shiftTo(NBTNode<NamedNBT> NBTNode) {
        int index = indexOf(NBTNode);
        if (index != -1) {
            GuiNBTNode gui = nodes.get(index);
            shift((bottom + START_Y + 1) / 2 - (gui.y + gui.height));
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

    public void closeWindow() {
        window = null;
    }

    public boolean isEditingSlot() {
        return focusedSlotIndex != -1;
    }

    public void stopEditingSlot() {
        saves[focusedSlotIndex].stopEditing();
        NBTEdit.getClipboardSaves().save();
        focusedSlotIndex = -1;
    }

    public void keyTyped(char ch, int key) {
        if (focusedSlotIndex != -1) {
            saves[focusedSlotIndex].keyTyped(ch, key);
        } else {
            if (key == InputConstants.KEY_C && Screen.hasControlDown())
                copy();
            if (key == InputConstants.KEY_V && Screen.hasControlDown() && canPaste())
                paste();
            if (key == InputConstants.KEY_X && Screen.hasControlDown())
                cut();
        }
    }

    public void rightClick(double xIn, double yIn) {
        for (int i = 0; i < 7; ++i) {
            if (saves[i].inBounds(xIn, yIn)) {
                setFocused(null);
                if (focusedSlotIndex != -1) {
                    if (focusedSlotIndex != i) {
                        saves[focusedSlotIndex].stopEditing();
                        NBTEdit.getClipboardSaves().save();
                    } else {
                        //Already editing the correct one!
                        return;
                    }
                }
                saves[i].startEditing();
                focusedSlotIndex = i;
                break;
            }
        }
    }

    private void putColor(BufferBuilder renderer, int argb, int p_178988_2_) {
        // FIXME: 2021/8/6 AS: Why an int unused?
//        int i = renderer.getColorIndex(p_178988_2_);
//        int j = argb >> 16 & 255;
//        int k = argb >> 8 & 255;
//        int l = argb & 255;
//        int i1 = argb >> 24 & 255;
//        renderer.putColorRGBA(i, j, k, l, i1);

        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        int i1 = argb >> 24 & 255;
        renderer.color(j, k, l, i1);
    }

}
