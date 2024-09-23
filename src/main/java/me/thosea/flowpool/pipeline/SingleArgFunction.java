package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * One arg and a return. For use with {@link PoolPipeline}.
 * @param <T> arg 1
 * @param <R> return type
 */
@FunctionalInterface
public interface SingleArgFunction<T, R> {
	R call(T arg, PoolPipeline<SingleArgFunction<T, R>, R>.PipelineContext ctx);
}