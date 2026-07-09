# Testing Guidelines (Project Conventions)

## Test structure

- Always use the **given / when / then** pattern.
- Use the exact section comments:
  - `// given`
  - `// when`
  - `// then`
- Keep the `given` section minimal: set only fields relevant to the scenario.
- Use Arranger.some() to create the instances of test data, avoid mocks and hardcoded values.
- Assertions should reflect **behavior and intent**, not incidental object details.

Example:

``` java
@Test
void shouldCreateReportForProductBrand() {
    // given
    Product product = Arranger.some(Product.class);

    // when
    Report report = sut.createBrandReport(List.of(product));

    // then
    assertThat(report.getBrand()).isEqualTo(product.getBrand());
}
```

------------------------------------------------------------------------

## Test data generation (default: Arranger)

**By default, use test-arranger to generate test data.**
Do not hand-build large objects with constructors/builders unless the test truly depends on specific values.
Avoid using mocks, prefer instances filled with random data by the test-arranger.

### Basic usage (Java)

- `Arranger.some(X.class)` → fully populated random instance
- `Arranger.some(X.class, "fieldName")` → instance with given field unset
- `Arranger.someObjects(X.class, n)` → stream of `n` instances
- `Arranger.someEmail()`, `Arranger.someLong()`, `Arranger.someText()`
- `Arranger.someFrom(list)` → random element from list

Example:

``` java
// given
Product product = Arranger.some(Product.class);
product.setBrand("VIP");
```

------------------------------------------------------------------------

## Adjusting arranged data

Prefer expressing intent by modifying only fields that matter.

### 1. Mutate if possible

If the class is mutable:

``` java
Product product = Arranger.some(Product.class);
product.setBrand("VIP");
```

### 2. Use Arranger overrides (for non-mutable types)

``` java
Product product = Arranger.some(Product.class, Map.of(
    "brand", () -> "VIP",
    "price", () -> BigDecimal.TEN
));
```

### Rearranger (copy + selective overrides)

Use **Rearranger** when you already have a valid instance and want to tweak a few fields and the class is immutable and there is no with/toBuilder.

``` java
User original = Arranger.some(User.class);

// given
User admin = Rearranger.copy(original, Map.of(
    "role", () -> "ADMIN",
    "active", () -> true
));
```

#### When to prefer Rearranger

- You want to start from a valid domain object
- Only a few fields differ
- You want to keep the rest realistic and consistent

#### Important

- Rearranger performs a **shallow copy**.
- Nested mutable objects are shared between original and copy.
- Constructor logic may be bypassed in fallback scenarios; restore invariants via overrides if needed.

------------------------------------------------------------------------

## Custom Arrangers (encode invariants once)

If random-by-type generation violates domain rules, create a `CustomArranger<T>`.

``` java
class ProductArranger extends CustomArranger<Product> {

    @Override
    protected Product instance() {
        Product product = enhancedRandom.nextObject(Product.class);
        product.setPrice(
            BigDecimal.valueOf(Arranger.somePositiveLong(9_999L))
        );
        return product;
    }
}
```

Rules:

- Use custom arrangers when invariants must always hold.
- Add well-named factory methods only when tests require specific variants.
- `Arranger.some(X.class)` will automatically use `XArranger` if present.

------------------------------------------------------------------------

## Fixtures (reuse complex setups)

Use fixtures when tests repeatedly require a specific constellation of multiple related objects.

A fixture:

- Creates multiple domain objects
- Links them correctly
- Hides setup complexity
- Expresses domain meaning

Use `Fixture` suffix for such classes.

Example:

``` java
class ShopFixture {

    private final Repository repository;

    ShopFixture(Repository repository) {
        this.repository = repository;
    }

    void shopWithNineProductsAndFourCustomers() {
        Arranger.someObjects(Product.class, 9)
                .forEach(repository::save);

        Arranger.someObjects(Customer.class, 4)
                .forEach(repository::save);
    }
}
```

Guidelines:

- Use fixtures for **reused object graphs**, not simple objects.
- Keep fixture methods well-named and domain-oriented.
- Internally use Arranger and custom arrangers.
- Avoid duplicating complex setup logic across tests.

------------------------------------------------------------------------
## When to use what

Situation Prefer

Need a fresh random object                         `Arranger.some(X.class)`
Need multiple objects                              `Arranger.someObjects(X.class, n)`
Start from valid instance and tweak fields         `Rearranger.copy(...)`
Domain invariants needs tyo be hold in test entity `CustomArranger<T>`
Reuse complex multi-entity setup                   `Fixture`

## Practical rules for generated tests

- Do not manually construct large domain objects unless necessary.
- Generate with `Arranger.some(...)` first.
- Override only scenario-relevant fields.
- Prefer comparing against arranged values instead of hardcoded constants unless the constant is the test's purpose.
- Keep tests concise and intention-revealing.
