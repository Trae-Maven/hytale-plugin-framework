package io.github.trae.hytale.framework.helper.abstracts.interfaces;

public interface IAbstractHelper<Type> {

    void register(final Type type);

    void unregister(final Type type);
}