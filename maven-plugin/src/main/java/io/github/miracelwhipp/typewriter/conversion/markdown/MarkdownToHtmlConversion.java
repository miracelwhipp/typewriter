package io.github.miracelwhipp.typewriter.conversion.markdown;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import java.util.regex.Pattern;

public class MarkdownToHtmlConversion extends MarkdownConversion {

    public MarkdownToHtmlConversion(boolean debug) {
        super(debug);
    }


    @Override
    public Pattern targetExtension() {
        return Pattern.compile("html|htm");
    }


    @Override
    protected void setTransformerOutputProperties(Transformer transformer) {


        //            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01 Transitional//EN");
//            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/html4/loose.dtd");
//            transformer.setOutputProperty(OutputKeys.METHOD, "html");
//            transformer.setOutputProperty(OutputKeys.VERSION, "4.01");
//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.VERSION, "5.0");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    }
}
