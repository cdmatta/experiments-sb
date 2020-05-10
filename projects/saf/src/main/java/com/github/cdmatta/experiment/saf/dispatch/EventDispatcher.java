package com.github.cdmatta.experiment.saf.dispatch;

import javax.annotation.Nullable;

public interface EventDispatcher {

    void dispatch(@Nullable String routingKey, String eventString);
}
