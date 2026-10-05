package com.dervarex.minified.docs;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import java.util.Locale;
import java.util.Set;

/**
 * Doclet that turns the documented sources into the documentation site
 */
public final class MinifiedDoclet implements Doclet {

    /**
     * Set by {@link Main} before the javadoc tool instantiates the doclet reflectively
     */
    static Config config;

    @Override
    public void init(Locale locale, Reporter reporter) {
    }

    @Override
    public String getName() {
        return "minified";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        try {
            Site site = new Site(environment, config);
            site.generate();
            return site.warnings().ok();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
