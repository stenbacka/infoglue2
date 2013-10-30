/**
 * 
 */
package org.infoglue.deliver.externalsearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.infoglue.cms.exception.SystemException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Erik Stenbäcka
 *
 */
public class ExternalSearchServiceTest
{
	ExternalSearchService service;
	ExternalSearchServiceConfig config;

	@Mocked ExternalSearchServiceDirectoryHandler handler;
	@Mocked DataRetriever dataRetriever;
	@Mocked Parser parser;
	@Mocked Indexer indexer;
	@Mocked ExternalSearchManager manager;

	@Before
	public void setUp() throws Exception
	{
		config = new ExternalSearchServiceConfig();
		config.setName("foobar");
		config.setMaxAge(null);
		config.setDataRetriever(dataRetriever);
		config.setParser(parser);
		config.setIndexer(indexer);
	}

	@Test
	public void testDestroyService()
	{
		// Execution
		service = new ExternalSearchService(config, false, handler);
		service.destroyService();

		new Verifications()
		{{
			dataRetriever.destroy();
			parser.destroy();
			indexer.destroy();
		}};
	}

	@Test
	public void testDelegateLifeCycleInit()
	{
		// Execution
		service = new ExternalSearchService(config, false, handler);

		new Verifications()
		{{
			dataRetriever.init();
			parser.init();
			indexer.init();
		}};
	}

	@Test
	public void testDelegateLifeCycleDestroy()
	{
		service = new ExternalSearchService(config, false, handler);

		ExternalSearchServiceConfig config2 = new ExternalSearchServiceConfig();
		config2.setMaxAge(null);
		config2.setDataRetriever(new DummyRetriever());
		config2.setParser(new DummyParser());
		config2.setIndexer(new DummyIndexer());

		// Execution
		service.setConfig(config2);

		new Verifications()
		{{
			dataRetriever.destroy();
			parser.destroy();
			indexer.destroy();
		}};
	}

	@Test
	public void testIndexHasExpiredNoMaxAge()
	{
		// Execution
		service = new ExternalSearchService(config, false, handler);

		assertFalse("If there is no max age the index should never expire", service.indexHasExpired());
	}

	@Test
	public void testIndexHasExpiredNoCreated()
	{
		config.setMaxAge(3600);

		// Execution
		service = new ExternalSearchService(config, false, handler);

		assertTrue("If the index has no age at present it should be marked as expired", service.indexHasExpired());
	}

	@Test
	public void testIndexHasNotExpired()
	{
		config.setMaxAge(3600); // This value is seconds, will be converted to ms inside.
		new NonStrictExpectations()
		{
			{
				handler.getDirectoryAge(); result = 1800000;
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);

		assertFalse("", service.indexHasExpired());
		new Verifications()
		{{
			handler.getDirectoryAge();
		}};
	}

	@Test
	public void testIndexHasExpired()
	{
		config.setMaxAge(3600); // This value is seconds, will be converted to ms inside.
		new NonStrictExpectations()
		{
			{
				handler.getDirectoryAge(); result = 3700000;
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);

		assertTrue("", service.indexHasExpired());
		new Verifications()
		{{
			handler.getDirectoryAge();
		}};
	}

	@Test
	public void testDontIndexWhenNoMaxAge() throws NullPointerException, IOException
	{
		new NonStrictExpectations()
		{
			{
				handler.getDirectory(); result = new RAMDirectory();
			}
		};

		service = new ExternalSearchService(config, false, handler);
		// Call start once to remove the start on new service indicator
		service.startIndexing();

		// Execution
		boolean doIndex = service.startIndexing();

		assertFalse("Should not start index if no max age", doIndex);
	}

	@Test
	public void testIndexWhenNoDependencies() throws NullPointerException, IOException
	{
		config.setMaxAge(3600);

		new NonStrictExpectations()
		{
			{
				handler.getDirectory(); result = new RAMDirectory();
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);
		boolean doIndex = service.startIndexing();

		assertTrue("Should start index if no dependencies", doIndex);
	}

	@Test
	public void testDontIndexWhenNonExistingDependency() throws NullPointerException, IOException
	{
		config.setMaxAge(3600);
		config.setDependencis(Collections.singletonList("apa"));

		new NonStrictExpectations()
		{
			{
				ExternalSearchManager.getManager(); result = manager;
				manager.getService("apa"); result = null;
				handler.getDirectory(); result = new RAMDirectory();
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);
		boolean doIndex = service.startIndexing();

		assertFalse("Should not start index when missing dependency", doIndex);
	}

	@Test
	public void testDontIndexWhenExistingDependencyNotSearchable() throws NullPointerException, IOException
	{
		config.setMaxAge(3600);
		config.setDependencis(Collections.singletonList("apa"));

		new NonStrictExpectations()
		{
			@Injectable ExternalSearchService mockService;
			{
				ExternalSearchManager.getManager(); result = manager;
				manager.getService("apa"); result = mockService;
				handler.getDirectory(); result = new RAMDirectory();
				mockService.isSearchable(); result = false;
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);
		boolean doIndex = service.startIndexing();

		assertFalse("Should not start index when dependency not searchable", doIndex);
	}

	@Test
	public void testIndexWhenExistingDependencySearchable() throws NullPointerException, IOException
	{
		config.setMaxAge(3600);
		config.setDependencis(Collections.singletonList("apa"));

		new NonStrictExpectations()
		{
			@Injectable ExternalSearchService mockService;
			{
				ExternalSearchManager.getManager(); result = manager;
				manager.getService("apa"); result = mockService;
				handler.getDirectory(); result = new RAMDirectory();
				mockService.isSearchable(); result = true;
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);
		boolean doIndex = service.startIndexing();

		assertTrue("Should start index when dependency is searchable", doIndex);
	}

	@Test
	public void testDontIndexMultipleDependencies() throws NullPointerException, IOException
	{
		final String DEPENDENCY_1 = "apa";
		final String DEPENDENCY_2 = "bepa";
		
		config.setMaxAge(3600);
		List<String> dependencies = new ArrayList<String>();
		dependencies.add(DEPENDENCY_1);
		dependencies.add(DEPENDENCY_2);
		config.setDependencis(dependencies);

		new NonStrictExpectations()
		{
			@Injectable ExternalSearchService mockService;
			{
				ExternalSearchManager.getManager(); result = manager;
				manager.getService(DEPENDENCY_1); result = mockService;
				manager.getService(DEPENDENCY_2); result = null;
				handler.getDirectory(); result = new RAMDirectory();
				mockService.isSearchable(); result = true;
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);
		boolean doIndex = service.startIndexing();

		assertFalse("Should not start index if missing one dependency even if there are valid dependensies", doIndex);
	}

	@Test
	public void testDontSearchWhenNotSearchable() throws SystemException
	{
		new NonStrictExpectations()
		{
			{
				handler.handleOldDirectories(); result = null;
			}
		};

		// Execution
		service = new ExternalSearchService(config, false, handler);
		SearchRequest searchRequest = service.getSearchRequest();
		SearchResult result = service.search(searchRequest);

		assertTrue("Should not have performed search so no items in the count", result.totalSize == 0);
		assertTrue("Should not have performed search so no items in the list", result.result.isEmpty());
	}

	@Test
	public void testSearchWhenSearchable() throws SystemException, IOException
	{
		String searchObject = "unittest";
		final IndexSearcher indexSearcher = LuceneMocks.getIndexSearcher(Collections.singletonList((Object)searchObject));
		new NonStrictExpectations()
		{
			{
				handler.handleOldDirectories(); result = indexSearcher;
			}
		};
		// Execution
		service = new ExternalSearchService(config, false, handler);
		SearchRequest searchRequest = service.getSearchRequest();
		SearchResult result = service.search(searchRequest);

		assertTrue("Should perform search when dependency searchable. Count should be 1", result.totalSize == 1);
		assertTrue("Should perform search when dependency searchable. Object should be present", result.result.get(0).equals(searchObject));
	}

	@Test
	public void testSearchWhenSearchable2() throws SystemException, IOException
	{
		String searchObject1 = "unittest";
		String searchObject2 = "testunit";
		List<Object> searchObjects = new ArrayList<Object>();
		searchObjects.add(searchObject1); searchObjects.add(searchObject2);
		final IndexSearcher indexSearcher = LuceneMocks.getIndexSearcher(searchObjects);
		new NonStrictExpectations()
		{
			{
				handler.handleOldDirectories(); result = indexSearcher;
			}
		};
		// Execution
		service = new ExternalSearchService(config, false, handler);
		SearchRequest searchRequest = service.getSearchRequest();
		SearchResult result = service.search(searchRequest);
		
		assertTrue("Should have performed search. Count should be 2", result.totalSize == 2);
		assertTrue("Should perform search when dependency searchable. Object1 should be present", result.result.contains(searchObject1));
		assertTrue("Should perform search when dependency searchable. Object2 should be present", result.result.contains(searchObject2));
	}
}
