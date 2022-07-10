package com.planet.system.fieldsRepeatCheck;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.planet.common.base.BaseEntity;
import com.planet.util.jdk8.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FieldsRepeatCheckUtil {

    private static final String DEFAULT_METHOD="selects";

    public static final int INSERT=0;

    public static final int UPDATE=1;


    public static <T> List<FieldsRepeatCheckResult<T>> fieldsRepeatChecks(BaseMapper baseMapper, String executeMethod, List<T> ts, List<String> fieldNames, Integer model){
        return ts.stream().map(t->{
            try {
                FieldsRepeatCheckResult<T> fieldsRepeatChecks = fieldsRepeatCheck(baseMapper, executeMethod, t, fieldNames, model);
                if(t==null){//字段重复性校验异常
                    throw new RuntimeException("字段重复性校验异常..");
                }
                return fieldsRepeatChecks;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("字段重复性校验异常..");
            }
        }).collect(Collectors.toList());
    }


    /**
     * 指定字段的重复性校验
     * @param baseMapper
     * @param executeMethod
     * @param t
     * @param fieldNames
     * @param model
     * @param <T>
     * @return
     */
    public static  <T> FieldsRepeatCheckResult<T> fieldsRepeatCheck(BaseMapper baseMapper, String executeMethod, T t, List<String> fieldNames, Integer model) {
        if(baseMapper==null||t==null||fieldNames==null||fieldNames.size()<=0){
            throw new RuntimeException("参数传入错误,参数不能为空..");
        }
        if(StrUtil.isEmpty(executeMethod)){
            executeMethod=DEFAULT_METHOD;//设置默认值:使用selects方法进行重复性判断
        }
        if(model==null){
            model=INSERT;//设置默认值:按照新增模式来处理
        }
        boolean flag=false;
        Map<String,Object> errorMap=new LinkedHashMap<>();
        try{
            //对每个不可重复的字段进行检测,如果都没有重复才证明可以执行
            for (String fieldName: fieldNames) {
                //1.获取对应的属性值
                Object fieldValue=ReflectUtil.getFieldValue(fieldName,t);
                //2.获取对应的属性类型
//            Class fieldClass=ReflectUtil.getFieldClass(fieldName,t.getClass());
                //3.创建对应t类型的新的实例对象
//                List<String> fNames=new ArrayList<>();
//                fNames.add(fieldName);
//                List<Object> fValues=new ArrayList<>();
//                fValues.add(fieldValue);

                QueryWrapper<T> queryWrapper =new QueryWrapper<>();
                queryWrapper.eq(fieldName,fieldValue);
                //4.填充参数,执行指定的方法
                List<Class> classes=new ArrayList<>();
                classes.add(Wrapper.class);//这里要有方法真实拥有者的类,也就是QueryWrapper的父类
                List<Object> objects=new ArrayList<>();
                objects.add(queryWrapper);//这里用的是QueryWrapper
                Object methodResult=ReflectUtil.executeMethod(baseMapper,executeMethod,classes,objects);
                if(methodResult==null||((List)methodResult).size()<=0){//此字段肯定不重复
                    continue;
                }
                if(INSERT==model){//新增模式
                    flag=true;
                    errorMap.put(fieldName,fieldValue);
                    continue;
                }else if(UPDATE==model){//修改模式
                    List methodResults=(List)methodResult;
                    for (Object obj : methodResults) {
                        //目前的查询方法,其入参和返回值类型是一样的,只不过返回值是List的集合
                        T tObj=(T)obj;
                        Long tId=Long.valueOf(String.valueOf(ReflectUtil.getFieldValue("id",tObj)));//前提是这个表有一个叫id的字段
                        Long id=Long.valueOf(String.valueOf(ReflectUtil.getFieldValue("id",t)));
                        if(!tId.equals(id)){//如果id不一致,则说明已经存在了另一条记录占用了这个字段的值
                            flag=true;//出现重复字段,停止校验
                            errorMap.put(fieldName,fieldValue);
                            break;
                        }
                    }
                }else{
                    throw new RuntimeException("约定方法名输入错误..");
                }
            }
            FieldsRepeatCheckResult result=new FieldsRepeatCheckResult();
            result.setResult(flag);
            result.setErrorMap(errorMap);
            result.setData(t);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("字段重复性校验异常..");
        }

    }

}
