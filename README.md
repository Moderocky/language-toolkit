Language Toolkit
=====

An adaptable toolkit for building context-lite programming language parsers.

### Opus #29

## Introduction

This resource is an adaptation of utilities from several of my previous language-centric projects.
Its purpose is to turn source code into a reified 'model' of the resulting program,
which in turn can be fed to an assembler.

### N-Stage Compilers

In the course of my work in creating interpreters and compilers for programming languages,
I have found breaking the process down into discrete stages to be the most helpful approach.

Each stage turns an input into a more-structured output.

The first input and final output are fixed: reading a stream of bytes, representing unfiltered user input,
and writing the fully assembled program (or executing it).
Everything in between can be broken down into granular stages, for example,

- converting the source stream to characters,
- converting character groups to lexical elements,
- converting lexical tokens into grammar elements,
- creating a program structure out of grammar tokens,
- contextualising intra-program references (abbreviations, calls, types, variables, etc.)
- and feeding the contextualised model into the assembler.

Clearly, there will be slightly more boilerplate involved in making each step a self-contained unit,
however this approach has two significant advantages.

#### 1. Approachability

Making a compiler is difficult.
In almost any programming language, the source code looks nothing like what the machine needs to interpret.
Getting from one to the other in a useful way is a complicated task,
made more difficult by the approach described in most of the seminal literature looking nothing like any modern
compiler.
Many of the processes are described in an abstract way that,
while sounding simple, are whole projects in themselves.

Building a compiler in stages is equivalent to asking several smaller questions in place of one big one.
As a result, it is much more approachable.
Each stage involves turning data into a slightly more manageable form:
either by restructuring it in a way that is easier to interact with,
by identifying what it is,
or by collecting insights into its components that will be useful in later stages.

'Which variable locations are written to in this function?' is not an easy question to answer about a string of text.
What part of the text is the function? Where are the variable names within the text?
What determines whether a variable is being read from or written to?
It is much easier to answer this question when given a structural model representing the function,
where each element has been identified and its contextual metadata is available to us.

#### 2. Adaptability

Todo.

## Stages

### Lexer

### Tokeniser

### Parser

### Model

## Pattern Parsing

An easy-access tool for constructing patterns (for use with the standard lexer) is available out-of-the-box.

This `Pattern` tool allows a developer to construct something akin to a Backus-Naur Form grammar programmatically,
which can then parse raw code and feed this into the model-building stage directly.

### Pattern Structure

Instead of regular expressions or text, patterns are built out of code elements.

A pattern is made from elements.
```
pattern(elements...)
```

Elements are text sequences, but many types from the toolkit can also be used in a pattern for more complex behaviour.

#### Keywords

Case-sensitive keywords can be selected with simple strings.
The pattern below will match the text input `keyword` exactly.
```
pattern("keyword")
```

```bnf
::= keyword
```

#### Brackets

Utilities for round, square and curly brackets are provided.
There is also a method for other kinds of braces (although the user must provide the starting and ending character).
This helper is designed to select matching pairs of brackets.

```
pattern(round(element))
pattern(square(element))
pattern(curly(element))
pattern(brackets('^', '$', element))
```

```bnf
::= ( <element> )
::= [ <element> ]
::= { <element> }
::= ^ <element> $
```

The output of a bracket section is an `Input` containing anything parsed inside the bracket pair.

#### Token Words

For cases when the content of a word is needed directly, the provided `WORD` selector will help.

```
pattern(WORD)
pattern("keyword", WORD, "keyword")
pattern("def", WORD)
```

```bnf
::= <word>
::= keyword <word> keyword
::= def <word>
```

The word selector puts the raw text of the word into the `Input`.

#### Token Numbers

The `NUMBER` selector provides a number value as input.

```
pattern(NUMBER)
pattern("keyword", NUMBER, "keyword")
pattern("int", NUMBER)
```

```bnf
::= <number>
::= keyword <number> keyword
::= int <number>
```

The number selector puts the raw value of the number into the `Input`.

#### Unit

A `Unit` reference can be used to parse a unit from the same grammar.
This is vulnerable to infinite left-recursion.

```
pattern(myUnit)
pattern("keyword", myUnit)
pattern("if", expression, ":")
```

```bnf
::= <myUnit>
::= keyword <myUnit>
::= if <expression> :
```

The unit selector puts the model, wrapped in an `Input`, into the `Input`.

#### Repeating Phrase

A repeating phrase can be captured.
Repeats accept zero or more of the phrase.

```
pattern(repeat(WORD))
pattern("a", repeat(WORD), "house")
```

```bnf
r ::= <word> <r> | <>
```

The results of the repeat parsing are stored in a single `Input`, which is put into the `Input`.

#### Comma-separated Repeating Phrase

```
pattern(csv(WORD))
pattern(csv(WORD, "keyword"))
```

```bnf
c ::= <word>, <c> | <word>
```

The results of the repeat parsing are stored in a single `Input`, which is put into the `Input`.

#### Left-Recursive Phrase

Rudimentary support is included for parsing left-recursive phrases,
e.g. something of the form `expr = <expr> + <expr> | <number>`.
Typically, this would fail to parse, since the left-hand `<expr>` would always attempt to match itself,
until the machine runs out of resources.

A left-recursive phrase begins with its non-recursive pattern,
e.g. `expr = <number> + <number>`,
and then applies a special assembling function.

```
pattern(pattern(NUMBER, "+", NUMBER).leftRecursive(
    input -> new Sum(input.next(), input.next())
))
```

```bnf
::= <number> + <number> | <expr> + <number>
```

Note that this is not typically an issue with grammars,
however the pattern-consumer does _not_ need to parse all available tokens,
so would always select the shortest form.

Left-recursion takes greedily, e.g. `5 + 5 + 5` would be parsed as `((5 + 5) + 5)`,
rather than stopping after the initial `5 + 5` (and then having no match for the remainder).

#### Right-Recursive Phrase

Right-recursion is naturally supported by the grammar and has no issue.
As such, there is no special syntax for right-recursion.

Right-recursion has a natural limit of several hundred attempts (based on the machine stack size).
















