package me.thosea.flowpool.pipeline;

import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * No args and a return. For use with {@link PoolPipeline}.
 * @param <R> return type
 */
@FunctionalInterface
public interface NoArgsFunction<R> {
	R call(PoolPipeline<NoArgsFunction<R>, R>.PipelineContext ctx);
}