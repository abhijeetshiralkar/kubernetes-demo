package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
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
        //getListOfPods("vcp-facilities");

        //Create namespace
        //createNamespace("ingestion");

        //Get deployments for a namespace
        getListOfDeployments("vcp-facilities");

        //Check if Deployment is available
        checkifDeploymentIsRunning("commercialoptionmanager", "vcp-facilities");

    }

    private void checkifDeploymentIsRunning(String deploymentName, String namespace){
        System.out.println("Checking status for deployment " + deploymentName + " namespace "+ namespace);
        DeploymentList deployments = kubernetesClient.apps().deployments().inNamespace(namespace).list();
        Deployment matchingDeployment = deployments.getItems().stream().filter(deployment -> deployment.getMetadata().getName().equals(deploymentName)).findFirst().orElse(null);
        boolean active = matchingDeployment.getStatus().getReadyReplicas() > 1 ? true : false;
        System.out.println("Deployment " + deploymentName + "is active " + active);
        System.out.println("Deployment status" + matchingDeployment.getStatus());
    }

    private void getListOfDeployments(String namespace) {
        System.out.println("Getting list of deployments from namespace " + namespace);
            DeploymentList deployments = kubernetesClient.apps().deployments().inNamespace(namespace).list();
           deployments.getItems().forEach(deployment -> {
               System.out.println(deployment.getMetadata().getName());
           });
    }


    private void getListOfPods(String namespace) {
        System.out.println("Getting list of pods from namespace " + namespace);
        try{
            kubernetesClient.pods().inNamespace(namespace).list().getItems().forEach(
                    pod -> System.out.println(pod.getMetadata().getName())
            );
        } catch (KubernetesClientException ex) {
            // Handle exception
            ex.printStackTrace();
        }
    }

    private void createNamespace(String namespace){
        Namespace ingestionNamespace = kubernetesClient.namespaces().create(new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build());

        System.out.println("Ingestion namespace status is " + ingestionNamespace.getStatus().toString());
    }
}
