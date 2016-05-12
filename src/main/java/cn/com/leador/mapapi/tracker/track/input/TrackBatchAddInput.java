package cn.com.leador.mapapi.tracker.track.input;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessExceptionBean;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.helper.SpringContextHelper;
import cn.com.leador.mapapi.common.proxy.CommonInputProxy;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.HTTP_METHOD;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
import cn.com.leador.mapapi.tracker.track.service.TrackService;
import cn.com.leador.mapapi.tracker.track.service.impl.TrackServiceImpl;

@Component
public class TrackBatchAddInput extends CommonInputProxy<TrackBean> {

	private TrackService<TrackBean, TrackBean> trackService = null;
	private Logger logger = LogManager.getLogger(this.getClass());

	@PostConstruct
	private void init() {
		trackService = SpringContextHelper
				.getBeanByType(TrackServiceImpl.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ResultBean<TrackBean> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, Object> map = clientInfo.getMap();
		TrackBean bean = new TrackBean();
		for (String key : clientInfo.getOrginParamNames()) {
			if (key.equals("ak")) {
				bean.setAk(map.get(key).toString());
			} else if (key.equals("service_id")) {
				bean.setService_id(map.get(key).toString());
			} else if (key.equals("entity_name")) {
				bean.setEntity_name(map.get(key).toString());
			} else if (key.endsWith("point_list")) {
				bean.setBeanList((List<String>)map.get(key));
			}
		}
		ResultBean<TrackBean> result = trackService.addPoints(bean);
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
		bean = new BusinessExceptionBean(
				BusinessExceptionEnum.PARAM_VAILD_ERROR);
		Map<String, Object> map = clientInfo.getMap();
		if (map == null||map.size()==0) {
			bean.setSuccess(false);
			bean.appnedMessage("上传信息错误,必须走application/Muiltpart");
			return bean;
		}
		// 必填参数校验
		String ak = map.get("ak") == null ? null : map.get("ak").toString();
		if (this.checkParamIsNull(ak)) {
			bean.setSuccess(false);
			bean.appnedMessage("ak不能为空");
			return bean;
		}

		String serviceId = map.get("service_id") == null ? null : map.get(
				"service_id").toString();
		if (this.checkParamIsNull(serviceId)) {
			bean.appnedMessage("service_id不能为空");
			return bean;
		}

		String entityName = map.get("entity_name") == null ? null : map.get(
				"entity_name").toString();
		if (this.checkParamIsNull(entityName)) {
			bean.appnedMessage("entity_name不能为空");
			return bean;
		}
		
		BufferedReader reader = null;
		try {
			byte[] bytes=(byte[])map.get("point_list");
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
			String line=reader.readLine();
			List<String> pointList=new ArrayList<String>();
			int count=0;
			while((line=reader.readLine())!=null){
				if(++count>300){
					bean.appnedMessage("point_list记录超过300条");
					return bean;
				}
				pointList.add(line);
			}
			map.put("point_list",pointList);
		} catch (IOException e) {
			bean.appnedMessage("point_list读取失败");
			return bean;
		}finally{
			try {
				if(reader!=null) reader.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}

		bean.setSuccess(true);
		return bean;
	}
}
