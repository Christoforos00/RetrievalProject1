import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Utils {

    public static ArrayList<CustomDoc> getAllDocs(String path) {
        ArrayList<CustomDoc> docs = new ArrayList<CustomDoc>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);

        for (File file : listOfFiles) {
            if (file.getName().equals("LISA.QUE") || file.getName().equals("LISA.REL") || file.getName().equals("LISARJ.NUM") || file.getName().equals("README"))
                continue;

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String currentBatch = getNextBatch(reader);

                while (!currentBatch.equals("")) {
                    docs.add(batchToDoc(currentBatch));
                    currentBatch = getNextBatch(reader);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return docs;
    }


    public static ArrayList<String> getAllQueries(String path) {
        ArrayList<String> queries = new ArrayList<String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String currentQuery = getNextQuery(reader);

            while (!currentQuery.equals("")) {
                queries.add(currentQuery);
                currentQuery = getNextQuery(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }


    public static void generateTrecEvalQrels(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String currentResult = getNextResult(reader);
            String text = currentResult;
            while (!currentResult.equals("")) {
                currentResult = getNextResult(reader);
                if (!currentResult.equals(""))
                    text += "\n" + currentResult;
            }
            try (PrintWriter out = new PrintWriter("QRELS.test")) {
                out.println(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getNextResult(BufferedReader reader) throws IOException {
        String result = "", qnum;
        int resnum;
        String line = reader.readLine();
        if (line == null)
            return result;
        ArrayList<String> parts = new ArrayList<String>(Arrays.asList(line.split("        ")));
        qnum = "";
        resnum = 0;
        int i = 0;
        while (line != null) {
            parts = new ArrayList<String>(Arrays.asList(line.split("        ")));
            for (String part : parts) {
                if (part.equals("")) {
                    i -= 1;
                } else if (i == 0) {
                    qnum = part.trim();
                } else if (i == 1) {
                    resnum = Integer.parseInt(part.trim());
                } else if (i > 1) {
                    result += qnum + "\t0\t" + part.trim() + "\t1" + "\n";
                    if (i > resnum)
                        return result.trim();

                }
                i++;
            }
            line = reader.readLine();
        }

        return result.trim();
    }


    private static String getNextBatch(BufferedReader reader) throws IOException {
        String batch = "";
        String line = reader.readLine();
        if (line != null)
            batch += line.replace("Document ", "");
        line = reader.readLine();
        while (line != null) {
            if (line.contains("****"))
                break;
            batch += line + "\n";
            line = reader.readLine();
        }
        return batch;
    }

    private static CustomDoc batchToDoc(String batch) {
        int titleEndIndex = batch.indexOf(".");
        while (!batch.substring(titleEndIndex + 1, titleEndIndex + 2).equals("\n")) {
            titleEndIndex = batch.indexOf(".", titleEndIndex + 1);
        }
        String id = batch.substring(0, 4).trim();
        String title = batch.substring(4, titleEndIndex + 1).replace("\n", " ").trim();
        title = title.replaceAll("[^a-zA-Z ]", " ");
        String body = batch.substring(titleEndIndex + 1, batch.length()).replace("\n", " ").trim();
        body = body.replaceAll("[^a-zA-Z ]", " ");
        return new CustomDoc(title, body, id);
    }

    private static String getNextQuery(BufferedReader reader) throws IOException {
        String batch = "";
        reader.readLine();
        String line = reader.readLine();
        while (line != null) {
            batch += line + " ";
            if (line.contains("#")) {
                batch = batch.substring(0, batch.length() - 2);
                break;
            }
            line = reader.readLine();
        }
        return batch;
    }

}
