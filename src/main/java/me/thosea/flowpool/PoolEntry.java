package me.thosea.flowpool;

import lombok.NonNull;

/**
 * Entry in a {@link IPoolPushable}.
 * @param pool HandlerPool
 * @param obj object
 * @param <T> object type
 */
public record PoolEntry<T>(@NonNull HandlerPool pool, /*@Nullable*/ T obj) {
	/**
	 * Only checks if the other object has the same pool as this.
	 * The object is not checked!
	 * @param obj target object
	 * @return true if the object is a PoolEntry with the same type
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof PoolEntry<?> entry && entry.pool == this.pool;
	}

	/**
	 * @return hash of the HandlerPool, obj is not factored
	 */
	@Override
	public int hashCode() {
		return pool.hashCode();
	}

	/**
	 * Makes a PoolEntry with the specified pool and a null object.
	 * @param pool pool
	 * @param <T> object type
	 * @return PoolEntry with the pool and a null object
	 */
	public static <T> PoolEntry<T> createMarker(HandlerPool pool) {
		return new PoolEntry<>(pool, null);
	}
}