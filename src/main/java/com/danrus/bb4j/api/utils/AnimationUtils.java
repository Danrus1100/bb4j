package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.*;

import java.util.*;
import java.util.stream.Collectors;

public class AnimationUtils {
    
    private final BbModelDocument document;
    
    private AnimationUtils(BbModelDocument document) {
        this.document = document;
    }
    
    public static AnimationUtils forDocument(BbModelDocument document) {
        return new AnimationUtils(document);
    }
    
    public List<Animation> getAllAnimations() {
        return document.getAnimations() != null 
            ? new ArrayList<>(document.getAnimations()) 
            : Collections.emptyList();
    }
    
    public Animation getAnimationByUuid(String uuid) {
        if (uuid == null || document.getAnimations() == null) {
            return null;
        }
        return document.getAnimations().stream()
            .filter(a -> uuid.equals(a.getUuid()))
            .findFirst()
            .orElse(null);
    }
    
    public Animation getAnimationByName(String name) {
        if (name == null || document.getAnimations() == null) {
            return null;
        }
        return document.getAnimations().stream()
            .filter(a -> name.equals(a.getName()))
            .findFirst()
            .orElse(null);
    }
    
    public List<Animation> getAnimationsForElement(String elementUuid) {
        if (document.getAnimations() == null) {
            return Collections.emptyList();
        }
        
        return document.getAnimations().stream()
            .filter(anim -> hasAnimatorForElement(anim, elementUuid))
            .collect(Collectors.toList());
    }
    
    public List<Animation> getAnimationsForGroup(String groupUuid) {
        if (document.getAnimations() == null) {
            return Collections.emptyList();
        }
        
        return document.getAnimations().stream()
            .filter(anim -> hasAnimatorForGroup(anim, groupUuid))
            .collect(Collectors.toList());
    }
    
    private boolean hasAnimatorForElement(Animation animation, String elementUuid) {
        if (animation.getAnimators() == null) {
            return false;
        }
        return animation.getAnimators().containsKey(elementUuid);
    }
    
    private boolean hasAnimatorForGroup(Animation animation, String groupUuid) {
        if (animation.getAnimators() == null) {
            return false;
        }
        return animation.getAnimators().containsKey(groupUuid);
    }
    
    public Map<String, Animator> getAnimatorsForElement(String elementUuid, Animation animation) {
        if (animation == null || animation.getAnimators() == null) {
            return Collections.emptyMap();
        }
        return animation.getAnimators().entrySet().stream()
            .filter(e -> elementUuid.equals(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    public Set<String> getAnimatedElementUuids() {
        Set<String> uuids = new HashSet<>();
        
        if (document.getAnimations() == null) {
            return uuids;
        }
        
        for (Animation animation : document.getAnimations()) {
            if (animation.getAnimators() != null) {
                uuids.addAll(animation.getAnimators().keySet());
            }
        }
        
        return uuids;
    }
    
    public Map<String, List<Keyframe>> getKeyframesAtTime(Animation animation, double time) {
        Map<String, List<Keyframe>> result = new HashMap<>();
        
        if (animation.getAnimators() == null) {
            return result;
        }
        
        for (Map.Entry<String, Animator> entry : animation.getAnimators().entrySet()) {
            Animator animator = entry.getValue();
            if (animator.getKeyframes() != null) {
                List<Keyframe> keyframesAtTime = animator.getKeyframes().stream()
                    .filter(kf -> kf.getTime() != null && Math.abs(kf.getTime() - time) < 0.001)
                    .collect(Collectors.toList());
                
                if (!keyframesAtTime.isEmpty()) {
                    result.put(entry.getKey(), keyframesAtTime);
                }
            }
        }
        
        return result;
    }
    
    public double getAnimationDuration(Animation animation) {
        if (animation == null) {
            return 0;
        }
        
        if (animation.getLength() != null) {
            return animation.getLength();
        }
        
        if (animation.getStartTime() != null && animation.getEndTime() != null) {
            return animation.getEndTime() - animation.getStartTime();
        }
        
        double maxTime = 0;
        if (animation.getAnimators() != null) {
            for (Animator animator : animation.getAnimators().values()) {
                if (animator.getKeyframes() != null) {
                    for (Keyframe kf : animator.getKeyframes()) {
                        if (kf.getTime() != null && kf.getTime() > maxTime) {
                            maxTime = kf.getTime();
                        }
                    }
                }
            }
        }
        
        return maxTime;
    }
    
    public double getTotalAnimationDuration() {
        return getAllAnimations().stream()
            .mapToDouble(this::getAnimationDuration)
            .max()
            .orElse(0);
    }
    
    public boolean isLooping(Animation animation) {
        return animation != null && animation.getLoop() != null && animation.getLoop() == 1.0;
    }
    
    public List<Animation> getLoopingAnimations() {
        if (document.getAnimations() == null) {
            return Collections.emptyList();
        }
        return document.getAnimations().stream()
            .filter(this::isLooping)
            .collect(Collectors.toList());
    }
    
    public Set<String> getAnimatedChannels(Animation animation) {
        Set<String> channels = new HashSet<>();
        
        if (animation.getAnimators() == null) {
            return channels;
        }
        
        for (Animator animator : animation.getAnimators().values()) {
            if (animator.getKeyframes() != null) {
                for (Keyframe kf : animator.getKeyframes()) {
                    if (kf.getChannel() != null) {
                        channels.add(kf.getChannel());
                    }
                }
            }
        }
        
        return channels;
    }
    
    public List<Animation> getAnimationsWithPosition() {
        return getAllAnimations().stream()
            .filter(a -> getAnimatedChannels(a).contains("position"))
            .collect(Collectors.toList());
    }
    
    public List<Animation> getAnimationsWithRotation() {
        return getAllAnimations().stream()
            .filter(a -> getAnimatedChannels(a).contains("rotation"))
            .collect(Collectors.toList());
    }
    
    public List<Animation> getAnimationsWithScale() {
        return getAllAnimations().stream()
            .filter(a -> getAnimatedChannels(a).contains("scale"))
            .collect(Collectors.toList());
    }
}
