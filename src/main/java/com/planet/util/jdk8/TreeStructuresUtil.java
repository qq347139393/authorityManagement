package com.planet.util.jdk8;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class TreeStructuresUtil<T> {

    public static final String ID_NAME="id";

    public static final String PARENT_ID_NAME="parentId";

    public static final String CHILDREN_NAME="children";

    //******************构建树状结构集合相关******************
    public static <T, ID> List<T> buildTree(List<T> data){
        return buildTree(data,null,null,null);
    }

    public static <T, ID> List<T> buildTree(List<T> data, String idName){
        return buildTree(data,idName,null,null);
    }

    public static <T, ID> List<T> buildTree(List<T> data, String idName, String parentIdName){
        return buildTree(data,idName,parentIdName,null);
    }

    /**
     * 构造树状结构(核心)
     * 如果parentId为null或小于等于0,这里会作为顶级节点处理:因为parent一定找不到,所以一定会存入list的首层
     * @param data
     * @return
     */
    public static <T, ID> List<T> buildTree(List<T> data, String idName, String parentIdName, String childrenName){
        if(data==null||data.size()==0){
            log.error("传入的集合不能为空");
            return null;
        }
        if(idName==null||"".equals(idName)){
            idName=ID_NAME;
        }
        if(parentIdName==null||"".equals(parentIdName)){
            parentIdName=PARENT_ID_NAME;
        }
        if(childrenName==null||"".equals(childrenName)){
            childrenName=CHILDREN_NAME;
        }
        try {
            HashMap<ID,T> map = new HashMap<>(data.size());
            Class<?> tClass = data.get(0).getClass();
            Field idField = tClass.getDeclaredField(idName);
            idField.setAccessible(true);
            for (T t : data) {
                Object id = idField.get(t);
                map.put((ID)id,t);
            }
            List<T> list =  new LinkedList<>();

            Field parentIdField = tClass.getDeclaredField(parentIdName);
            parentIdField.setAccessible(true);
            Field childrenField = tClass.getDeclaredField(childrenName);
            childrenField.setAccessible(true);
            for (T t : data) {
                ID parentId = (ID)parentIdField.get(t);
                T parent = map.get(parentId);
                if(parent == null){
                    list.add(t);
                }else{
                    List<T> children = (List<T>)childrenField.get(parent);
                    if(children == null){
                        children = new LinkedList<>();
                    }
                    children.add(t);
                    childrenField.set(parent,children);
                }
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            log.error("构造树状结构出现异常,构建失败..");
            return null;
        }
    }

    //******************获取父对象相关******************
    //1.判断是否有指定层级的父元素
    public static <T,ID> boolean hasParent(ID parentId,List<T> data){
        List<T> parents = getParents(parentId, data, 0, true, null, null);
        if(parents==null||parents.size()==0){//未能找到
            return false;
        }
        return true;
    }
    public static <T,ID> boolean hasParent(ID parentId,List<T> data,int level){
        List<T> parents = getParents(parentId, data, level, true, null, null);
        if(parents==null||parents.size()==0){//未能找到
            return false;
        }
        return true;
    }
    public static <T,ID> boolean hasParent(ID parentId,List<T> data,int level,String idName){
        List<T> parents = getParents(parentId, data, level, true, idName, null);
        if(parents==null||parents.size()==0){//未能找到
            return false;
        }
        return true;
    }
    public static <T,ID> boolean hasParent(ID parentId,List<T> data,int level,
                                     String idName, String parentIdName){
        List<T> parents = getParents(parentId, data, level, true, idName, parentIdName);
        if(parents==null||parents.size()==0){//未能找到
            return false;
        }
        return true;
    }
    //2.获取指定层级的那个父元素
    public static <T,ID> T getParent(ID parentId,List<T> data){
        List<T> parents = getParents(parentId, data, 0, false, null, null);
        if(parents==null){//未能找到
            return null;
        }
        //最后一个元素一定是最顶级的父对象:有可能为null,因为size可能为0
        return parents.get(parents.size());
    }
    public static <T,ID> T getParent(ID parentId,List<T> data,int level){
        List<T> parents = getParents(parentId, data, level, false, null, null);
        if(parents==null){//未能找到
            return null;
        }
        //最后一个元素一定是最顶级的父对象:有可能为null,因为size可能为0
        return parents.get(parents.size());
    }
    public static <T,ID> T getParent(ID parentId,List<T> data,int level,boolean flag){
        List<T> parents = getParents(parentId, data, level, flag, null, null);
        if(parents==null){//未能找到
            return null;
        }
        //最后一个元素一定是最顶级的父对象:有可能为null,因为size可能为0
        return parents.get(parents.size());
    }
    public static <T,ID> T getParent(ID parentId,List<T> data,int level,boolean flag,String idName){
        List<T> parents = getParents(parentId, data, level, flag, idName, null);
        if(parents==null){//未能找到
            return null;
        }
        //最后一个元素一定是最顶级的父对象:有可能为null,因为size可能为0
        return parents.get(parents.size());
    }
    /**
     * 根据id在给定的集合中查找指定层级的父对象
     * @param parentId
     * @param data
     * @param level 值为0时表示查找到最顶级父对象
     * @param flag 指定层级找不到的话,是否不返回其最近的下级父对象:false,返回能查到的最高级的父对象;true,直接返回null
     * @param idName
     * @param parentIdName
     * @param <T>
     * @param <ID>
     * @return
     */
    public static <T,ID> T getParent(ID parentId,List<T> data,int level,boolean flag,
                                     String idName, String parentIdName){
        List<T> parents = getParents(parentId, data, level, flag, idName, parentIdName);
        if(parents==null){//未能找到
            return null;
        }
        //最后一个元素一定是最顶级的父对象:有可能为null,因为size可能为0
        return parents.get(parents.size());
    }
    //3.获取到指定层级的全部父级元素
    public static <T,ID> List<T> getParents(ID parentId,List<T> data){
        return getParents(parentId, data, 0, false, null, null);
    }
    public static <T,ID> List<T> getParents(ID parentId,List<T> data,int level){
        return getParents(parentId, data, level, false, null, null);
    }
    public static <T,ID> List<T> getParents(ID parentId,List<T> data,int level,boolean flag){
        return getParents(parentId, data, level, flag, null, null);
    }
    public static <T,ID> List<T> getParents(ID parentId,List<T> data,int level,boolean flag,String idName){
        return getParents(parentId, data, level, flag, idName, null);
    }
    /**
     * 根据id在给定的集合中查找指定层级的全部父对象(核心)
     * 每一级的父对象只会有一个,如果本身的data数据有问题则方法可以会出现错误
     * @param parentId
     * @param data
     * @param level 值为0时表示查找到最顶级父对象
     * @param flag 指定层级找不到的话,是否不返回其最近的下级父对象:false,返回能查到的最高级的父对象;true,直接返回null
     * @param <T>
     * @param <ID>
     * @return
     */
    public static <T,ID> List<T> getParents(ID parentId,List<T> data,int level,boolean flag,
                                            String idName, String parentIdName){
        if(data==null||data.size()==0){
            throw new RuntimeException("传入的集合不能为空");
        }
        if(parentId==null){
            throw new RuntimeException("传入的parentId不能为空");
        }
        if(level<0){
            level=0;
        }
        if(idName==null||"".equals(idName)){
            idName=ID_NAME;
        }
        if(parentIdName==null||"".equals(parentIdName)){
            parentIdName=PARENT_ID_NAME;
        }
//        if(childrenName==null||"".equals(childrenName)){
//            childrenName=CHILDREN_NAME;
//        }
        try {
            Class<?> dataClass = data.get(0).getClass();
            Field idField = dataClass.getDeclaredField(idName);
            idField.setAccessible(true);
            Field parentIdField = dataClass.getDeclaredField(parentIdName);
            parentIdField.setAccessible(true);
//            Field childrenField = dataClass.getDeclaredField(childrenName);
//            childrenField.setAccessible(true);
            int time;
            if(level>0){
                time=level;
            }else{
                time=data.size();//最多就是这些,不会比这个次数更大
            }
            List<T> parents=new ArrayList<>();
            boolean b;
            for (int i = 0; i < time; i++) {
                b=false;
                for (T t : data) {
                    ID id = (ID)idField.get(t);
                    if(id.equals(parentId)){
                        parents.add(t);
                        //将t的parentId传给parentId,供后面接着找
                        parentId = (ID)parentIdField.get(t);
                        b=true;
                        break;
                    }
                }
                if(!b){//未能找到
                    break;
                }
            }
            if(flag){
                if(level==0){
                    return parents;
                }
                if(parents.size()==level){//看是否找到了指定层级的父对象
                    return parents;
                }
                return null;
            }
            return parents;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("根据id在给定的集合中查找指定层级的父对象出现异常..");
        }
    }

    ////******************获取子对象相关******************

    /**
     * 核心逻辑方法(核心)
     * 这块的其他方法都要使用此方法执行功能
     * @param id
     * @param data
     * @param level
     * @param flag
     * @param idName
     * @param parentIdName
     * @param childrenName
     * @param currentLevel
     * @param currentData
     * @param <T>
     * @param <ID>
     * @return
     */
    private static <T,ID> List<T> getChildrenInner(ID id,List<T> data,int level,boolean flag,
                                                   String idName, String parentIdName,String childrenName,int currentLevel,List<T> currentData){
        if(level<currentLevel&&level!=0){//阻止继续进行下去:level==0表示无限进行查,直到查完所有的子元素为止
            return null;
        }
        if(data==null||data.size()==0){
            throw new RuntimeException("传入的集合不能为空");
        }
        if(id==null){
            throw new RuntimeException("传入的id不能为空");
        }
        if(level<0){
            level=0;
        }
        if(idName==null||"".equals(idName)){
            idName=ID_NAME;
        }
        if(parentIdName==null||"".equals(parentIdName)){
            parentIdName=PARENT_ID_NAME;
        }
        if(childrenName==null||"".equals(childrenName)){
            childrenName=CHILDREN_NAME;
        }

        try {
            Class<?> dataClass = data.get(0).getClass();
            Field idField = dataClass.getDeclaredField(idName);
            idField.setAccessible(true);
            Field parentIdField = dataClass.getDeclaredField(parentIdName);
            parentIdField.setAccessible(true);
            Field childrenField = dataClass.getDeclaredField(childrenName);
            childrenField.setAccessible(true);
            for (T t : data) {
                ID parentId=(ID)parentIdField.get(t);
                if(parentId.equals(id)){//说明是子元素
                    if(flag){//flag为true,表示只取指定层的那一层的全部子元素
                        if(currentLevel==level){
                            currentData.add(t);
                        }
                    }else{//如果flag为false,则表示全部层的子元素都要
                        currentData.add(t);
                    }
//                    //将t的id传给id,供后面接着找子元素
//                    id = (ID)idField.get(t);
                    ID childId=(ID)idField.get(t);
                    currentLevel++;//当前层+1
                    currentData=getChildrenInner(childId,data,level,flag, idName,parentIdName,childrenName,currentLevel,currentData);
                }
            }
            return currentData;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("根据id在给定的集合中查找指定层级的父对象出现异常..");
        }
    }

    //1.获取到指定层级的全部子元素
    //1)以List返回
    public static <T,ID> List<T> getAllChildren(ID id,List<T> data){
        return getChildrenInner(id, data, 0, false, null, null, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getAllChildren(ID id,List<T> data,int level){
        return getChildrenInner(id, data, level, false, null, null, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getAllChildren(ID id,List<T> data,int level,String idName){
        return getChildrenInner(id, data, level, false, idName, null, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getAllChildren(ID id,List<T> data,int level,String idName, String parentIdName){
        return getChildrenInner(id, data, level, false, idName, parentIdName, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getAllChildren(ID id,List<T> data,int level,String idName, String parentIdName,String childrenName){
        return getChildrenInner(id, data, level, false, idName, parentIdName, childrenName, 0, new ArrayList<T>());
    }

    //2)以树状List返回

    //2.获取仅指定层级的那一级的子元素
    //1)以List返回
    public static <T,ID> List<T> getChildren(ID id,List<T> data){
        return getChildrenInner(id, data, 1, true, null, null, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getChildren(ID id,List<T> data,int level){
        level=level<=0?1:level;
        return getChildrenInner(id, data, level, true, null, null, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getChildren(ID id,List<T> data,int level,String idName){
        level=level<=0?1:level;
        return getChildrenInner(id, data, level, true, idName, null, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getChildren(ID id,List<T> data,int level,String idName, String parentIdName){
        level=level<=0?1:level;
        return getChildrenInner(id, data, level, true, idName, parentIdName, null, 0, new ArrayList<T>());
    }
    public static <T,ID> List<T> getChildren(ID id,List<T> data,int level,String idName, String parentIdName,String childrenName){
        level=level<=0?1:level;
        return getChildrenInner(id, data, level, true, idName, parentIdName, childrenName, 0, new ArrayList<T>());
    }

    //2)以树状List返回

    //3.判断是否有指定层级的子元素
    public static <T,ID> boolean hasChildren(ID id,List<T> data){
        List<T> childrenInner = getChildrenInner(id, data, 1, true, null, null, null, 0, new ArrayList<T>());
        if(childrenInner!=null&&childrenInner.size()>0){
            return true;
        }
        return false;
    }
    public static <T,ID> boolean hasChildren(ID id,List<T> data,int level){
        level=level<=0?1:level;
        List<T> childrenInner = getChildrenInner(id, data, level, true, null, null, null, 0, new ArrayList<T>());
        if(childrenInner!=null&&childrenInner.size()>0){
            return true;
        }
        return false;
    }
    public static <T,ID> boolean hasChildren(ID id,List<T> data,int level,String idName){
        level=level<=0?1:level;
        List<T> childrenInner = getChildrenInner(id, data, level, true, idName, null, null, 0, new ArrayList<T>());
        if(childrenInner!=null&&childrenInner.size()>0){
            return true;
        }
        return false;
    }
    public static <T,ID> boolean hasChildren(ID id,List<T> data,int level,String idName, String parentIdName){
        level=level<=0?1:level;
        List<T> childrenInner = getChildrenInner(id, data, level, true, idName, parentIdName, null, 0, new ArrayList<T>());
        if(childrenInner!=null&&childrenInner.size()>0){
            return true;
        }
        return false;
    }
    public static <T,ID> boolean hasChildren(ID id,List<T> data,int level,String idName, String parentIdName,String childrenName){
        level=level<=0?1:level;
        List<T> childrenInner = getChildrenInner(id, data, level, true, idName, parentIdName, childrenName, 0, new ArrayList<T>());
        if(childrenInner!=null&&childrenInner.size()>0){
            return true;
        }
        return false;
    }




}
