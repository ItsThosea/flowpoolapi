package me.thosea.flowpool;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The definition of overengineering.<br>
 * A HandlerPool groups pushed stacks.
 * @author thosea
 */
@Getter
public class HandlerPool {
	private final List<Runnable> closeCallbacks = new ArrayList<>();
	private final Set<IPoolPushable<?>> pushedStacks = new HashSet<>();

	/**
	 * Adds a callback to be ran when {@link #close()} is called.
	 * @param action action to run
	 * @throws NullPointerException if action is null
	 */
	public void runOnClose(@NonNull Runnable action) {
		Objects.requireNonNull(action);
		closeCallbacks.add(action);
	}

	/**
	 * @param stack stack to check
	 * @return true if the stack was pushed by this HandlerPool
	 */
	public boolean isPushing(IPoolPushable<?> stack) {
		return pushedStacks.contains(stack);
	}

	/**
	 * Runs closed callbacks, then pops all pushed stacks.<br>
	 * The HandlerPool can still be used after.
	 * @see #runOnClose(Runnable)
	 */
	public void close() {
		if(!closeCallbacks.isEmpty()) {
			Iterator<Runnable> iterator = this.closeCallbacks.iterator();
			while(iterator.hasNext()) {
				iterator.next().run();
				iterator.remove();
			}
		}

		if(!pushedStacks.isEmpty()) {
			for(Object obj : pushedStacks.toArray()) {
				((IPoolPushable<?>) obj).pop(this);
			}
		}
	}
}