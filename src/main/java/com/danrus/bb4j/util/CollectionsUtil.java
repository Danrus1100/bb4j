package com.danrus.bb4j.util;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionsUtil {
    
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
    
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
    
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }
    
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }
    
    public static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
    
    public static <T> Set<T> nullToEmpty(Set<T> set) {
        return set == null ? Collections.emptySet() : set;
    }
    
    public static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
        return map == null ? Collections.emptyMap() : map;
    }
    
    public static <T> List<T> emptyToNull(List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }
    
    public static <T> Set<T> emptyToNull(Set<T> set) {
        return set == null || set.isEmpty() ? null : set;
    }
    
    public static <K, V> Map<K, V> emptyToNull(Map<K, V> map) {
        return map == null || map.isEmpty() ? null : map;
    }
    
    public static <T> List<T> safe(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
    
    public static <T> Set<T> safe(Set<T> set) {
        return set == null ? new HashSet<>() : set;
    }
    
    public static <K, V> Map<K, V> safe(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }
    
    public static <T> List<T> filter(List<T> list, java.util.function.Predicate<T> predicate) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
    
    public static <T, R> List<R> map(List<T> list, java.util.function.Function<T, R> mapper) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(mapper).collect(Collectors.toList());
    }
    
    public static <T> List<T> union(Collection<T> a, Collection<T> b) {
        Set<T> result = new HashSet<>();
        if (a != null) result.addAll(a);
        if (b != null) result.addAll(b);
        return new ArrayList<>(result);
    }
    
    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        if (a == null || b == null) {
            return Collections.emptyList();
        }
        Set<T> setB = new HashSet<>(b);
        return a.stream().filter(setB::contains).collect(Collectors.toList());
    }
    
    public static <T> List<T> difference(Collection<T> a, Collection<T> b) {
        if (a == null) {
            return Collections.emptyList();
        }
        if (b == null) {
            return new ArrayList<>(a);
        }
        Set<T> setB = new HashSet<>(b);
        return a.stream().filter(e -> !setB.contains(e)).collect(Collectors.toList());
    }
}
