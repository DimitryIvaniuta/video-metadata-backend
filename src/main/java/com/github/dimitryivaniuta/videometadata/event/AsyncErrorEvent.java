package com.github.dimitryivaniuta.videometadata.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Fired whenever an @Async method throws an uncaught exception.
 */
@Getter
public class AsyncErrorEvent extends ApplicationEvent {

    private final Throwable throwable;
    private final Method    method;
    private final Object[]  args;

    public AsyncErrorEvent(Object source,
                           Throwable throwable,
                           Method method,
                           Object[] args) {
        super(source);
        this.throwable = throwable;
        this.method    = method;
        // defensive copy
        this.args      = args != null ? Arrays.copyOf(args, args.length) : new Object[0];
    }

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }
}
