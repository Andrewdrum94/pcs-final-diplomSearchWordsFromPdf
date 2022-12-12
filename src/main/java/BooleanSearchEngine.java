import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    //???
    private Map<String, List<PageEntry>> index = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        // прочтите тут все pdf и сохраните нужные данные,
        // тк во время поиска сервер не должен уже читать файлы
        for (File pdfFile : Objects.requireNonNull(pdfsDir.listFiles())) {
            var doc = new PdfDocument(new PdfReader(pdfFile));
            for (int i = 0; i < doc.getNumberOfPages(); i++) {//i = номер страницы, смотрим одну страницу
                var text = PdfTextExtractor.getTextFromPage(doc.getPage(i + 1));
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();//ключ-слово, значение-count
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    freqs.put(word.toLowerCase(Locale.ROOT), freqs.getOrDefault(word.toLowerCase(Locale.ROOT), 0) + 1);
                }
                for (Map.Entry<String, Integer> freq : freqs.entrySet()) {
                    if (index.containsKey(freq.getKey())) {//если в мапе есть такой ключ, обновляем список PageEntry
                        List<PageEntry> uploaded = new ArrayList<>(index.get(freq.getKey()));//создаём новый список и кладём в него список из мапы, ищем по ключу.
                        uploaded.add(new PageEntry(pdfFile.getName(), (i + 1), freq.getValue()));//добавляем новую страницу
                        Collections.sort(uploaded);//Сортируем список
                        index.put(freq.getKey(), uploaded);
                    } else {
                        List<PageEntry> list = new ArrayList<>();
                        list.add(new PageEntry(pdfFile.getName(), (i + 1), freq.getValue()));
                        index.put(freq.getKey(), list);
                    }

                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        // тут реализуйте поиск по слову
        String lowerCaseWord = word.toLowerCase(Locale.ROOT);
        if (!(index.containsKey(lowerCaseWord))) return Collections.emptyList();
        return index.get(lowerCaseWord);
    }
}
