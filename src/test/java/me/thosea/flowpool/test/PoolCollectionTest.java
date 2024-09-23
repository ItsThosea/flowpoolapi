package me.thosea.flowpool.test;

import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.pushable.AbstractPoolCollection;
import me.thosea.flowpool.pushable.PoolList;
import me.thosea.flowpool.pushable.PoolPipeline;
import me.thosea.flowpool.pushable.PoolStack;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class PoolCollectionTest {
	HandlerPool poolA = new HandlerPool();
	HandlerPool poolB = new HandlerPool();
	HandlerPool poolC = new HandlerPool();

	@ParameterizedTest
	@MethodSource("pushableSupplier")
	void testPush(AbstractPoolCollection<String> pushable, ListCreator lists) {
		pushable.push(poolA, "apples");
		assertIterableEquals(List.of("apples"), pushable.getEntries());
		assertEquals(pushable.getPushCount(), 1);

		pushable.push(poolB, "carrots");
		pushable.push(poolB, "oranges");
		assertIterableEquals(lists.make("apples", "oranges"), pushable.getEntries());
		assertEquals(pushable.getPushCount(), 2);

		pushable.push(poolC, "broccoli");
		pushable.push(poolC, "bananas");
		assertIterableEquals(lists.make("apples", "oranges", "bananas"), pushable.getEntries());
		assertEquals(pushable.getPushCount(), 3);

		poolB.close();
		poolB.close();
		assertIterableEquals(lists.make("apples", "bananas"), pushable.getEntries());
		assertEquals(pushable.getPushCount(), 2);

		poolC.close();
		assertIterableEquals(List.of("apples"), pushable.getEntries());
		assertEquals(pushable.getPushCount(), 1);

		poolA.close();
		assertEquals(0, pushable.getPushCount());

		if(pushable instanceof PoolStack<String> stack) {
			AtomicInteger callCount = new AtomicInteger();
			AtomicInteger popCount = new AtomicInteger();
			stack.pushCallback((a, b) -> callCount.incrementAndGet());
			stack.popCallback((a, b) -> popCount.incrementAndGet());

			stack.push(poolB, "oranges"); // [oranges]
			stack.pushLast(poolA, "apples"); // [oranges, apples] (structure is reversed)
			assertIterableEquals(lists.make("apples", "oranges"), pushable.getEntries());
			assertEquals("apples", stack.popAndGet(poolA).obj());

			assertEquals(2, callCount.get());
			assertEquals(1, popCount.get());
		} else if(pushable instanceof PoolPipeline<String, ?> stack) {
			AtomicInteger callCount = new AtomicInteger();
			AtomicInteger popCount = new AtomicInteger();
			stack.pushCallback((a, b) -> callCount.incrementAndGet());
			stack.popCallback((a, b) -> popCount.incrementAndGet());

			// internal data structure is reversed compared to execution order,
			// so getEntries returns in order of pushes
			stack.push(poolB, "oranges"); // [oranges]
			stack.pushLast(poolA, "apples"); // [apples, oranges] (name is according to execution order)
			assertIterableEquals(lists.make("apples", "oranges"), pushable.getEntries());
			poolA.close();

			assertEquals(2, callCount.get());
			assertEquals(1, popCount.get());
		} else if(pushable instanceof PoolList<String> stack) {
			AtomicInteger callCount = new AtomicInteger();
			AtomicInteger popCount = new AtomicInteger();
			stack.pushCallback((a, b) -> callCount.incrementAndGet());
			stack.popCallback((a, b) -> popCount.incrementAndGet());

			stack.push(poolB, "oranges"); // [oranges]
			stack.pushFirst(poolA, "apples"); // [apples, oranges]
			assertIterableEquals(lists.make("apples", "oranges"), pushable.getEntries());
			poolA.close();

			assertEquals(2, callCount.get());
			assertEquals(1, popCount.get());
		}
	}

	private static Stream<Arguments> pushableSupplier() {
		return Stream.of(
				arguments(new PoolStack<>(), (ListCreator) args -> {
					List<String> list = Arrays.asList(args);
					Collections.reverse(list);
					return list;
				}),
				arguments(new PoolList<>(), (ListCreator) List::of),
				arguments(new PoolPipeline<>(), (ListCreator) List::of)
		);
	}

	@FunctionalInterface
	private interface ListCreator {
		List<String> make(String... args);
	}
}