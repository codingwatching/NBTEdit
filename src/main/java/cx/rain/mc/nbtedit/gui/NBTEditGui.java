package cx.rain.mc.nbtedit.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.gui.component.EditSubWindowComponent;
import cx.rain.mc.nbtedit.gui.component.NBTNodeComponent;
import cx.rain.mc.nbtedit.gui.component.button.NBTOperatorButton;
import cx.rain.mc.nbtedit.gui.component.button.SaveSlotButton;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import cx.rain.mc.nbtedit.utility.NBTIOHelper;
import cx.rain.mc.nbtedit.utility.nbt.ClipboardStates;
import cx.rain.mc.nbtedit.utility.nbt.NBTTree;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.NBTNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NBTEditGui extends Gui {
    private final int START_X = 10;
    private final int START_Y = 30;

    private final int X_GAP = 10;
    private final int Y_GAP = getMinecraft().font.lineHeight + 2;

    protected NBTTree tree;

    protected NBTNode<NamedNBT> focused;
    protected int focusedSaveSlotIndex = -1;

    protected SaveSlotButton[] saves = new SaveSlotButton[7];
    protected List<NBTNodeComponent> nodes = new ArrayList<>();
    protected NBTOperatorButton[] buttons = new NBTOperatorButton[16];

    protected int width;
    protected int height;
    protected int bottom;
    protected int heightDiff;
    protected int offset;

    protected int x;
    protected int y;

    protected int yClick;

    protected EditSubWindowComponent subWindow = null;

    public NBTEditGui(NBTTree treeIn) {
        super(Minecraft.getInstance());

        tree = treeIn;
    }

    protected Minecraft getMinecraft() {
        return minecraft;
    }

    public NBTTree getTree() {
        return tree;
    }

    public SaveSlotButton getFocusedSaveSlotIndex() {
        return (focusedSaveSlotIndex != -1) ? saves[focusedSaveSlotIndex] : null;
    }

    public void init(int widthIn, int heightIn, int bottomIn) {
        width = widthIn;
        height = heightIn;
        bottom = bottomIn;
        init(false);

        if (subWindow != null) {
            subWindow.setWindowTop((width - subWindow.getWidth()) / 2, (height - subWindow.getHeight()) / 2);
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

        if (focusedSaveSlotIndex != -1) {
            saves[focusedSaveSlotIndex].startEditing();
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
                node.shiftY(offset);
            }

            if (shiftToFocused && focused != null) {
                shiftTo(focused);
            }
        }
    }

    private void shiftTo(NBTNode<NamedNBT> node) {
        int index = indexOf(node);
        if (index != -1) {
            NBTNodeComponent nodeComponent = nodes.get(index);
            shift((bottom + START_Y + 1) / 2 - (nodeComponent.y + nodeComponent.getHeight()));
        }
    }

    private int indexOf(NBTNode<NamedNBT> node) {
        for (int i = 0; i < nodes.size(); ++i) {
            if (nodes.get(i).getNode() == node) {
                return i;
            }
        }
        return -1;
    }

    public void shift(int i) {
        if (heightDiff <= 0 || subWindow != null) {
            return;
        }

        int diff = offset + i;
        if (diff > 0) {
            diff = 0;
        }
        if (diff < -heightDiff) {
            diff = -heightDiff;
        }

        for (var node : nodes) {
            node.shiftY(diff - offset);
        }
        offset = diff;
    }

    private int getHeightDifference() {
        return getContentHeight() - (bottom - START_Y + 2);
    }

    private int getContentHeight() {
        return Y_GAP * nodes.size();
    }

    private boolean checkValidFocus(NBTNode<NamedNBT> focus) {
        for (var node : nodes) { //Check all nodes
            if (node.getNode() == focus) {
                setFocused(focus);
                return true;
            }
        }
        return focus.hasParent() && checkValidFocus(focus.getParent());
    }

    public void setFocused(NBTNode<NamedNBT> focus) {
        if (focus == null) {
            for (var b : buttons) {
                b.active = false;
            }
        } else if (focus.get().getTag() instanceof CompoundTag) {
            for (var b : buttons) {
                b.active = true;
            }

            buttons[12].active = focus != tree.getRoot();
            buttons[11].active = focus.hasParent() &&
                    !(focus.getParent().get().getTag() instanceof ListTag);
            buttons[13].active = true;
            buttons[14].active = focus != tree.getRoot();
            buttons[15].active = NBTEdit.CLIPBOARD != null;
        } else if (focus.get().getTag() instanceof ListTag) {
            if (focus.hasChildren()) {
                var type = focus.getChildren().get(0).get().getTag().getId();
                for (var b : buttons) {
                    b.active = false;
                }

                buttons[type - 1].active = true;
                buttons[12].active = true;
                buttons[11].active = !(focus.getParent().get().getTag() instanceof ListTag);
                buttons[13].active = true;
                buttons[14].active = true;
                buttons[15].active = NBTEdit.CLIPBOARD != null && NBTEdit.CLIPBOARD.getTag().getId() == type;
            } else {
                for (var b : buttons) {
                    b.active = true;
                }
            }

            buttons[11].active = !(focus.getParent().get().getTag() instanceof ListTag);
            buttons[13].active = true;
            buttons[14].active = true;
            buttons[15].active = NBTEdit.CLIPBOARD != null;
        } else {
            for (var b : buttons) {
                b.active = false;
            }

            buttons[12].active = true;
            buttons[11].active = true;
            buttons[13].active = true;
            buttons[14].active = true;
            buttons[15].active = false;
        }

        focused = focus;
        if (focused != null && focusedSaveSlotIndex != -1) {
            stopEditingSlot();
        }
    }

    private void stopEditingSlot() {
        saves[focusedSaveSlotIndex].stopEditing();
        NBTEdit.getClipboardSaves().save();
        focusedSaveSlotIndex = -1;
    }

    private void addNodes(NBTNode<NamedNBT> node, int x) {
        nodes.add(new NBTNodeComponent(x, y,
                new TextComponent(NBTHelper.getNBTNameSpecial(node.get())), this, node));
        x += X_GAP;
        y += Y_GAP;

        if (node.shouldShowChildren()) {
            for (var child : node.getChildren()) {
                addNodes(child, x);
            }
        }
    }

    private void addSaveSlotButtons() {
        ClipboardStates clipboardStates = NBTEdit.getClipboardSaves();
        for (var i = 0; i < 7; ++i) {
            saves[i] = new SaveSlotButton(clipboardStates.getClipboard(i), width - 24, 31 + i * 25, this::onSaveButtonClicked);
        }
    }

    private void onSaveButtonClicked(Button button) {
        // Silence is gold *2.
    }

    private void onSaveButtonClicked(SaveSlotButton button) {
        if (button.getSave().tag.isEmpty()) {
            NBTNode<NamedNBT> node = (focused == null) ? tree.getRoot() : focused;
            Tag base = node.get().getTag();
            String name = node.get().getName();
            if (base instanceof ListTag) {
                var list = new ListTag();
                tree.addChild(node, list);
                button.getSave().tag.put(name, list);
            } else if (base instanceof CompoundTag) {
                var compound = new CompoundTag();
                tree.addChild(node, compound);
                button.getSave().tag.put(name, compound);
            } else {
                button.getSave().tag.put(name, base.copy());
            }
            button.saved();
            NBTEdit.getClipboardSaves().save();
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        } else { //Paste into
            Map<String, Tag> nbtMap = NBTIOHelper.getMap(button.getSave().tag);
            if (nbtMap.isEmpty()) {
                NBTEdit.getInstance().getInternalLogger().warn("Unable to copy from save \"" + button.getSave().name + "\".");
                NBTEdit.getInstance().getInternalLogger().warn("The save is invalid, a valid save must only contain 1 core Tag.");
            } else {
                if (focused == null) {
                    setFocused(tree.getRoot());
                }

                Map.Entry<String, Tag> firstEntry = nbtMap.entrySet().iterator().next();
                assert firstEntry != null;
                String name = firstEntry.getKey();
                Tag nbt = firstEntry.getValue().copy();
                if (focused == tree.getRoot() && nbt instanceof CompoundTag && name.equals("ROOT")) {
                    setFocused(null);
                    tree = new NBTTree((CompoundTag) nbt);
                    init();
                    getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                } else if (canAddToParent(focused.get().getTag(), nbt)) {
                    focused.setShowChildren(true);
                    for (Iterator<NBTNode<NamedNBT>> it = focused.getChildren().iterator(); it.hasNext(); ) { //Replace object with same name
                        if (it.next().get().getName().equals(name)) {
                            it.remove();
                            break;
                        }
                    }
                    NBTNode<NamedNBT> node = insert(new NamedNBT(name, nbt));
                    tree.addChildrenToTree(node);
                    tree.sort(node);
                    setFocused(node);
                    init(true);
                    getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
            }
        }
    }

    private void addButtons() {
        var x = 18;
        var y = 4;

        for (byte i = 14; i < 17; ++i) {
            buttons[i - 1] = new NBTOperatorButton(i, x, y, this::onOperatorButtonClicked);
            x += 15;
        }

        x += 30;
        for (byte i = 12; i < 14; ++i) {
            buttons[i - 1] = new NBTOperatorButton(i, x, y, this::onOperatorButtonClicked);
            x += 15;
        }

        x = 18;
        y = 17;
        for (byte i = 1; i < 12; ++i) {
            buttons[i - 1] = new NBTOperatorButton(i, x, y, this::onOperatorButtonClicked);
            x += 9;
        }
    }

    private void onOperatorButtonClicked(Button button) {
        // Silence is gold.
    }

    private void onOperatorButtonClicked(NBTOperatorButton button) {
        if (button.getButtonId() == 16) {
            paste();
        }
        else if (button.getButtonId() == 15) {
            cut();
        }
        else if (button.getButtonId() == 14) {
            copy();
        }
        else if (button.getButtonId() == 13) {
            deleteSelected();
        }
        else if (button.getButtonId() == 12) {
            edit();
        }
        else if (focused != null) {
            focused.setShowChildren(true);
            List<NBTNode<NamedNBT>> children = focused.getChildren();
            String type = NBTHelper.getButtonName(button.getButtonId());

            if (focused.get().getTag() instanceof ListTag) {
                Tag nbt = NBTHelper.newTag(button.getButtonId());
                if (nbt != null) {
                    NBTNode<NamedNBT> newNode = new NBTNode<>(focused, new NamedNBT("", nbt));
                    children.add(newNode);
                    setFocused(newNode);
                }
            } else if (children.size() == 0) {
                setFocused(insert(type + "1", button.getButtonId()));
            } else {
                for (int i = 1; i <= children.size() + 1; ++i) {
                    String name = type + i;
                    if (validName(name, children)) {
                        setFocused(insert(name, button.getButtonId()));
                        break;
                    }
                }
            }
            init(true);
        }
    }

    private boolean validName(String name, List<NBTNode<NamedNBT>> list) {
        for (NBTNode<NamedNBT> node : list) {
            if (node.get().getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private NBTNode<NamedNBT> insert(NamedNBT nbt) {
        NBTNode<NamedNBT> newNode = new NBTNode<>(focused, nbt);

        if (focused.hasChildren()) {
            List<NBTNode<NamedNBT>> children = focused.getChildren();

            boolean added = false;
            for (int i = 0; i < children.size(); ++i) {
                if (NBTEdit.SORTER.compare(newNode, children.get(i)) < 0) {
                    children.add(i, newNode);
                    added = true;
                    break;
                }
            }

            if (!added) {
                children.add(newNode);
            }
        } else {
            focused.addChild(newNode);
        }
        return newNode;
    }

    private NBTNode<NamedNBT> insert(String name, byte type) {
        Tag nbt = NBTHelper.newTag(type);
        if (nbt != null) {
            return insert(new NamedNBT(name, nbt));
        }
        return null;
    }

    private void edit() {
        Tag base = focused.get().getTag();
        Tag parent = focused.getParent().get().getTag();
        subWindow = new EditSubWindowComponent(this, focused,
                !(parent instanceof ListTag), !(base instanceof CompoundTag || base instanceof ListTag),
                (width - EditSubWindowComponent.WIDTH) / 2, (height - EditSubWindowComponent.HEIGHT) / 2);
    }

    private boolean canPaste() {
        return NBTEdit.CLIPBOARD != null && focused != null && canAddToParent(focused.get().getTag(), NBTEdit.CLIPBOARD.getTag());
    }

    private boolean canAddToParent(Tag parent, Tag child) {
        if (parent instanceof CompoundTag) {
            return true;
        }
        if (parent instanceof ListTag) {
            var list = (ListTag) parent;
            return list.size() == 0 || list.getElementType() == child.getId();
        }
        return false;
    }

    private void paste() {
        if (NBTEdit.CLIPBOARD != null) {
            focused.setShowChildren(true);

            NamedNBT namedNBT = NBTEdit.CLIPBOARD.copy();
            if (focused.get().getTag() instanceof ListTag) {
                namedNBT.setName("");
                NBTNode<NamedNBT> node = new NBTNode<>(focused, namedNBT);
                focused.addChild(node);
                tree.addChildrenToTree(node);
                tree.sort(node);
                setFocused(node);
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
                NBTNode<NamedNBT> node = insert(namedNBT);
                tree.addChildrenToTree(node);
                tree.sort(node);
                setFocused(node);
            }

            init(true);
        }
    }

    private void copy() {
        if (focused != null) {
            var namedNBT = focused.get();
            if (namedNBT.getTag() instanceof ListTag) {
                var list = new ListTag();
                tree.addChild(focused, list);
                NBTEdit.CLIPBOARD = new NamedNBT(namedNBT.getName(), list);
            } else if (namedNBT.getTag() instanceof CompoundTag) {
                var compound = new CompoundTag();
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

    public void closeSubWindow() {
        subWindow = null;
    }

    public void onNodeModified(NBTNode<NamedNBT> node) {
        NBTNode<NamedNBT> parent = node.getParent();
        Collections.sort(parent.getChildren(), NBTEdit.SORTER);
        init(true);
    }

    protected void overlayBackground(int par1, int par2, int par3, int par4) {
        var tesselator = Tesselator.getInstance();
        var worldRenderer = tesselator.getBuilder();
        getMinecraft().textureManager.bindForSetup(BACKGROUND_LOCATION);
        float var6 = 32.0F;
        worldRenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Color color = new Color(4210752);
        worldRenderer.color(color.getRed(), color.getGreen(), color.getBlue(), par4);
        worldRenderer.vertex(0.0D, par2, 0.0D).uv(0.0f, (float) par2 / var6);
        worldRenderer.vertex(width, par2, 0.0D).uv((float) width / var6, (float) par2 / var6);
        worldRenderer.color(color.getRed(), color.getGreen(), color.getBlue(), par3);
        worldRenderer.vertex(width, par1, 0.0D).uv((float) width / var6, (float) par1 / var6);
        worldRenderer.vertex(0.0D, par1, 0.0D).uv(0.0f, (float) par1 / var6);
        tesselator.end();
    }

    public void mouseClicked(double par1, double par2, int par3) {
        if (subWindow == null) {
            boolean reInit = false;

            for (var node : nodes) {
                if (node.spoilerClicked((int) par1, (int) par2)) { // Check hide/show children buttons
                    reInit = true;
                    if (node.shouldShowChildren())
                        offset = (START_Y + 1) - (node.y) + offset;
                    break;
                }
            }
            if (!reInit) {
                for (var button : buttons) { //Check top buttons
                    if (button.isMouseInside((int) par1, (int) par2)) {
                        onOperatorButtonClicked(button);
                        return;
                    }
                }
                for (var button : saves) {
                    if (button.isMouseInside((int) par1, (int) par2)) {
                        button.reset();
                        NBTEdit.getClipboardSaves().save();
                        getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return;
                    }
                    if (button.isMouseInside((int) par1, (int) par2)) {
                        onSaveButtonClicked(button);
                        return;
                    }
                }
                if (par2 >= START_Y && par1 <= width - 175) { //Check actual nodes, remove focus if nothing clicked
                    NBTNode<NamedNBT> newFocus = null;
                    for (var node : nodes) {
                        if (node.isTextClicked((int) par1, (int) par2)) {
                            newFocus = node.getNode();
                            break;
                        }
                    }
                    if (focusedSaveSlotIndex != -1)
                        stopEditingSlot();
                    setFocused(newFocus);
                }
            } else {
                init();
            }
        } else {
            subWindow.mouseClicked(par1, par2, par3);
        }
    }

    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        var prevMouseX = mouseX;
        var prevMouseY = mouseY;
        if (subWindow != null) {
            prevMouseX = -1;
            prevMouseY = -1;
        }
        for (var node : nodes) {
            if (node.shouldRender(START_Y - 1, bottom)) {
                node.render(stack, prevMouseX, prevMouseY, partialTick);
            }
        }
        overlayBackground(0, START_Y - 1, 255, 255);
        overlayBackground(bottom, height, 255, 255);
        for (var button : buttons)
            button.render(stack, prevMouseX, prevMouseY, partialTick);
        for (var button : saves) {
            button.render(stack, prevMouseX, prevMouseY, partialTick);
        }
        drawScrollBar(stack, prevMouseX, prevMouseY, partialTick);
        if (subWindow != null) {
            subWindow.render(stack, mouseX, mouseY, partialTick);
        }
    }

    private void drawScrollBar(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        if (heightDiff > 0) {
            if (isKeyLeftDown()) {
                if (yClick == -1) {
                    if (mouseX >= width - 20 && mouseX < width && mouseY >= START_Y - 1 && mouseY < bottom) {
                        yClick = mouseY;
                    }
                } else {
                    float scrollMultiplier = 1.0F;
                    int height = getHeightDifference();

                    if (height < 1) {
                        height = 1;
                    }

                    int length = (bottom - (START_Y - 1)) * (bottom - (START_Y - 1)) / getContentHeight();
                    if (length < 32) {
                        length = 32;
                    }
                    if (length > bottom - (START_Y - 1) - 8) {
                        length = bottom - (START_Y - 1) - 8;
                    }

                    scrollMultiplier /= (float) (this.bottom - (START_Y - 1) - length) / (float) height;


                    shift((int) ((yClick - mouseY) * scrollMultiplier));
                    yClick = mouseY;
                }
            } else {
                yClick = -1;
            }


            fill(stack, width - 20, START_Y - 1, width, bottom, Integer.MIN_VALUE);


            int length = (bottom - (START_Y - 1)) * (bottom - (START_Y - 1)) / getContentHeight();

            if (length < 32) {
                length = 32;
            }
            if (length > bottom - (START_Y - 1) - 8) {
                length = bottom - (START_Y - 1) - 8;
            }

            int y = -offset * (this.bottom - (START_Y - 1) - length) / heightDiff + (START_Y - 1);

            if (y < START_Y - 1) {
                y = START_Y - 1;
            }

            fillGradient(stack, width - 20, y, width, y + length, 0x80ffffff, 0x80333333);
        }
    }

    private boolean isKeyLeftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.MOUSE_BUTTON_LEFT);
    }

    public NBTNode<NamedNBT> getFocused() {
        return focused;
    }
}
