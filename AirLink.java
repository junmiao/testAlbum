package com.ifenglian.airlink;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;

import com.ifenglian.airlink.bean.Is360InfoBean;
import com.ifenglian.airlink.bean.PluginsBean;
import com.ifenglian.airlink.local.AirLinkGareway;
import com.ifenglian.airlink.local.AirLinkLocal;
import com.ifenglian.airlink.local.LinkParam;
import com.ifenglian.airlink.local.LinkParamStatic;
import com.ifenglian.airlink.local.WifiScaner;
import com.ifenglian.airlink.task.AlDumpScanDevTask;
import com.ifenglian.airlink.task.RouterFunctionListTask;
import com.ifenglian.airlink.task.TaskCallback;
import com.ifenglian.airlink.util.LogUtil;

public class AirLink {

	public static final String TAG = "airlink";

	public static final String TAGTASK = "airlinktask";

	/**
	 * 查询路由器信息
	 * @param onCheckBeaconListener
	 */
	public static void checkInfo(Context context, final OnCheckInfoListener onCheckInfoListener) {
		new RouterFunctionListTask(context).execute(new TaskCallback<Is360InfoBean>() {

			@Override
			public void handlePostExecute(int errCode, String errMsg, Is360InfoBean retObj) {
				// String model, String romVersion, boolean supportBeacon
				if (errCode == 0) {
					ArrayList<PluginsBean> list = retObj.getList();
					boolean supportBeacon = false;
					for (PluginsBean pluginsBean : list) {
						if (pluginsBean.getName().equals("airlink_app")) {
							String feature = pluginsBean.getFeature();
							if (feature.contains("beacon")) {
								supportBeacon = true;
							}
						}
					}
					LogUtil.d(TAG, "CheckInfoTask success " + retObj.getModel() + " " + retObj.getFw_ver() + " supportBeacon "
							+ supportBeacon);
					onCheckInfoListener.onCheckInfoResult(retObj.getModel(), retObj.getFw_ver(), supportBeacon);
				} else {
					LogUtil.d(TAG, "CheckInfoTask fail errCode " + errCode + "  " + errMsg + "  " + retObj);
					onCheckInfoListener.onCheckInfoResult(null, null, false);
				}
			}
		});
	}

	/**
	 * 扫描未入网智能设备
	 * @param scanWithGatewayForce
	 *            强制使用网关扫描未入网设备。默认为 false
	 */
	public static void scanUnLinkedDevice(Context context, final OnScanUnLinkedDeviceListener onScanUnLinkedDeviceListener,
			boolean scanWithGatewayForce) {
		if (scanWithGatewayForce) {
			new AlDumpScanDevTask(context).execute(new TaskCallback<JSONArray>() {

				@Override
				public void handlePostExecute(int errCode, String errMsg, JSONArray unLinkedDevices) {
					// String model, String romVersion, boolean supportBeacon
					onScanUnLinkedDeviceListener.onScanResult(unLinkedDevices);
				}
			});
		} else {
			WifiScaner wifiScaner = new WifiScaner(context, onScanUnLinkedDeviceListener);
			wifiScaner.scanStart();
		}
	}

	/**
	 * 用于本地组播、广播快连的高级参数配置，如果不配置，则采用默认值
	 * @param sleep
	 *            为防止网络拥堵，发送完整数据后，暂停发送的时间。单位为microsecond，默认为10
	 * @param sleepframe
	 *            为防止网络拥堵，发送完整数据后，暂停发送的时间。单位为microsecond，默认为2750
	 * @param frameDivid
	 *            信标帧间隔。默认为5
	 * @param numberBroadcast
	 *            组播、广播循环发送，广播发送次数。默认为0
	 * @param numberMulticast
	 *            组播、广播循环发送，组播发送次数。默认为1
	 */
	public static void localLinkSetParams(int sleep, int sleepframe, int frameDivid, int numberBroadcast, int numberMulticast) {

		LinkParamStatic.setRoundTimes(numberMulticast, numberBroadcast);
		LinkParamStatic.setSendElapse(sleep, sleepframe, frameDivid);
	}

	/**
	 * 启动手机本地组播、广播快连
	 * @param onLinkListener
	 *            执行监听
	 * @param encryptKey
	 *            TEA加密数据用的key，长度16字节，否则采用默认值
	 * @param ssid
	 *            将要连接网关的SSID
	 * @param pwd
	 *            将要连接网关的密码
	 * @param extra
	 *            自定义数据
	 * @param runElapse
	 *            预定本次操作的时间，单位毫秒，传0则不自动停止。途中可手动停止，调用cancelLink()
	 */
	public static void localLinkStart(OnLinkListener onLinkListener, String encryptKey, String ssid, String pwd, String extra,
			int runElapse) {
		LinkParam linkParam = new LinkParam(encryptKey, ssid, pwd, extra, runElapse);
		AirLinkLocal.startLink(onLinkListener, linkParam);
	}

	/**
	 * 停止组播、广播
	 */
	public static void localLinkCancel(OnLinkCancelListener onLinkCancelListener) {
		boolean success = AirLinkLocal.stopLink();
		onLinkCancelListener.cancelSuccess(success);
	}

	/**
	 * 启动路由器快连（beacon方式），不需要输入SSID和密码
	 * @param onLinkListener
	 *            执行监听
	 * @param encryptKey
	 *            TEA加密数据用的key，长度16字节，否则无效
	 * @param extra
	 *            自定义数据
	 * @param runElapse
	 *            预定本次操作的时间，单位毫秒，传0则采用默认值，默认为60000。途中可手动停止，调用cancelLink()
	 */
	public static void gatewayLinkStart(Context context, final OnLinkListener onLinkListener, String encryptKey, String extra,
			int runElapse) {
		if (runElapse <= 0) {
			runElapse = 60000;
		}
		AirLinkGareway.gatewayLinkStart(context, onLinkListener, encryptKey, extra, runElapse);

	}

	/**
	 * 停止beacon
	 */
	public static void gatewayLinkCancel(Context context, final OnLinkCancelListener onLinkCancelListener) {
		AirLinkGareway.gatewayLinkCancel(context, onLinkCancelListener);
	}

	/**
	 * 执行Ping
	 * @param onPingListener
	 *            执行监听
	 * @param ip
	 *            目标ip地址
	 */
	public static void startPing(OnPingListener onPingListener, String ip) {

	}

	/**
	 * 执行绑定
	 * @param onBindListener
	 *            执行监听
	 * @param qt
	 *            360账号qt
	 * @param ip
	 *            目标ip地址
	 */
	public static void startBind(OnBindListener onBindListener, String qt, String ip) {

	}

	public interface OnCheckInfoListener {

		/**
		 * 查询结果
		 * @param model
		 *            网关型号 失败返回null
		 * @param romVersion
		 *            固件版本 失败返回null
		 * @param supportBeacon
		 *            是否支持beacon快连
		 */
		public void onCheckInfoResult(String model, String romVersion, boolean supportBeacon);
	}

	public interface OnScanUnLinkedDeviceListener {

		/**
		 * 返回未加密的无线热点的SSID和BSSID，当使用云查时，并有返回结果时，需要返回该设备的厂商、品牌、型号、扩展信息
		 * @param unLinkedDevices
		 */
		public void onScanResult(JSONArray unLinkedDevices);
	}

	/**
	 * 智能设备入网监听
	 */
	public interface OnLinkListener {

		/**
		 * 已启动快连
		 */
		public void onLinkStart();

		/**
		 * 实时返回已连上WiFi的设备列表
		 * @param results
		 *            已连上WiFi的设备列表，包含型号、厂商、品牌、MAC、bssid、ip地址
		 */
		public void onLinkResult(JSONArray results);

		/**
		 * 已停止快连
		 */
		public void onLinkEnd(String reason);
	}

	public interface OnLinkCancelListener {

		/**
		 * 停止快连
		 */
		public void cancelSuccess(boolean success);
	}

	/**
	 * Ping监听
	 */
	public interface OnPingListener {

		/**
		 * @param success
		 *            操作执行成功
		 */
		public void onPingResult(boolean success);
	}

	/**
	 * Bind监听
	 */
	public interface OnBindListener {

		/**
		 * @param success
		 *            绑定成功
		 */
		public void onBindResult(boolean success);
	}
}
