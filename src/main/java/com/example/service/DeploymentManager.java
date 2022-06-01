package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Capabilities;
import io.kubernetes.client.openapi.models.V1ConfigMapEnvSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvFromSource;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1SecurityContext;

@Service
public class DeploymentManager {

    private final AppsV1Api kubernetesAppsApi;

    public DeploymentManager(final AppsV1Api kubernetesAppsApi) {
        this.kubernetesAppsApi = kubernetesAppsApi;
    }

    public void createDeploymentInNamespace(final String deploymentName, final String nameSpace) {
        System.out.println("Creating deployment " + deploymentName + " in namespace " + nameSpace);

        try {
            final V1Deployment body = new V1Deployment();
            body.apiVersion("apps/v1");
            body.setKind("Deployment");
            body.setMetadata(getDeploymentMetaData(deploymentName));
            body.setSpec(getDeploymentSpec(deploymentName));
            kubernetesAppsApi.createNamespacedDeployment(nameSpace, body, null, null, null, null);
        } catch (final ApiException e) {
            System.out.println("Could not create deployment " + deploymentName + " in namespace " + nameSpace);
            e.printStackTrace();
        }

        System.out.println("Created deployment " + deploymentName + " in namespace " + nameSpace);
    }

    private V1DeploymentSpec getDeploymentSpec(final String deploymentName) {
        final V1DeploymentSpec spec = new V1DeploymentSpec();
        spec.setSelector(getSpecLabelSelector(deploymentName));
        spec.setReplicas(2);
        spec.setTemplate(getDeploymentTemplate(deploymentName));
        return spec;
    }

    private V1PodTemplateSpec getDeploymentTemplate(final String deploymentName) {
        final V1PodTemplateSpec template = new V1PodTemplateSpec();

        template.setMetadata(getTemplateMetaData(deploymentName));

        final V1PodSpec podSpec = new V1PodSpec();
        podSpec.setContainers(Collections.singletonList(getContainer(deploymentName)));
        podSpec.setNodeSelector(Collections.singletonMap("asml.com/vcp-node-type", "k8s_compute"));
        podSpec.setTerminationGracePeriodSeconds(120L);
        template.setSpec(podSpec);
        return template;
    }

    private V1ObjectMeta getTemplateMetaData(final String deploymentName) {
        final V1ObjectMeta templateMetaData = new V1ObjectMeta();
        final Map<String, String> labels = new HashMap<>();
        labels.put("app", deploymentName);
        labels.put("version", "k8s");
        templateMetaData.setLabels(labels);

        return templateMetaData;
    }

    private V1Container getContainer(final String deploymentName) {
        final V1Container container = new V1Container();
        container.setName(deploymentName);
        container.setImage("apps-vcp-nexus.asml.com:18444/vcp/systemcontext-rest-api:k8s");
        container.setImagePullPolicy("IfNotPresent");
        container.setResources(getContainerResources());
        container.setEnvFrom(Collections.singletonList(getConfigMapRef()));
        container.setEnv(getEnvironmentVariables());
        container.setLivenessProbe(getLivenessProbe());
        container.setReadinessProbe(getReadinessProbe());
        container.setSecurityContext(getSecurityContext());
        container.setPorts(getPorts());
        return container;
    }

    private List<V1ContainerPort> getPorts() {
        final List<V1ContainerPort> containerPortList = new ArrayList<>();
        final V1ContainerPort httpPort = new V1ContainerPort();
        httpPort.setName("http");
        httpPort.setContainerPort(8080);
        httpPort.setProtocol("TCP");

        final V1ContainerPort httpHealthPort = new V1ContainerPort();
        httpHealthPort.setName("http-health");
        httpHealthPort.setContainerPort(10080);
        httpHealthPort.setProtocol("TCP");
        containerPortList.add(httpPort);
        containerPortList.add(httpHealthPort);
        return containerPortList;
    }

    private V1SecurityContext getSecurityContext() {
        final V1SecurityContext securityContext = new V1SecurityContext();
        securityContext.setAllowPrivilegeEscalation(false);
        final V1Capabilities capabilities = new V1Capabilities();
        capabilities.addDropItem("ALL");
        securityContext.setCapabilities(capabilities);
        securityContext.setRunAsNonRoot(true);
        securityContext.setRunAsUser(1001L);
        return securityContext;
    }

    private V1Probe getReadinessProbe() {
        final V1Probe readinessProbe = new V1Probe();
        final V1HTTPGetAction httpGetAction = new V1HTTPGetAction();
        httpGetAction.setPath("/admin/health");
        httpGetAction.setPort(new IntOrString("http-health"));
        httpGetAction.setScheme("HTTP");
        readinessProbe.setHttpGet(httpGetAction);

        readinessProbe.setInitialDelaySeconds(30);
        readinessProbe.setPeriodSeconds(30);
        readinessProbe.setTimeoutSeconds(20);
        readinessProbe.setSuccessThreshold(1);
        readinessProbe.setFailureThreshold(3);

        return readinessProbe;
    }

    private V1Probe getLivenessProbe() {
        final V1Probe livenessProbe = new V1Probe();
        final V1HTTPGetAction httpGetAction = new V1HTTPGetAction();
        httpGetAction.setPath("/admin/health");
        httpGetAction.setPort(new IntOrString("http-health"));
        httpGetAction.setScheme("HTTP");
        livenessProbe.setHttpGet(httpGetAction);

        livenessProbe.setInitialDelaySeconds(30);
        livenessProbe.setPeriodSeconds(30);
        livenessProbe.setTimeoutSeconds(20);
        livenessProbe.setSuccessThreshold(1);
        livenessProbe.setFailureThreshold(3);
        return livenessProbe;
    }

    private V1EnvFromSource getConfigMapRef() {
        final V1EnvFromSource configMapRef = new V1EnvFromSource();
        final V1ConfigMapEnvSource configMapEnvSource = new V1ConfigMapEnvSource();
        configMapEnvSource.setName("mongodb-cm");
        configMapRef.setConfigMapRef(configMapEnvSource);
        return configMapRef;
    }

    private V1ResourceRequirements getContainerResources() {
        final V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
        final Map<String, Quantity> limits = new HashMap<>();
        limits.put("memory", Quantity.fromString("800Mi"));
        limits.put("cpu", Quantity.fromString("1"));
        resourceRequirements.setLimits(limits);

        final Map<String, Quantity> requests = new HashMap<>();
        requests.put("memory", Quantity.fromString("800Mi"));
        requests.put("cpu", Quantity.fromString("0.5"));
        resourceRequirements.setRequests(requests);

        return resourceRequirements;
    }

    private List<V1EnvVar> getEnvironmentVariables() {
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

        final V1EnvVar instanceId = new V1EnvVar();
        instanceId.setName("INSTANCE_ID");
        instanceId.setValue("prod");
        env.add(instanceId);

        final V1EnvVar baseSleepTimeMs = new V1EnvVar();
        baseSleepTimeMs.setName("BASE_SLEEP_TIME_MS");
        baseSleepTimeMs.setValue("1000");
        env.add(baseSleepTimeMs);

        final V1EnvVar mallocArenaMax = new V1EnvVar();
        mallocArenaMax.setName("MALLOC_ARENA_MAX");
        mallocArenaMax.setValue("1");
        env.add(mallocArenaMax);

        return env;
    }

    private V1LabelSelector getSpecLabelSelector(final String deploymentName) {
        final V1LabelSelector labelSelector = new V1LabelSelector();
        labelSelector.setMatchLabels(Collections.singletonMap("app", deploymentName));
        return labelSelector;
    }

    private V1ObjectMeta getDeploymentMetaData(final String deploymentName) {
        final V1ObjectMeta metaData = new V1ObjectMeta();
        metaData.setName(deploymentName);
        metaData.setLabels(Collections.singletonMap("app", deploymentName));
        return metaData;
    }

}
