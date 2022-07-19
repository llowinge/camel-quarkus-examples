/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme.master;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.arc.profile.UnlessBuildProfile;
import org.apache.camel.CamelContext;
import org.apache.camel.cluster.CamelClusterService;
import org.apache.camel.component.file.cluster.FileLockClusterService;
import org.apache.camel.component.kubernetes.cluster.KubernetesClusterService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ClusterLockProducer {
    @Inject
    KubernetesClient client;

    @Produces
    @UnlessBuildProfile("prod")
    public CamelClusterService fileLockClusterService(CamelContext camelContext) throws Exception {
        FileLockClusterService service = new FileLockClusterService();
        service.setRoot("cluster");
        service.setAcquireLockDelay(1, TimeUnit.SECONDS);
        service.setAcquireLockInterval(1, TimeUnit.SECONDS);
        return service;
    }

    @Produces
    @IfBuildProfile("prod")
    public CamelClusterService kubernetesClusterService(CamelContext camelContext) {
        KubernetesClusterService service = new KubernetesClusterService();
        service.setKubernetesNamespace(client.getNamespace());
        return service;
    }

}
