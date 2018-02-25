package org.adho.dhconvalidator.conversion;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.properties.PropertyKey;

public enum SubmissionLanguage {
  ENGLISH("Bibliography", "template.language.description.en", PropertyKey.templateFileEN),
  GERMAN("Bibliographie", "template.language.description.de", PropertyKey.templateFileDE),
  ;
  private String bibliographyTranslation;
  private String languageDescriptionKey;
  private PropertyKey templatePropertyKey;

  private SubmissionLanguage(
      String bibliographyTranslation,
      String languageDescriptionKey,
      PropertyKey templatePropertyKey) {
    this.bibliographyTranslation = bibliographyTranslation;
    this.languageDescriptionKey = languageDescriptionKey;
    this.templatePropertyKey = templatePropertyKey;
  }

  @Override
  public String toString() {
    return Messages.getString(languageDescriptionKey);
  }

  public String getBibliographyTranslation() {
    return bibliographyTranslation;
  }

  public PropertyKey getTemplatePropertyKey() {
    return templatePropertyKey;
  }
}
