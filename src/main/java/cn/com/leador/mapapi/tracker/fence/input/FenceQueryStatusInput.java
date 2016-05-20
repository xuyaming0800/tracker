package cn.com.leador.mapapi.tracker.fence.input;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
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
import cn.com.leador.mapapi.tracker.fence.bean.FenceBean;
import cn.com.leador.mapapi.tracker.fence.service.FenceService;
import cn.com.leador.mapapi.tracker.fence.service.impl.FenceServiceImpl;
import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class FenceQueryStatusInput extends CommonInputProxy<FenceBean> {

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

	@SuppressWarnings("unchecked")
	@Override
	protected ResultBean<FenceBean> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		FenceBean bean=new FenceBean();
		bean.setAk(map.get("ak"));
		bean.setService_id(map.get("service_id"));
		bean.setFence_id(map.get("fence_id"));
		if(!this.checkParamIsNull(map.get("monitored_persons"))){
			String[] person=map.get("monitored_persons").split(",");
			bean.setMonitored_persons(Arrays.asList(person));
		}
		return fenceService.listFenceStatus(bean);
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
		
		String fenceId = map.get("fence_ids");
		if (this.checkParamIsNull(fenceId)) {
			bean.appnedMessage("fence_id不能为空");
			return bean;
		}
		
		String monitored_persons = map.get("monitored_persons");
		if (!this.checkParamIsNull(monitored_persons)) {
			String[] persons=monitored_persons.split(",");
			if(persons.length>maxCount){
				bean.appnedMessage("monitored_persons最多为5个");
				return bean;
			}
		}
		
		


		bean.setSuccess(true);
		return bean;
	}
}
