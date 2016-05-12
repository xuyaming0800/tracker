package cn.com.leador.mapapi.tracker.track.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import cn.com.leador.mapapi.tracker.component.TrackerProceedComponent;
import cn.com.leador.mapapi.tracker.constants.TrackerConstants;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.IS_PROCESSED;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_COLUMN_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_RETURN_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_TIME_SORT_TYPE;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
import cn.com.leador.mapapi.tracker.track.bean.TrackColumnBean;
import cn.com.leador.mapapi.tracker.track.service.TrackService;
import cn.com.leador.mapapi.tracker.util.mq.RabbitMQUtils;

@Service
public class TrackServiceImpl implements TrackService<TrackBean, TrackBean> {

	private Logger logger = LogManager.getLogger(this.getClass());
	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private RedisUtilComponent redisUtilComponent;
	@Autowired
	private TrackerProceedComponent trackerProceedComponent;

	@Value("${mq.host}")
	private String mqHost;
	@Value("${mq.port}")
	private Integer mqPort;
	@Value("${mq.tracker_exchange}")
	private String mq_tracker_exchange;
	@Value("${mq.consumer_count}")
	private Integer consumerCount;

	@Override
	public ResultBean<TrackBean> addPoint(TrackBean bean)
			throws BusinessException {
		Jedis jedis = null;
		try {
			jedis = redisUtilComponent.getRedisInstance();
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 判断serviceId是否存在 redis
			String akId = redisUtilComponent.getRedisStringCache(
					TrackerConstants.APP_INFO_PREFIX + bean.getAk()
							+ bean.getService_id(), jedis);
			if (akId == null) {
				// 判断serviceId是否存在-mongodb 并重新缓存
				if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
						bean.getAk())) {
					TrackerException ex = new TrackerException(
							TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
					throw ex;
				} else {
					redisUtilComponent.setRedisStringCache(jedis,
							TrackerConstants.APP_INFO_PREFIX + bean.getAk()
									+ bean.getService_id(),
							bean.getAk() + bean.getService_id(), 0);
				}
			}

			Map<String, Object> queryMap = new HashMap<String, Object>();
			// 获取自定义字段列表
			Map<String, Integer> customMap = new HashMap<String, Integer>();
			List<?> list = redisUtilComponent
					.getRedisJsonCache(jedis,
							TrackerConstants.TRACK_COLUMN_COLLECT_KEY_PREFIX
									+ bean.getService_id(), List.class, binder,
							binder.getCollectionType(List.class,
									TrackColumnBean.class));
			if (list == null) {
				// 从mongodb获取 缓存时效情况下 并重新缓存
				queryMap.put("service_id", bean.getService_id());
				queryMap.put("ak", bean.getAk());
				List<TrackColumnBean> _list = mongoDBUtilComponent
						.selectObjectMultiProjection("track_column_info",
								queryMap, new String[] { "column_key",
										"column_type" }, false,
								TrackColumnBean.class, null);
				for (TrackColumnBean _bean : _list) {
					customMap
							.put(_bean.getColumn_key(), _bean.getColumn_type());
				}
				redisUtilComponent.setRedisJsonCache(
						jedis,
						TrackerConstants.TRACK_COLUMN_COLLECT_KEY_PREFIX
								+ bean.getService_id(), _list, binder, 0);
			} else {
				for (Object o : list) {
					TrackColumnBean _bean = (TrackColumnBean) o;
					customMap
							.put(_bean.getColumn_key(), _bean.getColumn_type());
				}
			}
			// 对照自定义列表
			Map<String, Object> _map = bean.getCustom_field();
			for (String key : _map.keySet()) {
				if (!customMap.containsKey(key)) {
					// 找不到直接放过 不存储
				} else {
					Integer type = customMap.get(key);
					try {
						if (type.intValue() == TRACK_COLUMN_TYPE.INT64
								.getStatus()) {
							_map.put(key,
									Long.parseLong(_map.get(key).toString()));
						} else if (type.intValue() == TRACK_COLUMN_TYPE.DOUBLE
								.getStatus()) {
							_map.put(key, Double.parseDouble(_map.get(key)
									.toString()));
						}
					} catch (NumberFormatException e) {
						TrackerException ex = null;
						if (type.intValue() == TRACK_COLUMN_TYPE.INT64
								.getStatus()) {
							ex = new TrackerException(
									TrackerExceptionEnum.REDIS_TRACK_COLUMN_TYPE_ERROR,
									key + "必须是整型");
						} else {
							ex = new TrackerException(
									TrackerExceptionEnum.REDIS_TRACK_COLUMN_TYPE_ERROR,
									key + "必须是浮点型");
						}
						throw ex;
					}
				}
			}
			// 放入消息队列
			Date date = new Date();
			bean.setCreate_time(date);
			bean.setModify_time(date);
			Long id = new Long(mq_tracker_exchange.hashCode());
			String routeKey = String.valueOf(bean.getEntity_name().hashCode()
					% consumerCount);
			RabbitMQUtils.sendToQueue(String.valueOf(id), mqHost, mqPort, null,
					null, mq_tracker_exchange, routeKey, bean, false);
			ResultBean<TrackBean> result = new ResultBean<TrackBean>();
			result.setSuccessful(true);
			result.setResult(bean);
			return result;

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
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
	public ResultBean<TrackBean> addPoints(TrackBean bean)
			throws BusinessException {
		bean.setTime(new Date().getTime() / 1000);
		Jedis jedis = null;
		try {
			jedis = redisUtilComponent.getRedisInstance();
			// 判断serviceId是否存在 redis
			String akId = redisUtilComponent.getRedisStringCache(
					TrackerConstants.APP_INFO_PREFIX + bean.getAk()
							+ bean.getService_id(), jedis);
			if (akId == null) {
				// 判断serviceId是否存在-mongodb 并重新缓存
				if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
						bean.getAk())) {
					TrackerException ex = new TrackerException(
							TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
					throw ex;
				} else {
					redisUtilComponent.setRedisStringCache(jedis,
							TrackerConstants.APP_INFO_PREFIX + bean.getAk()
									+ bean.getService_id(),
							bean.getAk() + bean.getService_id(), 0);
				}
			}
			bean.setTotal(0);
			// 插入monodb
			if (bean.getBeanList() != null) {
				Date date = new Date();
				for (String line : bean.getBeanList()) {
					TrackBean _bean = new TrackBean();
					try {
						String[] track = line.split(",");
						_bean.setLongitude(Double.valueOf(track[0]));
						_bean.setLatitude(Double.valueOf(track[1]));
						_bean.setCoord_type(Integer.valueOf(track[3]));
						_bean.setLoc_time(Long.valueOf(track[2]));
						if (_bean.getLoc_time() > (date.getTime() / 1000 + 600)) {
							TrackerException ex = new TrackerException(
									TrackerExceptionEnum.LOC_OVER_TIME_ERROR);
							throw ex;
						}
						_bean.setAk(bean.getAk());
						_bean.setService_id(bean.getService_id());
						_bean.setEntity_name(bean.getEntity_name());
						_bean.setCreate_time(date);
						_bean.setModify_time(date);
						trackerProceedComponent.addPoint(_bean);
						bean.setTotal(bean.getTotal() + 1);
					} catch (Exception e) {
						// 异常处理
						if (bean.getError_points() == null) {
							bean.setError_points(new ArrayList<TrackBean>());
						}
						String s = null;
						_bean.setCreate_time(s);
						_bean.setCreate_timestamp(null);
						_bean.setModify_time(s);
						_bean.setModify_timestamp(null);
						_bean.setAk(null);
						_bean.setEntity_name(null);
						_bean.setService_id(null);
						bean.getError_points().add(_bean);
					}
				}
			}

			ResultBean<TrackBean> result = new ResultBean<TrackBean>();
			bean.setBeanList(null);
			result.setSuccessful(true);
			result.setResult(bean);
			bean.setTime(new Date().getTime() / 1000 - bean.getTime());
			return result;

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
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
	public ResultBean<List<TrackBean>> getHistory(TrackBean bean)
			throws BusinessException {
		Jedis jedis = null;
		try {
			jedis = redisUtilComponent.getRedisInstance();
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 判断serviceId是否存在 redis
			String akId = redisUtilComponent.getRedisStringCache(
					TrackerConstants.APP_INFO_PREFIX + bean.getAk()
							+ bean.getService_id(), jedis);
			if (akId == null) {
				// 判断serviceId是否存在-mongodb 并重新缓存
				if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
						bean.getAk())) {
					TrackerException ex = new TrackerException(
							TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
					throw ex;
				} else {
					redisUtilComponent.setRedisStringCache(jedis,
							TrackerConstants.APP_INFO_PREFIX + bean.getAk()
									+ bean.getService_id(),
							bean.getAk() + bean.getService_id(), 0);
				}
			}
			// 查询列表
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("entity_name", bean.getEntity_name());
			// 时间戳条件
			Map<String, Object> _map = new HashMap<String, Object>();
			_map.put("$gte", bean.getStart_time());
			_map.put("$lte", bean.getEnd_time());
			queryMap.put("loc_time", _map);
			// 排序
			Map<String, Object> sortMap = new HashMap<String, Object>();
			if (bean.getSort_type().intValue() == TRACK_TIME_SORT_TYPE.ascending
					.getType()) {
				// 升序
				sortMap.put("loc_time", 1);
			} else {
				// 降序
				sortMap.put("loc_time", -1);
			}
			// 查询总数
			Integer count = mongoDBUtilComponent.countObject("track_info",
					queryMap);
			ResultBean<List<TrackBean>> result = new ResultBean<List<TrackBean>>();
			Map<String, Object> extraResult = new HashMap<String, Object>();
			if (count > 0) {
				// 轨迹纠偏 目前只支持抽希降噪
				if (bean.getIs_processed().intValue() == IS_PROCESSED.YES.getType()) {
					queryMap.put("is_pumping", false);
				}
				// 计算路径距离
				Map<String, Object> _sortMap = new HashMap<String, Object>();
				_sortMap.put("loc_time", 1);
				List<Map<String, Object>> aggregateList = new LinkedList<Map<String, Object>>();
				Map<String, Object> matchMap = new HashMap<String, Object>();
				Map<String, Object> reduceMap = new HashMap<String, Object>();
				matchMap.put("$match", queryMap);
				Map<String, Object> reduce = new HashMap<String, Object>();
				reduce.put("_id", null);
				Map<String, Object> sumMap = new HashMap<String, Object>();
				sumMap.put("$sum", "$distance");
				reduce.put("distance", sumMap);
				reduceMap.put("$group", reduce);
				aggregateList.add(matchMap);
				aggregateList.add(reduceMap);
				List<TrackBean> aggregateResult = mongoDBUtilComponent
						.aggregateObject("track_info", aggregateList,
								TrackBean.class, null, binder);
				//获取最早的点
				List<TrackBean> _list=mongoDBUtilComponent
					.selectObjectMultiProjection("track_info", queryMap,
							new String[]{}, false, TrackBean.class, null,
							1, 1,_sortMap);
				
				for (TrackBean _bean : aggregateResult) {
					extraResult.put("distance", _bean.getDistance()-_list.get(0).getDistance());
					break;
				}
				// 查询结果
				String[] projection = new String[] { "longitude", "latitude",
						"entity_name", "create_time", "loc_time", "direction",
						"speed", "custom_field" };
				if (bean.getSimple_return().intValue() == TRACK_RETURN_TYPE.SIMPLE
						.getStatus()) {
					projection = new String[] { "longitude", "latitude" };
				}
				List<TrackBean> list = mongoDBUtilComponent
						.selectObjectMultiProjection("track_info", queryMap,
								projection, false, TrackBean.class, null,
								bean.getPage_index(), bean.getPage_size(),sortMap);
				result.setResult(list);
				extraResult.put("size", list.size());

			} else {
				extraResult.put("distance", 0.0d);
				extraResult.put("size", 0);
			}
			result.setSuccessful(true);
			
			extraResult.put("entity_name", bean.getEntity_name());
			extraResult.put("total", count);
			result.setExtraResult(extraResult);
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
