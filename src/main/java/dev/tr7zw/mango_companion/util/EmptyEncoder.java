package dev.tr7zw.mango_companion.util;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import java.lang.reflect.Type;

public class EmptyEncoder implements Encoder {

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template)
            throws EncodeException {}
}
