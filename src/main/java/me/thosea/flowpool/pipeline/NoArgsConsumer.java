package me.thosea.flowpool.pipeline;


import me.thosea.flowpool.pushable.PoolPipeline;

/**
 * No args, no return. For use with {@link PoolPipeline}.
 */
@FunctionalInterface
public interface NoArgsConsumer {
	void call(PoolPipeline<NoArgsConsumer, Void>.PipelineContext ctx);
}