package me.thosea.flowpool.test;

import me.thosea.flowpool.HandlerPool;
import me.thosea.flowpool.pipeline.SingleArgFunction;
import me.thosea.flowpool.pushable.PoolPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PoolPipelineTest {
	PoolPipeline<SingleArgFunction<String, String>, String> stack = new PoolPipeline<>();
	HandlerPool poolA = new HandlerPool();
	HandlerPool poolB = new HandlerPool();

	@BeforeEach
	void beforeEach() {
		poolA.close();
		poolB.close();
	}

	@Test
	void testExecute() {
		AtomicInteger execCount = new AtomicInteger();

		stack.push(poolA, (param, ctx) -> {
			execCount.incrementAndGet();
			assertEquals("b", param);
			return ctx.pass().call("c", ctx);
		});
		stack.push(poolB, (param, ctx) -> {
			execCount.incrementAndGet();
			assertEquals("a", param);
			return ctx.pass().call("b", ctx);
		});

		assertEquals("result", stack.execute((initial, ctx) -> {
			return initial.call("a", ctx);
		}, (param, ctx) -> {
			execCount.incrementAndGet();
			assertEquals("c", param);
			return "result";
		}));

		assertEquals(3, execCount.get());
	}

	@Test
	void testExecuteReversed() {
		stack.push(poolA, (param, ctx) -> {
			assertEquals("a", param);
			return ctx.pass().call("b", ctx);
		});
		stack.push(poolB, (param, ctx) -> {
			assertEquals("b", param);
			return ctx.pass().call("c", ctx);
		});

		assertEquals("result", stack.executeReversed((initial, ctx) -> {
			return initial.call("a", ctx);
		}, (param, ctx) -> {
			assertEquals("c", param);
			return "result";
		}));
	}

	@Test
	void testDontCallBottom() {
		stack.push(poolA, (param, ctx) -> {
			assertEquals("normal", param);
			return ctx.pass().call("normal", ctx);
		});
		assertEquals("normal", stack.execute((initial, ctx) -> {
			return initial.call("normal", ctx);
		}, (param, ctx) -> {
			assertEquals("normal", param);
			return "normal";
		}));

		stack.push(poolA, (param, ctx) -> {
			// Don't call ctx.pass() here
			assertEquals("blah", param);
			return "early";
		});
		assertEquals("early", stack.execute((initial, ctx) -> {
			return initial.call("blah", ctx);
		}, (a, b) -> {
			return fail("reached bottomEntry handler");
		}));
	}

	@Test
	void testModQueue() {
		stack.push(poolA, (param, ctx) -> {
			stack.push(poolB, (param1, ctx1) -> {
				assertEquals("poolA", param1);
				return ctx.pass().call("poolB", ctx);
			});

			assertEquals(1, stack.getPushCount()); // add should be queued
			return ctx.pass().call("poolA", ctx);
		});

		stack.execute((initial, ctx) -> {
			initial.call("blah", ctx);
		}, (param, ctx) -> {
			assertEquals("poolA", param);
			return null;
		});
		assertEquals(2, stack.getPushCount()); // add queue should be run

		stack.push(poolA, (param, ctx) -> ctx.pass().call(param, ctx)); // dummy
		stack.push(poolB, (param, ctx) -> {
			poolB.close();
			assertEquals(2, stack.getPushCount()); // remove should be queued
			return ctx.pass().call(param, ctx);
		});
		stack.execute((initial, ctx) -> {
			initial.call("blah", ctx);
		}, (param, ctx) -> {
			return null;
		});

		assertFalse(poolB.isPushing(stack)); // remove should be run
		assertTrue(poolA.isPushing(stack));
	}
}