package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.Before;
import org.junit.Test;

public class RuntimeTypeAdapterFactoryTest {
  private Gson gson;
  private RuntimeTypeAdapterFactory<BillingInstrument> factory;

  @Before
  public void setUp() {
    factory =
        RuntimeTypeAdapterFactory.builder(BillingInstrument.class)
            .build()
            .registerSubtype(CreditCard.class)
            .registerSubtype(BankTransfer.class);

    gson = new GsonBuilder().registerTypeAdapterFactory(factory).create();
  }

  @Test
  public void testBasicSerialization() {
    CreditCard original = new CreditCard("Jesse", 234);
    String json = gson.toJson(original, BillingInstrument.class);

    assertThat(json).isEqualTo("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}");
  }

  @Test
  public void testBasicDeserialization() {
    BillingInstrument deserialized =
        gson.fromJson(
            "{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}",
            BillingInstrument.class);

    assertThat(deserialized).isInstanceOf(CreditCard.class);
    assertThat(((CreditCard) deserialized).cvv).isEqualTo(234);
    assertThat(deserialized.ownerName).isEqualTo("Jesse");
  }

  @Test
  public void testMissingTypeField() {
    JsonParseException e =
        assertThrows(
            JsonParseException.class,
            () -> gson.fromJson("{\"ownerName\":\"Jesse\"}", BillingInstrument.class));
    assertThat(e).hasMessageThat().contains("Missing type field: type");
  }

  @Test
  public void testUnregisteredType() {
    JsonParseException e =
        assertThrows(
            JsonParseException.class,
            () ->
                gson.fromJson(
                    "{\"type\":\"UnknownType\",\"ownerName\":\"Jesse\"}", BillingInstrument.class));
    assertThat(e).hasMessageThat().contains("Unknown type: UnknownType");
  }

  static class BillingInstrument {
    String ownerName;

    BillingInstrument(String ownerName) {
      this.ownerName = ownerName;
    }
  }

  static class CreditCard extends BillingInstrument {
    int cvv;

    CreditCard(String ownerName, int cvv) {
      super(ownerName);
      this.cvv = cvv;
    }
  }

  static class BankTransfer extends BillingInstrument {
    String accountNumber;

    BankTransfer(String ownerName, String accountNumber) {
      super(ownerName);
      this.accountNumber = accountNumber;
    }
  }
}
