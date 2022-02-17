package com.planet.util.springBoot;

import lombok.Data;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

@Data
public class SpringBootQuartzManager {

    private Scheduler scheduler;

    /**
     * 增加一个定时任务
     *
     * @param jobClass     任务实现类(必须实现了Job接口)
     * @param jobName      任务名称
     * @param jobGroupName 任务组名
     * @param jobCron      cron表达式(如：0/5 * * * * ? )
     * @return
     */
    public boolean addJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, String jobCron) {
        try {
            //判断是否已经存在同名的定时任务,如果有则不再创建
            TriggerKey checkTriggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger checkTrigger = (CronTrigger) scheduler.getTrigger(checkTriggerKey);
            if (checkTrigger != null) {//说明存在同名的定时任务
                return false;
            }
            // 通过JobBuilder构建JobDetail实例，JobDetail规定只能是实现Job接口的实例
            // JobDetail 是具体Job实例
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();

            //基于表达式构建触发器
            // CronTrigger表达式触发器 继承于Trigger
            // TriggerBuilder 用于构建触发器实例
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroupName)
                    .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobCron)).startNow().build();
            scheduler.scheduleJob(jobDetail, trigger);
            if (!scheduler.isShutdown()) {
                //添加定时任务后直接启动定时任务
                scheduler.start();
            }
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("增加一个定时任务出现异常");
        }
    }

    /**
     * 修改指定的定时任务的jobCron
     * @param jobName 任务名称
     * @param jobGroupName 任务组名
     * @param jobCron cron表达式(如：0/5 * * * * ? )
     * @return
     */
    public boolean updateJobCron(String jobName, String jobGroupName, String jobCron) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
//                return;
            }
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobCron)).build();
            // 重启触发器
            scheduler.rescheduleJob(triggerKey, trigger);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("修改指定的定时任务的jobCron出现异常");
        }
    }

    /**
     * 修改(or新增)一个定时任务的jobCron
     * 存在则更新不存在创建
     * @param jobClass     任务实现类(必须实现了Job接口)
     * @param jobName      任务名称
     * @param jobGroupName 任务组名称
     * @param jobCron      cron表达式(如：0/5 * * * * ? )
     * @return
     */
    public boolean updateJobCronOrAddJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, String jobCron) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return addJob(jobClass, jobName, jobGroupName, jobCron);
            } else {
                if (trigger.getCronExpression().equals(jobCron)) {
                    return false;
                }
                return updateJobCron(jobName, jobGroupName, jobCron);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("修改(or新增)一个定时任务的jobCron出现异常");
        }
    }

    /**
     * 删除任务一个定时任务
     *
     * @param jobName 任务名称
     * @param jobGroupName 任务组名
     */
    public boolean deleteJob(String jobName, String jobGroupName) {
        try {
            return scheduler.deleteJob(new JobKey(jobName, jobGroupName));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除任务一个定时任务出现异常");
        }
    }

    /**
     * 根据name获取指定的定时任务的信息列表
     * @param jobName 任务名称
     * @param jobGroupName 任务组名
     * @return
     */
    public Map<String, Object> getJobByName(String jobName, String jobGroupName){
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger != null) {//说明存在指定名称的定时任务
                JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("jobName", jobKey.getName());
                jobMap.put("jobGroupName", jobKey.getGroup());
                jobMap.put("description", "触发器:" + trigger.getKey());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                jobMap.put("jobStatus", triggerState.name());
                jobMap.put("jobTime", trigger.getCronExpression());
                return jobMap;
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("根据name获取指定的定时任务的信息列表出现异常");
        }
        return null;
    }


    /**
     * 获取所有计划中的定时任务列表
     *
     * @return
     */
    public List<Map<String, Object>> queryAllJob() {
        List<Map<String, Object>> jobList = null;
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            jobList = new ArrayList<>();
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("jobName", jobKey.getName());
                    map.put("jobGroupName", jobKey.getGroup());
                    map.put("description", "触发器:" + trigger.getKey());
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    map.put("jobStatus", triggerState.name());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        map.put("jobTime", cronExpression);
                    }
                    jobList.add(map);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return jobList;
    }

    /**
     * 获取所有正在运行的定时任务
     *
     * @return
     */
    public List<Map<String, Object>> queryRunJon() {
        List<Map<String, Object>> jobList = null;
        try {
            List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
            jobList = new ArrayList<Map<String, Object>>(executingJobs.size());
            for (JobExecutionContext executingJob : executingJobs) {
                Map<String, Object> map = new HashMap<String, Object>();
                JobDetail jobDetail = executingJob.getJobDetail();
                JobKey jobKey = jobDetail.getKey();
                Trigger trigger = executingJob.getTrigger();
                map.put("jobName", jobKey.getName());
                map.put("jobGroupName", jobKey.getGroup());
                map.put("description", "触发器:" + trigger.getKey());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                map.put("jobStatus", triggerState.name());
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    map.put("jobTime", cronExpression);
                }
                jobList.add(map);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return jobList;
    }




    /**
     * 根据name暂停一个定时任务
     *
     * @param jobName 任务名称
     * @param jobGroupName 任务组名
     */
    public boolean pauseJobByName(String jobName, String jobGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
            }
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            scheduler.pauseJob(jobKey);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("根据name暂停一个job出现异常");
        }
    }

    /**
     * 根据name恢复一个定时任务
     *
     * @param jobName 任务名称
     * @param jobGroupName 任务组名
     */
    public boolean resumeJobByName(String jobName, String jobGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
            }
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            scheduler.resumeJob(jobKey);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("根据name恢复一个job出现异常");
        }
    }


    /**
     * 根据name立即执行一个定时任务
     *
     * @param jobName 任务名称
     * @param jobGroupName 任务组名
     */
    public boolean runJobNowByName(String jobName, String jobGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
            }
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            scheduler.triggerJob(jobKey);
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("根据name立即执行一个job出现异常");
        }
    }


//    public void addJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, int jobTime) {
//        addJob(jobClass, jobName, jobGroupName, jobTime, -1);
//    }
//
//    public void addJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, int jobTime, int jobTimes) {
//        try {
//            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName)// 任务名称和组构成任务key
//                    .build();
//            // 使用simpleTrigger规则
//            Trigger trigger = null;
//            if (jobTimes < 0) {
//                trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroupName)
//                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1).withIntervalInSeconds(jobTime))
//                        .startNow().build();
//            } else {
//                trigger = TriggerBuilder
//                        .newTrigger().withIdentity(jobName, jobGroupName).withSchedule(SimpleScheduleBuilder
//                                .repeatSecondlyForever(1).withIntervalInSeconds(jobTime).withRepeatCount(jobTimes))
//                        .startNow().build();
//            }
//            scheduler.scheduleJob(jobDetail, trigger);
//            if (!scheduler.isShutdown()) {
//                //添加定时任务后直接启动定时任务
//                scheduler.start();
//            }
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
//    }

}
