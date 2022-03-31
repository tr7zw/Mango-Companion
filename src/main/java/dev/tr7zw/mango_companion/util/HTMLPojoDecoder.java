package dev.tr7zw.mango_companion.util;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import pl.droidsonroids.jspoon.HtmlAdapter;
import pl.droidsonroids.jspoon.Jspoon;

public class HTMLPojoDecoder implements Decoder {

    private static final Jspoon jspoon = Jspoon.create();

    @Override
    public Object decode(Response response, Type type)
            throws IOException, DecodeException, FeignException {
        @SuppressWarnings("unchecked")
        HtmlAdapter<Object> adapter = jspoon.adapter((Class<Object>) type);

        InputStream is = response.body().asInputStream();
        try {
            return adapter.fromInputStream(
                    is, StandardCharsets.UTF_8, new URL(response.request().url()));
        } finally {
            is.close();
        }
    }
}
