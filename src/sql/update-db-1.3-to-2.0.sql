-- ===============================================================================
--
-- Part of the InfoGlue Content Management Platform (www.infoglue.org)
--
-- ===============================================================================
--
--  Copyright (C)
--
-- This program is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License version 2, as published by the
-- Free Software Foundation. See the file LICENSE.html for more information.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT
-- ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along with
-- this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
-- Place, Suite 330 / Boston, MA 02111-1307 / USA.
--
-- ===============================================================================
--
-- $Id: update-db-1.3-to-2.0.sql,v 1.10 2005/03/31 07:54:11 mattias Exp $
--
-- This script contains the database updates required to go from 1.3 to 2.0.
----------------------------------------------------------------------------------
-- Update class names of invokers in cmSiteNodeTypeDefinition to reflect that
-- deliver stuff was moved into a separate package.  You will have to restart
-- your web server for the change to be visible in the management tool.
----------------------------------------------------------------------------------
update cmSiteNodeTypeDefinition
set invokerClassName = 'org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker'
where invokerClassName = 'org.infoglue.cms.invokers.ComponentBasedHTMLPageInvoker';

update cmSiteNodeTypeDefinition
set invokerClassName = 'org.infoglue.deliver.invokers.HTMLPageInvoker'
where invokerClassName = 'org.infoglue.cms.invokers.HTMLPageInvoker' OR 
invokerClassName = 'StandardHTMLInvoker' OR
invokerClassName = 'se.sprawl.services.invokers.HtmlInvoker' OR
invokerClassName = 'HTMLInvoker';

----------------------------------------------------------------------------------
-- Add table for Category
----------------------------------------------------------------------------------
DROP TABLE IF EXISTS cmCategory;

CREATE TABLE cmCategory
(
	categoryId		INTEGER(11) unsigned NOT NULL auto_increment,
	name			VARCHAR(100) NOT NULL,
	description		TEXT,
	active			TINYINT(4) NOT NULL default '1',
	parentId		INTEGER(11),
	PRIMARY KEY (categoryId)
);

----------------------------------------------------------------------------------
-- Add table for ContentCategory
----------------------------------------------------------------------------------
DROP TABLE IF EXISTS cmContentCategory;

CREATE TABLE cmContentCategory
(
	contentCategoryId	INTEGER(11) unsigned NOT NULL auto_increment,
	attributeName		VARCHAR(100) NOT NULL,
	contentVersionId	INTEGER(11) NOT NULL,
	categoryId			INTEGER(11) NOT NULL,
	PRIMARY KEY (contentCategoryId)
);

create index attributeName_categoryId on cmContentCategory (attributeName, categoryId);
create index contentVersionId on cmContentCategory (contentVersionId);

----------------------------------------------------------------------------------
-- Add table for UserPropertiesDigitalAsset
----------------------------------------------------------------------------------

DROP TABLE IF EXISTS cmUserPropertiesDigitalAsset;

CREATE TABLE cmUserPropertiesDigitalAsset (
  userPropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  userPropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (userPropertiesDigitalAssetId)
) TYPE=MyISAM;

----------------------------------------------------------------------------------
-- Add table for RolePropertiesDigitalAsset
----------------------------------------------------------------------------------

DROP TABLE IF EXISTS cmRolePropertiesDigitalAsset;

CREATE TABLE cmRolePropertiesDigitalAsset (
  rolePropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  rolePropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesDigitalAssetId)
) TYPE=MyISAM;

----------------------------------------------------------------------------------
-- Added sort possibility to repository languages
----------------------------------------------------------------------------------

ALTER TABLE cmRepositoryLanguage ADD COLUMN sortOrder integer default 0 NOT NULL;

----------------------------------------------------------------------------------
-- Added table for the new registry 
----------------------------------------------------------------------------------

DROP TABLE IF EXISTS cmRegistry;

CREATE TABLE cmRegistry
(
	registryId		            INTEGER(11) unsigned NOT NULL auto_increment,
	entityName		            VARCHAR(100) NOT NULL,
	entityId		            VARCHAR(200) NOT NULL,
	referenceType	            TINYINT(4) NOT NULL,
	referencingEntityName		VARCHAR(100) NOT NULL,
	referencingEntityId		    VARCHAR(200) NOT NULL,
	referencingEntityComplName	VARCHAR(100) NOT NULL,
	referencingEntityComplId	VARCHAR(200) NOT NULL,
    PRIMARY KEY (registryId)
);

----------------------------------------------------------------------------------
-- Add indexes to cmContent on parentContentId and contentTypeDefinitionId
----------------------------------------------------------------------------------
CREATE INDEX contentTypeDefinitionId ON cmContent (contentTypeDefinitionId);
CREATE INDEX parentContentId ON cmContent (parentContentId);

----------------------------------------------------------------------------------
-- Add indexes to cmPublicationDetail on publicationId (parent)
----------------------------------------------------------------------------------
CREATE INDEX publicationId ON cmPublicationDetail (publicationId);

----------------------------------------------------------------------------------
-- Add new interception point for content type definitions                      --
----------------------------------------------------------------------------------
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (27,'ContentTypeDefinition','ContentTypeDefinition.Read','This point checks access to read/use a content type definition',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (27, 1);
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (28,'Category','Category.Read','This point checks access to read/use a category',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (28, 1);


  
CREATE TABLE cmGroup (
  groupName varchar(255) NOT NULL default '',
  description text NOT NULL,
  PRIMARY KEY  (groupName)
) TYPE=MyISAM;

CREATE TABLE cmSystemUserGroup (
  userName varchar(200) NOT NULL default '',
  groupName varchar(200) NOT NULL default '',
  PRIMARY KEY  (userName,groupName)
) TYPE=MyISAM;


CREATE TABLE COPY_OF_cmAccessRight SELECT * FROM cmAccessRight;

ALTER TABLE cmAccessRight DROP roleName;

CREATE TABLE cmAccessRightRole (
  accessRightRoleId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  roleName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightRoleId),
) TYPE=MyISAM;

CREATE TABLE cmAccessRightGroup (
  accessRightGroupId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  groupName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightGroupId),
) TYPE=MyISAM;
