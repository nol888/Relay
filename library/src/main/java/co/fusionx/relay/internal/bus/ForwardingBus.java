package co.fusionx.relay.internal.bus;

import com.fusionx.bus.Bus;

import co.fusionx.relay.bus.GenericBus;

public class ForwardingBus<T> implements PostableBus<T> {

    private final Bus mBus;

    private final PostableBus<? super T> mForwardBus;

    public ForwardingBus(final Bus bus, final PostableBus<? super T> forwardBus) {
        mBus = bus;
        mForwardBus = forwardBus;
    }

    @Override
    public void registerForEvents(final Object object) {
        mBus.register(object);
    }

    @Override
    public void registerForEvents(final Object object, final int priority) {
        mBus.register(object, priority);
    }

    @Override
    public void unregisterFromEvents(final Object object) {
        mBus.unregister(object);
    }

    @Override
    public void postEvent(final T event) {
        mBus.post(event);
        mForwardBus.postEvent(event);
    }
}
