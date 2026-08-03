package com.dervarex.minified.launch.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.events.EventBus;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class LaunchContext {

    private final User user;
    private final LaunchConfiguration launchConfiguration;
    private final EventBus eventBus;

    @Setter
    private boolean online;

    public LaunchContext(User user, LaunchConfiguration launchConfiguration) {
        this.user = user;
        this.launchConfiguration = launchConfiguration;
        this.eventBus = launchConfiguration.getEventBus();
    }

}