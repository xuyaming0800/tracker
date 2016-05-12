package cn.com.leador.mapapi.tracker.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.constants.TrackerConstants;

/**
 * 轨迹云初始化工具
 * 
 * @author xuyaming
 *
 */

@Component
public class TrackerInitComponent {
	private Logger logger = LogManager.getLogger(this.getClass());

	private final Long DEFAULT_SERVICE_ID = 100000L;

	@Autowired
	private RedisUtilComponent redisUtilComponent;

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostConstruct
	private void init() {
		// redis初始化检测
		try {
			logger.info("初始化redis");
			String value = redisUtilComponent
					.getRedisStringCache(TrackerConstants.MAX_APP_ID_KEY);
			if (value == null) {
				// 需要进行初始化 首先检测mongodb是否已经存在值
				Map<String, Object> queryMap = new HashMap<String, Object>();
				JsonBinder binder = JsonBinder.buildNonNullBinder(false);
				queryMap.put("status", 0);
				List<Map> _list = mongoDBUtilComponent.selectObjectMultiProjection(
						"app_max_id", queryMap, new String[] { "maxId" }, false,
						Map.class, binder.getCollectionType(Map.class,
								String.class, Object.class));
				if (_list.size() == 0) {
					// 没有找到最大ID 直接初始化值 同时设置redis和mongodb
					redisUtilComponent.setRedisJsonCache(
							TrackerConstants.MAX_APP_ID_KEY, DEFAULT_SERVICE_ID, 0);
					queryMap.clear();
					queryMap.put("maxId", DEFAULT_SERVICE_ID);
					queryMap.put("status", 0);
					mongoDBUtilComponent.insertObject("app_max_id", queryMap);
				}else{
					//只设置redis
					Map<String,Object> map=(Map<String,Object>)_list.get(0);
					Long maxId=new Long(map.get("maxId").toString());
					redisUtilComponent.setRedisJsonCache(
							TrackerConstants.MAX_APP_ID_KEY, maxId, 0);
				}

			}
		} catch (Exception e) {
			logger.error("初始化错误 系统无法启动",e);
			logger.error("系统无法启动，将关闭");
			System.exit(0);
		}

	}

}
