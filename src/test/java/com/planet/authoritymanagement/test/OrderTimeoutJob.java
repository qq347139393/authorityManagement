package com.planet.authoritymanagement.test;

import com.planet.util.springBoot.SpringBootQuartzManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 * 动态定时任务的模板类
 * @author father
 *
 */
//1）单独创建一个封装要定时执行一段特定的业务代码的“动态定时任务模板”类
public class OrderTimeoutJob extends QuartzJobBean {//2）让该“动态定时任务模板”类继承QuartzJobBean类
	@Autowired
	private SpringBootQuartzManager qm;

	//3）让该“动态定时任务模板”类重写QuartzJobBean父类的executeInternal方法
	/**
	 * 定时任务要执行的具体业务代码
	 */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    	//要定时执行一段特定的业务代码
        System.out.println("武器强化失败");
        String id=jobExecutionContext.getFireInstanceId();
        System.out.println("id="+id);
        try {
			System.out.println(jobExecutionContext.getScheduler().getSchedulerName());
			System.out.println(123);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        qm.deleteJob("job1", "group1");
    }
}
