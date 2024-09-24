package me.thosea.flowpool.pushable;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.PoolEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * First-in, first-iterated pushable of handlers managed by {@link HandlerPool}s.<br>
 * It is a pipeline-a callback in the pushable can not call the next handler,
 * or change the parameters before calling {@link PipelineContext#pass()}.
 * @param <T> handler type - this should be an interface with a {@link PipelineContext} parameter,
 * basic ones are provided in {@code me.thosea.flowpool.pipeline} package
 * @param <R> return type, this should be returned by the handler type
 */
public class PoolPipeline<T, R> extends AbstractPoolCollection<T> {
	protected final List<PoolEntry<T>> list;

	@Accessors(fluent = true)
	@Getter @Setter
	private BiConsumer<PoolPipeline<T, R>, PoolEntry<T>> pushCallback;
	@Accessors(fluent = true)
	@Getter @Setter
	private BiConsumer<PoolPipeline<T, R>, PoolEntry<T>> popCallback;

	protected final Map<HandlerPool, Runnable> queuedModifications = new HashMap<>();

	protected PipelineContext context = new PipelineContext();
	/**
	 * Execution depth. If not executing, this will be zero.
	 */
	@Getter protected int depth = 0;

	/**
	 * Constructs a new PoolPipeline with an initial capacity of {@link AbstractPoolCollection#DEFAULT_COLLECTION_SIZE}.
	 */
	public PoolPipeline() {
		this.list = new ArrayList<>(DEFAULT_COLLECTION_SIZE);
	}

	/**
	 * Constructs a new PoolPipeline with the specified initial capacity.
	 * @param initialCapacity initial capacity
	 */
	public PoolPipeline(int initialCapacity) {
		this.list = new ArrayList<>(initialCapacity);
	}

	/**
	 * Pushes the handler to the back of the list.<br>
	 * It is executed in reverse order, so the handler will be the first executed.<p>
	 * If this is called during execution, it will be queued and executed once execution finishes.
	 * @param pool HandlerPool
	 * @param obj handler
	 */
	@Override
	public void push(HandlerPool pool, T obj) {
		if(depth == 0) {
			doPush(pool, obj, false);
		} else {
			queuedModifications.put(pool, () -> this.push(pool, obj));
		}
	}

	/**
	 * Pushes the handler to the front of the list.<br>
	 * It is executed in reverse order, so the handler will be the last executed.<p>
	 * If this is called during execution, it will be queued and executed once execution finishes.
	 * @param pool HandlerPool
	 * @param obj handler
	 */
	public void pushLast(HandlerPool pool, T obj) {
		if(depth == 0) {
			doPush(pool, obj, true);
		} else {
			queuedModifications.put(pool, () -> this.pushLast(pool, obj));
		}
	}

	/**
	 * If this is called during execution, it will be queued and executed once execution finishes,
	 * and this method will return false even if the pool does push this.
	 * @param pool HandlerPool
	 * @return true if the push was removed, false if not pushed by the pool or if called during execution
	 */
	@Override
	public boolean pop(HandlerPool pool) {
		if(depth == 0) {
			return super.pop(pool);
		} else {
			queuedModifications.put(pool, () -> this.pop(pool));
			return false;
		}
	}

	/**
	 * If this is called during execution, it will be queued and executed once execution finishes,
	 * and this method will return null even if the pool does push this.
	 * @param pool HandlerPool
	 * @return removed {@link PoolEntry} or null if not pushed by pool or if called during execution
	 */
	@Override
	public PoolEntry<T> popAndGet(HandlerPool pool) {
		if(depth == 0) {
			return super.popAndGet(pool);
		} else {
			queuedModifications.put(pool, () -> this.pop(pool));
			return null;
		}
	}

	/**
	 * Executes the pipeline and returns nothing. Example: <pre>{@code
	 * pipeline.execute((initial, ctx) -> {
	 *   initial.call(someParameter, 5, ctx);
	 * }, (parameter, num, ctx) -> {
	 *     // Don't use ctx.pass() here!
	 *     System.out.println("Parameter: " + parameter + ", Number: " + num);
	 * });
	 * }</pre>
	 * Handlers are executed in order of first pushed to last pushed.<br>
	 * <strong>**Do not**</strong> call {@link PipelineContext#pass()}
	 * in either the {@code initialCaller} or {@code bottomEntry}!
	 * @param initialCaller will be called with the first entry, pass parameters to this
	 * @param bottomEntry entry to call at the bottom.
	 * if the pipeline is empty, this will be passed to {@code initialCaller}
	 */
	public void execute(@NonNull BiConsumer<T, PipelineContext> initialCaller,
	                    @NotNull T bottomEntry) {
		this.execute((entry, ctx) -> {
			initialCaller.accept(entry, ctx);
			return null;
		}, bottomEntry);
	}

	/**
	 * Executes the pipeline and returns nothing. Example: <pre>{@code
	 * pipeline.execute((initial, ctx) -> {
	 *   initial.call(someParameter, 5, ctx);
	 * }, (parameter, num, ctx) -> {
	 *     // Don't use ctx.pass() here!
	 *     System.out.println("Parameter: " + parameter + ", Number: " + num);
	 * });
	 * }</pre>
	 * Handlers are executed in order of last pushed to first pushed.<br>
	 * <strong>**Do not**</strong> call {@link PipelineContext#pass()}
	 * in either the {@code initialCaller} or {@code bottomEntry}!
	 * @param initialCaller will be called with the first entry, pass parameters to this
	 * @param bottomEntry entry to call at the bottom.
	 * if the pipeline is empty, this will be passed to {@code initialCaller}
	 */
	public void executeReversed(@NonNull BiConsumer<T, PipelineContext> initialCaller,
	                    @NotNull T bottomEntry) {
		this.executeReversed((entry, ctx) -> {
			initialCaller.accept(entry, ctx);
			return null;
		}, bottomEntry);
	}

	/**
	 * Executes the pipeline and returns the result. Example: <pre>{@code
	 * String result = pipeline.execute((initial, ctx) -> {
	 *   return initial.call(someParameter, 5, ctx);
	 * }, (parameter, num, ctx) -> {
	 *     // Don't use ctx.pass() here!
	 *     System.out.println("Parameter: " + parameter + ", Number: " + num);
	 *     return "Hi!";
	 * });
	 * }</pre>
	 * Handlers are executed in order of first pushed to last pushed.<br>
	 * <strong>**Do not**</strong> call {@link PipelineContext#pass()}
	 * in either the {@code initialCaller} or {@code bottomEntry}!
	 * @param initialCaller will be called with the first entry, pass parameters to this
	 * @param bottomEntry entry to call at the bottom.
	 * if the pipeline is empty, this will be passed to {@code initialCaller}
	 * @return result of execution
	 */
	public R execute(@NonNull BiFunction<T, PipelineContext, R> initialCaller,
	                 @NotNull T bottomEntry) {
		PipelineContext ctx = this.getContext(bottomEntry, false);
		try {
			return initialCaller.apply(ctx.pass(), ctx);
		} finally {
			this.releaseContext(ctx);
		}
	}

	/**
	 * Executes the pipeline and returns the result. Example: <pre>{@code
	 * String result = pipeline.execute((initial, ctx) -> {
	 *   return initial.call(someParameter, 5, ctx);
	 * }, (parameter, num, ctx) -> {
	 *     // Don't use ctx.pass() here!
	 *     System.out.println("Parameter: " + parameter + ", Number: " + num);
	 *     return "Hi!";
	 * });
	 * }</pre>
	 * Handlers are executed in order of last pushed to first pushed.<br>
	 * <strong>**Do not**</strong> call {@link PipelineContext#pass()}
	 * in either the {@code initialCaller} or {@code bottomEntry}!
	 * @param initialCaller will be called with the first entry, pass parameters to this
	 * @param bottomEntry entry to call at the bottom.
	 * if the pipeline is empty, this will be passed to {@code initialCaller}
	 * @return result of execution
	 */
	public R executeReversed(@NonNull BiFunction<T, PipelineContext, R> initialCaller,
	                 @NotNull T bottomEntry) {
		PipelineContext ctx = this.getContext(bottomEntry, true);
		try {
			return initialCaller.apply(ctx.pass(), ctx);
		} finally {
			this.releaseContext(ctx);
		}
	}

	protected PipelineContext getContext(@NonNull T bottomEntry, boolean reversed) {
		this.depth++;

		PipelineContext result;
		if(this.context != null) {
			result = this.context;
			this.context = null;
		} else { // recursive call
			result = new PipelineContext();
		}

		result.index = 0;
		result.bottomEntry = bottomEntry;
		result.reversed = reversed;
		return result;
	}

	protected void releaseContext(PipelineContext context) {
		if(depth <= 0) {
			throw new IllegalStateException("Cannot not decrease depth below 0");
		}

		this.depth--;
		this.context = context;

		context.index = 0;
		context.bottomEntry = null;
		context.reversed = false;

		if(this.depth == 0) {
			queuedModifications.values().forEach(Runnable::run);
			queuedModifications.clear();
		}
	}

	/**
	 * Pipeline execution context.
	 */
	@Getter
	public class PipelineContext {
		/**
		 * true if execution is reversed.<p>
		 * Normal = first pushed, first called.<br>
		 * Reversed = first pushed, last called.
		 */
		@Getter private boolean reversed;

		private int index;
		private T bottomEntry;

		/**
		 * Returns the next handler in the pipeline.
		 * You don't need to call this, but if you do, don't call it twice or in the bottomEntry handler.
		 * Example: <pre>{@code
		 * if(myCondition()) {
		 *     return ctx.pass().call(someParameter, 5 + num, ctx);
		 * } else {
		 *     return null;
		 * }
		 * }</pre>
		 * @return next handler
		 * @throws IllegalStateException if called too many times, most likely in the bottomEntry handler
		 */
		public T pass() {
			if(this.index >= getPushCount()) {
				if(bottomEntry != null) {
					T result = this.bottomEntry;
					this.bottomEntry = null;
					return result;
				} else {
					throw new IllegalStateException("pass() called too many times. Did you call it in the bottomEntry handler?");
				}
			}

			T result = list.get(reversed ? index : getPushCount() - index - 1).obj();
			this.index++;
			return result;
		}

		/**
		 * @return true if we reached the bottom, most likely in the bottomEntry handler.
		 * If this returns true, do not call {{@link #pass()}!
		 */
		public boolean reachedBottom() {
			return bottomEntry == null;
		}

		/**
		 * @return {@link PoolPipeline} owning this context
		 */
		public PoolPipeline<T, R> getOwner() {
			return PoolPipeline.this;
		}
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
		// list is executed in reverse order,
		// we don't use a pushable for better iteration performance (?)

		if(reverse) { // pushLast
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