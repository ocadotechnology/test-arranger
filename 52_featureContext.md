# Feature: Add support for EnumSet

## Goal

Currently instantiation of `EnumSet` ends up with `java.lang.InstantiationError: java.util.EnumSet` thrown from
`org.objenesis.instantiator.sun.SunReflectionFactoryInstantiator`, reached through
`org.jeasy.random.util.ReflectionUtils.createEmptyCollectionForType` and `org.jeasy.random.CollectionPopulator`.

(GitHub issue #52: https://github.com/ocadotechnology/test-arranger/issues/52,
related upstream issue: https://github.com/j-easy/easy-random/issues/489)

`EnumSet` is an abstract class with package-private implementations and no accessible constructor, so the generic
collection handling of easy-random (reflective no-arg constructor with an Objenesis fallback) cannot work for it.
The only supported way of creating one is `EnumSet.noneOf(enumType)`, which requires knowing the enum type.

## Decisions

- Solve it with a new `RandomizerRegistry` (`EnumSetRandomizerRegistry`) instead of patching the collection handling.
  Reasoning: `FieldPopulator` asks the randomizer provider **before** it dispatches to `CollectionPopulator`, so a
  registry short-circuits the failing path without touching easy-random internals. Rejected alternatives: forking
  `CollectionPopulator` (would mean maintaining a copy of an easy-random class) and registering a per-type randomizer
  with `parameters.randomize(EnumSet.class, ...)` (a type-based randomizer does not know the enum type parameter).
- The registry resolves the enum from the **field declaration** (`Field#getGenericType`), extracted by the small
  `EnumTypeExtractor` helper. Reasoning: the enum type is erased at runtime, the declaration is the only place it is
  available.
- `getRandomizer(Class<?> type)` always returns `null`. Reasoning: without a field declaration the enum type cannot be
  derived, so `Arranger.some(EnumSet.class)` keeps failing - that call has no meaningful result anyway.
- The size of the generated set respects `EasyRandomParameters.collectionSizeRange` (read in `init`), clamped to the
  number of enum constants. Reasoning: consistency with every other collection arranged by test-arranger, including
  `someSimplified` which uses a narrower range. A set larger than the number of constants is impossible, hence the
  clamping.
- The registry gets `@Priority(-3)`, the same as `CustomArrangerRandomizerRegistry`, and is registered next to it in
  `EnhancedRandom.addRandomizersToParameters` with the same seed. Reasoning: keeps the arranged data reproducible for
  a fixed seed and puts the registry in the same position in the priority chain, which is below bean validation and
  above the easy-random internal registry.
- Raw `EnumSet` fields (declared without a type parameter) are left to the previous behaviour, i.e. they still fail.
  Reasoning: there is nothing to derive the enum from, and a raw declaration is a defect in the tested code rather
  than a case worth supporting.

## Constraints

- Must not change how any other collection type is arranged - the registry returns `null` for everything that is not
  assignable to `EnumSet`.
- The existing behaviour of skipping an `EnumSet` field with an override (`ArrangerEnumSetTest.skipEnumSet`) has to
  keep working; overrides exclude the field from generation, so the registry is never asked for it.
- Reproducibility: the generated set has to be stable for a fixed seed, so the shuffling uses a seeded `java.util.Random`
  owned by the registry, never `Math.random()`.

## Open questions

-

## Status

- [x] `EnumSetRandomizerRegistry` - resolves the enum from the field declaration and builds the set with
      `EnumSet.noneOf` plus a seeded shuffle of the constants
- [x] `EnumTypeExtractor` - extracts the enum type parameter from a generic declaration
- [x] Registered in `EnhancedRandom.addRandomizersToParameters`
- [x] `ArrangerEnumSetTest` extended: plain field, record component, nested object, variety across many instances,
      enum with fewer constants than the collection size; the pre-existing override tests are untouched
- [x] `EnumSetRandomizerRegistryTest` covers: EnumSet field, raw EnumSet field, other collection / non-collection
      fields, lookup by type, collection size range
- [x] README: note in the `Arranger` section
- [x] Full test suite green (176 tests), no regression
