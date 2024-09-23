package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * Three args and a return. For use with {@link PoolPipeline}.
 * @param <A> arg 1
 * @param <B> arg 2
 * @param <C> arg 3
 * @param <R> return type
 */
@FunctionalInterface
public interface TripleArgFunction<A, B, C, R> {
	R call(A arg1, B arg2, C arg3, PoolPipeline<TripleArgFunction<A, B, C, R>, R>.PipelineContext ctx);
}