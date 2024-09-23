package me.thosea.flowpool.pushable;

import lombok.Getter;
import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.IPoolPushable;
import me.thosea.flowpool.PoolEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Provides a base for collection-based {@link IPoolPushable}s.
 * @param <T> collection type
 */
@Getter
public abstract class AbstractPoolCollection<T> implements IPoolPushable<T> {
	// abstract to force superclasses to write docs
	@Override
	public abstract void push(HandlerPool pool, T obj);

	protected void doPush(HandlerPool pool, T obj, boolean reverse) {
		if(pool.isPushing(this)) {
			this.pop(pool);
		}

		PoolEntry<T> entry = new PoolEntry<>(pool, obj);
		this.doAdd(entry, reverse);
		pool.getPushedStacks().add(this);

		this.onPush(entry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean pop(HandlerPool pool) {
		return popAndGet(pool) != null;
	}

	/**
	 * Removes the {@link HandlerPool}'s push from this pushable,
	 * does nothing if the HandlerPool hasn't pushed this.
	 * @param pool HandlerPool
	 * @return removed {@link PoolEntry} or null if not pushed by pool
	 */
	@Nullable
	public PoolEntry<T> popAndGet(HandlerPool pool) {
		var iterator = this.getCollection().iterator();

		while(iterator.hasNext()) {
			PoolEntry<T> entry = iterator.next();
			if(entry.pool() == pool) {
				iterator.remove();
				pool.getPushedStacks().remove(this);
				this.onPop(entry);
				return entry;
			}
		}

		return null;
	}

	/**
	 * Returns the PoolEntry pushed onto the stack by the HandlerPool.
	 * @param pool HandlerPool
	 * @return {@link PoolEntry} if pushed by it, null otherwise
	 */
	@Nullable
	public PoolEntry<T> getPushEntry(HandlerPool pool) {
		if(pool != null) {
			for(PoolEntry<T> entry : this.getCollection()) {
				if(entry.pool() == pool) {
					return entry;
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPushCount() {
		return getCollection().size();
	}

	/**
	 * Gets the collection.
	 * If you are editing this, make sure to add/remove from {@link HandlerPool#getPushedStacks()}!
	 * @return collection, can be read/written to
	 */
	public abstract Collection<PoolEntry<T>> getCollection();

	/**
	 * Gets the entries in the collection. Be careful using this,
	 * as it streams and filters {@link #getCollection()}, which may
	 * create overhead.
	 * @return entries in the collection
	 * @see #getCollection()
	 */
	public List<T> getEntries() {
		return getCollection().stream().map(PoolEntry::obj).toList();
	}

	protected abstract void doAdd(PoolEntry<T> entry, boolean reverse);

	protected abstract void onPush(PoolEntry<T> entry);
	protected abstract void onPop(PoolEntry<T> entry);
}