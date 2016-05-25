package cn.com.leador.mapapi.tracker.component;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 坐标计算工具
 * 
 * @author xuyaming
 *
 */
@Component
public class CoordUtilComponent {
	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(this.getClass());
	/**
	 * 地球半径 单位米
	 */
	private final double EARTH_RADIUS = 6371229;

	/**
	 * 计算两点间距离 单位米
	 * 
	 * @param x1
	 *            精度
	 * @param y1
	 *            维度
	 * @param x2
	 *            精度
	 * @param y2
	 *            维度
	 * @return
	 */
	public double getDistance(double x1, double y1, double x2, double y2) {
		double x, y, distance;
		x = (x2 - x1) * Math.PI * EARTH_RADIUS
				* Math.cos(((y2 + y1) / 2) * Math.PI / 180) / 180;
		y = (y2 - y1) * Math.PI * EARTH_RADIUS / 180;
		distance = Math.hypot(x, y);
		BigDecimal b = new BigDecimal(distance);
		return b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 计算方向 正北为0度 顺时针旋转360度
	 * 
	 * @param x1
	 *            精度
	 * @param y1
	 *            维度
	 * @param x2
	 *            精度
	 * @param y2
	 *            维度
	 * @return
	 */
	public Integer getDirection(double x1, double y1, double x2, double y2) {
		if(x1==x2&&y1==y2){
			return null;
		}
		// 弧度制
		Double direction = Math.atan2(y2 - y1, x2 - x1);
		// 弧度转角度 角度转正北为零度
		direction = 90 - direction * 180 / Math.PI;
		if (direction < 0) {
			direction = 360.0D + direction;
		}
		return direction.intValue();

	}
	
	public Double getSpeed(double distance,
			Long timeSplit) {
		// 转换为千米
		distance = distance / 1000;
		double speed = distance * (60 * 60 / timeSplit);
		BigDecimal b = new BigDecimal(speed);
		return b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 计算速度 单位 km/h
	 * 
	 * @param x1
	 *            精度
	 * @param y1
	 *            维度
	 * @param x2
	 *            精度
	 * @param y2
	 *            维度
	 * @param timeSplit
	 *            时间间隔 单位秒
	 * @return
	 */
	public Double getSpeed(double x1, double y1, double x2, double y2,
			Long timeSplit) {
		// 获取距离 单位米
		double distance = this.getDistance(x1, y1, x2, y2);
		// 转换为千米
		distance = distance / 1000;
		double speed = distance * (60 * 60 / timeSplit);
		BigDecimal b = new BigDecimal(speed);
		return b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public static void main(String[] argu){
		CoordUtilComponent a=new CoordUtilComponent();
		System.out.println(a.getDirection(116.43149900000024, 39.899993, 116.43099900000024, 39.899993));
	}

}
