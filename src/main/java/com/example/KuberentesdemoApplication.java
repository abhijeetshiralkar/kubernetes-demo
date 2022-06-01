package com.example;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

/**
 * Refer https://github.com/fabric8io/kubernetes-client for more details about kubernetes client
 * Refer https://itnext.io/difference-between-fabric8-and-official-kubernetes-java-client-3e0a994fd4af for difference between kubernetes official
 * client vs one from fabric 8
 */
@SpringBootApplication
public class KuberentesdemoApplication {

    private ApiClient kubernetesApiClient;

    public static void main(String[] args) {
        SpringApplication.run(KuberentesdemoApplication.class, args);
    }

    @Bean
    ApiClient kubernetesApiClient() throws IOException {
        return Config.defaultClient();
    }

    @Bean
    CoreV1Api kubernetesApi() throws IOException {
        return new CoreV1Api(kubernetesApiClient);
    }

    @Bean
    AppsV1Api kubernetesAppsApi() {
        return new AppsV1Api(kubernetesApiClient);
    }

}
