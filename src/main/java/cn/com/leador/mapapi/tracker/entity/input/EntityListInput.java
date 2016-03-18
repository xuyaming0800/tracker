package cn.com.leador.mapapi.tracker.entity.input;

import java.util.HashMap;
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
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.ENTITY_RETURN_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.HTTP_METHOD;
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.entity.bean.EntityLocationBean;
import cn.com.leador.mapapi.tracker.entity.service.EntityService;
import cn.com.leador.mapapi.tracker.entity.service.impl.EntityServiceImpl;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

@Component
public class EntityListInput extends CommonInputProxy<List<EntityBean>> {

	private EntityService<EntityBean, EntityBean> entityService = null;

	@PostConstruct
	private void init() {
		entityService = SpringContextHelper
				.getBeanByType(EntityServiceImpl.class);
	}

	@Override
	protected ResultBean<List<EntityBean>> getSearchMethod(
			ClientInfo clientInfo, HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		EntityBean bean = new EntityBean();
		EntityLocationBean locationBean = new EntityLocationBean();
		bean.setCustom_field(new HashMap<String, Object>());
		for (String key : clientInfo.getOrginParamNames()) {
			if (key.equals("ak")) {
				bean.setAk(map.get(key));
			} else if (key.equals("service_id")) {
				bean.setService_id(map.get(key));
			} else if (key.equals("active_time")) {
				if (map.get(key) != null) {
					locationBean.setLoc_time(Long.valueOf(map.get(key)));
					bean.setRealtime_point(locationBean);
				}
			} else if (key.equals("return_type")) {
				if (map.get(key) != null) {
					bean.setReturn_type(Integer.parseInt(map.get(key)));
				}
			} else if (key.equals("page_index")) {
				if (map.get(key) != null) {
					bean.setPage_index(Integer.parseInt(map.get(key)));
				}
			} else if (key.equals("page_size")) {
				if (map.get(key) != null) {
					bean.setPage_size(Integer.parseInt(map.get(key)));
				}
			} else if (key.equals("entity_names")) {
				if (map.get(key) != null) {
					bean.setEntity_names(map.get(key).split(","));
				}
			} else {
				bean.getCustom_field().put(key,
						map.get(key) == null ? "" : map.get(key));
			}
		}
		ResultBean<List<EntityBean>> result = entityService.listEntity(bean);
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
		// 设置默认值
		String activeTime = map.get("active_time");
		if (this.checkParamIsNull(activeTime)) {
			map.put("active_time", null);
		} else {
			if (!this.checkParamIsNumber(activeTime, Long.class)) {
				bean.appnedMessage("active_time不是整数");
				return bean;
			}
			long num = Long.parseLong(activeTime);
			if (num < 1L || num > 2147483647L) {
				bean.appnedMessage("active_time 最小值是1;最大值是2147483647");
				return bean;
			}
		}
		String entityNames = map.get("entity_names");
		if (this.checkParamIsNull(entityNames)) {
			map.put("entity_names", null);
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
			if (!this.checkParamIsNumber(pageSize, Integer.class)) {
				bean.appnedMessage("page_size 不是整数");
				return bean;
			}
			int num = Integer.parseInt(pageSize);
			if (num < 1 || num > 100) {
				bean.appnedMessage("page_size 最小值是1;最大值是100");
				return bean;
			}
		}
		String returnType = map.get("return_type");
		if (this.checkParamIsNull(returnType)) {
			map.put("return_type", "0");
			clientInfo.getOrginParamNames().add("return_type");
		} else {
			if (!this.checkParamIsNumber(returnType, Integer.class)) {
				bean.appnedMessage("return_type 不是整数");
				return bean;
			}
			if (!this
					.checkParamIsInAllowed(
							returnType,
							new String[] {
									String.valueOf(ENTITY_RETURN_TYPE.FULL
											.getStatus()),
									String.valueOf(ENTITY_RETURN_TYPE.SIMPLE
											.getStatus()) })) {
				bean.appnedMessage("return_type 为0或者1");
				return bean;
			}
		}

		bean.setSuccess(true);
		return bean;
	}

}
