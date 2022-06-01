package com.example.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KubernetesRunner implements CommandLineRunner {

    private final DeploymentService deploymentService;

    public KubernetesRunner(final DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }


    @Override
    public void run(final String... args) throws Exception {
        deploymentService.createNamespace("ingestion");
        deploymentService.createconfigMap("ingestion", "mongodb-cm");
        deploymentService.createDeploymentInNamespace("systemcontext", "ingestion");
    }
}
