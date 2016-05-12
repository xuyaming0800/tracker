package cn.com.leador.mapapi.tracker.quartz;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.component.RedisUtilComponent;
import cn.com.leador.mapapi.tracker.constants.TrackerConstants;
import cn.com.leador.mapapi.tracker.entity.bean.EntityColumnBean;
import cn.com.leador.mapapi.tracker.entity.service.EntityColumnService;
import cn.com.leador.mapapi.tracker.track.bean.TrackColumnBean;
import cn.com.leador.mapapi.tracker.track.service.TrackColumnService;

public class TrackerRemoveCleaner {
	private Logger logger = LogManager.getLogger(this.getClass());
	private static Object synFlag = new Object();
	private static ExecutorService cachedThreadPool = Executors
			.newFixedThreadPool(100);
	@Value("${cleaner.entity_column}")
	private Integer max;
	@Value("${cleaner.track_column}")
	private Integer max_track;
	@Resource(name = "entityColumnServiceImpl")
	private EntityColumnService<EntityColumnBean, EntityColumnBean> entityColumnService;
	@Resource(name = "trackColumnServiceImpl")
	private TrackColumnService<TrackColumnBean, TrackColumnBean> trackColumnService;
	@Autowired
	private RedisUtilComponent redisUtilComponent;

	public void process() throws Exception {
		synchronized (synFlag) {
			logger.info("清理垃圾数据");
			// 获取 redis中需要处理的数据长度
			Long size = redisUtilComponent
					.getListSize(TrackerConstants.REMOVE_ENTITY_COLUMN_LIST);
			final JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			for (Long i = 0L; i < size; i++) {
				cachedThreadPool.execute(new Runnable() {

					@Override
					public void run() {
						try {
							// 获取数据
							String message = redisUtilComponent
									.prependPop(TrackerConstants.REMOVE_ENTITY_COLUMN_LIST);
							// 转换对象
							EntityColumnBean bean = binder.fromJson(message,
									EntityColumnBean.class);
							// 处理数据
							entityColumnService.cascadeDeleteColumn(bean);
							// 判断是否需要跑多次
							if (bean.getProcced_count() + 1 < max) {
								bean.setProcced_count(bean.getProcced_count() + 1);
								// 重新放回队列 等待下次
								redisUtilComponent
										.appendPush(
												TrackerConstants.REMOVE_ENTITY_COLUMN_LIST,
												binder.toJson(bean));
							}

						} catch (BusinessException e) {
							logger.error(e.getMessage(), e);
						}
					}

				});
			}
			size=redisUtilComponent
					.getListSize(TrackerConstants.REMOVE_TRACK_COLUMN_LIST);
			for (Long i = 0L; i < size; i++) {
				cachedThreadPool.execute(new Runnable() {

					@Override
					public void run() {
						try {
							// 获取数据
							String message = redisUtilComponent
									.prependPop(TrackerConstants.REMOVE_TRACK_COLUMN_LIST);
							// 转换对象
							TrackColumnBean bean = binder.fromJson(message,
									TrackColumnBean.class);
							// 处理数据
							trackColumnService.cascadeDeleteColumn(bean);
							// 判断是否需要跑多次
							if (bean.getProcced_count() + 1 < max_track) {
								bean.setProcced_count(bean.getProcced_count() + 1);
								// 重新放回队列 等待下次
								redisUtilComponent
										.appendPush(
												TrackerConstants.REMOVE_TRACK_COLUMN_LIST,
												binder.toJson(bean));
							}

						} catch (BusinessException e) {
							logger.error(e.getMessage(), e);
						}
					}

				});
			}
		}
	}

}
