package io.github.miracelwhipp.typewriter.font;

import io.github.miracelwhipp.typewriter.spi.util.ForEachFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FontDescription {

    private static List<FontDescription> systemFonts;

    private final File location;
    private final String family;

    public FontDescription(File location, String family) {
        this.location = location;
        this.family = family;
    }

    public File getLocation() {
        return location;
    }

    public String getFamily() {
        return family;
    }

    public static Path getFontPath() {

        if (SystemUtils.IS_OS_WINDOWS) {

            return new File("c:\\Windows\\Fonts").toPath();
        }

        return new File("/usr/share/fonts/truetype/").toPath();
    }


    public static List<FontDescription> getSystemFonts() throws IOException {

        if (systemFonts != null) {

            return systemFonts;
        }

        List<FontDescription> result = new ArrayList<>();

        ForEachFile.foreach(getFontPath(), (file, attributes) -> {

            if (!FilenameUtils.isExtension(file.toString(), "ttf")) {

                return;
            }

            try {

                final Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file.toFile()));

                result.add(new FontDescription(file.toFile(), font.getFamily()));

            } catch (FontFormatException e) {

                return;
            }

        });


        return systemFonts = result;
    }

}
