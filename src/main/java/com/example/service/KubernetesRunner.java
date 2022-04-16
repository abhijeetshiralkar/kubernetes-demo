package com.example.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

@Component
public class KubernetesRunner implements CommandLineRunner {

    private final KubernetesClient kubernetesClient;

    public KubernetesRunner(final KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public void run(final String... args) throws Exception {
        //Get list of pods in a namespace
        //getListOfPods();

        //Create namespace
        createNamespace();
    }

    private void getListOfPods() {
        System.out.println("Getting list of pods from namespace");
        try{
            kubernetesClient.pods().inNamespace("vcp-facilities").list().getItems().forEach(
                    pod -> System.out.println(pod.getMetadata().getName())
            );
        } catch (KubernetesClientException ex) {
            // Handle exception
            ex.printStackTrace();
        }
    }

    private void createNamespace(){
        Namespace ingestionNamespace = kubernetesClient.namespaces().create(new NamespaceBuilder()
                .withNewMetadata()
                .withName("ingestion")
                .endMetadata()
                .build());

        System.out.println("Ingestion namespace status is " + ingestionNamespace.getStatus().toString());
    }
}
