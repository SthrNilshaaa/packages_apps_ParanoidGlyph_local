package co.aospa.glyph.Utils;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomRingtoneManager {

    private static final String RINGTONE_DIR = "custom_ringtones";

    /**
     * Copies the picked OGG file to internal storage.
     */
    public static File importOgg(Context context, Uri uri, String fileName) {
        File dir = new File(context.getFilesDir(), RINGTONE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destination = new File(dir, fileName);
        try (InputStream is = context.getContentResolver().openInputStream(uri);
                OutputStream os = new FileOutputStream(destination)) {

            if (is == null)
                return null;
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return destination;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lists all imported custom ringtones.
     */
    public static List<File> getImportedRingtones(Context context) {
        List<File> ringtones = new ArrayList<>();
        File dir = new File(context.getFilesDir(), RINGTONE_DIR);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".ogg")) {
                        ringtones.add(f);
                    }
                }
            }
        }
        return ringtones;
    }

    /**
     * Checks if a selected style matches an imported custom ringtone name.
     */
    public static File getCustomRingtoneFile(Context context, String styleName) {
        List<File> imported = getImportedRingtones(context);
        for (File f : imported) {
            if (f.getName().equals(styleName)) {
                return f;
            }
        }
        return null; // Not a custom ringtone
    }

    /**
     * Deletes an imported custom ringtone.
     */
    public static boolean deleteRingtone(Context context, String fileName) {
        File dir = new File(context.getFilesDir(), RINGTONE_DIR);
        File file = new File(dir, fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
