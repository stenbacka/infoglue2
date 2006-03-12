/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.applications.managementtool.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGluePropertiesAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.entities.management.ServerNodeVO;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This class implements the action class for viewServerNodeProperties.
 * The use-case lets the user see all extra-properties for a serverNode
 * 
 * @author Mattias Bogeblad  
 */

public class ViewServerNodePropertiesAction extends InfoGluePropertiesAbstractAction
{ 
	private static final long serialVersionUID = 1L;

	private ServerNodeVO serverNodeVO 			= new ServerNodeVO();
	private PropertySet propertySet				= null; 
	private List serverNodeVOList				= null;
	
	
    public ViewServerNodePropertiesAction()
    {
    }
        
    protected void initialize(Integer serverNodeId) throws Exception
    {
        if(serverNodeId != null && serverNodeId.intValue() > -1)
            this.serverNodeVO = ServerNodeController.getController().getServerNodeVOWithId(serverNodeId);
     
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    this.propertySet = PropertySetManager.getInstance("jdbc", args);
    } 

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doExecute() throws Exception
    {
        this.initialize(getServerNodeId());

        return "success";
    }
    
    private void populate(PropertySet ps, String key)
    {
        String value = this.getRequest().getParameter(key);
	    if(value != null && !value.equals(""))
	        ps.setString("serverNode_" + this.getServerNodeId() + "_" + key, value);
    }

    private void populateData(PropertySet ps, String key)
    {
        try
        {
            String value = this.getRequest().getParameter(key);
    	    if(value != null && !value.equals(""))
    	        ps.setData("serverNode_" + this.getServerNodeId() + "_" + key, value.getBytes("utf-8"));            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSave() throws Exception
    {
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    populate(ps, "isPageCacheOn");
	    populate(ps, "useSelectivePageCacheUpdate");
	    populate(ps, "expireCacheAutomatically");
	    populate(ps, "cacheExpireInterval");
	    populate(ps, "session.timeout");
	    populate(ps, "compressPageCache");
	    populate(ps, "siteNodesToRecacheOnPublishing");
	    populate(ps, "recachePublishingMethod");
	    populate(ps, "recacheUrl");
	    populate(ps, "useUpdateSecurity");
	    populate(ps, "allowedAdminIP");
	    populate(ps, "pageKey");
	    populate(ps, "cmsBaseUrl");
	    populate(ps, "componentEditorUrl");
	    populate(ps, "componentRendererUrl");
	    populate(ps, "componentRendererAction");
	    populate(ps, "editOnSiteUrl");
	    populate(ps, "useFreeMarker");
	    populate(ps, "webServerAddress");
	    populate(ps, "applicationBaseAction");
	    populate(ps, "digitalAssetBaseUrl");
	    populate(ps, "imagesBaseUrl");
	    populate(ps, "digitalAssetPath");
	    populate(ps, "urlFormatting");
	    populate(ps, "enableNiceURI");
	    populate(ps, "niceURIEncoding");
	    populate(ps, "niceURIAttributeName");
	    populate(ps, "requestArgumentDelimiter");
	    populate(ps, "errorHandling");
	    populate(ps, "errorUrl");
	    populate(ps, "errorBusyUrl");
	    populate(ps, "externalThumbnailGeneration");
	    populate(ps, "URIEncoding");
	    populate(ps, "workflowEncoding");
	    populate(ps, "formsEncoding");
	    populate(ps, "useShortTableNames");
	    populate(ps, "logDatabaseMessages");
	    populate(ps, "statistics.enabled");
	    populate(ps, "statisticsLogPath");
	    populate(ps, "statisticsLogOneFilePerDay");
	    populate(ps, "statisticsLogger");
	    populate(ps, "enablePortal");
	    populate(ps, "portletBase");
	    populate(ps, "mail.smtp.host");
	    populate(ps, "mail.smtp.auth");
	    populate(ps, "mail.smtp.user");
	    populate(ps, "mail.smtp.password");
	    populate(ps, "mail.contentType");
	    populate(ps, "systemEmailSender");
	    populate(ps, "loginUrl");
	    populate(ps, "logoutUrl");
	    populate(ps, "invalidLoginUrl");
	    populate(ps, "successLoginBaseUrl");
	    populate(ps, "authenticatorClass");
	    populate(ps, "authorizerClass");
	    populate(ps, "serverName");
	    populate(ps, "authConstraint");
	    populate(ps, "extraParametersFile");
	    populateData(ps, "extraSecurityParameters");
	    populate(ps, "casValidateUrl");
	    populate(ps, "casServiceUrl");
	    populate(ps, "casLogoutUrl");
	    
	    populate(ps, "deliver_loginUrl");
	    populate(ps, "deliver_logoutUrl");
	    populate(ps, "deliver_invalidLoginUrl");
	    populate(ps, "deliver_successLoginBaseUrl");
	    populate(ps, "deliver_authenticatorClass");
	    populate(ps, "deliver_authorizerClass");
	    populate(ps, "deliver_serverName");
	    populate(ps, "deliver_authConstraint");
	    populate(ps, "deliver_extraParametersFile");
	    populateData(ps, "deliver_extraSecurityParameters");
	    populate(ps, "deliver_casValidateUrl");
	    populate(ps, "deliver_casServiceUrl");
	    populate(ps, "deliver_casLogoutUrl");
	    populateData(ps, "shortcuts");

	    populate(ps, "protectContentTypes");
	    populate(ps, "protectWorkflows");
	    populate(ps, "protectCategories");

	    populate(ps, "maxRows");
	    populate(ps, "showContentVersionFirst");
	    populate(ps, "tree");
	    populate(ps, "treemode");
	    populate(ps, "showComponentsFirst");
	    populate(ps, "showAllWorkflows");
	    populate(ps, "editOnSight");
	    populate(ps, "previewDeliveryUrl");
	    populate(ps, "stagingDeliveryUrl");
	    populate(ps, "edition.pageSize");
	    populate(ps, "content.tree.sort");
	    populate(ps, "structure.tree.sort");
	    populate(ps, "disableEmptyUrls");
	    populate(ps, "cacheUpdateAction");
	    populate(ps, "logPath");

	    populate(ps, "logTransactions");
	    populate(ps, "enableExtranetCookies");
	    populate(ps, "useAlternativeBrowserLanguageCheck");
	    populate(ps, "caseSensitiveRedirects");
	    populate(ps, "useDNSNameInURI");
	    
	    populate(ps, "extranetCookieTimeout");
	    populate(ps, "webServicesBaseUrl");
	    populate(ps, "publicationThreadDelay");
	    populate(ps, "pathsToRecacheOnPublishing");
	    populate(ps, "disableTemplateDebug");
	    populate(ps, "dbRelease");
	    populate(ps, "dbUser");
	    populate(ps, "dbPassword");
	    populate(ps, "masterServer");
	    populate(ps, "slaveServer");
	    populate(ps, "buildName");
	    populate(ps, "adminToolsPath");
	    populate(ps, "dbScriptPath");
	    populate(ps, "digitalAssetUploadPath");

		NotificationMessage notificationMessage = new NotificationMessage("ViewServerNodePropertiesAction.doSave():", "ServerNodeProperties", this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, "0", "ServerNodeProperties");
		//ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
		RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
		
	    //TODO - hack to get the caches to be updated when properties are affected..
	    /*
	    ServerNodeVO serverNodeVO = ServerNodeController.getController().getFirstServerNodeVO();
	    serverNodeVO.setDescription(serverNodeVO.getDescription() + ".");
	    ServerNodeController.getController().update(serverNodeVO);
	    */
	    
    	return "save";
    }

    /**
     * The main method that fetches the Value-objects for this use-case
     */
    
    public String doSaveAndExit() throws Exception
    {
        return "saveAndExit";
    }

    public java.lang.Integer getServerNodeId()
    {
        return this.serverNodeVO.getServerNodeId();
    }
        
    public void setServerNodeId(java.lang.Integer serverNodeId) throws Exception
    {
        this.serverNodeVO.setServerNodeId(serverNodeId);
    }

	public ServerNodeVO getServerNodeVO() 
	{
		return serverNodeVO;
	}

	public PropertySet getPropertySet() 
	{
		return propertySet;
	}

	public String getPropertyValue(String prefix, String key) 
	{
		String value = propertySet.getString("serverNode_" + this.getServerNodeId() + "_" + prefix + "_" + key);

		return (value != null ? value : "");
	}

	public String getPropertyValue(String key) 
	{
		String value = propertySet.getString("serverNode_" + this.getServerNodeId() + "_" + key);

		return (value != null ? value : "");
	}
	
	public String getDataPropertyValue(String key) throws Exception
	{
		byte[] valueBytes = propertySet.getData("serverNode_" + this.getServerNodeId() + "_" + key);
	    
		return (valueBytes != null ? new String(valueBytes, "utf-8") : "");
	}

	public String getDataPropertyValue(String prefix, String key) throws Exception
	{
		byte[] valueBytes = propertySet.getData("serverNode_" + this.getServerNodeId() + "_" + prefix + "_" + key);
	    
		return (valueBytes != null ? new String(valueBytes, "utf-8") : "");
	}

    public List getServerNodeVOList()
    {
        return serverNodeVOList;
    }
}
