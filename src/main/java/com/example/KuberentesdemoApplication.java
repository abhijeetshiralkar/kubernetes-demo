package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Refer https://github.com/fabric8io/kubernetes-client for more details about kubernetes client
 */
@SpringBootApplication
public class KuberentesdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuberentesdemoApplication.class, args);
    }

    @Bean
    KubernetesClient kubernetesClient(){
        return new DefaultKubernetesClient();
    }

}
