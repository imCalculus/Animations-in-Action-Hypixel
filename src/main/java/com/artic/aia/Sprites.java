package com.artic.aia;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

final class Sprites {
   private static final Map<String, Sprites.Img> CACHE = new HashMap<>();

   private Sprites() {
   }

   static Sprites.Img read(String itemName, int damage, boolean overlay) {
      String key = itemName + "|" + damage + (overlay ? "|overlay" : "");
      Sprites.Img hit = CACHE.get(key);
      if (hit != null) {
         return hit;
      } else {
         Sprites.Img img = null;

         try {
            String base = LegacyItems.texture(itemName, damage);
            img = load(overlay ? base + "_overlay" : base);
         } catch (Throwable t) {
            AIAConfig.log("sprite not readable for " + key + ": " + t);
         }

         if (img != null) {
            CACHE.put(key, img);
         }

         return img;
      }
   }

   private static Sprites.Img load(String texture) throws Exception {
      Identifier id = Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + texture + ".png");
      Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(id);
      if (!res.isPresent()) {
         return null;
      } else {
         InputStream in = res.get().open();

         try {
            NativeImage ni = NativeImage.read(in);

            try {
               int w = ni.getWidth();
               int h = ni.getHeight();
               int[] out = new int[w * h];

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     out[y * w + x] = ni.getPixel(x, y);
                  }
               }

               return new Sprites.Img(w, h, out);
            } finally {
               ni.close();
            }
         } finally {
            in.close();
         }
      }
   }

   static final class Img {
      final int w;
      final int h;
      final int[] argb;

      Img(int w, int h, int[] argb) {
         this.w = w;
         this.h = h;
         this.argb = argb;
      }

      int get(int x, int y) {
         return this.argb[y * this.w + x];
      }
   }
}
