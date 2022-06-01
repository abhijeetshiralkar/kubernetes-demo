package com.example.service;

import org.springframework.stereotype.Service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

@Service
public class NamespaceManager {

    private final CoreV1Api kubernetesCoreApi;

    public NamespaceManager(final CoreV1Api kubernetesCoreApi) {
        this.kubernetesCoreApi = kubernetesCoreApi;
    }

    public void createNamespace(final String namespace) {
        System.out.println("Creating namespace " + namespace);
        try {
            final V1NamespaceList namespaceList = kubernetesCoreApi.listNamespace(null, null, null, null, null, null, null, null, null, null);
            // Check if namespace already exist before creating one
            if (namespaceList.getItems().stream().filter(v1Namespace -> v1Namespace.getMetadata().getName().equals(namespace)).count() > 0) {
                System.out.println("Namespace " + namespace + " already exists");
            } else {
                // Create namespace
                final V1Namespace ingestionNamespace = new V1Namespace();
                final V1ObjectMeta namespaceMetaData = new V1ObjectMeta();
                namespaceMetaData.setName(namespace);
                ingestionNamespace.setMetadata(namespaceMetaData);
                kubernetesCoreApi.createNamespace(ingestionNamespace, null, null, null, null);
                System.out.println("Namespace created successfully");
            }
        } catch (final ApiException e) {
            System.out.println("Could not create namespace " + namespace);
            e.printStackTrace();
        }
    }

}
