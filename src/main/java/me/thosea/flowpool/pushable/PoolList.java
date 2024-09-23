package me.thosea.flowpool.pushable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.PoolEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * First-in, last-iterated pushable managed by {@link HandlerPool}s.
 * @param <T> type
 */
@Setter
@Getter @Accessors(fluent = true, chain = true)
public class PoolList<T> extends AbstractPoolCollection<T> {
	private final List<PoolEntry<T>> list = new ArrayList<>();
	private BiConsumer<PoolList<T>, PoolEntry<T>> pushCallback;
	private BiConsumer<PoolList<T>, PoolEntry<T>> popCallback;

	/**
	 * Pushes the object to the back of the list.
	 * It will be the last objected iterated over until another object is pushed.
	 * @param pool HandlerPool
	 * @param obj object to push
	 */
	@Override
	public void push(HandlerPool pool, T obj) {
		doPush(pool, obj, false);
	}

	/**
	 * Pushes the object to the front of the list.
	 * It will be the first objected iterated over.
	 * @param pool HandlerPool
	 * @param obj object to push
	 */
	public void pushFirst(HandlerPool pool, T obj) {
		doPush(pool, obj, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PoolEntry<T>> getCollection() {
		return list;
	}

	@Override
	protected void doAdd(PoolEntry<T> entry, boolean reverse) {
		if(reverse) { // pushFirst
			list.add(0, entry);
		} else { // push
			list.add(entry);
		}
	}

	@Override
	protected void onPush(PoolEntry<T> entry) {
		if(pushCallback != null) {
			pushCallback.accept(this, entry);
		}
	}

	@Override
	protected void onPop(PoolEntry<T> entry) {
		if(popCallback != null) {
			popCallback.accept(this, entry);
		}
	}
}