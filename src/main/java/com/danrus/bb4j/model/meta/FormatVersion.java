package com.danrus.bb4j.model.meta;

import java.util.Arrays;
import java.util.Objects;

public class FormatVersion implements Comparable<FormatVersion> {
    private final int[] version;
    private final int[] beta;
    private final String raw;

    public FormatVersion(String versionString) {
        this.raw = versionString;
        String[] parts = versionString.split("-beta\\.", 2);
        
        this.version = Arrays.stream(parts[0].split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
        
        if (parts.length > 1) {
            this.beta = Arrays.stream(parts[1].split("\\."))
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } else {
            this.beta = null;
        }
    }

    public int[] getVersion() {
        return version;
    }

    public int[] getBeta() {
        return beta;
    }

    public String getRaw() {
        return raw;
    }

    public boolean hasBeta() {
        return beta != null;
    }

    @Override
    public int compareTo(FormatVersion other) {
        int maxLength = Math.max(this.version.length, other.version.length);
        
        for (int i = 0; i < maxLength; i++) {
            int thisPart = i < this.version.length ? this.version[i] : 0;
            int otherPart = i < other.version.length ? other.version[i] : 0;
            
            if (thisPart > otherPart) return 1;
            if (thisPart < otherPart) return -1;
        }

        if (this.beta != null && other.beta == null) return 1;
        if (this.beta == null && other.beta != null) return -1;
        
        if (this.beta != null && other.beta != null) {
            maxLength = Math.max(this.beta.length, other.beta.length);
            for (int i = 0; i < maxLength; i++) {
                int thisPart = i < this.beta.length ? this.beta[i] : 0;
                int otherPart = i < other.beta.length ? other.beta[i] : 0;
                
                if (thisPart > otherPart) return 1;
                if (thisPart < otherPart) return -1;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormatVersion that = (FormatVersion) o;
        return compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    @Override
    public String toString() {
        return raw;
    }
}
