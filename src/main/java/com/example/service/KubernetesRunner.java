package com.example.service;

import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
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
        // createNamespace("ingestion");

        // createDeploymentInNamespace("twinscan-scheduler-3205", "ingestion");

        //Get deployments for a namespace
        //getListOfDeployments("vcp-facilities");

        //Check if Deployment is available
        //checkifDeploymentIsRunning("commercialoptionmanager", "vcp-facilities");

        // undeploy deployment
        // undeployDeploymentFromNameSpace("twinscan-scheduler-3205", "ingestion");

    }

    private void undeployDeploymentFromNameSpace(String deploymentName, String nameSpace) {
        System.out.println("Undeploying " + deploymentName + " from namespace " + nameSpace);
        Boolean isDeleted = kubernetesClient.apps().deployments().inNamespace(nameSpace).withName(deploymentName).delete();
        if (isDeleted) {
            System.out.println("Deleted deployment " + deploymentName + " successfully");
        } else {
            System.out.println("Deletion of deployment failed " + deploymentName + " successfully");
        }
    }

    private void createDeploymentInNamespace(String deploymentName, String nameSpace){
        System.out.println("Creating deployment " + deploymentName + " in namespace "+ nameSpace);

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata().withName(deploymentName).endMetadata()
                .withNewSpec()
                .withReplicas(2)
                .withNewSelector()
                .withMatchLabels(Collections.singletonMap("app", deploymentName))
                .endSelector()
                .withNewTemplate()
                .withNewMetadata().addToLabels("app", deploymentName).endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName("nginx")
                .withImage("nginx:1.7.9")
                .addNewPort().withContainerPort(80).endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        kubernetesClient.apps().deployments().inNamespace(nameSpace).createOrReplace(deployment);
        System.out.println("Created deployment " + deploymentName + " in namespace "+ nameSpace);
   }

    private void checkifDeploymentIsRunning(String deploymentName, String namespace){
        System.out.println("Checking status for deployment " + deploymentName + " namespace "+ namespace);
        Deployment deployment = kubernetesClient.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
        boolean active = deployment.getStatus().getReadyReplicas() > 1 ? true : false;
        System.out.println("Deployment " + deploymentName + "is active " + active);
        System.out.println("Deployment status" + deployment.getStatus());
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
        Namespace ingestionNamespace = kubernetesClient.namespaces().createOrReplace(new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build());

        System.out.println("Ingestion namespace status is " + ingestionNamespace.getStatus().toString());
    }
}
