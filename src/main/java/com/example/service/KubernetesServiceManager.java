package com.example.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;

@Service
public class KubernetesServiceManager {

    private final CoreV1Api kubernetesCoreApi;

    public KubernetesServiceManager(final CoreV1Api kubernetesCoreApi) {
        this.kubernetesCoreApi = kubernetesCoreApi;
    }

    public void createKubernetesService(final String serviceName, final String namespace) {
        System.out.println("Creating service " + serviceName + " in namespace " + namespace);
        try {
            final V1Service serviceBody = new V1Service();
            serviceBody.setApiVersion("v1");
            serviceBody.setKind("Service");
            serviceBody.setMetadata(getServiceMetaData(serviceName, namespace));
            serviceBody.setSpec(getServiceSpec(serviceName));
            final V1ServiceList serviceList = kubernetesCoreApi.listNamespacedService(namespace, null, null, null, "metadata.name=systemcontext",
                    null, null, null, null, null,
                    null);
            if (serviceList.getItems().size() > 0) {
                System.out.println("Service " + serviceName + " already exists in namespace " + namespace + " replacing it");
            } else {
                kubernetesCoreApi.createNamespacedService(namespace, serviceBody, null, null, null, null);
                System.out.println("Created service " + serviceName + " in namespace " + namespace);
            }
        } catch (final ApiException e) {
            System.out.println("Could not create service " + serviceName + " in namespace " + namespace);
            e.printStackTrace();
        }
    }

    public void deleteKubernetesService(final String systemcontext, final String ingestion) {
    }

    private V1ServiceSpec getServiceSpec(final String serviceName) {
        final V1ServiceSpec serviceSpec = new V1ServiceSpec();
        serviceSpec.setSelector(Collections.singletonMap("app", serviceName));
        final V1ServicePort servicePort = new V1ServicePort();
        servicePort.setName("http");
        servicePort.setPort(8080);
        serviceSpec.setPorts(Collections.singletonList(servicePort));
        return serviceSpec;
    }

    private V1ObjectMeta getServiceMetaData(final String serviceName, final String namespace) {
        final V1ObjectMeta serviceMetaData = new V1ObjectMeta();
        serviceMetaData.setName(serviceName);
        serviceMetaData.setNamespace(namespace);
        final Map<String, String> labels = new HashMap<>();
        labels.put("app", serviceName);
        labels.put("component", serviceName);
        serviceMetaData.setLabels(labels);

        return serviceMetaData;
    }
}
