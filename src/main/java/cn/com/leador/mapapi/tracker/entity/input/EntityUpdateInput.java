package cn.com.leador.mapapi.tracker.entity.input;

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
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.entity.service.EntityService;
import cn.com.leador.mapapi.tracker.entity.service.impl.EntityServiceImpl;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionBean;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
@Component
public class EntityUpdateInput extends CommonInputProxy<EntityBean> {
	
	private EntityService<EntityBean,EntityBean> entityService=null;
	
	@PostConstruct
	private void init(){
		entityService=SpringContextHelper.getBeanByType(EntityServiceImpl.class);
	}

	@Override
	protected ResultBean<EntityBean> getSearchMethod(ClientInfo clientInfo,
			HttpServletRequest request) throws Exception {
		Map<String, String> map = clientInfo.getInfoTable();
		EntityBean bean=new EntityBean();
		bean.setCustom_field(new HashMap<String,Object>());
		for(String key:clientInfo.getOrginParamNames()){
			if(key.equals("ak")){
				bean.setAk(map.get(key));
			}else if(key.equals("service_id")){
				bean.setService_id(map.get(key));
			}else if(key.equals("entity_name")){
				bean.setEntity_name(map.get(key));
			}else{
				bean.getCustom_field().put(key, map.get(key)==null?"":map.get(key));
			}
		}
		ResultBean<EntityBean> result=entityService.updateEntity(bean);
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
		String entityName = map.get("entity_name");
		if (this.checkParamIsNull(entityName)) {
			bean.appnedMessage("entity_name不能为空");
			return bean;
		}
		if(entityName.length()>128){
			bean.appnedMessage("entity_name过长");
			return bean;
		}
		
		bean.setSuccess(true);
		return bean;
	}

}
