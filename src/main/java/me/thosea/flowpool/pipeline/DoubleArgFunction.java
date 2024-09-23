package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * Two args and a return. For use with {@link PoolPipeline}.
 * @param <A> arg 1
 * @param <B> arg 2
 * @param <R> return type
 */
@FunctionalInterface
public interface DoubleArgFunction<A, B, R> {
	R call(A arg1, B arg2, PoolPipeline<DoubleArgFunction<A, B, R>, R>.PipelineContext ctx);
}