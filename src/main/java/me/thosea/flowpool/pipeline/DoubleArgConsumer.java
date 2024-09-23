package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * Two args, no return. For use with {@link PoolPipeline}.
 * @param <A> arg 1
 * @param <B> arg 2
 */
@FunctionalInterface
public interface DoubleArgConsumer<A, B> {
	void call(A arg1, B arg2, PoolPipeline<DoubleArgConsumer<A, B>, Void>.PipelineContext ctx);
}