# Feature: Make the language of markov chain configurable

## Goal

It could be whatever exists in the classpath, i.e. a solution on top of #28. Some additional languages should have
predefined Markov chains (pl, de...).

(GitHub issue #51: https://github.com/ocadotechnology/test-arranger/issues/51)

Before the change `MarkovStringRandomizer` held a `static MarkovChain markov = new MarkovChain("enMarkovChain")`,
i.e. the language was hardcoded and the only way to change the generated text was to shadow the `enMarkovChain`
resource on the test classpath.

## Decisions

- Configuration through a new property `arranger.text.language`, default `en`, resolved to the classpath resource
  `{language}MarkovChain`. Reasoning: consistent with the rest of the configuration (`arranger.*` properties read by
  `PropertiesWrapper`) and with the already used `enMarkovChain` naming, and it implements "whatever exists in the
  classpath" from the issue - a chain delivered by any jar (see #28) is picked up with no further change.
- Property named `arranger.text.language` rather than `arranger.markov.language`. Reasoning: it describes the effect
  (the language of `someText()`), not the implementation; the Markov chain is an implementation detail of `someText()`.
- New `MarkovChainProvider` caches the parsed chains per language in a `ConcurrentHashMap`. Reasoning: parsing a chain
  file takes noticeable time and `Arranger.someText(min, max)` builds a new randomizer on every call.
- `MarkovStringRandomizer.markov` changed from a `static` field to an instance field. Reasoning: the property has to be
  read per randomizer for the configuration to have any effect, and the previous package-private constructor assigned
  the static field, so a test that used a word based chain silently replaced the chain for every later caller in the
  same JVM. Rejected alternative: keeping the field static and reloading it when the property changes - global mutable
  state that is hard to reason about in concurrent tests.
- Predefined chains for `pl` and `de` are generated as character level bigram chains, in the same format and the same
  way as the existing `enMarkovChain` (see the disabled `MarkovChainTest.generateChainOfChars`). Reasoning: a
  character level chain is what `someText()` expects.
- The corpora are passages of novel prose reconstructed from memory in the manner of public domain novelists - for
  Polish Sienkiewicz, Prus, Reymont, Zeromski and Orzeszkowa, for German Kafka, Fontane, Goethe, Keller and Thomas
  Mann (in the public domain in the EU since January 2026). Reasoning: literary prose gives a richer and more
  characteristic distribution of letters than plain contemporary sentences. The passages are not verbatim quotations
  and are skewed by the reconstruction, which does not matter for a bigram model. Rejected alternative: Lem and Grass,
  proposed as style references but still in copyright (died 2006 and 2015).
- Corpus size is about 11k characters for `pl` and 12.5k for `de`, giving 33 and 31 states and 519 and 411 distinct
  bigrams. Reasoning: a bigram model over roughly 30 states saturates at this order of magnitude, so a larger corpus
  would add little. The shipped `enMarkovChain` is still considerably richer, as it was trained on a much larger text.
- The German corpus contains a passage with `q`, `x` and `y` words on purpose, so that the chain covers the whole
  German alphabet; `q`, `v` and `x` are deliberately absent from the Polish chain as they are not native Polish
  letters.
- The generated `pl` and `de` chains contain letters and spaces only, no punctuation. Reasoning: punctuation in these
  corpora is nearly always followed by a space, which produced Markov nodes with no transition to a non blank value
  (see the constraint below).
- `MarkovStringRandomizer.getTrimmed` no longer loops forever when a node has no transition to a non blank value; after
  a number of attempts it continues from a randomly selected node and, if the whole chain turns out to generate blanks
  only, it fails with a descriptive `IllegalStateException`. Reasoning: this is a pre-existing defect - the shipped
  `enMarkovChain` has such nodes for `%` and `+` - and it becomes much easier to hit once arbitrary chains can be
  plugged in, which is the point of this issue.
- `MarkovChain` reports a missing chain file with `IllegalStateException` naming the file, and reads it as UTF-8
  explicitly. Reasoning: a typo in the language property used to end up as a `NullPointerException` with no hint; the
  explicit charset is needed because the new chains contain non ASCII characters and the platform default charset
  cannot be relied on.

## Constraints

- A Markov chain node must have at least one transition to a non blank value, otherwise text generation cannot
  progress from it. Shipped chains must satisfy this; for chains delivered by users the randomizer degrades to picking
  a random node instead of hanging.
- The chain file format stays unchanged: `##key##-->>probability##target##probability##target##...`, one line per
  state, as parsed by `MarkovChainParser`.
- `Arranger.someText()` (the no-arg variant) uses a randomizer created when the `Arranger` class is loaded, so setting
  the property as a system property afterwards does not affect it; `arranger.properties` does, and so does
  `someText(min, max)`. This matches how every other property of test-arranger behaves.
- `src/test/resources/enMarkovChain` shadows the real English chain on the test classpath, so tests of the shipped
  chains have to use `pl`/`de`.

## Open questions

-

## Status

- [x] `arranger.text.language` property added to `PropertiesWrapper` (default `en`)
- [x] `MarkovChainProvider` resolves and caches the chain of the configured language
- [x] `MarkovStringRandomizer` holds the chain per instance and takes it from the provider
- [x] `MarkovChain` fails with a descriptive message on a missing file and reads UTF-8
- [x] `MarkovStringRandomizer.getTrimmed` hardened against nodes with no transition to a non blank value
- [x] Predefined `plMarkovChain` and `deMarkovChain` added to `src/main/resources`
- [x] `MarkovChainLanguageTest` covers: text in the configured language, language specific characters, the English
      default, a missing chain file, caching per language
- [x] `MarkovStringRandomizerTest` extended with a chain containing a node with no transition to a non blank value
      (`src/test/resources/deadEndMarkovChain`)
- [x] `PropertiesWrapperTest` covers reading the new property (set / not set)
- [x] README: the `someText()` row in the `Arranger` table and a new entry in the `Properties` section
- [x] Full test suite green (186 tests), no regression
