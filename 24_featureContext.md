# Feature: Extend support for creating values for fields of abstract type

## Goal

If a field is abstract, e.g. of type `java.lang.Number` and there are concrete types extending it, test-arranger could
create an instance of one of the child types and use it as field value.

(GitHub issue #24: https://github.com/ocadotechnology/test-arranger/issues/24)

Before the change, arranging a class with a field of an abstract class or interface type failed with
`org.jeasy.random.ObjectCreationException: Unable to create a random instance of type ...`, because
`ObjenesisObjectFactory` can only instantiate concrete types.

## Decisions

- Reuse easy-random's built-in `EasyRandomParameters.scanClasspathForConcreteTypes` instead of implementing an own
  subtype lookup. Reasoning: `ObjenesisObjectFactory` (which `DecoratedObjectFactory` already delegates to) resolves the
  abstract type via `ReflectionUtils.getPublicConcreteSubTypesOf` when the flag is on, `classgraph` is already a direct
  dependency of test-arranger, and an own implementation would duplicate that logic with no added value. Rejected
  alternative: a dedicated `RandomizerRegistry` resolving abstract types - more code, and it would not cover the
  `Arranger.some(AbstractType.class)` entry point handled by the object factory.
- Expose the feature through a new property `arranger.scanClasspathForConcreteTypes`, read in `PropertiesWrapper` and
  applied in `ArrangersConfigurer.sharedParameters()`, so it affects both the default and the simplified random.
  Reasoning: every behaviour-changing feature in this project is a property (`arranger.cache.enable`,
  `arranger.overridedefaults`, ...), so this is consistent with the existing configuration style.
- Default value is `false`. Reasoning: turning the flag on makes easy-random's `ClassGraphFacade` scan the whole
  classpath including system jars and modules on first use, which takes noticeable time for every user, also those who
  have no abstract fields. Keeping it off also preserves the current behaviour exactly. Rejected alternative: enabling
  it by default - arguably closer to the issue's wording ("test-arranger could create an instance"), but it would slow
  down every test suite and silently change what existing tests get for interface-typed fields.
- No new public API on `Arranger`/`testUtils.kt`. Reasoning: the feature is about how existing `some()` calls behave,
  not about a new kind of value to arrange.

## Constraints

- Only **public** and non-abstract subtypes are taken into account - that is how easy-random's `ClassGraphFacade`
  filters candidates. Test fixtures for this feature therefore have to be public (nested `public static` classes).
- The default behaviour (property absent) must stay exactly as before, i.e. arranging a field of abstract type fails.
- `MyEasyRandomParameters.copy()` already copies `scanClasspathForConcreteTypes`, so no change was needed there; if a
  new parameter is ever added, that copy method has to be updated as well.
- Properties are read when the `EnhancedRandom` is built. `Arranger.random` is initialized on class load, so a test
  that flips the property has to rebuild via `ArrangersConfigurer.instance().defaultRandom()` (same pattern as
  `ArrangerOverrideDefaultsTest`).

## Open questions

-

## Status

- [x] `arranger.scanClasspathForConcreteTypes` property added to `PropertiesWrapper` (default `false`)
- [x] Property applied in `ArrangersConfigurer.sharedParameters()`
- [x] `ArrangerAbstractTypesTest` covers: abstract class field, interface field, unchanged default behaviour, concrete
      types not affected
- [x] `PropertiesWrapperTest` covers reading the new property (set / not set)
- [x] README: new entry in the `Properties` section
- [x] Full test suite green (166 tests), no regression
