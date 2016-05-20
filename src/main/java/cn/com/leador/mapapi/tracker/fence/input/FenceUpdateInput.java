package cn.com.leador.mapapi.tracker.fence.input;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.geo.util.CoordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessExceptionBean;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.helper.SpringContextHelper;
import cn.com.leador.mapapi.common.proxy.CommonInputProxy;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_ALARM_CONDITION_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_CYCLE_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_ENTITY_STATUS;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.FENCE_SHAPE_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.HTTP_METHOD;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_GPS_TYPE;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.fence.bean.FenceBean;
import cn.com.leador.mapapi.tracker.fence.bean.ValidTimeBean;
import cn.com.leador.mapapi.tracker.fence.service.FenceService;
import cn.com.leador.mapapi.tracker.fence.service.impl.FenceServiceImpl;
import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class FenceUpdateInput extends CommonInputProxy<FenceBean> {

	private FenceService<FenceBean, FenceBean> fenceService = null;
	
	@Value("${fence.max_count.entity}")
	private Integer maxCount;

	@Value("${fence.max_count.per.entity}")
	private Integer perMaxCount;


	@PostConstruct
	private void init() {
		fenceService = SpringContextHelper
				.getBeanByType(FenceServiceImpl.class);
	}

	@Override
	protected ResultBean<FenceBean> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
//		Map<String, String> map = clientInfo.getInfoTable();
		FenceBean bean=(FenceBean)clientInfo.getResultMap().get("bean");
		clientInfo.setResultMap(null);
//		ResultBean<TrackBean> result = trackService.addPoint(bean);
		return fenceService.updateFence(bean);
	}

	@SuppressWarnings({ "serial", "unchecked" })
	@Override
	protected BusinessExceptionBean checkInput(ClientInfo clientInfo,
			HttpServletRequest request) {
		BusinessExceptionBean bean = null;
		final FenceBean _bean=new FenceBean();
		clientInfo.setResultMap(new HashMap<String,Object>(){
			{
				put("bean",_bean);
			}
		});
		// 校验method only-post
		if (!request.getMethod().toUpperCase()
				.equals(HTTP_METHOD.POST.getMethod())) {
			bean = new TrackerExceptionBean(
					TrackerExceptionEnum.HTTP_METHOD_EXCEPTION);
			bean.setSuccess(false);
			return bean;
		}
		Map<String, String> map = clientInfo.getInfoTable();
		// 必填参数校验
		String ak = map.get("ak");
		bean = new BusinessExceptionBean(
				BusinessExceptionEnum.PARAM_VAILD_ERROR);
		if (this.checkParamIsNull(ak)) {
			bean.setSuccess(false);
			bean.appnedMessage("ak不能为空");
			return bean;
		}
		_bean.setAk(ak);

		String serviceId = map.get("service_id");
		if (this.checkParamIsNull(serviceId)) {
			bean.appnedMessage("service_id不能为空");
			return bean;
		}
		_bean.setService_id(serviceId);
		
		String fenceId = map.get("fence_id");
		if (this.checkParamIsNull(fenceId)) {
			bean.appnedMessage("fence_id不能为空");
			return bean;
		}
		_bean.setFence_id(fenceId);
		
		String name = map.get("name");
		if (!this.checkParamIsNull(name)) {
			_bean.setName(name);
		}
		
		String monitored_persons = map.get("monitored_persons");
		if (!this.checkParamIsNull(monitored_persons)) {
			String[] persons=monitored_persons.split(",");
			if(persons.length>maxCount){
				bean.appnedMessage("monitored_persons最多存在"+maxCount+"个实体");
				return bean;
			}
			Set<String> personSet=new HashSet<String>();
			for(String s:persons){
				if(!personSet.add(s)){
					bean.appnedMessage("monitored_persons中实体名称重复");
					return bean;
				}
			}
			personSet.clear();
			personSet=null;
			
			_bean.setMonitored_persons(Arrays.asList(persons));
			Map<String,Integer> _map=new HashMap<String,Integer>();
			for(String entity:_bean.getMonitored_persons()){
				_map.put(entity, FENCE_ENTITY_STATUS.NO_STATUS.getStatus());
			}
			_bean.setMonitored_type(_map);
		}
		
		
		String valid_times = map.get("valid_times");
		if (!this.checkParamIsNull(valid_times)) {
			String[] times=valid_times.split(";");
			List<ValidTimeBean> l=new ArrayList<ValidTimeBean>();
			for(String time:times){
				String[] ts=time.split(",");
				if(ts.length!=2){
					bean.appnedMessage("valid_times分割格式错误");
					return bean;
				}else{
					try {
						Integer start=Integer.valueOf(ts[0]);
						Integer end=Integer.valueOf(ts[1]);
						ValidTimeBean b=new ValidTimeBean();
						b.setStart(start);
						b.setEnd(end);
						l.add(b);
					} catch (Exception e) {
						bean.appnedMessage("valid_times时间格式错误");
						return bean;
					}
				}
			}
			_bean.setValid_times(l);
		}
		
		
		String valid_cycle = map.get("valid_cycle");
		if (!this.checkParamIsNull(valid_cycle)) {
			if (!this.checkParamIsInAllowed(
					valid_cycle,
					new String[] { String.valueOf(FENCE_CYCLE_TYPE.NO_REPEAT.getType()),
							String.valueOf(FENCE_CYCLE_TYPE.WORKDAY.getType()),
							String.valueOf(FENCE_CYCLE_TYPE.WEEKEND.getType()), 
							String.valueOf(FENCE_CYCLE_TYPE.EVERYDAY.getType()),
							String.valueOf(FENCE_CYCLE_TYPE.CUSTOMER.getType())})) {
				bean.appnedMessage("valid_cycle类型错误");
				return bean;

			}
			_bean.setValid_cycle(Integer.valueOf(valid_cycle));
			if(valid_cycle.equals(String.valueOf(FENCE_CYCLE_TYPE.NO_REPEAT.getType()))){
				String valid_date = map.get("valid_date");
				if (this.checkParamIsNull(valid_date)) {
					bean.appnedMessage("valid_date不能为空");
					return bean;
				}
				_bean.setValid_date(valid_date);
			}
			
			if(valid_cycle.equals(String.valueOf(FENCE_CYCLE_TYPE.CUSTOMER.getType()))){
				String valid_days = map.get("valid_days");
				if (this.checkParamIsNull(valid_days)) {
					bean.appnedMessage("valid_days不能为空");
					return bean;
				}
				String[] days=valid_days.split(",");
				for(String day:days){
					try {
						int i=Integer.valueOf(day);
						if(i<1&&i>7){
						 bean.appnedMessage("valid_days格式错误");
						 return bean;
						}
					} catch (Exception e) {
						bean.appnedMessage("valid_days格式错误");
						return bean;
					}
				}
				_bean.setValid_days(Arrays.asList(days));
			}
		}
		
		
		String shape=map.get("shape");
		if (!this.checkParamIsNull(shape)) {
			if (!this.checkParamIsInAllowed(
					shape,
					new String[] { String.valueOf(FENCE_SHAPE_TYPE.CYCLE.getType()),
							String.valueOf(FENCE_SHAPE_TYPE.POLYGON.getType())})) {
				bean.appnedMessage("coord_type类型错误");
				return bean;
			}
			_bean.setShape(Integer.valueOf(shape));
		}
		
		if (!this.checkParamIsNull(shape)) {
			String coordType = map.get("coord_type");
			if (this.checkParamIsNull(coordType)) {
				bean.appnedMessage("coord_type不能为空");
				return bean;
			}
			if (!this.checkParamIsInAllowed(
					coordType,
					new String[] { String.valueOf(TRACK_GPS_TYPE.GPS.getType()),
							String.valueOf(TRACK_GPS_TYPE.GCJ02.getType()),
							String.valueOf(TRACK_GPS_TYPE.BAIDU.getType()) })) {
				bean.appnedMessage("coord_type类型错误");
				return bean;
			}
			_bean.setCoord_type(Integer.valueOf(coordType));
			if(shape.equals(String.valueOf(FENCE_SHAPE_TYPE.CYCLE.getType()))){
				//圆形
				List<Object> centerSphere=new ArrayList<Object>();
				String center=map.get("center");
				if (this.checkParamIsNull(center)) {
					bean.appnedMessage("center不能为空");
					return bean;
				}
				String[] centers=center.split(",");
				if(centers.length!=2){
					bean.appnedMessage("center格式错误");
					return bean;
				}
				final double x=Double.valueOf(centers[0]);
				final double y=Double.valueOf(centers[1]);
				double[] xy=new double[2];
				if(_bean.getCoord_type().intValue()==TRACK_GPS_TYPE.GPS
						.getType()){
					CoordUtils.gps2gcj(x, y, xy);
				}else if(_bean.getCoord_type().intValue()==TRACK_GPS_TYPE.BAIDU
						.getType()){
					CoordUtils.bd2gcj(x, y, xy);
				}else{
					xy[0]=x;
					xy[1]=y;
				}
				centerSphere.add(xy);
				String radius=map.get("radius");
				if (this.checkParamIsNull(radius)) {
					bean.appnedMessage("radius不能为空");
					return bean;
				}
				double radiusInt=Double.valueOf(radius);
				if(radiusInt<1||radiusInt>5000){
					bean.appnedMessage("radius值必须在1-5000");
					return bean;
				}
				double sphereRadius= (radiusInt/1000.0d)/6378.1d;
				DecimalFormat decimalFormat = new DecimalFormat("0.00000000000");
				centerSphere.add(decimalFormat.format(sphereRadius));
				_bean.setCenterSphere(centerSphere);
				_bean.setCenter(new HashMap<String,Double>()
				{{
					put("longitude",x);
					put("latitude",y);
				}});
				_bean.setRadius(new Double(radiusInt).intValue());
			}else{
				//多边形
				String coords = map.get("coordStr");
				
				if (this.checkParamIsNull(coords)) {
					bean.appnedMessage("coordStr不能为空");
					return bean;
				}
				String[] coordArray=coords.split(";");
				if(!coordArray[0].equals(coordArray[coordArray.length-1])){
					bean.appnedMessage("coordStr不是一个闭合的多边形");
					return bean;
				}
				List<List<Double[]>> surList=new ArrayList<List<Double[]>>();
				List<Double[]> innerList=new ArrayList<Double[]>();
				for(String coord:coordArray){
					String[] _coordArray=coord.split(",");
					if(_coordArray.length!=2){
						bean.appnedMessage("coordStr格式错误");
						return bean;
					}
					try {
						double x=Double.valueOf(_coordArray[0]);
						double y=Double.valueOf(_coordArray[1]);
						double[] xy=new double[2];
						Double[] _xy=new Double[2];
						if(_bean.getCoord_type().intValue()==TRACK_GPS_TYPE.GPS
								.getType()){
							CoordUtils.gps2gcj(x, y, xy);
						}else if(_bean.getCoord_type().intValue()==TRACK_GPS_TYPE.BAIDU
								.getType()){
							CoordUtils.bd2gcj(x, y, xy);
						}else{
							xy[0]=x;
							xy[1]=y;
						}
						_xy[0]=xy[0];
						_xy[1]=xy[1];	
						innerList.add(_xy);
						
					} catch (Exception e) {
						bean.appnedMessage("coords坐标必须是浮点型数字");
						return bean;
					}
				}
				surList.add(innerList);
				_bean.setCoords(surList);
				_bean.setCoordStr(coords);
			}
		}
		
		
		
		
		
		
		
		
		
		String desc = map.get("desc");
		_bean.setDesc(desc);
		
		String alarm_condition = map.get("alarm_condition");
		if (!this.checkParamIsNull(alarm_condition)) {
			if (!this.checkParamIsInAllowed(
					alarm_condition,
					new String[] { String.valueOf(FENCE_ALARM_CONDITION_TYPE.ENTRY.getType()),
							String.valueOf(FENCE_ALARM_CONDITION_TYPE.EXIT.getType()),
							String.valueOf(FENCE_ALARM_CONDITION_TYPE.ALL.getType()) })) {
				bean.appnedMessage("alarm_condition类型错误");
				return bean;
			}
			
			_bean.setAlarm_condition(Integer.valueOf(alarm_condition));
		}
		
		


		bean.setSuccess(true);
		return bean;
	}
}
