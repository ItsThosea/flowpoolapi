package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * One arg, no return. For use with {@link PoolPipeline}.
 * @param <T> arg 1
 */
@FunctionalInterface
public interface SingleArgConsumer<T> {
	void call(T arg, PoolPipeline<SingleArgConsumer<T>, Void>.PipelineContext ctx);
}