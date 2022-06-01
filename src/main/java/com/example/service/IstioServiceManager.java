package com.example.service;

import org.springframework.stereotype.Service;

import io.fabric8.istio.api.networking.v1alpha3.VirtualService;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceSpec;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
public class IstioServiceManager {

    private final IstioClient istioClient;

    public IstioServiceManager(final IstioClient istioClient) {
        this.istioClient = istioClient;
    }

    public void createIngressService(final String serviceName, final String namespace) {
        System.out.println("Creating Ingress service " + serviceName + " in namespace " + namespace);
        final VirtualService virtualService = new VirtualServiceBuilder().withApiVersion("networking.istio.io/v1beta1").withKind("VirtualService")
                .withMetadata(getIngressServiceMetaData(namespace)).withSpec(getIngressServiceSpec(serviceName))
                .build();

        istioClient.v1alpha3().virtualServices().create(virtualService);
        System.out.println("Created service " + serviceName + " in namespace " + namespace);
    }

    private VirtualServiceSpec getIngressServiceSpec(final String serviceName) {
        final VirtualServiceSpec spec = new VirtualServiceSpec();
        return spec;
    }

    private ObjectMeta getIngressServiceMetaData(final String namespace) {
        final ObjectMeta metaData = new ObjectMeta();
        metaData.setName("vcp-ingress-systemcontext");
        metaData.setNamespace(namespace);
        return metaData;
    }

}
