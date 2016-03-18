package cn.com.leador.mapapi.tracker.app.controller;

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
import cn.com.leador.mapapi.tracker.app.bean.AppServiceBean;
import cn.com.leador.mapapi.tracker.app.controller.input.AppServiceAddInput;
import cn.com.leador.mapapi.tracker.app.controller.output.AppServiceAddOutput;
@WebController
public class AppSeviceAddController extends HttpServletProxy {
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private CommonInputProxy<AppServiceBean> input=null;
	private CommonOutputProxy<AppServiceBean> trans=null;
	@PostConstruct
	private void init(){
		if(logger.isDebugEnabled()){
			logger.debug("初始化"+this.getClass());
		}
		input=SpringContextHelper.getBeanByType(AppServiceAddInput.class);
		trans=SpringContextHelper.getBeanByType(AppServiceAddOutput.class);
	}

	@Override
	@UriMapping(value="/service/add",sid="100001",name="增加鹰眼服务")
	public void commonProceed(HttpServletRequest request,
			HttpServletResponse response, ClientInfo clientInfo) {
		ResultBean<AppServiceBean> bean=null;
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
