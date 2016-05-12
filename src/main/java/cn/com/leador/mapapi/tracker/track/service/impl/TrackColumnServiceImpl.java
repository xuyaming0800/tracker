package cn.com.leador.mapapi.tracker.track.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.component.MongoDBUtilComponent;
import cn.com.leador.mapapi.tracker.component.RedisUtilComponent;
import cn.com.leador.mapapi.tracker.constants.TrackerConstants;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.track.bean.TrackColumnBean;
import cn.com.leador.mapapi.tracker.track.service.TrackColumnService;

@Service
public class TrackColumnServiceImpl implements
		TrackColumnService<TrackColumnBean, TrackColumnBean> {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private RedisUtilComponent redisUtilComponent;

	@Value("${track.column-max-count}")
	private Integer columnMax;

	@Override
	public ResultBean<TrackColumnBean> addColumn(TrackColumnBean bean)
			throws BusinessException {
		Jedis jedis = null;
		ResultBean<TrackColumnBean> result = new ResultBean<TrackColumnBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);

			// 锁定SERVICE_ID
			redisUtilComponent.lockIdByRedis(
					TrackerConstants.TRACK_COLUMN_KEY_PREFIX,
					bean.getService_id(),
					TrackerExceptionEnum.REDIS_TRACK_COLUMN_DUP, 5, jedis);
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 判断名称是否存在
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("column_key", bean.getColumn_key());
			boolean check = mongoDBUtilComponent.checkObjectIsExist(
					"track_column_info", queryMap);
			if (check) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_TRACK_COLUMN_DUP);
				throw ex;
			}
			// 判断自定义字段数量
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			Integer count = mongoDBUtilComponent.countObject(
					"track_column_info", queryMap);
			if (count >= columnMax) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_TRACK_COLUMN_MAX);
				throw ex;
			}
			// 插入
			Date date = new Date();
			bean.setCreate_time(date);
			bean.setModify_time(date);
			mongoDBUtilComponent.insertObject("track_column_info",
					binder.toJson(bean));
			redisUtilComponent.releaseIdByRedis(
					TrackerConstants.TRACK_COLUMN_KEY_PREFIX,
					bean.getService_id(), jedis);
			// 更新redis
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("ak", bean.getAk());
			List<TrackColumnBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("track_column_info", queryMap,
							new String[] { "column_key", "column_type" },
							false, TrackColumnBean.class, null);
			redisUtilComponent.setRedisJsonCache(
					jedis,
					TrackerConstants.TRACK_COLUMN_COLLECT_KEY_PREFIX
							+ bean.getService_id(), list, binder, 0);
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

	@Override
	public ResultBean<List<TrackColumnBean>> listColumn(TrackColumnBean bean)
			throws BusinessException {
		try {
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}

			// 查询列表
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("ak", bean.getAk());
			List<TrackColumnBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("track_column_info", queryMap,
							new String[] { "service_id", "column_key",
									"column_desc", "column_type",
									"create_time", "modify_time" }, false,
							TrackColumnBean.class, null);
			ResultBean<List<TrackColumnBean>> result = new ResultBean<List<TrackColumnBean>>();
			result.setSuccessful(true);
			result.setResult(list);
			return result;

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		}
	}

	@Override
	public ResultBean<TrackColumnBean> deleteColumn(TrackColumnBean bean)
			throws BusinessException {
		ResultBean<TrackColumnBean> result = new ResultBean<TrackColumnBean>();
		Integer step=0;
		Jedis jedis = null;
		try {
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 判断名称是否存在
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("column_key", bean.getColumn_key());
			boolean check = mongoDBUtilComponent.checkObjectIsExist(
					"track_column_info", queryMap);
			if (!check) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_TRACK_COLUMN_NOT_FOUND);
				throw ex;
			}
			jedis=redisUtilComponent.getRedisInstance();
			// 删除本体
			mongoDBUtilComponent.removeCommonObject("track_column_info",
					queryMap);
			step=1;

			// redis缓存处理
			bean.setProcced_count(0);
			redisUtilComponent.appendPush(
					TrackerConstants.REMOVE_TRACK_COLUMN_LIST,
					binder.toJson(bean),jedis);

			// 更新redis
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("ak", bean.getAk());
			List<TrackColumnBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("track_column_info", queryMap,
							new String[] { "column_key", "column_type" },
							false, TrackColumnBean.class, null);
			redisUtilComponent.setRedisJsonCache(jedis,
					TrackerConstants.TRACK_COLUMN_COLLECT_KEY_PREFIX
							+ bean.getService_id(), list, binder, 0);
			// 删除关联
			this.cascadeDeleteColumn(bean);
			result.setSuccessful(true);
			result.setResult(bean);
			return result;

		} catch (Exception e) {
			if(step==1){
				logger.error(e.getMessage(), e);
				result.setSuccessful(true);
				result.setResult(bean);
				return result;
			}
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

	@SuppressWarnings("serial")
	@Override
	public void cascadeDeleteColumn(TrackColumnBean bean)
			throws BusinessException {
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("service_id", bean.getService_id());		
		Map<String, Object> updateMap = new HashMap<String, Object>();
		final String name=bean.getColumn_key();
		queryMap.put("custom_field."+name, new HashMap<String, Object>() {
			{
				put("$exists", true);
			}
		});
		updateMap.put("$unset", new HashMap<String, Object>() {
			{
				put("custom_field."+name, 1);
			}
		});
		mongoDBUtilComponent.executeUpdate("track_info", queryMap, updateMap);
		updateMap.clear();
		queryMap.remove("custom_field."+name);
		queryMap.put("realtime_point.trackColumns."+name, new HashMap<String, Object>() {
			{
				put("$exists", true);
			}
		});
		
		updateMap.put("$unset", new HashMap<String, Object>() {
			{
				put("realtime_point.trackColumns."+name, 1);
			}
		});
		mongoDBUtilComponent.executeUpdate("entity_info", queryMap, updateMap);
	}

}
