import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.classification.utils.DocToDoubleVectorUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class TermDocMatrixCreator {


    public static void CreateTermDocMatrix(String path) {
        try {
            Directory indexDir = FSDirectory.open(Paths.get("Index2"));
            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            Similarity similarity = new ClassicSimilarity();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            config.setSimilarity(similarity);

            FieldType type = new FieldType();
            type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            type.setTokenized(true);
            type.setStored(true);
            type.setStoreTermVectors(true);
            IndexWriter indexWriter = new IndexWriter(indexDir, config);

            // Documents
            for (CustomDoc cDoc : Utils.getAllDocs(path)) {
                Document doc = new Document();
                doc.add(new Field("contents", cDoc.getTitle() + " " + cDoc.getBody(), type));
                if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE)
                    indexWriter.addDocument(doc);
            }

            // Queries
            for (String query : Utils.getAllQueries(path + File.separator + "LISA.QUE")) {
                Document doc = new Document();
                doc.add(new Field("contents", query, type));
                if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE)
                    indexWriter.addDocument(doc);
            }

            indexWriter.close();

            IndexReader reader = DirectoryReader.open(indexDir);
            testSparseFreqDoubleArrayConversion(reader);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testSparseFreqDoubleArrayConversion(IndexReader reader) throws Exception {
        Terms fieldTerms = MultiFields.getTerms(reader, "contents");   //the number of terms in the lexicon after analysis of the Field "title"

        System.out.println("Terms:" + fieldTerms.size());

        TermsEnum it = fieldTerms.iterator();                        //iterates through the terms of the lexicon

        while (it.next() != null) {
            System.out.print(it.term().utf8ToString() + " ");        //prints the terms
        }
        File file = new File("termDocMatrix.csv");
        BufferedWriter output = new BufferedWriter(new FileWriter(file));

        if (fieldTerms != null && fieldTerms.size() != -1) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {   //retrieves all documents
                System.out.println("DocID: " + scoreDoc.doc);
                Terms docTerms = reader.getTermVector(scoreDoc.doc, "contents");
                Double[] vector = DocToDoubleVectorUtils.toSparseLocalFreqDoubleArray(docTerms, fieldTerms); //creates document's vector
                NumberFormat nf = new DecimalFormat("0.#");
                for (int i = 0; i <= vector.length - 1; i++) {

                    output.write(nf.format(vector[i]) + ",");
                }
                output.write("\n");
            }
        }
        output.close();
    }


    public static void main(String[] args) {
        CreateTermDocMatrix(System.getProperty("user.dir") + File.separator + "lisa");
    }


}
