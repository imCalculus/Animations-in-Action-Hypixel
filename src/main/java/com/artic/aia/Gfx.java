package com.artic.aia;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

final class Gfx {
   static final RenderPipeline GUI_ADDITIVE = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
         .withLocation(Identifier.fromNamespaceAndPath("animationinaction", "pipeline/gui_additive"))
         .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
         .withCull(false)
         .build()
   );
   static final RenderPipeline GUI_QUADS = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
         .withLocation(Identifier.fromNamespaceAndPath("animationinaction", "pipeline/gui_quads"))
         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
         .withCull(false)
         .build()
   );
   static final RenderPipeline GUI_TEXTURED_QUADS = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
         .withLocation(Identifier.fromNamespaceAndPath("animationinaction", "pipeline/gui_textured_quads"))
         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
         .withCull(false)
         .build()
   );

   private Gfx() {
   }

   static void init() {
   }

   static TextureSetup textureSetup(Identifier id) {
      AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(id);
      return TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler());
   }

   static void submit(GuiGraphicsExtractor ctx, RenderPipeline pipeline, TextureSetup textureSetup, Consumer<VertexConsumer> emitter) {
      ScreenRectangle scissor = ctx.scissorStack.peek();
      ScreenRectangle full = new ScreenRectangle(0, 0, Math.max(1, ctx.guiWidth()), Math.max(1, ctx.guiHeight()));
      ScreenRectangle bounds = scissor != null ? scissor.intersection(full) : full;
      if (bounds != null) {
         ctx.guiRenderState.addGuiElement(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer vc) {
               emitter.accept(vc);
            }

            @Override
            public RenderPipeline pipeline() {
               return pipeline;
            }

            @Override
            public TextureSetup textureSetup() {
               return textureSetup;
            }

            @Override
            public ScreenRectangle scissorArea() {
               return scissor;
            }

            @Override
            public ScreenRectangle bounds() {
               return bounds;
            }
         });
      }
   }

   static void clearDepth() {
   }

   static void additive() {
   }

   static void normalBlend() {
   }
}
