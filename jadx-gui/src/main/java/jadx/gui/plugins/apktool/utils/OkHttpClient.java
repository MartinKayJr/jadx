package jadx.gui.plugins.apktool.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author MartinKay
 * @since 2021-11-24 22:50
 */
public class OkHttpClient {
	private static final Logger LOG = LoggerFactory.getLogger(OkHttpClient.class);

	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private volatile static okhttp3.OkHttpClient client;

	private static final int MAX_IDLE_CONNECTION = 20000;

	private static final long KEEP_ALIVE_DURATION = 30000;

	private static final long CONNECT_TIMEOUT = 20000;

	private static final long READ_TIMEOUT =30000;

	/**
	 * 单例模式(双重检查模式) 获取类实例
	 *
	 * @return client
	 */
	private static okhttp3.OkHttpClient getInstance() {
		if (client == null) {
			synchronized (okhttp3.OkHttpClient.class) {
				if (client == null) {
					client = new okhttp3.OkHttpClient.Builder()
							.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
							.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
							.connectionPool(new ConnectionPool(MAX_IDLE_CONNECTION, KEEP_ALIVE_DURATION,
									TimeUnit.MINUTES))
							.build();
				}
			}
		}
		return client;
	}

	public static String syncPost(String url, String json) throws IOException {
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		try {
			Response response = OkHttpClient.getInstance().newCall(request).execute();
			if (response.isSuccessful()) {
				String result = response.body().string();
				LOG.info("syncPost response = {}, responseBody= {}", response, result);
				return result;
			}
			String result = response.body().string();
			LOG.info("syncPost response = {}, responseBody= {}", response, result);
			throw new IOException("三方接口返回http状态码为" + response.code());
		} catch (Exception e) {
			LOG.error("syncPost() url:{} have a ecxeption {}", url, e);
			throw new RuntimeException("syncPost() have a ecxeption {}" + e.getMessage());
		}
	}

	public static String syncGet(String url, Map<String, Object> headParamsMap) throws IOException {
		Request request;
		final Request.Builder builder = new Request.Builder().url(url);
		try {
			if (!(headParamsMap == null || headParamsMap.isEmpty())) {
				final Iterator<Map.Entry<String, Object>> iterator = headParamsMap.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					final Map.Entry<String, Object> entry = iterator.next();
					builder.addHeader(entry.getKey(), (String) entry.getValue());
				}
			}
			request = builder.build();
			Response response = OkHttpClient.getInstance().newCall(request).execute();
			String result = response.body().string();
			LOG.info("syncGet response = {},responseBody= {}", response, result);
			if (!response.isSuccessful()) {
				throw new IOException("三方接口返回http状态码为" + response.code());
			}
			return result;
		} catch (Exception e) {
			LOG.error("remote interface url:{} have a ecxeption {}", url, e);
			throw new RuntimeException("三方接口返回异常");
		}
	}

}
