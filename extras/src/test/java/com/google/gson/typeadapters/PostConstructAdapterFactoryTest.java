/*
 * Copyright (C) 2016 Gson Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Test;

public class PostConstructAdapterFactoryTest {
  private Gson gson;

  @Before
  public void setUp() {
    gson = new GsonBuilder().registerTypeAdapterFactory(new PostConstructAdapterFactory()).create();
  }

  @Test
  public void testValidSandwichDeserialization() {
    Sandwich sandwich =
        gson.fromJson("{\"bread\":\"white\",\"cheese\":\"cheddar\"}", Sandwich.class);

    assertThat(sandwich.bread).isEqualTo("white");
    assertThat(sandwich.cheese).isEqualTo("cheddar");
  }

  @Test
  public void testInvalidSandwichThrowsException() {
    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                gson.fromJson(
                    "{\"bread\":\"cheesey bread\",\"cheese\":\"swiss\"}", Sandwich.class));

    assertThat(e).hasMessageThat().isEqualTo("too cheesey");
  }

  @Test
  public void testMultipleSandwichesSerialization() {
    MultipleSandwiches sandwiches =
        new MultipleSandwiches(
            Arrays.asList(new Sandwich("white", "cheddar"), new Sandwich("whole wheat", "swiss")));

    String json = gson.toJson(sandwiches);

    assertThat(json)
        .isEqualTo(
            "{\"sandwiches\":[{\"bread\":\"white\",\"cheese\":\"cheddar\"},"
                + "{\"bread\":\"whole wheat\",\"cheese\":\"swiss\"}]}");
  }

  @Test
  public void testMultipleSandwichesDeserialization() {
    MultipleSandwiches original =
        new MultipleSandwiches(
            Arrays.asList(new Sandwich("white", "cheddar"), new Sandwich("whole wheat", "swiss")));

    String json = gson.toJson(original);
    MultipleSandwiches deserialized = gson.fromJson(json, MultipleSandwiches.class);

    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  public void testNullInputHandling() {
    Sandwich sandwich = gson.fromJson("null", Sandwich.class);
    assertThat(sandwich).isNull();
  }

  static class Sandwich {
    String bread;
    String cheese;

    public Sandwich(String bread, String cheese) {
      this.bread = bread;
      this.cheese = cheese;
    }

    @PostConstruct
    private void validate() {
      if (Objects.equals(bread, "cheesey bread") && cheese != null) {
        throw new IllegalArgumentException("too cheesey");
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Sandwich)) {
        return false;
      }
      Sandwich other = (Sandwich) o;
      return Objects.equals(bread, other.bread) && Objects.equals(cheese, other.cheese);
    }

    @Override
    public int hashCode() {
      return Objects.hash(bread, cheese);
    }
  }

  static class MultipleSandwiches {
    List<Sandwich> sandwiches;

    public MultipleSandwiches(List<Sandwich> sandwiches) {
      this.sandwiches = sandwiches;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof MultipleSandwiches)) {
        return false;
      }
      MultipleSandwiches other = (MultipleSandwiches) o;
      return Objects.equals(sandwiches, other.sandwiches);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sandwiches);
    }
  }
}
