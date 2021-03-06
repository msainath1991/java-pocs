### Benefits

1. Threads are create 1 time and 1 time only and reused. This happens up to front.
2. Dinamically we add/drop queues/triggers/jobs but thats it fine.

### Architecture

![alt text](https://raw.githubusercontent.com/diegopacheco/java-pocs/master/pocs/task-scheduller-poc/threads-model-screth-draw.jpg "Architecture Draw / Scatch")

Basically here we are 4 main componentes on this architecre:
 - Qaurtz
 - Thread Pool in Quartz = 10 Threads (check quartz.properties)
 - QueueManager who has queues per groups and only let 1 thread use 1 queue per per time per group
 - QueueTaskWrapper which is a generic Quartz task which wrappes other quartz tasks in order to run whithin 2 properties:
   1. Whithin a group all tasks are serial - 1 at the time
   2. Whithin multiples groups all can run in parallel

There are 2 solutions - 1 using quartz here: 
https://github.com/diegopacheco/java-pocs/tree/master/pocs/task-scheduller-poc/src/main/java/com/github/diegopacheco/java/pocs/quartz

Also there is other solution(same ideas) but whithout quartz:
https://github.com/diegopacheco/java-pocs/tree/master/pocs/task-scheduller-poc/src/main/java/com/github/diegopacheco/java/pocs/executor

### How Thread Model Works (Quartz)

You need to create your quartzs tasks normally and them you basically need to create an extra class that extends from: QueueTaskWrapperBase. You need to implement the method getGroup and need provide your group. Each group has they own queue.
Them you need to register the group before start enqueue tasks like:
```java
QueueManager.getInstance().register("g1");    
``` 
After Register the groups we can create the Tasks, Triggers and Jobs.
```java
  JobDetail job1 = newJob(Group1TaskAdapter.class)
        .withIdentity("job1", "group1")
        .build();
    
    Trigger trigger1 = newTrigger()
        .withIdentity("trigger1", "group1")
        .startNow()
        .withSchedule(simpleSchedule()
                .withIntervalInSeconds(1)
                .repeatForever())
        .build();
    
      scheduler.scheduleJob(job1, trigger1);
```   
We should have onlye 1 job/trigger/schedulle per group and them we should add tasks on the queue. Like:
```java
QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
```
When we schdulle something in the queue we need pass the group. This will work well for single_shot tasks.
For RECORENT taskas we need have a backhround task or another single scheduller in quartz to KEEP schdulling recorent tasks
but this need to happens only when is time to them to run. Its also possible to use a GenericTaskAdapter where the queue/group will be
the quartz group fron the jobkey - this way we dont need create any adapters classes.
```java
    JobDetail job3 = newJob(GenericTaskAdapter.class)
        .withIdentity("job3", "g3")
        .build();
    
    Trigger trigger3 = newTrigger()
        .withIdentity("trigger3", "g3")
        .startNow()
        .withSchedule(simpleSchedule()
                .withIntervalInSeconds(1)
                .repeatForever())
        .build();

    scheduler.scheduleJob(job3, trigger3);
``` 

## How Thread Model Works (Executors - dont use quartz)]

The ideas are pretty much the same - however with Executors we can have a generic GenericTaskAdapter and we dont need
create multiple adapters classes. Also with Executors we need to have a WorkerManager in order to schdule threads since
we dont have quartz anymore.

```java
    QueueManager.getInstance().register("g1");
    QueueManager.getInstance().register("g2");
    
    WorkerManager.getInstance().schedule(new GenericTaskAdapter("g1"));
    WorkerManager.getInstance().schedule(new GenericTaskAdapter("g2"));
    
    QuietThread.sleep(3000);
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QuietThread.sleep(5000);
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g2");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g1");
    QueueManager.getInstance().enqueueTask(new DateTask(), "g2");
```

### Sample Output

Solution not using Quartz
```bash
[pool-1-thread-1] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-3] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-1] Nothing to do... 
[pool-1-thread-4] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:12 PDT 2018
[pool-1-thread-3] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:14 PDT 2018
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:16 PDT 2018
Running Task: DateTask from group: g2
Current Date:Sat Jul 21 23:18:17 PDT 2018
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:18 PDT 2018
Running Task: DateTask from group: g2
Current Date:Sat Jul 21 23:18:19 PDT 2018
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:20 PDT 2018
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:22 PDT 2018
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 23:18:25 PDT 2018
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
[pool-1-thread-2] Nothing to do... 
[pool-1-thread-6] Nothing to do... 
```

Solution using Quartz

```bash
jul 21, 2018 10:42:50 PM org.quartz.impl.StdSchedulerFactory instantiate
INFO: Using default implementation for ThreadExecutor
jul 21, 2018 10:42:50 PM org.quartz.core.SchedulerSignalerImpl <init>
INFO: Initialized Scheduler Signaller of type: class org.quartz.core.SchedulerSignalerImpl
jul 21, 2018 10:42:50 PM org.quartz.core.QuartzScheduler <init>
INFO: Quartz Scheduler v.2.3.0 created.
jul 21, 2018 10:42:50 PM org.quartz.simpl.RAMJobStore initialize
INFO: RAMJobStore initialized.
jul 21, 2018 10:42:50 PM org.quartz.core.QuartzScheduler initialize
INFO: Scheduler meta-data: Quartz Scheduler (v2.3.0) 'GlobalScheduler' with instanceId 'NON_CLUSTERED'
  Scheduler class: 'org.quartz.core.QuartzScheduler' - running locally.
  NOT STARTED.
  Currently in standby mode.
  Number of jobs executed: 0
  Using thread pool 'org.quartz.simpl.SimpleThreadPool' - with 10 threads.
  Using job-store 'org.quartz.simpl.RAMJobStore' - which does not support persistence. and is not clustered.
jul 21, 2018 10:42:50 PM org.quartz.impl.StdSchedulerFactory instantiate
INFO: Quartz scheduler 'GlobalScheduler' initialized from default resource file in Quartz package: 'quartz.properties'
jul 21, 2018 10:42:50 PM org.quartz.impl.StdSchedulerFactory instantiate
INFO: Quartz scheduler version: 2.3.0
jul 21, 2018 10:42:50 PM org.quartz.core.QuartzScheduler start
INFO: Scheduler GlobalScheduler_$_NON_CLUSTERED started.
[GlobalScheduler_Worker-1] Nothing to do... 
[GlobalScheduler_Worker-2] Nothing to do... 
[GlobalScheduler_Worker-3] Nothing to do... 
[GlobalScheduler_Worker-4] Nothing to do... 
[GlobalScheduler_Worker-5] Nothing to do... 
[GlobalScheduler_Worker-6] Nothing to do... 
[GlobalScheduler_Worker-7] Nothing to do... 
[GlobalScheduler_Worker-8] Nothing to do... 
Running Task: DateTask from group: g1
[GlobalScheduler_Worker-10] Nothing to do... 
Current Date:Sat Jul 21 22:42:54 PDT 2018
Queue: g1 is busy now...  Size: 4
[GlobalScheduler_Worker-1] Nothing to do... 
[GlobalScheduler_Worker-2] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 22:42:56 PDT 2018
[GlobalScheduler_Worker-4] Nothing to do... 
Queue: g1 is busy now...  Size: 3
[GlobalScheduler_Worker-5] Nothing to do... 
[GlobalScheduler_Worker-6] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 22:42:58 PDT 2018
[GlobalScheduler_Worker-8] Nothing to do... 
Queue: g1 is busy now...  Size: 4
[GlobalScheduler_Worker-10] Nothing to do... 
Running Task: DateTask from group: g2
Current Date:Sat Jul 21 22:42:59 PDT 2018
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 22:43:00 PDT 2018
Queue: g2 is busy now...  Size: 1
[GlobalScheduler_Worker-9] Nothing to do... 
Queue: g1 is busy now...  Size: 3
[GlobalScheduler_Worker-4] Nothing to do... 
Running Task: DateTask from group: g2
Current Date:Sat Jul 21 22:43:01 PDT 2018
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 22:43:02 PDT 2018
[GlobalScheduler_Worker-6] Nothing to do... 
Queue: g1 is busy now...  Size: 2
[GlobalScheduler_Worker-8] Nothing to do... 
[GlobalScheduler_Worker-10] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 22:43:04 PDT 2018
[GlobalScheduler_Worker-9] Nothing to do... 
Running Task: DateTask from group: g1
Current Date:Sat Jul 21 22:43:05 PDT 2018
[GlobalScheduler_Worker-2] Nothing to do... 
[GlobalScheduler_Worker-4] Nothing to do... 
[GlobalScheduler_Worker-5] Nothing to do... 
[GlobalScheduler_Worker-6] Nothing to do... 
[GlobalScheduler_Worker-8] Nothing to do... 
[GlobalScheduler_Worker-3] Nothing to do... 
[GlobalScheduler_Worker-10] Nothing to do... 
[GlobalScheduler_Worker-9] Nothing to do... 
[GlobalScheduler_Worker-7] Nothing to do... 
[GlobalScheduler_Worker-2] Nothing to do... 
[GlobalScheduler_Worker-4] Nothing to do... 
```

### Diferences between Quartz and Executors

With Quartz we dont have isolation between Threads, all tasks run in parallel, that why we need queue to SERIALIZE inside same group.
With Executors N elements on same queue dont run any parallel, so they queue is the only thing we need to dont need CODE to ensure SERIALIZE like ConcurrentHashMap we need to have in quartz, so Executors PARALELZE per schule but SERIALIZE per queue thus more simple.