package org.infoglue.deliver.externalsearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import mockit.Delegate;
import mockit.Injectable;
import mockit.NonStrictExpectations;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class LuceneMocks
{
	public static IndexSearcher getIndexSearcher(final List<Object> searchObjects) throws IOException
	{
		final byte[][] resultArray = new byte[searchObjects.size()][];
		for (int i = 0; i < searchObjects.size(); i++)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(searchObjects.get(i));
			oos.close();
			resultArray[i] = baos.toByteArray();
		}
		return new NonStrictExpectations()
		{
			IndexSearcher indexSearcher;
			Hits hits;
			{
				indexSearcher.search((Query)any); result = hits;
				hits.length(); result = resultArray.length;
				hits.doc(anyInt); result = new Delegate<Integer>()
				{
					@SuppressWarnings("unused")
					Document doc(final int i) throws IOException
					{
						System.out.println("i: " + i);
						return new NonStrictExpectations()
						{
							@Injectable Document doc;
							{
								doc.getBinaryValue(anyString); result = resultArray[i];
							}
							Document getDocument()
							{
								return doc;
							}
						}.getDocument();
					}
				};
			}
			IndexSearcher getIndexSearcher()
			{
				return indexSearcher;
			}
		}.getIndexSearcher();
	}
}
