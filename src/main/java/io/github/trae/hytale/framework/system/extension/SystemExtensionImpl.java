package io.github.trae.hytale.framework.system.extension;

import java.util.Map;

public interface SystemExtensionImpl {

    Map<String, SystemExtension<?>> getExtensions();
}