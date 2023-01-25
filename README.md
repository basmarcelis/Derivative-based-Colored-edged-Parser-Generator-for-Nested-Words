# Derivative-based-Colored-edged-Parser-Generator-for-Nested-Words

This repository contains a parser generator for nested words. It uses derivates to accept ambiguous grammars and return all possible derivations. 
Additionally, error handling is provided by an intuitive approach on accepting pending calls based on Colored Nested Words.

JUnitTest.java contains tests of multiple grammars. This file can be used to create parsing for a new VPG as well.

Grammars are required to be in the following well-matched form:
1)  L => e;                       //Epsilon rule
2)  L_0 => "a" L_1;               //Regular rule
3)  L_0 => [ "(" L_1 ")" ] L_2;   //Nesting rule

Additionally, "|" is the _or_ operator for these grammar rules.
JUnitTest.java contains multiple examples of grammars in this form.
