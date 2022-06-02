package com.example.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KubernetesRunner implements CommandLineRunner {

    private final DeploymentManager deploymentManager;
    private final NamespaceManager namespaceManager;
    private final ConfigMapManager configMapManager;
    private final KubernetesServiceManager kubernetesServiceManager;
    private final IstioServiceManager istioServiceManager;

    public KubernetesRunner(final DeploymentManager deploymentManager, final NamespaceManager namespaceManager,
            final ConfigMapManager configMapManager, final KubernetesServiceManager kubernetesServiceManager,
            final IstioServiceManager istioServiceManager) {
        this.deploymentManager = deploymentManager;
        this.namespaceManager = namespaceManager;
        this.configMapManager = configMapManager;
        this.kubernetesServiceManager = kubernetesServiceManager;
        this.istioServiceManager = istioServiceManager;
    }


    @Override
    public void run(final String... args) throws Exception {
        // Create namespace if it doesnot exist
        namespaceManager.createNamespace("ingestion");
        // Create configmap if it doesnot exist. If it exists replace it
        configMapManager.createconfigMap("ingestion", "mongodb-cm");
        // Create deployment
        deploymentManager.createDeploymentInNamespace("systemcontext", "ingestion");
        // create service
        kubernetesServiceManager.createKubernetesService("systemcontext", "ingestion");
        // Create or replace virtual service
        istioServiceManager.createVirtualService("systemcontext", "ingestion");
        // Create or replace virtual service
        istioServiceManager.createIngressService("systemcontext", "ingestion");
    }
}
