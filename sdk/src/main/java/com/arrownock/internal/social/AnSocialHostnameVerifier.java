package com.arrownock.internal.social;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500Principal;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import com.arrownock.internal.util.DistinguishedNameParser;

/**
 * A HostnameVerifier consistent with <a
 * href="http://www.ietf.org/rfc/rfc2818.txt">RFC 2818</a>.
 * 
 * @hide accessible via HttpsURLConnection.getMRMHostnameVerifier()
 */
public final class AnSocialHostnameVerifier implements X509HostnameVerifier {

    @Override
    public final boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certificates = session.getPeerCertificates();
            verify(host, (X509Certificate) certificates[0]);
        } catch (SSLException e) {
            return false;
        }
        return true;
    }

    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
        // TODO Auto-generated method stub

    }

    // @Override
    // public boolean verify(String host, X509Certificate certificate) {
    // Matcher matcher = ipPattern.matcher(host);
    // return matcher.matches() ? verifyIpAddress(host, certificate)
    // : verifyHostName(host, certificate);
    // }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        // TODO Auto-generated method stub

    }

    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;

    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final Pattern ipPattern = Pattern.compile(IPADDRESS_PATTERN);

    /**
     * Returns true if {@code certificate} matches {@code ipAddress}.
     */
    private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
        for (String altName : getSubjectAltNames(certificate, ALT_IPA_NAME)) {
            if (ipAddress.equalsIgnoreCase(altName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if {@code certificate} matches {@code hostName}.
     */
    private boolean verifyHostName(String hostName, X509Certificate certificate) {
        hostName = hostName.toLowerCase(Locale.US);
        boolean hasDns = false;
        for (String altName : getSubjectAltNames(certificate, ALT_DNS_NAME)) {
            hasDns = true;
            if (verifyHostName(hostName, altName)) {
                return true;
            }
        }

        if (!hasDns) {
            X500Principal principal = certificate.getSubjectX500Principal();
            String cn = new DistinguishedNameParser(principal).find("cn");
            if (cn != null) {
                return verifyHostName(hostName, cn);
            }
        }

        return false;
    }

    private List<String> getSubjectAltNames(X509Certificate certificate, int type) {
        List<String> result = new ArrayList<String>();
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                return Collections.emptyList();
            }
            for (Object subjectAltName : subjectAltNames) {
                List<?> entry = (List<?>) subjectAltName;
                if (entry == null || entry.size() < 2) {
                    continue;
                }
                Integer altNameType = (Integer) entry.get(0);
                if (altNameType == null) {
                    continue;
                }
                if (altNameType == type) {
                    String altName = (String) entry.get(1);
                    if (altName != null) {
                        result.add(altName);
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns true if {@code hostName} matches the name or pattern {@code cn}.
     * 
     * @param hostName
     *            lowercase host name.
     * @param cn
     *            certificate host name. May include wildcards like
     *            {@code *.android.com}.
     */
    public boolean verifyHostName(String hostName, String cn) {
        if (hostName == null || hostName.isEmpty() || cn == null || cn.isEmpty()) {
            return false;
        }

        cn = cn.toLowerCase(Locale.US);

        if (!cn.contains("*")) {
            return hostName.equals(cn);
        }

        if (cn.startsWith("*.") && hostName.regionMatches(0, cn, 2, cn.length() - 2)) {
            return true; // "*.foo.com" matches "foo.com"
        }

        int asterisk = cn.indexOf('*');
        int dot = cn.indexOf('.');
        if (asterisk > dot) {
            return false; // malformed; wildcard must be in the first part of
            // the cn
        }

        if (!hostName.regionMatches(0, cn, 0, asterisk)) {
            return false; // prefix before '*' doesn't match
        }

        int suffixLength = cn.length() - (asterisk + 1);
        int suffixStart = hostName.length() - suffixLength;
        if (hostName.indexOf('.', asterisk) < suffixStart) {
            // TODO: remove workaround for *.clients.google.com http://b/5426333
            if (!hostName.endsWith(".clients.google.com")) {
                return false; // wildcard '*' can't match a '.'
            }
        }

        if (!hostName.regionMatches(suffixStart, cn, asterisk + 1, suffixLength)) {
            return false; // suffix after '*' doesn't match
        }

        return true;
    }

    @Override
    public void verify(String host, X509Certificate certificate) throws SSLException {
        Matcher matcher = ipPattern.matcher(host);
        boolean x = matcher.matches() ? verifyIpAddress(host, certificate) : verifyHostName(host, certificate);
        if (!x)
            throw new SSLException("not verified");
    }

}
