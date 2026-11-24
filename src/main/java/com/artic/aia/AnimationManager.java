package com.artic.aia;

import com.mojang.authlib.properties.Property;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class AnimationManager {
   public static final AnimationManager INSTANCE = new AnimationManager();
   public static final long BURST_WINDOW_MS = 150L;
   private static final int MAX_WAIT_TICKS = 10;
   private final LinkedList<AnimationManager.Pending> pending = new LinkedList<>();
   private final LinkedList<DropAnimation> queue = new LinkedList<>();
   private DropAnimation current;
   private long burstStartMs = -1L;
   private int warmIdx;
   private int warmTick;

   public void submit(DropEntry entry, String name, Rarity rarity, boolean immediate) {
      this.submit(entry, name, rarity, immediate, System.currentTimeMillis());
   }

   public void submit(DropEntry entry, String name, Rarity rarity, boolean immediate, long nowMs) {
      Rarity resolvedRarity = rarity != null ? rarity : (entry != null ? entry.effectiveDefault() : Rarity.COMMON);
      AnimationManager.Pending p = new AnimationManager.Pending(entry, name, resolvedRarity);
      DropAnimation incoming = this.build(p);

      if (this.current != null && this.current.finished(nowMs)) {
         this.current = null;
      }

      if (this.current == null) {
         incoming.startMs = nowMs;
         this.current = incoming;
         this.burstStartMs = nowMs;
         SoundPlayer.play(this.current.rarity);
      } else if (this.burstStartMs >= 0L && nowMs - this.burstStartMs <= BURST_WINDOW_MS) {
         if (comparePriority(incoming.entry, incoming.rarity, this.current.entry, this.current.rarity) > 0) {
            incoming.startMs = nowMs;
            this.current = incoming;
            SoundPlayer.play(this.current.rarity);
         }
      } else {
         this.enqueue(incoming);
      }
   }

   public static int comparePriority(DropEntry aEntry, Rarity aRarity, DropEntry bEntry, Rarity bRarity) {
      Rarity effA = aRarity != null ? aRarity : (aEntry != null ? aEntry.effectiveDefault() : Rarity.COMMON);
      Rarity effB = bRarity != null ? bRarity : (bEntry != null ? bEntry.effectiveDefault() : Rarity.COMMON);
      int cmp = Integer.compare(effA.ordinal(), effB.ordinal());
      if (cmp != 0) {
         return cmp;
      }

      int tierA = tierPriority(aEntry);
      int tierB = tierPriority(bEntry);
      cmp = Integer.compare(tierA, tierB);
      if (cmp != 0) {
         return cmp;
      }

      Rarity defA = aEntry != null && aEntry.defaultRarity != null ? aEntry.defaultRarity : effA;
      Rarity defB = bEntry != null && bEntry.defaultRarity != null ? bEntry.defaultRarity : effB;
      cmp = Integer.compare(defA.ordinal(), defB.ordinal());
      if (cmp != 0) {
         return cmp;
      }

      int idxA = aEntry != null ? DropCatalog.ALL.indexOf(aEntry) : Integer.MAX_VALUE;
      int idxB = bEntry != null ? DropCatalog.ALL.indexOf(bEntry) : Integer.MAX_VALUE;
      if (idxA < 0) {
         idxA = Integer.MAX_VALUE;
      }
      if (idxB < 0) {
         idxB = Integer.MAX_VALUE;
      }
      return Integer.compare(idxB, idxA);
   }

   private static int tierPriority(DropEntry e) {
      if (e == null) {
         return 0;
      }
      String id = e.id != null ? e.id.toLowerCase() : "";
      String name = e.name != null ? e.name.toLowerCase() : "";
      if (id.contains("rune") || name.contains("rune")) {
         return 0;
      }
      if (id.contains("catalyst") || name.contains("catalyst")) {
         return 1;
      }
      if (e.book) {
         return 2;
      }
      return 3;
   }

   public DropAnimation current() {
      return this.current;
   }

   public int queueSize() {
      return this.queue.size();
   }

   public void reset() {
      this.pending.clear();
      this.queue.clear();
      this.current = null;
      this.burstStartMs = -1L;
   }

   private void enqueue(DropAnimation a) {
      if (this.queue.size() < Math.max(1, AIAConfig.queueLimit)) {
         this.queue.add(a);
      }
   }

   private DropAnimation build(AnimationManager.Pending p) {
      DropEntry e = p.entry;
      ItemLook look = e != null ? e.look : null;
      Visual visual = null;
      if (e != null && e.preferInventory) {
         ItemStack st = this.findInInventory(p);
         if (st != null) {
            visual = fromStack(st);
         }
      }

      if (visual == null && look != null) {
         if (look.mode() == ItemLook.Mode.VOXEL) {
            VoxelModel vox = VoxelModel.get(look);
            if (vox != null) {
               visual = Visual.voxel(vox);
            } else if (Minecraft.getInstance() != null) {
               try {
                  visual = Visual.item(look.fallbackStack());
               } catch (Throwable ignored) {
               }
            }
         } else {
            visual = Visual.head(look.headKey());
         }
      }

      return new DropAnimation(e, p.name, p.rarity, visual);
   }

   private static Visual fromStack(ItemStack st) {
      if (st.is(Items.PLAYER_HEAD)) {
         String key = headKeyOf(st);
         if (key != null) {
            HeadTextures.warm(key);
            return Visual.head(key);
         }
      }

      ItemStack one = st.copy();
      one.setCount(1);
      return Visual.item(one);
   }

   private static String headKeyOf(ItemStack st) {
      try {
         ResolvableProfile profile = st.get(DataComponents.PROFILE);
         if (profile == null) {
            return null;
         }

         Collection<Property> props = profile.partialProfile().properties().get("textures");
         if (props == null) {
            return null;
         }

         for (Property pr : props) {
            String value = propertyValue(pr);
            if (value != null && !value.isEmpty()) {
               String url = ItemLook.urlFromValue(value);
               if (url != null) {
                  return "url:" + url;
               }
            }
         }
      } catch (Throwable t) {
         AIAConfig.log("could not read head texture from inventory stack: " + t);
      }

      return null;
   }

   private static String propertyValue(Property p) {
      try {
         return (String)p.getClass().getMethod("value").invoke(p);
      } catch (Throwable ignored) {
         try {
            return (String)p.getClass().getMethod("getValue").invoke(p);
         } catch (Throwable ignored2) {
            return null;
         }
      }
   }

   public void tick(Minecraft mc) {
      if (mc.player != null && mc.level != null || mc.gui.screen() instanceof ConfigScreen) {
         if ((++this.warmTick & 1) == 0 && this.warmIdx < DropCatalog.ALL.size()) {
            DropEntry d = DropCatalog.ALL.get(this.warmIdx++);
            if (d.enabled && d.look.headKey() != null) {
               HeadTextures.warm(d.look.headKey());
            }
         }

         Iterator<AnimationManager.Pending> it = this.pending.iterator();

         while (it.hasNext()) {
            AnimationManager.Pending p = it.next();
            p.age++;
            if (this.findInInventory(p) != null || p.age >= MAX_WAIT_TICKS) {
               it.remove();
               this.enqueue(this.build(p));
            }
         }

         long now = System.currentTimeMillis();
         if (this.current != null && this.current.finished(now)) {
            this.current = null;
         }

         if (this.current == null && !this.queue.isEmpty()) {
            this.current = this.queue.poll();
            this.current.startMs = now;
            this.burstStartMs = now;
            SoundPlayer.play(this.current.rarity);
         }
      } else {
         this.pending.clear();
         this.queue.clear();
         this.current = null;
         this.burstStartMs = -1L;
         this.warmIdx = 0;
      }
   }

   public void draw(GuiGraphicsExtractor ctx, int w, int h) {
      DropAnimation a = this.current;
      if (a != null) {
         if (!a.finished(System.currentTimeMillis())) {
            a.render(ctx, w, h);
         }
      }
   }

   private static String strip(String s) {
      String r = ChatFormatting.stripFormatting(s);
      return r == null ? "" : r.trim();
   }

   private ItemStack findInInventory(AnimationManager.Pending p) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null) {
         return null;
      } else {
         String want = p.name.toLowerCase();
         boolean usePattern = p.entry.pattern != null && !p.entry.generic;

         for (ItemStack st : mc.player.getInventory().getNonEquipmentItems()) {
            if (st != null && !st.isEmpty()) {
               String n = strip(st.getHoverName().getString());
               String nl = n.toLowerCase();
               if (nl.equals(want) || want.length() > 3 && nl.contains(want)) {
                  return st.copy();
               }

               if (usePattern && p.entry.pattern.matcher(n).find()) {
                  return st.copy();
               }
            }
         }

         return null;
      }
   }

   private static class Pending {
      final DropEntry entry;
      final String name;
      final Rarity rarity;
      int age;

      Pending(DropEntry e, String n, Rarity r) {
         this.entry = e;
         this.name = n;
         this.rarity = r;
      }
   }
}
