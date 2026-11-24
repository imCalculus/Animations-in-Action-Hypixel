package com.artic.aia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.joml.Matrix3x2fStack;

public class ConfigScreen extends Screen {
   private static final int HEADER_H = 30;
   private static final int SIDE_W = 112;
   private static final int ROW_H = 26;
   private static final int RB_W = 80;
   private static final int TB_W = 34;
   private static final int FLAKES = 90;
   private static final int SL_W = 100;
   private static final int C_TEXT = -1378305;
   private static final int C_DIM = -9468250;
   private static final int C_ICE = -7351297;
   private static final int C_ACCENT = -11549705;
   private static final Identifier CLICK = Identifier.fromNamespaceAndPath("minecraft", "ui.button.click");
   private final Category[] cats = Category.values();
   private final List<ConfigScreen.Setting> settings = new ArrayList<>();
   private final float[] fx = new float[FLAKES];
   private final float[] fy = new float[FLAKES];
   private final float[] fs = new float[FLAKES];
   private final float[] fp = new float[FLAKES];
   private final Random rnd = new Random();
   private int tab;
   private int px;
   private int py;
   private int pw;
   private int ph;
   private int dragging = -1;
   private float scroll;
   private float scrollTarget;
   private long lastNanos = System.nanoTime();
   private GuiGraphicsExtractor g;

   public ConfigScreen() {
      super(Component.literal("Animation in Action"));
      this.settings.add(new ConfigScreen.Heading("Animation"));
      this.settings.add(new ConfigScreen.Toggle("Animations enabled", () -> AIAConfig.master, v -> AIAConfig.master = v));
      this.settings.add(new ConfigScreen.Toggle("Screen flash", () -> AIAConfig.flash, v -> AIAConfig.flash = v));
      this.settings.add(new ConfigScreen.Toggle("Dim background", () -> AIAConfig.dim, v -> AIAConfig.dim = v));
      this.settings.add(new ConfigScreen.Slider("Animation length", 3.0F, 10.0F, 0.5F, "s", () -> AIAConfig.durationSec, v -> AIAConfig.durationSec = v));
      this.settings.add(new ConfigScreen.Slider("Item size", 3.0F, 12.0F, 0.5F, "", () -> AIAConfig.size, v -> AIAConfig.size = v));
      this.settings.add(new ConfigScreen.Slider("Spin speed", 30.0F, 360.0F, 10.0F, " deg/s", () -> AIAConfig.spinSpeed, v -> AIAConfig.spinSpeed = v));
      this.settings.add(new ConfigScreen.Slider("Ray intensity", 0.2F, 2.0F, 0.1F, "x", () -> AIAConfig.rayIntensity, v -> AIAConfig.rayIntensity = v));
      this.settings
         .add(new ConfigScreen.Slider("Queue limit", 1.0F, 10.0F, 1.0F, "", () -> (float)AIAConfig.queueLimit, v -> AIAConfig.queueLimit = Math.round(v)));
      this.settings.add(new ConfigScreen.Action("Preview a random drop", "Play", new Runnable() {
         @Override
         public void run() {
            List<DropEntry> pool = new ArrayList<>();

            for (DropEntry d : DropCatalog.ALL) {
               if (!d.generic) {
                  pool.add(d);
               }
            }

            DropEntry dx = pool.get(ConfigScreen.this.rnd.nextInt(pool.size()));
            AnimationManager.INSTANCE.submit(dx, dx.name, dx.effectiveDefault(), true);
         }
      }));
      this.settings.add(new ConfigScreen.Heading("Sound"));
      this.settings.add(new ConfigScreen.Toggle("Play sound", () -> AIAConfig.sound, v -> AIAConfig.sound = v));
      this.settings.add(new ConfigScreen.Slider("Sound volume", 0.0F, 1.0F, 0.05F, "", () -> AIAConfig.volume, v -> AIAConfig.volume = v));
      this.settings.add(new ConfigScreen.Action("Test the sound", "Play", new Runnable() {
         @Override
         public void run() {
            SoundPlayer.play(Rarity.LEGENDARY);
         }
      }));
      this.settings.add(new ConfigScreen.Heading("Detection & display"));
      this.settings.add(new ConfigScreen.Toggle("Show over open menus", () -> AIAConfig.aboveGui, v -> AIAConfig.aboveGui = v));
      this.settings.add(new ConfigScreen.Toggle("Strict anti-spoof check", () -> AIAConfig.strict, v -> AIAConfig.strict = v));
      this.settings.add(new ConfigScreen.Toggle("Debug: log chat lines", () -> AIAConfig.debug, v -> AIAConfig.debug = v));
   }

   private int cX() {
      return this.px + SIDE_W + 8;
   }

   private int cW() {
      return this.pw - SIDE_W - 14;
   }

   private int listTop() {
      return this.py + HEADER_H + 30;
   }

   private int listBottom() {
      return this.py + this.ph - 8;
   }

   private int tabH() {
      return Math.max(14, Math.min(22, (this.ph - HEADER_H - 12) / this.cats.length));
   }

   private int rowCount() {
      return this.tab == 0 ? this.settings.size() : DropCatalog.rows(this.cats[this.tab]).size();
   }

   private float maxScroll() {
      return (float)Math.max(0, this.rowCount() * ROW_H - (this.listBottom() - this.listTop()));
   }

   private int rowY(int i) {
      return this.listTop() + i * ROW_H - (int)this.scroll;
   }

   private int rowRight() {
      return this.cX() + this.cW() - 6;
   }

   private int sliderX() {
      return this.rowRight() - 8 - 40 - SL_W;
   }

   private static boolean in(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void rect(int x1, int y1, int x2, int y2, int color) {
      this.g.fill(x1, y1, x2, y2, color);
   }

   private void grad(int x1, int y1, int x2, int y2, int c1, int c2) {
      this.g.fillGradient(x1, y1, x2, y2, c1, c2);
   }

   private void str(String s, float x, float y, int color) {
      this.g.text(this.font, s, Math.round(x), Math.round(y), color);
   }

   private int sw(String s) {
      return this.font.width(s);
   }

   @Override
   protected void init() {
      this.pw = Math.min(this.width - 12, 480);
      this.ph = Math.min(this.height - 12, 312);
      this.px = (this.width - this.pw) / 2;
      this.py = (this.height - this.ph) / 2;

      for (int i = 0; i < FLAKES; i++) {
         this.fx[i] = this.rnd.nextFloat() * (float)this.width;
         this.fy[i] = this.rnd.nextFloat() * (float)this.height;
         this.fs[i] = 10.0F + this.rnd.nextFloat() * 32.0F;
         this.fp[i] = this.rnd.nextFloat() * 6.28F;
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void removed() {
      AIAConfig.save();
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
   }

   private void click() {
      this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(CLICK), 1.0F, 1.0F));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.g = context;
      long nowN = System.nanoTime();
      float dt = Math.min(0.1F, (float)(nowN - this.lastNanos) / 1.0E9F);
      this.lastNanos = nowN;
      this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll(), this.scrollTarget));
      this.scroll = this.scroll + (this.scrollTarget - this.scroll) * Math.min(1.0F, dt * 14.0F);
      if (Math.abs(this.scrollTarget - this.scroll) < 0.05F) {
         this.scroll = this.scrollTarget;
      }

      this.grad(0, 0, this.width, this.height, -536341464, -266319240);
      this.stepSnow(dt);
      this.drawSnow(false);
      this.rect(this.px - 1, this.py - 1, this.px + this.pw + 1, this.py + this.ph + 1, -6628097);
      this.grad(this.px, this.py, this.px + this.pw, this.py + this.ph, -267311542, -267774160);
      this.rect(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 2, 1728053247);
      this.drawHeader(mouseX, mouseY);
      this.drawSidebar(mouseX, mouseY);
      this.drawContent(mouseX, mouseY);
      this.drawSnow(true);
   }

   private void drawHeader(int mx, int my) {
      this.grad(this.px + 1, this.py + 2, this.px + this.pw - 1, this.py + HEADER_H, -14922362, -15783342);
      int i = 0;

      for (int x = this.px + 6; x < this.px + this.pw - 10; x += 13) {
         int len = 4 + i * 7 % 5;

         for (int r = 0; r < len; r++) {
            this.rect(x + r / 2, this.py + HEADER_H + r, x + 7 - r / 2, this.py + HEADER_H + r + 1, -1326778369);
         }

         i++;
      }

      this.drawSnowflake(this.px + 14, this.py + 15, 5, C_TEXT);
      String title = "Animation in Action";
      Matrix3x2fStack ms = this.g.pose();
      ms.pushMatrix();
      ms.translate((float)(this.px + 26), (float)(this.py + 8));
      ms.scale(1.5F, 1.5F);
      this.str(title, 0.0F, 0.0F, C_TEXT);
      ms.popMatrix();
      boolean hov = in(mx, my, this.px + this.pw - 24, this.py + 7, 16, 16);
      this.rect(this.px + this.pw - 24, this.py + 7, this.px + this.pw - 8, this.py + 23, hov ? -2075307 : 1442840575);
      this.str("x", (float)(this.px + this.pw - 18), (float)(this.py + 11), -1);
      int sx = this.px + this.pw - 24 - 8 - 26;
      this.drawSwitch(sx, this.py + 11, AIAConfig.master);
      String lab = "Enabled";
      this.str(lab, (float)(sx - this.sw(lab) - 5), (float)(this.py + 13), C_TEXT);
   }

   private void drawSidebar(int mx, int my) {
      this.rect(this.px + 3, this.py + HEADER_H + 3, this.px + SIDE_W + 1, this.py + this.ph - 3, 1711607327);
      int th = this.tabH();

      for (int i = 0; i < this.cats.length; i++) {
         int ty = this.py + HEADER_H + 6 + i * th;
         boolean sel = i == this.tab;
         boolean hov = in(mx, my, this.px + 4, ty, 108, th - 1);
         if (sel) {
            this.grad(this.px + 4, ty, this.px + SIDE_W, ty + th - 1, -13664320, -14854513);
            this.rect(this.px + 4, ty, this.px + 6, ty + th - 1, -1);
         } else if (hov) {
            this.rect(this.px + 4, ty, this.px + SIDE_W, ty + th - 1, 872415231);
         }

         int tx = this.px + 10;
         if (th >= 18) {
            this.g.item(this.cats[i].icon(), tx, ty + (th - 16) / 2);
            tx += 20;
         }

         String t = this.font.plainSubstrByWidth(this.cats[i].title, SIDE_W - (tx - this.px) - 3);
         this.str(t, (float)tx, (float)(ty + (th - 8) / 2), sel ? -1 : -4598032);
      }
   }

   private void drawContent(int mx, int my) {
      int cx = this.cX();
      int cw = this.cW();
      Matrix3x2fStack ms = this.g.pose();
      ms.pushMatrix();
      ms.translate((float)cx, (float)(this.py + HEADER_H + 8));
      ms.scale(1.25F, 1.25F);
      this.str(this.cats[this.tab].title, 0.0F, 0.0F, C_TEXT);
      ms.popMatrix();
      if (this.tab == 0) {
         this.str("How animations look and trigger", (float)cx, (float)(this.py + HEADER_H + 21), C_ICE);
      } else {
         List<DropCatalog.Row> list = DropCatalog.rows(this.cats[this.tab]);
         int on = 0;
         int total = 0;

         for (DropCatalog.Row r : list) {
            if (!r.isHeader()) {
               total++;
               if (r.enabled()) {
                  on++;
               }
            }
         }

         this.str(on + "/" + total + " animations enabled  -  click a row to toggle, rarity to cycle", (float)cx, (float)(this.py + HEADER_H + 21), C_ICE);
         int by = this.py + HEADER_H + 6;
         this.drawBtn(this.rowRight() - 124 - 4, by, 62, 13, "Enable all", C_TEXT, in(mx, my, this.rowRight() - 124 - 4, by, 62, 13));
         this.drawBtn(this.rowRight() - 62, by, 62, 13, "Disable all", C_TEXT, in(mx, my, this.rowRight() - 62, by, 62, 13));
      }

      int top = this.listTop();
      int bottom = this.listBottom();
      this.g.enableScissor(cx - 2, top, cx + cw, bottom);
      if (this.tab == 0) {
         this.drawSettingRows(mx, my, top, bottom);
      } else {
         this.drawDropRows(mx, my, top, bottom);
      }

      this.g.disableScissor();
      float mxs = this.maxScroll();
      if (mxs > 0.0F) {
         int h = bottom - top;
         int thumb = Math.max(16, (int)((float)h * (float)h / (float)(this.rowCount() * ROW_H)));
         int ty = top + (int)((float)(h - thumb) * (this.scroll / mxs));
         this.rect(cx + cw - 4, top, cx + cw - 1, bottom, 872415231);
         this.rect(cx + cw - 4, ty, cx + cw - 1, ty + thumb, -6628097);
      }
   }

   private void drawDropRows(int mx, int my, int top, int bottom) {
      List<DropCatalog.Row> list = DropCatalog.rows(this.cats[this.tab]);
      int cx = this.cX();
      int cw = this.cW();

      for (int i = 0; i < list.size(); i++) {
         int y = this.rowY(i);
         if (y + ROW_H >= top && y <= bottom) {
            DropCatalog.Row row = list.get(i);
            if (row.isHeader()) {
               this.str(row.label.toUpperCase(), (float)(cx + 4), (float)(y + 9), C_ICE);
               this.rect(cx, y + ROW_H - 6, this.rowRight(), y + ROW_H - 5, 1715108789);
            } else {
               DropEntry d = row.lead();
               boolean on = row.enabled();
               boolean hov = in(mx, my, cx, y, cw - 6, 24) && my >= top && my < bottom;
               this.rect(cx, y, this.rowRight(), y + ROW_H - 2, hov ? 1090519039 : ((i & 1) == 0 ? 654311423 : 352321535));
               this.drawSwitch(cx + 8, y + 7, on);
               this.drawRowIcon(d, cx + 40, y + 4);
               int nameCol = on ? 0xFF000000 | d.effectiveDefault().primary : C_DIM;
               int tbX = this.rowRight() - TB_W - 4;
               int rbX = tbX - RB_W - 4;
               int textW = rbX - (cx + 62) - 6;
               String nm = this.font.plainSubstrByWidth(row.label, textW);
               if (row.summary != null) {
                  this.str(nm, (float)(cx + 62), (float)(y + 3), nameCol);
                  this.str(this.font.plainSubstrByWidth(row.summary, textW), (float)(cx + 62), (float)(y + 13), -7362876);
               } else {
                  this.str(nm, (float)(cx + 62), (float)(y + 9), nameCol);
               }

               String rl = d.override == null ? "AUTO" : d.override.label;
               int rc = d.override == null ? C_ICE : 0xFF000000 | d.override.primary;
               this.drawBtn(rbX, y + 5, RB_W, 14, rl, rc, in(mx, my, rbX, y + 5, RB_W, 14));
               this.drawBtn(tbX, y + 5, TB_W, 14, "Test", C_TEXT, in(mx, my, tbX, y + 5, TB_W, 14));
            }
         }
      }
   }

   private void drawSettingRows(int mx, int my, int top, int bottom) {
      int cx = this.cX();
      int cw = this.cW();

      for (int i = 0; i < this.settings.size(); i++) {
         int y = this.rowY(i);
         if (y + ROW_H >= top && y <= bottom) {
            ConfigScreen.Setting s = this.settings.get(i);
            if (s instanceof ConfigScreen.Heading) {
               this.str(s.label.toUpperCase(), (float)(cx + 4), (float)(y + 9), C_ICE);
               this.rect(cx, y + ROW_H - 6, this.rowRight(), y + ROW_H - 5, 1715108789);
            } else {
               boolean hov = in(mx, my, cx, y, cw - 6, 24) && my >= top && my < bottom;
               this.rect(cx, y, this.rowRight(), y + ROW_H - 2, hov ? 1090519039 : ((i & 1) == 0 ? 654311423 : 352321535));
               this.str(s.label, (float)(cx + 8), (float)(y + 9), C_TEXT);
               if (s instanceof ConfigScreen.Toggle) {
                  this.drawSwitch(this.rowRight() - 8 - 26, y + 7, ((ConfigScreen.Toggle)s).get.getAsBoolean());
               } else if (s instanceof ConfigScreen.Slider sl) {
                  float v = sl.get.get();
                  float p = (v - sl.min) / (sl.max - sl.min);
                  int tx = this.sliderX();
                  this.rect(tx, y + 11, tx + SL_W, y + 13, -13417377);
                  this.rect(tx, y + 11, tx + (int)((float)SL_W * p), y + 13, C_ACCENT);
                  int kx = tx + (int)((float)SL_W * p);
                  this.rect(kx - 2, y + 7, kx + 2, y + 17, -1);
                  this.str(sl.fmt(v), (float)(tx + SL_W + 8), (float)(y + 9), C_ICE);
               } else if (s instanceof ConfigScreen.Action a) {
                  int bx = this.rowRight() - 8 - 44;
                  this.drawBtn(bx, y + 5, 44, 14, a.button, C_TEXT, in(mx, my, bx, y + 5, 44, 14));
               }
            }
         }
      }
   }

   private void drawRowIcon(DropEntry d, int x, int y) {
      ItemLook look = d.look;
      if (look.mode() == ItemLook.Mode.HEAD) {
         HeadTextures.Tex t = HeadTextures.get(look.headKey());
         if (t == null) {
            t = HeadTextures.placeholder();
         }

         HeadRenderer.draw(this.g, t, (float)x + 8.0F, (float)y + 8.0F, 8.0F, 0.0F);
      } else {
         this.g.item(look.iconStack(), x, y);
      }
   }

   private void drawSwitch(int x, int y, boolean on) {
      this.rect(x - 1, y - 1, x + 27, y + 13, on ? -6628097 : -11903878);
      this.rect(x, y, x + 26, y + 12, on ? -13917216 : -14273459);
      int kx = on ? x + 15 : x + 1;
      this.rect(kx, y + 1, kx + 10, y + 11, -1);
   }

   private void drawBtn(int x, int y, int w, int h, String label, int color, boolean hover) {
      this.rect(x - 1, y - 1, x + w + 1, y + h + 1, hover ? -4200193 : -9455376);
      this.grad(x, y, x + w, y + h, hover ? -13865042 : -14988160, hover ? -14723958 : -15583392);
      int tw = this.sw(label);
      this.str(label, (float)x + (float)(w - tw) / 2.0F, (float)y + (float)(h - 8) / 2.0F, color);
   }

   private void drawSnowflake(int cx, int cy, int r, int color) {
      this.rect(cx - r, cy, cx + r + 1, cy + 1, color);
      this.rect(cx, cy - r, cx + 1, cy + r + 1, color);

      for (int k = -(r - 1); k <= r - 1; k++) {
         this.rect(cx + k, cy + k, cx + k + 1, cy + k + 1, color);
         this.rect(cx + k, cy - k, cx + k + 1, cy - k + 1, color);
      }
   }

   private void stepSnow(float dt) {
      double t = (double)System.nanoTime() / 1.0E9;

      for (int i = 0; i < FLAKES; i++) {
         this.fy[i] = this.fy[i] + this.fs[i] * dt;
         this.fx[i] = this.fx[i] + (float)Math.sin((double)this.fp[i] + t * 0.8) * 10.0F * dt;
         if (this.fy[i] > (float)(this.height + 2)) {
            this.fy[i] = -3.0F;
            this.fx[i] = this.rnd.nextFloat() * (float)this.width;
         }
      }
   }

   private void drawSnow(boolean front) {
      for (int i = 0; i < FLAKES; i++) {
         if (!front || i % 4 == 0) {
            int size = this.fs[i] > 28.0F ? 2 : 1;
            int a = front ? 110 : 70 + (int)this.fs[i] * 3;
            int x = (int)this.fx[i];
            int y = (int)this.fy[i];
            this.rect(x, y, x + size, y + size, Math.min(a, 255) << 24 | 0xFFFFFF);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (verticalAmount != 0.0) {
         this.scrollTarget = (float)((double)this.scrollTarget - Math.signum(verticalAmount) * (double)ROW_H * 1.5);
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (AnimationInAction.openKey != null && AnimationInAction.openKey.matches(event)) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = (int)event.x();
      int my = (int)event.y();
      int button = event.button();
      if (in(mx, my, this.px + this.pw - 24, this.py + 7, 16, 16)) {
         this.click();
         this.onClose();
         return true;
      } else if (in(mx, my, this.px + this.pw - 24 - 8 - 26, this.py + 11, 26, 12)) {
         this.click();
         AIAConfig.master = !AIAConfig.master;
         return true;
      } else {
         int th = this.tabH();

         for (int i = 0; i < this.cats.length; i++) {
            if (in(mx, my, this.px + 4, this.py + HEADER_H + 6 + i * th, 108, th - 1)) {
               if (this.tab != i) {
                  this.tab = i;
                  this.scroll = this.scrollTarget = 0.0F;
                  this.dragging = -1;
               }

               this.click();
               return true;
            }
         }

         if (this.tab != 0) {
            List<DropCatalog.Row> list = DropCatalog.rows(this.cats[this.tab]);
            int by = this.py + HEADER_H + 6;
            if (in(mx, my, this.rowRight() - 124 - 4, by, 62, 13)) {
               for (DropCatalog.Row r : list) {
                  r.setEnabled(true);
               }

               this.click();
               return true;
            }

            if (in(mx, my, this.rowRight() - 62, by, 62, 13)) {
               for (DropCatalog.Row r : list) {
                  r.setEnabled(false);
               }

               this.click();
               return true;
            }
         }

         if (my >= this.listTop() && my < this.listBottom() && mx >= this.cX() && mx < this.rowRight()) {
            int idx = (my - this.listTop() + (int)this.scroll) / ROW_H;
            if (idx >= 0 && idx < this.rowCount()) {
               int y = this.rowY(idx);
               if (this.tab == 0) {
                  ConfigScreen.Setting s = this.settings.get(idx);
                  if (s instanceof ConfigScreen.Toggle t) {
                     t.set.accept(!t.get.getAsBoolean());
                     this.click();
                  } else if (s instanceof ConfigScreen.Slider) {
                     if (mx >= this.sliderX() - 4 && mx <= this.sliderX() + SL_W + 4) {
                        this.dragging = idx;
                        this.applySlider((ConfigScreen.Slider)s, mx);
                     }
                  } else if (s instanceof ConfigScreen.Action) {
                     int bx = this.rowRight() - 8 - 44;
                     if (in(mx, my, bx, y + 5, 44, 14)) {
                        this.click();
                        ((ConfigScreen.Action)s).run.run();
                     }
                  }
               } else {
                  DropCatalog.Row row = DropCatalog.rows(this.cats[this.tab]).get(idx);
                  if (row.isHeader()) {
                     return true;
                  }

                  int tbX = this.rowRight() - TB_W - 4;
                  int rbX = tbX - RB_W - 4;
                  if (in(mx, my, tbX, y + 5, TB_W, 14)) {
                     this.click();
                     DropEntry t = row.previewTarget();
                     AnimationManager.INSTANCE.submit(t, t.name, t.effectiveDefault(), true);
                  } else if (in(mx, my, rbX, y + 5, RB_W, 14)) {
                     this.click();
                     row.cycleRarity(button != 1);
                  } else {
                     this.click();
                     row.setEnabled(!row.enabled());
                  }
               }

               return true;
            } else {
               return true;
            }
         } else {
            return super.mouseClicked(event, doubleClick);
         }
      }
   }

   private void applySlider(ConfigScreen.Slider s, int mx) {
      float p = Math.max(0.0F, Math.min(1.0F, (float)(mx - this.sliderX()) / (float)SL_W));
      float v = s.min + p * (s.max - s.min);
      v = s.min + (float)Math.round((v - s.min) / s.step) * s.step;
      s.set.accept(Math.max(s.min, Math.min(s.max, v)));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      if (this.tab == 0 && this.dragging >= 0 && this.dragging < this.settings.size() && this.settings.get(this.dragging) instanceof ConfigScreen.Slider) {
         this.applySlider((ConfigScreen.Slider)this.settings.get(this.dragging), (int)event.x());
         return true;
      } else {
         return super.mouseDragged(event, deltaX, deltaY);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.dragging >= 0) {
         this.dragging = -1;
         AIAConfig.save();
      }

      return super.mouseReleased(event);
   }

   private static class Action extends ConfigScreen.Setting {
      final String button;
      final Runnable run;

      Action(String l, String b, Runnable r) {
         super(l);
         this.button = b;
         this.run = r;
      }
   }

   private static class Heading extends ConfigScreen.Setting {
      Heading(String l) {
         super(l);
      }
   }

   private abstract static class Setting {
      final String label;

      Setting(String label) {
         this.label = label;
      }
   }

   private static class Slider extends ConfigScreen.Setting {
      final float min;
      final float max;
      final float step;
      final String unit;
      final Supplier<Float> get;
      final Consumer<Float> set;

      Slider(String l, float min, float max, float step, String unit, Supplier<Float> g, Consumer<Float> s) {
         super(l);
         this.min = min;
         this.max = max;
         this.step = step;
         this.unit = unit;
         this.get = g;
         this.set = s;
      }

      String fmt(float v) {
         return (this.step >= 1.0F ? String.valueOf(Math.round(v)) : String.format("%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "")) + this.unit;
      }
   }

   private static class Toggle extends ConfigScreen.Setting {
      final BooleanSupplier get;
      final Consumer<Boolean> set;

      Toggle(String l, BooleanSupplier g, Consumer<Boolean> s) {
         super(l);
         this.get = g;
         this.set = s;
      }
   }
}
