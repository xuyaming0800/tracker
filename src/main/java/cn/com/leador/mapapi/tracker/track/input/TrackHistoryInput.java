package cn.com.leador.mapapi.tracker.track.input;

import java.util.List;
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
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.IS_PROCESSED;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_RETURN_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACK_TIME_SORT_TYPE;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
import cn.com.leador.mapapi.tracker.track.service.TrackService;
import cn.com.leador.mapapi.tracker.track.service.impl.TrackServiceImpl;

@Component
public class TrackHistoryInput extends CommonInputProxy<List<TrackBean>> {

	private TrackService<TrackBean, TrackBean> trackService = null;

	@PostConstruct
	private void init() {
		trackService = SpringContextHelper
				.getBeanByType(TrackServiceImpl.class);
	}

	@Override
	protected ResultBean<List<TrackBean>> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		TrackBean bean = new TrackBean();
		bean.setAk(map.get("ak"));
		bean.setService_id(map.get("service_id"));
		bean.setEntity_name(map.get("entity_name"));
		bean.setStart_time(new Long(map.get("start_time")));
		bean.setEnd_time(new Long(map.get("end_time")));
		bean.setSort_type(new Integer(map.get("sort_type")));
		bean.setSimple_return(new Integer(map.get("simple_return")));
		bean.setIs_processed(new Integer(map.get("is_processed")));
		bean.setPage_index(new Integer(map.get("page_index")));
		bean.setPage_size(new Integer(map.get("page_size")));
		ResultBean<List<TrackBean>> result = trackService.getHistory(bean);
		return result;
	}

	@Override
	protected BusinessExceptionBean checkInput(ClientInfo clientInfo,
			HttpServletRequest request) {
		BusinessExceptionBean bean = null;
		// 校验method only-post
		if (!request.getMethod().toUpperCase()
				.equals(HTTP_METHOD.GET.getMethod())) {
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
		bean.setSuccess(false);
		if (this.checkParamIsNull(ak)) {
			bean.appnedMessage("ak不能为空");
			return bean;
		}

		String serviceId = map.get("service_id");
		if (this.checkParamIsNull(serviceId)) {
			bean.appnedMessage("service_id不能为空");
			return bean;
		}
		String entityName = map.get("entity_name");
		if (this.checkParamIsNull(entityName)) {
			bean.appnedMessage("entity_name不能为空");
			return bean;
		}
		String startTime = map.get("start_time");
		if (this.checkParamIsNull(startTime)) {
			bean.appnedMessage("start_time不能为空");
			return bean;
		}
		Long start = 0L;
		try {
			start = Long.valueOf(startTime);
		} catch (NumberFormatException e) {
			bean.appnedMessage("start_time必须为整型数据");
			return bean;
		}

		String endTime = map.get("end_time");
		if (this.checkParamIsNull(endTime)) {
			bean.appnedMessage("end_time不能为空");
			return bean;
		}
		Long end = 0L;
		try {
			end = Long.valueOf(endTime);
		} catch (NumberFormatException e) {
			bean.appnedMessage("end_time必须为整型数据");
			return bean;
		}
		if (end <= start || end - start > 24 * 60 * 60) {
			bean.appnedMessage("end_time必须大于start_time 且在24小时之内");
			return bean;
		}

		String sortType = map.get("sort_type");
		if (this.checkParamIsNull(sortType)) {
			map.put("sort_type",
					String.valueOf(TRACK_TIME_SORT_TYPE.descending.getType()));
		} else {
			if (!this.checkParamIsInAllowed(
					sortType,
					new String[] {
							String.valueOf(TRACK_TIME_SORT_TYPE.descending
									.getType()),
							String.valueOf(TRACK_TIME_SORT_TYPE.ascending
									.getType()) })) {
				bean.appnedMessage("sort_type值错误");
				return bean;

			}
		}

		String simpleReturn = map.get("simple_return");
		if (this.checkParamIsNull(simpleReturn)) {
			map.put("simple_return",
					String.valueOf(TRACK_RETURN_TYPE.FULL.getStatus()));
		} else {
			if (!this
					.checkParamIsInAllowed(
							simpleReturn,
							new String[] {
									String.valueOf(TRACK_RETURN_TYPE.FULL
											.getStatus()),
									String.valueOf(TRACK_RETURN_TYPE.SIMPLE
											.getStatus()) })) {
				bean.appnedMessage("simple_return值错误");
				return bean;

			}
		}

		String isProcessed = map.get("is_processed");
		if (this.checkParamIsNull(isProcessed)) {
			map.put("is_processed", String.valueOf(IS_PROCESSED.NO.getType()));
		} else {
			if (!this.checkParamIsInAllowed(isProcessed,
					new String[] { String.valueOf(IS_PROCESSED.NO.getType()),
							String.valueOf(IS_PROCESSED.YES.getType()) })) {
				bean.appnedMessage("is_processed值错误");
				return bean;

			}
		}

		String pageIndex = map.get("page_index");
		if (this.checkParamIsNull(pageIndex)) {
			map.put("page_index", "1");
			clientInfo.getOrginParamNames().add("page_index");
		} else {
			if (!this.checkParamIsNumber(pageIndex, Integer.class)) {
				bean.appnedMessage("page_index 不是整数");
				return bean;
			}
		}
		String pageSize = map.get("page_size");
		if (this.checkParamIsNull(pageSize)) {
			map.put("page_size", "20");
			clientInfo.getOrginParamNames().add("page_size");
		} else {
			int num = 0;
			try {
				num=Integer.parseInt(pageSize);
			} catch (NumberFormatException e) {
				bean.appnedMessage("page_size 不是整数");
				return bean;
			}
			if (num < 1 || num > 100) {
				bean.appnedMessage("page_size 最小值是1;最大值是100");
				return bean;
			}
		}
		bean.setSuccess(true);
		return bean;
	}
}
