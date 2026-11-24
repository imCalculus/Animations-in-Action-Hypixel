package com.artic.aia;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemLook {
   public final String skull;
   public final String custom;
   public final String itemName;
   public final int damage;
   public final boolean glint;
   public final int tint;
   private final boolean voxel;
   private final String headKey;
   private ItemStack cached;

   private ItemLook(String skull, String custom, String itemName, int damage, boolean glint, int tint, boolean voxel) {
      this.skull = skull;
      this.custom = custom;
      this.itemName = itemName;
      this.damage = damage;
      this.glint = glint;
      this.tint = tint;
      this.voxel = voxel;
      String key = null;
      if (skull != null && !skull.isEmpty()) {
         String url = urlFromValue(skull);
         if (url != null) {
            key = "url:" + url;
         }
      } else if (custom != null && custom.startsWith("file:")) {
         key = custom;
      }

      this.headKey = key;
   }

   public static ItemLook parse(String kind, String data) {
      data = data == null ? "" : data.trim();
      if ("skull".equals(kind)) {
         return new ItemLook(data, null, null, 0, false, -1, false);
      } else if ("custom".equals(kind)) {
         String item = null;
         int dmg = 0;
         if (data.startsWith("sprite:")) {
            String[] p = data.substring(7).split("\\|");
            item = p[0];

            try {
               if (p.length > 1) {
                  dmg = Integer.parseInt(p[1].trim());
               }
            } catch (NumberFormatException ignored) {
            }
         }

         return new ItemLook(null, data, item, dmg, false, -1, false);
      } else {
         String[] p = data.split("\\|", -1);
         String name = p.length > 0 && !p[0].isEmpty() ? p[0] : "minecraft:stick";
         int dmg = 0;

         try {
            if (p.length > 1) {
               dmg = Integer.parseInt(p[1].trim());
            }
         } catch (NumberFormatException ignored) {
         }

         boolean glint = p.length > 2 && "1".equals(p[2].trim());
         int tint = -1;

         try {
            if (p.length > 3 && !p[3].trim().isEmpty()) {
               tint = Integer.parseInt(p[3].trim(), 16);
            }
         } catch (NumberFormatException ignored) {
         }

         return new ItemLook(null, null, name, dmg, glint, tint, true);
      }
   }

   public static String urlFromValue(String base64) {
      try {
         String json = new String(Base64.getDecoder().decode(base64.trim()), StandardCharsets.UTF_8);
         JsonObject o = JsonParser.parseString(json).getAsJsonObject();
         return o.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
      } catch (Throwable t) {
         AIAConfig.log("could not read skull texture url: " + t);
         return null;
      }
   }

   public boolean hasTexture() {
      return this.skull != null && !this.skull.isEmpty() || this.custom != null;
   }

   public String headKey() {
      return this.headKey;
   }

   public ItemLook.Mode mode() {
      if (this.voxel) {
         return ItemLook.Mode.VOXEL;
      } else if (this.headKey != null) {
         return ItemLook.Mode.HEAD;
      } else {
         return this.itemName != null ? ItemLook.Mode.VOXEL : ItemLook.Mode.HEAD;
      }
   }

   public ItemStack iconStack() {
      if (this.mode() == ItemLook.Mode.HEAD) {
         return null;
      } else {
         if (this.cached == null) {
            this.cached = LegacyItems.stack(this.itemName, this.damage, this.glint, this.tint);
         }

         return this.cached;
      }
   }

   public ItemStack fallbackStack() {
      ItemStack s = this.iconStack();
      return s != null ? s : LegacyItems.stackById("player_head");
   }

   public static enum Mode {
      HEAD,
      VOXEL;
   }
}
