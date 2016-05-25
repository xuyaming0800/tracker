package cn.com.leador.mapapi.tracker.util.mq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import cn.com.leador.mapapi.common.util.json.JsonBinder;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQUtils {

	private static Logger logger = LogManager.getLogger(RabbitMQUtils.class);
	private static final Map<String, ConnectionFactory> resourceMap = new HashMap<String, ConnectionFactory>();
	private static JsonBinder jsonBinder = JsonBinder.buildNonNullBinder(false);

	@Value("${mq.consumer_count}")
	private Integer consumerCount;
	@Value("${mq.tracker_queue}")
	private String queueName;

	private static ExecutorService cachedThreadPool = Executors
			.newFixedThreadPool(1000);

	@SuppressWarnings("unused")
	private SerializationType serializationType = SerializationType.jackson;

	public void setSerializationType(SerializationType serializationType) {
		this.serializationType = serializationType;
	}

	/**
	 * 更新内存数据
	 * 
	 * @param id
	 * @param url
	 * @param username
	 * @param password
	 */
	public void updateResourceMap(String id, String host, int port,
			String username, String password) {
		ConnectionFactory factory = null;
		factory = new ConnectionFactory();
		if (host != null && !host.isEmpty())
			factory.setHost(host);
		if (port > 0)
			factory.setPort(port);
		if (username != null && !username.isEmpty())
			factory.setUsername(username);
		if (password != null && !password.isEmpty())
			factory.setPassword(password);
		resourceMap.put(id, factory);
	}

	/**
	 * 发送-非应答
	 * 
	 * @param id
	 * @param url
	 * @param username
	 * @param password
	 * @param queueName
	 * @param message
	 * @throws Exception
	 */
	public static void send(String id, String host, int port, String username,
			String password, String queueName, byte[] message, boolean isUpdate)
			throws Exception {
		ConnectionFactory factory = getConnectionFectory(id, host, port,
				username, password, isUpdate);
		Connection connection = null;
		Channel channel = null;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.addReturnListener(new ReturnListener() {// 添加basic.return方法监听。
				@Override
				public void handleReturn(int arg0, String arg1, String arg2,
						String arg3, BasicProperties arg4, byte[] arg5)
						throws IOException {
					logger.entry(arg0, arg1, arg2, arg3, arg4, arg5);
				}
			});
			channel.queueDeclare(queueName, true, false, false, null);
			// channel.confirmSelect();
			channel.basicPublish("",// exchange
					queueName,// routekey
					// true,// mandatory flag
					// false,// immediate flag
					null,// props
					message// body
			);
			// channel.waitForConfirmsOrDie();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (channel != null)
				channel.close();
			if (connection != null)
				connection.close();
		}
	}

	public static void send(String id, String host, int port, String username,
			String password, String exchange, String routeKey, byte[] message,
			boolean isUpdate) throws Exception {
		ConnectionFactory factory = getConnectionFectory(id, host, port,
				username, password, isUpdate);
		Connection connection = null;
		Channel channel = null;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
//			channel.addReturnListener(new ReturnListener() {// 添加basic.return方法监听。
//				@Override
//				public void handleReturn(int arg0, String arg1, String arg2,
//						String arg3, BasicProperties arg4, byte[] arg5)
//						throws IOException {
//					logger.entry(arg0, arg1, arg2, arg3, arg4, arg5);
//				}
//			});
			// channel.queueDeclare(queueName, true, false, false, null);
			// channel.confirmSelect();
			channel.basicPublish(exchange,// exchange
					routeKey,// routekey
					// true,// mandatory flag
					// false,// immediate flag
					null,// props
					message// body
			);

			// channel.waitForConfirmsOrDie();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (channel != null)
				channel.close();
			if (connection != null)
				connection.close();
		}
	}

	/**
	 * 发送--应答
	 * 
	 * @param id
	 * @param url
	 * @param username
	 * @param password
	 * @param queueName
	 * @param message
	 * @throws Exception
	 */
	public static void sendAck(String id, String host, int port,
			String username, String password, String queueName, byte[] message,
			boolean isUpdate) throws Exception {
		ConnectionFactory factory = getConnectionFectory(id, host, port,
				username, password, isUpdate);
		Connection connection = null;
		Channel channel = null;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.addReturnListener(new ReturnListener() {// 添加basic.return方法监听。
				@Override
				public void handleReturn(int arg0, String arg1, String arg2,
						String arg3, BasicProperties arg4, byte[] arg5)
						throws IOException {
					logger.entry(arg0, arg1, arg2, arg3, arg4, arg5);
				}
			});
			channel.queueDeclare(queueName, true, false, false, null);
			channel.confirmSelect();
			channel.basicPublish("",// exchange
					queueName,// routekey
					true,// mandatory flag
					false,// immediate flag
					null,// props
					message// body
			);
			channel.waitForConfirmsOrDie();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (channel != null)
				channel.close();
			if (connection != null)
				connection.close();
		}
	}

	/**
	 * 获得连接工厂
	 * 
	 * @param id
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	private static ConnectionFactory getConnectionFectory(String id,
			String host, int port, String username, String password,
			boolean isUpdate) {
		ConnectionFactory factory = null;
		if (resourceMap.containsKey(id) && !isUpdate) {
			factory = resourceMap.get(id);
		} else {
			factory = new ConnectionFactory();
			if (host != null && !host.isEmpty())
				factory.setHost(host);
			if (port > 0)
				factory.setPort(port);
			if (username != null && !username.isEmpty())
				factory.setUsername(username);
			if (password != null && !password.isEmpty())
				factory.setPassword(password);
			resourceMap.put(id, factory);
		}
		return factory;
	}

	/**
	 * 发送
	 * 
	 * @param id
	 * @param queueName
	 * @param message
	 * @throws Exception
	 */
	public static void send(String id, String queueName, byte[] message)
			throws Exception {
		send(id, null, -1, null, null, queueName, message, false);
	}

	/**
	 * 发送<br/>
	 * 使用jackson序列化
	 * 
	 * @param id
	 * @param url
	 * @param username
	 * @param password
	 * @param queueName
	 * @param message
	 * @throws Exception
	 */
	public static void send(String id, String host, int port, String username,
			String password, String queueName, Object message, boolean isUpdate)
			throws Exception {
		byte[] bm = jsonBinder.toJson(message).getBytes("utf-8");
		send(id, host, port, username, password, queueName, bm, isUpdate);
	}

	public static void sendToQueue(final String id, final String host,
			final int port, final String username, final String password,
			final String exchange, final String routeKey, final Object message,
			final boolean isUpdate) throws Exception {
		cachedThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					byte[] bm = jsonBinder.toJson(message).getBytes("utf-8");
					send(id, host, port, username, password, exchange,
							routeKey, bm, isUpdate);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});
	}

	/**
	 * 发送<br/>
	 * 使用jackson序列化
	 * 
	 * @param id
	 * @param queueName
	 * @param message
	 * @throws Exception
	 */
	public static void send(String id, String queueName, Object message)
			throws Exception {
		byte[] bm = jsonBinder.toJson(message).getBytes("utf-8");
		send(id, null, -1, null, null, queueName, bm, false);
	}

	/**
	 * 消费
	 * 
	 * @param id
	 *            业务系统ID
	 * @param host
	 *            地址
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @param queueName
	 *            队列名
	 * @param clazz
	 *            如果传递此项，则通过jackson进行序列化
	 * @param isUpdate
	 *            是否更新缓存
	 * @param handler
	 *            得到消息后，会通过该接口传递结果
	 * @throws IOException
	 * @throws ShutdownSignalException
	 * @throws ConsumerCancelledException
	 * @throws InterruptedException
	 */
	public static <T> void receive(String id, String host, int port,
			String username, String password, String queueName,
			String exchange, String routeKey, Class<T> clazz, boolean isUpdate,
			RabbitMQMessageHandler handler) throws IOException,
			ShutdownSignalException, ConsumerCancelledException,
			InterruptedException {
		ConnectionFactory factory = getConnectionFectory(id, host, port,
				username, password, isUpdate);
		Connection connection = null;
		Channel channel = null;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			//设置初始化路由
			channel.exchangeDeclare(exchange, "direct",true,false,false,null);
			// channel.basicQos(1);// 实现公平调度的方式就是让每个消费者在同一时刻会分配一个任务。
			// boolean durable = true;
			channel.queueDeclare(queueName+"_"+routeKey, true, false, false, null);
			channel.queueBind(queueName+"_"+routeKey, exchange, routeKey);
			QueueingConsumer consumer = new QueueingConsumer(channel);

			// 取消 autoAck
			boolean autoAck = false;
			channel.basicConsume(queueName+"_"+routeKey, autoAck, consumer);
			while (true) {
				QueueingConsumer.Delivery delivery = null;
				try {
					delivery = consumer.nextDelivery();
					logger.trace(new String(delivery.getBody()));
					if (clazz != null) {
						handler.setMessage(jsonBinder.fromJson(new String(
								delivery.getBody(), "utf-8"), clazz));
					} else
						handler.setMessage(delivery.getBody());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					try {
						// 确认消息，已经收到
						channel.basicAck(delivery.getEnvelope()
								.getDeliveryTag(), false);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		} finally {
			if (channel != null)
				channel.close();
			if (connection != null)
				connection.close();
		}
	}
}
