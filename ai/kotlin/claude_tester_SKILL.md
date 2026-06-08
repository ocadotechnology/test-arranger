---
name: tester
description: test creation guidelines and conventions; use when user asks to create new tests or update existing ones
---

# Testing Guidelines (Project Conventions)

## Test structure

### Instruction

-   Always use the **given / when / then** pattern.
-   Use the exact section comments:
    -   `// given`
    -   `// when`
    -   `// then`
-   Keep the `given` section minimal: set only fields relevant to the scenario.
-   Use the test-arranger top-level functions (`some<T>()`, `someObjects<T>(n)`, …) to create test data, avoid mocks and hardcoded values.
-   Assertions should reflect **behavior and intent**, not incidental object details.

Practical rules for generated tests
-   Do not manually construct large domain objects unless necessary.
-   Generate with `some<X>()` first.
-   Override only scenario-relevant fields, preferring the mutable DSL or `data class` `copy()` over override maps.
-   Use property references (`Type::field`) with Rearranger so overrides stay refactor-safe.
-   Prefer comparing against arranged values instead of hardcoded constants unless the constant is the test's purpose.
-   Keep tests concise and intention-revealing.

### Example

``` kotlin
@Test
fun `should create report for product brand`() {
    // given
    val product = some<Product>()

    // when
    val report = sut.createBrandReport(listOf(product))

    // then
    assertThat(report.brand).isEqualTo(product.brand)
}
```

## Test data generation (default: test-arranger)

### Instruction

**By default, use test-arranger to generate test data.**
Do not hand-build large objects with constructors unless the test truly depends on specific values.
Avoid using mocks, prefer instances filled with random data by the test-arranger.

#### Basic usage (Kotlin)

The Kotlin API is a set of top-level functions imported from `com.ocadotechnology.gembus.test`.
The generic ones (`some<X>()`, `someObjects<X>(n)`, `someMatching<X>()`) are `reified`/`inline`, so you pass the type as a type argument.
You do not write `Arranger.some(X.class)` as in Java — you use the wrapping functions:

-   `some<X>()` → fully populated random instance
-   `some<X>("fieldName")` → instance with given field unset (varargs: `some<X>("a", "b")`)
-   `someObjects<X>(n)` → a `Sequence` of `n` instances
-   `someMatching<X>({ predicate })` → an instance satisfying the predicate
-   `someEmail()`, `someLong()`, `someInt()`, `someText()`, `someString()`, `someBoolean()`
-   `someFrom(list)` → random element from a collection

> Note: `someObjects<X>(n)` returns a `Sequence<X>`. Call `.toList()` when you need a `List`.

### Example

``` kotlin
// given
val product = some<Product>()
product.brand = "VIP"
```

## Adjusting arranged data

### Instruction

Prefer expressing intent by modifying only the fields that matter.

1.  **Mutable DSL** (preferred when fields are `var`) — assign fields in a trailing lambda; each is set after random population.
2.  **Kotlin `data class` `copy()`** — when the type is a data class.
3.  **Override map** — for immutable, non-data types. Keys are field names, values are suppliers (`() -> Any`); also accepted by `someObjects<X>(n, overrides)`.

### Example

#### 1. Mutable adjustment via the DSL (preferred when fields are mutable)

``` kotlin
val product = some<Product> {
    brand = "VIP"
}
```

You can set as many fields as needed inside the block, but every assigned property must be mutable (`var`).

#### 2. Kotlin data class `copy()`

``` kotlin
val product = some<Product>().copy(brand = "VIP")
```

#### 3. Override map (for immutable / non-data types)

``` kotlin
val product = some<Product>(
    mapOf(
        "brand" to { "VIP" },
        "price" to { BigDecimal.TEN }
    )
)
```

## Rearranger (copy + selective overrides)

### Instruction

Use the Kotlin **Rearranger** DSL (`com.ocadotechnology.gembus.test.rearrangerkt.Rearranger`) when you already have a valid instance and want to tweak a few fields, and the class is immutable, not a `data class`, or otherwise lacks a convenient `copy()`.
The DSL uses **property references** (`Type::property`), which are refactor-safe and IDE-friendly (renames are tracked).

When to prefer Rearranger
-   You want to start from a valid domain object
-   Only a few fields differ
-   You want to keep the rest realistic and consistent
-   The type is not a `data class` (otherwise just use `copy()`)

Important
-   Rearranger performs a **shallow copy**: nested mutable objects are shared between original and copy.
-   For types without a matching constructor, the instance is created without running its constructor body (Objenesis); restore invariants via overrides if the constructor logic matters.
-   Override inherited properties through the **most specific (child)** property reference, e.g. `Child::name`, not the parent's.

### Example

``` kotlin
val original = some<User>()

// given
val admin = Rearranger.copy(original) {
    User::role set "ADMIN"
    User::active set true
}
```

## Custom Arrangers (encode invariants once)

### Instruction

If random-by-type generation violates domain rules, create a custom arranger by extending the Java `CustomArranger<T>` base class.
Override `instance()` and use the inherited `enhancedRandom` field.

Rules:
-   Use custom arrangers when invariants must always hold.
-   Add well-named factory methods only when tests require specific variants.
-   `some<Product>()` will automatically use `ProductArranger` if present (picked up by reflection), including indirectly — e.g. when arranging a `Shop` that contains a `List<Product>`.
-   Custom arrangers are discovered by scanning the package configured with `arranger.root` (default `com.ocado`). Set it in `arranger.properties` on the test classpath.

### Example

``` kotlin
class ProductArranger : CustomArranger<Product>() {

    override fun instance(): Product {
        val product = enhancedRandom.nextObject(Product::class.java)
        return product.copy(
            price = BigDecimal.valueOf(somePositiveLong(9_999L))
        )
    }
}
```

## When to use what

| Situation | Prefer |
|-----------|--------|
| Need a fresh random object | `some<X>()` |
| Need multiple objects | `someObjects<X>(n)` |
| Mutable type, tweak a few fields | `some<X> { field = value }` |
| Kotlin `data class`, tweak a few fields | `some<X>().copy(field = value)` |
| Immutable non-data type, override fields at creation | `some<X>(mapOf("field" to { value }))` |
| Start from a valid instance and tweak (immutable/no `copy()`) | `Rearranger.copy(original) { X::field set value }` |
| Domain invariants must always hold in the test entity | `CustomArranger<T>` |
| Reuse complex multi-entity setup | `Fixture` |

------------------------------------------------------------------------

## Fixtures (reuse complex setups)

### Instruction

Use fixtures when tests repeatedly require a specific constellation of multiple related objects.

A fixture:
- Creates multiple domain objects
- Links them correctly
- Hides setup complexity
- Expresses domain meaning

Use the `Fixture` suffix for such classes.

Guidelines:
-   Use fixtures for **reused object graphs**, not simple objects.
-   Keep fixture methods well-named and domain-oriented.
-   Internally use the `some*` functions and custom arrangers.
-   Avoid duplicating complex setup logic across tests.

### Example

``` kotlin
class ShopFixture(private val repository: Repository) {

    fun shopWithNineProductsAndFourCustomers() {
        someObjects<Product>(9).forEach(repository::save)
        someObjects<Customer>(4).forEach(repository::save)
    }
}
```