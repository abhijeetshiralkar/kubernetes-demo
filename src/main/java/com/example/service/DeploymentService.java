package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapEnvSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvFromSource;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;

@Service
public class DeploymentService {

    private final CoreV1Api kubernetesCoreApi;
    private final AppsV1Api kubernetesAppsApi;

    public DeploymentService(final CoreV1Api kubernetesCoreApi, final AppsV1Api kubernetesAppsApi) {
        this.kubernetesCoreApi = kubernetesCoreApi;
        this.kubernetesAppsApi = kubernetesAppsApi;
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
            kubernetesCoreApi.createNamespacedConfigMap(namespace, configMap, null, null, null, null);
        } catch (final ApiException e) {
            System.out.println("Could not create configmap " + configMapName + " in namespace " + namespace);
            e.printStackTrace();
        }
    }

    public void createDeploymentInNamespace(final String deploymentName, final String nameSpace) {
        System.out.println("Creating deployment " + deploymentName + " in namespace " + nameSpace);

        final V1Deployment body = new V1Deployment();
        body.apiVersion("apps/v1");
        body.setKind("Deployment");

        final V1ObjectMeta metaData = new V1ObjectMeta();
        metaData.setName("systemcontext");
        metaData.setLabels(Collections.singletonMap("app", deploymentName));
        body.setMetadata(metaData);

        final V1DeploymentSpec spec = new V1DeploymentSpec();

        final V1LabelSelector labelSelector = new V1LabelSelector();
        labelSelector.setMatchLabels(Collections.singletonMap("app", deploymentName));
        spec.setSelector(labelSelector);
        spec.setReplicas(2);

        final V1PodTemplateSpec template = new V1PodTemplateSpec();

        final V1ObjectMeta templateMetaData = new V1ObjectMeta();
        final Map<String, String> labels = new HashMap<>();
        labels.put("app", deploymentName);
        labels.put("version", "k8s");
        templateMetaData.setLabels(labels);

        template.setMetadata(templateMetaData);

        final V1PodSpec podSpec = new V1PodSpec();
        final V1Container container = new V1Container();
        container.setName(deploymentName);
        container.setImage("apps-vcp-nexus.asml.com:18444/vcp/systemcontext-rest-api:k8s");
        container.setImagePullPolicy("IfNotPresent");

        final V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
        final Map<String, Quantity> limits = new HashMap<>();
        limits.put("memory", Quantity.fromString("800Mi"));
        limits.put("cpu", Quantity.fromString("1"));
        resourceRequirements.setLimits(limits);

        final Map<String, Quantity> requests = new HashMap<>();
        requests.put("memory", Quantity.fromString("800Mi"));
        requests.put("cpu", Quantity.fromString("0.5"));
        resourceRequirements.setRequests(requests);

        container.setResources(resourceRequirements);
        final V1EnvFromSource configMapRef = new V1EnvFromSource();
        final V1ConfigMapEnvSource configMapEnvSource = new V1ConfigMapEnvSource();
        configMapEnvSource.setName("mongodb-cm");
        configMapRef.setConfigMapRef(configMapEnvSource);
        container.setEnvFrom(Collections.singletonList(configMapRef));

        final List<V1EnvVar> env = new ArrayList<>();
        final V1EnvVar javaOpts = new V1EnvVar();
        javaOpts.setName("JAVA_OPTS");
        javaOpts.setValue(
                "-server -Xms400m -Xmx400m -Xss512k -XX:+AlwaysPreTouch -XX:ReservedCodeCacheSize=64M -XX:MaxMetaspaceSize=100M -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError "
                        +
                        "-XX:+PrintReferenceGC -XX:+PrintAdaptiveSizePolicy -XX:+PrintFlagsFinal -XX:CICompilerCount=2 -Djdk.nio.maxCachedBufferSize=262144 -XX:+UseG1GC -Xloggc:/mnt/kube/sandbox/gc.log "
                        +
                        "-XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=5M  -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -XX:MetaspaceSize=50M "
                        +
                        "-XX:ParallelGCThreads=1 -XX:ConcGCThreads=1");
        env.add(javaOpts);

        container.setEnv(env);

        podSpec.setContainers(Collections.singletonList(container));
        template.setSpec(podSpec);

        // spec.setTemplate();
        body.setSpec(spec);

        // kubernetesAppsApi.createNamespacedDeployment(nameSpace, );

        System.out.println("Created deployment " + deploymentName + " in namespace " + nameSpace);
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
