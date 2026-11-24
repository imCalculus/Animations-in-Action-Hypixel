package com.artic.aia;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetsCategoryTest {
   private static final String[][] APPROVED_PETS = new String[][]{
      {"golden_dragon_egg", "Golden Dragon Egg"},
      {"jade_dragon_egg", "Jade Dragon Egg"},
      {"rose_dragon_egg", "Rose Dragon Egg"},
      {"spirit_pet", "[Lvl 1] Spirit"},
      {"scatha_pet", "[Lvl 1] Scatha"},
      {"bal_pet", "[Lvl 1] Bal"},
      {"flying_fish_pet", "[Lvl 1] Flying Fish"},
      {"baby_yeti_pet", "[Lvl 1] Baby Yeti"},
      {"megalodon_pet", "[Lvl 1] Megalodon"},
      {"squid_pet", "[Lvl 1] Squid"},
      {"guardian_pet", "[Lvl 1] Guardian"},
      {"reindeer_pet", "[Lvl 1] Reindeer"},
      {"seal_pet", "[Lvl 1] Seal"},
      {"frog_pet", "[Lvl 1] Frog"},
      {"hermit_crab_pet", "[Lvl 1] Hermit Crab"},
      {"kuudra_pet", "[Lvl 1] Kuudra"},
      {"slug_pet", "[Lvl 1] Slug"},
      {"phoenix_pet", "[Lvl 1] Phoenix"},
      {"ender_dragon_pet", "[Lvl 1] Ender Dragon"},
      {"enderman_pet", "[Lvl 1] Enderman"},
      {"tarantula_pet", "[Lvl 1] Tarantula"},
      {"spider_pet", "[Lvl 1] Spider"},
      {"hound_pet", "[Lvl 1] Hound"},
      {"ghoul_pet", "[Lvl 1] Ghoul"},
      {"rat_pet", "[Lvl 1] Rat"},
      {"jerry_pet", "[Lvl 1] Jerry"}
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
   void testExactTwentySixPetsAndSpiritMovedOutOfDungeons() {
      List<DropEntry> pets = DropCatalog.byCategory(Category.PETS);
      assertEquals(26, pets.size(), "Category.PETS must contain exactly the 26 approved entries");

      // Spirit Pet must NOT exist in DUNGEONS
      for (DropEntry d : DropCatalog.byCategory(Category.DUNGEONS)) {
         assertNotEquals("spirit_pet", d.id, "spirit_pet must be moved out of DUNGEONS into PETS");
      }

      // Removed non-approved pets must not exist
      assertNull(DropCatalog.byId("goat_pet"));
      assertNull(DropCatalog.byId("skeleton_horse_pet"));
      assertNull(DropCatalog.byId("snowman_pet"));

      for (String[] pair : APPROVED_PETS) {
         String id = pair[0];
         String sample = pair[1];
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing approved PETS entry: " + id);
         assertEquals(Category.PETS, d.category, id + " must belong to Category.PETS");
         assertNull(d.override, id + " must default to AUTO rarity");
         assertFalse(d.fixedRarity, id + " must not set fixedRarity");
         assertEquals(ItemLook.Mode.HEAD, d.look.mode(), id + " must use HEAD skull mode");
         assertNotNull(d.look.headKey());
         assertTrue(d.look.headKey().startsWith("url:http"), id + " must have verified Mojang URL");
         assertTrue(d.matches("PET DROP!", sample), id + " failed to match: " + sample);
      }
   }

   @Test
   void testChatDetectorPetDropMessagesAndAutoRarity() {
      ChatDetector detector = new ChatDetector();

      // 1. PET DROP! [Lvl 1] Spirit (EPIC §5)
      detector.handle("§6§lPET DROP! §r§7[Lvl 1] §5Spirit §r§b(+180% ✯ Magic Find)");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("spirit_pet", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);

      // 2. PET DROP! [Lvl 1] Megalodon (LEGENDARY §6)
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lPET DROP! §r§7[Lvl 1] §6Megalodon §r§b(+220% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("megalodon_pet", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 3. PRAY TO RNGESUS DROP! [Lvl 1] Tarantula (LEGENDARY §6)
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lPRAY TO RNGESUS DROP! §r§7[Lvl 1] §6Tarantula §r§b(+240% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("tarantula_pet", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 4. PET DROP! [Lvl 1] Jerry (MYTHIC §d)
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lPET DROP! §r§7[Lvl 1] §dJerry");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("jerry_pet", current.entry.id);
      assertEquals(Rarity.MYTHIC, current.rarity);

      // 5. RARE REWARD! [Lvl 1] Kuudra (RARE §9)
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE REWARD! §r§7[Lvl 1] §9Kuudra");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("kuudra_pet", current.entry.id);
      assertEquals(Rarity.RARE, current.rarity);
   }
}
