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
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class TextureUtils {
    
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
        if (uuid == null || document.getTextures() == null) {
            return null;
        }
        return document.getTextures().stream()
            .filter(t -> uuid.equals(t.getUuid()))
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
}
