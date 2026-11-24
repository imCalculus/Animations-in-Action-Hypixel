package com.artic.aia;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

final class HeadTextures {
   private static final Map<String, HeadTextures.Entry> MAP = new ConcurrentHashMap<>();
   private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
      Thread t = new Thread(r, "AIA-head-loader");
      t.setDaemon(true);
      return t;
   });
   private static HeadTextures.Tex placeholder;

   private HeadTextures() {
   }

   static HeadTextures.Tex get(String key) {
      if (key == null) {
         return null;
      } else {
         HeadTextures.Entry e = MAP.get(key);
         if (e == null) {
            e = new HeadTextures.Entry();
            HeadTextures.Entry prev = MAP.putIfAbsent(key, e);
            if (prev == null) {
               start(key, e);
               return null;
            }

            e = prev;
         }

         if (e.state == 1) {
            return e.tex;
         } else {
            if (e.state == 2 && System.currentTimeMillis() - e.failedAt > 30000L) {
               e.state = 0;
               start(key, e);
            }

            return null;
         }
      }
   }

   static void warm(String key) {
      get(key);
   }

   static HeadTextures.Tex placeholder() {
      if (placeholder == null) {
         DynamicTexture t = new DynamicTexture(() -> "aia_head_placeholder", 64, 64, true);
         NativeImage img = t.getPixels();

         for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
               img.setPixel(x, y, -9408400);
            }
         }

         t.upload();
         Identifier id = Identifier.fromNamespaceAndPath("animationinaction", "head/placeholder");
         Minecraft.getInstance().getTextureManager().register(id, t);
         placeholder = new HeadTextures.Tex(id, 64, 64);
      }

      return placeholder;
   }

   private static void start(String key, HeadTextures.Entry e) {
      POOL.execute(() -> {
         try {
            byte[] png = fetch(key);
            Minecraft.getInstance().execute(() -> register(key, e, png));
         } catch (Throwable t) {
            AIAConfig.log("head texture failed (" + key + "): " + t);
            e.failedAt = System.currentTimeMillis();
            e.state = 2;
         }
      });
   }

   private static void register(String key, HeadTextures.Entry e, byte[] png) {
      try {
         NativeImage img = NativeImage.read(new ByteArrayInputStream(png));
         int w = img.getWidth();
         int h = img.getHeight();
         if (w < 64 || h < 32) {
            img.close();
            throw new IllegalStateException("unexpected skin size " + w + "x" + h);
         }

         String hashed = hash(key);
         DynamicTexture tex = new DynamicTexture(() -> "aia_head_" + hashed, img);
         Identifier id = Identifier.fromNamespaceAndPath("animationinaction", "head/" + hashed);
         Minecraft.getInstance().getTextureManager().register(id, tex);
         e.tex = new HeadTextures.Tex(id, w, h);
         e.state = 1;
      } catch (Throwable t) {
         AIAConfig.log("head texture could not be decoded (" + key + "): " + t);
         e.failedAt = System.currentTimeMillis();
         e.state = 2;
      }
   }

   private static byte[] fetch(String key) throws Exception {
      if (key.startsWith("file:")) {
         InputStream in = HeadTextures.class.getResourceAsStream("/assets/animationinaction/skulls/" + key.substring(5) + ".png");
         if (in == null) {
            throw new IllegalStateException("bundled skull missing");
         } else {
            try {
               return readAll(in);
            } finally {
               in.close();
            }
         }
      } else if (!key.startsWith("url:")) {
         throw new IllegalStateException("unknown head key");
      } else {
         String url = key.substring(4);
         if (url.startsWith("http://")) {
            url = "https://" + url.substring(7);
         }

         File cache = new File(new File(AIAConfig.dir, "skins"), hash(key) + ".png");
         if (cache.isFile() && cache.length() > 0L) {
            try {
               return Files.readAllBytes(cache.toPath());
            } catch (Exception ignored) {
            }
         }

         HttpURLConnection c = (HttpURLConnection)URI.create(url).toURL().openConnection();
         c.setConnectTimeout(8000);
         c.setReadTimeout(10000);
         c.setInstanceFollowRedirects(true);
         c.setRequestProperty("User-Agent", "AnimationInAction/1.0");

         try {
            int code = c.getResponseCode();
            if (code != 200) {
               throw new IllegalStateException("HTTP " + code);
            }

            InputStream in = c.getInputStream();

            byte[] data;
            try {
               data = readAll(in);
            } finally {
               in.close();
            }

            try {
               cache.getParentFile().mkdirs();
               FileOutputStream fo = new FileOutputStream(cache);

               try {
                  fo.write(data);
               } finally {
                  fo.close();
               }
            } catch (Exception ignored) {
            }

            return data;
         } finally {
            c.disconnect();
         }
      }
   }

   private static byte[] readAll(InputStream in) throws Exception {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];

      int n;
      while ((n = in.read(buf)) > 0) {
         out.write(buf, 0, n);
         if (out.size() > 4194304) {
            throw new IllegalStateException("skin too large");
         }
      }

      return out.toByteArray();
   }

   private static String hash(String key) throws Exception {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] d = md.digest(key.getBytes("UTF-8"));
      StringBuilder sb = new StringBuilder();

      for (byte b : d) {
         sb.append(String.format("%02x", b & 255));
      }

      return sb.toString();
   }

   private static final class Entry {
      volatile int state;
      volatile HeadTextures.Tex tex;
      volatile long failedAt;
   }

   static final class Tex {
      final Identifier id;
      final int w;
      final int h;

      Tex(Identifier id, int w, int h) {
         this.id = id;
         this.w = w;
         this.h = h;
      }
   }
}
