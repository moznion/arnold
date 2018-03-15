# Arnold

[![Build Status](https://travis-ci.org/moznion/arnold.svg?branch=master)](https://travis-ci.org/moznion/arnold)

Arnold is a builder (and the generator) that guarantees all mandatory elements for instantiation.

This library uses annotation processor, so it generates the builder automatically on annotation
processing phase if `@ArnoldBuilder` annotation is marked.

## Synopsis

### Basic usage

```java
import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class Example {
    @Required
    private String foo;
    @Required
    private int bar;
    @Required
    private double buz;
}
```

Then build the project, it generates builder(s). You can use the builder like so;

```java
Example example = new ExampleBuilder().foo("foo")
                                      .bar(42)
                                      .buz(2.71828)
                                      .build();
```

### With `final` field

```java
import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class Example {
    private final String foo;
    private final int bar;
    private final double buz;
    
    /**
     * This constructor is dummy. ArnoldBuilder requires no-arg constructor for instantiate.
     */
    Example() {
        this.foo = "";
        this.bar = 0;
        this.buz = 0.0;
    }
}
```

Then;

```java
Example example = new ExampleBuilder().foo("foo")
                                      .bar(42)
                                      .buz(2.71828)
                                      .build();
```

The `final` field is treated as a necessary element even if `@Required` is not given.

### Specify the order

You can specify the order of appearance the fields in the builder.

```java
import net.moznion.arnold.annotation.ArnoldBuilder;
import net.moznion.arnold.annotation.Required;

@ArnoldBuilder
public class Example {
    @Required(order = 2)
    private String foo;
    @Required(order = 0)
    private int bar;
    @Required
    private double buz;
    @Required(order = 1)
    private boolean qux;
}
```

Then;

```java
Example example = new ExampleBuilder().buz(2.71828) // order -1 (not specified)
                                      .bar(42)      // order 0
                                      .qux(true)    // order 1
                                      .foo("foo")   // order 2
                                      .build();
```

Lower values are processed first. If the same value is given, the one defined earlier will be
processed first.

### Note!

ArnoldBuilder requires the default (no-arg) constructor in the target class for instantiation.

## Motivation

In a general builder (such as a builder that lombok generates) it is inconvenient, as it could potentially drop out the necessary elements.
For example;

```java
public class Example {
    private final String foo;
    private final String bar;
    private final String buz;

    private Example(final String foo, final String bar, final String buz) {
        this.foo = foo;
        this.bar = bar;
        this.buz = buz;
    }

    public static class Builder {
        private String foo;
        private String bar;
        private String buz;

        public Builder foo(final String foo) {
            this.foo = foo;
            return this;
        }

        public Builder bar(final String bar) {
            this.bar = bar;
            return this;
        }

        public Builder buz(final String buz) {
            this.buz = buz;
            return this;
        }

        public Example build() {
            return new Example(foo, bar, buz);
        }
    }
}
```

This builder works well, but...

```java
new Builder().foo("foo")
             .buz("buz")
             .build(); // bar is missing!
```

It can create incomplete instances like this.

Arnold is developed to solve these problems. This library generates a Builder Class that guarantees
all necessary elements in the phase of annotation Processing.

## How does it work

Arnold uses annotation processor.

This library generates as many classes as the number of mandatory elements. The following example 
will be easy to understand.

```java
new ExampleBuilder()       // => ExampleBuilder: receives `foo`
             .foo("foo")   // => ExampleBuilder1: receives `bar`
             .bar(42)      // => ExampleBuilder2: receives `buz`
             .buz(2.71828) // => ExampleBuilder3: buildable
             .build();     // => instance!
```

In other words, it generates one class for one required element.
This may be a rich solution depending on the environment.

## FAQ

### Why not expand the lombok

I tried it first, but it was a hard way to expand the lombok (that is complicated!).
When implementing a single feature simply as this library, it is easier to implement as an
individual library.

## Author

moznion (<moznion@gmail.com>)

## License

```
The MIT License (MIT)
Copyright © 2018 moznion, http://moznion.net/ <moznion@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
