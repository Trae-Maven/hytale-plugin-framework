package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import io.github.trae.utilities.UtilCollection;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Hytale-specific search utility that delegates to {@link UtilCollection#search}
 * with message output routed through {@link UtilMessage}.
 */
@UtilityClass
public class UtilSearch {

    /**
     * Search a collection for a matching element, sending any result or
     * ambiguity messages to the given {@link IMessageReceiver}.
     *
     * @param collection        the collection to search
     * @param equalsPredicate   predicate for exact matching, or {@code null} to skip
     * @param containsPredicate predicate for partial matching, or {@code null} to skip
     * @param listConsumer      receives the list of partial matches, or {@code null} to skip
     * @param colorFunction     applies color formatting to highlighted segments of the result message
     * @param resultFunction    maps each matched element to its display name for the result message
     * @param prefix            the message prefix passed to {@link UtilMessage#message}
     * @param messageReceiver   the receiver to send result messages to
     * @param input             the raw input string shown in the "no matches" message
     * @param inform            whether to send a result message to the receiver
     * @param <Type>            the element type
     * @return the matched element, or {@link Optional#empty()} if zero or multiple matches were found
     */
    public static <Type> Optional<Type> search(final Collection<? extends Type> collection, final Predicate<Type> equalsPredicate, final Predicate<Type> containsPredicate, final Consumer<List<Type>> listConsumer, final Function<String, String> colorFunction, final Function<Type, String> resultFunction, final String prefix, final IMessageReceiver messageReceiver, final String input, final boolean inform) {
        final Consumer<String> messageConsumer = message -> UtilMessage.message(messageReceiver, prefix, message);

        return UtilCollection.search(collection, equalsPredicate, containsPredicate, listConsumer, messageConsumer, colorFunction, resultFunction, input, inform);
    }
}