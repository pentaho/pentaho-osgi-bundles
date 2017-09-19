This OSGi bundle simply wraps camel-guava-eventbus-2.17.7.jar artifact
with the reason to override version of com.google.common.eventbus package being imported (force it to be 17.x).
This is needed due to inability of using guava's EventBus in blueprint container starting from guava-18.0.

Considering there are no breaking changes in 18.0 and 19.0 releases of Guava eventbus,
that could affect camel-guava-eventbus, we need to downgrade guava to be able to use it in Blueprint context.

EventBus instance can't be injected from a reference, since the Blueprint container must proxy it.
The container generates a subclass at runtime to be able to proxy a class.
This has the limitation of not being able to work on final classes or final methods.
And starting from guava-18.0 version, the EventBus class does have final methods.