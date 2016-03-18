package cn.com.leador.mapapi.tracker.entity.input;

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
import cn.com.leador.mapapi.tracker.entity.bean.EntityColumnBean;
import cn.com.leador.mapapi.tracker.entity.service.EntityColumnService;
import cn.com.leador.mapapi.tracker.entity.service.impl.EntityColumnServiceImpl;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
@Component
public class EntityColumnListInput extends CommonInputProxy<List<EntityColumnBean>> {
	
	private EntityColumnService<EntityColumnBean,EntityColumnBean> entityColumnService=null;
	
	@PostConstruct
	private void init(){
		entityColumnService=SpringContextHelper.getBeanByType(EntityColumnServiceImpl.class);
	}

	@Override
	protected ResultBean<List<EntityColumnBean>> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		EntityColumnBean bean=new EntityColumnBean();
		bean.setAk(map.get("ak"));
		bean.setService_id(map.get("service_id"));
		ResultBean<List<EntityColumnBean>> result=entityColumnService.listColumn(bean);
		return result;
	}

	@Override
	protected BusinessExceptionBean checkInput(ClientInfo clientInfo,
			HttpServletRequest request) {
		BusinessExceptionBean bean = null;
		// 校验method only-get
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
		bean.setSuccess(true);
		return bean;
	}

}
