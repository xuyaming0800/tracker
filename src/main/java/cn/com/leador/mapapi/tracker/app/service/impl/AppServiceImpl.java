package cn.com.leador.mapapi.tracker.app.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.app.bean.AppServiceBean;
import cn.com.leador.mapapi.tracker.app.service.AppService;
import cn.com.leador.mapapi.tracker.component.MongoDBUtilComponent;
import cn.com.leador.mapapi.tracker.component.RedisUtilComponent;
import cn.com.leador.mapapi.tracker.constants.TrackerConstants;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

@Service
public class AppServiceImpl implements AppService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private RedisUtilComponent redisUtilComponent;

	@Override
	public ResultBean<AppServiceBean> addAppService(AppServiceBean bean)
			throws BusinessException {
		Jedis jedis = null;
		ResultBean<AppServiceBean> result = new ResultBean<AppServiceBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			// 锁定名称
			redisUtilComponent.lockIdByRedis(TrackerConstants.APP_NAME_PREFIX,
					bean.getAk() + bean.getName(),
					TrackerExceptionEnum.REDIS_APP_NAME_DUP, 5, jedis);
			// 判断名称是否存在
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("ak", bean.getAk());
			queryMap.put("name", bean.getName());
			boolean check = mongoDBUtilComponent.checkObjectIsExist("app_info",
					queryMap);
			if (check) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_NAME_DUP);
				throw ex;
			}
			String maxIdStr = redisUtilComponent.getRedisStringCache(
					TrackerConstants.MAX_APP_ID_KEY, jedis);
			if (maxIdStr == null) {
				// 初始化错误 会有自动机制对数
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_NO_INIT);
				throw ex;
			}
			// 获取app的最大ID
			Long maxId = redisUtilComponent.increament(
					TrackerConstants.MAX_APP_ID_KEY, jedis);
			bean.setId(maxId.toString());
			// 检测ID是否存在
			queryMap.clear();
			queryMap.put("id", maxId);
			check = mongoDBUtilComponent.checkObjectIsExist("app_info",
					queryMap);
			if (check) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_DUP);
				throw ex;
			}
			// 更新最大值
			queryMap.clear();
			Map<String, Long> updateMap = new HashMap<String, Long>();
			queryMap.put("status", 0);
			updateMap.put("maxId", maxId);
			mongoDBUtilComponent.upsertCommonObject("app_max_id", queryMap,
					null, updateMap);
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 创建和更新时间
			Date date = new Date();
			bean.setCreateTime(date);
			bean.setUpdateTime(date);
			mongoDBUtilComponent.insertObject("app_info", binder.toJson(bean));
			// 释放名称锁
			redisUtilComponent.releaseIdByRedis(
					TrackerConstants.APP_NAME_PREFIX,
					bean.getAk() + bean.getName());
			// 封装结果
			result.setSuccessful(true);
			result.setResult(bean);
			return result;
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		} finally {
			redisUtilComponent.returnRedis(jedis);
		}
	}

}
