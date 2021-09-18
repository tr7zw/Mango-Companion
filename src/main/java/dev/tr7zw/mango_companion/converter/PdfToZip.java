package dev.tr7zw.mango_companion.converter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import dev.tr7zw.mango_companion.util.ZipCreator;

public class PdfToZip extends PDFStreamEngine {

    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println("Pass the path to the folder containing the pdfs");
            System.exit(1);
        }
        File folder = new File(args[0]);
        for(File f : folder.listFiles()) {
            if(f.getName().endsWith(".pdf")) {
                System.out.println("Processing " + f.getName());
                File in = f;
                File out = new File(folder, f.getName().replace(".pdf", ".zip"));
                if(out.exists()) {
                    out.delete();
                }
                new PdfToZip().convertPdf(in, out);
                System.out.println("Saved as " + out.getName());
            }
        }
    }

    /**
     * Default constructor.
     *
     * @throws IOException If there is an error loading text stripper properties.
     */
    public PdfToZip() throws IOException {
    }

    public int imageNumber = 1;
    
    private ZipCreator zipCreator;

    /**
     * @param args The command line arguments.
     * @throws Exception 
     */
    public void convertPdf(File pdf, File out) throws Exception {
        PDDocument document = null;
        try (ZipCreator zip = new ZipCreator(out)) {
            this.zipCreator = zip;
            document = Loader.loadPDF(pdf);
            for (PDPage page : document.getPages()) {
                processPage(page);
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if ("Do".equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xobject;

                // same image to local
                zipCreator.addFile(imageNumber + ".png", image.getImage());
                imageNumber++;

            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

}
