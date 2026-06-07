package io.github.trae.hytale.framework.utility;

import io.github.trae.utilities.UtilJava;
import lombok.experimental.UtilityClass;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector4d;
import org.joml.Vector4i;

import java.util.LinkedHashMap;

/**
 * Utility class providing serialization and deserialization helpers for JOML vector types.
 * <p>
 * Each vector is represented as a {@link LinkedHashMap} keyed by axis component
 * ({@code X}, {@code Y}, {@code Z}, {@code W}), preserving insertion order to maintain
 * a stable component layout. Integer vectors store their components as {@link Integer}
 * values, while double vectors store them as {@link Double} values.
 * <p>
 * All methods are null-safe: serialization returns {@code null} for {@code null} input,
 * and deserialization returns {@code null} for {@code null} or empty maps.
 */
@UtilityClass
public class UtilVector {

    /**
     * Serializes a 2-component integer vector into an ordered map.
     *
     * @param vector2i the vector to serialize, may be {@code null}
     * @return a map containing the {@code X} and {@code Y} components, or {@code null} if the input is {@code null}
     */
    public static LinkedHashMap<String, Object> serializeVector2i(final Vector2i vector2i) {
        if (vector2i == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("X", vector2i.x());
            map.put("Y", vector2i.y());
        });
    }

    /**
     * Deserializes a 2-component integer vector from an ordered map.
     *
     * @param serializedMap the map containing the {@code X} and {@code Y} components, may be {@code null} or empty
     * @return the reconstructed vector, or {@code null} if the map is {@code null} or empty
     */
    public static Vector2i deserializeVector2i(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final Integer x = UtilJava.cast(Integer.class, serializedMap.get("X"));
        final Integer y = UtilJava.cast(Integer.class, serializedMap.get("Y"));

        return new Vector2i(x, y);
    }

    /**
     * Serializes a 3-component integer vector into an ordered map.
     *
     * @param vector3i the vector to serialize, may be {@code null}
     * @return a map containing the {@code X}, {@code Y} and {@code Z} components, or {@code null} if the input is {@code null}
     */
    public static LinkedHashMap<String, Object> serializeVector3i(final Vector3i vector3i) {
        if (vector3i == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("X", vector3i.x());
            map.put("Y", vector3i.y());
            map.put("Z", vector3i.z());
        });
    }

    /**
     * Deserializes a 3-component integer vector from an ordered map.
     *
     * @param serializedMap the map containing the {@code X}, {@code Y} and {@code Z} components, may be {@code null} or empty
     * @return the reconstructed vector, or {@code null} if the map is {@code null} or empty
     */
    public static Vector3i deserializeVector3i(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final Integer x = UtilJava.cast(Integer.class, serializedMap.get("X"));
        final Integer y = UtilJava.cast(Integer.class, serializedMap.get("Y"));
        final Integer z = UtilJava.cast(Integer.class, serializedMap.get("Z"));

        return new Vector3i(x, y, z);
    }

    /**
     * Serializes a 4-component integer vector into an ordered map.
     *
     * @param vector4i the vector to serialize, may be {@code null}
     * @return a map containing the {@code X}, {@code Y}, {@code Z} and {@code W} components, or {@code null} if the input is {@code null}
     */
    public static LinkedHashMap<String, Object> serializeVector4i(final Vector4i vector4i) {
        if (vector4i == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("X", vector4i.x());
            map.put("Y", vector4i.y());
            map.put("Z", vector4i.z());
            map.put("W", vector4i.w());
        });
    }

    /**
     * Deserializes a 4-component integer vector from an ordered map.
     *
     * @param serializedMap the map containing the {@code X}, {@code Y}, {@code Z} and {@code W} components, may be {@code null} or empty
     * @return the reconstructed vector, or {@code null} if the map is {@code null} or empty
     */
    public static Vector4i deserializeVector4i(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final Integer x = UtilJava.cast(Integer.class, serializedMap.get("X"));
        final Integer y = UtilJava.cast(Integer.class, serializedMap.get("Y"));
        final Integer z = UtilJava.cast(Integer.class, serializedMap.get("Z"));
        final Integer w = UtilJava.cast(Integer.class, serializedMap.get("W"));

        return new Vector4i(x, y, z, w);
    }

    /**
     * Serializes a 2-component double vector into an ordered map.
     *
     * @param vector2d the vector to serialize, may be {@code null}
     * @return a map containing the {@code X} and {@code Y} components, or {@code null} if the input is {@code null}
     */
    public static LinkedHashMap<String, Object> serializeVector2d(final Vector2d vector2d) {
        if (vector2d == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("X", vector2d.x());
            map.put("Y", vector2d.y());
        });
    }

    /**
     * Deserializes a 2-component double vector from an ordered map.
     *
     * @param serializedMap the map containing the {@code X} and {@code Y} components, may be {@code null} or empty
     * @return the reconstructed vector, or {@code null} if the map is {@code null} or empty
     */
    public static Vector2d deserializeVector2d(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final Double x = UtilJava.cast(Double.class, serializedMap.get("X"));
        final Double y = UtilJava.cast(Double.class, serializedMap.get("Y"));

        return new Vector2d(x, y);
    }

    /**
     * Serializes a 3-component double vector into an ordered map.
     *
     * @param vector3d the vector to serialize, may be {@code null}
     * @return a map containing the {@code X}, {@code Y} and {@code Z} components, or {@code null} if the input is {@code null}
     */
    public static LinkedHashMap<String, Object> serializeVector3d(final Vector3d vector3d) {
        if (vector3d == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("X", vector3d.x());
            map.put("Y", vector3d.y());
            map.put("Z", vector3d.z());
        });
    }

    /**
     * Deserializes a 3-component double vector from an ordered map.
     *
     * @param serializedMap the map containing the {@code X}, {@code Y} and {@code Z} components, may be {@code null} or empty
     * @return the reconstructed vector, or {@code null} if the map is {@code null} or empty
     */
    public static Vector3d deserializeVector3d(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final Double x = UtilJava.cast(Double.class, serializedMap.get("X"));
        final Double y = UtilJava.cast(Double.class, serializedMap.get("Y"));
        final Double z = UtilJava.cast(Double.class, serializedMap.get("Z"));

        return new Vector3d(x, y, z);
    }

    /**
     * Serializes a 4-component double vector into an ordered map.
     *
     * @param vector4d the vector to serialize, may be {@code null}
     * @return a map containing the {@code X}, {@code Y}, {@code Z} and {@code W} components, or {@code null} if the input is {@code null}
     */
    public static LinkedHashMap<String, Object> serializeVector4d(final Vector4d vector4d) {
        if (vector4d == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("X", vector4d.x());
            map.put("Y", vector4d.y());
            map.put("Z", vector4d.z());
            map.put("W", vector4d.w());
        });
    }

    /**
     * Deserializes a 4-component double vector from an ordered map.
     *
     * @param serializedMap the map containing the {@code X}, {@code Y}, {@code Z} and {@code W} components, may be {@code null} or empty
     * @return the reconstructed vector, or {@code null} if the map is {@code null} or empty
     */
    public static Vector4d deserializeVector4d(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final Double x = UtilJava.cast(Double.class, serializedMap.get("X"));
        final Double y = UtilJava.cast(Double.class, serializedMap.get("Y"));
        final Double z = UtilJava.cast(Double.class, serializedMap.get("Z"));
        final Double w = UtilJava.cast(Double.class, serializedMap.get("W"));

        return new Vector4d(x, y, z, w);
    }
}