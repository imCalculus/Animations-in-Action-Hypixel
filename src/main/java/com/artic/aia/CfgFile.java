package com.artic.aia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class CfgFile {
   private final File file;
   private final Map<String, Map<String, String>> values = new LinkedHashMap<>();
   private final Map<String, Map<String, String>> comments = new HashMap<>();

   CfgFile(File file) {
      this.file = file;
      this.read();
   }

   private void read() {
      if (this.file.isFile()) {
         BufferedReader r = null;

         try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8));
            String section = null;

            String line;
            while ((line = r.readLine()) != null) {
               String t = line.trim();
               if (!t.isEmpty() && !t.startsWith("#")) {
                  if (t.endsWith("{")) {
                     section = t.substring(0, t.length() - 1).trim();
                     if (!this.values.containsKey(section)) {
                        this.values.put(section, new LinkedHashMap<>());
                     }
                  } else if (t.equals("}")) {
                     section = null;
                  } else if (section != null) {
                     int eq = t.indexOf('=');
                     if (eq >= 0) {
                        String k = t.substring(0, eq).trim();
                        if (k.length() >= 2 && k.charAt(1) == ':') {
                           k = k.substring(2);
                        }

                        this.values.get(section).put(k, t.substring(eq + 1).trim());
                     }
                  }
               }
            }
         } catch (IOException ignored) {
         } finally {
            if (r != null) {
               try {
                  r.close();
               } catch (IOException ignored) {
               }
            }
         }
      }
   }

   synchronized String getString(String category, String key, String def, String comment) {
      Map<String, String> m = this.values.get(category);
      if (m == null) {
         m = new LinkedHashMap<>();
         this.values.put(category, m);
      }

      String v = m.get(key);
      if (v == null) {
         v = def;
         m.put(key, def);
      }

      if (comment != null) {
         Map<String, String> c = this.comments.get(category);
         if (c == null) {
            c = new HashMap<>();
            this.comments.put(category, c);
         }

         c.put(key, comment);
      }

      return v;
   }

   synchronized void set(String category, String key, String value) {
      Map<String, String> m = this.values.get(category);
      if (m == null) {
         m = new LinkedHashMap<>();
         this.values.put(category, m);
      }

      m.put(key, value);
   }

   synchronized void save() {
      File parent = this.file.getParentFile();
      if (parent != null) {
         parent.mkdirs();
      }

      Writer w = null;

      try {
         w = new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8);
         w.write("# Configuration file\n\n");

         for (Entry<String, Map<String, String>> sec : this.values.entrySet()) {
            w.write(sec.getKey() + " {\n");
            Map<String, String> c = this.comments.get(sec.getKey());

            for (Entry<String, String> e : sec.getValue().entrySet()) {
               String cm = c == null ? null : c.get(e.getKey());
               if (cm != null) {
                  w.write("    # " + cm.replace('\n', ' ') + "\n");
               }

               w.write("    S:" + e.getKey() + "=" + e.getValue() + "\n");
            }

            w.write("}\n\n");
         }
      } catch (IOException ignored) {
      } finally {
         if (w != null) {
            try {
               w.close();
            } catch (IOException ignored) {
            }
         }
      }
   }
}
