package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * Three args, no return. For use with {@link PoolPipeline}.
 * @param <A> arg 1
 * @param <B> arg 2
 * @param <C> arg 3
 */
@FunctionalInterface
public interface TripleArgConsumer<A, B, C> {
	void call(A arg1, B arg2, C arg3, PoolPipeline<TripleArgConsumer<A, B, C>, Void>.PipelineContext ctx);
}