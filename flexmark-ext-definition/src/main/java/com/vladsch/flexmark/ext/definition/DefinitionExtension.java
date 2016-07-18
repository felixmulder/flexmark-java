
package com.vladsch.flexmark.ext.definition;

import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.internal.util.options.DataKey;
import com.vladsch.flexmark.parser.Parser;

/**
 * Extension for definitions
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link com.vladsch.flexmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link com.vladsch.flexmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed definition text is turned into {@link DefinitionList}, {@link DefinitionTerm} and {@link DefinitionItem} nodes.
 * </p>
 */
public class DefinitionExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    // final public static DataKey<DefinitionRepository> DEFINITIONS = new DataKey<>("DEFINITIONS", DefinitionRepository::new); 
    // final public static DataKey<KeepType> DEFINITIONS_KEEP = new DataKey<>("DEFINITIONS_KEEP", KeepType.FIRST); // standard option to allow control over how to handle duplicates 
    final public static DataKey<Boolean> NO_TERM_TRAILING_COLON = new DataKey<>("NO_TERM_TRAILING_COLON", false); 

    private DefinitionExtension() {
    }

    public static Extension create() {
        return new DefinitionExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        //parserBuilder.customBlockParserFactory(new DefinitionBlockParser.Factory());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        //rendererBuilder.nodeRendererFactory(DefinitionNodeRenderer::new);
        // rendererBuilder.linkResolverFactory(new DefinitionLinkResolver.Factory());
    }
}
