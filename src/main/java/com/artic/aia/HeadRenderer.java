package com.artic.aia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class HeadRenderer {
   private static final float TILT_DEG = 30.0F;
   private static final float BASE_DEG = 45.0F;
   private static final HeadRenderer.Face[] FACES = new HeadRenderer.Face[]{
      new HeadRenderer.Face(
         0.0F,
         0.0F,
         1.0F,
         new float[]{-1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F},
         new float[]{8.0F, 8.0F, 16.0F, 8.0F, 16.0F, 16.0F, 8.0F, 16.0F}
      ),
      new HeadRenderer.Face(
         0.0F,
         0.0F,
         -1.0F,
         new float[]{1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F},
         new float[]{24.0F, 8.0F, 32.0F, 8.0F, 32.0F, 16.0F, 24.0F, 16.0F}
      ),
      new HeadRenderer.Face(
         -1.0F,
         0.0F,
         0.0F,
         new float[]{-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, -1.0F},
         new float[]{0.0F, 8.0F, 8.0F, 8.0F, 8.0F, 16.0F, 0.0F, 16.0F}
      ),
      new HeadRenderer.Face(
         1.0F,
         0.0F,
         0.0F,
         new float[]{1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F},
         new float[]{16.0F, 8.0F, 24.0F, 8.0F, 24.0F, 16.0F, 16.0F, 16.0F}
      ),
      new HeadRenderer.Face(
         0.0F,
         -1.0F,
         0.0F,
         new float[]{-1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F},
         new float[]{8.0F, 0.0F, 16.0F, 0.0F, 16.0F, 8.0F, 8.0F, 8.0F}
      ),
      new HeadRenderer.Face(
         0.0F,
         1.0F,
         0.0F,
         new float[]{-1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F},
         new float[]{16.0F, 0.0F, 24.0F, 0.0F, 24.0F, 8.0F, 16.0F, 8.0F}
      )
   };

   private HeadRenderer() {
   }

   private static float shade(float d) {
      d = Math.max(0.0F, d);
      return 0.5F + 0.5F * Math.min(1.0F, d / 0.8F);
   }

   static void draw(GuiGraphicsExtractor ctx, HeadTextures.Tex tex, float cx, float cy, float edge, float spinDeg) {
      Quaternionf q = new Quaternionf()
         .rotateY((float)Math.toRadians((double)spinDeg))
         .rotateX((float)Math.toRadians((double)(-TILT_DEG)))
         .rotateY((float)Math.toRadians((double)BASE_DEG));
      float lx = -0.42F;
      float ly = -0.55F;
      float lz = 0.72F;
      float[] shades = new float[FACES.length];
      Vector3f n = new Vector3f();

      for (int i = 0; i < FACES.length; i++) {
         n.set(FACES[i].nx, FACES[i].ny, FACES[i].nz);
         q.transform(n);
         shades[i] = shade(n.x * lx + n.y * ly + n.z * lz);
      }

      float h = edge * 0.5F;
      float hh = h * 1.0625F;
      float uScale = 0.015625F;
      float vScale = 1.0F / (64.0F * (float)tex.h / (float)tex.w);
      Matrix3x2f pose2d = new Matrix3x2f(ctx.pose());
      List<float[]> quads = new ArrayList<>(12);
      Vector3f v = new Vector3f();

      for (int layer = 0; layer < 2; layer++) {
         float half = layer == 0 ? h : hh;
         float uOff = layer == 0 ? 0.0F : 32.0F;

         for (int i = 0; i < FACES.length; i++) {
            HeadRenderer.Face f = FACES[i];
            float col = (int)(255.0F * shades[i]);
            float[] qd = new float[18];
            float zSum = 0.0F;

            for (int k = 0; k < 4; k++) {
               v.set(f.c[k * 3] * half, f.c[k * 3 + 1] * half, f.c[k * 3 + 2] * half);
               q.transform(v);
               qd[k * 4] = cx + v.x;
               qd[k * 4 + 1] = cy + v.y;
               qd[k * 4 + 2] = (f.uv[k * 2] + uOff) * uScale;
               qd[k * 4 + 3] = f.uv[k * 2 + 1] * vScale;
               zSum += v.z;
            }

            qd[16] = zSum * 0.25F;
            qd[17] = col;
            quads.add(qd);
         }
      }

      quads.sort(Comparator.comparingDouble(a -> (double)a[16]));
      Gfx.submit(ctx, Gfx.GUI_TEXTURED_QUADS, Gfx.textureSetup(tex.id), vc -> {
         for (float[] qd : quads) {
            int col = (int)qd[17];

            for (int k = 0; k < 4; k++) {
               vc.addVertexWith2DPose(pose2d, qd[k * 4], qd[k * 4 + 1])
                  .setUv(qd[k * 4 + 2], qd[k * 4 + 3])
                  .setColor(col, col, col, 255);
            }
         }
      });
   }

   private static final class Face {
      final float nx;
      final float ny;
      final float nz;
      final float[] c;
      final float[] uv;

      Face(float nx, float ny, float nz, float[] c, float[] uv) {
         this.nx = nx;
         this.ny = ny;
         this.nz = nz;
         this.c = c;
         this.uv = uv;
      }
   }
}
