package com.vladsch.flexmark.internal;

import com.vladsch.flexmark.ast.Block;
import com.vladsch.flexmark.ast.BlockContent;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.options.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FencedCodeBlockParser extends AbstractBlockParser {

    private static final Pattern OPENING_FENCE = Pattern.compile("^`{3,}(?!.*`)|^~{3,}(?!.*~)");
    private static final Pattern CLOSING_FENCE = Pattern.compile("^(?:`{3,}|~{3,})(?=[ \t]*$)");

    private final FencedCodeBlock block = new FencedCodeBlock();
    private BlockContent content = new BlockContent();
    private char fenceChar;
    private int fenceLength;
    private int fenceIndent;
    private final boolean matchingCloser;

    public FencedCodeBlockParser(DataHolder options, char fenceChar, int fenceLength, int fenceIndent) {
        this.fenceChar = fenceChar;
        this.fenceLength = fenceLength;
        this.fenceIndent = fenceIndent;
        this.matchingCloser = options.get(Parser.MATCH_CLOSING_FENCE_CHARACTERS);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        int newIndex = state.getIndex();
        BasedSequence line = state.getLine();
        Matcher matcher;
        boolean matches = (state.getIndent() <= 3 &&
                nextNonSpace < line.length() &&
                (!matchingCloser || line.charAt(nextNonSpace) == fenceChar));

        if (matches) {
            BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
            matcher = CLOSING_FENCE.matcher(trySequence);
            if (matcher.find()) {
                int foundFenceLength = matcher.group(0).length();

                if (foundFenceLength >= fenceLength) {
                    // closing fence - we're at end of line, so we can finalize now
                    block.setClosingMarker(trySequence.subSequence(0, foundFenceLength));
                    return BlockContinue.finished();
                }
            }
        }
        // skip optional spaces of fence indent
        int i = fenceIndent;
        while (i > 0 && newIndex < line.length() && line.charAt(newIndex) == ' ') {
            newIndex++;
            i--;
        }
        return BlockContinue.atIndex(newIndex);
    }

    @Override
    public void addLine(ParserState state, BasedSequence line) {
        content.add(line, state.getIndent());
    }

    @Override
    public boolean isPropagatingLastBlankLine(BlockParser lastMatchedBlockParser) {
        return false;
    }

    @Override
    public void closeBlock(ParserState state) {
        // first line, if not blank, has the info string
        List<BasedSequence> lines = content.getLines();
        if (lines.size() > 0) {
            BasedSequence info = lines.get(0);
            if (!info.isBlank()) {
                block.setInfo(info.trim());
            }

            BasedSequence chars = content.getSpanningChars();
            BasedSequence spanningChars = chars.baseSubSequence(chars.getStartOffset(), lines.get(0).getEndOffset());

            if (lines.size() > 1) {
                // have more lines
                block.setContent(spanningChars, lines.subList(1, lines.size()));
            } else {
                block.setContent(spanningChars, BasedSequence.EMPTY_LIST);
            }
        } else {
            block.setContent(content);
        }

        block.setCharsFromContent();
        content = null;
    }

    public static class Factory implements CustomBlockParserFactory {
        @Override
        public Set<Class<? extends CustomBlockParserFactory>> getAfterDependents() {
            return new HashSet<>(Arrays.asList(
                    BlockQuoteParser.Factory.class,
                    HeadingParser.Factory.class
                    //FencedCodeBlockParser.Factory.class,
                    //HtmlBlockParser.Factory.class,
                    //ThematicBreakParser.Factory.class,
                    //ListBlockParser.Factory.class,
                    //IndentedCodeBlockParser.Factory.class
            ));
        }

        @Override
        public Set<Class<? extends CustomBlockParserFactory>> getBeforeDependents() {
            return new HashSet<>(Arrays.asList(
                    //BlockQuoteParser.Factory.class,
                    //HeadingParser.Factory.class,
                    //FencedCodeBlockParser.Factory.class,
                    HtmlBlockParser.Factory.class,
                    ThematicBreakParser.Factory.class,
                    ListBlockParser.Factory.class,
                    IndentedCodeBlockParser.Factory.class
            ));
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public BlockParserFactory create(DataHolder options) {
            return new BlockFactory(options);
        }
    }

    private static class BlockFactory extends AbstractBlockParserFactory {
        private BlockFactory(DataHolder options) {
            super(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            BasedSequence line = state.getLine();
            Matcher matcher;
            if (state.getIndent() < 4) {
                BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
                if ((matcher = OPENING_FENCE.matcher(trySequence)).find()) {
                    int fenceLength = matcher.group(0).length();
                    char fenceChar = matcher.group(0).charAt(0);
                    FencedCodeBlockParser blockParser = new FencedCodeBlockParser(state.getProperties(), fenceChar, fenceLength, state.getIndent());
                    blockParser.block.setOpeningMarker(trySequence.subSequence(0, fenceLength));
                    return BlockStart.of(blockParser).atIndex(nextNonSpace + fenceLength);
                }
            }
            return BlockStart.none();
        }
    }
}

