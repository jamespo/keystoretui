package keystoretui;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class KeystoreService {
    private final KeyStore keyStore;

    public KeystoreService(String path, String password) throws Exception {
        String type = "JKS";
        if (path.toLowerCase().endsWith(".p12") || path.toLowerCase().endsWith(".pfx")) {
            type = "PKCS12";
        }
        keyStore = KeyStore.getInstance(type);
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        }
    }

    public List<String> getAliases() throws KeyStoreException {
        Enumeration<String> enumeration = keyStore.aliases();
        List<String> aliases = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            aliases.add(enumeration.nextElement());
        }
        Collections.sort(aliases);
        return aliases;
    }

    public String getEntryDetails(String alias) throws KeyStoreException {
        if (!keyStore.containsAlias(alias)) {
            return "Alias not found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Alias: ").append(alias).append("\n");
        if (keyStore.isCertificateEntry(alias)) {
            sb.append("Type: Certificate Entry\n");
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            sb.append("Subject: ").append(cert.getSubjectX500Principal()).append("\n");
            sb.append("Issuer: ").append(cert.getIssuerX500Principal()).append("\n");
            sb.append("Valid from: ").append(cert.getNotBefore()).append("\n");
            sb.append("Valid until: ").append(cert.getNotAfter()).append("\n");
            sb.append("Serial Number: ").append(cert.getSerialNumber()).append("\n");
            sb.append("Signature Algorithm: ").append(cert.getSigAlgName()).append("\n");
        } else if (keyStore.isKeyEntry(alias)) {
            sb.append("Type: Key Entry\n");
            // Can't easily get key details without password for entry, but we can show it's a key
        } else {
            sb.append("Type: Unknown\n");
        }
        return sb.toString();
    }
}
