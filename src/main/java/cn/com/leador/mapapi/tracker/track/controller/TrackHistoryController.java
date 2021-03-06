package cn.com.leador.mapapi.tracker.track.controller;

import java.util.List;

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
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
import cn.com.leador.mapapi.tracker.track.input.TrackHistoryInput;
import cn.com.leador.mapapi.tracker.track.output.TrackHistoryOutput;
@WebController
public class TrackHistoryController extends HttpServletProxy {
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private CommonInputProxy<List<TrackBean>> input=null;
	private CommonOutputProxy<List<TrackBean>> trans=null;
	@PostConstruct
	private void init(){
		if(logger.isDebugEnabled()){
			logger.debug("初始化"+this.getClass());
		}
		input=SpringContextHelper.getBeanByType(TrackHistoryInput.class);
		trans=SpringContextHelper.getBeanByType(TrackHistoryOutput.class);
	}

	@Override
	@UriMapping(value="/track/gethistory",sid="100010",name="track历史记录查询")
	public void commonProceed(HttpServletRequest request,
			HttpServletResponse response, ClientInfo clientInfo) {
		ResultBean<List<TrackBean>> bean=null;
		try {
			bean=input.inputAndProcess(clientInfo, request);
			clientInfo.setResultMap(bean.getExtraResult());
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
