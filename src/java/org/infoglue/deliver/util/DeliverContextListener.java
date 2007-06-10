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

package org.infoglue.deliver.util;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker;
import org.infoglue.deliver.invokers.DecoratedComponentBasedHTMLPageInvoker;

/**
 * This class functions as the entry-point for all initialization of the Cms-tool.
 * The class responds to the startup or reload of a whole context.
 */

public final class DeliverContextListener implements ServletContextListener 
{
    private final static Logger logger = Logger.getLogger(DeliverContextListener.class.getName());

	private static CacheController cacheController = new CacheController();
	
	private static ServletContext servletContext = null;
	
	public static ServletContext getServletContext()
	{
	    return servletContext;
	}
	
	/**
	 * This method is called when the servlet context is 
	 * initialized(when the Web Application is deployed). 
	 * You can initialize servlet context related data here.
     */
	 
    public void contextInitialized(ServletContextEvent event) 
    {
		try
		{		    
		    /*
			String isHeadless = System.getProperty("java.awt.headless");
			System.out.println("java.awt.headless=" + isHeadless);
			if(isHeadless == null || !isHeadless.equalsIgnoreCase("true"))
			{
				System.setProperty("java.awt.headless", "true");
			}
			*/
		    
			String contextRootPath = event.getServletContext().getRealPath("/");
			if(!contextRootPath.endsWith("/") && !contextRootPath.endsWith("\\")) 
				contextRootPath = contextRootPath + "/";
			
			System.out.println("\n**************************************");
			System.out.println("Initializing deliver context for directory:" + contextRootPath);
					
			CmsPropertyHandler.setApplicationName("deliver");
			
			CmsPropertyHandler.setContextRootPath(contextRootPath); 
			CmsPropertyHandler.setOperatingMode(CmsPropertyHandler.getProperty("operatingMode"));
			
			String logPath = CmsPropertyHandler.getLogPath();
			if(logPath == null || logPath.equals(""))
			{
				logPath = contextRootPath + "logs" + File.separator + "infoglueDeliver.log";
				CmsPropertyHandler.setProperty("logPath", logPath);
			}

			Enumeration enumeration = Logger.getLogger("org.infoglue.cms").getAllAppenders();
	        while(enumeration.hasMoreElements())
	        {
	        	RollingFileAppender appender = (RollingFileAppender)enumeration.nextElement();
	            if(appender.getName().equalsIgnoreCase("INFOGLUE-FILE"))
	            {
	    			appender.setFile(logPath);
	    			appender.activateOptions();
	            	Logger.getLogger(ComponentBasedHTMLPageInvoker.class).addAppender(appender);
	            	break;
	            }
	        }

			String statisticsLogPath = CmsPropertyHandler.getStatisticsLogPath();
			if(statisticsLogPath == null || statisticsLogPath.equals(""))
			{
				statisticsLogPath = contextRootPath + "logs";
				CmsPropertyHandler.setProperty("statisticsLogPath", statisticsLogPath);
			}

			String assetPath = CmsPropertyHandler.getDigitalAssetPath();
			if(assetPath == null || assetPath.equals(""))
			{
				assetPath = contextRootPath + "digitalAssets";
				CmsPropertyHandler.setProperty("digitalAssetPath", assetPath);
			}

			String digitalAssetPath0 = CmsPropertyHandler.getProperty("digitalAssetPath.0");
			if(digitalAssetPath0 == null || digitalAssetPath0.equals(""))
			{
				CmsPropertyHandler.setProperty("digitalAssetPath.0", assetPath);
			}

			String assetUploadPath = CmsPropertyHandler.getDigitalAssetUploadPath();
			if(assetUploadPath == null || assetUploadPath.equals(""))
			{
			    assetUploadPath = contextRootPath + "uploads";
				CmsPropertyHandler.setProperty("digitalAssetUploadPath", assetUploadPath);
			}

			String expireCacheAutomaticallyString = CmsPropertyHandler.getExpireCacheAutomatically();
			if(expireCacheAutomaticallyString != null)
				cacheController.setExpireCacheAutomatically(Boolean.parseBoolean(expireCacheAutomaticallyString));

			String intervalString = CmsPropertyHandler.getCacheExpireInterval();
			if(intervalString != null)
				cacheController.setCacheExpireInterval(Integer.parseInt(intervalString));
		
			//Starting the cache-expire-thread
			if(cacheController.getExpireCacheAutomatically())
				cacheController.start();
			
			InfoGlueAuthenticationFilter.initializeProperties();
			
			CmsPropertyHandler.setStartupTime(new Date()); 
			
			System.out.println("**************************************\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }

    /**
     * This method is invoked when the Servlet Context 
     * (the Web Application) is undeployed or 
     * WebLogic Server shuts down.
     */			    

    public void contextDestroyed(ServletContextEvent event) 
    {
		System.out.println("contextDestroyed....");
		cacheController.stopThread();
		cacheController.interrupt();
    }
}

