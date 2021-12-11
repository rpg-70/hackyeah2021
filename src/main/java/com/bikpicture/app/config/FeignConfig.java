package com.bikpicture.app.config;

import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class FeignConfig {
    private static final String CERT_PATH = "classpath:data/mentorzyBIK.pfx";
    private static final String CERT_PASS = "P6Lqeukbc2kT";

    @Bean
    public void Config() {
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.keyStore", CERT_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", CERT_PASS);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Feign.Builder feignBuilder() throws Exception {
        return Feign.builder()
                .retryer(Retryer.NEVER_RETRY)
                .client(new Client.Default(getSSLSocketFactory(), null));
    }

    SSLSocketFactory getSSLSocketFactory() throws Exception {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        };
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        InputStream inputStream = resourceLoader.getResource(CERT_PATH).getInputStream();
        char[] allPassword = CERT_PASS.toCharArray();
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(inputStream, allPassword);
        SSLContext sslContext = null;
        sslContext = SSLContextBuilder
                .create()
                .setKeyStoreType("PKCS12")
                .loadKeyMaterial(keystore, allPassword)
                .loadTrustMaterial(acceptingTrustStrategy)
                .build();
        return sslContext.getSocketFactory();
    }
}