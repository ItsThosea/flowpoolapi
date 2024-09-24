package me.thosea.flowpool.pushable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.PoolEntry;

import java.util.ArrayDeque;
import java.util.function.BiConsumer;

/**
 * First-in, first-iterated pushable managed by {@link HandlerPool}s.
 * @param <T> type
 */
@Setter
@Getter @Accessors(fluent = true, chain = true)
public class PoolStack<T> extends AbstractPoolCollection<T> {
	private final ArrayDeque<PoolEntry<T>> stack;
	private BiConsumer<PoolStack<T>, PoolEntry<T>> pushCallback;
	private BiConsumer<PoolStack<T>, PoolEntry<T>> popCallback;

	/**
	 * Constructs a new PoolStack with an initial capacity of {@link AbstractPoolCollection#DEFAULT_COLLECTION_SIZE}.
	 */
	public PoolStack() {
		this.stack = new ArrayDeque<>(DEFAULT_COLLECTION_SIZE);
	}

	/**
	 * Constructs a new PoolStack with the specified initial capacity.
	 * @param initialCapacity initial capacity
	 */
	public PoolStack(int initialCapacity) {
		this.stack = new ArrayDeque<>(initialCapacity);
	}

	/**
	 * Gets the object that was last {@code push}ed onto this pushable,
	 * or null if there is none.
	 * @return {@code pushable.peek().obj()}
	 */
	public T peek() {
		PoolEntry<T> entry = this.stack.peek();
		return entry == null ? null : entry.obj();
	}

	/**
	 * Pushes the object to the front of the pushable.
	 * It will be the object returned by {@link #peek()} until another object is pushed.
	 * @param pool HandlerPool
	 * @param obj object to push
	 */
	@Override
	public void push(HandlerPool pool, T obj) {
		doPush(pool, obj, false);
	}

	/**
	 * Pushes the object to the back of the pushable.
	 * @param pool HandlerPool
	 * @param obj object to push
	 */
	public void pushLast(HandlerPool pool, T obj) {
		doPush(pool, obj, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayDeque<PoolEntry<T>> getCollection() {
		return stack;
	}

	@Override
	protected void doAdd(PoolEntry<T> entry, boolean reverse) {
		if(reverse) { // pushLast
			stack.addLast(entry);
		} else { // push
			stack.addFirst(entry);
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