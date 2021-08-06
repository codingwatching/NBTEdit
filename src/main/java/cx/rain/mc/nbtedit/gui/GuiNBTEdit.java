package cx.rain.mc.nbtedit.gui;

import cx.rain.mc.nbtedit.utility.nbt.NBTTree;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.NBTNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class GuiNBTEdit extends Gui {
    private NBTTree tree;

    private NBTNode<NamedNBT> focused;
    private int focusedSaveSlot = -1;

    private GuiSaveSlotButton[] saves;

    public GuiNBTEdit(NBTTree treeIn) {
        super(Minecraft.getInstance());

        tree = treeIn;

    }

    protected Minecraft getMinecraft() {
        return minecraft;
    }

    public NBTTree getTree() {
        return tree;
    }

    public NBTNode<NamedNBT> getFocused() {
        return focused;
    }

    public GuiSaveSlotButton getFocusedSaveSlot() {
        return (focusedSaveSlot != -1) ? saves[focusedSaveSlot] : null;
    }
}
