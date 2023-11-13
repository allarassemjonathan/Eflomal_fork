import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.util.Scanner;

public class SentencePairReader {

    public final String fileName;
    public final boolean reversed;
    private FileObject file1;
    private FileObject file2;
    private Scanner stream1;
    private Scanner stream2;

    /**
     *  <a href="https://commons.apache.org/proper/commons-vfs/commons-vfs2/apidocs/org/apache/commons/vfs2/FileSystemManager.html">...</a>
     *  <a href="https://commons.apache.org/proper/commons-vfs/commons-vfs2/apidocs/org/apache/commons/vfs2/FileObject.html">...</a>
     *  <a href="https://commons.apache.org/proper/commons-vfs/commons-vfs2/apidocs/org/apache/commons/vfs2/FileContent.html">...</a>
     */

    public SentencePairReader(String relativePath) {
        this(relativePath, false);
    }

    /**
     * Creates a new iterator object which reads sentence pairs from the given zip archive
     *
     * @param relativePath -- the relative path to a word alignment data-set
     * @param reversed -- whether the order should be revered in sentence pairs
     */
    public SentencePairReader(String relativePath, boolean reversed) {
        this.fileName = relativePath;
        this.reversed = reversed;
        try {
            // locate the Zip File
            String filePath = System.getProperty("user.dir") + "/" + relativePath;
            // open it with a file system manager
            FileSystemManager fsManager = VFS.getManager();
            FileObject zipFile = fsManager.resolveFile("zip:" + filePath);
            // store the two non-vocab children of this zip file, so they can be used for reading
            FileObject[] fileObjects = zipFile.getChildren();
            for (FileObject fileObject : fileObjects) {
                String name = fileObject.getName().getBaseName();
                if (!name.endsWith(".vocab")) {
                    if (this.file1 == null) {
                        System.out.printf("Source 1: %s\n", fileObject.getName().getBaseName());
                        this.file1 = fileObject;
                    } else {
                        System.out.printf("Source 2: %s\n", fileObject.getName().getBaseName());
                        this.file2 = fileObject;
                        break;
                    }
                }
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            System.err.println("Initialization Failed!");
        }

        // call reset to initialize new Scanners on the input streams from these files
        reset();
    }

    public void reset() {
        close();
        try {
            stream1 = new Scanner(file1.getContent().getInputStream());
            stream2 = new Scanner(file2.getContent().getInputStream());
        } catch (FileSystemException e) {
            System.err.println("Reset Failed!");
        }
    }

    public void close() {
        if (stream1 != null) {
            stream1.close();
        }
        if (stream2 != null) {
            stream2.close();
        }
    }

    public boolean hasNext() {
        return (stream1.hasNextLine() && stream2.hasNextLine());
    }

    public SentencePair next() {
        String langOneLine = stream1.nextLine();
        String langTwoLine = stream2.nextLine();
        Sentence langOneSentence = new Sentence(langOneLine);
        Sentence langTwoSentence = new Sentence((langTwoLine));
        SentencePair pair;
        if (!reversed) {
            pair = new SentencePair(langOneSentence, langTwoSentence);
        } else {
            pair = new SentencePair(langTwoSentence, langOneSentence);
        }
        return pair;
    }
}
