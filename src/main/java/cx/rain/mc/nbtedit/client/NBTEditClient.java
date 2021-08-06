package cx.rain.mc.nbtedit.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.gui.screen.NBTEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = NBTEdit.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NBTEditClient {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        var currentScreen = Minecraft.getInstance().screen;

        if (currentScreen instanceof NBTEditScreen nbtedit) {
            if (nbtedit.isEntity()) {
                var entity = nbtedit.getEntity();
                if (entity.isAlive()) {
                    drawBox(event.getContext(), event.getPartialTicks(), entity.getBoundingBox());
                }
            } else {
                var pos = nbtedit.getBlockPos();
                var level = Minecraft.getInstance().level;
                var state = level.getBlockState(pos);
                var block = state.getBlock();

                drawBox(event.getContext(), event.getPartialTicks(), state.getInteractionShape(level, pos).bounds());
            }
        }
    }

    private void drawBox(LevelRenderer context, float partialTicks, AABB aabb) {
        if (aabb == null) {
            return;
        }

        Entity player = Minecraft.getInstance().getCameraEntity();

        double var8 = player.xOld + (player.getX() - player.xOld) * (double) partialTicks;
        double var10 = player.yOld + (player.getY() - player.yOld) * (double) partialTicks;
        double var12 = player.zOld + (player.getZ() - player.zOld) * (double) partialTicks;

        aabb = aabb.expandTowards(-var8, -var10, -var12);

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._clearColor(1.0F, 0.0F, 0.0F, .5F);
        GL11.glLineWidth(3.5F);
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder worldRenderer = tesselator.getBuilder();

        worldRenderer.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        worldRenderer.vertex(aabb.minX, aabb.minY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.minY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.minY, aabb.maxZ);
        worldRenderer.vertex(aabb.minX, aabb.minY, aabb.maxZ);
        worldRenderer.vertex(aabb.minX, aabb.minY, aabb.minZ);
        tesselator.end();
        worldRenderer.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        worldRenderer.vertex(aabb.minX, aabb.maxY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.maxY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        worldRenderer.vertex(aabb.minX, aabb.maxY, aabb.maxZ);
        worldRenderer.vertex(aabb.minX, aabb.maxY, aabb.minZ);
        tesselator.end();
        worldRenderer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        worldRenderer.vertex(aabb.minX, aabb.minY, aabb.minZ);
        worldRenderer.vertex(aabb.minX, aabb.maxY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.minY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.maxY, aabb.minZ);
        worldRenderer.vertex(aabb.maxX, aabb.minY, aabb.maxZ);
        worldRenderer.vertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        worldRenderer.vertex(aabb.minX, aabb.minY, aabb.maxZ);
        worldRenderer.vertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tesselator.end();

        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
        GlStateManager._disableBlend();
    }
}