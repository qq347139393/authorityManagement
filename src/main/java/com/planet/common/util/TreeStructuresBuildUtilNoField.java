package com.planet.common.util;

import com.planet.common.base.BaseTreeStructuresEntityNoField;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TreeStructuresBuildUtilNoField<T extends BaseTreeStructuresEntityNoField> {
    /**
     * 构造一棵树(后续可能需要改进)
     * @param data
     * @return
     */
    public List<T> buildTree(List<T> data){
        HashMap<Long,T> map = new HashMap<>(data.size());
        for (T e : data) {
            map.put(e.getOwnId(),e);
        }
        List list =  new LinkedList<>();

        for (T e : data) {
            Long parentId =  e.getParentId();
            T parent = map.get(parentId);

            if(parent == null){
                list.add(e);
            }else{
                List<T> children =  parent.getChildren();
                if(children == null){
                    children = new LinkedList<>();
                    parent.setChildren(children);
                }
                children.add(e);
            }
        }
        return list;
    }
}
