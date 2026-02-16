package com.example.StaySearch.StaySearchBackend.Security;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public final class XssSanitizer {

    private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder().toFactory();

    private XssSanitizer() {
        // utility class
    }

    public static String sanitize(String input) {
        return input == null ? null : STRICT_POLICY.sanitize(input);
    }
}