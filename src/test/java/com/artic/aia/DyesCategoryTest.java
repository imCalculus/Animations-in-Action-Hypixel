package com.artic.aia;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DyesCategoryTest {
   private static final String[][] APPROVED_DYES = new String[][]{
      {"aquamarine_dye", "Aquamarine Dye"},
      {"archfiend_dye", "Archfiend Dye"},
      {"bone_dye", "Bone Dye"},
      {"brick_red_dye", "Brick Red Dye"},
      {"byzantium_dye", "Byzantium Dye"},
      {"carmine_dye", "Carmine Dye"},
      {"celadon_dye", "Celadon Dye"},
      {"celeste_dye", "Celeste Dye"},
      {"copper_dye", "Copper Dye"},
      {"cyclamen_dye", "Cyclamen Dye"},
      {"dung_dye", "Dung Dye"},
      {"emerald_dye", "Emerald Dye"},
      {"flame_dye", "Flame Dye"},
      {"fossil_dye", "Fossil Dye"},
      {"frostbitten_dye", "Frostbitten Dye"},
      {"holly_dye", "Holly Dye"},
      {"iceberg_dye", "Iceberg Dye"},
      {"jade_dye", "Jade Dye"},
      {"livid_dye", "Livid Dye"},
      {"mango_dye", "Mango Dye"},
      {"matcha_dye", "Matcha Dye"},
      {"midnight_dye", "Midnight Dye"},
      {"mocha_dye", "Mocha Dye"},
      {"mythological_dye", "Mythological Dye"},
      {"nadeshiko_dye", "Nadeshiko Dye"},
      {"necron_dye", "Necron Dye"},
      {"nyanza_dye", "Nyanza Dye"},
      {"pearlescent_dye", "Pearlescent Dye"},
      {"pelt_dye", "Pelt Dye"},
      {"periwinkle_dye", "Periwinkle Dye"},
      {"sangria_dye", "Sangria Dye"},
      {"secret_dye", "Secret Dye"},
      {"tentacle_dye", "Tentacle Dye"},
      {"wild_strawberry_dye", "Wild Strawberry Dye"},
      {"bingo_blue_dye", "Bingo Blue Dye"},
      {"dark_purple_dye", "Dark Purple Dye"},
      {"pure_black_dye", "Pure Black Dye"},
      {"pure_white_dye", "Pure White Dye"},
      {"pure_yellow_dye", "Pure Yellow Dye"},
      {"pure_blue_dye", "Pure Blue Dye"}
   };

   @BeforeEach
   void setUp() {
      AIAConfig.master = true;
      AIAConfig.strict = true;
      AIAConfig.serverFilter = "";
      AnimationManager.INSTANCE.reset();
      for (DropEntry d : DropCatalog.ALL) {
         d.enabled = true;
         d.override = null;
      }
   }

   @Test
   void testExactFortyDyesIsolatedInDyesCategoryWithVerifiedSkulls() {
      List<DropEntry> dyes = DropCatalog.byCategory(Category.DYES);
      assertEquals(40, dyes.size(), "Category.DYES must contain exactly the 40 approved dyes");

      // Ensure no dyes exist in any other category
      for (DropEntry entry : DropCatalog.ALL) {
         if (entry.category != Category.DYES) {
            assertFalse(entry.id.endsWith("_dye"), "Dye " + entry.id + " must not exist outside Category.DYES");
         }
      }

      // Ensure every approved dye exists in DYES with AUTO rarity and a real Mojang skull URL
      for (String[] pair : APPROVED_DYES) {
         String id = pair[0];
         String name = pair[1];
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing approved dye: " + id);
         assertEquals(Category.DYES, d.category, "Dye " + id + " must belong to Category.DYES");
         assertEquals("dyes", d.group, "Dye " + id + " must belong to group:dyes");
         assertNull(d.override, "Dye " + id + " must default to AUTO rarity");
         assertFalse(d.fixedRarity, "Dye " + id + " must not set fixedRarity");
         assertEquals(ItemLook.Mode.HEAD, d.look.mode(), "Dye " + id + " must use HEAD skull mode");
         assertNotNull(d.look.headKey());
         assertTrue(d.look.headKey().startsWith("url:http"), "Dye " + id + " must have Mojang texture URL");
         assertTrue(d.matches("RARE DROP!", name), "Dye " + id + " failed to match: " + name);
      }

      // Excluded dyes must not match
      for (DropEntry d : dyes) {
         assertFalse(d.matches("RARE DROP!", "Chocolate Dye"), "Chocolate Dye must be excluded");
         assertFalse(d.matches("RARE DROP!", "Warden Dye"), "Fire Sale dye Warden Dye must be excluded");
      }
   }

   @Test
   void testIsolatedDyesCategoryToggle() {
      List<DropCatalog.Row> rows = DropCatalog.rows(Category.DYES);
      assertEquals(1, rows.size(), "DYES tab should expose a single grouped toggle row for all 40 dyes");
      DropCatalog.Row groupRow = rows.get(0);
      assertTrue(groupRow.isGroup());
      assertEquals(40, groupRow.members.size());

      groupRow.setEnabled(false);
      for (DropEntry dye : DropCatalog.byCategory(Category.DYES)) {
         assertFalse(dye.enabled, "Disabling Dyes toggle must disable " + dye.id);
      }
      assertTrue(DropCatalog.byId("warden_heart").enabled, "Disabling Dyes must not affect non-dye drops");

      groupRow.setEnabled(true);
      for (DropEntry dye : DropCatalog.byCategory(Category.DYES)) {
         assertTrue(dye.enabled, "Re-enabling Dyes toggle must enable " + dye.id);
      }
   }

   @Test
   void testChatDetectorHypixelDyeAnnouncements() {
      ChatDetector detector = new ChatDetector();

      // 1. [!] with You dropped ... and (+X% Magic Find)
      detector.handle("§d§l[!] §r§7You dropped §r§6Wild Strawberry Dye§7! §r§b(+200% ✯ Magic Find)");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("wild_strawberry_dye", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 2. [!] with bracketed [+X% Magic Find]
      AnimationManager.INSTANCE.reset();
      detector.handle("§5§l[!] §r§5Tentacle Dye §r§b[+240% ✯ Magic Find]");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("tentacle_dye", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);

      // 3. PRAY TO RNGESUS DROP!
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lPRAY TO RNGESUS DROP! §r§7(§6Copper Dye§7) §r§b(+250% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("copper_dye", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 4. INSANE DROP!
      AnimationManager.INSTANCE.reset();
      detector.handle("§c§lINSANE DROP! §r§5Pure Black Dye §r§b(+300% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("pure_black_dye", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);

      // 5. CRAZY RARE DROP!
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lCRAZY RARE DROP! §r§6Mythological Dye §r§b(+210% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("mythological_dye", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);
   }
}
