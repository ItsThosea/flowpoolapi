package me.thosea.flowpool.test;

import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.pushable.PoolToggle;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PoolToggleTest {
	HandlerPool poolA = new HandlerPool();
	HandlerPool poolB = new HandlerPool();
	HandlerPool poolC = new HandlerPool();

	@Test
	void testToggleStack() {
		PoolToggle stack = new PoolToggle();

		assertFalse(stack.isPushed());
		assertEquals(stack.getPushCount(), 0);

		stack.push(poolA);
		assertTrue(stack.isPushed());
		assertEquals(stack.getPushCount(), 1);

		stack.push(poolB);
		assertEquals(stack.getPushCount(), 2);
		stack.push(poolB);
		assertEquals(stack.getPushCount(), 2);

		stack.push(poolC);
		assertEquals(stack.getPushCount(), 3);

		poolB.close();
		poolB.close();
		assertEquals(stack.getPushCount(), 2);

		poolC.close();
		assertEquals(stack.getPushCount(), 1);

		poolA.close();
		assertEquals(stack.getPushCount(), 0);
		assertFalse(stack.isPushed());

		AtomicInteger pushCount = new AtomicInteger();
		AtomicInteger popCount = new AtomicInteger();
		stack.pushCallback((a, b) -> pushCount.incrementAndGet());
		stack.popCallback((a, b) -> popCount.incrementAndGet());

		stack.push(poolA);
		stack.push(poolA); // should not trigger callback
		stack.push(poolB);
		poolB.close();

		assertEquals(2, pushCount.get());
		assertEquals(1, popCount.get());
	}
}