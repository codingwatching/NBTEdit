package cx.rain.mc.nbtedit.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.gui.NBTEditGui;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import cx.rain.mc.nbtedit.networking.packet.C2SEntityNBTSavePacket;
import cx.rain.mc.nbtedit.networking.packet.C2STileNBTSavePacket;
import cx.rain.mc.nbtedit.utility.EntityHelper;
import cx.rain.mc.nbtedit.utility.nbt.NBTTree;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

public class NBTEditScreen extends Screen {
    protected final boolean isEntity;

    protected UUID uuid;
    protected int id;
    protected boolean isMe;

    protected BlockPos pos;

    protected NBTEditGui gui;

    public NBTEditScreen(UUID uuidIn, CompoundTag tag, boolean isMeIn) {
        super(new TranslatableComponent(TranslateKeys.TITLE_NBTEDIT_ENTITY_GUI.getKey(), uuidIn));
        minecraft = Minecraft.getInstance();

        isEntity = true;
        uuid = uuidIn;
        isMe = isMeIn;

        gui = new NBTEditGui(new NBTTree(tag));
    }

    public NBTEditScreen(BlockPos posIn, CompoundTag tag) {
        super(new TranslatableComponent(TranslateKeys.TITLE_NBTEDIT_TILE_GUI.getKey(),
                posIn.getX(), posIn.getY(), posIn.getZ()));
        minecraft = Minecraft.getInstance();

        isEntity = false;
        pos	= posIn;

        gui = new NBTEditGui(new NBTTree(tag));
    }

    public boolean isEntity() {
        return isEntity;
    }

    public Entity getEntity() {
        if (!isEntity) {
            throw new UnsupportedOperationException("Cannot get Entity by an TileEntity!");
        }

        return EntityHelper.getEntityByUuidClient(id);
    }

    public BlockPos getBlockPos() {
        if (isEntity) {
            throw new UnsupportedOperationException("Cannot get block position of an Entity!");
        }

        return pos;
    }

    @Override
    protected void init() {
        super.init();

        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
        clearWidgets();

        gui.init(width, height, height - 35);

        addRenderableWidget(new Button(23, 17, width / 4 - 100, this.height - 27, new TextComponent("Save"), this::onSaveClicked));
        addRenderableWidget(new Button(23, 17, width * 3 / 4 - 100, this.height - 27, new TextComponent("Quit"), this::onQuitClicked));
    }

    private void onSaveClicked(Button button) {
        doSave();

        doClose();
    }

    private void onQuitClicked(Button button) {
        doClose();
    }

    private void doSave() {
        if (isEntity) {
            NBTEditNetworking.getInstance().getChannel().sendToServer(new C2SEntityNBTSavePacket(getEntity().getUUID(), getEntity().getId(), gui.getTree().toCompound(), false));
        } else {
            NBTEditNetworking.getInstance().getChannel().sendToServer(new C2STileNBTSavePacket(pos, gui.getTree().toCompound()));
        }
    }

    private void doClose() {
        getMinecraft().setScreen(null);
        getMinecraft().cursorEntered();
    }

    @Override
    public void onClose() {
        super.onClose();

        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean charTyped(char character, int keyId) {
        var subWindow = gui.getSubWindow();
        if (subWindow != null)
            subWindow.charTyped(character, keyId);
        else {
            if (keyId == 1) {
                if (gui.isEditingSlot()) {
                    gui.stopEditingSlot();
                } else {
                    doClose();
                }
            } else if (keyId == InputConstants.KEY_DELETE) {
                gui.doDeleteSelected();
            } else if (keyId == InputConstants.KEY_RETURN) {
                gui.doEditSelected();
            } else if (keyId == InputConstants.KEY_UP) {
                gui.arrowKeyPressed(true);
            } else if (keyId == InputConstants.KEY_DOWN) {
                gui.arrowKeyPressed(false);
            } else {
                gui.charTyped(character, keyId);
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        super.mouseScrolled(mouseX, mouseY, delta);

        int ofs = (int) delta;
        if (ofs != 0) {
            gui.shiftY((ofs >= 1) ? 6 : -6);
        }
        return true;
    }

    @Override
    public void tick() {
        if (!getMinecraft().player.isAlive()) {
            doSave();
            doClose();
        }
        else {
            gui.update();
        }
    }

    @Override
    public boolean mouseReleased(double par1, double par2, int par3) {
        gui.onMouseRelease(Mth.floor(par1), Mth.floor(par2), par3);
        return super.mouseReleased(par1, par2, par3);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        renderBackground(stack);
        gui.render(stack, mouseX, mouseY, partialTick);
        drawCenteredString(stack, getMinecraft().font, title, this.width / 2, 5, 16777215);

        if (gui.getSubWindow() == null) {
            super.render(stack, mouseX, mouseY, partialTick);
        } else {
            super.render(stack, -1, -1, partialTick);
        }
    }
}
