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

package org.infoglue.deliver.controllers.kernel;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicURLComposer;

/**
 * Created by IntelliJ IDEA.
 * User: lbj
 * Date: 22-01-2004
 * Time: 17:00:24
 * To change this template use Options | File Templates.
 */

public abstract class URLComposer
{

    public static URLComposer getURLComposer()
    {
        String className = CmsPropertyHandler.getURLComposerClass();
        if (className == null || className.trim().equals(""))
            return new BasicURLComposer();

        // @TODO : implement dynamic loading of URLComposer
        return null;
    }


    public abstract String composeDigitalAssetUrl(String dnsName, String filename, DeliveryContext deliveryContext);

    public abstract String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException;

    public abstract String composePageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException;

    public abstract String composePageBaseUrl(String dnsName);

} 