package com.planetProvide.easyExcelPlus.core.baseReadListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.planetProvide.easyExcelPlus.core.baseConvert.BaseExcelToPoConverter;
import com.planetProvide.easyExcelPlus.core.baseDao.BaseDao;
import com.planetProvide.easyExcelPlus.core.baseExcelImportValid.BaseExcelImportValid;
import com.planetProvide.easyExcelPlus.core.ExcelException.ExcelInsertsException;
import com.planetProvide.easyExcelPlus.core.constant.DefaultConstant;
import com.planetProvide.easyExcelPlus.core.entity.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class BaseRowReadListener<T> extends AnalysisEventListener<T> {

    public long readBatchCount;

    private BaseExcelImportValid<T> baseExcelImportValid;

    private List<T> rows;

    private BaseDao baseDao;

    private List<T> unqualifiedRows;

    private int resultCode;

    private long rowsSum;

    private Result<T> result;

    private BaseExcelToPoConverter<T> baseExcelToPoConverter;

    //用户可以自定义的部分
    private String daoFullClassName;

    private String daoBeanName;

    private String insertsMethodName;

    private Class<?>[] parameterTypes;

    private String getRowCode;

    private String setRowOrder;

    private int unqualifiedRowCode;

    //默认无参构造器
    public BaseRowReadListener() {
//        this.baseExcelImportValid=new BaseExcelImportValid<T>();由springboot来注入
        this.rows=new ArrayList<T>();
//        this.baseDao=new BaseDao<T>();由springboot来注入
        this.unqualifiedRows=new ArrayList<T>();
//        this.resultCode=0;//默认就是0
//        this.rowsSum=0l;//默认就是0
        this.result=new Result<T>();
    }

    //给用户继承重写的
    public BaseRowReadListener(Long readBatchCount, BaseExcelImportValid<T> baseExcelImportValid,
                               BaseDao baseDao,BaseExcelToPoConverter baseExcelToPoConverter) {
        this();
        this.readBatchCount = readBatchCount;
        this.baseExcelImportValid = baseExcelImportValid;
        this.baseDao = baseDao;
        this.baseExcelToPoConverter=baseExcelToPoConverter;
    }

//    @Transactional
    @Override
    public void invoke(T row, AnalysisContext context) {
        //通用方法数据校验:注解校验
        try {
            baseExcelImportValid.annotationValid(row);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ExcelInsertsException("获取字段的值异常,事务回滚并中断当前方法的执行");
        }

        //自定义额外的校验方式:该方法要让程序员用户自己去继承重写的:这种是不写注解的方式来实现的行级校验规则
        baseExcelImportValid.extraValid(row);

        int rowCode = 0;
        try {
            rowCode = (int) row.getClass().getMethod((getRowCode==null||"".equals(getRowCode)?DefaultConstant.DEFAULT_GET_ROW_CODE:getRowCode)).invoke(row);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(rowCode==(unqualifiedRowCode==0?DefaultConstant.DEFAULT_UNQUALIFIED_ROW_CODE:unqualifiedRowCode)){//说明经过校验后当前记录确实存在不合格的地方
            unqualifiedRows.add(row);
        }

        //将解析完的数据加入到list中
        rows.add(row);
        rowsSum++;//后续能够通过这个值和readBatchCount值在分段持久化的情况下计算出出现错误的那段记录所在的范围区间

        try {
            row.getClass().getMethod((setRowOrder==null||"".equals(setRowOrder))?DefaultConstant.DEFAULT_SET_ROW_ORDER:setRowOrder, long.class).invoke(row,rowsSum);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
//        row.setRowOrder(rowsSum);//当前记录的行排序

        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (rows.size() >= readBatchCount) {
            //这里也可以自定义每次操作的数量:如果想一次性全部导入(有的时候为了进行全数据的列校验要这样设置才行),
            //就将这个readBatchCount设置成跟记录数量一致即可,当然前端需要传一个表示当前记录总数的参数

            //增加列级校验方式:比如列的某个字段的名称不能重复这种条件:目前只限制在一个范围内进行比对(如果不这样,那么readBatchCount分段来降低运行内容的意义就没了)
            baseExcelImportValid.rowsValid(rows,unqualifiedRows);
            //只要unqualifiedRows中有记录,则本次的持久化操作一定失败..所以这里要在unqualifiedRows有记录时进行屏蔽持久化操作
            if(unqualifiedRows==null||unqualifiedRows.size()==0) {
                try {
                    baseDao.inserts(rows,daoFullClassName,daoBeanName,insertsMethodName,parameterTypes,baseExcelToPoConverter);//如果持久化失败,会自动进行事务回滚并中断此方法的执行
                }catch (Exception e){
                    e.printStackTrace();
                    throw new ExcelInsertsException("持久化异常,事务回滚并中断当前方法的执行");
                }
            }//大于0说明存在不合格的记录,所以不进行持久化方法..但是要继续进行校验工作
            rows.clear();
        }

    }

    //excel文件的全部记录读取完毕后,执行此方法
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //最后一次或者用户文档中的记录数根本到不了我们设的缓存最大值时,我们要最后一次性进行持久化
        //增加列级校验方式:比如列的某个字段的名称不能重复这种条件:目前只限制在一个范围内进行比对(如果不这样,那么readBatchCount分段来降低运行内容的意义就没了)
        baseExcelImportValid.rowsValid(rows,unqualifiedRows);
        //只要unqualifiedRows中有记录,则本次的持久化操作一定失败..所以这里要在unqualifiedRows有记录时进行屏蔽持久化操作
        if(unqualifiedRows==null||unqualifiedRows.size()==0) {
            try {
                baseDao.inserts(rows,daoFullClassName,daoBeanName,insertsMethodName,parameterTypes,baseExcelToPoConverter);//如果持久化失败,会自动进行事务回滚并中断此方法的执行
            }catch (Exception e){
                e.printStackTrace();
                throw new ExcelInsertsException("持久化异常,事务回滚并中断当前方法的执行");
            }
        }//大于0说明存在不合格的记录,所以不进行持久化方法..但是要继续进行校验工作
        rows.clear();


//        recoverEmptyRow(analysisContext.getCurrentRowNum() - 1, rows.size());
        if(unqualifiedRows==null||unqualifiedRows.size()==0){//说明未发现不合格的记录
            resultCode=0;//表示成功
        }else{//校验过程中发现不合格的记录
            resultCode=1;//表示校验记录不合格导致的失败
        }
        //封装最终的执行结果
        result.setResultCode(resultCode);
        result.setUnqualifiedRows(unqualifiedRows);
        result.setStart(0l);
        result.setEnd(rowsSum);
    }

    //出现异常后,要构建结果值:这里可能并没有完全将导入的excel文件中的全部记录都校验过,只是到出现持久化异常时的那些记录进行了校验;
    // 并且回滚也只是回滚那一段的数据,而之前符合条件的记录已经全部持久化了..所以需要告知用户,在出现异常时是在某一段到某一段中的记录的问题,而之前的记录已经持久化了
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if(exception instanceof ExcelInsertsException){//持久化时出现的异常
            resultCode=-1;//表示执行持久化异常而非记录不合格导致的失败
            //封装最终的执行结果
            result.setResultCode(resultCode);
            result.setUnqualifiedRows(unqualifiedRows);
            result.setStart(rowsSum-readBatchCount);//只要能执行持久化操作,说明在之前的记录肯定都是符合检测规则的..所以出问题的就是在这一段记录上
            result.setEnd(rowsSum);
        }else{
            resultCode=-2;//未知异常
            //封装最终的执行结果
            result.setResultCode(resultCode);
            result.setUnqualifiedRows(unqualifiedRows);
            result.setStart(0l);
            result.setEnd(rowsSum);
        }
        super.onException(exception, context);
    }


}
