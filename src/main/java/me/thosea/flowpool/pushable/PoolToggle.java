package me.thosea.flowpool.pushable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.IPoolPushable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Only holds a Set of the {@link HandlerPool}s that pushed it.<br>
 * Use to manage toggles or counts, like if an entity is invulnerable or something.
 */
@Accessors(fluent = true)
@Getter
@Setter
public class PoolToggle implements IPoolPushable<Void> {
	private final Set<HandlerPool> pushedBy = new HashSet<>();
	private BiConsumer<PoolToggle, HandlerPool> pushCallback;
	private BiConsumer<PoolToggle, HandlerPool> popCallback;

	/**
	 * The passed object will be ignored.
	 * Use {@link #push(HandlerPool)} instead.
	 * @param pool HandlerPool
	 * @param obj object to push
	 * @deprecated use {@link #push(HandlerPool)} instead
	 */
	@Override
	@Deprecated
	public void push(HandlerPool pool, Void obj) {
		this.push(pool);
	}

	/**
	 * Marks this toggle as pushed by the {@link HandlerPool}.<br>
	 * {@link #isPushed()} and {@link HandlerPool#isPushing(IPoolPushable)} will return true,
	 * and if the pool didn't push this already, {@link #getPushCount()} will increase.
	 * @param pool HandlerPool
	 */
	public void push(HandlerPool pool) {
		if(pushedBy.add(pool)) {
			pool.getPushedStacks().add(this);

			if(pushCallback != null) {
				pushCallback.accept(this, pool);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean pop(HandlerPool pool) {
		if(!pushedBy.remove(pool))
			return false;

		pool.getPushedStacks().remove(this);
		if(popCallback != null) {
			popCallback.accept(this, pool);
		}

		return true;
	}

	/**
	 * @return the amount of pools that pushed this
	 */
	@Override
	public int getPushCount() {
		return pushedBy.size();
	}
}