package cn.com.leador.mapapi.tracker.app.controller.input;

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
import cn.com.leador.mapapi.tracker.app.bean.AppServiceBean;
import cn.com.leador.mapapi.tracker.app.service.AppService;
import cn.com.leador.mapapi.tracker.app.service.impl.AppServiceImpl;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.HTTP_METHOD;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.TRACKER_SERVICE_TYPE;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
@Component
public class AppServiceAddInput extends CommonInputProxy<AppServiceBean> {
	
	private AppService appService=null;
	
	@PostConstruct
	private void init(){
		appService=SpringContextHelper.getBeanByType(AppServiceImpl.class);
	}

	@Override
	protected ResultBean<AppServiceBean> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		AppServiceBean bean=new AppServiceBean();
		bean.setName(map.get("name"));
		bean.setType(map.get("type").toUpperCase());
		bean.setAk(map.get("ak"));
		bean.setDesc(map.get("desc")==null?"":map.get("desc"));
		ResultBean<AppServiceBean> result=appService.addAppService(bean);
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
		String serviceType = map.get("type");
		if (this.checkParamIsNull(serviceType)) {
			bean.setSuccess(false);
			bean.appnedMessage("type不能为空");
			return bean;
		}
		if (!this.checkParamIsInAllowed(serviceType.toUpperCase(),
				new String[] { TRACKER_SERVICE_TYPE.CAR.getCode(),
						TRACKER_SERVICE_TYPE.MTK.getCode(),
						TRACKER_SERVICE_TYPE.O2O.getCode(),
						TRACKER_SERVICE_TYPE.OTHER.getCode() })) {
			bean.setSuccess(false);
			bean.appnedMessage("type类型错误");
			return bean;
		}
		String serviceName = map.get("name");
		if (this.checkParamIsNull(serviceName)) {
			bean.appnedMessage("name不能为空");
			return bean;
		}
		bean.setSuccess(true);
		return bean;
	}

}
