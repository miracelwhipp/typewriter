package io.github.miracelwhipp.typewriter.library;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.xwiki.macros.MacroExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import freemarker.core.Environment;
import freemarker.template.*;
import io.github.miracelwhipp.typewriter.spi.DataModelProvider;
import io.github.miracelwhipp.typewriter.spi.EvaluationParameters;
import io.github.miracelwhipp.typewriter.spi.SingletonDataModelProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

public class MarkdownDirective implements TemplateDirectiveModel {

    @Override
    public void execute(
            Environment env,
            Map params,
            TemplateModel[] loopVars,
            TemplateDirectiveBody body
    ) throws TemplateException, IOException {

        if (!params.isEmpty()) {

            throw new TemplateModelException("This directive doesn't allow parameters.");
        }
        if (loopVars.length != 0) {

            throw new TemplateModelException("This directive doesn't allow loop variables.");
        }

        if (body == null) {

            return;
        }

        try (StringWriter writer = new StringWriter()) {

            body.render(writer);

            String nested = writer.toString();

            MutableDataSet options = new MutableDataSet();

            options.set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    MacroExtension.create()
            ));

            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            Node document = parser.parse(nested);

            String html = renderer.render(document);

            env.getOut().write(html);
        }
    }

    public static class Provider extends SingletonDataModelProvider {

        @Override
        public String name() {
            return "markdown";
        }

        @Override
        protected Object makeDataModel(Map<String, String> customConfiguration, EvaluationParameters evaluationParameters) {
            return new MarkdownDirective();
        }
    }
}
