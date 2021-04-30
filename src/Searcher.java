import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    public static void search(String indexPath , String queriesPath ,String field) {
        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new ClassicSimilarity());
            searchQueries(indexSearcher,queriesPath, field);
            indexReader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void searchQueries(IndexSearcher indexSearcher, String queriesPath, String field) throws ParseException, IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);
        int q = 0;
        String text = "";
        for (String query : Utils.getAllQueries(queriesPath)){
            TopDocs results = indexSearcher.search(parser.parse(query), 50);
            ScoreDoc[] hits = results.scoreDocs;
            long numTotalHits = results.totalHits;

            for(int i=0; i<hits.length; i++){
                Document hitDoc = indexSearcher.doc(hits[i].doc);
                text += q + "\t0\t" + hitDoc.get("id") + "\t0\t" +hits[i].score + "\tmethod1\t" + "\n";
            }
            q++;
        }
        text = text.trim();
        try (PrintWriter out = new PrintWriter("RESULTS.test")) {
            out.println(text );
        }
    }

    public static void main(String[] args) {
        Searcher.search(System.getProperty("user.dir")+"/Index" , System.getProperty("user.dir")+"/lisa/LISA.QUE" , "contents");

    }
}
