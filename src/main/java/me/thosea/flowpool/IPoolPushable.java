package me.thosea.flowpool;

public interface IPoolPushable<T> {
	void push(HandlerPool pool, T obj);

	/**
	 * Removes the {@link HandlerPool}'s push from this pushable,
	 * does nothing if the HandlerPool hasn't pushed this.
	 * @param pool HandlerPool
	 * @return true if the push was removed, false if not pushed by the pool
	 */
	boolean pop(HandlerPool pool);

	/**
	 * @return amount of times this pushable was pushed
	 */
	int getPushCount();

	/**
	 * @return true if {@link #getPushCount()} > 0
	 */
	default boolean isPushed() {
		return getPushCount() != 0;
	}
}