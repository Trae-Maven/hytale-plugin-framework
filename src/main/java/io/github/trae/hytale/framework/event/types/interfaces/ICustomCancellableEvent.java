package io.github.trae.hytale.framework.event.types.interfaces;

import com.hypixel.hytale.event.ICancellable;

public interface ICustomCancellableEvent extends ICancellable {

    String getCancelledReason();

    void setCancelledWithReason(final String cancelledReason);
}