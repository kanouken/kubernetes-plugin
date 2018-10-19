/*
 * Copyright (C) 2017 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.csanchez.jenkins.plugins.kubernetes.pipeline;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud;
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Node;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * helper class for steps running in a kubernetes `node` context
 */
class KubernetesNodeContext {

    private static final transient Logger LOGGER = Logger.getLogger(KubernetesNodeContext.class.getName());

    private static final transient String HOSTNAME_FILE = "/etc/hostname";
    private StepContext context;
    private FilePath workspace;

    KubernetesNodeContext(StepContext context) throws Exception {
        this.context = context;
        workspace = context.get(FilePath.class);
    }

    String getPodName() throws Exception {
        String podName = System.getenv("POD_NAME");
        LOGGER.log(Level.FINEST, "Detected pod name: {0}", podName);
        return podName;
    }

    public String getNamespace() throws Exception {
        return workspace.child(Config.KUBERNETES_NAMESPACE_PATH).readToString().trim();
    }

    KubernetesClient connectToCloud() throws Exception {
        Node node = context.get(Node.class);
        if (! (node instanceof KubernetesSlave)) {
            throw new AbortException(String.format("Node is not a Kubernetes node: %s", node != null ? node.getNodeName() : null));
        }
        KubernetesSlave slave = (KubernetesSlave) node;
        KubernetesCloud cloud = slave.getKubernetesCloud();
        return cloud.connect();
    }
}
