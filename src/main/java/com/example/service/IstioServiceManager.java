package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import io.fabric8.istio.api.networking.v1beta1.Destination;
import io.fabric8.istio.api.networking.v1beta1.HTTPMatchRequest;
import io.fabric8.istio.api.networking.v1beta1.HTTPRetry;
import io.fabric8.istio.api.networking.v1beta1.HTTPRoute;
import io.fabric8.istio.api.networking.v1beta1.HTTPRouteDestination;
import io.fabric8.istio.api.networking.v1beta1.PortSelector;
import io.fabric8.istio.api.networking.v1beta1.StringMatch;
import io.fabric8.istio.api.networking.v1beta1.StringMatchRegex;
import io.fabric8.istio.api.networking.v1beta1.VirtualService;
import io.fabric8.istio.api.networking.v1beta1.VirtualServiceBuilder;
import io.fabric8.istio.api.networking.v1beta1.VirtualServiceSpec;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
public class IstioServiceManager {

    private static final String API_VERSION = "networking.istio.io/v1beta1";
    private static final String SERVICE_KIND = "VirtualService";

    private final IstioClient istioClient;

    public IstioServiceManager(final IstioClient istioClient) {
        this.istioClient = istioClient;
    }

    public void createIngressService(final String serviceName, final String namespace) {
        System.out.println("Creating Ingress service " + serviceName + " in namespace " + namespace);
        final VirtualService virtualService = new VirtualServiceBuilder().withApiVersion(API_VERSION).withKind(SERVICE_KIND)
                .withMetadata(getIngressServiceMetaData(serviceName, namespace)).withSpec(getIngressServiceSpec())
                .build();

        istioClient.v1beta1().virtualServices().inNamespace(namespace).createOrReplace(virtualService);
        System.out.println("Created Ingress service " + serviceName + " in namespace " + namespace);
    }

    public void deleteIngressService(final String serviceName, final String namespace) {
        System.out.println("Deleting Ingress service " + serviceName + " in namespace " + namespace);
        final VirtualService virtualService = new VirtualServiceBuilder().withApiVersion(API_VERSION).withKind(SERVICE_KIND)
                .withMetadata(getIngressServiceMetaData(serviceName, namespace))
                .build();

        istioClient.v1beta1().virtualServices().delete(virtualService);
        System.out.println("Deleted Ingress service " + serviceName + " in namespace " + namespace);
    }

    public void createVirtualService(final String serviceName, final String namespace) {
        System.out.println("Creating VirtualService " + serviceName + " in namespace " + namespace);
        final VirtualService virtualService = new VirtualServiceBuilder().withApiVersion(API_VERSION).withKind(SERVICE_KIND)
                .withMetadata(getVirtualServiceMetaData(namespace, serviceName)).withSpec(getVirtualServiceSpec(serviceName))
                .build();
        istioClient.v1beta1().virtualServices().inNamespace(namespace).createOrReplace(virtualService);
        System.out.println("Created VirtualService " + serviceName + " in namespace " + namespace);
    }

    public void deleteVirtualService(final String serviceName, final String namespace) {
        System.out.println("Deleting VirtualService " + serviceName + " in namespace " + namespace);
        final VirtualService virtualService = new VirtualServiceBuilder().withApiVersion(API_VERSION).withKind(SERVICE_KIND)
                .withMetadata(getVirtualServiceMetaData(namespace, serviceName))
                .build();

        istioClient.v1beta1().virtualServices().delete(virtualService);
        System.out.println("Deleted VirtualService " + serviceName + " in namespace " + namespace);
    }

    private VirtualServiceSpec getVirtualServiceSpec(final String serviceName) {
        final VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHosts(Collections.singletonList(serviceName));

        final List<HTTPRoute> http = new ArrayList<>();
        final HTTPRoute httpRoute = new HTTPRoute();
        httpRoute.setTimeout("300s");

        final List<HTTPRouteDestination> route = new ArrayList<>();
        final HTTPRouteDestination routeDestination = new HTTPRouteDestination();
        routeDestination.setDestination(new Destination(serviceName, new PortSelector(8080), null));
        route.add(routeDestination);
        httpRoute.setRoute(route);

        httpRoute.setRetries(new HTTPRetry(3, "2s", null, null));

        http.add(httpRoute);

        spec.setHttp(http);
        return spec;
    }

    private ObjectMeta getVirtualServiceMetaData(final String namespace, final String serviceName) {
        final ObjectMeta metaData = new ObjectMeta();
        metaData.setName(serviceName);
        metaData.setNamespace(namespace);
        return metaData;
    }

    private VirtualServiceSpec getIngressServiceSpec() {
        final VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHosts(Collections.singletonList("10.246.15.246"));
        spec.setGateways(Collections.singletonList("istio-system/ingress-prod"));

        final List<HTTPRoute> httpRouteList = new ArrayList<>();
        final HTTPRoute httpRoute = new HTTPRoute();
        final List<HTTPMatchRequest> httpMatchRequests = new ArrayList<>();
        final HTTPMatchRequest httpMatchRequest = new HTTPMatchRequest();
        httpMatchRequest.setUri(new StringMatch(new StringMatchRegex("/systemcontext(/|$)(.*)")));
        httpMatchRequests.add(httpMatchRequest);
        httpRoute.setMatch(httpMatchRequests);

        final List<HTTPRouteDestination> routeDestinations = new ArrayList<>();
        final HTTPRouteDestination routeDestination = new HTTPRouteDestination();
        final Destination destination = new Destination("systemcontext", new PortSelector(8080), null);
        routeDestination.setDestination(destination);
        routeDestinations.add(routeDestination);
        httpRoute.setRoute(routeDestinations);

        httpRouteList.add(httpRoute);
        spec.setHttp(httpRouteList);
        return spec;
    }

    private ObjectMeta getIngressServiceMetaData(final String serviceName, final String namespace) {
        final ObjectMeta metaData = new ObjectMeta();
        metaData.setName(serviceName);
        metaData.setNamespace(namespace);
        return metaData;
    }
}
