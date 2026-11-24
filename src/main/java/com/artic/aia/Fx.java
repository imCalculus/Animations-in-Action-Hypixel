package com.artic.aia;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

final class Fx {
   private static final double TAU = Math.PI * 2;

   private Fx() {
   }

   static void begin() {
      Gfx.additive();
   }

   static void end() {
      Gfx.normalBlend();
   }

   private static int a255(float a) {
      int v = (int)(a * 255.0F);
      return v < 0 ? 0 : (v > 255 ? 255 : v);
   }

   private static void v(VertexConsumer vc, Matrix3x2fc m, double x, double y, int r, int g, int b, int a) {
      vc.addVertexWith2DPose(m, (float)x, (float)y).setColor(r, g, b, a);
   }

   static void glow(GuiGraphicsExtractor ctx, Matrix3x2fc pose, float cx, float cy, float r, int rgb, float alpha) {
      if (alpha > 0.002F && r > 0.0F) {
         int R = rgb >> 16 & 0xFF;
         int G = rgb >> 8 & 0xFF;
         int B = rgb & 0xFF;
         int A = a255(alpha);
         Matrix3x2f m = new Matrix3x2f(pose);
         int seg = 40;
         Gfx.submit(ctx, Gfx.GUI_ADDITIVE, TextureSetup.noTexture(), vc -> {
            for (int i = 0; i < seg; i++) {
               double ang0 = (double)i * TAU / (double)seg;
               double ang1 = (double)(i + 1) * TAU / (double)seg;
               double x0 = (double)cx + Math.cos(ang0) * (double)r;
               double y0 = (double)cy + Math.sin(ang0) * (double)r;
               double x1 = (double)cx + Math.cos(ang1) * (double)r;
               double y1 = (double)cy + Math.sin(ang1) * (double)r;
               v(vc, m, (double)cx, (double)cy, R, G, B, A);
               v(vc, m, x0, y0, R, G, B, 0);
               v(vc, m, x1, y1, R, G, B, 0);
               v(vc, m, x1, y1, R, G, B, 0);
            }
         });
      }
   }

   static void ring(GuiGraphicsExtractor ctx, Matrix3x2fc pose, float cx, float cy, float r, float thick, int rgb, float alpha) {
      if (alpha > 0.002F && r > 0.0F) {
         int R = rgb >> 16 & 0xFF;
         int G = rgb >> 8 & 0xFF;
         int B = rgb & 0xFF;
         int A = a255(alpha);
         Matrix3x2f m = new Matrix3x2f(pose);
         int seg = 56;
         Gfx.submit(ctx, Gfx.GUI_ADDITIVE, TextureSetup.noTexture(), vc -> {
            for (int i = 0; i < seg; i++) {
               double ang0 = (double)i * TAU / (double)seg;
               double ang1 = (double)(i + 1) * TAU / (double)seg;
               double c0 = Math.cos(ang0);
               double s0 = Math.sin(ang0);
               double c1 = Math.cos(ang1);
               double s1 = Math.sin(ang1);
               v(vc, m, (double)cx + c0 * (double)(r - thick), (double)cy + s0 * (double)(r - thick), R, G, B, 0);
               v(vc, m, (double)cx + c0 * (double)r, (double)cy + s0 * (double)r, R, G, B, A);
               v(vc, m, (double)cx + c1 * (double)r, (double)cy + s1 * (double)r, R, G, B, A);
               v(vc, m, (double)cx + c1 * (double)(r - thick), (double)cy + s1 * (double)(r - thick), R, G, B, 0);
            }

            for (int i = 0; i < seg; i++) {
               double ang0 = (double)i * TAU / (double)seg;
               double ang1 = (double)(i + 1) * TAU / (double)seg;
               double c0 = Math.cos(ang0);
               double s0 = Math.sin(ang0);
               double c1 = Math.cos(ang1);
               double s1 = Math.sin(ang1);
               v(vc, m, (double)cx + c0 * (double)r, (double)cy + s0 * (double)r, R, G, B, A);
               v(vc, m, (double)cx + c0 * (double)(r + thick), (double)cy + s0 * (double)(r + thick), R, G, B, 0);
               v(vc, m, (double)cx + c1 * (double)(r + thick), (double)cy + s1 * (double)(r + thick), R, G, B, 0);
               v(vc, m, (double)cx + c1 * (double)r, (double)cy + s1 * (double)r, R, G, B, A);
            }
         });
      }
   }

   static Fx.TriBatch beginTris() {
      return new Fx.TriBatch();
   }

   static void endTris(GuiGraphicsExtractor ctx, Fx.TriBatch batch) {
      batch.submit(ctx);
   }

   static void wedge(Fx.TriBatch bb, Matrix3x2fc m, float cx, float cy, float ang, float len, float half, int rgb, float aApex, float aTip) {
      int R = rgb >> 16 & 0xFF;
      int G = rgb >> 8 & 0xFF;
      int B = rgb & 0xFF;
      float x1 = cx + (float)(Math.cos((double)(ang - half)) * (double)len);
      float y1 = cy + (float)(Math.sin((double)(ang - half)) * (double)len);
      float x2 = cx + (float)(Math.cos((double)(ang + half)) * (double)len);
      float y2 = cy + (float)(Math.sin((double)(ang + half)) * (double)len);
      bb.tri(m, cx, cy, R, G, B, a255(aApex), x1, y1, R, G, B, a255(aTip), x2, y2, R, G, B, a255(aTip));
   }

   static void sparkle(Fx.TriBatch bb, Matrix3x2fc m, float x, float y, float s, float alpha) {
      int A = a255(alpha);
      int A0 = 0;
      float w = s * 0.14F;
      bb.tri(m, x - s, y, 255, 255, 255, A0, x, y - w, 255, 255, 255, A, x + s, y, 255, 255, 255, A0);
      bb.tri(m, x - s, y, 255, 255, 255, A0, x + s, y, 255, 255, 255, A0, x, y + w, 255, 255, 255, A);
      bb.tri(m, x, y - s, 255, 255, 255, A0, x - w, y, 255, 255, 255, A, x, y + s, 255, 255, 255, A0);
      bb.tri(m, x, y - s, 255, 255, 255, A0, x, y + s, 255, 255, 255, A0, x + w, y, 255, 255, 255, A);
   }

   static final class TriBatch {
      private float[] xy = new float[256];
      private int[] rgba = new int[128];
      private int count;

      void tri(
         Matrix3x2fc m,
         float x0,
         float y0,
         int r0,
         int g0,
         int b0,
         int a0,
         float x1,
         float y1,
         int r1,
         int g1,
         int b1,
         int a1,
         float x2,
         float y2,
         int r2,
         int g2,
         int b2,
         int a2
      ) {
         this.vert(m, x0, y0, r0, g0, b0, a0);
         this.vert(m, x1, y1, r1, g1, b1, a1);
         this.vert(m, x2, y2, r2, g2, b2, a2);
         this.vert(m, x2, y2, r2, g2, b2, a2);
      }

      private void vert(Matrix3x2fc m, float x, float y, int r, int g, int b, int a) {
         if (this.count * 2 >= this.xy.length) {
            float[] nxy = new float[this.xy.length * 2];
            System.arraycopy(this.xy, 0, nxy, 0, this.xy.length);
            this.xy = nxy;
            int[] nrgba = new int[this.rgba.length * 2];
            System.arraycopy(this.rgba, 0, nrgba, 0, this.rgba.length);
            this.rgba = nrgba;
         }

         float tx = m.m00() * x + m.m10() * y + m.m20();
         float ty = m.m01() * x + m.m11() * y + m.m21();
         this.xy[this.count * 2] = tx;
         this.xy[this.count * 2 + 1] = ty;
         this.rgba[this.count] = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
         this.count++;
      }

      void submit(GuiGraphicsExtractor ctx) {
         if (this.count > 0) {
            int n = this.count;
            float[] coords = this.xy;
            int[] cols = this.rgba;
            Gfx.submit(ctx, Gfx.GUI_ADDITIVE, TextureSetup.noTexture(), vc -> {
               for (int i = 0; i < n; i++) {
                  int c = cols[i];
                  vc.addVertex(coords[i * 2], coords[i * 2 + 1], 0.0F)
                     .setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, c >>> 24 & 0xFF);
               }
            });
         }
      }
   }
}
