package com.dervarex.minified.events;

import org.apiguardian.api.API;

/**
 * An Event that goes the other way round, your launcher posts it and minified's backend listens
 * <br>
 * Normal events tell you that something happened (like {@code GameStoppedEvent}, which fires after the game exited),
 * reverse events ask the backend to do something (like {@code StopGameEvent}, this one wants the backend to stop the Game)
 * <pre>{@code
 * eventBus.post(new StopGameEvent());
 * }</pre>
 * Post it on the same {@link EventBus} you gave the backend, posting while nothing is listening does nothing
 */
@API(status = API.Status.EXPERIMENTAL)
public interface ReverseEvent extends Event {
}
