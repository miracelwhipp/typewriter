package io.github.miracelwhipp.typewriter.conversion.markdown;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import java.util.regex.Pattern;

public class MarkdownToXhtmlConversion extends MarkdownConversion {

    public MarkdownToXhtmlConversion(boolean debug) {
        super(debug);
    }

    @Override
    public Pattern targetExtension() {
        return Pattern.compile("xhtml");
    }

    @Override
    protected void setTransformerOutputProperties(Transformer transformer) {

        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.1//EN");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.1");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    }
}
