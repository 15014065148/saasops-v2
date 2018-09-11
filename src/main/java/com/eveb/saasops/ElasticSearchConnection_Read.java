package com.eveb.saasops;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ElasticSearchConnection_Read {

    @Value("${read_elasticsearch.url}")
    private String url;
    @Value("${read_elasticsearch.port}")
    private int clientport;
    @Value("${read_elasticsearch.rest.port}")
    private int restport;
    @Value("${read_elasticsearch.name}")
    private String name;
    @Value("${read_elasticsearch.password}")
    private String password;
    @Value("${read_elasticsearch.timeout}")
    private int timeout;

    public TransportClient client;

    public RestClient restClient_Read;

    public ElasticSearchConnection_Read() {

    }

    @PostConstruct
    private void init() throws Exception {
        // 配置信息
        Settings esSetting = Settings.builder()
                .put("cluster.name", "saasops-dbcenter")//设置ES实例的名称
                .put("client.transport.sniff", true)//自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .build();
        client = new PreBuiltTransportClient(esSetting);//初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(name, password));
        RestClientBuilder builder = RestClient.builder(new HttpHost(url, restport, "http"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        requestConfigBuilder.setConnectTimeout(timeout);
                        requestConfigBuilder.setSocketTimeout(timeout);
                        requestConfigBuilder.setConnectionRequestTimeout(timeout);
                        return requestConfigBuilder;
                    }
                })/***超时时间设为2分钟**/
                .setMaxRetryTimeoutMillis(timeout);
        restClient_Read = builder.build();
    }

}
