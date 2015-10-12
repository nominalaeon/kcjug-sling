
package com.github.bstopp.kcjug.sling.twitter.service;

import java.io.IOException;

import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.EntitySupport;
import twitter4j.HashtagEntity;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Formats a Tweet into an HTML representation.
 *
 * @author Bryan Stopp
 * @since 1.0
 */
public class TweetFormatter {

    private static final Logger LOG         = LoggerFactory.getLogger(TweetFormatter.class);

    public static final String  HASHTAG_URL = TwitterApiService.TWITTER_URL + "hashtag/";
    public static final String  SYMBOL_URL  = TwitterApiService.TWITTER_URL + "search?q=";
    public static final String  AMPERSAND   = "@";
    public static final String  HASHTAG     = "#";

    public String formatText(String originalText, EntitySupport entitySupport) {
        String text = originalText;
        text = formatUrl(text, originalText, entitySupport.getURLEntities());
        text = formatUrl(text, originalText, entitySupport.getMediaEntities());
        text = formatUserMentions(text, originalText, entitySupport.getUserMentionEntities());
        text = formatHashTag(text, originalText, entitySupport.getHashtagEntities());
        text = formatSymbol(text, originalText, entitySupport.getSymbolEntities());
        text = formatEmoji(text);
        return text;
    }

    public String formatEmoji(String text) {
        LOG.debug("TweetFormatter - formatEmoji " + text);
        UnicodeEscaper privateRange = UnicodeEscaper.between(0xE000, 0xF8FF);
        UnicodeEscaper firstRange = UnicodeEscaper.between(0x1F300, 0x1F64F);
        UnicodeEscaper secondRange = UnicodeEscaper.between(0x1F680, 0x1F6FF);
        UnicodeEscaper thirdRange = UnicodeEscaper.between(0x2600, 0x27BF);

        StringBuilder stringbuilder = new StringBuilder(text.length());

        try {

            for (int i = 0; i < text.length(); i++) {
                int codepoint = text.codePointAt(i);
                if (privateRange.translate(codepoint, NullWriter.NULL_WRITER)) {
                    continue;
                } else if (firstRange.translate(codepoint, NullWriter.NULL_WRITER)
                        || secondRange.translate(codepoint, NullWriter.NULL_WRITER)
                        || thirdRange.translate(codepoint, NullWriter.NULL_WRITER)) {
                    i++;
                    continue;
                }
                stringbuilder.append((char) codepoint);
            }
        } catch (IOException e) {
            LOG.error("Tweet Formatter - Format Emoji Error : ", e);
        }

        return stringbuilder.toString();
    }

    public String formatUrl(String text, String originalText, URLEntity[] entities) {
        LOG.debug("TweetFormatter - formatUrl " + text);
        for (URLEntity urlentity : entities) {
            int end = urlentity.getEnd();
            if (urlentity.getEnd() > originalText.length()) {
                end = originalText.length();
            }
            int start = urlentity.getStart();
            if (start < 0) {
                start = 0;
            }
            String originalString = originalText.substring(start, end);
            String displayText = urlentity.getDisplayURL();
            // replace ellipsis character which doesnt save properly to jcr.
            displayText = displayText.replaceAll("\u2026", "&hellip;");

            String anchor = createAnchor(urlentity.getURL(), displayText, "twitter-link");
            text = text.replaceAll(originalString, anchor);
        }
        return text;
    }

    public String formatUserMentions(String text, String originalText, UserMentionEntity[] entities) {
        LOG.debug("TweetFormatter - formatUserMentions " + text);
        for (UserMentionEntity usermentionentity : entities) {
            int end = usermentionentity.getEnd();
            if (usermentionentity.getEnd() > originalText.length()) {
                end = originalText.length();
            }
            int start = usermentionentity.getStart();
            if (start < 0) {
                start = 0;
            }
            String originalString = originalText.substring(start, end);
            String display = AMPERSAND + usermentionentity.getText();
            String anchor = createAnchor(TwitterApiService.TWITTER_URL + usermentionentity.getScreenName(), display,
                    "twitter-user-link");
            text = text.replaceAll(originalString, anchor);
        }
        return text;
    }

    public String formatHashTag(String text, String originalText, HashtagEntity[] entities) {
        LOG.debug("TweetFormatter - formatHashTag " + text);
        for (HashtagEntity hashtagentity : entities) {
            int end = hashtagentity.getEnd();
            if (hashtagentity.getEnd() > originalText.length()) {
                end = originalText.length();
            }
            int start = hashtagentity.getStart();
            if (start < 0) {
                start = 0;
            }
            String originalString = originalText.substring(start, end);

            String display = HASHTAG + hashtagentity.getText();
            String anchor = createAnchor(HASHTAG_URL + hashtagentity.getText(), display, "twitter-hashtag-link");
            text = text.replaceAll(originalString, anchor);
        }
        return text;
    }

    public String formatSymbol(String text, String originalText, SymbolEntity[] entities) {
        LOG.debug("TweetFormatter - formatSymbol " + text);
        for (SymbolEntity symbolentity : entities) {
            int end = symbolentity.getEnd();
            if (symbolentity.getEnd() > originalText.length()) {
                end = originalText.length();
            }
            int start = symbolentity.getStart();
            if (start < 0) {
                start = 0;
            }
            String originalString = originalText.substring(start, end);
            String display = symbolentity.getText();
            String anchor = createAnchor(SYMBOL_URL + display, display, "twitter-symbol-link");
            text = text.replaceAll(originalString, anchor);
        }
        return text;
    }

    public String createAnchor(String url, String display, String clazz) {
        LOG.debug("TweetFormatter - createAchor " + url);
        String displayUrl = StringUtils.defaultIfBlank(display, url);

        StringBuilder stringbuilder = new StringBuilder(256);
        stringbuilder.append("<a href='").append(url).append("' ");
        stringbuilder.append("class='").append(clazz).append("' ");
        stringbuilder.append("title='").append(url).append("' ");
        stringbuilder.append("target='_blank' ");
        stringbuilder.append(">");
        stringbuilder.append(displayUrl);
        stringbuilder.append("</a>");
        return stringbuilder.toString();
    }

}
