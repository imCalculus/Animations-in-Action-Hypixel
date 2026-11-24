package com.artic.aia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import org.joml.Matrix3x2f;

final class VoxelModel {
   private static final int N = 16;
   private static final int DEPTH = 3;
   private static final Map<String, VoxelModel> CACHE = new HashMap<>();
   private final boolean[][] solid = new boolean[N][N];
   private final int[][] rgb = new int[N][N];

   private VoxelModel() {
   }

   static VoxelModel get(ItemLook look) {
      String key = look.itemName + "|" + look.damage + "|" + look.tint;
      VoxelModel m = CACHE.get(key);
      if (m != null) {
         return m;
      } else {
         try {
            m = build(look);
         } catch (Throwable t) {
            AIAConfig.log("voxel model failed for " + key + ": " + t);
         }

         if (m != null) {
            CACHE.put(key, m);
         }

         return m;
      }
   }

   private static VoxelModel build(ItemLook look) {
      Sprites.Img base = Sprites.read(look.itemName, look.damage, false);
      if (base == null) {
         return null;
      } else {
         Sprites.Img over = look.tint >= 0 ? Sprites.read(look.itemName, look.damage, true) : null;
         VoxelModel m = new VoxelModel();
         boolean overlayTint = look.itemName != null && (look.itemName.endsWith("firework_charge") || look.itemName.endsWith("firework_star"));
         boolean any = false;

         for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
               int c = base.get(
                  Math.min(base.w - 1, (int)(((float)x + 0.5F) * (float)base.w / (float)N)),
                  Math.min(base.h - 1, (int)(((float)y + 0.5F) * (float)base.h / (float)N))
               );
               int r = c >> 16 & 0xFF;
               int g = c >> 8 & 0xFF;
               int b = c & 0xFF;
               boolean on = (c >>> 24 & 0xFF) >= 128;
               if (look.tint >= 0 && !overlayTint) {
                  r = r * (look.tint >> 16 & 0xFF) / 255;
                  g = g * (look.tint >> 8 & 0xFF) / 255;
                  b = b * (look.tint & 0xFF) / 255;
               }

               if (over != null) {
                  int oc = over.get(
                     Math.min(over.w - 1, (int)(((float)x + 0.5F) * (float)over.w / (float)N)),
                     Math.min(over.h - 1, (int)(((float)y + 0.5F) * (float)over.h / (float)N))
                  );
                  if ((oc >>> 24 & 0xFF) >= 128) {
                     r = oc >> 16 & 0xFF;
                     g = oc >> 8 & 0xFF;
                     b = oc & 0xFF;
                     if (overlayTint) {
                        r = r * (look.tint >> 16 & 0xFF) / 255;
                        g = g * (look.tint >> 8 & 0xFF) / 255;
                        b = b * (look.tint & 0xFF) / 255;
                     }

                     on = true;
                  }
               }

               m.solid[x][y] = on;
               m.rgb[x][y] = r << 16 | g << 8 | b;
               any |= on;
            }
         }

         return any ? m : null;
      }
   }

   private static float shade(float d) {
      d = Math.max(0.0F, d);
      return 0.5F + 0.5F * Math.min(1.0F, d / 0.8F);
   }

   void draw(GuiGraphicsExtractor ctx, float cx, float cy, float s, float angleDeg) {
      double rad = Math.toRadians((double)angleDeg);
      float cs = (float)Math.cos(rad);
      float sn = (float)Math.sin(rad);
      float lx = -0.42F;
      float ly = -0.55F;
      float lz = 0.72F;
      float bLeft = shade(-cs * lx + sn * lz);
      float bRight = shade(cs * lx - sn * lz);
      float bTop = shade(-ly);
      float bBottom = shade(ly);
      float bFront = shade(sn * lx + cs * lz);
      float bBack = shade(-sn * lx - cs * lz);
      float x0 = -8.0F * s;
      float y0 = -8.0F * s;
      float zh = (float)DEPTH * 0.5F * s;
      List<float[]> quads = new ArrayList<>(512);

      for (int py = 0; py < N; py++) {
         for (int px = 0; px < N; px++) {
            if (this.solid[px][py]) {
               int c = this.rgb[px][py];
               float X0 = x0 + (float)px * s;
               float X1 = X0 + s;
               float Y0 = y0 + (float)py * s;
               float Y1 = Y0 + s;
               addQuad(quads, cx, cy, cs, sn, X0, Y0, zh, X0, Y1, zh, X1, Y1, zh, X1, Y0, zh, c, bFront);
               addQuad(quads, cx, cy, cs, sn, X1, Y0, -zh, X1, Y1, -zh, X0, Y1, -zh, X0, Y0, -zh, c, bBack);
               if (px == 0 || !this.solid[px - 1][py]) {
                  addQuad(quads, cx, cy, cs, sn, X0, Y0, -zh, X0, Y1, -zh, X0, Y1, zh, X0, Y0, zh, c, bLeft);
               }

               if (px == N - 1 || !this.solid[px + 1][py]) {
                  addQuad(quads, cx, cy, cs, sn, X1, Y0, zh, X1, Y1, zh, X1, Y1, -zh, X1, Y0, -zh, c, bRight);
               }

               if (py == 0 || !this.solid[px][py - 1]) {
                  addQuad(quads, cx, cy, cs, sn, X0, Y0, zh, X1, Y0, zh, X1, Y0, -zh, X0, Y0, -zh, c, bTop);
               }

               if (py == N - 1 || !this.solid[px][py + 1]) {
                  addQuad(quads, cx, cy, cs, sn, X0, Y1, -zh, X1, Y1, -zh, X1, Y1, zh, X0, Y1, zh, c, bBottom);
               }
            }
         }
      }

      quads.sort(Comparator.comparingDouble(a -> (double)a[8]));
      Matrix3x2f pose2d = new Matrix3x2f(ctx.pose());
      Gfx.submit(ctx, Gfx.GUI_QUADS, TextureSetup.noTexture(), vc -> {
         for (float[] qd : quads) {
            int r = (int)qd[9];
            int g = (int)qd[10];
            int b = (int)qd[11];
            vc.addVertexWith2DPose(pose2d, qd[0], qd[1]).setColor(r, g, b, 255);
            vc.addVertexWith2DPose(pose2d, qd[2], qd[3]).setColor(r, g, b, 255);
            vc.addVertexWith2DPose(pose2d, qd[4], qd[5]).setColor(r, g, b, 255);
            vc.addVertexWith2DPose(pose2d, qd[6], qd[7]).setColor(r, g, b, 255);
         }
      });
   }

   private static void addQuad(
      List<float[]> quads,
      float cx,
      float cy,
      float cs,
      float sn,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float dx,
      float dy,
      float dz,
      float ex,
      float ey,
      float ez,
      int rgb,
      float b
   ) {
      float rx0 = cx + ax * cs + az * sn;
      float rz0 = -ax * sn + az * cs;
      float rx1 = cx + bx * cs + bz * sn;
      float rz1 = -bx * sn + bz * cs;
      float rx2 = cx + dx * cs + dz * sn;
      float rz2 = -dx * sn + dz * cs;
      float rx3 = cx + ex * cs + ez * sn;
      float rz3 = -ex * sn + ez * cs;
      int r = (int)((float)(rgb >> 16 & 0xFF) * b);
      int g = (int)((float)(rgb >> 8 & 0xFF) * b);
      int bl = (int)((float)(rgb & 0xFF) * b);
      quads.add(new float[]{rx0, cy + ay, rx1, cy + by, rx2, cy + dy, rx3, cy + ey, (rz0 + rz1 + rz2 + rz3) * 0.25F, (float)r, (float)g, (float)bl});
   }
}
