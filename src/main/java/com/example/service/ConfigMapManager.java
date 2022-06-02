package com.example.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

@Service
public class ConfigMapManager {

    private final CoreV1Api kubernetesCoreApi;

    public ConfigMapManager(final CoreV1Api kubernetesCoreApi) {
        this.kubernetesCoreApi = kubernetesCoreApi;
    }

    public void createconfigMap(final String namespace, final String configMapName) {
        try {
            final V1ConfigMap configMap = new V1ConfigMap();
            final Map<String, String> configMapData = new HashMap<>();
            configMapData.put("MONGO_URI", "mongodb://vcp:vcppassword@psmdb-db-rs0.mongodb.svc.cluster.local/admin?replicaSet=rs0&ssl=false");
            configMapData.put("MONGO_WRITE_CONCERN", "majority");
            configMap.setData(configMapData);
            final V1ObjectMeta configMapMetaData = new V1ObjectMeta();
            configMapMetaData.setName(configMapName);
            configMap.setMetadata(configMapMetaData);
            final V1ConfigMapList configMapList = kubernetesCoreApi.listNamespacedConfigMap(namespace, null, null, null, "metadata.name="
                    + configMapName,
                    null, null, null, null, null,
                    null);
            if (configMapList.getItems().size() > 0) {
                System.out.println("Config map already exist with name=" + configMapName + " in namespace " + namespace + " replacing it");
                kubernetesCoreApi.replaceNamespacedConfigMap(configMapName, namespace, configMap, null, null, null, null);
                System.out.println("Replaced Config map with name=" + configMapName + " in namespace " + namespace);
            } else {
                kubernetesCoreApi.createNamespacedConfigMap(namespace, configMap, null, null, null, null);
            }
        } catch (final ApiException e) {
            System.out.println("Could not create configmap " + configMapName + " in namespace " + namespace);
            e.printStackTrace();
        }
    }

}
