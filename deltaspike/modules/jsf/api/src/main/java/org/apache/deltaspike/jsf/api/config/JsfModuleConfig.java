/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.jsf.api.config;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;

/**
 * Config for all JSF specific configurations.
 */
@ApplicationScoped
public class JsfModuleConfig
{
    public static final String CLIENT_WINDOW_CONFIG_KEY = "javax.faces.CLIENT_WINDOW_MODE";
    public static final String CLIENT_WINDOW_CLASS_NAME = "javax.faces.lifecycle.ClientWindow";

    private static final long serialVersionUID = -487295181899986237L;

    protected Boolean delegatedWindowHandlingEnabled;

    protected JsfModuleConfig()
    {
    }

    /**
     * If the window-handling of JSF 2.2+ is enabled,
     * {@link org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig.ClientWindowRenderMode#DELEGATED}
     * will be returned. In all other cases <code>null</code> gets returned as application wide default value.
     * That leads to a default-handling per session (which includes logic for handling bots,...)
     * @return application-default for the window-mode
     */
    public ClientWindowConfig.ClientWindowRenderMode getDefaultWindowMode()
    {
        if (this.delegatedWindowHandlingEnabled == null)
        {
            lazyInitDelegatedWindowHandlingEnabled();
        }

        if (this.delegatedWindowHandlingEnabled)
        {
            return ClientWindowConfig.ClientWindowRenderMode.DELEGATED;
        }
        return null;
    }

    protected synchronized void lazyInitDelegatedWindowHandlingEnabled()
    {
        if (this.delegatedWindowHandlingEnabled != null)
        {
            return;
        }

        this.delegatedWindowHandlingEnabled = isDelegatedWindowHandlingEnabled();
    }

    protected boolean isDelegatedWindowHandlingEnabled()
    {
        if (ClassUtils.tryToLoadClassForName(CLIENT_WINDOW_CLASS_NAME) == null)
        {
            return false;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext == null) // can happen in case of a very simple test-setup without a mocked jsf container
        {
            return false;
        }
        String configuredWindowHandling = facesContext.getExternalContext()
                                .getInitParameter(CLIENT_WINDOW_CONFIG_KEY);

        return !(configuredWindowHandling == null || "none".equalsIgnoreCase(configuredWindowHandling.trim()));
    }
}
