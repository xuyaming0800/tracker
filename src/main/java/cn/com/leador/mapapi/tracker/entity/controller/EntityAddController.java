package cn.com.leador.mapapi.tracker.entity.controller;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.com.leador.mapapi.common.annotation.UriMapping;
import cn.com.leador.mapapi.common.annotation.WebController;
import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.helper.SpringContextHelper;
import cn.com.leador.mapapi.common.proxy.CommonInputProxy;
import cn.com.leador.mapapi.common.proxy.CommonOutputProxy;
import cn.com.leador.mapapi.common.proxy.HttpServletProxy;
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.entity.input.EntityAddInput;
import cn.com.leador.mapapi.tracker.entity.output.EntityAddOutput;
@WebController
public class EntityAddController extends HttpServletProxy {
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private CommonInputProxy<EntityBean> input=null;
	private CommonOutputProxy<EntityBean> trans=null;
	@PostConstruct
	private void init(){
		if(logger.isDebugEnabled()){
			logger.debug("初始化"+this.getClass());
		}
		input=SpringContextHelper.getBeanByType(EntityAddInput.class);
		trans=SpringContextHelper.getBeanByType(EntityAddOutput.class);
	}

	@Override
	@UriMapping(value="/entity/add",sid="100004",name="增加实体")
	public void commonProceed(HttpServletRequest request,
			HttpServletResponse response, ClientInfo clientInfo) {
		ResultBean<EntityBean> bean=null;
		try {
			bean=input.inputAndProcess(clientInfo, request);
			trans.printOut(response, bean, clientInfo);
		} catch (Exception e) {
			this.writeErrorMessage(response, trans, bean, clientInfo, e);
		}

	}

	@Override
	public void streamProceed(HttpServletResponse response, byte[] bytes) {
		// TODO Auto-generated method stub

	}

}
