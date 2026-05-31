package io.github.trae.hytale.framework.system.extension;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class SystemExtension<ECS_TYPE> {

    private final SystemGroup<ECS_TYPE> group;
    private final Set<Dependency<ECS_TYPE>> dependencies;

    public SystemExtension(final SystemGroup<ECS_TYPE> group) {
        this(group, null);
    }

    public SystemExtension(final Set<Dependency<ECS_TYPE>> dependencies) {
        this(null, dependencies);
    }
}