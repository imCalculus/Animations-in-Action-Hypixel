package com.artic.aia;

import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

abstract class Visual {
   abstract void draw(GuiGraphicsExtractor ctx, float cx, float cy, float s, float angleDeg);

   static Visual head(String key) {
      return new Visual.Head(key);
   }

   static Visual voxel(VoxelModel model) {
      return new Visual.Voxel(model);
   }

   static Visual item(ItemStack stack) {
      return new Visual.Stack(stack);
   }

   private static final class Head extends Visual {
      private final String key;

      Head(String key) {
         this.key = key;
      }

      @Override
      void draw(GuiGraphicsExtractor ctx, float cx, float cy, float s, float angleDeg) {
         HeadTextures.Tex t = HeadTextures.get(this.key);
         if (t == null) {
            t = HeadTextures.placeholder();
         }

         HeadRenderer.draw(ctx, t, cx, cy, 8.0F * s, angleDeg);
      }
   }

   private static final class Stack extends Visual {
      private final ItemStack stack;

      Stack(ItemStack stack) {
         this.stack = stack;
      }

      @Override
      void draw(GuiGraphicsExtractor ctx, float cx, float cy, float s, float angleDeg) {
         if (this.stack != null && !this.stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            TrackingItemStackRenderState itemState = new TrackingItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(itemState, this.stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
            int half = Math.max(8, (int)Math.ceil((double)(12.0F * s)));
            int x0 = Math.round(cx) - half;
            int y0 = Math.round(cy) - half;
            int x1 = Math.round(cx) + half;
            int y1 = Math.round(cy) + half;
            ScreenRectangle scissor = ctx.scissorStack.peek();
            Matrix3x2f pose = new Matrix3x2f(ctx.pose());
            ScreenRectangle bounds = PictureInPictureRenderState.getBounds(x0, y0, x1, y1, pose, scissor);
            ctx.guiRenderState.addPicturesInPictureState(new Visual.SpinItemState(itemState, x0, y0, x1, y1, s, angleDeg, pose, scissor, bounds));
         }
      }
   }

   static record SpinItemState(
      TrackingItemStackRenderState itemState,
      int x0,
      int y0,
      int x1,
      int y1,
      float scale,
      float angleDeg,
      Matrix3x2fc pose,
      ScreenRectangle scissorArea,
      ScreenRectangle bounds
   ) implements PictureInPictureRenderState {
   }

   static final class SpinItemRenderer extends PictureInPictureRenderer<Visual.SpinItemState> {
      @Override
      public Class<Visual.SpinItemState> getRenderStateClass() {
         return Visual.SpinItemState.class;
      }

      @Override
      protected void renderToTexture(Visual.SpinItemState state, PoseStack poseStack, SubmitNodeCollector collector) {
         poseStack.scale(16.0F, -16.0F, -16.0F);
         poseStack.rotateDegrees(Axis.YP, state.angleDeg());
         TrackingItemStackRenderState itemState = state.itemState();
         if (!itemState.usesBlockLight()) {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ITEMS_FLAT);
         } else {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ITEMS_3D);
         }

         itemState.submit(poseStack, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);
      }

      @Override
      protected float getTranslateY(int height, int guiScale) {
         return (float)height / 2.0F;
      }

      @Override
      protected String getTextureLabel() {
         return "aia_spinning_item";
      }
   }

   private static final class Voxel extends Visual {
      private final VoxelModel model;

      Voxel(VoxelModel model) {
         this.model = model;
      }

      @Override
      void draw(GuiGraphicsExtractor ctx, float cx, float cy, float s, float angleDeg) {
         this.model.draw(ctx, cx, cy, s, angleDeg);
      }
   }
}
