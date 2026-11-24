package com.artic.aia;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

public class DropAnimation {
   final DropEntry entry;
   final String name;
   final Rarity rarity;
   final Visual visual;
   final float dur;
   long startMs = -1L;
   private static final int SPIKES = 26;
   private static final int STREAKS = 7;
   private static final int SPARKS = 14;
   private final float[] spikeOff = new float[SPIKES];
   private final float[] spikeLen = new float[SPIKES];
   private final float[] spikePh = new float[SPIKES];
   private final float[] stAng = new float[STREAKS];
   private final float[] stPer = new float[STREAKS];
   private final float[] stOff = new float[STREAKS];
   private final float[] stSweep = new float[STREAKS];
   private final float[] stLen = new float[STREAKS];
   private final float[] stW = new float[STREAKS];
   private final float[] spAng = new float[SPARKS];
   private final float[] spRad = new float[SPARKS];
   private final float[] spPh = new float[SPARKS];
   private final float[] spSz = new float[SPARKS];

   DropAnimation(DropEntry entry, String name, Rarity rarity, Visual visual) {
      this.entry = entry;
      this.name = name;
      this.rarity = rarity;
      this.visual = visual;
      this.dur = AIAConfig.durationSec;
      Random r = new Random(System.nanoTime() ^ (long)name.hashCode());

      for (int i = 0; i < SPIKES; i++) {
         this.spikeOff[i] = (float)((double)r.nextFloat() * 0.18);
         this.spikeLen[i] = 0.8F + r.nextFloat() * 1.2F;
         this.spikePh[i] = r.nextFloat() * 6.28F;
      }

      for (int i = 0; i < STREAKS; i++) {
         this.stAng[i] = r.nextFloat() * 6.2832F;
         this.stPer[i] = 0.9F + r.nextFloat() * 1.1F;
         this.stOff[i] = r.nextFloat();
         this.stSweep[i] = (float)(r.nextBoolean() ? 1 : -1) * (0.15F + r.nextFloat() * 0.35F);
         this.stLen[i] = 0.55F + r.nextFloat() * 0.45F;
         this.stW[i] = 0.012F + r.nextFloat() * 0.02F;
      }

      for (int i = 0; i < SPARKS; i++) {
         this.spAng[i] = r.nextFloat() * 6.2832F;
         this.spRad[i] = 1.1F + r.nextFloat() * 1.5F;
         this.spPh[i] = r.nextFloat();
         this.spSz[i] = 0.5F + r.nextFloat() * 0.9F;
      }
   }

   boolean finished(long now) {
      return this.startMs >= 0L && (float)(now - this.startMs) / 1000.0F >= this.dur;
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   private static float easeOutBack(float p) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      float q = p - 1.0F;
      return 1.0F + c3 * q * q * q + c1 * q * q;
   }

   void render(GuiGraphicsExtractor ctx, int w, int h) {
      Minecraft mc = Minecraft.getInstance();
      long now = System.currentTimeMillis();
      if (this.startMs < 0L) {
         this.startMs = now;
      }

      float t = (float)(now - this.startMs) / 1000.0F;
      float D = this.dur;
      float flashIn = 0.22F;
      float burstEnd = 0.62F;
      float outStart = Math.max(1.12F, D - 1.0F);
      float outFlash = Math.max(outStart + 0.2F, D - 0.45F);
      ctx.nextStratum();
      Gfx.normalBlend();
      float S = AIAConfig.size;
      float R = 8.0F * S;
      float cx = (float)w / 2.0F;
      float cy = (float)h * 0.42F + (float)Math.sin((double)(t * 1.7F)) * 3.0F;
      if (AIAConfig.dim) {
         float d = clamp01(t / 0.4F);
         if (t > D - 0.5F) {
            d *= clamp01((D - t) / 0.5F);
         }

         int a = (int)(d * 0.6F * 255.0F);
         if (a > 0) {
            ctx.fill(0, 0, w, h, a << 24);
            ctx.nextStratum();
         }
      }

      float appear = t < flashIn ? 0.0F : clamp01((t - flashIn) / 0.3F);
      float boost = t < flashIn ? 0.0F : 1.0F + 1.6F * (float)Math.exp((double)(-(t - flashIn) * 3.2F));
      float fxs = (t >= outFlash ? 0.0F : appear * boost) * AIAConfig.rayIntensity;
      float pop;
      if (t < flashIn) {
         pop = 0.0F;
      } else if (t < burstEnd) {
         pop = easeOutBack((t - flashIn) / (burstEnd - flashIn));
      } else if (t < outFlash) {
         pop = 1.0F;
      } else {
         pop = 0.0F;
      }

      float pulse = 0.85F + 0.15F * (float)Math.sin((double)(t * 4.0F));
      if (fxs > 0.01F) {
         this.drawRays(ctx, cx, cy, R, t, fxs, pulse, flashIn, w, h);
         ctx.nextStratum();
      }

      if (pop > 0.02F) {
         float angle = t * AIAConfig.spinSpeed;
         if (this.visual != null) {
            this.visual.draw(ctx, cx, cy, S * pop, angle);
            ctx.nextStratum();
         }

         Gfx.normalBlend();
      }

      if (fxs > 0.01F) {
         this.drawSparkles(ctx, cx, cy, R, t, fxs);
         ctx.nextStratum();
      }

      float ta = t < burstEnd ? 0.0F : clamp01((t - burstEnd) / 0.3F);
      if (t > outStart) {
         ta *= clamp01((outFlash - t) / (outFlash - outStart));
      }

      int alpha = (int)(ta * 255.0F);
      if (alpha > 8) {
         Font fr = mc.font;
         float ns = Math.max(1.4F, Math.min(2.6F, 0.6F + S * 0.2F));
         float ty = cy + R + 10.0F;
         drawCentered(ctx, fr, this.name, cx, ty, ns, alpha << 24 | this.rarity.primary);
         drawCentered(ctx, fr, this.rarity.label, cx, ty + 9.0F * ns + 3.0F, ns * 0.6F, alpha << 24 | this.rarity.secondary);
         ctx.nextStratum();
      }

      if (AIAConfig.flash) {
         float f;
         if (t < flashIn) {
            f = t / flashIn;
         } else if (t < burstEnd) {
            f = 1.0F - (t - flashIn) / (burstEnd - flashIn);
         } else if (t >= outStart && t < outFlash) {
            f = (t - outStart) / (outFlash - outStart);
         } else if (t >= outFlash) {
            f = 1.0F - (t - outFlash) / Math.max(0.05F, D - outFlash);
         } else {
            f = 0.0F;
         }

         int fa = (int)(clamp01(f) * 0.92F * 255.0F);
         if (fa > 0) {
            ctx.fill(0, 0, w, h, fa << 24 | 0xFFFFFF);
            ctx.nextStratum();
         }
      }

      Gfx.normalBlend();
   }

   private void drawRays(GuiGraphicsExtractor ctx, float cx, float cy, float R, float t, float fxs, float pulse, float flashIn, int w, int h) {
      int c1 = this.rarity.primary;
      int c2 = this.rarity.secondary;
      float maxLen = (float)Math.max(w, h) * 0.65F;
      float since = Math.max(0.0F, t - flashIn);
      Matrix3x2f m = new Matrix3x2f(ctx.pose());
      Fx.begin();
      float bubble = clamp01(1.0F - since / 0.7F);
      if (bubble > 0.0F) {
         Fx.glow(ctx, m, cx, cy, R * (1.6F + since * 1.5F), 0xFFFFFF, 0.45F * bubble);
      }

      if (since > 0.0F && since < 0.9F) {
         float p = since / 0.9F;
         Fx.ring(ctx, m, cx, cy, R * (0.7F + 2.6F * p), 3.0F + 5.0F * p, c2, (1.0F - p) * 0.8F * AIAConfig.rayIntensity);
      }

      Fx.glow(ctx, m, cx, cy, R * 2.7F, c1, 0.3F * fxs * pulse);
      Fx.glow(ctx, m, cx, cy, R * 1.6F, c2, 0.5F * fxs * pulse);
      Fx.glow(ctx, m, cx, cy, R * 0.95F, 0xFFFFFF, 0.55F * fxs * pulse);
      Fx.TriBatch wr = Fx.beginTris();

      for (int i = 0; i < SPIKES; i++) {
         float ang = (float)i * ((float)(Math.PI * 2.0) / (float)SPIKES) + this.spikeOff[i] + t * 0.35F;
         float len = R * (1.2F + this.spikeLen[i] * 1.1F) * (0.82F + 0.18F * (float)Math.sin((double)(t * 5.0F + this.spikePh[i])));
         float half = 0.016F + 0.01F * (float)(i % 3);
         int col = i % 3 == 0 ? 0xFFFFFF : (i % 3 == 1 ? c2 : c1);
         Fx.wedge(wr, m, cx, cy, ang, len, half, col, 0.95F * Math.min(1.0F, fxs), 0.0F);
      }

      for (int k = 0; k < STREAKS; k++) {
         float ph = (t / this.stPer[k] + this.stOff[k]) % 1.0F;
         float life = (float)Math.sin(Math.PI * (double)ph);
         if (life > 0.02F) {
            float ang = this.stAng[k] + (ph - 0.5F) * this.stSweep[k];
            float len = Math.min(maxLen, maxLen * this.stLen[k]);
            int col = k % 3 == 0 ? c1 : (k % 3 == 1 ? c2 : 0xFFFFFF);
            Fx.wedge(wr, m, cx, cy, ang, len, this.stW[k], col, 0.9F * life * Math.min(1.0F, fxs), 0.1F * life);
         }
      }

      Fx.endTris(ctx, wr);
      Fx.end();
   }

   private void drawSparkles(GuiGraphicsExtractor ctx, float cx, float cy, float R, float t, float fxs) {
      Matrix3x2f m = new Matrix3x2f(ctx.pose());
      Fx.begin();
      Fx.TriBatch wr = Fx.beginTris();

      for (int i = 0; i < SPARKS; i++) {
         float ph = (t * 0.8F + this.spPh[i]) % 1.0F;
         float a = (float)Math.sin(Math.PI * (double)ph);
         if (a > 0.05F) {
            float x = cx + (float)Math.cos((double)(this.spAng[i] + t * 0.15F)) * R * this.spRad[i];
            float y = cy + (float)Math.sin((double)(this.spAng[i] + t * 0.15F)) * R * this.spRad[i];
            Fx.sparkle(wr, m, x, y, (4.0F + R * 0.16F) * this.spSz[i] * a, Math.min(1.0F, a * fxs));
         }
      }

      Fx.endTris(ctx, wr);
      Fx.end();
   }

   private static void drawCentered(GuiGraphicsExtractor ctx, Font fr, String text, float cx, float y, float scale, int argb) {
      Matrix3x2fStack ms = ctx.pose();
      ms.pushMatrix();
      ms.translate(cx - (float)fr.width(text) * scale / 2.0F, y);
      ms.scale(scale, scale);
      ctx.text(fr, text, 0, 0, argb);
      ms.popMatrix();
   }
}
