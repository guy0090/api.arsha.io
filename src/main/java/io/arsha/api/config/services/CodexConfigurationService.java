package io.arsha.api.config.services;

import io.arsha.api.config.properties.BdoCodexProperties;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CodexConfigurationService {

    private final BdoCodexProperties bdoCodexProperties;

    public String getUrl() {
        return bdoCodexProperties.getUrl();
    }

    public URI getItemsEndpoint(String locale) {
        if (!isValidLocale(locale)) throw new IllegalArgumentException("Invalid locale: " + locale);

        var path = String.format("https://%s/query.php?a=items&l=%s", getUrl(), locale.toLowerCase());
        return URI.create(path);
    }

    public boolean isValidLocale(String locale) {
        return bdoCodexProperties.getLocales().contains(locale.toLowerCase());
    }

    public List<String> getLocales() {
        return bdoCodexProperties.getLocales();
    }
}
