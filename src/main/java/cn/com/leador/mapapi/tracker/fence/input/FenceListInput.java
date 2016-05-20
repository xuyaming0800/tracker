package cn.com.leador.mapapi.tracker.fence.input;

import java.util.List;
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
public class FenceListInput extends CommonInputProxy<List<FenceBean>> {

	private FenceService<FenceBean, FenceBean> fenceService = null;
	
	@Value("${fence.max_count.entity}")
	private Integer maxCount;

	@Value("${fence.max_count.per.entity}")
	private Integer perMaxCount;

	@Value("${fence.max_count.id}")
	private Integer maxIdCount;

	@PostConstruct
	private void init() {
		fenceService = SpringContextHelper
				.getBeanByType(FenceServiceImpl.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ResultBean<List<FenceBean>> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		FenceBean bean=new FenceBean();
		bean.setAk(map.get("ak"));
		bean.setService_id(map.get("service_id"));
		bean.setFence_ids(Arrays.asList(map.get("fence_ids").split(",")));
		return fenceService.listFence(bean);
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
		
		String fenceIds = map.get("fence_ids");
		if (this.checkParamIsNull(fenceIds)) {
			bean.appnedMessage("fence_ids不能为空");
			return bean;
		}
		if(fenceIds.split(",").length>maxIdCount){
			bean.appnedMessage("fence_ids数量过多");
			return bean;
		}
		
		
		


		bean.setSuccess(true);
		return bean;
	}
}
