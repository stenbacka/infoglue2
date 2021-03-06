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

/**
 * 
 */
package org.infoglue.deliver.externalsearch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.infoglue.cms.exception.ConfigurationError;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.externalsearch.SearchRequest.ParameterType;

/**
 * @author Erik Stenbäcka
 * 
 */
public class ExternalSearchService
{
	private static final Logger logger = Logger.getLogger(ExternalSearchService.class);

	private AtomicBoolean running;
	private String name;
	private Integer maxAge;
	private Locale defaultLanguage;
	private Map<String, IndexableField> fields;
	private List<String> dependencies;
	private boolean startIndexing;

	private DataRetriever dataRetriever;
	private Parser parser;
	private Indexer indexer;

	private ExternalSearchServiceConfig newConfig;
	private IndexSearcher indexSearcher;

	private ExternalSearchServiceDirectoryHandler directoryHandler;

	public ExternalSearchService(ExternalSearchServiceConfig config) throws ConfigurationError
	{
		this(config, true, null);
	}

	public ExternalSearchService(ExternalSearchServiceConfig config, boolean startIndexing, ExternalSearchServiceDirectoryHandler directoryHandler) throws ConfigurationError
	{
		this.name = config.getName();
		if (this.name == null)
		{
			throw new ConfigurationError("A service needs a name. The configuration file should contain one.");
		}
		if (directoryHandler == null)
		{
			this.directoryHandler = new ExternalSearchServiceDirectoryHandler(this.name);
		}
		else
		{
			this.directoryHandler = directoryHandler;
		}
		this.running = new AtomicBoolean(false);
		this.startIndexing = startIndexing;
		setConfig(config);
		this.indexSearcher = this.directoryHandler.handleOldDirectories();
	}

	public String getName()
	{
		return name;
	}

	public Integer getIndexAge()
	{
		return directoryHandler.getDirectoryAge();
	}

	public Integer getMaxAge()
	{
		return maxAge;
	}

	public boolean getStartIndexing()
	{
		return startIndexing;
	}

	public void forceReindexing()
	{
		this.startIndexing = true;
	}

	public Integer getNumberEntries()
	{
		if (indexSearcher == null)
		{
			return -1;
		}
		try
		{
			return indexSearcher.maxDoc();
		}
		catch (IOException ex)
		{
			logger.warn("IOException when trying to read number of entities in service. Service : " + name, ex);
			return -1;
		}
	}

	public boolean isIndexing()
	{
		return running.get();
	}

	public String getConfigDetails()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Data retreiver: ").append(this.dataRetriever.getClass()).append("\n");
		sb.append(this.dataRetriever.getConfigDetails("  ")).append("\n");
		sb.append("Parser: ").append(this.parser.getClass()).append("\n");
		sb.append(this.parser.getConfigDetails("  ")).append("\n");
		sb.append("Indexer: ").append(this.indexer.getClass()).append("\n");
		sb.append(this.indexer.getConfigDetails("  ")).append("\n");
		sb.append("Dependencies: ").append(this.dependencies).append("\n");
		sb.append("Max age: ").append(this.maxAge).append("\n");
		sb.append("Fields: ").append("\n");
		for (IndexableField field : fields.values())
		{
			sb.append("  ").append(field).append("\n");
		}
		return sb.toString();
	}

	public void setConfig(ExternalSearchServiceConfig newConfig) throws ConfigurationError
	{
		logger.info("Queing new config for service: " + name);
		this.newConfig = newConfig;
		if (running.compareAndSet(false, true))
		{
			updateConfig();
			running.set(false);
		}
	}

	public boolean startIndexing()
	{
		if ((startIndexing || indexHasExpired()) && checkDependencies())
		{
			if (running.compareAndSet(false, true))
			{
				startIndexing = false;
				logger.debug("Should start indexing for service: " + name);
				new Thread()
				{
					@Override
					public void run()
					{
						logger.info("Starting indexing for service: " + name);
						updateIndex();
						try
						{
							updateConfig();
						}
						catch (ConfigurationError cex)
						{
							logger.error("Invalid config for external search service. See warning log for more info. Service name: " + name);
							logger.warn("Invalid config for external search service. See warning log for more info. Service name: " + name + ". Config: " + newConfig);
						}
						running.set(false);
					}
				}.start();

				return true;
			}
		}

		logger.debug("It is not time to start service. Service name: " + name);
		return false;
	}

	public SearchResult search(SearchRequest params) throws SystemException
	{
		List<Object> result = new ArrayList<Object>();
		if (isSearchable())
		{
			try
			{
				StandardAnalyzer analyzer = new StandardAnalyzer();
				Query query = params.getQuery(analyzer);

				Hits hits;
				if (params.shouldSort())
				{
					Sort sort = params.getOrdering();
					if (logger.isDebugEnabled())
					{
						logger.debug("Searching with sort. Sort: " + Arrays.toString(sort.getSort()));
					}
					hits = indexSearcher.search(query, sort);
				}
				else
				{
					logger.debug("Searching without sort");
					hits = indexSearcher.search(query);
				}

				// Examine the Hits object to see if there were any matches
				int hitCount = hits.length();
				if (logger.isDebugEnabled())
				{
					logger.debug("hit count: " + hitCount + " for query: " + query.toString());
				}
				if (hitCount > 0)
				{
					if (params.getCount() != null)
					{
						hitCount = Math.min(hitCount, params.getStartIndex() + params.getCount());
					}
					for (int i = params.getStartIndex() == null ? 0 : params.getStartIndex(); i < hitCount; i++)
					{
						try
						{
							Document document = hits.doc(i);

							if (logger.isDebugEnabled())
							{
								StringBuilder sb = new StringBuilder();
								for (Object field : document.getFields())
								{
									sb.append(document.get(field.toString())).append(", ");
								}
								logger.debug("Search found document: " + sb);
							}

							byte[] resultBytes = document.getBinaryValue(SearchResult.getResultLabel(params.getLanguage()));
							if (resultBytes != null)
							{
								ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(resultBytes));
								Object post = ois.readObject();
								result.add(post);
							}
						}
						catch (Exception ex)
						{
							logger.warn("There was an error preparing a search hit for the result list. The item will be excluded from the list. Message: " + ex.getMessage() + ". Type: " + ex.getClass());
						}
					}
				}
				return new SearchResult(result, hits.length());
			}
			catch (ParseException pex)
			{
				logger.warn("Invalid Lucene search query in external search service. Service.name: " + name + ". Message: " + pex.getMessage());
				throw new SystemException("Invalid Lucene search query. See warning logs for more information.");
			}
			catch (Throwable tr)
			{
				logger.error("Error when searching in external search service. Service.name: " + name + ". Message: " + tr.getMessage() + ". Type: " + tr.getClass());
				logger.warn("Error when updating index for external search service. Service.name: " + name, tr);
				throw new SystemException("Error in Lucene search. See warning logs for more information.");
			}
		}

		return new SearchResult(Collections.emptyList(), 0);
	}

	public void destroyService()
	{
		if (this.dataRetriever != null)
		{
			this.dataRetriever.destroy();
		}
		if (this.parser != null)
		{
			this.parser.destroy();
		}
		if (this.indexer != null)
		{
			this.indexer.destroy();
		}
		if (this.directoryHandler != null)
		{
			this.directoryHandler.destroy(this.indexSearcher);
		}
	}

	public boolean isSearchable()
	{
		return indexSearcher != null;
	}

	public boolean indexHasExpired()
	{
		if (this.maxAge == null)
		{
			logger.debug("Service has no max age => the index has not expired. Service: " + name);
			return false;
		}
		Integer directoryAge = this.directoryHandler.getDirectoryAge();
		if (directoryAge == null)
		{
			logger.debug("No directory age => the index has expired. Service: " + name);
			return true;
		}
		else
		{
			if (logger.isTraceEnabled())
			{
				logger.trace("Comparing directory age. Diractory age: " + directoryAge + ", max age: " + maxAge + ". Service: " + name);
			}
			return directoryAge > this.maxAge;
		}
	}

	// ///////////////////////////////////////////////////////////////

	private void updateConfig() throws ConfigurationError
	{
		if (newConfig != null)
		{
			logger.info("Updating config for service: " + name);

			if (this.newConfig.getDataRetriever() == null || this.newConfig.getParser() == null || this.newConfig.getIndexer() == null)
			{
				throw new ConfigurationError("The new configuration does not contain all required entities. Config: " + this.newConfig);
			}

			this.maxAge = this.newConfig.getMaxAge() == null ? null : this.newConfig.getMaxAge() * 1000;
			this.dependencies = this.newConfig.getDependencies() == null ? new LinkedList<String>() : this.newConfig.getDependencies();
			this.defaultLanguage = this.newConfig.getDefaultLanguage();
			this.fields = this.newConfig.getFields();
			if (this.defaultLanguage == null)
			{
				this.defaultLanguage = Locale.ENGLISH;
			}

			if (this.dataRetriever != null)
			{
				this.dataRetriever.destroy();
			}
			this.dataRetriever = this.newConfig.getDataRetriever();
			this.dataRetriever.init();

			if (this.parser != null)
			{
				this.parser.destroy();
			}
			this.parser = this.newConfig.getParser();
			this.parser.init();

			if (this.indexer != null)
			{
				this.indexer.destroy();
			}
			this.indexer = this.newConfig.getIndexer();
			this.indexer.init();
			this.indexer.registerFields(fields);
			
			this.newConfig = null;
			startIndexing = true;
		}
		else
		{
			logger.debug("No new config to update to for service name: " + name);
		}
	}

	private void updateIndex()
	{
		IndexWriter indexWriter = null;
		Directory directory = null;
		IndexSearcher newIndexSearcher = null;
		try
		{
			if (!this.dataRetriever.hasChanged())
			{
				logger.debug("Skipping indexing since the underlying data source has not changed");
				return;
			}
			InputStream input = this.dataRetriever.openConnection();
			List<Map<String, Object>> entities = this.parser.parse(input);
			this.dataRetriever.closeConnection();

			directory = this.directoryHandler.getDirectory();

			StandardAnalyzer analyzer = new StandardAnalyzer();
			indexWriter = new IndexWriter(directory, analyzer, true);

			this.indexer.index(entities, fields, indexWriter);

			indexWriter.optimize();

			newIndexSearcher = this.directoryHandler.changeDirectory(this.indexSearcher, directory);
		}
		catch (Throwable tr)
		{
			logger.error("Error when updating index for external search service. Service.name: " + name + ". Message: " + tr.getMessage() + ". Type: " + tr.getClass());
			logger.warn("Error when updating index for external search service. Service.name: " + name, tr);
			if (directory != null)
			{
				this.directoryHandler.deleteDirectory(directory);
			}
		}
		finally
		{
			if (indexWriter != null)
			{
				try
				{
					indexWriter.close();
					this.indexSearcher = newIndexSearcher;
				}
				catch (Throwable tr)
				{
					logger.error("Error when closing IndexWriter for external search service. Service.name: " + name + ". Message: " + tr.getMessage() + ". Type: " + tr.getClass());
					logger.warn("Error when closing IndexWriter for external search service. Service.name: " + name, tr);
					this.directoryHandler.deleteDirectory(directory);
				}
			}
		}
	}

	private boolean checkDependencies()
	{
		if (dependencies != null)
		{
			for (String serviceName : dependencies)
			{
				logger.info("Checking dependency for service: " + name + ". Dependency: " + serviceName);
				ExternalSearchService service = ExternalSearchManager.getManager().getService(serviceName);
				if (service == null)
				{
					logger.debug("Dependecy was not found");
					return false;
				}
				else
				{
					if (!service.isSearchable())
					{
						logger.debug("Dependecy was not searchable");
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public SearchRequest getSearchRequest()
	{
		return getSearchRequest(null);
	}

	public SearchRequest getSimpleSearchRequest(String field, String query)
	{
		return getSimpleSearchRequest(null, field, query);
	}

	public SearchRequest getSearchRequest(Locale language)
	{
		if (language == null)
		{
			language = defaultLanguage;
		}
		return new SearchRequest(fields, language);
	}

	public SearchRequest getSimpleSearchRequest(Locale language, String field, String query)
	{
		SearchRequest request = getSearchRequest(language);
		request.addSearchParameter(field, query);
		return request;
	}

	public SearchRequest getFreeTextSearchRequest(Locale language, String query)
	{
		SearchRequest request = getSearchRequest(language);
		Collection<?> fieldNames = indexSearcher.getIndexReader().getFieldNames(FieldOption.INDEXED);

		for (Object obj : fieldNames)
		{
			request.addParameter(obj.toString(), query, ParameterType.SHOULD);
		}

		return request;
	}

	public SearchRequest getListAllQuery(Locale language)
	{
		SearchRequest request = getSearchRequest(language);
		request.listAll();
		return request;
	}
}



