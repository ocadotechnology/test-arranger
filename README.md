# Test Arranger

In TDD there are 3 phases: arrange, act and assert (given, when, then in BDD).
The assert phase has great tool support, you may be familiar with AssertJ, FEST-Assert or Hamcrest.
It is in contrast to the arrange phase.
While arranging test data is often challenging and significant part of test is typically devoted to it, it is hard to point out a tool that supports it. 

Test Arranger tries to fulfill this gap by arranging instances of classes required for tests.
The instances are filled with pseudo random values which simplifies the process of test data creation.
The tester only declares types of the required objects and gets brand new instances.
When pseudo random value for a given field is not good enough, only this field must be set manually:

```java
Product product = Arranger.some(Product.class);
product.setBrand("Ocado");
```

## Features

### Arranger

The Arranger class has a number of static methods for generating pseudo random values of simple types.
Each of them has a wrapping function to make the calls simpler for Kotlin.
Some of the possible calls are listed below:

|Java|Kotlin|result|
|----|------|------|
|```Arranger.some(Product.class)```|```some<Product>()```|instance of Product with all fields filled with values|
|```Arranger.some(Product.class, "brand")```|```some<Product>("brand")```|instance of Product without value for the brand field|
|```Arranger.someSimplified(Category.class)```|```someSimplified<Category>()```|instance of Category, fields of type collection has size reduced to 1 and depth for objects tree is limited to 3|
|```Arranger.someObjects(Product.class, 7)```|```someObjects<Product>(7)```|stream of size 7 of instances of Product|
|```Arranger.someEmail()```|```someEmail()```|string containing email address|
|```Arranger.someLong()```|```someLong()```|pseudo random number of type long|
|```Arranger.someFrom(listOfCategories)```|```someFrom(listOfCategories)```|entry form the listOfCategories|

### Custom Arrangers

By default the random values are generated according to field type.
Random values not always correspond well with class invariants.
When an entity always needs to be arranged with respect to some rules regarding values of fields you may provide a custom arranger:
```java
class ProductArranger extends CustomArranger<Product> {
    @Override
    protected Product instance() {
        Product product = enhancedRandom.nextObject(Parent.class);
        product.setPrice(BigDecimal.valueOf(Arranger.somePositiveLong(9_999L)));
        return product;
    }
}
```
The custom arranger extends the ```CustomArranger``` abstract class and specifies for sake of which type, in the above example it is ```Product```.
In order to have control over the process of instantiating ```Product``` we need to override the ```instance()``` method.
Inside the method we can create the instance of ```Product``` however we want.
Specifically, we may generate some random values.
For convenience we have there an ```enhancedRandom``` field in the ```CustomArranger``` class.
In the given example, we generate an instance of ```Product``` with all fields having pseudo random values, but then we change the price to something acceptable in our domain.
That is not negative and smaller than 10k number.

The ```ProductArranger``` is automatically (using reflection) picked up by Arranger and used whenever new instance of ```Product``` is requested.
It not only regards direct calls like ```Arranger.some(Product.class)```, but also indirect.
Assuming there is class ```Shop``` with field ```products``` of type ```List<Product>```.
When calling ```Arranger.some(Shop.class)```, the arranger will use ```ProductArranger``` to create all the products stored in ```Shop.products```.

The custom arrangers are picked up using reflection.
All classes extending ```CustomArranger``` are considered to be custom arrangers.
The reflection is focused on a certain package which by default is `com.ocado`.
That not necessarily is convenient for you.
If you want to use other package, create `arranger.properties` file and save it in the root of classpath (usually that will be `src/test/resources/` directory).
Inside the file write `arranger.root=your_package`.
Try to have the package as specific as possible as having something to generic (e.g. just `com` which is root package in many libraries) will result in scanning hundreds of classes which will take noticeable time.

## The challenges it solves

When going through tests of a software project one seldom has the impression that it cannot be done better.
In the scope of arranging test data, there are two areas we are trying to improve with Test Arranger.

### Tests readability

Tests are much easier to understand when knowing the intention of the creator, i.e why the test was written and what kind of issues it should detect.
Unfortunately, it is not extraordinary to see tests having in the arrange (given) section statements like the following one:
```java
Product product = Product.builder()
    .withName("Some name")
    .withBrand("Some brand")
    .withPrice(new BigDecimal("12.99"))
    .withCategory("Water, Juice & Drinks / Juice / Fresh")
...
    .build();
```
When looking at such code, it is hard to say which values are relevant for the test and which are provided only to satisfy some not-null requirements.
If the test is about brand, why not write it like that:
```java
Product product = Arranger.some(Product.class);
product.setBrand("Some brand");
```
Now it is obvious that the brand is important.
Let's try to make one step further.
It is possible that the whole test looks as follows:
```java
//arrange
Product product = Arranger.some(Product.class);
product.setBrand("Some brand");

//act
Report actualReport = sut.createBrandReport(Collections.singletonList(product))

//assert
assertThat(actualReport.getBrand).isEqualTo("Some brand") 
```
We're testing now that the report was created for "Some brand" brand.
But is that really the goal?
It makes more sense to expect that the report will be generated for the same brand, the given product is assigned to.
So what we want to test is:
```java
//arrange
Product product = Arranger.some(Product.class);

//act
Report actualReport = sut.createBrandReport(Collections.singletonList(product))

//assert
assertThat(actualReport.getBrand).isEqualTo(product.getBrand()) 
```
In case the brand field is mutable and we're afraid the `sut` may modify it, we can store its value in a variable before going into act phase and later use it for the assertion.
The test will be longer, but the intention remains clear.

It is noteworthy that what we have just did is application of Generated Value and to some extent Creation Method patterns described in *xUnit Test Patterns: Refactoring Test Code* by Gerard Meszaros.

### Shotgun surgery

Have you ever changed some tiny thing in production code and end up with errors in dozen of tests?
Some of them reporting failing assertion, some maybe even refusing to compile.
This is shotgun surgery code smell that just shot at your innocent tests.
Well maybe not so innocent as they could be designed differently, to limit the collateral damage caused by tiny change.
Let's analyse it using an example.
Suppose we have in our domain a following class:
```java
class TimeRange{
    private LocalDateTime start;
    private long durationinMs;

    public TimeRange(LocalDateTime start, long durationInMs) {
        ...
```
and that it is used in many places.
Especially in the tests, without Test Arranger, using statements like this one: ```new TimeRange(LocalDateTime.now(), 3600_000L);```
What will happen if for some important reasons we are be forced to change the class to:
```java
class TimeRange {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeRange(LocalDateTime start, LocalDateTime end) {
        ...
```
It is quite challenging to come up with a series of refactoring that transform the old version to the new one without breaking all dependant tests.
More likely is scenario where the tests are adjusted to new API of the class one by one.
Which means a lot of not exactly exciting work with many questions regarding the desired value of duration (should I carefully convert it to the ```end``` of LocalDateTime type or was it just a convenient random value).
The life would be much easier with Test Arranger.
When in all places requiring just not null ```TimeRange``` we have ```Arranger.some(TimeRange.class)```, it is as good for the new version of ```TimeRange``` as it was for the old one.
That leaves us with those few cases requiring not random ```TimeRange```, but as we already use Test Arranger to reveal test intention, in each case we exactly know what value should be used for the ```TimeRange```.

But, that is not everything we can do to improve the tests.
Presumably, we can identify some categories of the ```TimeRange``` instance, e.g. ranges from past, ranges from future and ranges currently active.
The ```TimeRangeArranger``` is a great place to arrange that:
```java
class TimeRangeArranger extends CustomArranger<TimeRange> {

    private final long MAX_DISTANCE = 999_999L;

    @Override
    protected TimeRange instance() {
        LocalDateTime start = enhancedRandom.nextObject(LocalDateTime.class);
        LocalDateTime end = start.plusHours(Arranger.somePositiveLong(MAX_DISTANCE));
        return new TimeRange(start,end);
    }

    public TimeRange fromPast() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.minusHours(Arranger.somePositiveLong(MAX_DISTANCE));
        return new TimeRange(end.minusHours(Arranger.somePositiveLong(MAX_DISTANCE)), end);
    }

    public TimeRange fromFuture() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(Arranger.somePositiveLong(MAX_DISTANCE));
        return new TimeRange(start, start.plusHours(Arranger.somePositiveLong(MAX_DISTANCE)));
    }

    public TimeRange currentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(Arranger.somePositiveLong(MAX_DISTANCE));
        LocalDateTime end = now.plusHours(Arranger.somePositiveLong(MAX_DISTANCE));
        return new TimeRange(start, end);
    }
}
```
Such creation method should not be created upfront but rather correspond with existing test cases.
Nonetheless, there are chances that the ```TimeRangeArranger``` will cover all cases where instances of ```TimeRange``` are created for tests.
As a consequence, in place of constructor calls with a number of mysterious parameters, we have arranger with well named method explaining the domain meaning of created object and helping in understanding test intention. 

## How to organize tests with Test Arranger

```mermaid
graph TD;
  A-->B;
  A-->C;
  B-->D;
  C-->D;
```
