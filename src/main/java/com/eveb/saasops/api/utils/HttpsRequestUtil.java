package com.eveb.saasops.api.utils;


import javax.servlet.http.HttpServletRequest;
import com.eveb.saasops.modules.log.entity.LogMbrRegister.RegIpValue;


public class HttpsRequestUtil {
	//将请求头中的dev=PC/H5转换为byte
	public static Byte getHeaderOfDev(String dev) {
		Byte Source=null;
		if (dev != null && !dev.equals("")) {
			switch (dev.toUpperCase()) {
			case "PC": {
				Source = RegIpValue.pcClient;
				break;
			}
			case "H5": {
				Source = RegIpValue.H5Client;
				break;
			}
			default: {
				Source = RegIpValue.pcClient;
				break;
			}
			}
		}
		return Source;
	}
	//获取前端传来的域名
	public static String getDomainURL(HttpServletRequest request){
		return request.getHeader("domainURL");
	}
	//@Autowired
	//private OkHttpProxyUtils okHttpProxyUtils;
	/*public static String send(String url, PtEntity ptEntity) {
		String inputLine = new String();
		try {
			URL pt = new URL(url);
			 //, 10.111.135.58 1090 两个代理
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ApiConstants.ProxyAttribute.ip, ApiConstants.ProxyAttribute.port));
			URLConnection yc = pt.openConnection(proxy);

			//URLConnection yc = pt.openConnection();
			yc.setConnectTimeout(HttpTime.connectTimeout);
			yc.setReadTimeout(HttpTime.requestTimeout);
			KeyStore ks = KeyStore.getInstance(PtConstants.SslEntity.KeyStore);
			InputStream stream = HttpsRequestUtil.class.getResourceAsStream(PtConstants.SslEntity.keyFilePath);
			ks.load(stream, PtConstants.SslEntity.keyPwd.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(PtConstants.SslEntity.KeyManager);
			kmf.init(ks, PtConstants.SslEntity.keyPwd.toCharArray());
			SSLContext sc = SSLContext.getInstance(PtConstants.SslEntity.tls);
			sc.init(kmf.getKeyManagers(), null, null);
			yc.setRequestProperty(ptEntity.getEntityKey(), ptEntity.getEntityContext());
			((HttpsURLConnection) yc).setSSLSocketFactory(sc.getSocketFactory());
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String str;
			while ((str = in.readLine()) != null)
				inputLine += str;
			in.close();
		} catch (Exception e) {
			throw new RRException(e.getMessage()+"PT接口连接异常,请重试!");
		}
		return inputLine;
	}*/

	/***
	 * 初始化PT连接
	 */
      /*private void initPtClient() {
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			InputStream stream = this.getClass().getResourceAsStream("/key/VBETCNYTLE.p12");
			// File file = new File("VBETCNYTLE.p12");
			// FileUtils.copyInputStreamToFile(stream,file);
			// FileInputStream fis = new FileInputStream(file);
			ks.load(stream, "iQ3xuZrS".toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, "iQ3xuZrS".toCharArray());
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			OkHttpClient.Builder ptbuilder = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			}).sslSocketFactory(sc.getSocketFactory());
			ptbuilder.addInterceptor(new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request request = chain.request().newBuilder().addHeader("X_ENTITY_KEY", "").build();
					return chain.proceed(request);
				}
			});
			ptbuilder.connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES));
			ptbuilder.readTimeout(60, TimeUnit.SECONDS);
			ptbuilder.connectTimeout(20, TimeUnit.SECONDS).build();
		} catch (Exception e) {
			throw new RRException("PT接口连接异常,请重试!");
			// ptclient = client;
			// log.error(e.getMessage());
		}
	}*/
	/*
	 * public static void main(String[] args) throws Exception {
	 * System.out.println("testaa");
	 * 
	 * String PTUrl = "https://kioskpublicapi.redhorse88.com/player/create"; String
	 * loginname ="AGAAZZ010"; String password ="111111"; String kioskname
	 * ="VBETCNYAG"; String adminname = "VBETCNYAG"; String requestUrl = PTUrl +
	 * "/playername/"+ loginname + "/kioskname/" + kioskname + "/adminname/" +
	 * adminname + "/password/" + password;
	 * 
	 * URL pt = new URL(requestUrl); URLConnection yc = pt.openConnection();
	 * 
	 * KeyStore ks = KeyStore.getInstance("PKCS12"); File file = new
	 * File("D:/ptjava/cer/vbet.1114721.p12"); FileInputStream fis = new
	 * FileInputStream(file); ks.load(fis, "TGX8BU20".toCharArray());
	 * KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	 * kmf.init(ks, "TGX8BU20".toCharArray()); SSLContext sc =
	 * SSLContext.getInstance("TLS"); sc.init(kmf.getKeyManagers(), null, null);
	 * 
	 * 
	 * yc.setRequestProperty("X_ENTITY_KEY","input your entity key");
	 * //yc.setSSLSocketFactory(sc.getSocketFactory()); ((HttpsURLConnection)
	 * yc).setSSLSocketFactory(sc.getSocketFactory()); BufferedReader in = new
	 * BufferedReader(new InputStreamReader( yc.getInputStream())); String
	 * inputLine; while ((inputLine = in.readLine()) != null)
	 * System.out.println(inputLine); in.close(); }
	 */
/**
 * https post 请求
 * @param url
 * @param param
 * @param headParams
 * @return
 */
	/*public static String httpsPost(String url, String param, Map<String, String> headParams) {
		// 构建请求
		HttpsURLConnection con=null;
		try {
			URL postUrl = new URL(null, url, new sun.net.www.protocol.https.Handler());
			// URL postUrl = new URL(url);
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ApiConstants.ProxyAttribute.ip, ApiConstants.ProxyAttribute.port));
			con = (HttpsURLConnection) postUrl.openConnection(proxy);
			con.setRequestMethod(PostCat.post);// post方式提交
			con.setDoOutput(true);// 打开读写属性，默认均为false
			con.setDoInput(true);
			con.setUseCaches(false);// Post请求不能使用缓存
			con.setInstanceFollowRedirects(true);
			con.setConnectTimeout(HttpTime.connectTimeout);
			con.setReadTimeout(HttpTime.requestTimeout);
			// 添加头信息
			con.setRequestProperty(HttpDefSet.contentType, HttpDefSet.contentTypeValJson);
			if (headParams != null && headParams.size() > 0) {
				for (String key : headParams.keySet()) {
					con.setRequestProperty(key, headParams.get(key));
				}
			}
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			// 发送请求
			if (!StringUtils.isEmpty(param))
				out.writeBytes(param);
			out.flush();
			out.close();
			//con.connect();
			//当PT2的创建新账号时候返回的CODE为 
			if (con.getResponseCode() == HttpStatus.SC_OK||con.getResponseCode()==HttpStatus.SC_CREATED) {
				// 接收数据
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), HttpDefSet.charSet));
				String line;
				StringBuffer responseText = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					responseText.append(line).append("\r\n");
				}
				reader.close();
				con.disconnect();
				return responseText.toString();
			} else {
				throw new RRException(con.getResponseCode()+"通信失败!");
			}
			
		} catch (IOException e) {
			throw new RRException(e.getMessage()+"服务异常,请稍后再试!");
		}  finally {
			if (con != null)
				con.disconnect();
		}// 打开连接
	}

	public static String httpsPost(String url, String param) {
		return httpsPost(url, param, new HashMap<String, String>());
	}

	public static String httpsPost(String url, String param, String token) {
		Map<String, String> headParams = new HashMap<String, String>();
		headParams.put(PtNewConstants.ACCESS_TOKE, token);
		return httpsPost(url, param, headParams);
	}

	public static String mgHttpsPost(String url, String param) {
		return httpsPost(url, param, MgConstants.getHead());
	}

	public static String mghttpsPut(String url, String param, String token) {
		// return httpsPost(url, param, mgWithTokenHead(token));
		//注意MG 只接受PUT
		HttpEntityEnclosingRequestBase request = new HttpPut(url);
		return httpPost(request, param, mgWithTokenHead(token));
	}

	public static String mghttpsPutXml(String url, String param) {
		HttpEntityEnclosingRequestBase request = new HttpPut(url);
		return httpPost(request, param, MgConstants.getHeadXml());
	}

	/*public  String mgGet(String url) {
		return httpDoGet(url, MgConstants.getHead());
	}*/

/*	private static String test(String url, Map<String, String> headParams){
		return OkHttpUtils.get(url,headParams,OkHttpUtils.IBCProxySelector());
	}*/

	/**  http get 常规请求
	 * 发送get请求
	 *
	 * @param url
	 *            路径
	 * @return
	 */
	/*public  String httpDoGet(String url, Map<String, String> headParams) {
		// get请求返回结果
		String strResult = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			// 发送get请求
			HttpGet request = new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(HttpTime.connectTimeout)
					.setConnectionRequestTimeout(HttpTime.requestTimeout).setSocketTimeout(HttpTime.socketTimeout)
					.setProxy(new HttpHost("myotherproxy", 8080)).build();
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(HttpTime.connectTimeout)
					.setConnectionRequestTimeout(HttpTime.requestTimeout).setSocketTimeout(HttpTime.socketTimeout).build();
			request.setConfig(requestConfig);
			// 添加头信息
			if (headParams != null && headParams.size() > 0) {
				for (String key : headParams.keySet()) {
					request.setHeader(key, headParams.get(key));
				}
			}
			InetSocketAddress socksaddr = new InetSocketAddress(ApiConstants.ProxyAttribute.ip, ApiConstants.ProxyAttribute.port);
			HttpClientContext context = HttpClientContext.create();
			context.setAttribute("socks.address", socksaddr);
			response = httpclient.execute(request,context);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK||response.getStatusLine().getStatusCode()==HttpStatus.SC_CREATED) {
				strResult = EntityUtils.toString(response.getEntity());
				// url = URLDecoder.decode(url, "UTF-8");
			} else {
				throw new RRException(response.getStatusLine().getStatusCode()+"通信失败!");
			}
		} catch (IOException e) {
			throw new RRException(e.getMessage()+"get请求提交失败!");
		} finally {
			try {
				if (response != null)
					response.close();
				if (httpclient != null)
					httpclient.close();
			} catch (IOException e) {
			}
		}
		return strResult;


		return okHttpProxyUtils.getHeader(okHttpProxyUtils.proxyClient,url,headParams);
	}

	public  String httpGet(String url) {
		return httpDoGet(url, null);
	}

	public  String httpGet(String url, String token) {
		Map<String, String> headParams = new HashMap<String, String>();
		if (!StringUtils.isEmpty(token)) {
			headParams.put(PtNewConstants.ACCESS_TOKE, token);
		}
		return httpDoGet(url, headParams);
	}*/
	/**
	 * https  发送XML 流文件请求
	 * @param urlStr
	 * @param context
	 * @param headParams
	 * @return
	 */
/*	public static String httpSoapPost(String urlStr, String context, Map<String, String> headParams) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlStr);
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ApiConstants.ProxyAttribute.ip, ApiConstants.ProxyAttribute.port));
			connection = (HttpURLConnection) url.openConnection(proxy);
			//connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(PostCat.post);
			connection.setConnectTimeout(HttpTime.connectTimeout);
			connection.setReadTimeout(HttpTime.requestTimeout);
			connection.setDoInput(true);
			connection.setRequestProperty(HttpDefSet.contentType, HttpDefSet.contentTypeVal);*/
			//connection.setRequestProperty("accept","*/*");
			/*if (headParams != null && headParams.size() > 0) {
				for (String key : headParams.keySet()) {
					connection.setRequestProperty(key, headParams.get(key));
				}
			}
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), HttpDefSet.charSet);
			out.write(context.toString()); // 直接post的进行调用！
			// 解析返回的XML字串
			out.flush();
			out.close();
			//connection.connect();
			//有使用 getResponseCode 就不需要显示使用 connection.connect()
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
					|| connection.getResponseCode() == HttpURLConnection.HTTP_CREATED||connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
				//NT无此订单会报500
				InputStream urlStream =(connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR)?connection.getErrorStream():connection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlStream));
				String ss = null;
				StringBuffer total = new StringBuffer();
				while ((ss = bufferedReader.readLine()) != null) {
					total.append(ss);
				}
				bufferedReader.close();
				return total.toString();
			}
			else {
				//System.out.println(connection.getResponseCode());
				throw new RRException(connection.getResponseCode()+"通信失败!");
			}
		} catch (Exception e) {
			throw new RRException(e.getMessage()+"提交请求错误,请稍后再试！");
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	public static String httpsNtPost(String urlStr) {
		Map<String, String> headParams = new HashMap<String, String>();
		headParams.put(NtConstants.USER_AGENT, NtConstants.USER_AGENT_VAL);
		return httpsPost(urlStr, null, headParams);
	}
	
	public static String httpNtSoapPost(String urlStr, String context) {
		Map<String, String> headParams = new HashMap<String, String>();
		headParams.put(NtConstants.USER_AGENT, NtConstants.USER_AGENT_VAL);
		return httpSoapPost(urlStr, context, headParams);
	}
	
	public static String httpSoapPost(String urlStr, String context) {
		return httpSoapPost(urlStr, context, null);
	}

	public static String evPost(String url, Map<String, String> headParams) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		return httpPost(request, null, headParams);
	}

	public static String evebPost(String url) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		return httpPost(request, null, null);
	}

	public static String opusSbPost(String url, String jsonStr) {
		return httpsPost(url, jsonStr);
	}

	public static String opusSbPost(String url) {
		return httpsPost(url, null);
	}

	/*public  String opusLiveGet(String url) {
		return httpDoGet(url, null);
	}

	public  String PbhttpsGet(String url,Map<String,String> headParams) {
		return httpDoGet(url, headParams);
	}
*/
	/**
	 * http 常规请求JSON方式
	 * @param request
	 * @param jsonStr
	 * @param headParams
	 * @return
	 */
	/*public static String httpPost(HttpEntityEnclosingRequestBase request, String jsonStr,
			Map<String, String> headParams){
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;//10.111.135.58 1080, 10.111.135.58 1090 两个代理
		/*RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(HttpTime.connectTimeout)
				.setConnectionRequestTimeout(HttpTime.requestTimeout).setSocketTimeout(HttpTime.socketTimeout).setProxy(new HttpHost("myotherproxy", 8080)).build();*/
		/*RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(HttpTime.connectTimeout)
				.setConnectionRequestTimeout(HttpTime.requestTimeout).setSocketTimeout(HttpTime.socketTimeout).build();
		request.setConfig(requestConfig);
		if (headParams != null && headParams.size() > 0) {
			for (String key : headParams.keySet()) {
				request.addHeader(key, headParams.get(key));
			}
		}
		try {
			if (!StringUtils.isEmpty(jsonStr)) {
				StringEntity params = new StringEntity(jsonStr);
				request.setEntity(params);
			}
			InetSocketAddress socksaddr = new InetSocketAddress(ApiConstants.ProxyAttribute.ip, ApiConstants.ProxyAttribute.port);
			HttpClientContext context = HttpClientContext.create();
			context.setAttribute("socks.address", socksaddr);
			response = httpclient.execute(request,context);
			// System.out.println("得到的结果:" + response.getStatusLine());//得到请求结果
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK||response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				HttpEntity entity = response.getEntity();// 得到请求回来的数据
				return EntityUtils.toString(entity);
			} else {
				throw new RRException(response.getStatusLine().getStatusCode() + "通信失败!");
			}
		} catch (Exception e) {
			throw new RRException(e.getMessage() + "连接异常!");
		} finally {
			try {
				if (response != null)
					response.close();
				if (httpclient != null)
					httpclient.close();
			} catch (IOException e) {
			}
		}
	}
	
	static Map<String, String> mgWithTokenHead(String token) {
		Map<String, String> head = MgConstants.getHead();
		head.put(MgConstants.TOKEN_KEY, token);
		return head;
	}
*/}