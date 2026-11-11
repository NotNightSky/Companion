package org.notnightsky.companion.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import org.notnightsky.companion.CompanionClient;
import org.notnightsky.companion.render.Anchor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Textures {
    public record TextureSet(Path jsonPath, JsonObject json) {}
    public record GifFrame(NativeImage image, int delayMs) {}

    public static List<TextureSet> scan() {
        try (Stream<Path> stream = Files.walk(CompanionClient.getConfigPath())) {
            return stream.filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().equals("config.json"))
                    .sorted()
                    .map(path -> new TextureSet(path, load(path)))
                    .filter(set -> set.json() != null && set.json().has("textures"))
                    .toList();
        } catch (IOException e) {
            CompanionClient.LOGGER.error("Failed to scan the mod configuration folder for texture sets.", e);
        }
        return List.of();
    }

    public static JsonObject load(Path jsonPath) {
        try (FileReader reader = new FileReader(jsonPath.toFile())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | IllegalStateException | JsonParseException e) {
            CompanionClient.LOGGER.warn("Failed to parse {}, skipping it.", jsonPath, e);
        }
        return null;
    }

    public static String relativePath(Path jsonPath) {
        Path relative = CompanionClient.getConfigPath().relativize(jsonPath);
        return relative.toString().replace('\\', '/');
    }

    public static Path resolveTexturePath(Path baseDir, String rawName, String cleanName) {
        Path exact = baseDir.resolve(rawName);
        if (Files.exists(exact) && !Files.isDirectory(exact)) {
            return exact;
        }
        Path gifPath = baseDir.resolve(cleanName + ".gif");
        if (Files.exists(gifPath)) {
            return gifPath;
        }
        Path pngPath = baseDir.resolve(cleanName + ".png");
        if (Files.exists(pngPath)) {
            return pngPath;
        }
        return null;
    }

    public static List<GifFrame> decodeGif(Path gifPath) throws IOException {
        List<GifFrame> results = new ArrayList<>();

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) {
            throw new IOException("No ImageIO GIF reader available");
        }
        ImageReader reader = readers.next();

        try (ImageInputStream iis = ImageIO.createImageInputStream(gifPath.toFile())) {
            reader.setInput(iis, false, false);

            int canvasWidth = -1;
            int canvasHeight = -1;

            IIOMetadata streamMeta = reader.getStreamMetadata();
            if (streamMeta != null) {
                IIOMetadataNode globalRoot = (IIOMetadataNode) streamMeta.getAsTree(streamMeta.getNativeMetadataFormatName());
                NodeList screenDescriptors = globalRoot.getElementsByTagName("LogicalScreenDescriptor");
                if (screenDescriptors.getLength() > 0) {
                    IIOMetadataNode screenDescriptor = (IIOMetadataNode) screenDescriptors.item(0);
                    canvasWidth = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    canvasHeight = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }

            int numFrames = reader.getNumImages(true);
            BufferedImage master = null;

            for (int i = 0; i < numFrames; i++) {
                BufferedImage rawFrame = reader.read(i);

                if (canvasWidth <= 0 || canvasHeight <= 0) {
                    canvasWidth = rawFrame.getWidth();
                    canvasHeight = rawFrame.getHeight();
                }

                if (master == null) {
                    master = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                }

                IIOMetadata imageMeta = reader.getImageMetadata(i);
                IIOMetadataNode root = (IIOMetadataNode) imageMeta.getAsTree("javax_imageio_gif_image_1.0");
                IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);

                int delayMs = 100;
                String disposal = "none";
                if (gce != null) {
                    int delayCs = Integer.parseInt(gce.getAttribute("delayTime"));
                    delayMs = delayCs <= 0 ? 100 : delayCs * 10;
                    disposal = gce.getAttribute("disposalMethod");
                }

                int frameX = 0;
                int frameY = 0;
                NodeList children = root.getChildNodes();
                for (int c = 0; c < children.getLength(); c++) {
                    Node nodeItem = children.item(c);
                    if ("ImageDescriptor".equals(nodeItem.getNodeName())) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        frameX = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
                        frameY = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }

                BufferedImage backup = null;
                if ("restoreToPrevious".equals(disposal)) {
                    backup = copyBufferedImage(master);
                }

                BufferedImage argbSubFrame = new BufferedImage(rawFrame.getWidth(), rawFrame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < rawFrame.getHeight(); y++) {
                    for (int x = 0; x < rawFrame.getWidth(); x++) {
                        argbSubFrame.setRGB(x, y, rawFrame.getRGB(x, y));
                    }
                }

                Graphics2D g2d = master.createGraphics();
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.drawImage(argbSubFrame, frameX, frameY, null);
                g2d.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(master, "png", baos);
                NativeImage nativeImg = NativeImage.read(new ByteArrayInputStream(baos.toByteArray()));
                results.add(new GifFrame(nativeImg, delayMs));

                if ("restoreToBackgroundColor".equals(disposal)) {
                    Graphics2D clearG = master.createGraphics();
                    clearG.setComposite(AlphaComposite.Clear);
                    clearG.fillRect(frameX, frameY, rawFrame.getWidth(), rawFrame.getHeight());
                    clearG.dispose();
                } else if ("restoreToPrevious".equals(disposal) && backup != null) {
                    master = backup;
                }
            }
        } finally {
            reader.dispose();
        }

        return results;
    }

    private static BufferedImage copyBufferedImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    public record Suggestions(Integer width, Integer height, Long frametime, Anchor anchor) {}

    public static Suggestions parseSuggestions(JsonObject json) {
        Integer width = null;
        Integer height = null;
        Long frametime = null;
        Anchor anchor = null;

        if (json.has("suggested") && json.get("suggested").isJsonObject()) {
            JsonObject suggested = json.getAsJsonObject("suggested");
            width = readPositiveInt(suggested, "width");
            height = readPositiveInt(suggested, "height");
            frametime = readPositiveLong(suggested, "frametime");
            anchor = readAnchor(suggested, "anchor");
        }
        if (width == null) {
            width = readPositiveInt(json, "suggested_width");
        }
        if (height == null) {
            height = readPositiveInt(json, "suggested_height");
        }
        if (frametime == null) {
            frametime = readPositiveLong(json, "suggested_frametime");
        }
        if (anchor == null) {
            anchor = readAnchor(json, "suggested_anchor");
        }

        return new Suggestions(width, height, frametime, anchor);
    }

    private static Integer readPositiveInt(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return Math.max(1, obj.get(key).getAsInt());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Long readPositiveLong(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return Math.max(1, obj.get(key).getAsLong());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Anchor readAnchor(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        return Anchor.tryParse(obj.get(key).getAsString());
    }
}