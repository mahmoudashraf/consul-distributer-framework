# consul-leaderelection
 Leader Election Module for services running accress Concul


TODO:
export as jar 
Build test aplication
who to handle closing resources and removing listners
@Bean(name="graphDatabaseService", destroyMethod = "shutdown")->removelistner









@Before(set service node info {"ipAddress":"127.0.0.1","port":8080,"nodeId":"localhost-8080"}")
E2E Scenarios:
@Before(Live Consul Leader values is {"ipAddress":"live1","port":8081,"nodeId":"id1"})
1-"StartUp With Valid Leader exist values
observer run -> receive valid values from listner ->isgranted returns false
	->new leader configred notification ->watcher isleader assigned false -> observer leader is {"ipAddress":"live1","port":8081,"nodeId":"id1"}
 	->live Consul Leader key {"ipAddress":"live1","port":8081,"nodeId":"id1"}  

@Before(Live Consul Leader values is {"ipAddress":"Live2","port":808")
2-"StartUp With InValid Leader values
observer run -> receive valid values from listner ->isgranted returns false
	->new leader configred notification ->watcher isleader assigned false -> observer leader isEmpty return true -> watcher isleader assigned false
	->volounteer start -> granted Leader notification ->watcher isleader assigned true -> observer leader isEmpty return false -> watcher isleader assigned true
	->observer leader is  {"ipAddress":"127.0.0.1","port":8080,"nodeId":"localhost-8080"}
	->live Consul Leader key {"ipAddress":"127.0.0.1","port":8080,"nodeId":"localhost-8080"}

 
@Before( Live Consul Leader values is "")
3-"StartUp With Empty Leader values
observer run -> receive valid values from listner ->isgranted returns false
	->new leader configred notification ->watcher isleader assigned false -> observer leader isEmpty return true -> watcher isleader assigned false
	->volounteer start -> granted Leader notification ->watcher isleader assigned true -> observer leader isEmpty return false -> watcher isleader assigned true
	->observer leader is  {"ipAddress":"127.0.0.1","port":8080,"nodeId":"localhost-8080"}
	->live Consul Leader key {"ipAddress":"127.0.0.1","port":8080,"nodeId":"localhost-8080"}


@Before( Live Consul Leader values is "")
4-"StartUp With Empty Leader values Trying to volounteer While Another service is Volounteering ({"ipAddress":"127.0.0.2","port":8080,"nodeId":"localhost-8080"}) 
observer run -> receive valid values from listner ->isgranted returns false
	->new leader configred notification ->watcher isleader assigned false -> observer leader isEmpty return true -> watcher isleader assigned false
	->volounteer start ->new leader configred notification ->watcher isleader assigned false -> observer leader isEmpty return false -> watcher isleader assigned false
	->observer leader is  {"ipAddress":"127.0.0.2","port":8080,"nodeId":"localhost-8080"}
	->live Consul Leader key {"ipAddress":"127.0.0.2","port":8080,"nodeId":"localhost-8080"}



start with leadership is disabled
5-Stepdown and receive new leader 
6-stepup and rimeout to volounteer 
7-voulounter with invalid service node 
8-session validation 
gnore key remove while session associated 
new leader notification while watcher in load  
step down while watcher in loop 






d{} - Election Data is presented{"ipAddress":"172.27.166.177","port":8080,"nodeId":"localhost:customer-service:879572"} -- Optional[{"ipAddress":"172.27.166.177","port":8080,"nodeId":"localhost:customer-service:879572"}]
d{} - Election Result is Current Leader{"ipAddress":"172.27.166.177","port":8080,"nodeId":"localhost:customer-service:879572"}
d{} - Checking if current serviceNode is granted Leader ({"host":"172.27.166.177","port":"8080","nodeId":"localhost:customer-service:745620"}),({"host":"172.27.166.177","port":"8080","nodeId":"localhost:customer-service:879572"})
d{} - I'm Servent Now
d{} - Publishing New Leader Configured Event
Exception in thread "Thread-1654" org.springframework.core.task.TaskRejectedException: Executor [java.util.concurrent.ThreadPoolExecutor@6bc76d22[Terminated, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 41]] did not accept task: org.springframework.context.event.SimpleApplicationEventMulticaster$$Lambda$1072/0x0000000801303840@5c1c3482
	at org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.execute(ThreadPoolTaskExecutor.java:317)
	at org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor.execute(LazyTraceThreadPoolTaskExecutor.java:66)
	at jdk.internal.reflect.GeneratedMethodAccessor75.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.springframework.cloud.sleuth.instrument.async.ExecutorMethodInterceptor.invoke(ExecutorBeanPostProcessor.java:351)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749)
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:691)
	at org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor$$EnhancerBySpringCGLIB$$fb37d50f.execute(<generated>)
	at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:136)
	at org.springframework.context.support.AbstractApplicationContext.publishEvent(AbstractApplicationContext.java:403)
	at org.springframework.context.support.AbstractApplicationContext.publishEvent(AbstractApplicationContext.java:360)
	at pl.piomin.services.customer.event.NewLeaderConfiguredEventPublisher.publish(NewLeaderConfiguredEventPublisher.java:19)
	at pl.piomin.services.customer.leader.LeaderUtil$1.run(LeaderUtil.java:46)
	at java.base/java.lang.Thread.run(Thread.java:830)
Caused by: java.util.concurrent.RejectedExecutionException: Task org.springframework.context.event.SimpleApplicationEventMulticaster$$Lambda$1072/0x0000000801303840@5c1c3482 rejected from java.util.concurrent.ThreadPoolExecutor@6bc76d22[Terminated, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 41]
	at java.base/java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2055)
	at java.base/java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:825)
	at java.base/java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1355)
	at org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.execute(ThreadPoolTaskExecutor.java:314)
	... 15 more