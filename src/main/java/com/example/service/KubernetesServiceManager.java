package com.example.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.gson.JsonSyntaxException;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
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
            if (checkIfServiceExistsInNamespace(serviceName, namespace)) {
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

    public void deleteKubernetesService(final String serviceName, final String namespace) {
        if (checkIfServiceExistsInNamespace(serviceName, namespace)) {
            try {
                System.out.println("Deleting service " + serviceName + "from namespace " + namespace);
                kubernetesCoreApi.deleteNamespacedService(serviceName, namespace, null, null, null, null, null, new V1DeleteOptions());
                System.out.println("Deleted service " + serviceName + "from namespace " + namespace);
            } catch (final ApiException e) {
                System.out.println("Exception ocurred while deleting " + serviceName + "from namespace " + namespace);
                e.printStackTrace();
            } catch (final JsonSyntaxException e) {
                if (checkIfServiceExistsInNamespace(serviceName, namespace)) {
                    // There is an exception message while deleting service "com.google.gson.JsonSyntaxException: java.lang.IllegalStateException:
                    // Expected BEGIN_OBJECT but was STRING at line 1 column 60 path $.status" which occurs during parsing the response internally by
                    // the kubernetes client library
                    // But in reality the service is deleted successfully. So verify if the service exists.
                    System.out.println("Exception ocurred while deleting " + serviceName + "from namespace " + namespace);
                    e.printStackTrace();
                } else {
                    System.out.println("Deleted service " + serviceName + " from namespace " + namespace);
                }
            }
        } else {
            System.out.println("Did not delete service " + serviceName + " from namespace " + namespace + " as it does not exist");
        }
    }

    private boolean checkIfServiceExistsInNamespace(final String serviceName, final String namespace) {
        final V1ServiceList serviceList;
        try {
            serviceList = kubernetesCoreApi.listNamespacedService(namespace, null, null, null, "metadata.name=" + serviceName,
                    null, null, null, null, null,
                    null);
            if (serviceList.getItems().size() > 0) {
                return true;
            }
        } catch (final ApiException ex) {
            System.out.println("Could not validate if service " + serviceName + " exists in namepsace " + namespace);
            ex.printStackTrace();
        }
        return false;
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
