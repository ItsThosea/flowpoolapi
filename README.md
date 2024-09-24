## FlowPoolAPI
...is a simple-yet-powerful state management system based around the HandlerPool.

## Why?
Time after time I see the broken state-machine approach games and libraries use like OpenGL.<br>
While this initially works, once more than one source affects one state, the state will be reverted earlier than expected.<br>
Take something like this: 
```java
Runnable someCallback = () -> {
	entity.setSomeProperty(true);
	entity.doCalculation();
	entity.setSomeProperty(false); 
};

entity.setSomeProperty(true);
entity.doCalculation(); // I expect `someProperty` to be true.
someCallback.run();
entity.doOtherCalculation(); // Oops! - someProperty was set to false too early.
entity.setSomeProperty(false);
```
With FlowPoolAPI, this same logic would be: 
```java
// entity.someProperty is a `PoolToggle`

HandlerPool pool = new HandlerPool();
HandlerPool callbacksPool = new HandlerPool();

Runnable someCallback = () -> {
	entity.someProperty.push(callbacksPool);
	entity.doCalculation();
	callbacksPool.close(); // or entity.someProperty.pop(pool);
};

entity.someProperty.push(pool);
entity.doCalculation();
someCallback.run();
entity.doOtherCalculation(); // entity.someProperty is still true!
pool.close(); // entity.someProperty set to false
```

Notice how each state manipulator hold its own `HandlerPool`.<br>
Alongside `PoolToggle`, there's `PoolList` and `PoolStack` for basic collections, and `PoolPipeline` for handler-based execution pipelines.<br><br>
Find more about it in the [wiki](https://github.com/ItsThosea/flowpoolapi/wiki)!

## How do I use it?
Add the maven repo: 
```groovy
maven {
    name "teamcelestial-public"
    url "https://maven.teamcelestial.org/public"
}
```
And find the latest Gradle/Maven dependency here: https://maven.teamcelestial.org/#/public/me/thosea/flowpool/flowpoolapi<br>
It requires Java 17 or higher.

### Javadocs?
https://maven.teamcelestial.org/javadoc/public/me/thosea/flowpool/flowpoolapi/1.0.0

### Thread Safety?
None of these are thread-safe! You are responsible for enforcing thread safety yourself.

### How'd you get the name?
An AI generated it.