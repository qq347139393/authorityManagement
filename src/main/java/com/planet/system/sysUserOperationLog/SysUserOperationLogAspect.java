package com.planet.system.sysUserOperationLog;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.planet.common.base.BaseEntity;
import com.planet.common.constant.ComponentConstant;
import com.planet.module.authManage.dao.mysql.sysUserOperationLogMapper.SysUserOperationLogDao;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.AccountModuleService;
import com.planet.system.sysUserOperationLog.enumeration.MethodType;
import com.planet.util.jdk8.StrUtils.StrConvertUtil;
import com.planet.util.shiro.ShiroUtil;
import com.planet.util.springBoot.WebUtil;
import com.planet.system.sysUserOperationLog.annotation.SysUserOperationMethodLog;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@Aspect
@Slf4j
@Order(value = 100)//设置切面类之间的优先级,值越低优先级越高
@Data
public class SysUserOperationLogAspect {
    @Autowired
    private SysUserOperationLogDao sysUserOperationLogDao;
    @Autowired
    private AccountModuleService accountModuleService;

    /**
     * Pointcut 切入点
     * 这里可以直接写在下面的@Around的value中..但这里这样写是为了提高复用性
     */
    @Pointcut("@annotation(com.planet.system.sysUserOperationLog.annotation.SysUserOperationMethodLog)")
    public void sysUserOperationLogPointcut(){}

    /**
     * 环绕通知
     * 环绕通知可以同时替代其他四种类型的通知
     */
    @Around(value = "sysUserOperationLogPointcut()")
    public Object around(ProceedingJoinPoint pjp) {
        try {
            //执行业务方法之前插入的代码
            //1.获取目标方法的注解
            Object[] args = pjp.getArgs();
            Class<?>[] argClasses = Arrays.stream(args).sequential().map(arg -> {
                Class<?> aClass = arg.getClass();
                if(aClass==ArrayList.class){//如果是ArrayList,就要向上造型成父级List:因为这个才是我们业务方法的入参类型
                    return List.class;
                }
                return aClass;
            }).toArray(Class<?>[]::new);

            Method method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(), argClasses);
            SysUserOperationMethodLog annotation = method.getAnnotation(SysUserOperationMethodLog.class);
            //2.获取方法对象所属类的springBean对象
            String classSimpleName = method.getDeclaringClass().getSimpleName();
            //根据本项目的标准类名与标准表名的对应关系,来通过类名动态构建对应的Dao名
            classSimpleName=classSimpleName.replace("ServiceImpl","Mapper");
            String infoSpringBeanName=classSimpleName.substring(0,1).toLowerCase()+classSimpleName.substring(1);
            //通过springBean名称获取对应的bean对象
            Object infoSpringBean = SpringUtil.getBean(infoSpringBeanName);
            List infoList=new ArrayList();
            List<Long> ids=new ArrayList<>();
            //3.判断是否是delete:如果是delete,则要在执行前拿List<Long>中的ids,然后查出主体name
            if (annotation.MethodType().equals(MethodType.DELETE)) {
                switch (annotation.parameterType()) {
                    case List://找当前方法的List类型的入参
                        for (Object arg : args) {
                            if(arg instanceof List){
                                //获取List的泛型,判断泛型是否是Long:如果是,这才是我们需要的参数..具体的做法是拿出里面的第一个元素来判断类型是否为Long
                                Object element = ((List) arg).get(0);
                                if(element instanceof Long){//说明就是我们要找的参数
                                    ids=(List)arg;
                                    break;
                                }
                            }
                        }
                        break;
                    case Long://找当前方法的Long类型的入参
                        for (Object arg : args) {
                            if(arg instanceof Long){
                                ids.add((Long)arg);
                            }
                        }
                        break;
                    default://delete是根据id删除的,所以不会匹配BaseEntity类型
                        throw new RuntimeException("没有匹配到合适的参数类型..");
                }
                if (annotation.MethodType().equals(MethodType.DELETE)) {
                    //删除要在执行前查出主体name,等执行成功后再正式持久化
                    infoList = sysUserOperationLogDao.selectNamesByIds(ids, infoSpringBean);
                }
            }

            Object o =  pjp.proceed();//执行正常的业务代码
            //执行完业务方法之后插入的代码
            //3.判断是否是insert或update:如果是则拿List<T>中的ids,然后查出主体name
            if (annotation.MethodType().equals(MethodType.INSERT)||
                    annotation.MethodType().equals(MethodType.UPDATE)) {
                switch (annotation.parameterType()) {
                    case List://找当前方法的List类型的入参
                        for (Object arg : args) {
                            if(arg instanceof List){
                                //判断当前List的泛型类型是否是我们需要的实体类的类型:我们拿出第一个元素来判断类型来进行判断
                                Object element = ((List) arg).get(0);
                                //我们的实体类,都会继承BaseEntity基类..可以利用这一点来判断
                                if(element instanceof BaseEntity){//说明正是我们找的实体类
                                    List entityList=(List) arg;
                                    for (Object entity : entityList) {
                                        Field id = entity.getClass().getDeclaredField("id");
                                        id.setAccessible(true);
                                        ids.add((Long)id.get(entity));
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    case BaseEntity://找当前方法的实体类类型的入参
                        for (Object arg : args) {
                            if(arg instanceof BaseEntity){
                                Field id = arg.getClass().getDeclaredField("id");
                                id.setAccessible(true);
                                ids.add((Long)id.get(arg));
                            }
                        }
                        break;
                    default://insert和update是根据对象的属性来操作的,所以不会匹配Long类型
                        throw new RuntimeException("没有匹配到合适的参数类型..");
                }
                //新增和修改要在执行后查出主体name
                infoList = sysUserOperationLogDao.selectNamesByIds(ids, infoSpringBean);
            }

            //4.进行log表的插入操作
            //1)通过pring获取对应的Dao的bean对象
            //根据本项目的标准类名与标准表名的对应关系,来通过类名动态构建对应的Dao名
            String logSpringBeanName = classSimpleName.replace("InfoMapper", "LogServiceImpl");
            logSpringBeanName=logSpringBeanName.substring(0,1).toLowerCase()+logSpringBeanName.substring(1);
            //通过springBean名称获取对应的bean对象
            Object logSpringBean = SpringUtil.getBean(logSpringBeanName);
            //2)获取相应的属性值:跟当前项目的具体要求绑定,已经设定好的
            String methodName=method.getName();
            StringBuilder sb=new StringBuilder();
            Arrays.stream(args).sequential().forEach(arg->{
                arg=argHandle(arg);//定制化处理的方法
                sb.append("(");
                if(arg instanceof List){
                    List list=(List)arg;
                    list.stream().forEach(l->{
                        sb.append("[").append(l).append("]");
                    });
                    sb.append(")");
                }else{
                    sb.append(arg).append(")");
                }
            });
            String content=sb.toString();
//            String id = WebUtil.getSession().getId();
            //a1:获得shiro本地缓存中的当前用户对象
            Object principal = ShiroUtil.getPrincipal();
            //a2:获取当前用户对象的userId
            Long operatorId = JSONUtil.parseObj(principal).get("id",Long.class);
            String operatorName = JSONUtil.parseObj(principal).get("name",String.class);
            //3)通过反射创建实体类对象,并为实体类对象的属性赋值
            List logList=new ArrayList();
            for (Object info : infoList) {
                //获取info的id和name,作为主体的id和name
                Field idFiled = info.getClass().getDeclaredField("id");
                idFiled.setAccessible(true);
                Field nameFiled = info.getClass().getDeclaredField("name");
                nameFiled.setAccessible(true);

                //构建log实体对象
                String logName = info.getClass().getSimpleName().replace("Info", "Log");
                String canonicalName=info.getClass().getCanonicalName();
                canonicalName=canonicalName.substring(0,canonicalName.lastIndexOf("."))+"."+logName;
                Class<?> logClass = Class.forName(canonicalName);
                Object logObj = logClass.newInstance();

                //为log实体对象设置属性值
                String logClassName = logClass.getSimpleName().replace("Info", "");
                logClassName=logClassName.substring(0,1).toLowerCase()+logClassName.substring(1);
                Field subjectIdFiled = logObj.getClass().getDeclaredField(logClassName.replace("Log","") + "Id");
                subjectIdFiled.setAccessible(true);
                subjectIdFiled.set(logObj,idFiled.get(info));
                Field subjectNameFiled = logObj.getClass().getDeclaredField("name");
                subjectNameFiled.setAccessible(true);
                subjectNameFiled.set(logObj,nameFiled.get(info));

                Field operatorIdFiled = logObj.getClass().getDeclaredField("operatorId");
                operatorIdFiled.setAccessible(true);
                operatorIdFiled.set(logObj,operatorId);
                Field operatorNameFiled = logObj.getClass().getDeclaredField("operatorName");
                operatorNameFiled.setAccessible(true);
                operatorNameFiled.set(logObj,operatorName);

                Field methodField = logObj.getClass().getDeclaredField("method");
                methodField.setAccessible(true);
                methodField.set(logObj,methodName);
                Field contentField = logObj.getClass().getDeclaredField("content");
                contentField.setAccessible(true);
                contentField.set(logObj,content);

                Field creatorField = logObj.getClass().getDeclaredField("creator");
                creatorField.setAccessible(true);
                creatorField.set(logObj,operatorName);

                logList.add(logObj);
            }

            boolean b = sysUserOperationLogDao.saveBatch(logList, logSpringBean, ComponentConstant.DAO_BATCH_SIZE);
            if(b){
                log.info("记录用户操作成功..");
            }else{
                log.error("记录用户操作失败..");
            }
            return o;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("记录用户操作记录时出现异常..");
        }
    }


    /**
     * 对参数进行定制化处理的方法
     * @param arg
     * @return
     */
    private Object argHandle(Object arg){
        if(arg instanceof List){
            List list=(List)arg;
            Object o = list.get(0);
            if(o instanceof UserInfo){
                try {
                    for (Object l : list) {
                        Field password = l.getClass().getDeclaredField("password");
                        password.setAccessible(true);
                        password.set(l,null);
                        Field salt = l.getClass().getDeclaredField("salt");
                        salt.setAccessible(true);
                        salt.set(l,null);
                    }
                    return list;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("对参数进行定制化处理失败..");
                }
            }
        }
        return arg;
    }




}
