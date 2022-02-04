package com.planet.module.authManage.dao.redis;

/**
 * redis缓存的crud工具类
 */
public interface BaseMapper {
    /**
     * @Description 创建缓存
     * @param cacheName 缓存名称
     * @param  cacheObj 缓存对象
     * @param  millisecond 存活时间
     * @return
     */
    void creatCache(String cacheName, Object cacheObj, Long millisecond);

    /**
     * @Description 获得缓存
     * @param cacheName 缓存名称
     * @return 缓存对象
     */
    Object getCache(String cacheName);

    /**
     * @Description 删除缓存
     * @param cacheName 缓存名称
     * @return
     */
    void removeCache(String cacheName);

    /**
     * @Description 更新缓存
     * @param cacheName 缓存名称
     * @param  cacheObj 新的缓存对象
     * @param  millisecond 存活时间
     * @return
     */
    void updateCache(String cacheName,Object cacheObj, Long millisecond);
}
