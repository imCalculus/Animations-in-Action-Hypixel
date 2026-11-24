package com.artic.aia;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlayerDropsAndPreemptionTest {
   private static final Set<Category> SLAYER_CATEGORIES = EnumSet.of(
      Category.REVENANT,
      Category.SVEN,
      Category.TARANTULA,
      Category.ENDERMAN,
      Category.DEMONLORD,
      Category.VAMPIRE
   );

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
   void testNoDuplicateSlayerRowsAndAutoRarityDefault() throws Exception {
      assertFalse(DropCatalog.ALL.isEmpty(), "DropCatalog.ALL must not be empty");

      Set<String> allIds = new HashSet<>();
      Set<String> slayerIds = new HashSet<>();
      Set<String> slayerNames = new HashSet<>();

      try (InputStream in = DropCatalog.class.getResourceAsStream("/assets/animationinaction/drops.tsv");
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
         String line;
         int lineNum = 0;
         while ((line = reader.readLine()) != null) {
            lineNum++;
            if (line.isEmpty() || line.charAt(0) == '#') {
               continue;
            }
            String[] cols = line.split("\t", -1);
            assertTrue(cols.length >= 9, "Line " + lineNum + " must have 9 TSV columns");
            String id = cols[0].trim();
            Category cat = Category.valueOf(cols[1].trim());
            String name = cols[2].trim();

            assertTrue(allIds.add(id.toLowerCase()), "Duplicate drop id in drops.tsv: " + id);
            if (SLAYER_CATEGORIES.contains(cat)) {
               assertTrue(slayerIds.add(id.toLowerCase()), "Duplicate Slayer drop id: " + id);
               assertTrue(slayerNames.add(name.toLowerCase()), "Duplicate Slayer drop name: " + name);
            }
         }
      }

      for (Category cat : SLAYER_CATEGORIES) {
         List<DropEntry> entries = DropCatalog.byCategory(cat);
         assertFalse(entries.isEmpty(), "Slayer category " + cat + " must have entries");
         for (DropEntry entry : entries) {
            assertNull(entry.override, "Slayer entry " + entry.id + " must default to AUTO rarity (override == null)");
            assertFalse(entry.fixedRarity, "Slayer entry " + entry.id + " must not force fixedRarity");
            assertNotNull(entry.defaultRarity, "Slayer entry " + entry.id + " must have a fallback defaultRarity");
            assertNotNull(entry.pattern, "Slayer entry " + entry.id + " must have a compiled regex pattern");
         }
      }
   }

   @Test
   void testAllSixSlayerBossTargetDropsPresent() {
      String[][] expectedDrops = new String[][]{
         // 1. Revenant Horror
         {"undead_catalyst", "Undead Catalyst"},
         {"revenant_catalyst", "Revenant Catalyst"},
         {"pestilence_rune_1", "◆ Pestilence Rune I"},
         {"snake_rune_1", "◆ Snake Rune I"},
         {"beheaded_horror", "Beheaded Horror"},
         {"festering_maggot", "Festering Maggot"},
         {"scythe_blade", "Scythe Blade"},
         {"severed_hand", "Severed Hand"},
         {"shredded_sinew", "Shredded Sinew"},
         {"warden_heart", "Warden Heart"},
         {"paragon_shard", "Paragon Shard"},
         {"smite_6", "Enchanted Book (Smite VI)"},
         // 2. Tarantula Broodfather
         {"spider_catalyst", "Spider Catalyst"},
         {"bite_rune_1", "◆ Bite Rune I"},
         {"darkness_within_rune_1", "◆ Darkness Within Rune I"},
         {"tarantula_catalyst", "Tarantula Catalyst"},
         {"fly_swatter", "Fly Swatter"},
         {"tarantula_talisman", "Tarantula Talisman"},
         {"vial_of_venom", "Vial of Venom"},
         {"digested_mosquito", "Digested Mosquito"},
         {"shriveled_wasp", "Shriveled Wasp"},
         {"ensnared_snail", "Ensnared Snail"},
         {"primordial_eye", "Primordial Eye"},
         {"arachnes_fang", "Arachne's Fang"},
         {"bane_of_arthropods_6", "Enchanted Book (Bane of Arthropods VI)"},
         // 3. Sven Packmaster
         {"spirit_rune_1", "◆ Spirit Rune I"},
         {"couture_rune_1", "◆ Couture Rune I"},
         {"red_claw_egg", "Red Claw Egg"},
         {"grizzly_bait", "Grizzly Salmon"},
         {"grizzly_bait", "Grizzly Bait"},
         {"overflux_capacitor", "Overflux Capacitor"},
         {"critical_6", "Enchanted Book (Critical VI)"},
         // 4. Voidgloom Seraph
         {"null_atom", "Null Atom"},
         {"end_rune_1", "◆ End Rune I"},
         {"transmission_tuner", "Transmission Tuner"},
         {"sinful_dice", "Sinful Dice"},
         {"etherwarp_merger", "Etherwarp Merger"},
         {"pocket_espresso_machine", "Pocket Espresso Machine"},
         {"handy_blood_chalice", "Handy Blood Chalice"},
         {"hazmat_enderman", "Hazmat Enderman"},
         {"judgement_core", "Judgement Core"},
         {"enchant_rune_1", "◆ Enchant Rune I"},
         {"exceedingly_rare_ender_artifact_upgrader", "Exceedingly Rare Ender Artifact Upgrader"},
         {"end_stone_idol", "End Stone Idol"},
         {"void_conqueror_enderman_skin", "Void Conqueror Enderman Skin"},
         {"mana_steal_1", "Enchanted Book (Mana Steal I)"},
         {"smarty_pants_1", "Enchanted Book (Smarty Pants I)"},
         {"ender_slayer_7", "Enchanted Book (Ender Slayer VII)"},
         // 5. Inferno Demonlord
         {"lavatears_rune_1", "◆ Lavatears Rune I"},
         {"mana_disintegrator", "Mana Disintegrator"},
         {"scorched_power_crystal", "Scorched Power Crystal"},
         {"scorched_books", "Scorched Books"},
         {"kelvin_inverter", "Kelvin Inverter"},
         {"fiery_burst_rune_1", "◆ Fiery Burst Rune I"},
         {"archfiend_dice", "Archfiend Dice"},
         {"wilsons_engineering_plans", "Wilson's Engineering Plans"},
         {"subzero_inverter", "Subzero Inverter"},
         {"high_class_archfiend_dice", "High Class Archfiend Dice"},
         {"fire_aspect_3", "Enchanted Book (Fire Aspect III)"},
         {"duplex_1", "Enchanted Book (Duplex I)"},
         {"smoldering_6", "Enchanted Book (Smoldering VI)"},
         // 6. Riftstalker Bloodfiend
         {"soultwist_rune_1", "◆ Soultwist Rune I"},
         {"bubba_blister", "Bubba Blister"},
         {"fangtastic_chocolate_chip", "Fang-tastic Chocolate Chip"},
         {"guardian_lucky_block", "Guardian Lucky Block"},
         {"mcgrubbers_burger", "McGrubber's Burger"},
         {"unfanged_vampire_part", "Unfanged Vampire Part"},
         {"quantum_bundle", "Enchanted Book Bundle (Quantum)"},
         {"the_one_bundle", "Enchanted Book Bundle (The One)"}
      };

      for (String[] pair : expectedDrops) {
         String id = pair[0];
         String sampleText = pair[1];
         DropEntry entry = DropCatalog.byId(id);
         assertNotNull(entry, "Missing Slayer drop entry: " + id);
         assertTrue(SLAYER_CATEGORIES.contains(entry.category), "Entry " + id + " must belong to a Slayer category");
         assertTrue(entry.matches("RARE DROP!", sampleText), "Entry " + id + " failed to match: " + sampleText);
      }
   }

   @Test
   void testEnchantedBooksMatchWrappedAndBareNames() {
      String[][] bookCases = new String[][]{
         {"smite_6", "Enchanted Book (Smite VI)", "Smite VI", "Smite 6"},
         {"bane_of_arthropods_6", "Enchanted Book (Bane of Arthropods VI)", "Bane of Arthropods VI", "Bane of Arthropods 6"},
         {"critical_6", "Enchanted Book (Critical VI)", "Critical VI", "Critical 6"},
         {"ender_slayer_7", "Enchanted Book (Ender Slayer VII)", "Ender Slayer VII", "Ender Slayer 7"},
         {"mana_steal_1", "Enchanted Book (Mana Steal I)", "Mana Steal I", "Mana Steal 1"},
         {"smarty_pants_1", "Enchanted Book (Smarty Pants I)", "Smarty Pants I", "Smarty Pants 1"},
         {"fire_aspect_3", "Enchanted Book (Fire Aspect III)", "Fire Aspect III", "Fire Aspect 3"},
         {"duplex_1", "Enchanted Book (Duplex I)", "Duplex I", "Duplex 1"},
         {"smoldering_6", "Enchanted Book (Smoldering VI)", "Smoldering VI", "Smoldering 6"},
         {"quantum_bundle", "Enchanted Book Bundle (Quantum)", "Enchanted Book (Quantum)", "Quantum"},
         {"the_one_bundle", "Enchanted Book Bundle (The One)", "Enchanted Book (The One)", "The One"}
      };

      for (String[] tc : bookCases) {
         String id = tc[0];
         DropEntry entry = DropCatalog.byId(id);
         assertNotNull(entry, "Book entry missing: " + id);
         assertTrue(entry.book, "Entry " + id + " must have book flag set");
         for (int i = 1; i < tc.length; i++) {
            String variant = tc[i];
            assertTrue(
               entry.matches("RARE DROP!", variant),
               "Book entry " + id + " must match variant: " + variant
            );
         }
      }
   }

   @Test
   void testChatDetectorHypixelSlayerMessagesAndAutoRarity() {
      ChatDetector detector = new ChatDetector();

      // 1. CRAZY RARE DROP! with parenthesized item and Magic Find tail
      MutableComponent wardenMsg = Component.empty()
         .append(Component.literal("CRAZY RARE DROP! ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
         .append(Component.literal("(").withStyle(ChatFormatting.GRAY))
         .append(Component.literal("Warden Heart").withStyle(ChatFormatting.GOLD))
         .append(Component.literal(") ").withStyle(ChatFormatting.GRAY))
         .append(Component.literal("(+250% ✯ Magic Find)").withStyle(ChatFormatting.AQUA));

      detector.onMessage(wardenMsg);
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "Warden Heart drop should trigger immediately");
      assertEquals("warden_heart", current.entry.id);
      assertEquals("Warden Heart", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 2. Enchanted Book (Smite VI) with Magic Find tail
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§9Enchanted Book (Smite VI) §r§b(+210% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "Enchanted Book (Smite VI) should trigger immediately");
      assertEquals("smite_6", current.entry.id);
      assertEquals("Smite VI", current.name);
      assertEquals(Rarity.RARE, current.rarity);

      // 3. Bare book name "Duplex I" wrapped in outer parentheses with MYTHIC color code in chat (testing AUTO rarity from chat color)
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lCRAZY RARE DROP! §r§7(§dDuplex I§7) §r§b(+300% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "Bare Duplex I should trigger immediately");
      assertEquals("duplex_1", current.entry.id);
      assertEquals("Duplex I", current.name);
      assertEquals(Rarity.MYTHIC, current.rarity, "AUTO rarity should adopt §d MYTHIC from chat color");

      // 4. COMMON item with §b(+200% ✯ Magic Find) tail must NOT misread §b Magic Find as DIVINE
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§fPocket Espresso Machine §r§b(+200% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "Pocket Espresso Machine should trigger immediately");
      assertEquals("pocket_espresso_machine", current.entry.id);
      assertEquals(Rarity.COMMON, current.rarity, "Magic Find §b colour must not leak into item rarity");

      // 5. RNG METER! prefix and rune symbol ◆
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lRNG METER! §r§d§lCRAZY RARE DROP! §r§7(§6◆ Snake Rune I§7) §r§b(+180% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "RNG METER! Snake Rune I should trigger immediately");
      assertEquals("snake_rune_1", current.entry.id);
      assertEquals("Snake Rune I", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);
   }

   @Test
   void testZeroDelayBurstPreemptionAndTieBreaking() {
      DropEntry undeadCatalyst = DropCatalog.byId("undead_catalyst"); // RARE catalyst
      DropEntry smite6 = DropCatalog.byId("smite_6");                 // RARE book
      DropEntry wardenHeart = DropCatalog.byId("warden_heart");       // LEGENDARY major drop
      DropEntry scytheBlade = DropCatalog.byId("scythe_blade");       // LEGENDARY major drop
      DropEntry snakeRune = DropCatalog.byId("snake_rune_1");         // LEGENDARY rune

      long t0 = 100_000L;

      // 1. First drop triggers immediately at 0ms
      AnimationManager.INSTANCE.submit(undeadCatalyst, undeadCatalyst.name, Rarity.RARE, false, t0);
      DropAnimation first = AnimationManager.INSTANCE.current();
      assertNotNull(first, "Drop must start with 0ms delay");
      assertEquals("undead_catalyst", first.entry.id);
      assertEquals(t0, first.startMs);
      assertEquals(0, AnimationManager.INSTANCE.queueSize());

      // 2. Same rarity (RARE) Enchanted Book arrives 20ms later (within 150ms burst):
      // Tie-breaker: Book (tier 2) > Catalyst (tier 1) -> preempts immediately without queuing!
      AnimationManager.INSTANCE.submit(smite6, smite6.name, Rarity.RARE, false, t0 + 20L);
      DropAnimation second = AnimationManager.INSTANCE.current();
      assertNotNull(second);
      assertEquals("smite_6", second.entry.id, "Book should preempt catalyst of equal rarity in same burst");
      assertEquals(0, AnimationManager.INSTANCE.queueSize(), "Preempted drop must not be queued");

      // 3. LEGENDARY Rune arrives 45ms after t0 (within 150ms burst):
      // Higher rarity (LEGENDARY > RARE) -> preempts RARE book immediately!
      AnimationManager.INSTANCE.submit(snakeRune, snakeRune.name, Rarity.LEGENDARY, false, t0 + 45L);
      DropAnimation third = AnimationManager.INSTANCE.current();
      assertNotNull(third);
      assertEquals("snake_rune_1", third.entry.id, "Higher rarity rune should preempt lower rarity book in burst");
      assertEquals(0, AnimationManager.INSTANCE.queueSize());

      // 4. LEGENDARY Major drop (Warden Heart) arrives 70ms after t0 (within 150ms burst):
      // Same rarity (LEGENDARY), but Major drop (tier 3) > Rune (tier 0) -> preempts rune!
      AnimationManager.INSTANCE.submit(wardenHeart, wardenHeart.name, Rarity.LEGENDARY, false, t0 + 70L);
      DropAnimation fourth = AnimationManager.INSTANCE.current();
      assertNotNull(fourth);
      assertEquals("warden_heart", fourth.entry.id, "Major slayer drop should preempt rune of same rarity in burst");
      assertEquals(0, AnimationManager.INSTANCE.queueSize());

      // 5. Earlier catalog-order LEGENDARY Major drop (Scythe Blade) arrives 110ms after t0 (within 150ms burst):
      // Same rarity (LEGENDARY) and same tier (Major drop), Scythe Blade is earlier alphabetically in catalog -> wins tie-break!
      AnimationManager.INSTANCE.submit(scytheBlade, scytheBlade.name, Rarity.LEGENDARY, false, t0 + 110L);
      DropAnimation fifth = AnimationManager.INSTANCE.current();
      assertNotNull(fifth);
      assertEquals("scythe_blade", fifth.entry.id, "Higher catalog priority major drop should win tie-break");
      assertEquals(0, AnimationManager.INSTANCE.queueSize());

      // 6. Lower priority drop (Undead Catalyst) arrives 140ms after t0 (still within 150ms burst):
      // Must be ignored and NOT queued!
      AnimationManager.INSTANCE.submit(undeadCatalyst, undeadCatalyst.name, Rarity.RARE, false, t0 + 140L);
      assertEquals("scythe_blade", AnimationManager.INSTANCE.current().entry.id, "Lower priority drop in burst must be ignored");
      assertEquals(0, AnimationManager.INSTANCE.queueSize(), "Ignored burst drop must not be queued");
   }
}
