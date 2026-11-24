package com.artic.aia;

public enum Rarity {
   COMMON("COMMON", 'f', 16777215, 13684944),
   UNCOMMON("UNCOMMON", 'a', 5635925, 11927477),
   RARE("RARE", '9', 5601279, 10533119),
   EPIC("EPIC", '5', 11545855, 14721279),
   LEGENDARY("LEGENDARY", '6', 16755200, 16769658),
   MYTHIC("MYTHIC", 'd', 14830591, 11563263),
   DIVINE("DIVINE", 'b', 5636095, 13172735),
   SPECIAL("SPECIAL", 'c', 16733525, 16756912);

   public final String label;
   public final char code;
   public final int primary;
   public final int secondary;

   private Rarity(String label, char code, int primary, int secondary) {
      this.label = label;
      this.code = code;
      this.primary = primary;
      this.secondary = secondary;
   }

   public static Rarity fromCode(char c) {
      c = Character.toLowerCase(c);

      for (Rarity r : values()) {
         if (r.code == c) {
            return r;
         }
      }

      return null;
   }
}
