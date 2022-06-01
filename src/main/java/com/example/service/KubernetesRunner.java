package com.example.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KubernetesRunner implements CommandLineRunner {

    private final DeploymentManager deploymentManager;
    private final NamespaceManager namespaceManager;
    private final ConfigMapManager configMapManager;

    public KubernetesRunner(final DeploymentManager deploymentManager, final NamespaceManager namespaceManager,
            final ConfigMapManager configMapManager) {
        this.deploymentManager = deploymentManager;
        this.namespaceManager = namespaceManager;
        this.configMapManager = configMapManager;
    }


    @Override
    public void run(final String... args) throws Exception {
        namespaceManager.createNamespace("ingestion");
        configMapManager.createconfigMap("ingestion", "mongodb-cm");
        deploymentManager.createDeploymentInNamespace("systemcontext", "ingestion");
    }
}
