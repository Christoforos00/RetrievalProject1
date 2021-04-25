import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

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
        for (String query : Utils.getAllQueries(queriesPath)){
            TopDocs results = indexSearcher.search(parser.parse(query), 20);
            ScoreDoc[] hits = results.scoreDocs;
            long numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");


        }


    }

    public static void main(String[] args) {
        Searcher.search(System.getProperty("user.dir")+"/Index" , System.getProperty("user.dir")+"/lisa/LISA.QUE" , "Contents");

    }
}
