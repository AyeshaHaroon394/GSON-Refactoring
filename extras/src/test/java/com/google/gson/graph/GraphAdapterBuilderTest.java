package com.google.gson.graph;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/** Tests for {@link GraphAdapterBuilder} and its graph serialization functionality. */
public final class GraphAdapterBuilderTest {

  @Test
  public void testBasicCyclicSerialization() {
    // Setup cyclic structure
    Roshambo rock = createRockPaperScissorsGraph();

    // Create Gson with graph adapter
    Gson gson = createGsonWithGraphAdapter(Roshambo.class);

    // Verify serialization format
    String json = gson.toJson(rock).replace('"', '\'');
    assertThat(json)
        .isEqualTo(
            "{'0x1':{'name':'ROCK','beats':'0x2'},"
                + "'0x2':{'name':'SCISSORS','beats':'0x3'},"
                + "'0x3':{'name':'PAPER','beats':'0x1'}}");
  }

  @Test
  public void testBasicCyclicDeserialization() {
    String json =
        "{'0x1':{'name':'ROCK','beats':'0x2'},"
            + "'0x2':{'name':'SCISSORS','beats':'0x3'},"
            + "'0x3':{'name':'PAPER','beats':'0x1'}}";

    Gson gson = createGsonWithGraphAdapter(Roshambo.class);
    Roshambo rock = gson.fromJson(json, Roshambo.class);

    verifyRockPaperScissorsGraph(rock);
  }

  @Test
  public void testSelfReferencingDeserialization() {
    String json = "{'0x1':{'name':'SUICIDE','beats':'0x1'}}";
    Gson gson = createGsonWithGraphAdapter(Roshambo.class);

    Roshambo suicide = gson.fromJson(json, Roshambo.class);
    assertThat(suicide.name).isEqualTo("SUICIDE");
    assertThat(suicide.beats).isSameInstanceAs(suicide);
  }

  @Test
  public void testCustomInstanceCreator() {
    // Setup builder with custom instance creator
    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Company.class, type -> new Company("custom"))
        .addType(Employee.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    // Test deserialization with custom creator
    String json = "{'0x1':{'employees':['0x2']},'0x2':{'name':'Jesse','company':'0x1'}}";
    Company company = gson.fromJson(json, Company.class);

    // Verify results
    assertThat(company.name).isEqualTo("custom");
    Employee employee = company.employees.get(0);
    assertThat(employee.name).isEqualTo("Jesse");
    assertThat(employee.company).isSameInstanceAs(company);
  }

  @Test
  public void testInstanceCreatorOverride() {
    // Test overriding with another custom creator
    Gson gson1 = createGsonWithCustomCompanyCreator("custom-2");
    Company company = gson1.fromJson("{'0x1':{}}", Company.class);
    assertThat(company.name).isEqualTo("custom-2");

    // Test overriding with default creator
    Gson gson2 = createGsonWithDefaultCompanyCreator();
    company = gson2.fromJson("{'0x1':{}}", Company.class);
    assertThat(company.name).isNull();
  }

  @Test
  public void testGenericListSerialization() {
    // Setup nested list structure
    Type listOfListsType = new TypeToken<List<List<?>>>() {}.getType();
    Type listOfAnyType = new TypeToken<List<?>>() {}.getType();
    List<List<?>> listOfLists = createSelfReferencingList();

    // Create Gson with graph adapter for lists
    Gson gson = createGsonWithGraphAdapter(listOfListsType, listOfAnyType);

    // Verify serialization
    String json = gson.toJson(listOfLists, listOfListsType);
    assertThat(json.replace('"', '\'')).isEqualTo("{'0x1':['0x1','0x2'],'0x2':[]}");
  }

  @Test
  public void testGenericListDeserialization() {
    // Setup types and create Gson
    Type listOfListsType = new TypeToken<List<List<?>>>() {}.getType();
    Type listOfAnyType = new TypeToken<List<?>>() {}.getType();
    Gson gson = createGsonWithGraphAdapter(listOfListsType, listOfAnyType);

    // Test deserialization
    String json = "{'0x1':['0x1','0x2'],'0x2':[]}";
    List<List<?>> listOfLists = gson.fromJson(json, listOfListsType);

    // Verify structure
    assertThat(listOfLists).hasSize(2);
    assertThat(listOfLists.get(0)).isSameInstanceAs(listOfLists);
    assertThat(listOfLists.get(1)).isEmpty();
  }

  @Test
  public void testComplexObjectGraphSerialization() {
    // Setup company with employees
    Company google = createCompanyWithEmployees();

    // Create Gson with graph adapter
    Gson gson = createGsonWithGraphAdapter(Company.class, Employee.class);

    // Verify serialization
    String json = gson.toJson(google).replace('"', '\'');
    assertThat(json)
        .isEqualTo(
            "{'0x1':{'name':'Google','employees':['0x2','0x3']},"
                + "'0x2':{'name':'Jesse','company':'0x1'},"
                + "'0x3':{'name':'Joel','company':'0x1'}}");
  }

  @Test
  public void testComplexObjectGraphDeserialization() {
    String json =
        "{'0x1':{'name':'Google','employees':['0x2','0x3']},"
            + "'0x2':{'name':'Jesse','company':'0x1'},"
            + "'0x3':{'name':'Joel','company':'0x1'}}";

    Gson gson = createGsonWithGraphAdapter(Company.class, Employee.class);
    Company company = gson.fromJson(json, Company.class);

    verifyCompanyWithEmployees(company);
  }

  @Test
  public void testBuilderReuse() {
    // Initial setup
    GraphAdapterBuilder builder =
        new GraphAdapterBuilder()
            .addType(Company.class, type -> new Company("custom"))
            .addType(Employee.class);

    // Create first Gson instance
    GsonBuilder gsonBuilder1 = new GsonBuilder();
    builder.registerOn(gsonBuilder1);
    Gson gson1 = gsonBuilder1.create();

    // Verify first instance
    Company company1 = gson1.fromJson("{'0x1':{}}", Company.class);
    assertThat(company1.name).isEqualTo("custom");

    // Modify builder and create second Gson instance
    GsonBuilder gsonBuilder2 = new GsonBuilder();
    builder.addType(Company.class, type -> new Company("custom-2"));
    builder.registerOn(gsonBuilder2);
    Gson gson2 = gsonBuilder2.create();

    // Verify second instance
    Company company2 = gson2.fromJson("{'0x1':{}}", Company.class);
    assertThat(company2.name).isEqualTo("custom-2");

    // Verify first instance wasn't affected
    company1 = gson1.fromJson("{'0x1':{}}", Company.class);
    assertThat(company1.name).isEqualTo("custom");
  }

  // Helper methods for test setup
  private Roshambo createRockPaperScissorsGraph() {
    Roshambo rock = new Roshambo("ROCK");
    Roshambo scissors = new Roshambo("SCISSORS");
    Roshambo paper = new Roshambo("PAPER");
    rock.beats = scissors;
    scissors.beats = paper;
    paper.beats = rock;
    return rock;
  }

  private void verifyRockPaperScissorsGraph(Roshambo rock) {
    assertThat(rock.name).isEqualTo("ROCK");
    Roshambo scissors = rock.beats;
    assertThat(scissors.name).isEqualTo("SCISSORS");
    Roshambo paper = scissors.beats;
    assertThat(paper.name).isEqualTo("PAPER");
    assertThat(paper.beats).isSameInstanceAs(rock);
  }

  private Company createCompanyWithEmployees() {
    Company company = new Company("Google");
    new Employee("Jesse", company);
    new Employee("Joel", company);
    return company;
  }

  private void verifyCompanyWithEmployees(Company company) {
    assertThat(company.name).isEqualTo("Google");
    Employee jesse = company.employees.get(0);
    assertThat(jesse.name).isEqualTo("Jesse");
    assertThat(jesse.company).isSameInstanceAs(company);
    Employee joel = company.employees.get(1);
    assertThat(joel.name).isEqualTo("Joel");
    assertThat(joel.company).isSameInstanceAs(company);
  }

  private List<List<?>> createSelfReferencingList() {
    List<List<?>> listOfLists = new ArrayList<>();
    listOfLists.add(listOfLists);
    listOfLists.add(new ArrayList<>());
    return listOfLists;
  }

  private Gson createGsonWithGraphAdapter(Type... types) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    GraphAdapterBuilder graphBuilder = new GraphAdapterBuilder();
    for (Type type : types) {
      graphBuilder.addType(type);
    }
    graphBuilder.registerOn(gsonBuilder);
    return gsonBuilder.create();
  }

  private Gson createGsonWithCustomCompanyCreator(String name) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Company.class, type -> new Company(name))
        .addType(Employee.class)
        .registerOn(gsonBuilder);
    return gsonBuilder.create();
  }

  private Gson createGsonWithDefaultCompanyCreator() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Company.class)
        .addType(Employee.class)
        .registerOn(gsonBuilder);
    return gsonBuilder.create();
  }

  // Test classes
  static class Roshambo {
    String name;
    Roshambo beats;

    Roshambo(String name) {
      this.name = name;
    }
  }

  static class Employee {
    final String name;
    final Company company;

    Employee(String name, Company company) {
      this.name = name;
      this.company = company;
      this.company.employees.add(this);
    }
  }

  static class Company {
    final String name;
    final List<Employee> employees = new ArrayList<>();

    Company(String name) {
      this.name = name;
    }
  }
}
