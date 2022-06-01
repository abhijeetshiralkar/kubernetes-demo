package com.example.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KubernetesRunner implements CommandLineRunner {

    private final DeploymentManager deploymentManager;
    private final NamespaceManager namespaceManager;
    private final ConfigMapManager configMapManager;
    private final KubernetesServiceManager kubernetesServiceManager;

    public KubernetesRunner(final DeploymentManager deploymentManager, final NamespaceManager namespaceManager,
            final ConfigMapManager configMapManager, final KubernetesServiceManager kubernetesServiceManager) {
        this.deploymentManager = deploymentManager;
        this.namespaceManager = namespaceManager;
        this.configMapManager = configMapManager;
        this.kubernetesServiceManager = kubernetesServiceManager;
    }


    @Override
    public void run(final String... args) throws Exception {
        namespaceManager.createNamespace("ingestion");
        configMapManager.createconfigMap("ingestion", "mongodb-cm");
        deploymentManager.createDeploymentInNamespace("systemcontext", "ingestion");
        kubernetesServiceManager.createKubernetesService("systemcontext", "ingestion");
    }
}
