package dev.tr7zw.mango_companion.parser;

import dev.tr7zw.mango_companion.util.parser.AbstractMadaraParser;
import dev.tr7zw.mango_companion.util.parser.AbstractMadaraParser.MadaraSite;

public class Mangatx extends AbstractMadaraParser implements MadaraSite {

  @Override
  public MadaraSite getTarget() {
    return this;
  }

  @Override
  public String name() {
    return "Mangatx";
  }

  @Override
  public String url() {
    return "https://mangatx.com";
  }
}
