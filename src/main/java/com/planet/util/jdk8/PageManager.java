package com.planet.util.jdk8;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页处理器
 * @author planet
 *
 * @param <T>
 */
@Getter
public class PageManager<T> {

	public static final long SIZE=20;
	/** 当前页 */
	private Long current;
	/** 每页条数 */
	private Long size;
	/** 起始条序 */
	private Long start;
	/** 结尾条序 */
	private Long end;
	/** 总条目数 */
	private Long total;
	/** 总页数 */
	private Long pages;
	/** 分段记录 */
	private List<T> records;

	//给current和size,计算出能计算的余下属性值并存入当前创建的对象中..这样想用的时候直接拿即可
	public PageManager(Long current,Long size){
		if(size==null||size<=0){
			size=SIZE;
		}
		if(current==null||current<=0){
			current=1l;
		}
		this.current=current;
		this.size=size;
		this.start=(current-1)*size;
		this.end=current*size-1;
	}

	public PageManager(Long current,Long size,Long total){
		this(current,size);
		if(total==null||total<=0){
			total=0l;
		}
		this.pages=total%this.size==0?total/this.size:total/this.size+1;
	}

	//进行伪分页的
	public PageManager(Long current,Long size,List<T> allRecords){
		this(current,size);
		if(allRecords==null){
			allRecords=new ArrayList<>();
		}
		this.total=allRecords.size()+0l;
		this.pages=total%this.size==0?total/this.size:total/this.size+1;
		this.records=allRecords.stream().skip(end).limit(this.size).collect(Collectors.toList());
	}




}
