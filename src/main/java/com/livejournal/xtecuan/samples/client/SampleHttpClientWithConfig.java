/*
 * Copyright 2018 xtecuan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.livejournal.xtecuan.samples.client;

import com.livejournal.xtecuan.samples.config.facade.utils.ConfigurationFacade;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author xtecuan
 */
public class SampleHttpClientWithConfig {

    private static final Logger LOGGER = Logger.getLogger(SampleHttpClientWithConfig.class);
    private static final ConfigurationFacade CONFIG = new ConfigurationFacade();
    
    private HttpHost getHttpProxy() {
        return new HttpHost(CONFIG.getValue("http.proxy.host"), CONFIG.getInteger("http.proxy.port"));
    }

    private Boolean isProxyEnabled() {
        return Boolean.valueOf(CONFIG.getValue("http.proxy.enabled"));
    }

    private String getHttpProxyUsername() {
        return CONFIG.getValue("http.proxy.user");
    }

    private String getHttpProxyPassword() {
        return CONFIG.getValue("http.proxy.pass");
    }

    private Boolean isUserAndPasswordSet() {
        Boolean result = Boolean.FALSE;
        String user = getHttpProxyUsername();
        String pass = getHttpProxyPassword();
        if ((user != null && !user.equals("")) && (pass != null && !pass.equals(""))) {
            result = Boolean.TRUE;
        }

        return result;
    }

    private Header getXDefaultHeader() {
        return new BasicHeader("X-Default-Header", "Default Header: " + getClass().getCanonicalName());
    }

    private Header getUserAgent() {
        return new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
    }

    private Header getAcceptHeader(String accept) {
        return new BasicHeader("accept", accept);
    }

    public String getJsonApplication() {
        return "application/json";
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public String executeGet(String requestURL) {
        String resultResponse = null;
        List<Header> defaultHeaders = Arrays.asList(getXDefaultHeader(), getUserAgent(), getAcceptHeader(getJsonApplication()));
        CloseableHttpClient httpclient = null;
        if (isProxyEnabled() && isUserAndPasswordSet()) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(getHttpProxy()), new UsernamePasswordCredentials(getHttpProxyUsername(), getHttpProxyPassword()));
            httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .setDefaultHeaders(defaultHeaders)
                    .build();
        } else {
            httpclient = HttpClients.custom()
                    .setDefaultHeaders(defaultHeaders)
                    .build();
        }

        try {
            HttpGet get = new HttpGet(requestURL);
            if (isProxyEnabled()) {
                RequestConfig config = RequestConfig.custom()
                        .setProxy(getHttpProxy())
                        .build();
                get.setConfig(config);
            }
            getLogger().info("Executing request " + get.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(get);
            try {
                getLogger().info("=================================================================");
                getLogger().info(response.getStatusLine());
                resultResponse = EntityUtils.toString(response.getEntity());
                //getLogger().info(resultResponse);

                getLogger().info("=================================================================");
            } finally {
                response.close();
            }

        } catch (Exception e) {
            getLogger().error("Error executing GET: " + requestURL, e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                getLogger().error("Error closing client...", ex);
            }
        }

        return resultResponse;
    }

    public static void main(String[] args) {

        SampleHttpClientWithConfig myClient = new SampleHttpClientWithConfig();
        String sampleURL = "http://ip.jsontest.com/";

        getLogger().info("Result: " + myClient.executeGet(sampleURL));

    }
}
