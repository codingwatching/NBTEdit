package cx.rain.mc.nbtedit.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import cx.rain.mc.nbtedit.gui.NBTEditGui;
import cx.rain.mc.nbtedit.utility.EntityHelper;
import cx.rain.mc.nbtedit.utility.nbt.NBTTree;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

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
        renderables.clear();

        gui.init(width, height, height - 35);
    }

    @Override
    public boolean mouseClicked(double par1, double par2, int par3) {
        gui.mouseClicked(par1, par2, par3);
        return true;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        gui.render(stack, mouseX, mouseY, partialTick);
    }
}
