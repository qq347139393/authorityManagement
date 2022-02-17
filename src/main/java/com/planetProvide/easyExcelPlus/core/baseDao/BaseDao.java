package com.planetProvide.easyExcelPlus.core.baseDao;

import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Data
@Slf4j
public class BaseDao<T> implements ApplicationContextAware {
    /**
     * 上下文对象实例
     */
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    /**
     * 获取applicationContext
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /** 执行持久化操作的类名对应实体类的后缀 */
    private String serviceClassNameSuffix;
    /** 执行持久化操作的类名的完整前缀 */
    private String serviceClassFullNamePrefix;
    /** 选择是否基于spring创建对象的模式,还是自己new的模式 */
    private int model;
    /** 进行持久化操作的方法名 */
    private String insertsMethod;

    public BaseDao() {
    }

    //这里要支持继承重写,以便可以让用户自定义实现导入的数据的持久化的具体逻辑:不重写就用这个,重写了就用子类自己定义的
    public int inserts(List<T> rows, String daoFullClassName, String daoBeanName, String insertsMethodName, Class<?>[] parameterTypes
        , BaseExcelToPoConverter<T> baseExcelToPoConverter){//上层已经使用了事务控制

//        for (int i = 0; i < rows.size(); i++) {
//            System.out.println("第"+rows.get(i).getOrder()+"条记录:"+rows.get(i));
//        }
        //1.通过T对象来获取T的类名
        if(rows==null||rows.size()==0){
            return 0;
        }

        try {
            Class<?> tClass = rows.get(0).getClass();
            //2.判断采用spring管理对象的模式,还是自己new对象的模式..创建持久化操作的对象
            Object daoClassBean =null;
            //3.构建执行持久化操作的类名或对象
            String simpleName = tClass.getSimpleName();
            if(daoFullClassName==null||"".equals(daoFullClassName)){
                daoFullClassName=serviceClassFullNamePrefix+simpleName+serviceClassNameSuffix;
            }
            Class daoClass = null;
            Method method = null;
            //4.构建持久化操作的方法:通常inserts方法都直接放List<实体类>的对象作为参数,所以这里我们统一设置成这样(如果用户并非这种,可以继承重写自定义)
            if(model==0){//自己new对象的模式
                daoClass=Class.forName(daoFullClassName);
                daoClassBean=daoClass.newInstance();
            }else if (model==1) {//spring模式
                simpleName=simpleName.substring(0,1).toLowerCase()+simpleName.substring(1);
                daoBeanName=(daoBeanName==null||"".equals(daoBeanName))?simpleName+serviceClassNameSuffix:daoBeanName;
                daoClassBean = getApplicationContext().getBean(daoBeanName);
                daoClass = daoClassBean.getClass();
            }
            insertsMethodName=(insertsMethodName==null||"".equals(insertsMethodName))?insertsMethod:insertsMethodName;
            //5.用指定的持久化对象执行指定的持久化方法
            //这里做的不好,后续需要优化:因为这里已经跟我们的具体项目的特征有过多的针对性配置
            if(parameterTypes==null){
                method=daoClass.getMethod(insertsMethodName,List.class);
                Object invoke = method.invoke(daoClassBean, baseExcelToPoConverter.convertExcelToPo(rows));
            }else{
                method=daoClass.getMethod(insertsMethodName,parameterTypes);
                boolean b = Arrays.stream(parameterTypes).anyMatch(parameterType -> parameterType == MultipartFile[].class);
                if(b){
                    Object invoke = method.invoke(daoClassBean, null,baseExcelToPoConverter.convertExcelToPo(rows));
                }else{
                    Object invoke = method.invoke(daoClassBean, baseExcelToPoConverter.convertExcelToPo(rows));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error("ClassNotFoundException异常");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            log.error("InvocationTargetException异常");
        } catch (InstantiationException e) {
            e.printStackTrace();
            log.error("InstantiationException异常");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            log.error("IllegalAccessException异常");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            log.error("NoSuchMethodException异常");
        }

        return rows.size();
    }

//    public Object converter

}
