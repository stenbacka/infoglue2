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

package org.infoglue.cms.controllers.kernel.impl.simple;

import org.infoglue.cms.entities.kernel.*;
import org.infoglue.cms.entities.management.*;
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.util.CacheController;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Mattias Bogeblad
 */

public class RedirectController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RedirectController.class.getName());

	/**
	 * Factory method
	 */

	public static RedirectController getController()
	{
		return new RedirectController();
	}

    public RedirectVO getRedirectVOWithId(Integer redirectId) throws SystemException, Bug
    {
		return (RedirectVO) getVOWithId(RedirectImpl.class, redirectId);
    }

    public Redirect getRedirectWithId(Integer redirectId, Database db) throws SystemException, Bug
    {
		return (Redirect) getObjectWithId(RedirectImpl.class, redirectId, db);
    }

    public List getRedirectVOList() throws SystemException, Bug
    {
		List redirectVOList = getAllVOObjects(RedirectImpl.class, "redirectId");

		return redirectVOList;
    }
    
    public RedirectVO create(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
        Redirect redirect = new RedirectImpl();
        redirect.setValueObject(redirectVO);
        redirect = (Redirect) createEntity(redirect);
        return redirect.getValueObject();
    }

    public void delete(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
    	deleteEntity(RedirectImpl.class, redirectVO.getRedirectId());
    }

    public RedirectVO update(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
    	return (RedirectVO) updateEntity(RedirectImpl.class, redirectVO);
    }
    
    /**
     * This method checks if there is a redirect that should be used instead.
     * @param requestURI
     * @throws Exception
     */
    
    public String getRedirectUrl(HttpServletRequest request) throws Exception
    {
        try
        {
            String requestURL = request.getRequestURL().toString();
            logger.info("requestURL:" + requestURL);
            
            Collection cachedRedirects = (Collection)CacheController.getCachedObject("redirectCache", "allRedirects");
            if(cachedRedirects == null)
            {
                cachedRedirects = RedirectController.getController().getRedirectVOList();
                CacheController.cacheObject("redirectCache", "allRedirects", cachedRedirects);
            }
            
            Iterator redirectsIterator = cachedRedirects.iterator();
            while(redirectsIterator.hasNext())
            {
                RedirectVO redirect = (RedirectVO)redirectsIterator.next(); 
                logger.info("url:" + redirect.getUrl());
                if(redirect.getUrlCompiledPattern().matcher(requestURL).matches())
                {
                    return redirect.getRedirectUrl();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new SystemException("An error occurred when looking for page:" + e.getMessage());
        }
        
        return null;
    }


	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentTypeDefinitionVO();
	}
}
