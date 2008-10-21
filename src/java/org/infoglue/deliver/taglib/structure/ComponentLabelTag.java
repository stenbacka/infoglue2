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

package org.infoglue.deliver.taglib.structure;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.util.Support;
import org.infoglue.deliver.taglib.content.ContentAttributeTag;

/**
 * This is a new tag which get's a label from the current template. 
 * It's really just a variant of content:contentAttribute-tag but with the current component content id.
 */

public class ComponentLabelTag extends ContentAttributeTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private String attributeName 					= "ComponentLabels";
	private Integer languageId;
	private String mapKeyName;
    private boolean disableEditOnSight 				= true;
    private boolean useAttributeLanguageFallback 	= true; 
    
    public ComponentLabelTag()
    {
        super();
    }
    
	private String getContentAttributeValue(Integer languageId) throws JspException
	{
		System.out.println("attributeName:" + attributeName);
		System.out.println("componentContentId:" + getController().getComponentLogic().getInfoGlueComponent().getContentId());
		System.out.println("languageId:" + languageId);
		System.out.println("disableEditOnSight:" + disableEditOnSight);
		String result = getController().getContentAttribute(getController().getComponentLogic().getInfoGlueComponent().getContentId(), languageId, attributeName, disableEditOnSight);
		System.out.println("result:" + result);

		return result;
	}
	
    public int doEndTag() throws JspException
    {
	    if(this.languageId == null)
	        this.languageId = getController().getLanguageId();

        String result = null;

        result = getContentAttributeValue(this.languageId);
        if ( mapKeyName != null && result != null )
        {
            Map map = Support.convertTextToProperties( result.toString() );
            if ( map != null && !map.isEmpty() )
            {
                result = (String)map.get( mapKeyName );
            }
        }
        
        System.out.println("result2:" + result);
        if((result == null || result.trim().equals("")) && useAttributeLanguageFallback)
		{
			try
			{
	            LanguageVO masteLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(this.getController().getDatabase(), this.getController().getSiteNodeId());
			    result = getContentAttributeValue(masteLanguageVO.getLanguageId());
		        if ( mapKeyName != null && result != null )
		        {
		            Map map = Support.convertTextToProperties( result.toString() );
		            if ( map != null && !map.isEmpty() )
		            {
		                result = (String)map.get( mapKeyName );
		                System.out.println("result3:" + result);
		            }
		        }
			}
			catch(Exception e)
			{
				throw new JspException("Error getting the master language for this sitenode:" + this.getController().getSiteNodeId());
			}
		}
		
        produceResult( result );

	    attributeName = "ComponentLabels";
	    mapKeyName = null;;
	    disableEditOnSight = false;
	    useAttributeLanguageFallback = true;
	    languageId = null;

        return EVAL_PAGE;
    }

    public void setAttributeName(String attributeName) throws JspException
    {
        this.attributeName = evaluateString("contentAttribute", "attributeName", attributeName);
    }
    
    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("contentAttribute", "languageId", languageId);
    }

    public void setMapKeyName( String mapKeyName )
    {
        this.mapKeyName = mapKeyName;
    }
    
    public void setUseAttributeLanguageFallback(boolean useAttributeLanguageFallback)
    {
        this.useAttributeLanguageFallback = useAttributeLanguageFallback;
    }

}