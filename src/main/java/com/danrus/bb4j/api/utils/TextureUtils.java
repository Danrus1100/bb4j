package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.assets.AssetResolver;
import com.danrus.bb4j.assets.DataUrl;
import com.danrus.bb4j.assets.DataUrlDecoder;
import com.danrus.bb4j.assets.FileSystemAssetResolver;
import com.danrus.bb4j.assets.TextureContentLoader;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.texture.Texture;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class TextureUtils {
    public enum AlphaMode {
        OPAQUE,
        CUTOUT,
        TRANSLUCENT
    }

    private static final Map<String, AlphaMode> ALPHA_MODE_CACHE = new ConcurrentHashMap<>();
    
    private final BbModelDocument document;
    private final TextureContentLoader loader;
    
    private TextureUtils(BbModelDocument document, AssetResolver resolver, String basePath) {
        this.document = document;
        
        if (resolver != null) {
            this.loader = new TextureContentLoader(resolver);
        } else if (basePath != null) {
            this.loader = new TextureContentLoader(new FileSystemAssetResolver(basePath));
        } else {
            this.loader = new TextureContentLoader();
        }
    }
    
    public static TextureUtils forDocument(BbModelDocument document) {
        return new TextureUtils(document, null, null);
    }
    
    public static TextureUtils forDocument(BbModelDocument document, String basePath) {
        return new TextureUtils(document, null, basePath);
    }
    
    public static TextureUtils forDocument(BbModelDocument document, AssetResolver resolver) {
        return new TextureUtils(document, resolver, null);
    }
    
    public List<Texture> getAllTextures() {
        return document.getTextures() != null 
            ? new ArrayList<>(document.getTextures()) 
            : Collections.emptyList();
    }
    
    public Texture getTextureByUuid(String uuid) {
        return getTextureByReference(uuid);
    }

    public Texture getTextureByReference(String reference) {
        if (reference == null || document.getTextures() == null || document.getTextures().isEmpty()) {
            return null;
        }

        boolean indexedReference = reference.startsWith("#");
        String ref = indexedReference ? reference.substring(1) : reference;

        if (indexedReference) {
            try {
                int index = Integer.parseInt(ref);
                if (index >= 0 && index < document.getTextures().size()) {
                    return document.getTextures().get(index);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Texture byUuid = document.getTextures().stream()
            .filter(t -> ref.equals(t.getUuid()))
            .findFirst()
            .orElse(null);
        if (byUuid != null) {
            return byUuid;
        }

        try {
            int numericRef = Integer.parseInt(ref);

            if (!indexedReference && numericRef >= 0 && numericRef < document.getTextures().size()) {
                return document.getTextures().get(numericRef);
            }

            Texture byId = document.getTextures().stream()
                .filter(t -> t.getId() != null && t.getId() == numericRef)
                .findFirst()
                .orElse(null);
            if (byId != null) {
                return byId;
            }
        } catch (NumberFormatException ignored) {
        }

        return document.getTextures().stream()
            .filter(t -> ref.equals(t.getName()))
            .findFirst()
            .orElse(null);
    }
    
    public Texture getTextureByName(String name) {
        if (name == null || document.getTextures() == null) {
            return null;
        }
        return document.getTextures().stream()
            .filter(t -> name.equals(t.getName()))
            .findFirst()
            .orElse(null);
    }
    
    public List<Texture> getTexturesByFolder(String folder) {
        if (folder == null || document.getTextures() == null) {
            return Collections.emptyList();
        }
        return document.getTextures().stream()
            .filter(t -> folder.equals(t.getFolder()))
            .collect(Collectors.toList());
    }
    
    public Set<String> getAllFolders() {
        if (document.getTextures() == null) {
            return Collections.emptySet();
        }
        return document.getTextures().stream()
            .map(Texture::getFolder)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    public List<Texture> getEmbeddedTextures() {
        if (document.getTextures() == null) {
            return Collections.emptyList();
        }
        return document.getTextures().stream()
            .filter(Texture::isEmbedded)
            .collect(Collectors.toList());
    }
    
    public List<Texture> getExternalTextures() {
        if (document.getTextures() == null) {
            return Collections.emptyList();
        }
        return document.getTextures().stream()
            .filter(t -> !t.isEmbedded())
            .filter(Texture::hasFileReference)
            .collect(Collectors.toList());
    }
    
    public BufferedImage loadTextureImage(Texture texture) {
        try {
            InputStream stream = loader.loadTexture(texture);
            if (stream != null) {
                try (stream) {
                    return ImageIO.read(stream);
                }
            }
        } catch (Exception e) {
            // Return null on error
        }
        return null;
    }
    
    public BufferedImage loadTextureImageByUuid(String uuid) {
        Texture texture = getTextureByUuid(uuid);
        return texture != null ? loadTextureImage(texture) : null;
    }
    
    public BufferedImage loadTextureImageByName(String name) {
        Texture texture = getTextureByName(name);
        return texture != null ? loadTextureImage(texture) : null;
    }
    
    public byte[] loadTextureData(Texture texture) {
        if (texture.getSource() != null && DataUrl.isDataUrl(texture.getSource())) {
            return DataUrlDecoder.decodeToBytes(texture.getSource());
        }
        
        try {
            InputStream stream = loader.loadTexture(texture);
            if (stream != null) {
                try (stream) {
                    return stream.readAllBytes();
                }
            }
        } catch (Exception e) {
            // Return null on error
        }
        return null;
    }
    
    public Map<String, BufferedImage> loadAllTextureImages() {
        Map<String, BufferedImage> result = new HashMap<>();
        for (Texture texture : getAllTextures()) {
            BufferedImage image = loadTextureImage(texture);
            if (image != null) {
                result.put(texture.getUuid(), image);
                if (texture.getName() != null) {
                    result.put(texture.getName(), image);
                }
            }
        }
        return result;
    }
    
    public boolean textureExists(Texture texture) {
        return loader.textureExists(texture);
    }
    
    public String resolveTexturePath(Texture texture) {
        return loader.resolveTexturePath(texture);
    }
    
    public List<String> getTextureUuidsUsedByElements() {
        if (document.getElements() == null) {
            return Collections.emptyList();
        }
        
        Set<String> usedUuids = new HashSet<>();
        for (var element : document.getElements()) {
            if (element.getFaces() != null) {
                for (var face : element.getFaces().values()) {
                    String texture = face.getTexture();
                    if (texture != null) {
                        usedUuids.add(texture);
                    }
                }
            }
        }
        return new ArrayList<>(usedUuids);
    }
    
    public List<Texture> getUsedTextures() {
        List<String> usedUuids = getTextureUuidsUsedByElements();
        return getAllTextures().stream()
            .filter(t -> usedUuids.contains(t.getUuid()))
            .collect(Collectors.toList());
    }
    
    public List<Texture> getUnusedTextures() {
        List<String> usedUuids = getTextureUuidsUsedByElements();
        return getAllTextures().stream()
            .filter(t -> !usedUuids.contains(t.getUuid()))
            .collect(Collectors.toList());
    }
    
    public boolean hasTransparency(Texture texture) {
        return getAlphaMode(texture) != AlphaMode.OPAQUE;
    }
    
    public boolean hasTransparencyByUuid(String uuid) {
        Texture texture = getTextureByReference(uuid);
        return texture != null && hasTransparency(texture);
    }

    public boolean hasTransparencyByReference(String reference) {
        Texture texture = getTextureByReference(reference);
        return texture != null && hasTransparency(texture);
    }

    public AlphaMode getAlphaModeByReference(String reference) {
        Texture texture = getTextureByReference(reference);
        return texture != null ? getAlphaMode(texture) : AlphaMode.OPAQUE;
    }
    
    public boolean hasTransparencyByName(String name) {
        Texture texture = getTextureByName(name);
        return texture != null && hasTransparency(texture);
    }

    public AlphaMode getAlphaMode(Texture texture) {
        String cacheKey = buildTransparencyCacheKey(texture);
        if (cacheKey != null) {
            AlphaMode cached = ALPHA_MODE_CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        BufferedImage image = loadTextureImage(texture);
        if (image == null) {
            return AlphaMode.OPAQUE;
        }

        boolean hasZeroAlpha = false;
        boolean hasPartialAlpha = false;
        for (int y = 0; y < image.getHeight() && !hasPartialAlpha; y++) {
            for (int x = 0; x < image.getWidth() && !hasPartialAlpha; x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                if (alpha == 0) {
                    hasZeroAlpha = true;
                } else if (alpha < 255) {
                    hasPartialAlpha = true;
                }
            }
        }

        AlphaMode mode;
        if (hasPartialAlpha) {
            mode = AlphaMode.TRANSLUCENT;
        } else if (hasZeroAlpha) {
            mode = AlphaMode.CUTOUT;
        } else {
            mode = AlphaMode.OPAQUE;
        }

        if (cacheKey != null) {
            ALPHA_MODE_CACHE.put(cacheKey, mode);
        }
        return mode;
    }

    private String buildTransparencyCacheKey(Texture texture) {
        if (texture == null) {
            return null;
        }

        if (texture.getUuid() != null) {
            return "uuid:" + texture.getUuid();
        }

        if (texture.getPath() != null) {
            return "path:" + texture.getPath();
        }

        if (texture.getRelativePath() != null) {
            return "relative:" + texture.getRelativePath();
        }

        if (texture.getSource() != null) {
            return "source:" + texture.getSource().hashCode();
        }

        if (texture.getName() != null) {
            return "name:" + texture.getName();
        }

        return null;
    }
}
