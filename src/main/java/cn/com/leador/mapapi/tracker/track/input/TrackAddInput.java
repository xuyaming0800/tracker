package cn.com.leador.mapapi.tracker.track.input;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessExceptionBean;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.helper.SpringContextHelper;
import cn.com.leador.mapapi.common.proxy.CommonInputProxy;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.HTTP_METHOD;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_GPS_TYPE;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
import cn.com.leador.mapapi.tracker.track.service.TrackService;
import cn.com.leador.mapapi.tracker.track.service.impl.TrackServiceImpl;

@Component
public class TrackAddInput extends CommonInputProxy<TrackBean> {

	private TrackService<TrackBean, TrackBean> trackService = null;

	@PostConstruct
	private void init() {
		trackService = SpringContextHelper
				.getBeanByType(TrackServiceImpl.class);
	}

	@Override
	protected ResultBean<TrackBean> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		TrackBean bean = new TrackBean();
		bean.setCustom_field(new HashMap<String,Object>());
		for(String key:clientInfo.getOrginParamNames()){
			if(key.equals("ak")){
				bean.setAk(map.get(key));
			}else if(key.equals("service_id")){
				bean.setService_id(map.get(key));
			}else if(key.equals("entity_name")){
				bean.setEntity_name(map.get(key));
			}else if(key.equals("longitude")){
				bean.setLongitude(Double.valueOf((map.get(key))));
			}else if(key.equals("latitude")){
				bean.setLatitude(Double.valueOf((map.get(key))));
			}else if(key.equals("coord_type")){
				bean.setCoord_type(Integer.valueOf((map.get(key))));
			}else if(key.equals("loc_time")){
				bean.setLoc_time(Long.valueOf((map.get(key))));
			}else{
				bean.getCustom_field().put(key, map.get(key)==null?"":map.get(key));
			}
		}
		ResultBean<TrackBean> result = trackService.addPoint(bean);
		return result;
	}

	@Override
	protected BusinessExceptionBean checkInput(ClientInfo clientInfo,
			HttpServletRequest request) {
		BusinessExceptionBean bean = null;
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

		String serviceId = map.get("service_id");
		if (this.checkParamIsNull(serviceId)) {
			bean.appnedMessage("service_id不能为空");
			return bean;
		}
		String longitude = map.get("longitude");
		if (this.checkParamIsNull(longitude)) {
			bean.appnedMessage("longitude不能为空");
			return bean;
		}
		if (!this.checkParamIsNumber(longitude, Double.class)) {
			bean.appnedMessage("longitude必须为浮点型数据");
			return bean;
		}

		String latitude = map.get("latitude");
		if (this.checkParamIsNull(latitude)) {
			bean.appnedMessage("latitude不能为空");
			return bean;
		}
		if (!this.checkParamIsNumber(latitude, Double.class)) {
			bean.appnedMessage("latitude必须为浮点型数据");
			return bean;
		}

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

		}

		String locTime = map.get("loc_time");
		if (this.checkParamIsNull(locTime)) {
			bean.appnedMessage("loc_time不能为空");
			return bean;
		}
		if (!this.checkParamIsNumber(locTime, Long.class)) {
			bean.appnedMessage("loc_time必须为整型数字");
			return bean;
		}
		if(Long.parseLong(locTime)>(new Date().getTime())/1000+10*60){
			bean.appnedMessage("loc_time存在于未来");
			return bean;
		}
		
		String entityName = map.get("entity_name");
		if (this.checkParamIsNull(entityName)) {
			bean.appnedMessage("entity_name不能为空");
			return bean;
		}

		bean.setSuccess(true);
		return bean;
	}
}
