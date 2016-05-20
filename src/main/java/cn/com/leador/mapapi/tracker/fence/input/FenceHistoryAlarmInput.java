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
import cn.com.leador.mapapi.tracker.fence.bean.FenceAlarmBean;
import cn.com.leador.mapapi.tracker.fence.service.FenceAlarmService;
import cn.com.leador.mapapi.tracker.fence.service.impl.FenceAlarmServiceImpl;
import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class FenceHistoryAlarmInput extends CommonInputProxy<List<FenceAlarmBean>> {

	private FenceAlarmService<FenceAlarmBean, FenceAlarmBean> fenceAlarmService = null;
	
	@Value("${fence.max_count.entity}")
	private Integer maxCount;

	@Value("${fence.max_count.per.entity}")
	private Integer perMaxCount;


	@PostConstruct
	private void init() {
		fenceAlarmService = SpringContextHelper
				.getBeanByType(FenceAlarmServiceImpl.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ResultBean<List<FenceAlarmBean>> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		FenceAlarmBean bean=new FenceAlarmBean();
		bean.setAk(map.get("ak"));
		bean.setService_id(map.get("service_id"));
		bean.setFence_id(map.get("fence_id"));
		if(!this.checkParamIsNull(map.get("monitored_persons"))){
			String[] person=map.get("monitored_persons").split(",");
			bean.setMonitored_persons(Arrays.asList(person));
		}
		bean.setBegin_time(Long.parseLong(map.get("begin_time")));
		bean.setEnd_time(Long.parseLong(map.get("end_time")));
		bean.setPage_num(Integer.valueOf(map.get("page_num")));
		bean.setPage_size(Integer.valueOf(map.get("page_size")));
		return fenceAlarmService.listFenceAlarmByEntitys(bean);
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
		
		String fenceId = map.get("fence_id");
		if (this.checkParamIsNull(fenceId)) {
			bean.appnedMessage("fence_id不能为空");
			return bean;
		}
		String begin_time = map.get("begin_time");
		if (this.checkParamIsNull(begin_time)) {
			bean.appnedMessage("begin_time不能为空");
			return bean;
		}
		
		String end_time = map.get("end_time");
		if (this.checkParamIsNull(end_time)) {
			bean.appnedMessage("end_time不能为空");
			return bean;
		}
		
		String page_size = map.get("page_size");
		if (this.checkParamIsNull(page_size)) {
			page_size="20";
			map.put("page_size", page_size);
		}else{
			Integer size=Integer.valueOf(page_size);
			if(size<1&&size>100){
				bean.appnedMessage("page_size必须在1-100之间");
				return bean;
			}
		}
		String page_num = map.get("page_num");
		if (this.checkParamIsNull(page_num)) {
			page_num="1";
			map.put("page_num", page_num);
		}else{
			Integer num=Integer.valueOf(page_num);
			if(num<1){
				bean.appnedMessage("page_num必须大于或者等于1");
				return bean;
			}
		}
		
		
		
		try {
			Long begin=Long.valueOf(begin_time);
			Long end=Long.valueOf(end_time);
			Long split=end-begin;
			if(split<=0||split>24*60*60){
				bean.appnedMessage("时间间隔不能大于24小时");
				return bean;
			}
		} catch (NumberFormatException e) {
			bean.appnedMessage("时间格式错误");
			return bean;
		}
		
		
		String monitored_persons = map.get("monitored_persons");
		if (this.checkParamIsNull(monitored_persons)) {
			bean.appnedMessage("monitored_persons不能为空");
			return bean;
		}
		
		String[] persons=monitored_persons.split(",");
		if(persons.length>maxCount){
			bean.appnedMessage("monitored_persons最多为5个");
			return bean;
		}
		


		bean.setSuccess(true);
		return bean;
	}
}
