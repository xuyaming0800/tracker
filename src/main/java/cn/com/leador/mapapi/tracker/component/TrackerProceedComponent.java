package cn.com.leador.mapapi.tracker.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.geo.util.CoordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_ALARM_CONDITION_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_CYCLE_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_ENTITY_STATUS;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_SHAPE_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_GPS_TYPE;
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.entity.bean.EntityLocationBean;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.fence.bean.FenceAlarmBean;
import cn.com.leador.mapapi.tracker.fence.bean.FenceBean;
import cn.com.leador.mapapi.tracker.fence.bean.FenceEntityBean;
import cn.com.leador.mapapi.tracker.fence.bean.FencePointBean;
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
import cn.com.leador.mapapi.tracker.track.bean.TrackColumnBean;
import cn.com.leador.mapapi.tracker.util.DateUtil;
import cn.com.leador.mapapi.tracker.util.mq.RabbitMQMessageHandler;
import cn.com.leador.mapapi.tracker.util.mq.RabbitMQUtils;

@Component
public class TrackerProceedComponent {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private CoordUtilComponent coordUtilComponent;
	@Value("${mq.host}")
	private String host;
	@Value("${mq.port}")
	private Integer port;
	@Value("${mq.tracker_queue}")
	private String queueName;
	@Value("${mq.tracker_exchange}")
	private String exchange;
	@Value("${mq.consumer_count}")
	private Integer consumerCount;
	@Value("${entity.max-count}")
	private Integer entityMax;
	@Value("${coords.min_distance}")
	private Double minDistance;
	
	@Value("${mq.username}")
	private String userName;
	@Value("${mq.password}")
	private String password;

	@PostConstruct
	private void init() throws BusinessException {
		if(userName!=null&&userName.equals("")){
			userName=null;
		}
		if(password!=null&&password.equals("")){
			password=null;
		}
		logger.info("启动轨迹上传消息队列处理消费者");
		for (int i = 0; i < consumerCount; i++) {
			this.receiveCollectInfoToAuditQueue(executeTrack(String.valueOf(i)),
					String.valueOf(i));
		}

	}

	private void receiveCollectInfoToAuditQueue(
			final RabbitMQMessageHandler handler, final String index)
			throws BusinessException {
		try {
			final Long id = new Long(queueName.hashCode());
			Thread t = new Thread() {
				public void run() {
					try {
						RabbitMQUtils.receive(String.valueOf(id), host, port,
								userName, password, queueName, exchange, index,
								TrackBean.class, false, handler);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			};
			t.start();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			TrackerException ex = new TrackerException(
					TrackerExceptionEnum.MQ_RECEIVE_ERROR);
			throw ex;
		}
	}

	private RabbitMQMessageHandler executeTrack(final String index) {
		RabbitMQMessageHandler handler = new RabbitMQMessageHandler() {

			@Override
			public void setMessage(Object message) {
				try {
					TrackBean bean = (TrackBean) message;
					logger.info("处理队列"+index);
					addPoint(bean);

				} catch (Exception e) {
					logger.error("处理track时发生错误");
					logger.error(e.getMessage(), e);
				}
			}

		};
		return handler;
	}

	@SuppressWarnings("serial")
	public final TrackBean addPoint(final TrackBean bean) throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 处理坐标转换
			if (bean.getCoord_type().intValue() != TrackerEnumConstant.TRACK_GPS_TYPE.GCJ02
					.getType()) {
				double[] xy = new double[2];
				if (bean.getCoord_type().intValue() == TRACK_GPS_TYPE.GPS
						.getType()) {
					CoordUtils.gps2gcj(bean.getLongitude(), bean.getLatitude(),
							xy);
				} else {
					CoordUtils.bd2gcj(bean.getLongitude(), bean.getLatitude(),
							xy);
				}
				bean.setLongitude(xy[0]);
				bean.setLatitude(xy[1]);
			}
			// 检测bean的自定义字段
			Map<String, Object> newCustomMap = new HashMap<String, Object>();
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			List<TrackColumnBean> _l = mongoDBUtilComponent
					.selectObjectMultiProjection("track_column_info", queryMap,
							new String[] { "column_key" }, false,
							TrackColumnBean.class, null);
			for (TrackColumnBean _bean : _l) {
				if (bean.getCustom_field().containsKey(_bean.getColumn_key())) {
					newCustomMap.put(_bean.getColumn_key(), bean
							.getCustom_field().get(_bean.getColumn_key()));
				}
			}
			bean.setCustom_field(newCustomMap);
			// 检测bean对应的实体是否存在，不存在则创建
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("entity_name", bean.getEntity_name());
			List<EntityBean> entitylist = mongoDBUtilComponent
					.selectObjectMultiProjection("entity_info", queryMap,
							new String[] { "realtime_point" }, false,
							EntityBean.class, null);
			logger.info("查询到的实体列表大小:"+entitylist.size());
			if (entitylist != null && entitylist.size() > 0) {
				// 存在情况，更新信息
				EntityBean _bean = entitylist.get(0);
				if (bean.getLoc_time() >= _bean.getRealtime_point()
						.getLoc_time()) {
					logger.info("往后更新实时位置:"+bean.getLoc_time());
					// 时间大于上一次才更新实时位置
					EntityLocationBean locate = _bean.getRealtime_point();
					Map<String, Object> updateMap = new HashMap<String, Object>();
					Double[] xy = locate.getLocation();
					if (xy == null || xy.length != 2
							|| locate.getLocation()[0] == 0.0D) {
						// 初次上传轨迹 无需计算速度和方向
						bean.setIs_pumping(false);
					} else {
						double distance = coordUtilComponent.getDistance(xy[0],
								xy[1], bean.getLongitude(), bean.getLatitude());
						double speed = 0.0D;
						Integer direction = coordUtilComponent.getDirection(
								xy[0], xy[1], bean.getLongitude(),
								bean.getLatitude());
						if (bean.getLoc_time() > _bean.getRealtime_point()
								.getLoc_time()) {
							speed = coordUtilComponent.getSpeed(distance,
									bean.getLoc_time()
											- _bean.getRealtime_point()
													.getLoc_time());
						}
						updateMap.put("realtime_point.speed", speed);
						updateMap.put("realtime_point.direction", direction);
						bean.setSpeed(speed);
						bean.setDirection(direction);
						bean.setDistance(distance);

					}

					updateMap
							.put("realtime_point.loc_time", bean.getLoc_time());
					updateMap.put("realtime_point.location", new Double[] {
							bean.getLongitude(), bean.getLatitude() });
					updateMap.put("realtime_point.trackColumns",
							bean.getCustom_field());
					Map<String, Object> _map = new HashMap<String, Object>();
					_map.put("$lt", bean.getLoc_time());
					queryMap.put("realtime_point.loc_time", _map);
					mongoDBUtilComponent.batchUpdateCommonObject("entity_info",
							queryMap, updateMap);

				} else {
					// 时间发生错乱时 修正后一个点的速度等信息
					Map<String, Object> sort = new HashMap<String, Object>();
					sort.put("loc_time", -1);
					Map<String, Object> _map = new HashMap<String, Object>();
					_map.put("$lte", bean.getLoc_time());
					queryMap.put("loc_time", _map);
					// 查询上一个点
					List<TrackBean> _list = mongoDBUtilComponent
							.selectObjectMultiProjection("track_info",
									queryMap, new String[] { "longitude",
											"latitude", "loc_time" }, false,
									TrackBean.class, null, 1, 1, sort);
					if (_list != null && _list.size() > 0) {
						TrackBean __bean = _list.get(0);
						Double speed = null;
						Double distance = null;
						Integer direction = null;
						distance = coordUtilComponent.getDistance(
								__bean.getLongitude(), __bean.getLatitude(),
								bean.getLongitude(), bean.getLatitude());
						if (__bean.getLoc_time() < bean.getLoc_time()) {
							speed = coordUtilComponent.getSpeed(distance,
									bean.getLoc_time() - __bean.getLoc_time());
						} else {
							speed = 0.0d;
						}
						direction = coordUtilComponent.getDirection(
								__bean.getLongitude(), __bean.getLatitude(),
								bean.getLongitude(), bean.getLatitude());
						bean.setSpeed(speed);
						bean.setDistance(distance);
						bean.setDirection(direction);
					}
					// 查询下一个点
					_map.clear();
					_map.put("$gte", bean.getLoc_time());
					sort.put("loc_time", 1);
					_list = mongoDBUtilComponent.selectObjectMultiProjection(
							"track_info", queryMap, new String[] { "longitude",
									"latitude", "loc_time" }, true,
							TrackBean.class, null, 1, 1, sort);
					if (_list != null && _list.size() > 0) {
						TrackBean __bean = _list.get(0);
						Double speed = null;
						Double distance = null;
						Integer direction = null;
						distance = coordUtilComponent.getDistance(
								__bean.getLongitude(), __bean.getLatitude(),
								bean.getLongitude(), bean.getLatitude());
						if (__bean.getLoc_time() > bean.getLoc_time()) {
							speed = coordUtilComponent.getSpeed(distance,
									__bean.getLoc_time() - bean.getLoc_time());
						} else {
							speed = 0.0d;
						}
						direction = coordUtilComponent.getDirection(
								bean.getLongitude(), bean.getLatitude(),
								__bean.getLongitude(), __bean.getLatitude());
						// 更新下一个点
						queryMap.clear();
						queryMap.put("_id",
								new ObjectId(__bean.get_id().get("$oid")
										.toString()));
						Map<String, Object> updateMap = new HashMap<String, Object>();
						updateMap.put("speed", speed);
						updateMap.put("distance", distance);
						updateMap.put("direction", direction);
						updateMap.put("modify_time", bean.getCreate_time());
						updateMap.put("modify_timestamp",
								bean.getCreate_timestamp());
						mongoDBUtilComponent.batchUpdateCommonObject(
								"track_info", queryMap, updateMap);
					}

				}
				//计算是否是需要抽希 
				Map<String, Object> sort = new HashMap<String, Object>();
				sort.put("loc_time", -1);
				Map<String, Object> _map = new HashMap<String, Object>();
				_map.put("$lte", bean.getLoc_time());
				queryMap.put("loc_time", _map);
				queryMap.put("is_pumping", false);
				// 查询上一个降噪后存留的点
				List<TrackBean> _list = mongoDBUtilComponent
						.selectObjectMultiProjection("track_info",
								queryMap, new String[] { "longitude",
										"latitude", "loc_time" }, false,
								TrackBean.class, null, 1, 1, sort);
				if (_list != null && _list.size() > 0) {
					TrackBean __bean = _list.get(0);
					Double distance=coordUtilComponent.getDistance(
							__bean.getLongitude(), __bean.getLatitude(),
							bean.getLongitude(), bean.getLatitude());
					if(distance!=null&&distance < minDistance){
						bean.setIs_pumping(true);
					}else{
						bean.setIs_pumping(false);
					}
					
				}else{
					bean.setIs_pumping(false);
				}
				
//				if (bean.getDistance() != null
//						&& bean.getDistance() < minDistance) {
//					bean.setIs_pumping(true);
//				} else {
//					bean.setIs_pumping(false);
//				}

			} else {
				// 不存在 默认插入一个
				queryMap.clear();
				queryMap.put("service_id", bean.getService_id());
				Integer count = mongoDBUtilComponent.countObject("entity_info",
						queryMap);
				if (count >= entityMax) {
					TrackerException ex = new TrackerException(
							TrackerExceptionEnum.REDIS_ENTITY_MAX);
					throw ex;
				}
				EntityBean _bean = new EntityBean();
				_bean.setAk(bean.getAk());
				_bean.setEntity_name(bean.getEntity_name());
				_bean.setService_id(bean.getService_id());
				_bean.setCreate_time(bean.getCreate_time());
				_bean.setCreate_timestamp(bean.getCreate_timestamp());
				_bean.setModify_time(bean.getModify_time());
				_bean.setModify_timestamp(bean.getModify_timestamp());
				EntityLocationBean realPoint = new EntityLocationBean();
				realPoint.setDirection(null);
				realPoint.setLoc_time(bean.getLoc_time());
				realPoint.setSpeed(null);
				realPoint.setLocation(new Double[] { bean.getLongitude(),
						bean.getLatitude() });
				realPoint.setTrackColumns(bean.getCustom_field());
				_bean.setRealtime_point(realPoint);
				_bean.setCustom_field_common(new ArrayList<Map<String, Object>>());
				_bean.setCustom_field_index(new ArrayList<Map<String, Object>>());
				_bean.setCustom_field(null);
				bean.setIs_pumping(false);
				try {
					mongoDBUtilComponent.insertObject("entity_info",
							binder.toJson(_bean));
				} catch (Exception e) {
					logger.warn("高并发条件下，实体[" + bean.getEntity_name()
							+ "]已经创建成功，重新执行更新");
					addPoint(bean);
				}
			}
			// 查询上一个点
			if (bean.getSpeed() == null) {
				bean.setDistance(0.0d);
				bean.setSpeed(0.0d);
				bean.setDirection(null);
			}
			mongoDBUtilComponent
					.insertObject("track_info", binder.toJson(bean));
			//检测是否需要围栏判断
			Date date=new Date(bean.getLoc_time()*1000);
			final String dateStr=DateUtil.parseString(date, "yyyyMMdd");
			final String timeStr=DateUtil.parseString(date, "Hmm");
			final Integer day=DateUtil.getDay(date);
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("valid_times", new HashMap<String, Object>() {
				{
					put("$elemMatch", new HashMap<String, Object>() {
						{
							put("start",new HashMap<String, Object>() {
								{
									put("$lte",Integer.valueOf(timeStr) );
								}
							});
							put("end", new HashMap<String, Object>() {
								{
									put("$gte",Integer.valueOf(timeStr) );
								}
							});
						}
					});
				}
			});
			queryMap.put("monitored_persons",
					new HashMap<String, Object>() {
						{
							put("$all", new String[] { bean.getEntity_name() });
						}
					});
			System.out.println(binder.toJson(queryMap));
			List<FenceBean> fenceBeanList=mongoDBUtilComponent.selectObjectMultiProjection("fence_info", queryMap, null, false, FenceBean.class, null);
			List<FenceBean> _fenceBeanList=new ArrayList<FenceBean>();
			if(fenceBeanList!=null&&fenceBeanList.size()>0){
				//前置判断围栏是否生效
				
				for(FenceBean fenceBean:fenceBeanList){
					if(fenceBean.getValid_cycle().intValue()==FENCE_CYCLE_TYPE.NO_REPEAT.getType()){
						if(fenceBean.getValid_date().equals(dateStr)){
							_fenceBeanList.add(fenceBean);
						}
					}else if(fenceBean.getValid_cycle().intValue()==FENCE_CYCLE_TYPE.WORKDAY.getType()){
						if(day-1>=1&&day-1<=5){
							_fenceBeanList.add(fenceBean);
						}
					}else if(fenceBean.getValid_cycle().intValue()==FENCE_CYCLE_TYPE.WEEKEND.getType()){
						if(day==1&&day==1){
							_fenceBeanList.add(fenceBean);
						}
					}else if(fenceBean.getValid_cycle().intValue()==FENCE_CYCLE_TYPE.EVERYDAY.getType()){
						_fenceBeanList.add(fenceBean);
					}else{
						if(fenceBean.getValid_days().contains(day)){
							_fenceBeanList.add(fenceBean);
						}
					}
					
				}
				if(_fenceBeanList.size()>0){
					//更新点位置
					queryMap.clear();
					queryMap.put("service_id", bean.getService_id());
					queryMap.put("entity_name", bean.getEntity_name());
					FencePointBean point=new FencePointBean();
					point.getCoordinates()[0]=bean.getLongitude();
					point.getCoordinates()[1]=bean.getLatitude();
					FenceEntityBean entityBean=new FenceEntityBean();
					entityBean.setService_id(bean.getService_id());
					entityBean.setEntity_name(bean.getEntity_name());
					entityBean.setLoc(point);
					entityBean.setLoc_time(bean.getLoc_time());
					entityBean.setModify_time(bean.getModify_time());
					entityBean.setModify_timestamp(bean.getCreate_timestamp());
					mongoDBUtilComponent.upsertCommonObject("fence_entity_info", queryMap, binder.toJson(entityBean));
				}
				
				for(FenceBean fenceBean:_fenceBeanList){
					queryMap.clear();
					queryMap.put("service_id", bean.getService_id());
					queryMap.put("entity_name", bean.getEntity_name());
					//查询点是否在围栏里面
					if(fenceBean.getShape().intValue()==FENCE_SHAPE_TYPE.CYCLE.getType()){
						final List<Object> centerSphere=fenceBean.getCenterSphere();
						queryMap.put("loc",new HashMap<String, Object>() {
							{
								put("$geoWithin", new HashMap<String, Object>() {
									{
										put("$centerSphere", centerSphere);
									}
								});
							}
						});
					}else{
						final List<List<Double[]>> coords=fenceBean.getCoords();
						queryMap.put("loc",new HashMap<String, Object>() {
							{
								put("$geoWithin", new HashMap<String, Object>() {
									{
										put("$geometry", new HashMap<String, Object>() {
											{
												put("type", "Polygon");
												put("coordinates", coords);
											}
										});
									}
								});
							}
						});
					}
					int count=mongoDBUtilComponent.countObject("fence_entity_info", queryMap);
					queryMap.clear();
					queryMap.put("fence_id", fenceBean.getFence_id());
					Map<String, Object> updateMap = new HashMap<String, Object>();
					if(count==1){
						updateMap.put("$set", new HashMap<String, Object>() {
							{
								put("monitored_type."+bean.getEntity_name(), FENCE_ENTITY_STATUS.IN.getStatus());
							}
						});
					}else{
						updateMap.put("$set", new HashMap<String, Object>() {
							{
								put("monitored_type."+bean.getEntity_name(), FENCE_ENTITY_STATUS.OUT.getStatus());
							}
						});
					}
					mongoDBUtilComponent.executeUpdate("fence_info", queryMap, updateMap);
					FenceAlarmBean fenceAlarmBean=new FenceAlarmBean(bean.getAk(),bean.getService_id(),
							bean.getEntity_name(),bean.getCreate_time(),bean.getCreate_timestamp(),bean.getLoc_time());
					if(fenceBean.getAlarm_condition().intValue()==FENCE_ALARM_CONDITION_TYPE.ENTRY.getType()){
						if(count==1){
							fenceAlarmBean.setAction(FENCE_ENTITY_STATUS.IN.getStatus());
						}
					}else if(fenceBean.getAlarm_condition().intValue()==FENCE_ALARM_CONDITION_TYPE.EXIT.getType()){
						if(count==0){
							fenceAlarmBean.setAction(FENCE_ENTITY_STATUS.OUT.getStatus());
						}
					}else{
						if(count==1){
							fenceAlarmBean.setAction(FENCE_ENTITY_STATUS.IN.getStatus());
						}else{
							fenceAlarmBean.setAction(FENCE_ENTITY_STATUS.OUT.getStatus());
						}
					}
					if(fenceAlarmBean.getAction()!=null){
						//存在需要报警 存储报警
						fenceAlarmBean.setFence_id(fenceBean.getFence_id());
						mongoDBUtilComponent.insertObject("fence_alarm_info", binder.toJson(fenceAlarmBean));
						//报警信息推送 暂未实现
					}
				}
			}
			
			return bean;
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		}
	}
}
